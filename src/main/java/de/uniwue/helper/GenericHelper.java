package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import de.uniwue.config.ProjectConfiguration;

/**
 * Helper class for generic controller
 */
public class GenericHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public GenericHelper(String projectDir) {
        projConf = new ProjectConfiguration(projectDir);
    }

    /**
     * Constructor
     *
     * @param projConf Project configuration object
     */
    public GenericHelper(ProjectConfiguration projConf) {
        this.projConf = projConf;
    }

    /**
     * Gets all page IDs of the project
     *
     * @param imageType Type of the images
     * @return Array of page IDs
     * @throws IOException
     */
    public ArrayList<String> getPageList(String imageType) throws IOException {
        ArrayList<String> pageList = new ArrayList<String>();
        if (!new File(projConf.getImageDirectoryByType(imageType)).exists())
            return pageList;

        String imageExtension = projConf.getImageExtensionByType(imageType);
        
        // File depth of 1 -> no recursive (file)listing 
        Files.walk(Paths.get(projConf.getImageDirectoryByType(imageType)), 1)
        .map(Path::toFile)
        .filter(File::isFile)
        .filter(fileEntry -> fileEntry.getName().endsWith(imageExtension))
        .sorted()
        .forEach(
            fileEntry -> { pageList.add(fileEntry.getName().replace(imageExtension, "")); }
        );
        return pageList;
    }

    /**
     * Checks if the directory of given image type exits
     *
     * @param imageType Type of the image directory (original, gray, binary, despeckled, OCR)
     * @return Information if the directory exists
     */
    public boolean checkIfImageDirectoryExists(String imageType) {
        String imageDir = projConf.getImageDirectoryByType(imageType);
        return new File(imageDir).exists();
    }

    /**
     * Checks if images of given image type exit
     *
     * @param imageType Type of the image (original, gray, binary, despeckled, OCR)
     * @return Information if the file type exists
     */
    public boolean checkIfImageTypeExists(String imageType) {
        File imageDir = new File(projConf.PREPROC_DIR);
        String imageTypeExt = projConf.getImageExtensionByType(imageType);

        File[] files = imageDir.listFiles((d, name) -> name.endsWith(imageTypeExt));

        return files.length > 0;
    }

    /**
     * Gets the the number of logical thread of the system
     *
     * @return Number of logical threads
     */
    public static int getLogicalThreadCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Builds a map containing all imgExt for each image in /processing
     *
     * @return Number of logical threads
     */
    public Map<String, List<String>> getImageMap() {
        Map<String, List<String>> imageMap = new TreeMap<>();
        File directImageFolder = new File(projConf.PREPROC_DIR);
        File[] images = directImageFolder.listFiles((d, name) -> name.endsWith(projConf.IMG_EXT));
        for (File image : images) {
            String imageName = image.getName().split("\\.")[0];
            List<String> imagePathList = new ArrayList<>();
            if(imageMap.containsKey(imageName)) { imagePathList = imageMap.get(imageName); }
            imagePathList.add(image.getAbsolutePath());
            Collections.sort(imagePathList);
            imageMap.put(imageName, imagePathList);
        }
        return imageMap;
    }
}
