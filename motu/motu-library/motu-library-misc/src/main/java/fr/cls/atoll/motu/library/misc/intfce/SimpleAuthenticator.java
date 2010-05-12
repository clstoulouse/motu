package fr.cls.atoll.motu.library.misc.intfce;

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
     * @return a password authentification.
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password.toCharArray());
    }

}
