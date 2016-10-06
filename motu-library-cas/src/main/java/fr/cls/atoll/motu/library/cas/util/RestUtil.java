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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import fr.cls.atoll.motu.api.message.AuthenticationMode;
import fr.cls.atoll.motu.library.cas.UserBase;
import fr.cls.atoll.motu.library.cas.exception.MotuCasBadRequestException;
import fr.cls.atoll.motu.library.cas.exception.MotuCasException;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class RestUtil {

    /** Logger for this class. */
    private static final Logger LOG = LogManager.getLogger();

    /** The Constant CAS_REST_URL_SUFFIX. */
    public static final String CAS_REST_URL_SUFFIX = "/v1/tickets";

    /** The Constant LOCATION_HTTP_HEADER_KEY. */
    public static final String LOCATION_HTTP_HEADER_KEY = "Location";

    /** The Constant WWW_AUTHENTICATE_HTTP_HEADER_KEY. */
    public static final String WWW_AUTHENTICATE_HTTP_HEADER_KEY = "WWW-Authenticate";

    /** The Constant DEFAULT_BUFFER_SIZE. */
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    /**
     * The Enum HttpMethod.
     */
    public enum HttpMethod {
        DELETE, GET, HEAD, POST, PUT,
    }

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
     * Check authentication mode.
     * 
     * @param uri the uri
     * @return the authentication mode
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasException the motu cas exception
     */
    public static AuthenticationMode checkAuthenticationMode(URI uri, UserBase user) throws IOException, MotuCasException {
        return RestUtil.checkAuthenticationMode(uri.toString(), null, user);
    }

    /**
     * Check authentication mode.
     * 
     * @param uri the uri
     * @param proxy the proxy
     * @return the authentication mode
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasException the motu cas exception
     */
    public static AuthenticationMode checkAuthenticationMode(URI uri, Proxy proxy, UserBase user) throws IOException, MotuCasException {
        return RestUtil.checkAuthenticationMode(uri.toString(), proxy, user);
    }

    /**
     * Check authentication mode.
     * 
     * @param url the url
     * @return the authentication mode
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasException the motu cas exception
     */
    public static AuthenticationMode checkAuthenticationMode(String url, UserBase user) throws IOException, MotuCasException {
        return RestUtil.checkAuthenticationMode(url, null, user);
    }

    /**
     * Check authentication mode.
     * 
     * @param url the url
     * @param proxy the proxy
     * @return the authentication mode
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasException the motu cas exception
     */
    public static AuthenticationMode checkAuthenticationMode(String url, Proxy proxy, UserBase user) throws IOException, MotuCasException {

        AuthenticationMode authenticationMode = AuthenticationMode.NONE;
        String casUrl = null;

        try {
            casUrl = RestUtil.getRedirectUrl(url, proxy);
            boolean isCasified = (!AssertionUtils.isNullOrEmpty(casUrl));

            if (isCasified) {
                authenticationMode = AuthenticationMode.CAS;
            }
        } catch (MotuCasBadRequestException e) {

            switch (e.getCode()) {

            case HttpURLConnection.HTTP_UNAUTHORIZED:
            case HttpURLConnection.HTTP_FORBIDDEN:
                authenticationMode = checkAuthenticationMode(e);
                break;

            default:
                throw e;
            }
        }

        if (user != null) {
            user.setAuthenticationMode(authenticationMode);
            user.setCasURL(casUrl);
        }

        return authenticationMode;

    }

    /**
     * Check authentication mode.
     * 
     * @param e the exception that contains request headers to check authentication
     * @return the authentication mode
     * @throws MotuCasException the motu cas exception
     */
    public static AuthenticationMode checkAuthenticationMode(MotuCasBadRequestException e) throws MotuCasException {

        String wwwAuthenticate = e.getHeaderField(RestUtil.WWW_AUTHENTICATE_HTTP_HEADER_KEY);

        if (RestUtil.isNullOrEmpty(wwwAuthenticate)) {
            return AuthenticationMode.NONE;
        }

        String wwwAuthenticateUpperCase = wwwAuthenticate.toUpperCase();

        if (wwwAuthenticateUpperCase.startsWith("BASIC")) {
            return AuthenticationMode.BASIC;
        }

        throw new MotuCasException(String.format("Authentication '%s' is not implemented", wwwAuthenticate));

    }

    /**
     * Check authentication mode.
     * 
     * @param targetService the target service
     * @param client the client
     * @param method the method
     * @return the authentication mode
     * @throws MotuCasException the motu cas exception
     */
    public static AuthenticationMode checkAuthenticationMode(URI targetService, Client client, RestUtil.HttpMethod method, UserBase user)
            throws MotuCasException {
        return checkAuthenticationMode(targetService.toString(), client, method, user);
    }

    /**
     * Check authentication mode.
     * 
     * @param targetService the target service
     * @param client the client
     * @param method the method
     * @return the authentication mode
     * @throws MotuCasException
     */
    public static AuthenticationMode checkAuthenticationMode(String targetService, Client client, RestUtil.HttpMethod method, UserBase user)
            throws MotuCasException {

        AuthenticationMode authenticationMode = AuthenticationMode.NONE;
        String casUrl = null;

        try {
            casUrl = RestUtil.getRedirectUrl(targetService, client, method);
            boolean isCasified = (!AssertionUtils.isNullOrEmpty(casUrl));

            if (isCasified) {
                authenticationMode = AuthenticationMode.CAS;
            }
        } catch (MotuCasBadRequestException e) {

            switch (e.getCode()) {

            case HttpURLConnection.HTTP_UNAUTHORIZED:
            case HttpURLConnection.HTTP_FORBIDDEN:
                authenticationMode = checkAuthenticationMode(e);
                break;

            default:
                throw e;
            }
        }

        if (user != null) {
            user.setAuthenticationMode(authenticationMode);
            user.setCasURL(casUrl);
        }

        return authenticationMode;

    }

    /**
     * Checks if is casified url.
     * 
     * @param serviceURL the service url
     * @return true, if is casified url
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException
     */
    public static boolean isCasifiedUrl(URI serviceURL) throws IOException, MotuCasBadRequestException {

        return isCasifiedUrl(serviceURL.toString(), null, false);

    }

    /**
     * Checks if is casified url.
     *
     * @param serviceURL the service url
     * @param ignoreHttp4XX the ignore http4 xx
     * @return true, if checks if is casified url
     * @throws IOException the IO exception
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static boolean isCasifiedUrl(URI serviceURL, boolean ignoreHttp4XX) throws IOException, MotuCasBadRequestException {

        return isCasifiedUrl(serviceURL.toString(), null, ignoreHttp4XX);

    }

    /**
     * Checks if is casified url.
     * 
     * @param serviceURL the service url
     * @param proxy the proxy
     * @return true, if is casified url
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static boolean isCasifiedUrl(URI serviceURL, Proxy proxy) throws IOException, MotuCasBadRequestException {
        return isCasifiedUrl(serviceURL.toString(), proxy, false);
    }

    /**
     * Checks if is casified url.
     *
     * @param serviceURL the service url
     * @param proxy the proxy
     * @param ignoreHttp4XX the ignore http4 xx
     * @return true, if checks if is casified url
     * @throws IOException the IO exception
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static boolean isCasifiedUrl(URI serviceURL, Proxy proxy, boolean ignoreHttp4XX) throws IOException, MotuCasBadRequestException {
        return isCasifiedUrl(serviceURL.toString(), proxy, ignoreHttp4XX);
    }

    /**
     * Checks if is casified url.
     * 
     * @param serviceURL the service url
     * @return true, if is casified url
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException
     */
    public static boolean isCasifiedUrl(String serviceURL) throws IOException, MotuCasBadRequestException {
        return isCasifiedUrl(serviceURL, null, false);
    }

    /**
     * Checks if is casified url.
     * 
     * @param serviceURL the service url
     * @param ignoreHttp4XX the ignore http4 xx
     * @return true, if checks if is casified url
     * @throws IOException the IO exception
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static boolean isCasifiedUrl(String serviceURL, boolean ignoreHttp4XX) throws IOException, MotuCasBadRequestException {
        return isCasifiedUrl(serviceURL, null, ignoreHttp4XX);
    }

    /**
     * Checks if is casified url.
     * 
     * @param serviceURL the service url
     * @param proxy the proxy
     * @param ignoreHttp4XX the ignore http4 xx
     * @return true, if is casified url
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static boolean isCasifiedUrl(String serviceURL, Proxy proxy, boolean ignoreHttp4XX) throws IOException, MotuCasBadRequestException {

        String casUrl = null;
        boolean isCasified = false;

        try {
            casUrl = RestUtil.getRedirectUrl(serviceURL, proxy);
            isCasified = (!AssertionUtils.isNullOrEmpty(casUrl));

        } catch (MotuCasBadRequestException e) {
            // WARNING : An 4xx http error is not necessary an error,
            // i.e. opening an TDS dataset url failed always with URLConnection,
            // this is not an error here, it just used to check if openConnection returns http 302 or not.
            // So, don't raise exception if http code is 4xx

            if (ignoreHttp4XX) {
                if (e.getCode() >= 500) {
                    throw e;
                }
            } else {
                throw e;
            }
        }

        return isCasified;

    }

    /**
     * Checks if is casified url.
     * 
     * @param serviceURL the service url
     * @param client the client
     * @param method the method
     * @return true, if is casified url
     * @throws MotuCasBadRequestException
     */
    public static boolean isCasifiedUrl(String serviceURL, Client client, RestUtil.HttpMethod method) throws MotuCasBadRequestException {
        String casUrl = RestUtil.getRedirectUrl(serviceURL, client, method);
        return (!AssertionUtils.isNullOrEmpty(casUrl));
    }

    /**
     * Append path.
     * 
     * @param prefix the prefix
     * @param suffix the suffix
     * @return the string
     */
    public static String appendPath(String prefix, String suffix) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(prefix);
        if ((!prefix.endsWith("/")) && (!suffix.startsWith("/"))) {
            stringBuffer.append("/");
        }

        if ((prefix.endsWith("/")) && (suffix.startsWith("/"))) {
            stringBuffer.append(suffix.substring(1));
        } else {
            stringBuffer.append(suffix);
        }

        return stringBuffer.toString();

    }

    /**
     * Gets the cas restlet url.
     * 
     * @param client the client
     * @param method the method
     * @param serviceURL the service url
     * @param casRestUrlSuffix the cas rest url suffix
     * @return the cas restlet url
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException the motu cas bad request exception
     */
    public static String getCasRestletUrl(Client client, RestUtil.HttpMethod method, String serviceURL, String casRestUrlSuffix)
            throws IOException, MotuCasBadRequestException {

        if (client == null) {
            return null;

        }
        String casServerPrefix = RestUtil.getRedirectUrl(serviceURL, client, method);
        if (AssertionUtils.isNullOrEmpty(casServerPrefix)) {

            return null;
        }

        // StringBuffer stringBuffer = new StringBuffer();
        // stringBuffer.append(casServerPrefix);
        // if ((!casServerPrefix.endsWith("/")) && (!casRestUrlSuffix.startsWith("/"))) {
        // stringBuffer.append("/");
        // }
        //
        // if ((casServerPrefix.endsWith("/")) && (casRestUrlSuffix.startsWith("/"))) {
        // stringBuffer.append(casRestUrlSuffix.substring(1));
        // } else {
        // stringBuffer.append(casRestUrlSuffix);
        // }
        //
        // return stringBuffer.toString();

        return RestUtil.appendPath(casServerPrefix, casRestUrlSuffix);

    }

    /**
     * Gets the cas restlet url.
     * 
     * @param serviceURL the service url
     * @param casRestUrlSuffix the cas rest url suffix
     * 
     * @return the cas restlet url
     * @throws IOException
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException
     */
    public static String getCasRestletUrl(String serviceURL, String casRestUrlSuffix) throws IOException, MotuCasBadRequestException {

        String casServerPrefix = RestUtil.getRedirectUrl(serviceURL);
        if (AssertionUtils.isNullOrEmpty(casServerPrefix)) {

            return null;
        }

        // StringBuffer stringBuffer = new StringBuffer();
        // stringBuffer.append(casServerPrefix);
        // if ((!casServerPrefix.endsWith("/")) && (!casRestUrlSuffix.startsWith("/"))) {
        // stringBuffer.append("/");
        // }
        //
        // if ((casServerPrefix.endsWith("/")) && (casRestUrlSuffix.startsWith("/"))) {
        // stringBuffer.append(casRestUrlSuffix.substring(1));
        // } else {
        // stringBuffer.append(casRestUrlSuffix);
        // }
        //
        // return stringBuffer.toString();

        return RestUtil.appendPath(casServerPrefix, casRestUrlSuffix);

    }

    /**
     * Gets the redirect url.
     * 
     * @param path the path
     * 
     * @return the redirect url
     * @throws IOException
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException
     */
    public static String getRedirectUrl(String path) throws IOException, MotuCasBadRequestException {
        return RestUtil.getRedirectUrl(path, null);

    }

    /**
     * Gets the redirect url.
     * 
     * @param path the path
     * @param proxy the proxy
     * @return the redirect url
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MotuCasBadRequestException
     */
    public static String getRedirectUrl(String path, Proxy proxy) throws IOException, MotuCasBadRequestException {

        String redirectUrl = "";

        if (AssertionUtils.isNullOrEmpty(path)) {
            return null;
        }

        URL url = new URL(path);
        String protocol = url.getProtocol();
        HttpURLConnection conn = null;

        if ((protocol.equalsIgnoreCase("http")) || (protocol.equalsIgnoreCase("https"))) {

            if (proxy == null) {
                conn = (HttpURLConnection) url.openConnection();
            } else {
                conn = (HttpURLConnection) url.openConnection(proxy);
            }
        } else {
            return redirectUrl;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getRedirectUrl(String) - HttpURLConnection response code=" + conn.getResponseCode());
        }
        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            conn.disconnect();
            throw new MotuCasBadRequestException(
                    conn,
                    responseCode,
                    path,
                    "Error while trying to retrieve CAS url (RestUtil.getRedirectUrl(String path, Proxy proxy)");
        }

        redirectUrl = RestUtil.extractRedirectUrl(conn.getHeaderField(RestUtil.LOCATION_HTTP_HEADER_KEY), responseCode);

        // Iterator<?> it = conn.getHeaderFields().entrySet().iterator();
        // while (it.hasNext()) {
        // Map.Entry entry = (Map.Entry) it.next();
        // System.out.print(entry.getKey());
        // System.out.print(" --> ");
        // System.out.println(entry.getValue());
        // }

        conn.disconnect();
        return redirectUrl;
    }

    /**
     * Gets the redirect url.
     * 
     * @param path the path
     * @param client the client
     * @param method the method
     * @return the redirect url
     * @throws MotuCasBadRequestException
     */
    public static String getRedirectUrl(String path, Client client, RestUtil.HttpMethod method) throws MotuCasBadRequestException {

        WebResource webResource = client.resource(path);

        ClientResponse response = webResource.accept(MediaType.WILDCARD).method(method.toString(), ClientResponse.class);

        Integer responseCode = response.getStatus();

        if (responseCode >= 400) {
            throw MotuCasBadRequestException
                    .createMotuCasBadRequestException(response,
                                                      path,
                                                      "Error while trying to retrieve CAS url (RestUtil.getRedirectUrl(String path, Proxy proxy)");
        }

        String location = "";
        if (response.getLocation() != null) {
            location = response.getLocation().toString();
        }
        return RestUtil.extractRedirectUrl(location, responseCode);

    }

    /**
     * Extract redirect url.
     * 
     * @param url the url
     * @param httpCode the http code
     * @return the string
     */
    public static String extractRedirectUrl(String url, int httpCode) {
        String redirectUrl = url;
        if (!RestUtil.isNullOrEmpty(redirectUrl) && (httpCode == 302)) {
            redirectUrl = redirectUrl.substring(0, redirectUrl.lastIndexOf("/login") + 1);
            if (LOG.isDebugEnabled()) {
                LOG.debug("extractRedirectUrl(String url, int httpCode) - redirectUrl=" + redirectUrl);
            }
        }
        return redirectUrl;
    }

    /**
     * Gets the ticket granting ticket.
     * 
     * @param casRestUrl the cas rest url
     * @param user the user
     * @return the ticket granting ticket
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String getTicketGrantingTicket(String casRestUrl, UserBase user) throws IOException {
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

        HttpsURLConnection hsu = (HttpsURLConnection) RestUtil.openHttpsConnection(casRestUrl);
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

        String ticketGrantingTicket = hsu.getHeaderField(RestUtil.LOCATION_HTTP_HEADER_KEY);

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
     */
    public static String loginToCAS(String casRestUrl, UserBase user, String serviceURL) throws IOException {
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
     */
    public static String loginToCAS(String casRestUrl, String username, String password, String serviceURL) throws IOException {
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
     */
    public static String loginToCASWithTGT(String casRestUrl, String ticketGrantingTicket, String serviceURL) throws IOException {
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
        // System.out.println(casURL);
        HttpsURLConnection hsu = (HttpsURLConnection) RestUtil.openHttpsConnection(casURL);
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
    static URLConnection openHttpsConnection(String urlk) throws IOException {

        URL url = new URL(urlk);
        HttpsURLConnection hsu = (HttpsURLConnection) url.openConnection();
        hsu.setDoInput(true);
        hsu.setDoOutput(true);
        hsu.setRequestMethod(HttpMethod.POST.toString());
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
     * Checks if is null or empty.
     * 
     * @param value the value
     * 
     * @return true, if is null or empty
     */

    static public boolean isNullOrEmpty(Collection<?> value) {
        if (value == null) {
            return true;
        }
        if (value.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Url to input stream.
     * 
     * @param url the url
     * @return the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static final InputStream urlToInputStream(String url) throws IOException {
        url = url.trim();
        if (url.indexOf(':') > 1) {
            URL urlObject = new URL(url);
            return urlObject.openStream();
        }
        return new FileInputStream(url);
    }

    /**
     * Copy stream to file.
     * 
     * @param input the input
     * @param destination the destination
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void copyStreamToFile(InputStream input, File destination) throws IOException {
        FileOutputStream output = null;
        try {
            if (destination.getParentFile() != null && !destination.getParentFile().exists()) {
                if (!destination.getParentFile().mkdirs()) {
                    throw new IOException("Cannot create the parent file of " + destination);
                }
            }
            if (destination.exists() && !destination.canWrite()) {
                throw new IOException("Unable to open file " + destination + " for writing.");
            } else {
                output = new FileOutputStream(destination);
                RestUtil.copy(input, output);
            }
        } finally {
            RestUtil.close(input);
            RestUtil.close(output);
        }
    }

    /**
     * Copy.
     * 
     * @param in the in
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        RestUtil.copy(in, out, RestUtil.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copy.
     * 
     * @param in the in
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void copy(Reader in, Writer out) throws IOException {
        RestUtil.copy(in, out, RestUtil.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copy.
     * 
     * @param in the in
     * @param out the out
     * @param bufferSize the buffer size
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte buf[] = new byte[bufferSize];
        for (int len = in.read(buf); len >= 0; len = in.read(buf)) {
            out.write(buf, 0, len);
        }
    }

    /**
     * Copy.
     * 
     * @param in the in
     * @param out the out
     * @param bufferSize the buffer size
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void copy(Reader in, Writer out, int bufferSize) throws IOException {
        char buf[] = new char[bufferSize];
        for (int len = in.read(buf); len >= 0; len = in.read(buf)) {
            out.write(buf, 0, len);
        }
    }

    /**
     * Close.
     * 
     * @param in the in
     */
    public static final void close(InputStream in) {
        if (in == null) {
            return;
        }
        try {
            in.close();
        } catch (Exception ex) {
        }
    }

    /**
     * Close.
     * 
     * @param out the out
     */
    public static final void close(OutputStream out) {
        if (out == null) {
            return;
        }
        try {
            out.close();
        } catch (Exception ex) {
        }
    }

    /**
     * Close.
     * 
     * @param in the in
     */
    public static final void close(Reader in) {
        if (in == null) {
            return;
        }
        try {
            in.close();
        } catch (Exception ex) {
        }
    }

    /**
     * Close.
     * 
     * @param out the out
     */
    public static final void close(Writer out) {
        if (out == null) {
            return;
        }
        try {
            out.close();
        } catch (Exception ex) {
        }
    }

}
