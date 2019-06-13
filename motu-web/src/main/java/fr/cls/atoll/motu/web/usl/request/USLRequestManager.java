package fr.cls.atoll.motu.web.usl.request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.api.utils.JAXBWriter;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
import fr.cls.atoll.motu.web.usl.request.actions.AboutAction;
import fr.cls.atoll.motu.web.usl.request.actions.AbstractAction;
import fr.cls.atoll.motu.web.usl.request.actions.DebugAction;
import fr.cls.atoll.motu.web.usl.request.actions.DescribeCoverageAction;
import fr.cls.atoll.motu.web.usl.request.actions.DescribeProductAction;
import fr.cls.atoll.motu.web.usl.request.actions.DownloadProductAction;
import fr.cls.atoll.motu.web.usl.request.actions.GetRequestStatusAction;
import fr.cls.atoll.motu.web.usl.request.actions.GetSizeAction;
import fr.cls.atoll.motu.web.usl.request.actions.HttpErrorAction;
import fr.cls.atoll.motu.web.usl.request.actions.ListCatalogAction;
import fr.cls.atoll.motu.web.usl.request.actions.ListServicesAction;
import fr.cls.atoll.motu.web.usl.request.actions.LogoutAction;
import fr.cls.atoll.motu.web.usl.request.actions.PingAction;
import fr.cls.atoll.motu.web.usl.request.actions.ProductDownloadHomeAction;
import fr.cls.atoll.motu.web.usl.request.actions.ProductMetadataAction;
import fr.cls.atoll.motu.web.usl.request.actions.RefreshCacheAction;
import fr.cls.atoll.motu.web.usl.request.actions.TimeCoverageAction;
import fr.cls.atoll.motu.web.usl.request.actions.WelcomeAction;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.response.velocity.VelocityTemplateManager;

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
public class USLRequestManager implements IUSLRequestManager {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    public USLRequestManager() {

    }

    /** {@inheritDoc} */
    @Override
    public void init() throws MotuException {
        try {
            JAXBWriter.getInstance().init();
        } catch (JAXBException e) {
            throw new MotuException(ErrorType.SYSTEM, e);
        }
    }

    @Override
    public void onNewRequest(HttpServletRequest request, HttpServletResponse response) throws MotuException, InvalidHTTPParameterException {
        String action = CommonHTTPParameters.getActionFromRequest(request).toLowerCase();
        AbstractAction actionInst = retrieveActionFromHTTPParameters(action, request, response);
        String requestId = null;
        try {
            if (actionInst != null) {
                if (!(actionInst instanceof DownloadProductAction)) {
                    requestId = BLLManager.getInstance().getRequestManager().initRequest(actionInst);
                    BLLManager.getInstance().getRequestManager().setActionStatus(requestId, StatusModeType.INPROGRESS);
                    actionInst.doAction();
                    BLLManager.getInstance().getRequestManager().setActionStatus(requestId, StatusModeType.DONE);
                } else {
                    actionInst.doAction();
                }
            } else {
                throw new MotuException(ErrorType.UNKNOWN_ACTION, "The requested action is unknown : " + action);
            }
        } catch (Exception e) {
            onException(requestId, actionInst, e, response);
        }

    }

    private AbstractAction retrieveActionFromHTTPParameters(String action, HttpServletRequest request, HttpServletResponse response) {
        AbstractAction actionInst = null;
        switch (action) {
        case PingAction.ACTION_NAME:
            actionInst = new PingAction("002", request, response);
            break;
        case DebugAction.ACTION_NAME:
        case DebugAction.ACTION_NAME_ALIAS_QUEUE_SERVER:
            actionInst = new DebugAction("003", request, response);
            break;
        case GetRequestStatusAction.ACTION_NAME:
            actionInst = new GetRequestStatusAction("004", request, response);
            break;
        case GetSizeAction.ACTION_NAME:
            actionInst = new GetSizeAction("005", request, response, getSession(request));
            break;
        case DescribeProductAction.ACTION_NAME:
            actionInst = new DescribeProductAction("006", request, response, getSession(request));
            break;
        case TimeCoverageAction.ACTION_NAME:
            actionInst = new TimeCoverageAction("007", request, response, getSession(request));
            break;
        case LogoutAction.ACTION_NAME:
            actionInst = new LogoutAction("008", request, response, getSession(request));
            break;
        case DownloadProductAction.ACTION_NAME:
            actionInst = new DownloadProductAction("010", request, response, getSession(request));
            break;
        case ListCatalogAction.ACTION_NAME:
            actionInst = new ListCatalogAction("011", request, response, getSession(request));
            break;
        case ProductMetadataAction.ACTION_NAME:
            actionInst = new ProductMetadataAction("012", request, response, getSession(request));
            break;
        case ProductDownloadHomeAction.ACTION_NAME:
            actionInst = new ProductDownloadHomeAction("013", request, response, getSession(request));
            break;
        case ListServicesAction.ACTION_NAME:
            actionInst = new ListServicesAction("014", request, response, getSession(request));
            break;
        case DescribeCoverageAction.ACTION_NAME:
            actionInst = new DescribeCoverageAction("015", request, response, getSession(request));
            break;
        case AboutAction.ACTION_NAME:
            actionInst = new AboutAction("016", request, response);
            break;
        case HttpErrorAction.ACTION_NAME:
            actionInst = new HttpErrorAction("017", request, response);
            break;
        case WelcomeAction.ACTION_NAME:
            actionInst = new WelcomeAction("018", request, response);
            break;
        case RefreshCacheAction.ACTION_NAME:
            actionInst = new RefreshCacheAction("019", request, response);
            break;
        // case TransactionsAction.ACTION_NAME:
        // actionInst = new TransactionsAction("019", request, response);
        // break;
        // default:
        // Nothing to do
        }
        return actionInst;
    }

