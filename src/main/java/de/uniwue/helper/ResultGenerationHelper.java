package de.uniwue.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.P;
import org.docx4j.wml.STBrType;
import org.primaresearch.dla.page.Page;
import org.primaresearch.dla.page.io.FileInput;
import org.primaresearch.dla.page.io.xml.DefaultXmlNames;
import org.primaresearch.dla.page.io.xml.XmlPageReader;
import org.primaresearch.dla.page.layout.physical.Region;
import org.primaresearch.dla.page.layout.physical.shared.RegionType;
import org.primaresearch.dla.page.layout.physical.text.LowLevelTextObject;
import org.primaresearch.dla.page.layout.physical.text.impl.TextContentVariants;
import org.primaresearch.dla.page.layout.physical.text.impl.TextLine;
import org.primaresearch.dla.page.layout.physical.text.impl.TextRegion;
import org.primaresearch.io.UnsupportedFormatVersionException;
import org.primaresearch.shared.variable.IntegerValue;
import org.primaresearch.shared.variable.IntegerVariable;
import org.primaresearch.shared.variable.Variable;

import javax.xml.bind.JAXBException;

public class ResultGenerationHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Object to use generic functionalities
     */
    private GenericHelper genericHelper;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;


    /**
     * Progress of the result generation process
     */
    private int progress = -1;

    /**
     * Indicates if the result generation process should be stopped
     */
    private boolean stopProcess = false;

    /**
     * Structure to monitor the progress of the process
     * pageId : segmentId : lineSegmentId : processedState
     * <p>
     * Structure example:
     * {
     * "0002": {
     * "0002__000__paragraph" : {
     * "0002__000__paragraph__000" : true,
     * "0002__000__paragraph__001" : false,
     * ...
     * },
     * ...
     * },
     * ...
     * }
     */
    private TreeMap<String, TreeMap<String, TreeMap<String, Boolean>>> processState =
            new TreeMap<String, TreeMap<String, TreeMap<String, Boolean>>>();

    /**
     * Constructor
     *
     * @param projectDir       Path to the project directory
     * @param projectImageType Type of the project (binary, gray)
     */
    public ResultGenerationHelper(String projectDir, String projectImageType) {
        projConf = new ProjectConfiguration(projectDir);
        processHandler = new ProcessHandler();
        procStateCol = new ProcessStateCollector(projConf, projectImageType);
        genericHelper = new GenericHelper(projConf);
    }

    /**
     * Gets the process handler object
     *
     * @return Returns the process Helper
     */
    public ProcessHandler getProcessHandler() {
        return processHandler;
    }

    /**
     * Initializes the structure with which the progress of the process can be monitored
     *
     * @param pageIds Identifiers of the chosen pages (e.g 0002,0003)
     * @throws IOException
     */
    public void initialize(List<String> pageIds) throws IOException {
		// Initialize the status structure
		processState = new TreeMap<String, TreeMap<String, TreeMap<String, Boolean>>>();

		for (String pageId : pageIds) {
			TreeMap<String, TreeMap<String, Boolean>> segments = new TreeMap<String, TreeMap<String, Boolean>>();
			processState.put(pageId, segments);
		}
    }

    /**
     * Create necessary Result directories if they do not exist
     */
    private String initializeResultDirectories(String resultType) {
        LocalDateTime localTime = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
                .withLocale( Locale.getDefault() )
                .withZone( ZoneId.systemDefault());
        String time = localTime.format(timeFormatter);
        File resultDir = new File(projConf.RESULT_DIR + time + "_" + resultType + File.separator);
        if (!resultDir.exists())
            resultDir.mkdir();

        if(!resultType.equals("docx")){
            File resultPagesDir = new File(resultDir.getPath() + File.separator + "pages" + File.separator);
            if (!resultPagesDir.exists())
                resultPagesDir.mkdir();
        }
        return time;
    }

    /**
     * Executes result generation process on all specified pages
     *
     * @param pageIds    Identifiers of the pages (e.g 0002,0003)
     * @param resultType specified resultType (txt, xml)
     * @throws IOException
     */
    public void executeProcess(List<String> pageIds,
                               String resultType,
                               String resultStrategy,
                               Boolean preserveEmptyLines,
                               Boolean addPageDelimiter,
                               String customPageDelimiter) throws IOException, UnsupportedFormatVersionException, Docx4JException, JAXBException {
        stopProcess = false;
        progress = 0;

        String initTime = initializeResultDirectories(resultType);

        switch(resultType){
            case "txt":
                executeTextProcess(pageIds, initTime, resultStrategy, preserveEmptyLines, addPageDelimiter, customPageDelimiter);
                break;
            case "xml":
                executeXmlProcess(pageIds, initTime);
                break;
            case "docx":
                executeDocxProcess(pageIds, initTime, resultStrategy, preserveEmptyLines, addPageDelimiter);
                break;
        }

        progress = 100;
    }

    /**
     * Exectues result DOCX generation process on all specified pages
     */
    public void executeDocxProcess(List<String> pageIds,
                                   String time,
                                   String strategy,
                                   Boolean preserveEmptyLines,
                                   Boolean addPageDelimiter) throws Docx4JException, IOException, UnsupportedFormatVersionException {
        initialize(pageIds);

        TreeMap<String, String> pageResult = new TreeMap<>();
        int processElementCount = pageIds.size();
        int processedElements = 0;

        File docxDir = new File(projConf.RESULT_DIR + time + "_docx");
        Files.createDirectories(Paths.get(docxDir.getAbsolutePath()));

        for (String pageId : processState.keySet()) {
            pageResult.put(pageId, "");

            // Retrieve every ground truth or recognition line in the page xmls and group them per page
            populatePageResult(pageId, pageResult, strategy, preserveEmptyLines, addPageDelimiter);

            // Find all textlines inside the file
            processedElements++;
            progress = (int) ((double) processedElements / processElementCount * 100);
        }

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

        for (String pageId : pageResult.keySet()) {
            String currentPageResult = pageResult.get(pageId);
            wordMLPackage.getMainDocumentPart().addStyledParagraphOfText("Title", pageId);
            wordMLPackage.getMainDocumentPart().addParagraphOfText(currentPageResult);

            // Add page break
            Br objBr = new Br();
            objBr.setType(STBrType.PAGE);
            wordMLPackage.getMainDocumentPart().getContent().add(objBr);

        }

        File docxOutFile = new File(docxDir + File.separator + "complete" + ".docx");
        wordMLPackage.save(docxOutFile);

    }

    /**
     * Executes result XML generation process on all specified pages
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void executeXmlProcess(List<String> pageIds, String time) throws IOException {
		File dir = new File(projConf.OCR_DIR);
		if (!dir.exists())
			return;

		initialize(pageIds);
        int processElementCount = pageIds.size();
        int processedElements = 0;

		File xmlDir = new File(projConf.RESULT_DIR + time + "_xml" + File.separator + "pages");
        Files.createDirectories(Paths.get(xmlDir.getAbsolutePath()));

        for(String xmlId : processState.keySet()) {
            File xmlInFile = new File(dir + File.separator + xmlId + projConf.CONF_EXT);
            File xmlOutFile = new File(xmlDir + File.separator +  xmlId + projConf.CONF_EXT);
            Files.copy(xmlInFile.toPath(), xmlOutFile.toPath());

            processedElements++;
            progress = (int) ((double) processedElements / processElementCount * 100);
    	}
    }

    private void populatePageResult(String pageId, TreeMap<String, String> pageResult, String strategy,
                                    Boolean preserveEmptyLines, Boolean addPageDelimiter) throws UnsupportedFormatVersionException {
        XmlPageReader reader = new XmlPageReader(null); // null ^= without validation
        Page page = reader.read(new FileInput(new File(projConf.PAGE_DIR + pageId + projConf.CONF_EXT)));
        List<String> pageString = new ArrayList<>();

        for (Region region : page.getLayout().getRegionsSorted()) {
            RegionType type = (RegionType) region.getType();

            if (type.equals(RegionType.TextRegion)) {
                TextRegion textRegion = (TextRegion) region;

                for (LowLevelTextObject text : textRegion.getTextObjectsSorted()) {
                    if (text instanceof TextLine) {
                        final TextLine textLine = (TextLine) text;

                        //// TextLine text content
                        final Map<Integer,String> content = new HashMap<>();

                        // List of all unindexed text contents
                        final List<String> unindexedContent = new ArrayList<>();
                        int highestIndex = -1;
                        for(int i = 0; i < textLine.getTextContentVariantCount(); i++) {
                            TextContentVariants.TextContentVariant textContent = (TextContentVariants.TextContentVariant) textLine.getTextContentVariant(i);

                            if(textContent.getText() != null) {
                                Variable indexVariable = textContent.getAttributes().get(DefaultXmlNames.ATTR_index);
                                if(indexVariable instanceof IntegerVariable) {
                                    final int index = ((IntegerValue)(indexVariable).getValue()).val;
                                    content.put(index, textContent.getText());
                                    highestIndex = Math.max(index, highestIndex);
                                } else {
                                    unindexedContent.add(textContent.getText());
                                }
                            }
                        }

                        if(content.size() == 0 && unindexedContent.size() == 1) {
                            content.put(1, unindexedContent.get(0));
                        } else {
                            // Give all unindexed content an index starting above the highest recorded index in the bunch (min 0)
                            for(String contentString : unindexedContent) {
                                content.put(++highestIndex, contentString);
                            }
                        }

                        switch(strategy){
                            case "fillUp":
                                if(content.containsKey(0)){
                                    pageString.add(content.get(0));
                                }else if(content.containsKey(1)){
                                    pageString.add(content.get(1));
                                }else{
                                    if(preserveEmptyLines)
                                        pageString.add("");
                                }
                                break;
                            case "gt":
                                if(content.containsKey(0)){
                                    pageString.add(content.get(0));
                                }else{
                                    if(preserveEmptyLines)
                                        pageString.add("");
                                }
                                break;
                            case "pred":
                                if(content.containsKey(1)){
                                    pageString.add(content.get(1));
                                }else{
                                    if(preserveEmptyLines)
                                        pageString.add("");
                                }
                                break;
                        }
                    }
                }
            }
        }

        pageResult.put(pageId, String.join("\n", pageString));
    };

    /**
     * Executes result TXT generation process on all specified pages
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void executeTextProcess(List<String> pageIds,
                                   String time,
                                   String strategy,
                                   Boolean preserveEmptyLines,
                                   Boolean addPageDelimiter,
                                   String customPageDelimiter)
            throws IOException, UnsupportedFormatVersionException {
        initialize(pageIds);

        TreeMap<String, String> pageResult = new TreeMap<>();
        int processElementCount = pageIds.size();
        int processedElements = 0;

        String delimiter = !customPageDelimiter.isEmpty() ? customPageDelimiter.concat(" ") : "### ";

        File textDir = new File(projConf.RESULT_DIR + time + "_txt" + File.separator + "pages");
        Files.createDirectories(Paths.get(textDir.getAbsolutePath()));

        // For each page: Concatenation of the recognition/gt output of the line segmentation of the page
        //                Saving output to a txt file (located at /Results/Pages/)
        for (String pageId : processState.keySet()) {
            pageResult.put(pageId, "");

            // Retrieve every ground truth or recognition line in the page xmls and group them per page
            populatePageResult(pageId, pageResult, strategy, preserveEmptyLines, addPageDelimiter);

            // Find all textlines inside the file
            processedElements++;
            progress = (int) ((double) processedElements / processElementCount * 100);

            try (OutputStreamWriter writer =
                         new OutputStreamWriter(new FileOutputStream(textDir + File.separator + pageId + ".txt"),
                                 StandardCharsets.UTF_8)) {
                writer.write(pageResult.get(pageId));
            }
		}

		// The recognition/gt output of the specified pages is concatenated
		StringBuilder completeResult = new StringBuilder();

        int idx = 0;
		for (String pageId : pageResult.keySet()) {
            if(addPageDelimiter) {
                if (idx == 0) {
                    completeResult.append(delimiter).append(pageId).append("\n\n");
                } else {
                    completeResult.append("\n\n\n").append(delimiter).append(pageId).append("\n\n");
                }
            }else{
                if(idx != 0){
                    completeResult.append("\n\n\n");
                }
            }
            String currentPageResult = pageResult.get(pageId);
			completeResult.append(currentPageResult);
            idx++;
		}
		try (OutputStreamWriter writer =
					 new OutputStreamWriter(new FileOutputStream(projConf.RESULT_DIR + time + "_txt" + File.separator + "complete" + ".txt"), StandardCharsets.UTF_8)) {
			writer.write(completeResult.toString());
		}
    }


    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        if (processHandler != null)
            processHandler.stopProcess();
        stopProcess = true;
    }

    /**
     * Returns the Ids of the pages, for which recognition process was already executed
     *
     * @return List of valid page Ids
     * @throws IOException
     */
    public ArrayList<String> getValidPageIds() throws IOException {
        // Get all pages and check which ones are already region extracted
        ArrayList<String> validPageIds = new ArrayList<String>();
        ArrayList<String> allPageIds = genericHelper.getPageList("Original");
        for (String pageId : allPageIds) {
            if (procStateCol.recognitionState(pageId) || procStateCol.groundTruthState(pageId))
                validPageIds.add(pageId);
        }

        Collections.sort(validPageIds);
        return validPageIds;
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Checks if process depending files already exist
     *
     * @param pageIds    Identifiers of the pages (e.g 0002,0003)
     * @param resultType Type of the result (xml, txt)
     * @return Information if files exist
     */
    public boolean doOldFilesExist(String[] pageIds, String resultType) {
        for (String pageId : pageIds) {
            if (procStateCol.resultGenerationState(pageId, resultType))
                return true;
        }
        return false;
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses) {
        return ProcessConflictDetector.resultConflict(currentProcesses);
    }
}
