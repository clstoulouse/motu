package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.RequestIdHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.response.xml.converter.XMLConverter;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)<br>
 * <br>
 * This interface is used to download data with subsetting.<br>
 * Operation invocation consists in performing an HTTP GET request.<br>
 * Input parameters are the following: [x,y] is the cardinality<br>
 * <ul>
 * <li><b>action</b>: [1]: {@link #ACTION_NAME}</li>
 * <li><b>requestid</b>: [1]: identifier of the order (download request) as a positive integer represented as
 * a string.</li>
 * </ul>
 * <br>
 * Output result (HTTP response) is an XML stream defined by the schema
 * /motu-api-message/src/main/schema/atoll-motu-msg.xsd#statusModeResponse
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class GetRequestStatusAction extends AbstractAction {

    public static final String ACTION_NAME = "getreqstatus";

    private RequestIdHTTPParameterValidator rqtIdValidator;

    /**
     * Constructeur.
     * 
     * @param actionName_
     */
    public GetRequestStatusAction(String actionCode_, HttpServletRequest request, HttpServletResponse response) {
        super(ACTION_NAME, actionCode_, request, response);

        rqtIdValidator = new RequestIdHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_REQUEST_ID,
                CommonHTTPParameters.getRequestIdFromRequest(getRequest()));
    }

    @Override
    public void process() throws MotuException {
        Long requestId = rqtIdValidator.getParameterValueValidated();

        try {
            if (requestId != null) {
                RequestDownloadStatus rds = BLLManager.getInstance().getRequestManager().getDownloadRequestStatus(requestId);
                if (rds == null) {
                    throw new MotuException(ErrorType.UNKNOWN_REQUEST_ID, "Oops, request id '" + requestId + "' does not exist.");
                } else {
                    getResponse().setContentType(CONTENT_TYPE_XML);
                    String response = XMLConverter.toXMLString(rds, getActionCode());
                    getResponse().getWriter().write(response);
                }
            } else {
                getResponse().setContentType(CONTENT_TYPE_XML);
                String response = XMLConverter.toXMLString(new MotuInvalidRequestIdException(-1L), getActionCode());
                getResponse().getWriter().write(response);
            }
        } catch (IOException e) {
            try {
                getResponse().sendError(500, String.format("ERROR: %s", e.getMessage()));
            } catch (IOException e1) {
                throw new MotuException(ErrorType.SYSTEM, "Error while writing response", e1);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        // No parameter to check
        rqtIdValidator.validate();
    }

}
