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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

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
import fr.cls.atoll.motu.library.cas.exception.MotuCasBadRequestException;
import fr.cls.atoll.motu.library.cas.util.AssertionUtils;
import fr.cls.atoll.motu.library.cas.util.RestUtil;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;

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
        String username = "xxx";
        String password = "xxx";
        // validateFromCAS(username, password);
        // loginToCAS(username, password);
        // getRedirectUrl();

        // validateFromCAS2(username, password);

        // testgetCASifiedResource();
        testDownloadCASifiedResource();

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
        // String username = "xxx";
        // String password = "xxx";
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

    public static void testgetCASifiedResource() throws MotuException, IOException, MotuCasBadRequestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("testLoginToCAS() - entering");
        }

        String serviceURL = "http://atoll-dev.cls.fr:43080/thredds/catalog.xml";
        String username = "xxx";
        String password = "xxx";

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

    public static void testDownloadCASifiedResource() throws MotuException, IOException, MotuCasBadRequestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("testDownloadCASifiedResource() - entering");
        }

        // http://atoll-dev.cls.fr:43080/thredds/dodsC/mercator_modified
        //String serviceURL = "http://atoll-dev.cls.fr:30080/atoll-motuservlet/OpendapAuth?action=productdownload&data=http://atoll-dev.cls.fr:43080/thredds/dodsC/nrt_glo_hr_infrared_sst&x_lo=2&x_hi=3&y_lo=1&y_hi=4&t_lo=2009-12-01&t_hi=2009-12-01&variable=Grid_0001&mode=console";
        String serviceURL = "http://atoll-dev.cls.fr:30080/mis-gateway-servlet/Motu?z_lo=0&y_lo=30&t_hi=2011-01-26&mode=console&product=dataset-psy2v3-pgs-med-myocean-bestestimate&x_lo=-6.0&y_hi=31&t_lo=2011-01-26&z_hi=0&action=productdownload&service=http%3A%2F%2Fpurl.org%2Fcls%2Fatoll%2Fontology%2Findividual%2Fatoll%23motu-opendap-mercator-myocean&x_hi=1.0&variable=u&variable=v";
        String username = "xxx";
        String password = "xxx";

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
