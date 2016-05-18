package fr.cls.atoll.motu.web.usl.request.session;

import javax.servlet.http.HttpSession;

import fr.cls.atoll.motu.library.misc.intfce.Organizer;

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
public class SessionManager {

    /** The Constant SESSION_AUTHORIZED_KEY. */
    private static final String SESSION_AUTHORIZED_KEY = "sessionAuthorized";

    /** The Constant SESSION_AUTHORIZED_USER. */
    private static final String SESSION_AUTHORIZED_USER = "sessionAuthorizedUser";

    /** The Constant ORGANIZER_SESSION_ATTR. */
    private static final String ORGANIZER_SESSION_ATTR = "organizer";

    private static SessionManager s_instance;

    public static SessionManager getInstance() {
        if (s_instance == null) {
            s_instance = new SessionManager();
        }
        return s_instance;
    }

    public SessionManager() {
    }

    /**
     * Invalidate session.
     * 
     * @param session the session
     */
    public void removeOrganizerSession(HttpSession session) {
        // To avoid to keep Organizer object in session (lot of memory allocated but
        // unused) invalidate the session, except in 'no mode'.
        if (session == null) {
            return;
        }
        session.removeAttribute(ORGANIZER_SESSION_ATTR);
    }

    /**
     * .
     * 
     * @param session
     * @return
     */
    public String getUserId(HttpSession session) {
        return (String) session.getAttribute(SESSION_AUTHORIZED_USER);
    }

    /**
     * .
     * 
     * @param session
     * @return
     */
    public Organizer getOrganizer(HttpSession session) {
        return (Organizer) session.getAttribute(ORGANIZER_SESSION_ATTR);
    }

    /**
     * .
     */
    public String getAuthorizedKey(HttpSession session) {
        return (String) session.getAttribute(SESSION_AUTHORIZED_KEY);
    }

    /**
     * .
     * 
     * @param b
     */
    public void setAuthorizedKey(HttpSession session, boolean isAuthorized) {
        session.setAttribute(SESSION_AUTHORIZED_KEY, isAuthorized);

    }

    /**
     * .
     * 
     * @param login
     */
    public void setAuthorizedUser(HttpSession session, String login) {
        session.setAttribute(SESSION_AUTHORIZED_USER, login);
    }

}
