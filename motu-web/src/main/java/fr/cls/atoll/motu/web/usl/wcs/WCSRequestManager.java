package fr.cls.atoll.motu.web.usl.wcs;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
import fr.cls.atoll.motu.web.usl.request.actions.AbstractAction;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.wcs.request.actions.Constants;
import fr.cls.atoll.motu.web.usl.wcs.request.actions.WCSDescribeCoverageAction;
import fr.cls.atoll.motu.web.usl.wcs.request.actions.WCSGetCapabilitiesAction;
import fr.cls.atoll.motu.web.usl.wcs.request.actions.WCSGetCoverageAction;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.WCSHTTPParameters;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.AcceptVersionsHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.RequestHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.ServiceHTTPParameterValidator;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class WCSRequestManager implements IWCSRequestManager {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * {@inheritDoc}
     * 
     * @throws InvalidHTTPParameterException
     * @throws MotuException
     */
    @Override
    public void onNewRequest(HttpServletRequest request, HttpServletResponse response) throws MotuException {
        String serviceValue = WCSHTTPParameters.getServiceFromRequest(request);
        String requestId = null;
        if (validateService(response, serviceValue)) {
            String versionValue = WCSHTTPParameters.getAcceptVersionsFromRequest(request);
            if (validateVersion(response, versionValue)) {
                AbstractAction actionInst = null;
                try {
                    String action = WCSHTTPParameters.getRequestFromRequest(request);
                    actionInst = retrieveActionFromHTTPParameters(request, response);
                    if (actionInst != null) {
                        try {
                            if (WCSGetCoverageAction.ACTION_NAME.equals(action)) {
                                actionInst.doAction();
                            } else {
                                requestId = BLLManager.getInstance().getRequestManager().initRequest(actionInst);
                                BLLManager.getInstance().getRequestManager().setActionStatus(requestId, StatusModeType.INPROGRESS);
                                actionInst.doAction();
                                BLLManager.getInstance().getRequestManager().setActionStatus(requestId, StatusModeType.DONE);
                            }
                        } catch (InvalidHTTPParameterException e) {
                            if (requestId != null) {
                                BLLManager.getInstance().getRequestManager().setActionStatus(requestId, StatusModeType.ERROR);
                            }
                            if (e.getParameterValue() == null) {
                                String errResponse = Utils.onError("",
                                                                   Constants.MISSING_PARAMETER_VALUE_CODE,
                                                                   ErrorType.WCS_MISSING_PARAMETER_VALUE,
                                                                   e.getParameterName());
                                HTTPUtils.writeHttpResponse(response, errResponse, HTTPUtils.CONTENT_TYPE_XML_UTF8, null);
                            } else {
                                String errResponse = Utils.onError("",
                                                                   Constants.INVALID_PARAMETER_VALUE_CODE,
                                                                   ErrorType.WCS_INVALID_PARAMETER_VALUE,
                                                                   e.getParameterName(),
                                                                   e.getParameterValue(),
                                                                   e.getParameterBoundaries());
                                HTTPUtils.writeHttpResponse(response, errResponse, HTTPUtils.CONTENT_TYPE_XML_UTF8, null);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (requestId != null) {
                        BLLManager.getInstance().getRequestManager().setActionStatus(requestId, StatusModeType.ERROR);
                    }
                    if (e instanceof MotuException && ((MotuException) e).getErrorType().equals(ErrorType.WCS_INVALID_AXIS_LABEL)) {
                        MotuException motuException = (MotuException) e;
                        Utils.onException(response,
                                          "103",
                                          Constants.INVALID_AXIS_LABEL_CODE,
                                          ErrorType.WCS_INVALID_AXIS_LABEL,
                                          e,
                                          motuException.getErrorArguments());
                    } else {
                        Utils.onException(response, "", Constants.NO_APPLICABLE_CODE_CODE, ErrorType.WCS_NO_APPLICABLE_CODE, e);
                    }
                }
            }
        }
    }

    private AbstractAction retrieveActionFromHTTPParameters(HttpServletRequest request, HttpServletResponse response) throws MotuException {
        String requestValue = WCSHTTPParameters.getRequestFromRequest(request);
        AbstractAction actionInst = null;
        if (requestValue != null) {
            switch (requestValue) {
            case WCSGetCapabilitiesAction.ACTION_NAME:
                actionInst = new WCSGetCapabilitiesAction("101", request, response);
                break;
            case WCSDescribeCoverageAction.ACTION_NAME:
                actionInst = new WCSDescribeCoverageAction("102", request, response);
                break;
            case WCSGetCoverageAction.ACTION_NAME:
                actionInst = new WCSGetCoverageAction("103", request, response);
                break;
            default:
                try {
                    String errResponse = Utils.onError("",
                                                       Constants.INVALID_PARAMETER_VALUE_CODE,
                                                       ErrorType.WCS_INVALID_PARAMETER_VALUE,
                                                       WCSHTTPParameters.REQUEST,
                                                       requestValue,
                                                       RequestHTTPParameterValidator.getParameterBoundariesAsString());
                    HTTPUtils.writeHttpResponse(response, errResponse, HTTPUtils.CONTENT_TYPE_XML_UTF8, null);
                } catch (IOException e) {
                    LOGGER.error("Error while writing HTTP response.", e);
                    throw new MotuException(ErrorType.SYSTEM, "Error while writing HTTP response: " + e.getMessage());
                }
            }
        } else {
            try {
                String errResponse = Utils
                        .onError("", Constants.MISSING_PARAMETER_VALUE_CODE, ErrorType.WCS_MISSING_PARAMETER_VALUE, WCSHTTPParameters.REQUEST);
                HTTPUtils.writeHttpResponse(response, errResponse, HTTPUtils.CONTENT_TYPE_XML_UTF8, null);
            } catch (IOException e) {
                LOGGER.error("Error while writing HTTP response.", e);
                throw new MotuException(ErrorType.SYSTEM, "Error while writing HTTP response: " + e.getMessage());
            }
        }
        return actionInst;
    }

    private boolean validateService(HttpServletResponse response, String serviceValue) throws MotuException {
        if (serviceValue == null) {
            try {
                String errResponse = Utils
                        .onError("", Constants.MISSING_PARAMETER_VALUE_CODE, ErrorType.WCS_MISSING_PARAMETER_VALUE, WCSHTTPParameters.SERVICE);
                HTTPUtils.writeHttpResponse(response, errResponse, HTTPUtils.CONTENT_TYPE_XML_UTF8, null);
            } catch (IOException e) {
                LOGGER.error("Error while writing HTTP response.", e);
                throw new MotuException(ErrorType.SYSTEM, "Error while writing HTTP response: " + e.getMessage());
            }
            return false;
        } else if (!Constants.WCS_SERVICE_NAME.equals(serviceValue.toUpperCase())) {
            try {
                String errResponse = Utils.onError("",
                                                   Constants.INVALID_PARAMETER_VALUE_CODE,
                                                   ErrorType.WCS_INVALID_PARAMETER_VALUE,
                                                   WCSHTTPParameters.SERVICE,
                                                   serviceValue,
                                                   ServiceHTTPParameterValidator.getParameterBoundariesAsString());
                HTTPUtils.writeHttpResponse(response, errResponse, HTTPUtils.CONTENT_TYPE_XML_UTF8, null);
            } catch (IOException e) {
                LOGGER.error("Error while writing HTTP response.", e);
                throw new MotuException(ErrorType.SYSTEM, "Error while writing HTTP response: " + e.getMessage());
            }
            return false;
        }
        return true;
    }

    private boolean validateVersion(HttpServletResponse response, String versionValue) throws MotuException {
        if (versionValue == null) {
            try {
                String errResponse = Utils.onError("",
                                                   Constants.MISSING_PARAMETER_VALUE_CODE,
                                                   ErrorType.WCS_MISSING_PARAMETER_VALUE,
                                                   WCSHTTPParameters.ACCEPT_VERSIONS);
                HTTPUtils.writeHttpResponse(response, errResponse, HTTPUtils.CONTENT_TYPE_XML_UTF8, null);
            } catch (IOException e) {
                LOGGER.error("Error while writing HTTP response.", e);
                throw new MotuException(ErrorType.SYSTEM, "Error while writing HTTP response: " + e.getMessage());
            }
            return false;

        } else if (!versionValue.toUpperCase().contains(Constants.WCS_VERSION_VALUE)) {
            try {
                String errResponse = Utils.onError("",
                                                   Constants.VERSION_NEGOTIATION_FAILED_CODE,
                                                   ErrorType.WCS_VERSION_NEGOTIATION_FAILED,
                                                   WCSHTTPParameters.ACCEPT_VERSIONS,
                                                   versionValue,
                                                   AcceptVersionsHTTPParameterValidator.getParameterBoundariesAsString());
                HTTPUtils.writeHttpResponse(response, errResponse, HTTPUtils.CONTENT_TYPE_XML_UTF8, null);
            } catch (IOException e) {
                LOGGER.error("Error while writing HTTP response.", e);
                throw new MotuException(ErrorType.SYSTEM, "Error while writing HTTP response: " + e.getMessage());
            }
            return false;
        }
        return true;
    }
}
