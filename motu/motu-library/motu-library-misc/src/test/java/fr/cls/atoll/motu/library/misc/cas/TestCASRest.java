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
package fr.cls.atoll.motu.library.misc.cas;

import fr.cls.atoll.motu.library.misc.cas.util.AssertionUtils;
import fr.cls.atoll.motu.library.misc.cas.util.RestUtil;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;

import ucar.nc2.util.net.EasySSLProtocolSocketFactory;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class TestCASRest {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(TestCASRest.class);

    public static final String casServerUrlPrefix = "https://atoll-dev.cls.fr:8443/cas-server-webapp-3.3.5";

    public static void main(String... args) throws Exception {
        String username = "dearith";
        String password = "bienvenue";
        // validateFromCAS(username, password);
        // loginToCAS(username, password);
        // getRedirectUrl();

        // validateFromCAS2(username, password);

        // testgetCASifiedResource();
        testDownloadCASifiedResource();

    }

    public static String getRedirectUrl() {
        URL url;
        String redirectUrl = "";
        try {
            url = new URL("http://atoll-dev.cls.fr:43080/thredds/catalog.xml");
            // url = new URL("http://mercator-data1.cls.fr:43080/thredds/catalog.xml");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            System.out.println(conn.getResponseCode());

            redirectUrl = conn.getHeaderField("location");

            Iterator<?> it = conn.getHeaderFields().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                System.out.print(entry.getKey());
                System.out.print(" --> ");
                System.out.println(entry.getValue());
            }

            if ((redirectUrl != null) && (conn.getResponseCode() == 302)) {
                System.out.println(redirectUrl);
                redirectUrl = redirectUrl.substring(0, redirectUrl.lastIndexOf("/") + 1);
                System.out.println("redirectUrl is : " + redirectUrl);

            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return redirectUrl;
    }

    public static boolean validateFromCAS(String username, String password) throws Exception {

        String url = casServerUrlPrefix + "/v1/tickets";
        try {
            HttpsURLConnection hsu = (HttpsURLConnection) openConn(url);
            String s = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
            s += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

            System.out.println(s);
            OutputStreamWriter out = new OutputStreamWriter(hsu.getOutputStream());
            BufferedWriter bwr = new BufferedWriter(out);
            bwr.write(s);
            bwr.flush();
            bwr.close();
            out.close();

            String tgt = hsu.getHeaderField("location");
            Iterator<?> it = hsu.getHeaderFields().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                System.out.print(entry.getKey());
                System.out.print(" --> ");
                System.out.println(entry.getValue());
            }
            System.out.println(hsu.getResponseCode());
            if (tgt != null && hsu.getResponseCode() == 201) {
                System.out.println(tgt);

                System.out.println("Tgt is : " + tgt.substring(tgt.lastIndexOf("/") + 1));
                tgt = tgt.substring(tgt.lastIndexOf("/") + 1);
                bwr.close();
                closeConn(hsu);

                // String serviceURL = "https://myserver.com/testApplication";
                // String serviceURL = "http://atoll-dev.cls.fr:43080/thredds/dodsC";
                String serviceURL = "http://atoll-dev.cls.fr:10080/mywebapp/protected/index.jsp";
                String proxyCallbackUrl = "https://atoll-dev.cls.fr:8443/mywebapp/proxyCallback";
                String targetService = "http://atoll-dev.cls.fr:43080/thredds/dodsC/nrt_glo_hr_infrared_sst.ascii?time[0:1:0]";

                String encodedServiceURL = URLEncoder.encode("service", "utf-8") + "=" + URLEncoder.encode(serviceURL, "utf-8");
                System.out.println("Service url is : " + encodedServiceURL);

                String myURL = url + "/" + tgt;
                System.out.println(myURL);
                hsu = (HttpsURLConnection) openConn(myURL);
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

                // Cookie[] cookies = getServiceUrlResponse(ticket, serviceURL);

                // Assertion assertion = validateTicket(ticket, serviceURL);
                // StringBuffer stringBuffer = new StringBuffer();
                // debugAssertion(stringBuffer, assertion);
                // System.out.println(stringBuffer.toString());
                // String proxyTicket = getProxyTicketFor(assertion, targetService);
                // System.out.println(proxyTicket);

                proxyValidate(ticket, proxyCallbackUrl, serviceURL);

                // validateTicket2(ticket, serviceURL, targetService, proxyCallbackUrl);

                return true;

            } else {
                return false;
            }

        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            throw mue;

        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        }

    }

    public static boolean loginToCAS(String username, String password) throws Exception {

        String url = casServerUrlPrefix + "/v1/tickets";
        try {
            HttpsURLConnection hsu = (HttpsURLConnection) openConn(url);
            String s = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
            s += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

            System.out.println(s);
            OutputStreamWriter out = new OutputStreamWriter(hsu.getOutputStream());
            BufferedWriter bwr = new BufferedWriter(out);
            bwr.write(s);
            bwr.flush();
            bwr.close();
            out.close();

            String tgt = hsu.getHeaderField("location");
            Iterator<?> it = hsu.getHeaderFields().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                System.out.print(entry.getKey());
                System.out.print(" --> ");
                System.out.println(entry.getValue());
            }
            System.out.println(hsu.getResponseCode());
            if (tgt != null && hsu.getResponseCode() == 201) {
                System.out.println(tgt);

                System.out.println("Tgt is : " + tgt.substring(tgt.lastIndexOf("/") + 1));
                tgt = tgt.substring(tgt.lastIndexOf("/") + 1);
                bwr.close();
                closeConn(hsu);

                // String serviceURL = "https://myserver.com/testApplication";
                // String serviceURL = "http://atoll-dev.cls.fr:43080/thredds/dodsC";
                String serviceURL = "http://atoll-dev.cls.fr:43080/thredds/catalog.xml";

                String encodedServiceURL = URLEncoder.encode("service", "utf-8") + "=" + URLEncoder.encode(serviceURL, "utf-8");
                System.out.println("Service url is : " + encodedServiceURL);

                String myURL = url + "/" + tgt;
                System.out.println(myURL);
                hsu = (HttpsURLConnection) openConn(myURL);
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

        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            throw mue;

        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        }

    }

    static URLConnection openConn(String urlk) throws MalformedURLException, IOException {

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

    public static Cookie[] validateFromCAS2(String username, String password) throws Exception {

        String url = casServerUrlPrefix + "/v1/tickets?";

        String s = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
        s += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

        HttpState initialState = new HttpState();
        // Initial set of cookies can be retrieved from persistent storage and
        // re-created, using a persistence mechanism of choice,
        // Cookie mycookie = new Cookie(".foobar.com", "mycookie", "stuff", "/", null, false);

        // Create an instance of HttpClient.
        // HttpClient client = new HttpClient();
        HttpClient client = new HttpClient();

        Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 8443);

        URI uri = new URI(url + s, true);
        // use relative url only
        PostMethod httpget = new PostMethod(url);
        httpget.addParameter("username", username);
        httpget.addParameter("password", password);

        HostConfiguration hc = new HostConfiguration();
        hc.setHost("atoll-dev.cls.fr", 8443, easyhttps);
        // client.executeMethod(hc, httpget);

        client.setState(initialState);

        // Create a method instance.
        System.out.println(url + s);

        GetMethod method = new GetMethod(url + s);
        // GetMethod method = new GetMethod(url );

        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setProxy("proxy.cls.fr", 8080);
        client.setHostConfiguration(hostConfiguration);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        // String username = "dearith";
        // String password = "bienvenue";
        // Credentials credentials = new UsernamePasswordCredentials(username, password);
        // AuthScope authScope = new AuthScope("proxy.cls.fr", 8080);
        //           
        // client.getState().setProxyCredentials(authScope, credentials);
        Cookie[] cookies = null;

        try {
            // Execute the method.
            // int statusCode = client.executeMethod(method);
            int statusCode = client.executeMethod(hc, httpget);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            for (Header header : method.getRequestHeaders()) {
                System.out.println(header.getName());
                System.out.println(header.getValue());
            }
            // Read the response body.
            byte[] responseBody = method.getResponseBody();

            // Deal with the response.
            // Use caution: ensure correct character encoding and is not binary data
            System.out.println(new String(responseBody));

            System.out.println("Response status code: " + statusCode);
            // Get all the cookies
            cookies = client.getState().getCookies();
            // Display the cookies
            System.out.println("Present cookies: ");
            for (int i = 0; i < cookies.length; i++) {
                System.out.println(" - " + cookies[i].toExternalForm());
            }

            Assertion assertion = AssertionHolder.getAssertion();
            if (assertion == null) {
                System.out.println("<p>Assertion is null</p>");
            }

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            method.releaseConnection();
        }

        return cookies;

    }

    public static Cookie[] getServiceUrlResponse(String ticket, String url) {
        // Get initial state object
        HttpState initialState = new HttpState();
        // Initial set of cookies can be retrieved from persistent storage and
        // re-created, using a persistence mechanism of choice,
        // Cookie mycookie = new Cookie(".foobar.com", "mycookie", "stuff", "/", null, false);
        Cookie mycookie = new Cookie();
        // and then added to your HTTP state instance
        initialState.addCookie(mycookie);

        // Create an instance of HttpClient.
        // HttpClient client = new HttpClient();
        HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());

        client.setState(initialState);

        // Create a method instance.
        GetMethod method = new GetMethod(url + "?ticket=" + ticket);
        // GetMethod method = new GetMethod(url );

        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setProxy("proxy.cls.fr", 8080);
        client.setHostConfiguration(hostConfiguration);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        // String username = "dearith";
        // String password = "bienvenue";
        // Credentials credentials = new UsernamePasswordCredentials(username, password);
        // AuthScope authScope = new AuthScope("proxy.cls.fr", 8080);
        //           
        // client.getState().setProxyCredentials(authScope, credentials);
        Cookie[] cookies = null;

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            // Read the response body.
            byte[] responseBody = method.getResponseBody();

            // Deal with the response.
            // Use caution: ensure correct character encoding and is not binary data
            System.out.println(new String(responseBody));

            System.out.println("Response status code: " + statusCode);
            // Get all the cookies
            cookies = client.getState().getCookies();
            // Display the cookies
            System.out.println("Present cookies: ");
            for (int i = 0; i < cookies.length; i++) {
                System.out.println(" - " + cookies[i].toExternalForm());
            }

            Assertion assertion = AssertionHolder.getAssertion();
            if (assertion == null) {
                System.out.println("<p>Assertion is null</p>");
            }

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            method.releaseConnection();
        }

        return cookies;
    }

    public static void proxyValidate(String ticket, String proxyCallbackUrl, String url) {
        // Get initial state object
        HttpState initialState = new HttpState();
        // Initial set of cookies can be retrieved from persistent storage and
        // re-created, using a persistence mechanism of choice,
        // Cookie mycookie = new Cookie(".foobar.com", "mycookie", "stuff", "/", null, false);
        // Cookie mycookie = new Cookie();
        // // and then added to your HTTP state instance
        // initialState.addCookies(myCookies);

        // Create an instance of HttpClient.
        // HttpClient client = new HttpClient();
        HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());

        client.setState(initialState);

        // Create a method instance.
        String s = "";
        try {
            s = URLEncoder.encode("pgturl", "UTF-8") + "=" + URLEncoder.encode(proxyCallbackUrl, "UTF-8");
            s += "&" + URLEncoder.encode("ticket", "UTF-8") + "=" + URLEncoder.encode(ticket, "UTF-8");
            s += "&" + URLEncoder.encode("service", "UTF-8") + "=" + URLEncoder.encode(url, "UTF-8");
            System.out.println(s);

        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        GetMethod method = new GetMethod(casServerUrlPrefix + "/proxyValidate?" + s);

        // "https://atoll-dev.cls.fr:8443/mywebapp/proxyCallback" + "?ticket=" + ticket);
        // GetMethod method = new GetMethod(url );

        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setProxy("proxy.cls.fr", 8080);
        client.setHostConfiguration(hostConfiguration);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        // String username = "dearith";
        // String password = "bienvenue";
        // Credentials credentials = new UsernamePasswordCredentials(username, password);
        // AuthScope authScope = new AuthScope("proxy.cls.fr", 8080);
        //           
        // client.getState().setProxyCredentials(authScope, credentials);

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            // Read the response body.
            byte[] responseBody = method.getResponseBody();

            // Deal with the response.
            // Use caution: ensure correct character encoding and is not binary data
            System.out.println(new String(responseBody));

            System.out.println("Response status code: " + statusCode);
            // Get all the cookies
            Cookie[] cookies = client.getState().getCookies();
            // Display the cookies
            System.out.println("Present cookies: ");
            for (int i = 0; i < cookies.length; i++) {
                System.out.println(" - " + cookies[i].toExternalForm());
            }

            Assertion assertion = AssertionHolder.getAssertion();
            if (assertion == null) {
                System.out.println("<p>Assertion is null</p>");
            }

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            method.releaseConnection();
        }

    }

    public final static Assertion validateTicket(String ticket, String legacyServerServiceUrl) {
        // AttributePrincipal principal = null;
        String casServerUrl = casServerUrlPrefix;
        Cas20ProxyTicketValidator pv = new Cas20ProxyTicketValidator(casServerUrl);
        // pv.setAcceptAnyProxy(true);
        // pv.setProxyCallbackUrl("https://atoll-dev.cls.fr:8443/mywebapp/proxyCallback");

        // pv.setProxyGrantingTicketStorage(new ProxyGrantingTicketStorageImpl());
        // pv.setProxyRetriever(new Cas20ProxyRetriever(casServerUrlPrefix));
        pv.setRenew(false);

        Assertion assertion = null;
        try {
            // there is no need, that the legacy application is accessible
            // through this URL. But for validation purpose, even a non-web-app
            // needs a valid looking URL as identifier.
            // String legacyServerServiceUrl = "http://otherserver/legacy/service";
            assertion = pv.validate(ticket, legacyServerServiceUrl);
            // principal = a.getPrincipal();
            // System.out.println("user name:" + principal.getName());
        } catch (TicketValidationException e) {
            e.printStackTrace(); // bad style, but only for demonstration purpose.
        }
        return assertion;
    }

    //    
    // public final static void validateTicket2(String ticket, String legacyServerServiceUrl, String
    // targetService, String proxyCallbackUrl) {
    // String user = null;
    // String errorCode = null;
    // String errorMessage = null;
    // String xmlResponse = null;
    // List proxyList = null;
    //
    // /* instantiate a new ProxyTicketValidator */
    // ProxyTicketValidator pv = new ProxyTicketValidator();
    //
    // /* set its parameters */
    // pv.setCasValidateUrl(casServerUrlPrefix + "/proxyValidate");
    // pv.setService(legacyServerServiceUrl);
    // pv.setServiceTicket(ticket);
    //
    // /*
    // * If we want to be able to acquire proxy tickets (requires callback servlet to be set up
    // * in web.xml -- see below)
    // */
    //
    // //String urlOfProxyCallbackServlet = "https://portal.yale.edu/CasProxyServlet";
    // String urlOfProxyCallbackServlet = proxyCallbackUrl;
    // pv.setProxyCallbackUrl(urlOfProxyCallbackServlet);
    //
    // /* contact CAS and validate */
    // try {
    // pv.validate();
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (SAXException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (ParserConfigurationException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // /* if we want to look at the raw response, we can use getResponse() */
    // xmlResponse = pv.getResponse();
    //
    // /* read the response */
    // // Yes, this method is misspelled in this way
    // // in the ServiceTicketValidator implementation.
    // // Sorry.
    // if(pv.isAuthenticationSuccesful()) {
    // user = pv.getUser();
    // proxyList = pv.getProxyList();
    // } else {
    // errorCode = pv.getErrorCode();
    // errorMessage = pv.getErrorMessage();
    // /* handle the error */
    // }
    //
    // /* The user is now authenticated. */
    //
    // /* If we did set the proxy callback url, we can get proxy tickets with this method call:
    // */
    //
    // //String urlOfTargetService = "http://hkg2.its.yale.edu/someApp/portalFeed";
    // String urlOfTargetService = targetService;
    //        
    //
    // try {
    // String proxyTicket =
    // edu.yale.its.tp.cas.proxy.ProxyTicketReceptor.getProxyTicket(
    // pv.getPgtIou(),urlOfTargetService);
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } }

    public final static String getProxyTicketFor(Assertion assertion, String targetService) {
        String proxyTicket = "";

        if (assertion == null) {
            return proxyTicket;
        }

        AttributePrincipal attributePrincipal = assertion.getPrincipal();
        if (attributePrincipal == null) {
            return proxyTicket;
        }

        proxyTicket = attributePrincipal.getProxyTicketFor(targetService);
        return proxyTicket;
    }

    public static void debugAssertion(StringBuffer stringBuffer, Assertion assertion) {
        if (assertion == null) {
            stringBuffer.append("\nAssertion is null\n");
            return;
        }

        AttributePrincipal attributePrincipal = assertion.getPrincipal();
        if (attributePrincipal == null) {
            stringBuffer.append("\nAttributePrincipal is null\n");
            return;
        }

        Date validFromDate = assertion.getValidFromDate();
        Date validUntilDate = assertion.getValidUntilDate();

        String principal = attributePrincipal.getName();
        stringBuffer.append("\nPrincipal:\n");

        if (principal == null) {
            stringBuffer.append("null");
        } else {
            stringBuffer.append(principal);
        }

        stringBuffer.append("\nValid from:\n");

        if (validFromDate == null) {
            stringBuffer.append("null");
        } else {
            stringBuffer.append(validFromDate.toString());
        }

        stringBuffer.append("\nValid until:\n");

        if (validUntilDate == null) {
            stringBuffer.append("null");
        } else {
            stringBuffer.append(validUntilDate.toString());
        }
        stringBuffer.append("\nAttributes:\n");
        Iterator<?> it = assertion.getAttributes().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            stringBuffer.append("\n");
            stringBuffer.append(entry.getKey());
            stringBuffer.append("=");
            stringBuffer.append(entry.getValue());
            stringBuffer.append("\n");
        }

    }

    public static void testgetCASifiedResource() throws MotuException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("testLoginToCAS() - entering");
        }

        String serviceURL = "http://atoll-dev.cls.fr:43080/thredds/catalog.xml";
        String username = "dearith";
        String password = "bienvenue";

        String casRestUrlSuffix = Organizer.getMotuConfigInstance().getCasRestUrlSuffix();
        String casRestUrl = RestUtil.getCasRestletUrl(serviceURL, casRestUrlSuffix);

        String serviceTicket = RestUtil.loginToCAS(casRestUrl, username, password, serviceURL);

        if (LOG.isDebugEnabled()) {
            LOG.debug("testLoginToCAS() - serviceTicket:" + serviceTicket);
        }

        // final Map CONST_ATTRIBUTES = new HashMap();
        // CONST_ATTRIBUTES.put(username, serviceTicket);
        //
        // final AttributePrincipal CONST_PRINCIPAL = new AttributePrincipalImpl(username);
        // final Assertion assertion = new AssertionImpl(CONST_PRINCIPAL,
        // CONST_ATTRIBUTES);
        //        
        //        
        // AssertionHolder.setAssertion(assertion);

        // //////////////String path = AssertionUtils.addCASTicket(serviceTicket, serviceURL);

        InputStream in;

        fr.cls.atoll.motu.library.misc.tds.server.Catalog catalogXml;
        try {
            // JAXBContext jc = JAXBContext.newInstance(TDS_SCHEMA_PACK_NAME);
            // Unmarshaller unmarshaller = jc.createUnmarshaller();

            URL url = new URL(AssertionUtils.addCASTicket(serviceTicket, serviceURL));
            URLConnection conn = url.openConnection();
            in = conn.getInputStream();
            if (Organizer.getUnmarshallerTdsConfig() == null) {
                Organizer.initJAXBTdsConfig();
            }
            synchronized (Organizer.getUnmarshallerTdsConfig()) {
                catalogXml = (fr.cls.atoll.motu.library.misc.tds.server.Catalog) Organizer.getUnmarshallerTdsConfig().unmarshal(in);
            }
        } catch (Exception e) {
            throw new MotuException("Error in loadConfigTds", e);
        }
        if (catalogXml == null) {
            throw new MotuException(String
                    .format("Unable to load Tds configuration (in loadConfigOpendap, cataloXml is null) - url : %s", serviceURL));
        }
        try {
            in.close();
        } catch (IOException io) {
            io.getMessage();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("testLoginToCAS() - catalogXml:" + catalogXml);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("testLoginToCAS() - exiting");
        }

    }

    public static void testDownloadCASifiedResource() throws MotuException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("testDownloadCASifiedResource() - entering");
        }

        // http://atoll-dev.cls.fr:43080/thredds/dodsC/mercator_modified
        String serviceURL = "http://atoll-dev.cls.fr:30080/atoll-motuservlet/OpendapAuth?action=productdownload&data=http://atoll-dev.cls.fr:43080/thredds/dodsC/nrt_glo_hr_infrared_sst&x_lo=2&x_hi=3&y_lo=1&y_hi=4&t_lo=2009-12-01&t_hi=2009-12-01&variable=Grid_0001&mode=console";
        String username = "dearith";
        String password = "bienvenue";

        String casRestUrlSuffix = Organizer.getMotuConfigInstance().getCasRestUrlSuffix();
        String casRestUrl = RestUtil.getCasRestletUrl(serviceURL, casRestUrlSuffix);

        String serviceTicket = RestUtil.loginToCAS(casRestUrl, username, password, serviceURL);

        if (LOG.isDebugEnabled()) {
            LOG.debug("testDownloadCASifiedResource() - serviceTicket:" + serviceTicket);
        }

        // final Map CONST_ATTRIBUTES = new HashMap();
        // CONST_ATTRIBUTES.put(username, serviceTicket);
        //
        // final AttributePrincipal CONST_PRINCIPAL = new AttributePrincipalImpl(username);
        // final Assertion assertion = new AssertionImpl(CONST_PRINCIPAL,
        // CONST_ATTRIBUTES);
        //        
        //        
        // AssertionHolder.setAssertion(assertion);

        // //////////////String path = AssertionUtils.addCASTicket(serviceTicket, serviceURL);

        InputStream is;

        fr.cls.atoll.motu.library.misc.tds.server.Catalog catalogXml;
        try {
            // JAXBContext jc = JAXBContext.newInstance(TDS_SCHEMA_PACK_NAME);
            // Unmarshaller unmarshaller = jc.createUnmarshaller();

            URL url = new URL(AssertionUtils.addCASTicket(serviceTicket, serviceURL));
            URLConnection conn = url.openConnection();
            is = conn.getInputStream();
            InputStreamReader eisr = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(eisr);
            String nextLine = "";
            // StringBuffer stringBuffer = new StringBuffer();
            while ((nextLine = in.readLine()) != null) {
                // stringBuffer.append(nextLine);
                System.out.println(nextLine);
            }
            in.close();
        } catch (Exception e) {
            throw new MotuException("Error in loadConfigTds", e);
        }
        try {
            is.close();
        } catch (IOException io) {
            io.getMessage();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("testLoginToCAS() - exiting");
        }

    }
}
