package fr.cls.atoll.motu.library.cas;

import java.io.IOException;

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

import fr.cls.atoll.motu.library.cas.util.AssertionUtils;
import fr.cls.atoll.motu.library.cas.util.CasAuthentificationHolder;
import fr.cls.atoll.motu.library.exception.MotuException;

public class HttpClientCAS extends HttpClient {

    static {
//        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
//        HttpClientCAS httpClient = new HttpClientCAS(connectionManager);
//
//        HttpClientParams clientParams = new HttpClientParams();
//        clientParams.setParameter("http.protocol.allow-circular-redirects", true);
//        httpClient.setParams(clientParams);
//
//        DConnect2.setHttpClient(httpClient);
//
//        connectionManager = new MultiThreadedHttpConnectionManager();
//        httpClient = new HttpClientCAS(connectionManager);
//
//        clientParams = new HttpClientParams();
//        clientParams.setParameter("http.protocol.allow-circular-redirects", true);
//        httpClient.setParams(clientParams);
//
//        HTTPRandomAccessFile.setHttpClient(httpClient);
//
        // DODSNetcdfFile.debugServerCall = true;
    }

//    /** Does Service needs CAS authentification to access catalog resources and data. */
//    protected final ThreadLocal<Boolean> isCas = new ThreadLocal<Boolean>();
//
//    public ThreadLocal<Boolean> getIsCas() {
//        return isCas;
//    }


    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(HttpClientCAS.class);

    // protected Assertion assertion;

    public HttpClientCAS() {
    }

    public HttpClientCAS(HttpClientParams params, HttpConnectionManager httpConnectionManager) {
        super(params, httpConnectionManager);
    }

    public HttpClientCAS(HttpClientParams params) {
        super(params);
    }

    public HttpClientCAS(HttpConnectionManager httpConnectionManager) {
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

    public static void addCASTicket(HttpMethod method) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addCASTicket(HttpMethod) - entering : debugHttpMethod BEFORE  " + HttpClientCAS.debugHttpMethod(method));
        }

        if (!CasAuthentificationHolder.isCasAuthentification()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCASTicket(HttpMethod) - exiting - NO CAS AUTHENTIFICATION : debugHttpMethod AFTER  " + HttpClientCAS.debugHttpMethod(method));
            }
            return;            
        }
        
        String newURIAsString = AssertionUtils.addCASTicket(method.getURI().getEscapedURI());
        if (!AssertionUtils.hasCASTicket(newURIAsString)) {
            throw new IOException(
                    "Unable to access resource '%s'. This resource has been declared as CASified, but the Motu application is not. \nTo access this TDS, the Motu Application must be CASified.");
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
