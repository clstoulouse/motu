package fr.cls.atoll.motu.library.cas;

import opendap.dap.DConnect2;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;

import ucar.nc2.dods.DODSNetcdfFile;
import ucar.unidata.io.http.HTTPRandomAccessFile;

import fr.cls.atoll.motu.library.cas.util.AssertionUtils;
import fr.cls.atoll.motu.library.exception.MotuException;

public class HttpClientForCAS extends HttpClient {

    static {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpClientForCAS httpClient = new HttpClientForCAS(connectionManager);

        HttpClientParams clientParams = new HttpClientParams();
        clientParams.setParameter("http.protocol.allow-circular-redirects", true);
        httpClient.setParams(clientParams);

        DConnect2.setHttpClient(httpClient);

        connectionManager = new MultiThreadedHttpConnectionManager();
        httpClient = new HttpClientForCAS(connectionManager);

        clientParams = new HttpClientParams();
        clientParams.setParameter("http.protocol.allow-circular-redirects", true);
        httpClient.setParams(clientParams);

        HTTPRandomAccessFile.setHttpClient(httpClient);

        // DODSNetcdfFile.debugServerCall = true;
    }
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(HttpClientForCAS.class);

    // protected Assertion assertion;

    public HttpClientForCAS() {
    }

    public HttpClientForCAS(HttpClientParams params, HttpConnectionManager httpConnectionManager) {
        super(params, httpConnectionManager);
    }

    public HttpClientForCAS(HttpClientParams params) {
        super(params);
    }

    public HttpClientForCAS(HttpConnectionManager httpConnectionManager) {
        super(httpConnectionManager);

    }

    @Override
    public int executeMethod(HostConfiguration hostconfig, HttpMethod method, HttpState state) throws IOException, HttpException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeMethod(HostConfiguration, HttpMethod, HttpState) - entering");
        }

        addCASTicket(method);

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

        addCASTicket(method);

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

        addCASTicket(method);

        // TODO Auto-generated method stub
        int returnint = super.executeMethod(method);
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeMethod(HttpMethod) - exiting");
        }
        return returnint;
    }

     
    public static void addCASTicket(HttpMethod method) throws URIException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(HttpMethod) - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(HttpMethod) : debugHttpMethod BEFORE  " + HttpClientForCAS.debugHttpMethod(method));
        }
        String newURIAsString = AssertionUtils.addCASTicket(method.getURI().getEscapedURI());
        URI newURI = new URI(newURIAsString, true);

        method.setURI(newURI);

        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(HttpMethod) : debugHttpMethod AFTER  " + HttpClientForCAS.debugHttpMethod(method));
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
