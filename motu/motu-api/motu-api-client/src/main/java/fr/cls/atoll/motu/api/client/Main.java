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
package fr.cls.atoll.motu.api.client;

import fr.cls.atoll.motu.api.message.AuthenticationMode;
import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.library.misc.cas.util.AuthentificationHolder;
import fr.cls.atoll.motu.library.misc.cas.util.RestUtil;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.intfce.User;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Main entry for the console client api.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class Main {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(Main.class);

    /** The charset name. */
    private static String charsetName = "UTF-8";

    /** The map params containing parameters arguments. */
    private static Map<String, String> mapParams = new HashMap<String, String>();

    // // Create a trust manager that does not validate certificate chains
    // // See : http://www.exampledepot.com/egs/javax.net.ssl/trustall.html
    // static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
    // public java.security.cert.X509Certificate[] getAcceptedIssuers() {
    // return null;
    // }
    //
    // public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
    // }
    //
    // public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
    // }
    // } };

    /**
     * .
     * 
     * @param args
     * @throws MotuException
     */
    public static void main(String[] args) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("main(String[]) - entering");
        }

        // try {
        // SSLContext sc = SSLContext.getInstance("SSL");
        // sc.init(null, trustAllCerts, new java.security.SecureRandom());
        // HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        // System.out.println("fr.cls.atoll.motu.api.client OK");
        // System.out.println(System.getProperties());
        // System.out.println("fr.cls.atoll.motu.api.client OK");
        //        
        // String strClassPath = System.getProperty("java.class.path");
        // System.out.println("Classpath is " + strClassPath);
        // System.out.println(Organizer.class.getName());
        int status = 0;

        try {
            // Loads parameters
            Main.loadArgs(args);

            if (mapParams.isEmpty()) {
                printUsage();
                System.exit(-1);
            }

            // Execue the request.
            Main.execRequest();

        } catch (MotuExceptionBase e) {
            LOG.error("main(String[])", e);

            System.err.println(e.notifyException());
            Main.printUsage();
            status = -1;
        } catch (Exception e) {
            LOG.error("main(String[])", e);

            System.err.println(e.getMessage());
            Main.printUsage();
            status = -1;
        }

        System.exit(status);

        if (LOG.isDebugEnabled()) {
            LOG.debug("main(String[]) - exiting");
        }
    }

    /**
     * Test if a string is null or empty.
     * 
     * @param value string to be tested.
     * 
     * @return true if string is null or empty, otherwise false.
     */
    public static boolean isNullOrEmpty(String value) {

        if (value == null) {
            return true;
        }
        if (value.equals("")) {
            return true;
        }

        return false;
    }

    /**
     * Prints the usage.
     */
    public static void printUsage() {

        System.out.println(Main.getUsage());
        Main.logUsage();

    }

    public static void logUsage() {

        if (LOG.isInfoEnabled()) {
            LOG.info(getUsage());
        }

    }

    /**
     * Gets the usage.
     * 
     * @return the usage
     */
    public static String getUsage() {

        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("Java Motu APIs Client Application :");
        stringBuffer.append("\nCommand line:\n");
        stringBuffer.append("\n\tjava -jar motu-api-client-xxx.jar action=nnnn [PARAMETERS]\n");

        stringBuffer.append("\nwith action=resquest to execute (optional - default is '" + MotuRequestParametersConstant.ACTION_DESCRIBE_PRODUCT
                + "')\n");
        stringBuffer.append("\n");
        stringBuffer.append("\n==========\n");
        stringBuffer.append("action=" + MotuRequestParametersConstant.ACTION_DESCRIBE_PRODUCT);
        stringBuffer.append("\n\nThis request allows to get the metadata of a product (currently only for TDS/Opendap media)");
        stringBuffer.append("\n");
        stringBuffer.append("PARAMETERS:\n");
        stringBuffer.append("\t" + MotuRequestParametersConstant.PARAM_DATA + "=dataset/product url (required)\n");
        stringBuffer.append("\t" + MotuRequestParametersConstant.PARAM_OUTPUT + "=output file path (optional - default is stdout)\n");
        stringBuffer.append("\t" + MotuRequestParametersConstant.PARAM_LOGIN + "=login authentification if needed (optional)\n");
        stringBuffer.append("\t" + MotuRequestParametersConstant.PARAM_PWD + "=password authentification if needed (optional)\n");
        stringBuffer.append("\t" + MotuRequestParametersConstant.PARAM_AUTHENTIFICATION_MODE + "=authentification mode (optional - default is '"
                + AuthenticationMode.CAS.toString() + "' - valid values: ");
        stringBuffer.append(AuthenticationMode.getAvailableValues().toString());
        stringBuffer.append(")\n");
        stringBuffer.append("\t" + MotuRequestParametersConstant.PARAM_XML_FILE + "=TDS Catalog file name (optional - default is '"
                + Organizer.TDS_CATALOG_FILENAME + "')\n");
        stringBuffer
                .append("\t"
                        + MotuRequestParametersConstant.PARAM_EXTRA_METADATA
                        + "=true/false : true to get all metadata (metadata from TDS data and TDS xml configuration), false to get simplified metadata (matadata from TDS data only) TDS Catalog file name (optional - default is 'true')\n");

        stringBuffer.append("\n==========\n");

        return stringBuffer.toString();

    }

    /**
     * Load args.
     * 
     * @param args the args
     * 
     * @throws MotuException the motu exception
     */
    private static void loadArgs(String[] args) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadArgs(String[]) - entering");
        }

        for (String arg : args) {
            System.out.println(arg);
            String[] argArray = arg.split("=");
            if (argArray.length != 2) {
                String msg = String.format("Invalid parameter form: '%s'. Valid form is key=value", arg);
                throw new MotuException(msg);
            }
            mapParams.put(argArray[0], argArray[1]);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadArgs(String[]) - exiting");
        }
    }

    /**
     * Gets the action.
     * 
     * @return the action
     */
    private static String getAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAction() - entering");
        }

        String action = mapParams.get(MotuRequestParametersConstant.PARAM_ACTION);
        if (Main.isNullOrEmpty(action)) {
            action = MotuRequestParametersConstant.ACTION_DESCRIBE_PRODUCT;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getAction() - exiting");
        }
        return action;

    }

    /**
     * Gets the xml file.
     * 
     * @return the xml file
     */
    private static String getXmlFile() {
        return mapParams.get(MotuRequestParametersConstant.PARAM_XML_FILE);

    }

    /**
     * Checks if is extra metadata.
     * 
     * @return true, if is extra metadata
     */
    private static boolean isExtraMetadata() {
        String extraMetadataAsString = mapParams.get(MotuRequestParametersConstant.PARAM_EXTRA_METADATA);
        if (Main.isNullOrEmpty(extraMetadataAsString)) {
            return true;
        }
        extraMetadataAsString = extraMetadataAsString.trim();
        return extraMetadataAsString.equalsIgnoreCase("true") || extraMetadataAsString.equalsIgnoreCase("1");
    }

    /**
     * Gets the action.
     * 
     * @return the action
     * @throws MotuException
     */
    private static User getUser() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUser() - entering");
        }
        String login = mapParams.get(MotuRequestParametersConstant.PARAM_LOGIN);

        if (Main.isNullOrEmpty(login)) {
            return null;
        }

        User user = new User();

        user.setLogin(login);
        user.setPwd(mapParams.get(MotuRequestParametersConstant.PARAM_PWD));
        user.setAuthentificationMode(mapParams.get(MotuRequestParametersConstant.PARAM_AUTHENTIFICATION_MODE));

        if ((user.getLogin() != null) && (user.getAuthentificationMode().equals(AuthenticationMode.NONE))) {
            user.setAuthentificationMode(AuthenticationMode.CAS);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getUser() - exiting");
        }
        return user;

    }

    /**
     * Exec request.
     * 
     * @throws IOException
     * @throws MotuExceptionBase
     * @throws MotuMarshallException
     */
    private static void execRequest() throws MotuMarshallException, MotuExceptionBase, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("execRequest() - entering");
        }

        String action = Main.getAction();
        if (Main.isActionDescribeProduct(action)) {
            // Nothing to do
        } else {
            throw new MotuException(String.format("request '%s' is not implemented", action));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("execRequest() - exiting");
        }
    }

    /**
     * Checks if is action describe coverage.
     * 
     * @param action the action
     * 
     * @return true, if is action describe coverage
     * 
     * @throws MotuExceptionBase the motu exception base
     * @throws MotuMarshallException the motu marshall exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static boolean isActionDescribeProduct(String action) throws MotuMarshallException, MotuExceptionBase, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionDescribeProduct(String) - entering");
        }

        if (!action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_DESCRIBE_PRODUCT)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionDescribeProduct(String) - exiting");
            }
            return false;
        }
        String data = mapParams.get(MotuRequestParametersConstant.PARAM_DATA);
        if (Main.isNullOrEmpty(data)) {
            throw new MotuException(String.format("the required parameter '%' has not bee provided for request '%s'",
                                                  MotuRequestParametersConstant.PARAM_DATA,
                                                  action));
        }

        boolean casAuthentification = RestUtil.isCasifiedUrl(data);

        Writer writer = null;

        // Gets output file parameter (default stdout)
        String output = mapParams.get(MotuRequestParametersConstant.PARAM_OUTPUT);
        if (Main.isNullOrEmpty(output)) {
            writer = new PrintWriter(new OutputStreamWriter(System.out, Main.charsetName), true);
        } else {
            writer = new FileWriter(output);
        }

        // Gets and sets user parameters or null
        User user = Main.getUser();
        user.setCASAuthentification(casAuthentification);
        AuthentificationHolder.setUser(user);

        // Get the TDS Catalog file name
        String xmlFile = Main.getXmlFile();

        boolean loadExtraMetadata = Main.isExtraMetadata();

        Organizer organizer = new Organizer();

        // Executes request
        organizer.getProductMetadataInfo(data, xmlFile, loadExtraMetadata, writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionDescribeProduct(String) - exiting");
        }
        return true;
    }
}
