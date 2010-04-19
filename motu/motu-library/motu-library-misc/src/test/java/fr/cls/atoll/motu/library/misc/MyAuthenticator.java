/**
 * 
 */
package fr.cls.atoll.motu.library.misc;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * 
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.2 $ - $Date: 2010-02-26 13:52:43 $
 */
public class MyAuthenticator extends Authenticator {
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("login", "pwd".toCharArray());
    }
}
