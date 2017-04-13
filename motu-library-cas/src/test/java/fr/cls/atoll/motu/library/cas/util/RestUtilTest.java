package fr.cls.atoll.motu.library.cas.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

import fr.cls.atoll.motu.library.cas.UserBase;
import fr.cls.atoll.motu.library.cas.exception.MotuCasBadRequestException;
import fr.cls.atoll.motu.library.cas.exception.MotuCasException;

public class RestUtilTest {

    /**
     * Create a client which trust any HTTPS server
     * 
     * @return
     */
    public static Client hostIgnoringClient() {
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, null, null);
            DefaultClientConfig config = new DefaultClientConfig();
            Map<String, Object> properties = config.getProperties();
            HTTPSProperties httpsProperties = new HTTPSProperties(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            }, sslcontext);
            properties.put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, httpsProperties);
            // config.getClasses().add( JacksonJsonProvider.class );
            return Client.create(config);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void checkAuthenticationMode() {
        Client c = hostIgnoringClient(); // new Client();
        UserBase ub = new UserBase();
        ub.setCASAuthentication(true);
        ub.setLogin("adminweb");
        ub.setPwd("");
        ub.setCasURL("https://coremis-cas.myocean.eu/cas");

        try {
            assertTrue("CAS".equalsIgnoreCase(RestUtil
                    .checkAuthenticationMode("http://motu-wmsdatastore.cls.fr/cls-gateway-servlet/Motu", c, RestUtil.HttpMethod.GET, ub).toString()));
            assertTrue("CAS".equalsIgnoreCase(RestUtil
                    .checkAuthenticationMode("https://motu-wmsdatastore.cls.fr/cls-gateway-servlet/Motu", c, RestUtil.HttpMethod.GET, ub)
                    .toString()));

            assertTrue("CAS".equalsIgnoreCase(RestUtil
                    .checkAuthenticationMode("https://motu.mfcglo-obs-qo.cls.fr/mfcglo-armor-gateway-servlet/Motu", c, RestUtil.HttpMethod.GET, ub)
                    .toString()));
            assertTrue("CAS".equalsIgnoreCase(RestUtil
                    .checkAuthenticationMode("http://motu.mfcglo-obs-qo.cls.fr/mfcglo-armor-gateway-servlet/Motu", c, RestUtil.HttpMethod.GET, ub)
                    .toString()));
        } catch (MotuCasBadRequestException e) {
            e.printStackTrace();
            fail();
        } catch (MotuCasException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void getRedirectUrl2() {
        // String path, Client client, RestUtil.HttpMethod method throws MotuCasBadRequestException
        Client c = new Client();
        try {
            assertTrue("".equalsIgnoreCase(RestUtil.getRedirectUrl("http://wpmanager.cls.fr/", c, RestUtil.HttpMethod.GET).toString()));
        } catch (MotuCasBadRequestException e) {
            e.printStackTrace();
            fail();
        }
    }

}
