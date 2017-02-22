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
package fr.cls.atoll.motu.library.cas.util;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;

import com.sun.jersey.api.client.Client;

import fr.cls.atoll.motu.library.cas.UserBase;
import fr.cls.atoll.motu.library.cas.exception.MotuCasBadRequestException;
import fr.cls.atoll.motu.library.cas.exception.MotuCasException;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class AssertionUtils {

    /** Logger for this class. */
    private static final Logger LOG = LogManager.getLogger();

    /** The Constant TICKET_PARAMETER. */
    public static final String TICKET_PARAMETER = "ticket";

    public static final String TICKET_PARAMETER_AS_GET_FIRST_POS = "?" + AssertionUtils.TICKET_PARAMETER + "=";
    public static final String TICKET_PARAMETER_AS_GET = "&" + AssertionUtils.TICKET_PARAMETER + "=";

    /**
     * Gets the attribute principal name.
     * 
     * @return the attribute principal name
     */
    public static String getAttributePrincipalName() {
        Assertion assertion = AssertionHolder.getAssertion();
        return getAttributePrincipalName(assertion);
    }

    /**
     * Gets the attribute principal name.
     * 
     * @param assertion the assertion
     * 
     * @return the attribute principal name
     */
    public static String getAttributePrincipalName(Assertion assertion) {

        AttributePrincipal attributePrincipal = AssertionUtils.getAttributePrincipal(assertion);

        String name = "";

        if (attributePrincipal != null) {
            name = attributePrincipal.getName();
        }
        return name;
    }

    /**
     * Checks for cas ticket.
     * 
     * @param targetService the target service
     * 
     * @return true, if successful
     * 
     */
    public static boolean hasCASTicket(String targetService) {
        return (targetService.indexOf(AssertionUtils.TICKET_PARAMETER_AS_GET_FIRST_POS) != -1)
                || (targetService.indexOf(AssertionUtils.TICKET_PARAMETER_AS_GET) != -1);
    }

    /**
     * Checks for cas ticket.
     * 
     * @param targetService the target service
     * @return true, if successful
     */
    public static boolean hasCASTicket(URL targetService) {
        return hasCASTicket(targetService.toString());
    }

    /**
     * Checks for cas ticket.
     * 
     * @param data the data
     * @return true, if successful
     */
    public static boolean hasCASTicket(MultivaluedMap<String, String> data) {
        return (data.containsKey(AssertionUtils.TICKET_PARAMETER));
    }

    /**
     * Gets the cAS ticket.
     * 
     * @param data the data
     * @return the cAS ticket
     */
    public static String getCASTicket(MultivaluedMap<String, String> data) {
        return data.getFirst(AssertionUtils.TICKET_PARAMETER);
    }

    /**
     * Adds the cas ticket.
     * 
     * @param targetService the target service
     * @param user the user
     * @return the string
     * @throws MotuCasException the motu cas exception
     * @throws IOException
     * @throws MotuCasException
     * @throws MotuCasException
     */
    public static String addCASTicket(String targetService, UserBase user, boolean useAssertion) throws IOException, MotuCasException {

        if (user == null) {
            return AssertionUtils.addCASTicket(targetService, null, null, null, useAssertion);
        }
        return AssertionUtils.addCASTicket(targetService, user.getLogin(), user.getPwd(), user.getCasRestSuffURL(), useAssertion);

    }

    /**
     * Adds the cas ticket.
     *
     * @param targetService the target service
     * @param user the user
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasException the motu cas exception
     */
    public static String addCASTicket(String targetService, UserBase user) throws IOException, MotuCasException {
        return AssertionUtils.addCASTicket(targetService, user, true);

    }

    /**
     * Adds the cas ticket.
     * 
     * @param client the client
     * @param method the method
     * @param targetService the target service
     * @param user the user
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasException the motu cas exception
     */
    public static String addCASTicket(Client client, RestUtil.HttpMethod method, String targetService, UserBase user)
            throws IOException, MotuCasException {

        if (user == null) {
            return AssertionUtils.addCASTicket(client, method, targetService, null, null, null, null);
        }
        return AssertionUtils.addCASTicket(client, method, targetService, user.getLogin(), user.getPwd(), user.getCasRestSuffURL(), user.getCasURL());

    }

    /**
     * Adds the cas ticket.
     * 
     * @param targetService the target service
     * @param user the user
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasException the motu cas exception
     */
    public static URL addCASTicket(URL targetService, UserBase user) throws IOException, MotuCasException {

        String returnString = AssertionUtils.addCASTicket(targetService.toString(), user);
        return new URL(returnString);

    }

    /**
     * Adds the cas ticket.
     * 
     * @param targetService the target service
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasException
     */
    public static URL addCASTicket(URL targetService) throws IOException, MotuCasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(URL) - entering");
        }

        URL returnURL = AssertionUtils.addCASTicket(targetService, null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(URL) - exiting");
        }
        return returnURL;
    }

    /**
     * Adds the cas ticket.
     * 
     * @param targetService the target service
     * 
     * @return the string
     * @throws IOException
     * @throws MotuCasBadRequestException
     * 
     * @throws URIException the URI exception
     */
    public static String addCASTicket(String targetService, boolean useAssertion) throws IOException, MotuCasBadRequestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String) - entering");
        }

        String returnString = AssertionUtils.addCASTicket(targetService, null, null, null, useAssertion);
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String) - exiting");
        }
        return returnString;
    }

    /**
     * Adds the cas ticket.
     *
     * @param targetService the target service
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static String addCASTicket(String targetService) throws IOException, MotuCasBadRequestException {
        return AssertionUtils.addCASTicket(targetService, true);
    }

    /**
     * Adds the cas ticket.
     * 
     * @param client the client
     * @param method the method
     * @param targetService the target service
     * @param username the username
     * @param password the password
     * @param casRestUrlSuffix the cas rest url suffix
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static String addCASTicket(Client client,
                                      RestUtil.HttpMethod method,
                                      String targetService,
                                      String username,
                                      String password,
                                      String casRestUrlSuffix,
                                      boolean useAssertion)
            throws IOException, MotuCasBadRequestException {

        return AssertionUtils.addCASTicket(client, method, targetService, username, password, casRestUrlSuffix, null);
    }

    /**
     * Adds the cas ticket.
     *
     * @param client the client
     * @param method the method
     * @param targetService the target service
     * @param username the username
     * @param password the password
     * @param casRestUrlSuffix the cas rest url suffix
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static String addCASTicket(Client client,
                                      RestUtil.HttpMethod method,
                                      String targetService,
                                      String username,
                                      String password,
                                      String casRestUrlSuffix)
            throws IOException, MotuCasBadRequestException {

        return AssertionUtils.addCASTicket(client, method, targetService, username, password, casRestUrlSuffix, true);
    }

    /**
     * Adds the cas ticket.
     * 
     * @param client the client
     * @param method the method
     * @param targetService the target service
     * @param username the username
     * @param password the password
     * @param casRestUrlSuffix the cas rest url suffix
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static String addCASTicket(Client client,
                                      RestUtil.HttpMethod method,
                                      String targetService,
                                      String username,
                                      String password,
                                      String casRestUrlSuffix,
                                      String casUrlToUse,
                                      boolean useAssertion)
            throws IOException, MotuCasBadRequestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(Client, RestUtil.HttpMethod, String, String, String, String) - start");
        }

        if (client == null) {
            return null;
        }

        String returnString = targetService;
        String casRestUrlSuffixToUse = casRestUrlSuffix;

        Assertion assertion = null;
        if (useAssertion) {
            assertion = AssertionHolder.getAssertion();
        }
        if (assertion != null) {
            returnString = AssertionUtils.addCASTicket(assertion, targetService);
        } else if (!AssertionUtils.isNullOrEmpty(username)) {
            if (AssertionUtils.isNullOrEmpty(casRestUrlSuffixToUse)) {
                casRestUrlSuffixToUse = RestUtil.CAS_REST_URL_SUFFIX;
            }
            returnString = AssertionUtils.addCASTicketFromTGT(client, method, casRestUrlSuffixToUse, username, password, targetService, casUrlToUse);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(Client, RestUtil.HttpMethod, String, String, String, String) - end");
        }
        return returnString;
    }

    /**
     * Adds the cas ticket.
     *
     * @param client the client
     * @param method the method
     * @param targetService the target service
     * @param username the username
     * @param password the password
     * @param casRestUrlSuffix the cas rest url suffix
     * @param casUrlToUse the cas url to use
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static String addCASTicket(Client client,
                                      RestUtil.HttpMethod method,
                                      String targetService,
                                      String username,
                                      String password,
                                      String casRestUrlSuffix,
                                      String casUrlToUse)
            throws IOException, MotuCasBadRequestException {
        return AssertionUtils.addCASTicket(client, method, targetService, username, password, casRestUrlSuffix, casUrlToUse, true);

    }

    /**
     * Adds the cas ticket.
     * 
     * @param targetService the target service
     * @param username the username
     * @param password the password
     * @param casRestUrlSuffix the cas rest url suffix
     * 
     * @return the string
     * @throws IOException
     * 
     * @throws MotuException the motu exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException
     */
    public static String addCASTicket(String targetService, String username, String password, String casRestUrlSuffix, boolean useAssertion)
            throws IOException, MotuCasBadRequestException {
        LOG.debug("addCASTicket(String, String, String, String) - entering: (" + username + ") " + targetService);

        String returnString = targetService;
        String casRestUrlSuffixToUse = casRestUrlSuffix;

        Assertion assertion = null;
        if (useAssertion) {
            assertion = AssertionHolder.getAssertion();
        }
        if (assertion != null) {
            returnString = AssertionUtils.addCASTicket(assertion, targetService);
        } else if (!AssertionUtils.isNullOrEmpty(username)) {
            if (AssertionUtils.isNullOrEmpty(casRestUrlSuffixToUse)) {
                casRestUrlSuffixToUse = RestUtil.CAS_REST_URL_SUFFIX;
            }
            returnString = AssertionUtils.addCASTicketFromTGT(casRestUrlSuffixToUse, username, password, targetService);
        }

        LOG.debug("addCASTicket(String, String, String, String) - exiting: (" + username + ") " + targetService);
        return returnString;

    }

    /**
     * Adds the cas ticket.
     *
     * @param targetService the target service
     * @param username the username
     * @param password the password
     * @param casRestUrlSuffix the cas rest url suffix
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static String addCASTicket(String targetService, String username, String password, String casRestUrlSuffix)
            throws IOException, MotuCasBadRequestException {
        return AssertionUtils.addCASTicket(targetService, username, password, casRestUrlSuffix, true);

    }

    /**
     * Gets the cAS ticket.
     *
     * @param targetService the target service
     * @param username the username
     * @param password the password
     * @param casRestUrlSuffix the cas rest url suffix
     * @return the cAS ticket
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static String getCASTicket(String targetService, String username, String password, String casRestUrlSuffix)
            throws IOException, MotuCasBadRequestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCASTicket(String, String, String, String) - entering");
        }
        String ticket = null;

        if (AssertionUtils.isNullOrEmpty(username)) {
            return ticket;
        }

        String casRestUrlSuffixToUse = casRestUrlSuffix;

        if (AssertionUtils.isNullOrEmpty(casRestUrlSuffixToUse)) {
            casRestUrlSuffixToUse = RestUtil.CAS_REST_URL_SUFFIX;
        }

        String casRestUrl = RestUtil.getCasRestletUrl(targetService, casRestUrlSuffixToUse);

        ticket = RestUtil.loginToCAS(casRestUrl, username, password, targetService);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getCASTicket(String, String, String, String) - exiting");
        }
        return ticket;

    }

    /**
     * Adds the cas ticket.
     * 
     * @param targetService the target service
     * @param data the data
     * @param user the user
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasException the motu cas exception
     */
    public static String addCASTicket(URI targetService, MultivaluedMap<String, String> data, UserBase user) throws IOException, MotuCasException {
        return AssertionUtils.addCASTicket(targetService.toString(), data, user.getLogin(), user.getPwd(), user.getCasRestSuffURL());
    }

    /**
     * Adds the cas ticket.
     * 
     * @param targetService the target service
     * @param data the data
     * @param username the username
     * @param password the password
     * @param casRestUrlSuffix the cas rest url suffix
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException
     */
    public static String addCASTicket(URI targetService,
                                      MultivaluedMap<String, String> data,
                                      String username,
                                      String password,
                                      String casRestUrlSuffix)
            throws IOException, MotuCasBadRequestException {
        return AssertionUtils.addCASTicket(targetService.toString(), data, username, password, casRestUrlSuffix);
    }

    /**
     * Adds the cas ticket.
     * 
     * @param targetService the target service
     * @param data the data
     * @param username the username
     * @param password the password
     * @param casRestUrlSuffix the cas rest url suffix
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException
     */
    public static String addCASTicket(String targetService,
                                      MultivaluedMap<String, String> data,
                                      String username,
                                      String password,
                                      String casRestUrlSuffix)
            throws IOException, MotuCasBadRequestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String, MultivaluedMap<String,String>, String, String, String) - start");
        }

        String returnString = "";
        String casRestUrlSuffixToUse = casRestUrlSuffix;

        Assertion assertion = AssertionHolder.getAssertion();
        if (assertion != null) {
            returnString = AssertionUtils.addCASTicket(assertion, targetService, data);
        } else if (!AssertionUtils.isNullOrEmpty(username)) {
            if (AssertionUtils.isNullOrEmpty(casRestUrlSuffixToUse)) {
                casRestUrlSuffixToUse = RestUtil.CAS_REST_URL_SUFFIX;
            }
            returnString = AssertionUtils.addCASTicketFromTGT(casRestUrlSuffix, username, password, targetService, data);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String, MultivaluedMap<String,String>, String, String, String) - end");
        }
        return returnString;

    }

    /**
     * Adds the cas ticket.
     * 
     * @param assertion the assertion
     * @param targetService the target service
     * @param data the data
     * @return the string
     */
    public static String addCASTicket(Assertion assertion, String targetService, MultivaluedMap<String, String> data) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(Assertion, String, MultivaluedMap<String,String>) - start");
        }

        String ticket = "";

        if (assertion == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicket(Assertion, String, MultivaluedMap<String,String>) - end - assertion is null");
            }
            return ticket;
        }
        if (data == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicket(Assertion, String, MultivaluedMap<String,String>) - end - data is null");
            }
            return ticket;
        }

        if (AssertionUtils.hasCASTicket(data)) {
            String returnString = AssertionUtils.getCASTicket(data);
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicket(Assertion, String, MultivaluedMap<String,String>) - end - data has already a CAS ticket");
            }
            return returnString;
        }

        ticket = AssertionUtils.getProxyTicketFor(assertion, targetService);

        if (AssertionUtils.isNullOrEmpty(ticket)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicket(Assertion, String, MultivaluedMap<String,String>) - end - ticket is null or empty");
            }
            return ticket;
        }

        data.add(AssertionUtils.TICKET_PARAMETER, ticket);

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(Assertion, String, MultivaluedMap<String,String>) - end");
        }
        return ticket;

    }

    /**
     * Adds the cas ticket from tgt.
     * 
     * @param client the client
     * @param method the method
     * @param casRestUrlSuffix the cas rest url suffix
     * @param username the username
     * @param password the password
     * @param targetService the target service
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static String addCASTicketFromTGT(Client client,
                                             RestUtil.HttpMethod method,
                                             String casRestUrlSuffix,
                                             String username,
                                             String password,
                                             String targetService,
                                             String casUrlToUse)
            throws IOException, MotuCasBadRequestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicketFromTGT(Client, RestUtil.HttpMethod, String, String, String, String) - start");
        }

        if (client == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(Client, RestUtil.HttpMethod, String, String, String, String) - end");
            }
            return targetService;
        }
        if (AssertionUtils.isNullOrEmpty(username)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(Client, RestUtil.HttpMethod, String, String, String, String) - end");
            }
            return targetService;
        }
        if (AssertionUtils.isNullOrEmpty(targetService)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(Client, RestUtil.HttpMethod, String, String, String, String) - end");
            }
            return targetService;
        }
        if (AssertionUtils.hasCASTicket(targetService)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(Client, RestUtil.HttpMethod, String, String, String, String) - end");
            }
            return targetService;
        }

        String casRestUrl = "";

        if (AssertionUtils.isNullOrEmpty(casUrlToUse)) {
            casRestUrl = RestUtil.getCasRestletUrl(client, method, targetService, casRestUrlSuffix);
        } else {
            casRestUrl = RestUtil.appendPath(casUrlToUse, casRestUrlSuffix);
        }

        String ticket = RestUtil.loginToCAS(casRestUrl, username, password, targetService);

        if (AssertionUtils.isNullOrEmpty(ticket)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(Client, RestUtil.HttpMethod, String, String, String, String) - end");
            }
            return targetService;
        }

        String returnString = AssertionUtils.addCASTicket(ticket, targetService);

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicketFromTGT(Client, RestUtil.HttpMethod, String, String, String, String) - end");
        }
        return returnString;

    }

    /**
     * Adds the cas ticket from tgt.
     *
     * @param casRestUrlSuffix the cas rest url suffix
     * @param username the username
     * @param password the password
     * @param targetService the target service
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static String addCASTicketFromTGT(String casRestUrlSuffix, String username, String password, String targetService)
            throws IOException, MotuCasBadRequestException {
        return AssertionUtils.addCASTicketFromTGT(casRestUrlSuffix, username, password, targetService, (String) null);

    }

    /**
     * Adds the cas ticket from tgt.
     * 
     * @param casRestUrlSuffix the cas rest url suffix
     * @param username the username
     * @param password the password
     * @param targetService the target service
     * 
     * @return the string
     * @throws IOException
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException
     * @throws MotuException the motu exception
     */
    public static String addCASTicketFromTGT(String casRestUrlSuffix, String username, String password, String targetService, String casUrlToUse)
            throws IOException, MotuCasBadRequestException {
        LOG.debug("addCASTicketFromTGT(String, String, String, String) - entering (" + username + ") " + targetService);

        if (AssertionUtils.isNullOrEmpty(username)) {
            LOG.debug("addCASTicketFromTGT(String, String, String, String) - exiting (" + username + ") " + targetService);
            return targetService;
        }
        if (AssertionUtils.isNullOrEmpty(targetService)) {
            LOG.debug("addCASTicketFromTGT(String, String, String, String) - exiting (" + username + ") " + targetService);
            return targetService;
        }
        if (AssertionUtils.hasCASTicket(targetService)) {
            LOG.debug("addCASTicketFromTGT(String, String, String, String) - exiting - URL has already a cas ticket (" + username + ") "
                    + targetService);
            return targetService;
        }

        String casRestUrl = "";

        if (AssertionUtils.isNullOrEmpty(casUrlToUse)) {
            casRestUrl = RestUtil.getCasRestletUrl(targetService, casRestUrlSuffix);
        } else {
            casRestUrl = RestUtil.appendPath(casUrlToUse, casRestUrlSuffix);
        }

        String ticket = RestUtil.loginToCAS(casRestUrl, username, password, targetService);

        if (AssertionUtils.isNullOrEmpty(ticket)) {
            LOG.debug("addCASTicketFromTGT(String, String, String, String) - exiting (" + username + ") " + targetService);
            return targetService;
        }

        String returnString = AssertionUtils.addCASTicket(ticket, targetService);
        LOG.debug("addCASTicketFromTGT(String, String, String, String) - exiting (" + username + ") " + targetService);
        return returnString;

    }

    public static String addCASTicketFromTGT(String casRestUrlSuffix,
                                             String username,
                                             String password,
                                             String targetService,
                                             MultivaluedMap<String, String> data)
            throws IOException, MotuCasBadRequestException {

        return AssertionUtils.addCASTicketFromTGT(casRestUrlSuffix, username, password, targetService, data, null);
    }

    /**
     * Adds the cas ticket from tgt.
     * 
     * @param casRestUrlSuffix the cas rest url suffix
     * @param username the username
     * @param password the password
     * @param targetService the target service
     * @param data the data
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException
     */
    public static String addCASTicketFromTGT(String casRestUrlSuffix,
                                             String username,
                                             String password,
                                             String targetService,
                                             MultivaluedMap<String, String> data,
                                             String casUrlToUse)
            throws IOException, MotuCasBadRequestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicketFromTGT(String, String, String, String, MultivaluedMap<String,String>) - start");
        }

        String ticket = "";

        if (AssertionUtils.isNullOrEmpty(username)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String, String, MultivaluedMap<String,String>) - end - username is null or empty");
            }
            return ticket;
        }
        if (AssertionUtils.isNullOrEmpty(targetService)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String, String, MultivaluedMap<String,String>) - end - targetService is null or empty");
            }
            return ticket;
        }
        if (AssertionUtils.hasCASTicket(data)) {
            String returnString = AssertionUtils.getCASTicket(data);
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String, String, MultivaluedMap<String,String>) - end - data has already a CAS ticket");
            }
            return returnString;
        }

        String casRestUrl = "";

        if (AssertionUtils.isNullOrEmpty(casUrlToUse)) {
            casRestUrl = RestUtil.getCasRestletUrl(targetService, casRestUrlSuffix);
        } else {
            casRestUrl = RestUtil.appendPath(casUrlToUse, casRestUrlSuffix);
        }

        ticket = RestUtil.loginToCAS(casRestUrl, username, password, targetService);

        if (AssertionUtils.isNullOrEmpty(ticket)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String, String, MultivaluedMap<String,String>) - end - ticket is null or empty");
            }
            return ticket;
        }

        data.add(AssertionUtils.TICKET_PARAMETER, ticket);

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicketFromTGT(String, String, String, String, MultivaluedMap<String,String>) - end");
        }
        return ticket;

    }

    /**
     * Adds the cas ticket from tgt.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param targetService the target service
     * @param casRestUrl the cas rest url
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static String addCASTicketFromTGT(String ticketGrantingTicket, String targetService, String casRestUrl)
            throws IOException, MotuCasBadRequestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicketFromTGT(String, String, String) - entering");
        }

        if (AssertionUtils.isNullOrEmpty(ticketGrantingTicket)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String) - exiting");
            }
            return targetService;
        }
        if (AssertionUtils.isNullOrEmpty(targetService)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String) - exiting");
            }
            return targetService;
        }
        if (AssertionUtils.isNullOrEmpty(casRestUrl)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String) - exiting");
            }
            return targetService;
        }
        if (AssertionUtils.hasCASTicket(targetService)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String) - exiting - URL has already a cas ticket");
            }
            return targetService;
        }

        String ticket = RestUtil.loginToCASWithTGT(casRestUrl, ticketGrantingTicket, targetService);

        if (AssertionUtils.isNullOrEmpty(ticket)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String) - exiting");
            }
            return targetService;
        }

        String returnString = AssertionUtils.addCASTicket(ticket, targetService);
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicketFromTGT(String, String, String) - exiting");
        }
        return returnString;

    }

    /**
     * Adds the cas ticket.
     * 
     * @param assertion the assertion
     * @param targetService the target service
     * 
     * @return the string
     * 
     * @throws URIException the URI exception
     */
    public static String addCASTicket(Assertion assertion, String targetService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(Assertion, String) - entering : debugPGTFromSession " + AssertionUtils.debugPGT(assertion));
        }

        if (assertion == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicket(String) - exiting - assertion is null");
            }
            return targetService;
        }

        if (AssertionUtils.isNullOrEmpty(targetService)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicket(String) - exiting - targetService is null or empty");
            }
            return targetService;
        }

        if (AssertionUtils.hasCASTicket(targetService)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicket(Assertion, String) - exiting - URL has already a cas ticket ");
            }
            return targetService;
        }

        String ticket = AssertionUtils.getProxyTicketFor(assertion, targetService);

        if (AssertionUtils.isNullOrEmpty(ticket)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicket(Assertion, String) - exiting - proxy ticket is null or empty");
            }
            return targetService;
        }

        // StringBuffer stringBuffer = new StringBuffer();
        // stringBuffer.append(targetService);
        // stringBuffer.append(targetService.indexOf("?") != -1 ? "&" : "?");
        //
        // stringBuffer.append("ticket=");
        // stringBuffer.append(ticket);
        // return stringBuffer.toString();
        //
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("addCASTicket(String) - exiting - new URL = " + stringBuffer.toString());
        // }
        String returnString = AssertionUtils.addCASTicket(ticket, targetService);
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(Assertion, String) - exiting");
        }
        return returnString;

    }

    /**
     * Adds the cas ticket.
     * 
     * @param ticket the ticket
     * @param targetService the target service
     * 
     * @return the string
     */
    public static String addCASTicket(String ticket, String targetService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String, String) - entering");
        }
        if (AssertionUtils.isNullOrEmpty(ticket)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicket(String, String) - exiting - service/proxy ticket is null or empty");
            }
            return targetService;
        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(targetService);
        stringBuffer.append(targetService.indexOf("?") != -1 ? "&" : "?");

        stringBuffer.append("ticket=");
        stringBuffer.append(ticket);

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String, String) - exiting - new URL = " + stringBuffer.toString());
        }
        return stringBuffer.toString();

    }

    /**
     * Gets the proxy ticket for.
     * 
     * @param targetService the target service
     * 
     * @return the proxy ticket for
     */
    public static String getProxyTicketFor(String targetService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyTicketFor(String) - entering");
        }

        Assertion assertion = AssertionHolder.getAssertion();
        String returnString = getProxyTicketFor(assertion, targetService);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyTicketFor(String) - exiting");
        }
        return returnString;

    }

    /**
     * Gets the proxy ticket for.
     * 
     * @param assertion the assertion
     * @param targetService the target service
     * 
     * @return the proxy ticket for
     */
    public static String getProxyTicketFor(Assertion assertion, String targetService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyTicketFor(Assertion, String) - entering");
        }

        String ticket = "";

        if (assertion == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getProxyTicketFor(Assertion, String) - exiting - assertion is null");
            }
            return ticket;
        }

        AttributePrincipal attributePrincipal = AssertionUtils.getAttributePrincipal(assertion);

        if (attributePrincipal != null) {
            ticket = attributePrincipal.getProxyTicketFor(targetService);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyTicketFor(Assertion, String) - exiting");
        }
        return ticket;

    }

    /**
     * Gets the attribute principal.
     * 
     * @param assertion the assertion
     * 
     * @return the attribute principal
     */
    public static AttributePrincipal getAttributePrincipal(Assertion assertion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAttributePrincipal(Assertion) - entering");
        }

        if (assertion == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAttributePrincipal(Assertion) - exiting");
            }
            return null;
        }

        AttributePrincipal returnAttributePrincipal = assertion.getPrincipal();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAttributePrincipal(Assertion) - exiting");
        }
        return returnAttributePrincipal;
    }

    /**
     * Gets the attribute principal.
     * 
     * @return the attribute principal
     */
    public static AttributePrincipal getAttributePrincipal() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAttributePrincipal() - entering");
        }

        AttributePrincipal returnAttributePrincipal = AssertionUtils.getAttributePrincipal(AssertionHolder.getAssertion());
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAttributePrincipal() - exiting");
        }
        return returnAttributePrincipal;
    }

    /**
     * Test if a string is null or empty.
     * 
     * @param value string to be tested.
     * 
     * @return true if string is null or empty, otherwise false.
     */
    static public boolean isNullOrEmpty(String value) {
        if (value == null) {
            return true;
        }
        if (value.equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Debug pgt.
     * 
     * @return the string
     */
    public static String debugPGT() {
        Assertion assertion = AssertionHolder.getAssertion();
        return debugPGT(assertion);

    }

    /**
     * Debug pgt.
     * 
     * @param assertion the assertion
     * 
     * @return the string
     */
    public static String debugPGT(Assertion assertion) {
        StringBuffer stringBuffer = new StringBuffer();
        if (assertion == null) {
            stringBuffer.append("\n Assertion is null \n");
            return stringBuffer.toString();
        }

        AttributePrincipal attributePrincipal = assertion.getPrincipal();
        if (attributePrincipal == null) {
            stringBuffer.append("\n AttributePrincipal is null \n");
            return stringBuffer.toString();
        }

        Date validFromDate = assertion.getValidFromDate();
        Date validUntilDate = assertion.getValidUntilDate();

        String principal = attributePrincipal.getName();
        stringBuffer.append("\n Principal: \n");

        if (principal == null) {
            stringBuffer.append("null");
        } else {
            stringBuffer.append(principal);
        }

        stringBuffer.append("\n Valid from: \n");

        if (validFromDate == null) {
            stringBuffer.append("null");
        } else {
            stringBuffer.append(validFromDate.toString());
        }

        stringBuffer.append("\n Valid until: \n");

        if (validUntilDate == null) {
            stringBuffer.append("null");
        } else {
            stringBuffer.append(validUntilDate.toString());
        }

        stringBuffer.append("\n Attributes: \n");
        Iterator<?> it = assertion.getAttributes().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            stringBuffer.append("\n ");
            stringBuffer.append(entry.getKey());
            stringBuffer.append("=");
            stringBuffer.append(entry.getValue());
            stringBuffer.append(" \n");
        }
        return stringBuffer.toString();

    }
}
