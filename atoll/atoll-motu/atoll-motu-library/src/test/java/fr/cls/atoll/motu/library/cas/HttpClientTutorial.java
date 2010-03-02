package fr.cls.atoll.motu.library.cas;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;

import fr.cls.atoll.motu.library.MyAuthenticator;

import java.io.*;
import java.net.Authenticator;

public class HttpClientTutorial {
  
  private static String url = "http://www.apache.org/";

  public static void main(String[] args) {
      
      //test();
//    System.setProperty("proxyHost", "proxy.cls.fr"); // adresse IP
//    System.setProperty("proxyPort", "8080");
//    System.setProperty("socksProxyPort", "1080");
//
//    Authenticator.setDefault(new MyAuthenticator());

    // Create an instance of HttpClient.
    //HttpClient client = new HttpClient();
    HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager()); 
    // Create a method instance.
    GetMethod method = new GetMethod(url);
    
    HostConfiguration hostConfiguration = new HostConfiguration();
    hostConfiguration.setProxy("proxy.cls.fr", 8080);
    client.setHostConfiguration(hostConfiguration);
    

        
    // Provide custom retry handler is necessary
    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
            new DefaultHttpMethodRetryHandler(3, false));
//    String username = "dearith";
//            String password = "bienvenue";
//            Credentials credentials = new UsernamePasswordCredentials(username, password);
//            AuthScope authScope = new AuthScope("proxy.cls.fr", 8080);
//     
//            client.getState().setProxyCredentials(authScope, credentials);
    
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
  public static void test() {
      
  HttpClient client = new HttpClient();
  client.getParams().setParameter("http.useragent", "Test Client");
  client.getParams().setParameter("http.connection.timeout",new Integer(5000));

  GetMethod method  = new GetMethod();
  FileOutputStream fos = null;

  try {

    method.setURI(new URI("http://www.google.com", true));
    int returnCode = client.executeMethod(method);

    if(returnCode != HttpStatus.SC_OK) {
      System.err.println(
        "Unable to fetch default page, status code: " + returnCode);
    }

    System.err.println(method.getResponseBodyAsString());

    method.setURI(new URI("http://www.google.com/images/logo.gif", true));
    returnCode = client.executeMethod(method);

    if(returnCode != HttpStatus.SC_OK) {
      System.err.println("Unable to fetch image, status code: " + returnCode);
    }

    byte[] imageData = method.getResponseBody();
    fos = new FileOutputStream(new File("google.gif"));
    fos.write(imageData);

    HostConfiguration hostConfig = new HostConfiguration();
    hostConfig.setHost("www.yahoo.com", null, 80, Protocol.getProtocol("http"));

    method.setURI(new URI("/", true));

    client.executeMethod(hostConfig, method);

    System.err.println(method.getResponseBodyAsString());

  } catch (HttpException he) {
    System.err.println(he);
  } catch (IOException ie) {
    System.err.println(ie);
  } finally {
    method.releaseConnection();
    if(fos != null) try { fos.close(); } catch (Exception fe) {}
  }


}  
}