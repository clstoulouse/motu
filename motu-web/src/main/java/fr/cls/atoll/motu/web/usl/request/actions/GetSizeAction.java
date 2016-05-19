package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasig.cas.client.util.AssertionHolder;

import fr.cls.atoll.motu.web.bll.request.ExtractionParameters;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites) <br>
 * <br>
 * This interface is used to download data with subsetting.<br>
 * Operation invocation consists in performing an HTTP GET request.<br>
 * Input parameters are the following: [x,y] is the cardinality<br>
 * <ul>
 * <li><b>action</b>: [1]: {@link #ACTION_NAME}</li>
 * <li><b>service</b>: [1]: identifier of the service that provides the desired data set to order.</li>
 * <li><b>product</b>: [1]: identifier of the desired data set to order.</li>
 * <li><b>variable</b>: [0,n]: physical variables to be extracted from the product. no variable is set, all
 * the variables of the dataset are extracted.</li>
 * <li><b>y_lo</b>: [0,1]: low latitude of a geographic extraction. Default value is -90.</li>
 * <li><b>y_hi</b>: [0,1]: high latitude of a geographic extraction. Default value is 90.</li>
 * <li><b>x_lo</b>: [0,1]: low longitude of a geographic extraction. Default value is -180.</li>
 * <li><b>x_hi</b>: [0,1]: high longitude of a geographic extraction. Default value is 180.</li>
 * <li><b>z_lo</b>: [0,1]: low vertical depth . Default value is 0.</li>
 * <li><b>z_hi</b>: [0,1]: high vertical depth. Default value is 180.</li>
 * <li><b>t_lo</b>: [0,1]: Start date of a temporal extraction. If not set, the default value is the first
 * date/time available for the dataset. Format is yyy-mm-dd or yyyy-dd h:m:s or yyyy-ddTh:m:s.</li>
 * <li><b>t_hi</b>: [0,1]: End date of a temporal extraction. If not set, the default value is the last
 * date/time available for the dataset. Format is yyy-mm-dd or yyyy-dd h:m:s or yyyy-ddTh:m:s.</li>
 * </ul>
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class GetSizeAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "getsize";

    /**
     * 
     * @param actionName_
     */
    public GetSizeAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, request, response, session);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws IOException {
        retrieveSize();
    }

    private void retrieveSize() throws IOException {
        createExtractionParameters();
    }

    private ExtractionParameters createExtractionParameters() throws IOException {
        Writer out = null;
        OutputFormat responseFormat = null;

        out = getResponse().getWriter();
        responseFormat = OutputFormat.HTML;

        ExtractionParameters extractionParameters = new ExtractionParameters(
                getServiceFromParameter(),
                getDataFromParameter(),
                getVariables(),
                getTemporalCoverage(),
                getGeoCoverage(),
                getDepthCoverage(),
                getProductId(),
                getOutputFormat(),
                out,
                responseFormat,
                getLoginOrUserHostname(),
                isAnAnonymousUser());
        extractionParameters.setBatchQueue(isBatch());

        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());
        return extractionParameters;
    }

    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        latitudeLowHTTPParameterValidator.validate();
        latitudeHighHTTPParameterValidator.validate();
        longitudeLowHTTPParameterValidator.validate();
        longitudeHighHTTPParameterValidator.validate();

        depthLowHTTPParameterValidator.validate();
        depthHighHTTPParameterValidator.validate();
    }
}
