package fr.cls.atoll.motu.library.cas.util;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;

import fr.cls.atoll.motu.library.cas.HttpClientCAS;

/**
 * <br><br>Copyright : Copyright (c) 2010.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.3 $ - $Date: 2010-03-04 16:05:15 $
 */
public class AssertionUtils {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(AssertionUtils.class);

    public static String getAttributePrincipalName()  {
        Assertion assertion = (Assertion) AssertionHolder.getAssertion();
        return getAttributePrincipalName(assertion);
    }    
    public static String getAttributePrincipalName(Assertion assertion)  {
        
        AttributePrincipal attributePrincipal =  AssertionUtils.getAttributePrincipal(assertion);
        
        String name = "";
        
        if (attributePrincipal != null) {
            name = attributePrincipal.getName();
        }
        return name;
    }

    public static boolean hasCASTicket(String targetService) throws URIException {
        return (targetService.indexOf("?ticket=") != -1) || (targetService.indexOf("&ticket=") != -1);
    }

    public static String addCASTicket(String targetService) throws URIException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String) - entering");
        }

        Assertion assertion = (Assertion) AssertionHolder.getAssertion();
        String returnString = addCASTicket(assertion, targetService);
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(String) - exiting");
        }
        return returnString;

    }

    public static String addCASTicket(Assertion assertion, String targetService) throws URIException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(Assertion, String) - entering : debugPGTFromSession " + AssertionUtils.debugPGT(assertion));
        }

        if (assertion == null ) {
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

        if (hasCASTicket(targetService)) {
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

//        StringBuffer stringBuffer = new StringBuffer();
//        stringBuffer.append(targetService);
//        stringBuffer.append(targetService.indexOf("?") != -1 ? "&" : "?");
//
//        stringBuffer.append("ticket=");
//        stringBuffer.append(ticket);
//        return stringBuffer.toString();
//
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("addCASTicket(String) - exiting - new URL = " + stringBuffer.toString());
//        }
        return addCASTicket(ticket, targetService);

    }
    
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

    
    public static String getProxyTicketFor(String targetService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyTicketFor(String) - entering");
        }

        Assertion assertion = (Assertion) AssertionHolder.getAssertion();
        String returnString = getProxyTicketFor(assertion, targetService);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyTicketFor(String) - exiting");
        }
        return returnString;

    }

    public static String getProxyTicketFor(Assertion assertion, String targetService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyTicketFor(Assertion, String) - entering");
        }
        
        String ticket = "";

        if (assertion == null ) {
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

    public static AttributePrincipal getAttributePrincipal() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAttributePrincipal() - entering");
        }

        AttributePrincipal returnAttributePrincipal = AssertionUtils.getAttributePrincipal((Assertion) AssertionHolder.getAssertion());
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

    public static String debugPGT() {
        Assertion assertion = (Assertion) AssertionHolder.getAssertion();
        return debugPGT(assertion);

    }

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
