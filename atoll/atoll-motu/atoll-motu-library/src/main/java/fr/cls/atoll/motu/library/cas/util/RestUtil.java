package fr.cls.atoll.motu.library.cas.util;

import org.apache.log4j.Logger;

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
 * @version $Revision: 1.1 $ - $Date: 2010-03-01 16:01:33 $
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
        if (! CAS_SERVER_URL_PREFIX.endsWith("/")) {
            stringBuffer.append("/");            
        }
        stringBuffer.append(CAS_RESTLET_URL_SUFFIX);
        
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
//    public static String getTicketGrantingTicket(String casServerUrlPrefix, String username, String password) throws IOException {
//    
//    }

    public static String getTicketGrantingTicket(String casServerUrlPrefix, String username, String password) throws IOException {

        String url = RestUtil.getCasRestletUrl();

        HttpsURLConnection hsu = (HttpsURLConnection) RestUtil.openConn(url);
        StringBuffer stringBuffer = new StringBuffer();
        
        stringBuffer.append(URLEncoder.encode("username", "UTF-8"));
        stringBuffer.append("=");
        stringBuffer.append(URLEncoder.encode(username, "UTF-8"));

        stringBuffer.append(URLEncoder.encode("password", "UTF-8"));
        stringBuffer.append("=");
        stringBuffer.append(URLEncoder.encode(password, "UTF-8"));

        OutputStreamWriter out = new OutputStreamWriter(hsu.getOutputStream());
        BufferedWriter bwr = new BufferedWriter(out);
        bwr.write(stringBuffer.toString());
        bwr.flush();
        bwr.close();
        out.close();

        String tgt = hsu.getHeaderField("location");

        //
        // Iterator<?> it = hsu.getHeaderFields().entrySet().iterator();
        // while (it.hasNext()) {
        // Map.Entry entry = (Map.Entry) it.next();
        // System.out.print(entry.getKey());
        // System.out.print(" --> ");
        // System.out.println(entry.getValue());
        // }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getRedirectUrl(String) - hsu.getResponseCode()=" + hsu.getResponseCode());
        }

        if (tgt == null || hsu.getResponseCode() != 201) {
            return null;
        }
        tgt = tgt.substring(tgt.lastIndexOf("/") + 1);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTGT(String, String, String) - String tgt=" + tgt);
        }

        bwr.close();
        RestUtil.closeConn(hsu);

        return tgt;

    }

    public static boolean getServiceTicket(String casServerUrlPrefix, String username, String password) throws IOException {

        String url = casServerUrlPrefix + "/v1/tickets";

        HttpsURLConnection hsu = (HttpsURLConnection) RestUtil.openConn(url);
        String s = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
        s += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

        OutputStreamWriter out = new OutputStreamWriter(hsu.getOutputStream());
        BufferedWriter bwr = new BufferedWriter(out);
        bwr.write(s);
        bwr.flush();
        bwr.close();
        out.close();

        String tgt = hsu.getHeaderField("location");
        //
        // Iterator<?> it = hsu.getHeaderFields().entrySet().iterator();
        // while (it.hasNext()) {
        // Map.Entry entry = (Map.Entry) it.next();
        // System.out.print(entry.getKey());
        // System.out.print(" --> ");
        // System.out.println(entry.getValue());
        // }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getRedirectUrl(String) - hsu.getResponseCode()=" + hsu.getResponseCode());
        }

        if (tgt != null && hsu.getResponseCode() == 201) {
            System.out.println(tgt);

            System.out.println("Tgt is : " + tgt.substring(tgt.lastIndexOf("/") + 1));
            tgt = tgt.substring(tgt.lastIndexOf("/") + 1);
            bwr.close();
            RestUtil.closeConn(hsu);

            String serviceURL = "http://atoll-dev.cls.fr:43080/thredds/catalog.xml";

            String encodedServiceURL = URLEncoder.encode("service", "utf-8") + "=" + URLEncoder.encode(serviceURL, "utf-8");
            System.out.println("Service url is : " + encodedServiceURL);

            String myURL = url + "/" + tgt;
            System.out.println(myURL);
            hsu = (HttpsURLConnection) RestUtil.openConn(myURL);
            out = new OutputStreamWriter(hsu.getOutputStream());
            bwr = new BufferedWriter(out);
            bwr.write(encodedServiceURL);
            bwr.flush();
            bwr.close();
            out.close();

            System.out.println("Response code is:  " + hsu.getResponseCode());

            BufferedReader isr = new BufferedReader(new InputStreamReader(hsu.getInputStream()));
            String line;
            System.out.println(hsu.getResponseCode());
            String ticket = "";
            while ((line = isr.readLine()) != null) {
                System.out.println(line);
                ticket = line;

            }
            isr.close();
            hsu.disconnect();

            return true;

        } else {
            return false;
        }

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
