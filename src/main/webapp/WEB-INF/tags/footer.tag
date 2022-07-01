<%@ tag description="Page Footer Tag" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ attribute name="hideOnPageLoad" required="true" %>
<jsp:useBean id="date" class="java.util.Date" />
<footer class="page-footer" <c:if test="${hideOnPageLoad == true}">style="display:none;"</c:if>>
    <script type="text/javascript">
        $(document).ready(function()
        {
            let OCR4ALL_VERSION = "UNKNOWN";
            let LAREX_VERSION = "UNKNOWN"
            $.get("ajax/team/sysenv")
                .done(function getEnv(data) {
                    const env = data.split("\n");
                    OCR4ALL_VERSION = "OCR4all: v" + env[0];
                    LAREX_VERSION =   "  LAREX: v" + env[1];
                    $("#OCR4all_Version").html(OCR4ALL_VERSION);
                    $("#LAREX_Version").html(LAREX_VERSION);
                })

        })
        
    </script>
    <div class="container">
        <div>
            <div class="row center-align">
                <div class="col l4 s6">
                    <a class="grey-text text-lighten-2" target="_blank" href="Team">
                        <b>TEAM</b>
                    </a>
                </div>
                <div class="col l4 s6">
                    <a target="_blank" class="grey-text text-lighten-2" href="https://github.com/OCR4all/getting_started">
                        <b>HELP/GUIDES</b>
                    </a>
                </div>
                <div class="col l4 s12">
                    <a target="_blank"  class="grey-text text-lighten-2" href="https://lists.uni-wuerzburg.de/mailman/listinfo/ocr4all">
                        <b>MAILING LIST</b>
                    </a>
                </div>
            </div>
            <div>
                <h6 class="grey-text dev-statement center-align text-lighten-2">
                    <b>
					Developed at the Chair of Artificial Intelligence and Applied Computer Science<br/>
					in collaboration with the Center for Philology and Digitality "Kallimachos" at the University of Würzburg
                    </b>
                </h6>
            </div>
        </div>
    </div>
    <div class="col footer-version">
        <div class="row" id="OCR4all_Version_container"><span id="OCR4all_Version"></span></div>
        <div class="row"><span id="LAREX_Version"></span></div>
    </div>
    <div class="container footer-copyright">
        <div class="row">
            <div class="col l3 s6 offset-l1 center-align">
                © 2017 - <fmt:formatDate value="${date}" pattern="yyyy" />
            </div>
            <div class="col l1 s6 offset-l6 center-align grey-text text-lighten-2">
                <a target="_blank" href="https://www.uni-wuerzburg.de/en/sonstiges/imprint-privacy-policy/">
                    Imprint
                </a>
            </div>
        </div>
    </div>
</footer>
