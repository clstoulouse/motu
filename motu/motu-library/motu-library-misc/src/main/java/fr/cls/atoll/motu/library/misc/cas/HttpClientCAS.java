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
import fr.cls.atoll.motu.library.misc.cas.util.AuthentificationHolder;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.intfce.SimpleAuthenticator;

import java.io.IOException;
import java.net.Authenticator;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class HttpClientCAS extends HttpClient {

    static {
        // MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        // HttpClientCAS httpClient = new HttpClientCAS(connectionManager);
        //
        // HttpClientParams clientParams = new HttpClientParams();
        // clientParams.setParameter("http.protocol.allow-circular-redirects", true);
        // httpClient.setParams(clientParams);
        //
        // DConnect2.setHttpClient(httpClient);
        //
        // connectionManager = new MultiThreadedHttpConnectionManager();
        // httpClient = new HttpClientCAS(connectionManager);
        //
        // clientParams = new HttpClientParams();
        // clientParams.setParameter("http.protocol.allow-circular-redirects", true);
        // httpClient.setParams(clientParams);
        //
        // HTTPRandomAccessFile.setHttpClient(httpClient);
        //
        // DODSNetcdfFile.debugServerCall = true;
    }

    // /** Does Service needs CAS authentification to access catalog resources and data. */
    // protected final ThreadLocal<Boolean> isCas = new ThreadLocal<Boolean>();
    //
    // public ThreadLocal<Boolean> getIsCas() {
    // return isCas;
    // }

    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(HttpClientCAS.class);

    // protected Assertion assertion;

    public HttpClientCAS() {
        setProxy();
    }

    public HttpClientCAS(HttpClientParams params, HttpConnectionManager httpConnectionManager) {
        super(params, httpConnectionManager);
        setProxy();
        
    }

    public HttpClientCAS(HttpClientParams params) {
        super(params);
        setProxy();
    }

    public HttpClientCAS(HttpConnectionManager httpConnectionManager) {
        super(httpConnectionManager);
        setProxy();

    }
    
    /**
     * Sets the proxy.
     *
     * @param httpClient the new proxy
     */
    public void setProxy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProxy() - start");
        }

        String proxyHost = System.getProperty("proxyHost");
        String proxyPort = System.getProperty("proxyPort");

        if ((!Organizer.isNullOrEmpty(proxyHost)) && (!Organizer.isNullOrEmpty(proxyPort))) {
            this.getHostConfiguration().setProxy(proxyHost, Integer.parseInt(proxyPort));
            this.setProxyUser();

            if (LOG.isDebugEnabled()) {
                LOG.debug("setProxy() - proxy parameters are set: proxyHost=" + proxyHost + " - proxyPort=" + proxyPort);
                LOG.debug("setProxy() - end");
            }
            return;
        }
        
        proxyHost = System.getProperty("http.proxyHost");
        proxyPort = System.getProperty("http.proxyPort");

        if ((!Organizer.isNullOrEmpty(proxyHost)) && (!Organizer.isNullOrEmpty(proxyPort))) {
            this.getHostConfiguration().setProxy(proxyHost, Integer.parseInt(proxyPort));
            this.setProxyUser();            
            LOG.debug("setProxy() - proxy parameters are set: proxyHost=" + proxyHost + " - proxyPort=" + proxyPort);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("setProxy() - end");
        }
    }
    
    /**
     * Sets the proxy user.
     */
    public void setProxyUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProxyUser() - start");
        }

        String proxyLogin = System.getProperty("proxyLogin");
        String proxyPassword = System.getProperty("proxyPassword");
        
        if ((!Organizer.isNullOrEmpty(proxyLogin)) && (!Organizer.isNullOrEmpty(proxyPassword))) {
            Authenticator.setDefault(new SimpleAuthenticator(proxyLogin, proxyPassword));

            if (LOG.isDebugEnabled()) {
                LOG.debug("setProxy() - proxy parameters are set: proxyLogin=" + proxyLogin + " - proxyPassword=" + proxyPassword);
                LOG.debug("setProxyUser() - end");
            }
            return;
        }

        proxyLogin = System.getProperty("http.proxyLogin");
        proxyPassword = System.getProperty("http.proxyPassword");
        
        if ((!Organizer.isNullOrEmpty(proxyLogin)) && (!Organizer.isNullOrEmpty(proxyPassword))) {
            Authenticator.setDefault(new SimpleAuthenticator(proxyLogin, proxyPassword));
            if (LOG.isDebugEnabled()) {
                LOG.debug("setProxy() - proxy parameters are set: proxyLogin=" + proxyLogin + " - proxyPassword=" + proxyPassword);
                LOG.debug("setProxyUser() - end");
            }
        }
        


        if (LOG.isDebugEnabled()) {
            LOG.debug("setProxyUser() - end");
        }
    }

    @Override
    public int executeMethod(HostConfiguration hostconfig, HttpMethod method, HttpState state) throws IOException, HttpException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeMethod(HostConfiguration, HttpMethod, HttpState) - entering");
        }

        try {
            addCASTicket(method);
        } catch (MotuException e) {
            throw new HttpException(e.notifyException(), e);
        }

        // TODO Auto-generated method stub
        int returnint = super.executeMethod(hostconfig, method, state);
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeMethod(HostConfiguration, HttpMethod, HttpState) - exiting");
        }
        return returnint;
    }

    @Override
    public int executeMethod(HostConfiguration hostConfiguration, HttpMethod method) throws IOException, HttpException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeMethod(HostConfiguration, HttpMethod) - entering");
        }

        try {
            addCASTicket(method);
        } catch (MotuException e) {
            throw new HttpException(e.notifyException(), e);
        }

        // TODO Auto-generated method stub
        int returnint = super.executeMethod(hostConfiguration, method);
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeMethod(HostConfiguration, HttpMethod) - exiting");
        }
        return returnint;
    }

    @Override
    public int executeMethod(HttpMethod method) throws IOException, HttpException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeMethod(HttpMethod) - entering");
        }

        try {
            addCASTicket(method);
        } catch (MotuException e) {
            throw new HttpException(e.notifyException(), e);
        }

        // TODO Auto-generated method stub
        int returnint = super.executeMethod(method);
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeMethod(HttpMethod) - exiting");
        }
        return returnint;
    }

    public static void addCASTicket(HttpMethod method) throws IOException, MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(HttpMethod) - entering : debugHttpMethod BEFORE  " + HttpClientCAS.debugHttpMethod(method));
        }

        if (!AuthentificationHolder.isCASAuthentification()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicket(HttpMethod) - exiting - NO CAS AUTHENTIFICATION : debugHttpMethod AFTER  "
                        + HttpClientCAS.debugHttpMethod(method));
            }
            return;
        }

        String newURIAsString = AssertionUtils.addCASTicket(method.getURI().getEscapedURI());
        if (!AssertionUtils.hasCASTicket(newURIAsString)) {
            newURIAsString = AssertionUtils.addCASTicket(method.getURI().getEscapedURI(), AuthentificationHolder.getUser());

            if (!AssertionUtils.hasCASTicket(newURIAsString)) {

                String login = AuthentificationHolder.getUserLogin();
                throw new MotuException(
                        String
                                .format("Unable to access resource '%s'. This resource has been declared as CASified, but the Motu application/API can't retrieve any ticket from CAS via REST. \nFor information, current user login is:'%s'",
                                        method.getURI().getEscapedURI(),
                                        login));

            }
        }

        URI newURI = new URI(newURIAsString, true);

        method.setURI(newURI);

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(HttpMethod) - exiting : debugHttpMethod AFTER  " + HttpClientCAS.debugHttpMethod(method));
        }

    }

    public static String debugHttpMethod(HttpMethod method) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\nName:");
        stringBuffer.append(method.getName());
        stringBuffer.append("\n");
        stringBuffer.append("\nPath:");
        stringBuffer.append(method.getPath());
        stringBuffer.append("\n");
        stringBuffer.append("\nQueryString:");
        stringBuffer.append(method.getQueryString());
        stringBuffer.append("\n");
        stringBuffer.append("\nUri:");
        try {
            stringBuffer.append(method.getURI().toString());
        } catch (URIException e) {
            // Do nothing
        }
        stringBuffer.append("\n");
        HttpMethodParams httpMethodParams = method.getParams();
        stringBuffer.append("\nHttpMethodParams:");
        stringBuffer.append(httpMethodParams.toString());

        return stringBuffer.toString();

    }

}
