/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.library.cas.util;

import fr.cls.atoll.motu.api.message.AuthenticationMode;
import fr.cls.atoll.motu.library.cas.UserBase;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class AuthenticationHolder {
    /**
     * ThreadLocal to hold the Assertion for Threads to access.
     */
    private static final ThreadLocal<UserBase> threadLocal = new ThreadLocal<UserBase>();

    /**
     * Retrieve the assertion from the ThreadLocal.
     */
    public static Boolean isCASAuthentication() {
        UserBase user = threadLocal.get();
        if (user == null) {
            return false;
        }

        return user.isCASAuthentication();

    }

    /**
     * Checks if is authentication.
     * 
     * @return the boolean
     */
    public static Boolean isAuthentication() {
        UserBase user = threadLocal.get();
        if (user == null) {
            return false;
        }

        return user.isAuthentication();

    }

    /**
     * Gets the authentication mode.
     * 
     * @return the authentication mode
     */
    public static AuthenticationMode getAuthenticationMode() {
        UserBase user = threadLocal.get();
        if (user == null) {
            return AuthenticationMode.NONE;
        }

        return user.getAuthenticationMode();

    }

    /**
     * Sets the user.
     * 
     * @param user the user
     * 
     * @return the user
     */
    public static void setUser(UserBase user) {
        threadLocal.set(user);
    }

    /**
     * Gets the user.
     * 
     * @return the user
     */
    public static UserBase getUser() {
        return threadLocal.get();
    }

    /**
     * Gets the user login.
     * 
     * @return the user login
     */
    public static String getUserLogin() {
        UserBase user = AuthenticationHolder.getUser();
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
     * @param casAuthentication the cas authentication
     */
    public static void setCASAuthentication(final Boolean casAuthentication) {
        UserBase user = AuthenticationHolder.getUser();
        if (user == null) {
            user = new UserBase();
        }
        user.setCASAuthentication(casAuthentication);
        AuthenticationHolder.setUser(user);
    }

    /**
     * Sets the cas authentication.
     * 
     * @param user the new cas authentication
     */
    public static void setCASAuthentication(final UserBase user) {
        AuthenticationHolder.setUser(user);
    }

    /**
     * Clear the ThreadLocal.
     */
    public static void clear() {
        threadLocal.set(null);
    }
}
