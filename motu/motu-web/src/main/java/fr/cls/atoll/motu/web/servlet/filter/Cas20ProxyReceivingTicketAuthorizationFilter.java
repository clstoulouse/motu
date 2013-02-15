/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */

package fr.cls.atoll.motu.web.servlet.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.security.Principal;

import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.authentication.AttributePrincipal;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.configuration.ConfigService;
import fr.cls.atoll.motu.library.misc.configuration.MotuConfig;
import fr.cls.atoll.motu.library.misc.exception.MotuException;

public class Cas20ProxyReceivingTicketAuthorizationFilter implements Filter {

    /**
     * Authorization Filter
     * 
     * @param servletResponse object that contains the response the servlet sends to the client
     * @param servletRequest object that contains the request the client has made of the servlet.
     * @param filterChain object to describe filter chain
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
	public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) 
			throws IOException, ServletException {
	
		// Get Request
		final HttpServletRequest request = (HttpServletRequest) servletRequest;
		final HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		// Get LDAP attributes from request
		Principal p = request.getUserPrincipal();
		AttributePrincipal principal = (AttributePrincipal)p;		
		Map attributes = principal.getAttributes();
		
		// Get the MOTU service
		String service = request.getParameter(MotuRequestParametersConstant.PARAM_SERVICE);
		
		// Authorization (only for service)
		if (service != null) {
			
			// Load MOTU configuration
			MotuConfig conf = null;
			try {
				conf = Organizer.getMotuConfigInstance();
			} catch (MotuException e) {
				e.printStackTrace();
			}			
					
			// Find the service in (MotuConfig, list of ConfigService) and check profiles from ldap attributes vs profile from MotuConfig
			boolean match = match_ldap_vs_motu(attributes, conf, service);
			if (!match) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		}
		
		filterChain.doFilter(request, response);
	}	
	
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	public void destroy() {
		// TODO Auto-generated method stub
	}
	
    /**
     * match_ldap_vs_motu 
     * 
     * @param attributes LDAP attributes
     * @param conf object that contains MotuConfig
     * @param selected_service The service selected by the user
     * 
     * @return boolean access granted/denied
     */
	private boolean match_ldap_vs_motu (Map attributes, MotuConfig conf, String selected_service) {
		
		// LDAP profiles from user X
		String profiles_ldap = "";
		Iterator attributeNames = attributes.keySet().iterator();
		
		
		for (;attributeNames.hasNext();) { 
			String attr = (String)attributeNames.next();
			String[] attr_spl = attr.split("\""); 
		    if (attr_spl.length != 5)	continue; // check valid attribute [name,value]
		    			
		    if (attr_spl[1].equals("profiles"))  { 
		    	profiles_ldap = attr_spl[3];
            	break; // found it
            }		    
		}
		
        // Check if the user is valid (must have at least one profile)
        if (profiles_ldap==null || profiles_ldap.isEmpty())	return false;
        
        // MotuConfig profiles for service Y       
		String profiles_conf = "";
		List<ConfigService> list_serv = conf.getConfigService();
		Iterator it_s = list_serv.iterator();
		
        while (it_s.hasNext()) {
        	ConfigService cs = (ConfigService)it_s.next();
            if (cs.getName().equals(selected_service))  {
            	profiles_conf = cs.getProfiles(); 
            	break;
            }
        }	
        
        // If there is a profile restriction for service Y 
        if (profiles_conf!=null && !profiles_conf.isEmpty()) {        	
        	String[] profiles_c = profiles_conf.split(",");
        	String[] profiles_l = profiles_ldap.split(",");
        	
        	for (String p : profiles_c) {
        		for (String l : profiles_l) {
        			if (p.trim().equals(l.trim()))	return true; // profile match
        		}
        	}
        	return false; // profiles don't match -> deny access to service Y for user X
        }
        
		return true; // everything ok (no profile restriction)
	}
}