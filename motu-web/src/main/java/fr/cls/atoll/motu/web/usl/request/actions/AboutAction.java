package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.config.version.IBLLVersionManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
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
        StringWriter sw = new StringWriter();
        try {
            sw.write("<!DOCTYPE html><html><body>");

            IBLLVersionManager versionMgr = BLLManager.getInstance().getConfigManager().getVersionManager();
            sw.write(getVersionOnOneLineAsHtml("Motu-products: ", versionMgr.getProductsVersion()));
            sw.write(getVersionOnOneLineAsHtml("Motu-distribution: ", versionMgr.getDistributionVersion()));
            sw.write(getVersionOnOneLineAsHtml("Motu-configuration: ", versionMgr.getConfigurationVersion()));

            sw.write(displayStaticFilesVersion("Motu-static-files (Graphic chart): "));
            sw.write("</body></html>");

            String response = sw.toString();
            writeResponse(response, HTTPUtils.CONTENT_TYPE_HTML_UTF8, new String[] { "Access-Control-Allow-Origin", "*" });
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while writing response", e);
        }
    }

    private String getVersionOnOneLineAsHtml(String entity, String version) throws IOException {
        return entity + version + "<BR/>";
    }

    /**
     * The static files can be installed in a folder on the local server, on a remote machine and none is
     * installed and in this case we displays "Default version". .
     * 
     * @param entity
     * @throws IOException
     */
    private String displayStaticFilesVersion(String entity) throws IOException {
        StringWriter sw = new StringWriter();
        String urlStaticFiles = BLLManager.getInstance().getConfigManager().getVersionManager().getStaticFilesVersion();
        String urlStaticFilesContent = "";
        if (StringUtils.isNullOrEmpty(urlStaticFiles)) {
            urlStaticFilesContent = "Default version";
        } else {
            urlStaticFilesContent = "<span id=\"staticFilesVersion\">loading...</span>";
        }
        sw.write(getVersionOnOneLineAsHtml(entity, urlStaticFilesContent));
        if (!StringUtils.isNullOrEmpty(urlStaticFiles)) {
            // Load version of static files from the Web server

            // In order to avoid Cross-Domain restriction as the web server which contain the version file is
            // not the same as Motu one,
            // we have a script on the static files web server which, once loaded, call directly the JS method
            // displayStaticFilesVersion.

            // @formatter:off
            sw.write("\n"
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
        return sw.toString();
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        // No parameter to check
    }

}
