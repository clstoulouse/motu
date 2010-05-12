package fr.cls.atoll.motu.library.misc.cas.util;

import fr.cls.atoll.motu.api.message.AuthenticationMode;
import fr.cls.atoll.motu.library.misc.intfce.User;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class AuthentificationHolder {
    /**
     * ThreadLocal to hold the Assertion for Threads to access.
     */
    private static final ThreadLocal<User> threadLocal = new ThreadLocal<User>();

    /**
     * Retrieve the assertion from the ThreadLocal.
     */
    public static Boolean isCASAuthentification() {
        User user = threadLocal.get();
        if (user == null) {
            return false;
        }

        return user.isCASAuthentification();

    }

    /**
     * Checks if is authentification.
     * 
     * @return the boolean
     */
    public static Boolean isAuthentification() {
        User user = threadLocal.get();
        if (user == null) {
            return false;
        }

        return user.isAuthentification();

    }

    /**
     * Gets the authentification mode.
     * 
     * @return the authentification mode
     */
    public static AuthenticationMode getAuthentificationMode() {
        User user = threadLocal.get();
        if (user == null) {
            return AuthenticationMode.NONE;
        }

        return user.getAuthentificationMode();

    }

    /**
     * Sets the user.
     * 
     * @param user the user
     * 
     * @return the user
     */
    public static void setUser(User user) {
        threadLocal.set(user);
    }

    /**
     * Gets the user.
     * 
     * @return the user
     */
    public static User getUser() {
        return threadLocal.get();
    }

    /**
     * Gets the user login.
     * 
     * @return the user login
     */
    public static String getUserLogin() {
        User user = AuthentificationHolder.getUser();
        if (user == null) {
            return "(null)";
        }
        String login = user.getLogin();
        if (login == null) {
            return "(null)";
        }
        if (login == "") {
            return "(empty)";
        }
        return login;
    }

    /**
     * Add the Assertion to the ThreadLocal.
     * 
     * @param casAuthentification the cas authentification
     */
    public static void setCASAuthentification(final Boolean casAuthentification) {
        User user = AuthentificationHolder.getUser();
        if (user == null) {
            user = new User();
        }
        user.setCASAuthentification(casAuthentification);
        AuthentificationHolder.setUser(user);
    }

    /**
     * Sets the cas authentification.
     * 
     * @param user the new cas authentification
     */
    public static void setCASAuthentification(final User user) {
        AuthentificationHolder.setUser(user);
    }

    /**
     * Clear the ThreadLocal.
     */
    public static void clear() {
        threadLocal.set(null);
    }
}
