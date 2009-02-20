/**
 * 
 */
package fr.cls.atoll.motu;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * 
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:01:43 $
 */
public class MyAuthenticator extends Authenticator {
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("dearith", "bienvenue".toCharArray());
    }
}
