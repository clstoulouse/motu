package fr.cls.atoll.motu.web.usl.wcs;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
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
        if (validateService(response, serviceValue)) {
            String versionValue = WCSHTTPParameters.getAcceptVersionsFromRequest(request);
            if (validateVersion(response, versionValue)) {
                try {
                    executeRequest(request, response);
                } catch (Exception e) {
                    Utils.onException(response, "", Constants.NO_APPLICABLE_CODE_CODE, ErrorType.WCS_NO_APPLICABLE_CODE, e);
                }
            }
        }
    }

    private void executeRequest(HttpServletRequest request, HttpServletResponse response) throws MotuException {
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
                Utils.onError(response,
                              "",
                              Constants.INVALID_PARAMETER_VALUE_CODE,
                              ErrorType.WCS_INVALID_PARAMETER_VALUE,
                              WCSHTTPParameters.REQUEST,
                              requestValue,
                              RequestHTTPParameterValidator.getParameterBoundariesAsString());
            }
            if (actionInst != null) {
                try {
                    actionInst.doAction();
                } catch (InvalidHTTPParameterException e) {
                    if (e.getParameterValue() == null) {
                        Utils.onError(response,
                                      "",
                                      Constants.MISSING_PARAMETER_VALUE_CODE,
                                      ErrorType.WCS_MISSING_PARAMETER_VALUE,
                                      e.getParameterName());
                    } else {
                        Utils.onError(response,
                                      "",
                                      Constants.INVALID_PARAMETER_VALUE_CODE,
                                      ErrorType.WCS_INVALID_PARAMETER_VALUE,
                                      e.getParameterName(),
                                      e.getParameterValue(),
                                      e.getParameterBoundaries());
                    }
                }
            }
        } else {
            Utils.onError(response, "", Constants.MISSING_PARAMETER_VALUE_CODE, ErrorType.WCS_MISSING_PARAMETER_VALUE, WCSHTTPParameters.REQUEST);
        }
    }

    private boolean validateService(HttpServletResponse response, String serviceValue) throws MotuException {
        if (serviceValue == null) {
            Utils.onError(response, "", Constants.MISSING_PARAMETER_VALUE_CODE, ErrorType.WCS_MISSING_PARAMETER_VALUE, WCSHTTPParameters.SERVICE);
            return false;
        } else if (!Constants.WCS_SERVICE_NAME.equals(serviceValue.toUpperCase())) {
            Utils.onError(response,
                          "",
                          Constants.INVALID_PARAMETER_VALUE_CODE,
                          ErrorType.WCS_INVALID_PARAMETER_VALUE,
                          WCSHTTPParameters.SERVICE,
                          serviceValue,
                          ServiceHTTPParameterValidator.getParameterBoundariesAsString());
            return false;
        }
        return true;
    }

    private boolean validateVersion(HttpServletResponse response, String versionValue) throws MotuException {
        if (versionValue == null) {
            Utils.onError(response,
                          "",
                          Constants.MISSING_PARAMETER_VALUE_CODE,
                          ErrorType.WCS_MISSING_PARAMETER_VALUE,
                          WCSHTTPParameters.ACCEPT_VERSIONS);
            return false;
        } else if (!versionValue.toUpperCase().contains(Constants.WCS_VERSION_VALUE)) {
            Utils.onError(response,
                          "",
                          Constants.INVALID_PARAMETER_VALUE_CODE,
                          ErrorType.WCS_INVALID_PARAMETER_VALUE,
                          WCSHTTPParameters.ACCEPT_VERSIONS,
                          versionValue,
                          AcceptVersionsHTTPParameterValidator.getParameterBoundariesAsString());
            return false;
        }
        return true;
    }
}
