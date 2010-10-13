package fr.cls.atoll.motu.authorization.filter;



/**
 * Filter implementation of AbstractSecureFilter to intercept all requests and attempt to authorize
 * the user.
 * <p>
 * This filter allows you to specify the following parameters (at either the context-level or the filter-level):
 * <ul>
 * <li><code>ldapAttribute</code> - ldap Attribute that contain pattern </li>
 * <li><code>defaultAuthorizedPattern</code> - the default pattern </li>
 * <li><code>errorRedirectFilterUrl</code> - The page to redirect if not authorized</li>
 * </ul>
 *
 * @author mhebert
 * @version $Revision: 1.1 $ - $Date: 2010/10/10 10:48:12 $
 */
public class SecureFilter extends AbstractSecureFilter {
	

	
    public boolean wildcardMatches(String pattern, String stringToMatch) {
        boolean match = false;
        int length = pattern.length();
        if(pattern.charAt(0) == '*') {
            if(length == 1) {
                match = true; // *
            } else if(pattern.charAt(length-1) == '*' && length > 2 && stringToMatch.contains(pattern.substring(1, length-3).toLowerCase())) {
                match = true; // *match*
            } else if(length > 1 && stringToMatch.endsWith(pattern.substring(1).toLowerCase())) {
                match = true; // *match
            }
        } else if(pattern.charAt(length-1) == '*' && stringToMatch.startsWith(pattern.substring(0, length-2).toLowerCase())) {
            match = true; // match*
        } else if(pattern.equalsIgnoreCase(stringToMatch)) { // match
            match = true;
        }
        return match;
    }


}
