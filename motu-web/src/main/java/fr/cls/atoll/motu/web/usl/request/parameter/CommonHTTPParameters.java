package fr.cls.atoll.motu.web.usl.request.parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.request.actions.AboutAction;
import fr.cls.atoll.motu.web.usl.request.actions.CacheStatusAction;
import fr.cls.atoll.motu.web.usl.request.actions.DebugAction;
import fr.cls.atoll.motu.web.usl.request.actions.DescribeCoverageAction;
import fr.cls.atoll.motu.web.usl.request.actions.DescribeProductAction;
import fr.cls.atoll.motu.web.usl.request.actions.DownloadProductAction;
import fr.cls.atoll.motu.web.usl.request.actions.GetRequestStatusAction;
import fr.cls.atoll.motu.web.usl.request.actions.GetSizeAction;
import fr.cls.atoll.motu.web.usl.request.actions.HealthzAction;
import fr.cls.atoll.motu.web.usl.request.actions.HttpErrorAction;
import fr.cls.atoll.motu.web.usl.request.actions.ListCatalogAction;
import fr.cls.atoll.motu.web.usl.request.actions.ListServicesAction;
import fr.cls.atoll.motu.web.usl.request.actions.LogoutAction;
import fr.cls.atoll.motu.web.usl.request.actions.PingAction;
import fr.cls.atoll.motu.web.usl.request.actions.ProductDownloadHomeAction;
import fr.cls.atoll.motu.web.usl.request.actions.ProductMetadataAction;
import fr.cls.atoll.motu.web.usl.request.actions.TimeCoverageAction;
import fr.cls.atoll.motu.web.usl.request.actions.TransactionsAction;
import fr.cls.atoll.motu.web.usl.request.actions.WelcomeAction;

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
public class CommonHTTPParameters {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    public static String getRequestParameterIgnoreCase(HttpServletRequest request, String parameter) {
        String paramValue = null;
        if (request.getParameterMap() != null) {
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                if (entry.getKey().equalsIgnoreCase(parameter)) {
                    paramValue = entry.getValue()[0];
                    break;
                }
            }
        }
        return paramValue;
    }

    /**
     * Gets the action.
     * 
     * @param request the request
     * 
     * @return the action
     */
    public static String getActionFromRequest(HttpServletRequest request) {
        String action = getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_ACTION);
        if (StringUtils.isNullOrEmpty(action)) {
            action = (String) request.getAttribute(MotuRequestParametersConstant.PARAM_ACTION);
            if (StringUtils.isNullOrEmpty(action)) {
                String defaultService = BLLManager.getInstance().getConfigManager().getMotuConfig().getDefaultService();
                if (StringUtils.isNullOrEmpty(defaultService)) {
                    action = MotuRequestParametersConstant.ACTION_LIST_SERVICES;
                } else {
                    if (isAKnownAction(defaultService)) {
                        action = defaultService;
                    } else {
                        LOGGER.error("Error in motuCOnfiguration.xml, default service has not a valide value: " + defaultService
                                + ", default value is: listservices");
                        action = MotuRequestParametersConstant.ACTION_LIST_SERVICES;
                    }
                }
            }
        }
        return action;
    }

    /**
     * .
     * 
     * @param defaultService
     * @return
     */
    public static boolean isAKnownAction(String defaultService) {
        boolean isAKnownAction = false;
        if (defaultService != null) {
            switch (defaultService) {
            case PingAction.ACTION_NAME:
            case DebugAction.ACTION_NAME:
            case DebugAction.ACTION_NAME_ALIAS_QUEUE_SERVER:
            case GetRequestStatusAction.ACTION_NAME:
            case GetSizeAction.ACTION_NAME:
            case DescribeProductAction.ACTION_NAME:
            case TimeCoverageAction.ACTION_NAME:
            case LogoutAction.ACTION_NAME:
            case DownloadProductAction.ACTION_NAME:
            case ListCatalogAction.ACTION_NAME:
            case ProductMetadataAction.ACTION_NAME:
            case ProductDownloadHomeAction.ACTION_NAME:
            case ListServicesAction.ACTION_NAME:
            case DescribeCoverageAction.ACTION_NAME:
            case AboutAction.ACTION_NAME:
            case HttpErrorAction.ACTION_NAME:
            case WelcomeAction.ACTION_NAME:
            case TransactionsAction.ACTION_NAME:
            case HealthzAction.ACTION_NAME:
            case CacheStatusAction.ACTION_NAME:
                isAKnownAction = true;
                break;
            default:
                // Nothing to do
            }
        }
        return isAKnownAction;
    }

    /**
     * Gets the mode parameter from the request.
     * 
     * @param request servlet request
     * 
     * @return how to return the result (mode=console : url file, otherwhise HTML pages)
     */
    public static String getModeFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_MODE);
    }

    /**
     * .
     * 
     * @param request
     * @return 0 if the request id has not the good format, otherwise the request Id
     */
    public static String getRequestIdFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_REQUEST_ID);
    }

    public static String getLatitudeLowFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_LOW_LAT);
    }

    public static String getLatitudeHighFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_HIGH_LAT);
    }

    public static String getLongitudeLowFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_LOW_LON);
    }

    public static String getLongitudeHighFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_HIGH_LON);
    }

    /**
     * .
     * 
     * @param request
     * @return
     */
    public static String getDepthLowFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_LOW_Z);
    }

    /**
     * .
     * 
     * @param request
     * @return
     */
    public static String getDepthHighFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_HIGH_Z);
    }

    /**
     * .
     * 
     * @param request
     * @return
     */
    public static String getStartDateFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_START_DATE);
    }

    /**
     * .
     * 
     * @param request
     * @return
     */
    public static String getEndDateFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_END_DATE);
    }

    public static String getServiceFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_SERVICE);
    }

    public static String getDataFromParameter(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_DATA);
    }

    public static String[] getListOfDataFromParameter(HttpServletRequest request) {
        return request.getParameterValues(MotuRequestParametersConstant.PARAM_DATA);
    }

    public static String[] getVariablesFromParameter(HttpServletRequest request) {
        return request.getParameterValues(MotuRequestParametersConstant.PARAM_VARIABLE);
    }

    public static List<String> getVariablesAsListFromParameter(HttpServletRequest request) {
        String[] variables = CommonHTTPParameters.getVariablesFromParameter(request);

        List<String> listVar = new ArrayList<String>();
        if (variables != null) {
            // Not an ArrayList type which cause issue while serializing to write xstream log messages
            // listVar = Arrays.asList(variables);
            for (String v : variables) {
                listVar.add(v);
            }
        }
        return listVar;
    }

    public static String getPriorityFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_PRIORITY);
    }

    public static String getOutputFormatFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_OUTPUT);
    }

    public static String getCatalogTypeFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_CATALOG_TYPE);
    }

    public static String getLanguageFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_LANGUAGE);
    }

    public static String getPasswordFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_PWD);
    }

    public static String getAnonymousParameterFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_ANONYMOUS);
    }

    public static String getProductFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_PRODUCT);
    }

    public static String getDatasetIdFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_DATASET_ID);
    }

    public static String getXmlFileFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_XML_FILE);
    }

    public static String getExtraMetaDataFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_EXTRA_METADATA);
    }

    public static String getDebugOrderFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_DEBUG_ORDER);
    }

    public static String getScriptVersionFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_SCRIPT_VERSION);
    }

    public static String getHttpErrorCodeFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_HTTP_ERROR_CODE);
    }

    public static String getFileNameFromRequest(HttpServletRequest request) {
        return getRequestParameterIgnoreCase(request, MotuRequestParametersConstant.PARAM_FILE_NAME);
    }

}
