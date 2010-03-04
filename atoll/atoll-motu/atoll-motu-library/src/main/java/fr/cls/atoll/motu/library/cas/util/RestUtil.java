package fr.cls.atoll.motu.library.cas.util;

import org.apache.log4j.Logger;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.intfce.Organizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2010. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.2 $ - $Date: 2010-03-04 16:05:15 $
 */
public class RestUtil {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(RestUtil.class);

    private static String CAS_SERVER_URL_PREFIX = null;

    public static String getCAS_SERVER_URL_PREFIX() {
        return CAS_SERVER_URL_PREFIX;
    }

    public static void setCAS_SERVER_URL_PREFIX(String cas_server_url_prefix) {
        CAS_SERVER_URL_PREFIX = cas_server_url_prefix;
    }

    private static String CAS_RESTLET_URL_SUFFIX = "/v1/tickets";

    public static String getCAS_RESTLET_URL_SUFFIX() {
        return CAS_RESTLET_URL_SUFFIX;
    }

    public static void setCAS_RESTLET_URL_SUFFIX(String cas_restlet_url_suffix) {
        CAS_RESTLET_URL_SUFFIX = cas_restlet_url_suffix;
    }

    public static String getCasRestletUrl() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(CAS_SERVER_URL_PREFIX);
        if (!CAS_SERVER_URL_PREFIX.endsWith("/")) {
            stringBuffer.append("/");
        }
        stringBuffer.append(CAS_RESTLET_URL_SUFFIX);

        return stringBuffer.toString();
    }
    
    public static String getCasRestletUrl(String serviceURL, String casRestUrlSuffix) throws IOException {
        
        String casServerPrefix = RestUtil.getRedirectUrl(serviceURL);

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(casServerPrefix);
        if ((!casServerPrefix.endsWith("/")) && (!casRestUrlSuffix.startsWith("/"))){
            stringBuffer.append("/");
        }
        
        if ((casServerPrefix.endsWith("/")) && (casRestUrlSuffix.startsWith("/"))){
            stringBuffer.append(casRestUrlSuffix.substring(1));
        } else {
            stringBuffer.append(casRestUrlSuffix);            
        }

        return stringBuffer.toString();
        
    }

    

    public static String getRedirectUrl(String path) throws IOException {

        String redirectUrl = "";
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

    public static String getTicketGrantingTicket(String casRestUrl, String username, String password) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTicketGrantingTicket(String, String, String) - entering");
        }
        
        
        if (AssertionUtils.isNullOrEmpty(casRestUrl))
        {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTicketGrantingTicket(String, String, String) - exiting");
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
        stringBuffer.append(URLEncoder.encode(password, "UTF-8"));
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTicketGrantingTicket(String, String, String) : "+ stringBuffer.toString());
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
    public static String loginToCAS(String casRestUrl, String username, String password, String serviceURL) throws IOException, MotuException {
        String ticketGrantingTicket = RestUtil.getTicketGrantingTicket(casRestUrl, username, password);
        return loginToCASWithTGT(casRestUrl, ticketGrantingTicket, serviceURL);
    }
    public static String loginToCASWithTGT(String casRestUrl, String ticketGrantingTicket, String serviceURL) throws IOException, MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loginToCAS(String, String, String) - entering");
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

    static URLConnection openConn(String urlk) throws IOException {

        URL url = new URL(urlk);
        HttpsURLConnection hsu = (HttpsURLConnection) url.openConnection();
        hsu.setDoInput(true);
        hsu.setDoOutput(true);
        hsu.setRequestMethod("POST");
        return hsu;

    }

    static void closeConn(HttpsURLConnection c) {
        c.disconnect();
    }

}
