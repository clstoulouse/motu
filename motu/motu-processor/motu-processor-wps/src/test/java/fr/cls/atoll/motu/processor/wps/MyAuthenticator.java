package fr.cls.atoll.motu.processor.wps;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * 
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MyAuthenticator extends Authenticator {
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("dearith", "bienvenue".toCharArray());
    }
}
