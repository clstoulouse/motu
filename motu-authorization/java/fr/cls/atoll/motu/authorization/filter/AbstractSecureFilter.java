package fr.cls.atoll.motu.authorization.filter;


import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract Filter to intercept all requests and attempt to authorize
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
public abstract class AbstractSecureFilter implements Filter {
	
	FilterConfig filterConfig;
	
	/**
	 *  Error redirect page.
	 */
	private String ErrorRedirectPage;
	
	/**
	 * Default pattern.
	 */
	private String defaultAuthorizedPattern = null;
	
	
	/**
	 *  ldap Attribute that contain pattern.
	 */
	private String ldapAttribute = "businessCategory";
	
	private String protocol,
    user,
    password,
    host,
    directory,
    file,
    query,
    ref;
	private int port = -1;


    public void init(final FilterConfig filterConfig) throws ServletException {
    	this.filterConfig = filterConfig;
    	
    	final Enumeration enumeration = filterConfig.getInitParameterNames();
		
		while (enumeration.hasMoreElements()) {
			final String paramName = (String) enumeration.nextElement();
			if (paramName.equals("defaultAuthorizedPattern")) {
				this.defaultAuthorizedPattern = filterConfig.getInitParameter(paramName);
			}
			if (paramName.equals("errorRedirectFilterUrl")) {
				this.ErrorRedirectPage = filterConfig.getInitParameter(paramName);
			}
			if (paramName.equals("ldapAttribute")) {
				this.ldapAttribute = filterConfig.getInitParameter(paramName);
			}
		}
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpSession session = request.getSession(false);
        final Assertion assertion = (Assertion) (session == null ? request.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION));
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        try {
        	String[] mypattern = new String[1];
        	boolean autorized = false;
            String path = request.getRequestURI();
            
            // Find attribute
            Principal p = request.getUserPrincipal();
            AttributePrincipal principal = (AttributePrincipal)p;
    		Map attributes = principal.getAttributes();
    		
    		if (attributes != null && attributes.size() > 0 ) {
    			// Decode URL
    			WildcardURL(path);
    			// Retrieve authorized pattern from attribute
    			String pattern = (String)attributes.get(ldapAttribute);
    			if(null == pattern){
    				if(null != defaultAuthorizedPattern){
    					wildcardMatches(defaultAuthorizedPattern, this.directory);
    				}	
    			}else{
    				mypattern = pattern.split(",");
    				// Verify pattern with URL
        			for(String pat : mypattern){
        				if(wildcardMatches(pat, this.directory)){
        	    			autorized = true;
        	    			}
        			}
    			}
    			

    		}
    		
    		// If authorize continue
            if (autorized) {
            	AssertionHolder.setAssertion(assertion);
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            } else if (filterConfig != null) {
            	// Else redirect to error page if exist
                if (ErrorRedirectPage != null && !"".equals(ErrorRedirectPage)) {
                    filterConfig.getServletContext().getRequestDispatcher(ErrorRedirectPage).
                    forward(servletRequest, servletResponse);
                    return;
                }
            }
            // If error page is not initialized
            // send error code 401.
            httpResponse .sendError(401);
            AssertionHolder.setAssertion(assertion);
            filterChain.doFilter(servletRequest, servletResponse);
            
            
        } finally {
            AssertionHolder.clear();
        }
    }
    
    /**
     * Decode URL.
     */
    public void WildcardURL(String url) {
        HashMap<String, String> tempUri = new HashMap<String, String>(14);
        String[] parts = {"source","protocol","authority","userInfo","user","password","host","port","relative","path","directory","file","query","ref"};
        boolean strictMode = false;
        Pattern pattern;
        if(strictMode) {
            pattern = Pattern.compile("^(?:([^:/?#]+):)?(?://((?:(([^:@]*):?([^:@]*))?@)?([^:/?#]*)(?::(\\d*))?))?((((?:[^?#/]*/)*)([^?#]*))(?:\\?([^#]*))?(?:#(.*))?)");
        } else {
            pattern = Pattern.compile("^(?:(?![^:@]+:[^:@/]*@)([^:/?#.]+):)?(?://)?((?:(([^:@]*):?([^:@]*))?@)?([^:/?#]*)(?::(\\d*))?)(((/(?:[^?#](?![^?#/]*\\.[^?#/.]+(?:[?#]|$)))*/?)?([^?#/]*))(?:\\?([^#]*))?(?:#(.*))?)");
        }

    	Matcher matcher = pattern.matcher(url);
        String match;
        if(matcher.find()) {
            for(int i=0;i<14;i++) {
                try {
                    match = matcher.group(i);
                } catch(Exception ex) {
                    match = "*";
                }
                tempUri.put(parts[i],  match == null ? "*" : match);
            }
    	}
        this.protocol = tempUri.get("protocol");
        this.user = tempUri.get("user");
        this.password = tempUri.get("password");
        this.host = tempUri.get("host");
        this.directory = tempUri.get("directory");
        this.file = tempUri.get("file");
        this.query = tempUri.get("query");
        this.ref = tempUri.get("ref");
        try {
            this.port = Integer.parseInt(tempUri.get("port"));
        } catch(NumberFormatException ignore) {}
    }

    /**
     * Gets whether a string matches a wildcard pattern. The following would be considered to be matches:
     *      <code>*pattern   somepattern</code>
     *      <code>pattern*   patternsome</code>
     *      <code>*pattern*  somepatternsome</code>
     * @param pattern The pattern to check
     * @param stringToMatch The string to check
     * @return <code>True</code> if the wildcard matches the pattern, otherwise <code>false</code>
     */

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


    public void destroy() {
        // nothing to do
    }
}