package fr.cls.atoll.motu.library.misc.cas.util;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.intfce.User;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class AssertionUtils {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(AssertionUtils.class);

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
     * @throws URIException the URI exception
     */
    public static boolean hasCASTicket(String targetService) throws URIException {
        return (targetService.indexOf("?ticket=") != -1) || (targetService.indexOf("&ticket=") != -1);
    }

    /**
     * Adds the cas ticket.
     * 
     * @param targetService the target service
     * @param user the user
     * 
     * @return the string
     * 
     * @throws MotuException the motu exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String addCASTicket(String targetService, User user) throws MotuException, IOException {

        if (user == null) {
            return addCASTicket(targetService, null, null, null);
        }
        return addCASTicket(targetService, user.getLogin(), user.getPwd(), user.getCasRestSuffURL());

    }

    /**
     * Adds the cas ticket.
     * 
     * @param targetService the target service
     * 
     * @return the string
     * @throws IOException
     * @throws MotuException
     * 
     * @throws URIException the URI exception
     */
    public static String addCASTicket(String targetService) throws MotuException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String) - entering");
        }

        String returnString = AssertionUtils.addCASTicket(targetService, null, null, null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String) - exiting");
        }
        return returnString;
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
     * 
     * @throws MotuException the motu exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String addCASTicket(String targetService, String username, String password, String casRestUrlSuffix) throws MotuException,
            IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String, String, String, String) - entering");
        }

        String returnString = targetService;
        String casRestUrlSuffixToUse = casRestUrlSuffix;

        Assertion assertion = AssertionHolder.getAssertion();
        if (assertion != null) {
            returnString = AssertionUtils.addCASTicket(assertion, targetService);
        } else if (!AssertionUtils.isNullOrEmpty(username)) {
            if (AssertionUtils.isNullOrEmpty(casRestUrlSuffixToUse)) {
                casRestUrlSuffixToUse = Organizer.getMotuConfigInstance().getCasRestUrlSuffix();
            }
            returnString = AssertionUtils.addCASTicketFromTGT(casRestUrlSuffix, username, password, targetService);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String, String, String, String) - exiting");
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
     * 
     * @return the string
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuException the motu exception
     */
    public static String addCASTicketFromTGT(String casRestUrlSuffix, String username, String password, String targetService) throws IOException,
            MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicketFromTGT(String, String, String, String) - entering");
        }

        if (AssertionUtils.isNullOrEmpty(username)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String, String) - exiting");
            }
            return targetService;
        }
        if (AssertionUtils.isNullOrEmpty(targetService)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String, String) - exiting");
            }
            return targetService;
        }
        if (AssertionUtils.hasCASTicket(targetService)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String, String) - exiting - URL has already a cas ticket");
            }
            return targetService;
        }

        String casRestUrl = RestUtil.getCasRestletUrl(targetService, casRestUrlSuffix);

        String ticket = RestUtil.loginToCAS(casRestUrl, username, password, targetService);

        if (AssertionUtils.isNullOrEmpty(ticket)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicketFromTGT(String, String, String, String) - exiting");
            }
            return targetService;
        }

        String returnString = AssertionUtils.addCASTicket(ticket, targetService);
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicketFromTGT(String, String, String, String) - exiting");
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
    public static String addCASTicket(Assertion assertion, String targetService) throws URIException {

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
