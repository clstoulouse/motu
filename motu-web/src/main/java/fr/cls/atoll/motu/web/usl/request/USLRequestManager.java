package fr.cls.atoll.motu.web.usl.request;

import java.io.IOException;

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
import fr.cls.atoll.motu.web.bll.request.IBLLRequestManager;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.request.actions.AboutAction;
import fr.cls.atoll.motu.web.usl.request.actions.AbstractAction;
import fr.cls.atoll.motu.web.usl.request.actions.DebugAction;
import fr.cls.atoll.motu.web.usl.request.actions.DeleteAction;
import fr.cls.atoll.motu.web.usl.request.actions.DescribeCoverageAction;
import fr.cls.atoll.motu.web.usl.request.actions.DescribeProductAction;
import fr.cls.atoll.motu.web.usl.request.actions.DownloadProductAction;
import fr.cls.atoll.motu.web.usl.request.actions.GetRequestStatusAction;
import fr.cls.atoll.motu.web.usl.request.actions.GetSizeAction;
import fr.cls.atoll.motu.web.usl.request.actions.ListCatalogAction;
import fr.cls.atoll.motu.web.usl.request.actions.ListServicesAction;
import fr.cls.atoll.motu.web.usl.request.actions.LogoutAction;
import fr.cls.atoll.motu.web.usl.request.actions.PingAction;
import fr.cls.atoll.motu.web.usl.request.actions.ProductDownloadHomeAction;
import fr.cls.atoll.motu.web.usl.request.actions.ProductMetadataAction;
import fr.cls.atoll.motu.web.usl.request.actions.TimeCoverageAction;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

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

    @Override
    public void onNewRequest(HttpServletRequest request, HttpServletResponse response) throws MotuException, InvalidHTTPParameterException {
        String action = CommonHTTPParameters.getActionFromRequest(request).toLowerCase();
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
        case DeleteAction.ACTION_NAME:
            actionInst = new DeleteAction("009", request, response, getSession(request));
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
        default:
            // Nothing to do
        }
        IBLLRequestManager requestManager = BLLManager.getInstance().getRequestManager();
        Long requestId = -1L;
        try {
            if (actionInst != null) {
                if (!(actionInst instanceof DownloadProductAction)) {
                    requestId = requestManager.initRequest(actionInst.getLoginFromRequest(), actionInst.getUserHostName(), actionInst);
                    requestManager.setActionStatus(requestId, StatusModeType.INPROGRESS);
                    actionInst.doAction();
                    requestManager.setActionStatus(requestId, StatusModeType.DONE);
                } else {
                    actionInst.doAction();
                }
            } else {
                throw new MotuException(ErrorType.UNKNOWN_ACTION, "The requested action is unknown : " + action);
            }
        } catch (Exception e) {
            if (requestId != -1L) {
                requestManager.setActionStatus(requestId, StatusModeType.ERROR);
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
            }

            if (errorType == ErrorType.SYSTEM) {
                LOGGER.error(StringUtils.getLogMessage(actionCode, errorType, errMessage), e);
            }

            try {
                response.getWriter().write(StringUtils.getLogMessage(actionCode, errorType, errMessage));
            } catch (IOException e1) {
                LOGGER.error(StringUtils.getLogMessage(actionCode,
                                                       ErrorType.SYSTEM,
                                                       BLLManager.getInstance().getMessagesErrorManager().getMessageError(ErrorType.SYSTEM)),
                             e1);
            }
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
        // TODO SMA Need to understand what mode is ???
        boolean noMode = false;// RunnableHttpExtraction.noMode(mode);

        boolean createOk = false;
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_REFRESH);
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_LIST_SERVICES);
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_LIST_CATALOG);
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_LIST_PRODUCT_METADATA);
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_PRODUCT_DOWNLOADHOME);
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_PRODUCT_DOWNLOAD);

        return createOk && noMode;
    }

    /**
     * Initializes a new session with a new Organizer object.
     *
     * @param session session to initialize
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    private void initSession(HttpSession session) {

        // TODO SMA To implements : What to save in the session?
        // Organizer organizer;
        //
        // LOGGER.info(String.format("initSession - MaxInactiveInterval " +
        // session.getMaxInactiveInterval()));
        //
        // try {
        // if (session.isNew()) {
        // LOGGER.info("initSession - session is New ");
        // createOrganizer(session);
        // }
        // organizer = (Organizer) session.getAttribute(ORGANIZER_SESSION_ATTR);
        // if (organizer == null) {
        // LOG.info("initSession - session is not New but Organizer is null");
        // createOrganizer(session);
        // }
        // } catch (Exception e) {
        // throw new ServletException(e);
        // }
    }

    /** {@inheritDoc} */
    @Override
    public String getProductDownloadUrlPath(Product product) {
        if (StringUtils.isNullOrEmpty(product.getExtractFilename())) {
            return "";
        }

        String httpDownloadUrlBase = BLLManager.getInstance().getConfigManager().getMotuConfig().getDownloadHttpUrl();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(httpDownloadUrlBase);
        if (!(httpDownloadUrlBase.endsWith("/"))) {
            stringBuffer.append("/");
        }
        stringBuffer.append(product.getExtractFilename());

        return stringBuffer.toString();
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

}
