/**
 * 
 */
package fr.cls.atoll.motu.processor.wps;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * 
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-08-20 16:11:11 $
 */
public class MyAuthenticator extends Authenticator {
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("dearith", "bienvenue".toCharArray());
    }
}
