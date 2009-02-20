/**
 * 
 */
package fr.cls.atoll.motu.intfce;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * A class use only in developpment for remote access through HTTP server using a proxy.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:26 $
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
