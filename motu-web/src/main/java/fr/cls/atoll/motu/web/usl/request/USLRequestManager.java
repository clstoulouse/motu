package fr.cls.atoll.motu.web.usl.request;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.servlet.RunnableHttpExtraction;
import fr.cls.atoll.motu.web.usl.request.actions.DebugAction;
import fr.cls.atoll.motu.web.usl.request.actions.DeleteAction;
import fr.cls.atoll.motu.web.usl.request.actions.DownloadProductAction;
import fr.cls.atoll.motu.web.usl.request.actions.GetRequestStatusAction;
import fr.cls.atoll.motu.web.usl.request.actions.GetSizeAction;
import fr.cls.atoll.motu.web.usl.request.actions.LogoutAction;
import fr.cls.atoll.motu.web.usl.request.actions.PingAction;
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
        switch (CommonHTTPParameters.getActionFromRequest(request).toLowerCase()) {
        case PingAction.ACTION_NAME:
            new PingAction(request, response).doAction();
            break;
        case DebugAction.ACTION_NAME:
        case DebugAction.ACTION_NAME_ALIAS_QUEUE_SERVER:
            new DebugAction(request, response).doAction();
            break;
        case GetRequestStatusAction.ACTION_NAME:
            new GetRequestStatusAction(request, response).doAction();
            break;
        case GetSizeAction.ACTION_NAME:
            new GetSizeAction(request, response, getSession(request)).doAction();
            break;
        case LogoutAction.ACTION_NAME:
            new LogoutAction(request, response, getSession(request)).doAction();
            break;
        case DeleteAction.ACTION_NAME:
            new DeleteAction(request, response, getSession(request)).doAction();
            break;

        // Authenticated actions
        case DownloadProductAction.ACTION_NAME:
            new DownloadProductAction(request, response, getSession(request)).doAction();
            break;
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
        boolean noMode = RunnableHttpExtraction.noMode(mode);

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

}
