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
package fr.cls.atoll.motu.library.misc.cas.util;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class RestUtil {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(RestUtil.class);

    //
    // private static String CAS_SERVER_URL_PREFIX = null;
    //
    // public static String getCAS_SERVER_URL_PREFIX() {
    // return CAS_SERVER_URL_PREFIX;
    // }
    //
    // public static void setCAS_SERVER_URL_PREFIX(String cas_server_url_prefix) {
    // CAS_SERVER_URL_PREFIX = cas_server_url_prefix;
    // }
    //
    // private static String CAS_RESTLET_URL_SUFFIX = "/v1/tickets";
    //
    // public static String getCAS_RESTLET_URL_SUFFIX() {
    // return CAS_RESTLET_URL_SUFFIX;
    // }
    //
    // public static void setCAS_RESTLET_URL_SUFFIX(String cas_restlet_url_suffix) {
    // CAS_RESTLET_URL_SUFFIX = cas_restlet_url_suffix;
    // }
    //
    // public static String getCasRestletUrl() {
    // StringBuffer stringBuffer = new StringBuffer();
    // stringBuffer.append(CAS_SERVER_URL_PREFIX);
    // if (!CAS_SERVER_URL_PREFIX.endsWith("/")) {
    // stringBuffer.append("/");
    // }
    // stringBuffer.append(CAS_RESTLET_URL_SUFFIX);
    //
    // return stringBuffer.toString();
    // }
    /**
     * Checks if is casified url.
     * 
     * @param serviceURL the service url
     * 
     * @return true, if is casified url
     * @throws IOException
     */
    public static boolean isCasifiedUrl(String serviceURL) throws IOException {

        String casUrl = RestUtil.getRedirectUrl(serviceURL);
        return (!AssertionUtils.isNullOrEmpty(casUrl));

    }

    /**
     * Gets the cas restlet url.
     * 
     * @param serviceURL the service url
     * @param casRestUrlSuffix the cas rest url suffix
     * 
     * @return the cas restlet url
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String getCasRestletUrl(String serviceURL, String casRestUrlSuffix) throws IOException {

        String casServerPrefix = RestUtil.getRedirectUrl(serviceURL);
        if (AssertionUtils.isNullOrEmpty(casServerPrefix)) {

            return null;
        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(casServerPrefix);
        if ((!casServerPrefix.endsWith("/")) && (!casRestUrlSuffix.startsWith("/"))) {
            stringBuffer.append("/");
        }

        if ((casServerPrefix.endsWith("/")) && (casRestUrlSuffix.startsWith("/"))) {
            stringBuffer.append(casRestUrlSuffix.substring(1));
        } else {
            stringBuffer.append(casRestUrlSuffix);
        }

        return stringBuffer.toString();

    }

    /**
     * Gets the redirect url.
     * 
     * @param path the path
     * 
     * @return the redirect url
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String getRedirectUrl(String path) throws IOException {

        String redirectUrl = "";

        if (AssertionUtils.isNullOrEmpty(path)) {
            return null;
        }

        URL url = new URL(path);
        String protocol = url.getProtocol();
        HttpURLConnection conn = null;

        if (protocol.equalsIgnoreCase("http")) {
            conn = (HttpURLConnection) url.openConnection();
        } else if (protocol.equalsIgnoreCase("https")) {
            conn = (HttpsURLConnection) url.openConnection();
        } else {
            return redirectUrl;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getRedirectUrl(String) - HttpURLConnection response code=" + conn.getResponseCode());
        }

        redirectUrl = conn.getHeaderField("location");

        // Iterator<?> it = conn.getHeaderFields().entrySet().iterator();
        // while (it.hasNext()) {
        // Map.Entry entry = (Map.Entry) it.next();
        // System.out.print(entry.getKey());
        // System.out.print(" --> ");
        // System.out.println(entry.getValue());
        // }

        if ((redirectUrl != null) && (conn.getResponseCode() == 302)) {
            redirectUrl = redirectUrl.substring(0, redirectUrl.lastIndexOf("/") + 1);
            if (LOG.isDebugEnabled()) {
                LOG.debug("getRedirectUrl(String) - redirectUrl=" + redirectUrl);
            }
        }
        conn.disconnect();
        return redirectUrl;
    }

    // public static String getTicketGrantingTicket(String casServerUrlPrefix, String username, String
    // password) throws IOException {
    //    
    // }
    public static String getTicketGrantingTicket(String casRestUrl, User user) throws IOException {
        return getTicketGrantingTicket(casRestUrl, user.getLogin(), user.getPwd());
    }

    /**
     * Gets the ticket granting ticket.
     * 
     * @param casRestUrl the cas rest url
     * @param username the username
     * @param password the password
     * 
     * @return the ticket granting ticket
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String getTicketGrantingTicket(String casRestUrl, String username, String password) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTicketGrantingTicket(String, String, String) - entering");
        }

        if (AssertionUtils.isNullOrEmpty(casRestUrl)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTicketGrantingTicket(String, String, String) - casRestUrl is null - exiting");
            }
            return null;
        }

        if (AssertionUtils.isNullOrEmpty(username)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTicketGrantingTicket(String, String, String) - username is null - exiting");
            }
            return null;
        }

        HttpsURLConnection hsu = (HttpsURLConnection) RestUtil.openConn(casRestUrl);
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(URLEncoder.encode("username", "UTF-8"));
        stringBuffer.append("=");
        stringBuffer.append(URLEncoder.encode(username, "UTF-8"));

        stringBuffer.append("&");

        stringBuffer.append(URLEncoder.encode("password", "UTF-8"));
        stringBuffer.append("=");
        String passwordToEncode = ((password == null) ? "" : password);
        stringBuffer.append(URLEncoder.encode(passwordToEncode, "UTF-8"));
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTicketGrantingTicket(String, String, String) : " + stringBuffer.toString());
        }

        OutputStreamWriter out = new OutputStreamWriter(hsu.getOutputStream());
        BufferedWriter bwr = new BufferedWriter(out);
        bwr.write(stringBuffer.toString());
        bwr.flush();
        bwr.close();
        out.close();

        if (LOG.isDebugEnabled()) {
            LOG.debug("getTicketGrantingTicket(String, String, String) - hsu.getResponseCode()=" + hsu.getResponseCode());
        }

        String ticketGrantingTicket = hsu.getHeaderField("location");

        //
        // Iterator<?> it = hsu.getHeaderFields().entrySet().iterator();
        // while (it.hasNext()) {
        // Map.Entry entry = (Map.Entry) it.next();
        // System.out.print(entry.getKey());
        // System.out.print(" --> ");
        // System.out.println(entry.getValue());
        // }

        if (ticketGrantingTicket == null || hsu.getResponseCode() != 201) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTicketGrantingTicket(String, String, String) - exiting");
            }
            return null;
        }
        ticketGrantingTicket = ticketGrantingTicket.substring(ticketGrantingTicket.lastIndexOf("/") + 1);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTGT(String, String, String) - String tgt=" + ticketGrantingTicket);
        }

        bwr.close();
        RestUtil.closeConn(hsu);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getTicketGrantingTicket(String, String, String) - exiting");
        }
        return ticketGrantingTicket;

    }

    /**
     * Login to cas.
     * 
     * @param casRestUrl the cas rest url
     * @param user the user
     * @param serviceURL the service url
     * 
     * @return the string
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuException the motu exception
     */
    public static String loginToCAS(String casRestUrl, User user, String serviceURL) throws IOException, MotuException {
        return loginToCAS(casRestUrl, user.getLogin(), user.getPwd(), serviceURL);
    }

    /**
     * Login to cas.
     * 
     * @param casRestUrl the cas rest url
     * @param username the username
     * @param password the password
     * @param serviceURL the service url
     * 
     * @return the string
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuException the motu exception
     */
    public static String loginToCAS(String casRestUrl, String username, String password, String serviceURL) throws IOException, MotuException {
        String ticketGrantingTicket = RestUtil.getTicketGrantingTicket(casRestUrl, username, password);
        return loginToCASWithTGT(casRestUrl, ticketGrantingTicket, serviceURL);
    }

    /**
     * Login to cas with tgt.
     * 
     * @param casRestUrl the cas rest url
     * @param ticketGrantingTicket the ticket granting ticket
     * @param serviceURL the service url
     * 
     * @return the string
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuException the motu exception
     */
    public static String loginToCASWithTGT(String casRestUrl, String ticketGrantingTicket, String serviceURL) throws IOException, MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loginToCAS(String, String, String) - entering");
        }

        if (AssertionUtils.isNullOrEmpty(casRestUrl)) {
            return null;

        }

        if (AssertionUtils.isNullOrEmpty(ticketGrantingTicket)) {
            return null;

        }

        if (AssertionUtils.isNullOrEmpty(serviceURL)) {
            return null;

        }

        String encodedServiceURL = URLEncoder.encode("service", "utf-8") + "=" + URLEncoder.encode(serviceURL, "utf-8");
        if (LOG.isDebugEnabled()) {
            LOG.debug("loginToCAS(String, String, String) - Service url is : " + encodedServiceURL);
        }

        String casURL = casRestUrl + "/" + ticketGrantingTicket;
        if (LOG.isDebugEnabled()) {
            LOG.debug("loginToCAS(String, String, String) - url is: " + casURL);
        }
        System.out.println(casURL);
        HttpsURLConnection hsu = (HttpsURLConnection) RestUtil.openConn(casURL);
        OutputStreamWriter out = new OutputStreamWriter(hsu.getOutputStream());
        BufferedWriter bwr = new BufferedWriter(out);
        bwr.write(encodedServiceURL);
        bwr.flush();
        bwr.close();
        out.close();

        if (LOG.isDebugEnabled()) {
            LOG.debug("loginToCAS(String, String, String) - Response code is: " + hsu.getResponseCode());
        }

        BufferedReader isr = new BufferedReader(new InputStreamReader(hsu.getInputStream()));
        String ticket = "";
        while ((ticket = isr.readLine()) != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("loginToCAS(String, String, String) - Service Ticket is: " + ticket);
            }
            break;
        }

        isr.close();
        hsu.disconnect();

        if (LOG.isDebugEnabled()) {
            LOG.debug("loginToCAS(String, String, String) - exiting");
        }
        return ticket;

    }

    /**
     * Open conn.
     * 
     * @param urlk the urlk
     * 
     * @return the uRL connection
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static URLConnection openConn(String urlk) throws IOException {

        URL url = new URL(urlk);
        HttpsURLConnection hsu = (HttpsURLConnection) url.openConnection();
        hsu.setDoInput(true);
        hsu.setDoOutput(true);
        hsu.setRequestMethod("POST");
        return hsu;

    }

    /**
     * Close conn.
     * 
     * @param c the c
     */
    static void closeConn(HttpsURLConnection c) {
        c.disconnect();
    }

}
