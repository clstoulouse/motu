/**
 * 
 */
package fr.cls.atoll.motu.library.misc.intfce;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * A class use only in developpment for remote access through HTTP server using a proxy.
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * 
 */
public class SimpleAuthenticator extends Authenticator {

    private String username;
    private String password;

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
     * @return a password authentification.
     */
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password.toCharArray());
    }

}
