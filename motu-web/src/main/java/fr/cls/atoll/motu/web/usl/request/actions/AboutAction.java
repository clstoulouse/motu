package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)<br>
 * <br>
 * <br>
 * This interface is used to download data with subsetting.<br>
 * Operation invocation consists in performing an HTTP GET request.<br>
 * Operation invocation consists in performing an HTTP GET request.<br>
 * Input parameters are the following: [x,y] is the cardinality<br>
 * <ul>
 * <li><b>action</b>: [1]: {@link #ACTION_NAME}</li>
 * </ul>
 * The output response is a simple plain text: "OK - response action=ping" <br>
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
        getResponse().setContentType(CONTENT_TYPE_PLAIN);
        try {
            getResponse().getWriter()
                    .write("Motu-distribution: " + BLLManager.getInstance().getConfigManager().getVersionManager().getDistributionVersion() + "\n");
            getResponse().getWriter()
                    .write("Motu-configuration: " + BLLManager.getInstance().getConfigManager().getVersionManager().getConfigurationVersion() + "\n");
            getResponse().getWriter()
                    .write("Motu-products: " + BLLManager.getInstance().getConfigManager().getVersionManager().getProductsVersion() + "\n");
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while writing response", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        // No parameter to check
    }

}
