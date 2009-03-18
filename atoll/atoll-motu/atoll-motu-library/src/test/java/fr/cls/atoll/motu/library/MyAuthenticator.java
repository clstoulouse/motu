/**
 * 
 */
package fr.cls.atoll.motu.library;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * 
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */
public class MyAuthenticator extends Authenticator {
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("dearith", "bienvenue".toCharArray());
    }
}
