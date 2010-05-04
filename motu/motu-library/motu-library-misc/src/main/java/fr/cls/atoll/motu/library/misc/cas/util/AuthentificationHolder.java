package fr.cls.atoll.motu.library.misc.cas.util;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.AuthentificationMode;
import fr.cls.atoll.motu.library.misc.intfce.User;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2010. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2010-03-04 16:05:15 $
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
    public static AuthentificationMode getAuthentificationMode() {
        User user = threadLocal.get();
        if (user == null) {
            return AuthentificationMode.NONE;
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
        String login =  user.getLogin();
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
        AuthentificationMode authentificationMode = ((casAuthentification) ? AuthentificationMode.CAS : AuthentificationMode.NONE);
        user.setAuthentificationMode(authentificationMode);
        threadLocal.set(user);
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
