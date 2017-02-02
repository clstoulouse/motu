package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.config.version.IBLLVersionManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)<br>
 * <br>
 * <br>
 * This interface is used to display the version of the Motu entities.<br>
 * Operation invocation consists in performing an HTTP GET request.<br>
 * There is no input parameter<br>
 * The output response is a simple HTML web page.<br>
 * <br>
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class AboutAction extends AbstractAction {

    public static final String ACTION_NAME = "about";

    /**
     * Constructeur.
     * 
     * @param actionName_
     */
    public AboutAction(String actionCode_, HttpServletRequest request, HttpServletResponse response) {
        super(ACTION_NAME, actionCode_, request, response);
    }

    @Override
    public void process() throws MotuException {
        getResponse().setContentType(CONTENT_TYPE_HTML);
        try {
            getResponse().getWriter().write("<!DOCTYPE html><html><body>");

            IBLLVersionManager versionMgr = BLLManager.getInstance().getConfigManager().getVersionManager();
            displayVersion("Motu-products: ", versionMgr.getProductsVersion());
            displayVersion("Motu-distribution: ", versionMgr.getDistributionVersion());
            displayVersion("Motu-configuration: ", versionMgr.getConfigurationVersion());

            displayStaticFilesVersion("Motu-static-files (Graphic chart): ");
            getResponse().setHeader("Access-Control-Allow-Origin", "*");
            getResponse().getWriter().write("</body></html>");
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while writing response", e);
        }
    }

    private void displayVersion(String entity, String version) throws IOException {
        getResponse().getWriter().write(entity + version + "<BR/>");
    }

    /**
     * The static files can be installed in a folder on the local server, on a remote machine and none is
     * installed and in this case we displays "Default version". .
     * 
     * @param entity
     * @throws IOException
     */
    private void displayStaticFilesVersion(String entity) throws IOException {
        String urlStaticFiles = BLLManager.getInstance().getConfigManager().getVersionManager().getStaticFilesVersion();
        String urlStaticFilesContent = "";
        if (StringUtils.isNullOrEmpty(urlStaticFiles)) {
            urlStaticFilesContent = "Default version";
        } else {
            urlStaticFilesContent = "<span id=\"staticFilesVersion\">loading...</span>";
        }
        displayVersion(entity, urlStaticFilesContent);
        if (!StringUtils.isNullOrEmpty(urlStaticFiles)) {
            // Load version of static files from the Web server

            // In order to avoid Cross-Domain restriction as the web server which contain the version file is
            // not the same as Motu one,
            // we have a script on the static files web server which, once loaded, call directly the JS method
            // displayStaticFilesVersion.

            // @formatter:off
            getResponse().getWriter().write("\n"
                    + "<script>\n" + ""
                    + "    function displayStaticFilesVersion(version){\n"
                    + "        var versionSpan = document.getElementById('staticFilesVersion');\n"
                    + "        if (versionSpan.innerText) {\n"
                    + "            versionSpan.innerText = version;\n"
                    + "        } else if (versionSpan.textContent) {\n"
                    + "            versionSpan.textContent = version;\n"
                    + "        }\n"
                    + "    };\n"
                    + "\n"
                    + "</script>\n"
                    + "<script src=\"" + urlStaticFiles + "\"></script>\n");
            // @formatter:on
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        // No parameter to check
    }

}