    public static void onException(String requestId, AbstractAction actionInst, Exception e, HttpServletResponse response) throws MotuException {
        if (requestId != null) {
            BLLManager.getInstance().getRequestManager().setActionStatus(requestId, StatusModeType.ERROR);
        }
        ErrorType errorType = ErrorType.SYSTEM;
        String actionCode = AbstractAction.UNDETERMINED_ACTION;
        if (actionInst != null) {
            actionCode = actionInst.getActionCode();
        }

        String errMessage = "";
        if (e instanceof MotuException) {
            errorType = ((MotuException) e).getErrorType();
            errMessage = BLLManager.getInstance().getMessagesErrorManager().getMessageError(errorType, e);
        } else if (e instanceof InvalidHTTPParameterException) {
            errorType = ErrorType.BAD_PARAMETERS;
            errMessage = e.getMessage();
        } else {
            errMessage = BLLManager.getInstance().getMessagesErrorManager().getMessageError(errorType, e);
        }

        if (isErrorTypeToLog(errorType)) {
            LOGGER.error(StringUtils.getLogMessage(actionCode, errorType, errMessage), e);
        }

        writeErrorMessage(actionCode, errorType, errMessage, response);
    }

    public static boolean isErrorTypeToLog(ErrorType errorType_) {
        boolean isErrorTypeToLog = false;
        switch (errorType_) {
        case WCS_NO_APPLICABLE_CODE:
        case SYSTEM:
        case NETCDF_GENERATION:
        case EXCEEDING_QUEUE_CAPACITY:
        case EXCEEDING_QUEUE_DATA_CAPACITY:
            isErrorTypeToLog = true;
            break;
        }

        return isErrorTypeToLog;
    }

    private static void writeErrorMessage(String actionCode, ErrorType errorType, String errMessage, HttpServletResponse response_)
            throws MotuException {
        response_.setContentType(HTTPUtils.CONTENT_TYPE_HTML_UTF8);

        Map<String, Object> velocityContext = new HashMap<String, Object>(2);
        velocityContext.put("body_template", VelocityTemplateManager.getTemplatePath("exception", VelocityTemplateManager.DEFAULT_LANG));
        velocityContext.put("message", StringUtils.getLogMessage(actionCode, errorType, errMessage));

        String response = VelocityTemplateManager.getInstance().getResponseWithVelocity(velocityContext, null, null);
        try {
            response_.getWriter().write(response);
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while using velocity template", e);
        }
    }

    /**
     * Returns the current session associated with this request, or if the request does not have a session,
     * creates one.
     *
     * @param request object that contains the request the client has made of the servlet.
     * @return the HttpSession associated with this request
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    protected HttpSession getSession(HttpServletRequest request) {

        if (!isCreateOrGetSession(request)) {
            return null;
        }

        HttpSession session = request.getSession(true);
        // isValid(session);
        initSession(session);

        return session;

    }

    /**
     * Checks if a session has to be created.
     * 
     * @param request the request
     * 
     * @return true, if a session has to be created, false otherwise
     */
    protected boolean isCreateOrGetSession(HttpServletRequest request) {
        String action = CommonHTTPParameters.getActionFromRequest(request);
        String mode = CommonHTTPParameters.getModeFromRequest(request);
        boolean isAActionWhichNeedASession = false;
        isAActionWhichNeedASession |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_REFRESH);
        isAActionWhichNeedASession |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_LIST_SERVICES);
        isAActionWhichNeedASession |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_LIST_CATALOG);
        isAActionWhichNeedASession |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_LIST_PRODUCT_METADATA);
        isAActionWhichNeedASession |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_PRODUCT_DOWNLOADHOME);
        isAActionWhichNeedASession |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_PRODUCT_DOWNLOAD);
        isAActionWhichNeedASession |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_LOGOUT);

        return isAActionWhichNeedASession && StringUtils.isNullOrEmpty(mode);
    }

    /**
     * Initializes a new session with a new Organizer object.
     *
     * @param session session to initialize
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    private void initSession(HttpSession session) {
    }

    /** {@inheritDoc} */
    @Override
    public String getProductDownloadUrlPath(RequestProduct product) {
        if (StringUtils.isNullOrEmpty(product.getRequestProductParameters().getExtractFilename())) {
            return "";
        }

        String httpDownloadUrlBase = BLLManager.getInstance().getConfigManager().getMotuConfig().getDownloadHttpUrl();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(httpDownloadUrlBase);
        if (!(httpDownloadUrlBase.endsWith("/"))) {
            stringBuffer.append("/");
        }
        stringBuffer.append(product.getRequestProductParameters().getExtractFilename());

        return stringBuffer.toString();
    }

}
