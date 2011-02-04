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

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * A class use only in developpment for remote access through HTTP server using a proxy.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class SimpleAuthenticator extends Authenticator {

    private final String username;
    private final String password;

    /**
     * @param username username
     * @param password password
     */
    public SimpleAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @see java.net.Authenticator#getPasswordAuthentication()
     * @return a password authentication.
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password.toCharArray());
    }

}
