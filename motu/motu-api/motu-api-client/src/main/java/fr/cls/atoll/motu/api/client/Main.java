package fr.cls.atoll.motu.api.client;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.AuthentificationMode;
import fr.cls.atoll.motu.library.misc.cas.util.AuthentificationHolder;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.intfce.User;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2010. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: $
 * @version $Revision: $ - $Date: $
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

        stringBuffer.append("\nwith action=resquest to execute (optional - default is '"+MotuRequestParametersConstant.ACTION_DESCRIBE_PRODUCT+"')\n");
        stringBuffer.append("\n");
        stringBuffer.append("\n==========\n");
        stringBuffer.append("action="+MotuRequestParametersConstant.ACTION_DESCRIBE_PRODUCT) ;
        stringBuffer.append("\n\nThis request allows to get the metadata of a product (currently only for TDS/Opendap media)");
        stringBuffer.append("\n");
        stringBuffer.append("PARAMETERS:\n");
        stringBuffer.append("\t"+MotuRequestParametersConstant.PARAM_DATA+"=dataset/product url (required)\n");
        stringBuffer.append("\t"+MotuRequestParametersConstant.PARAM_OUTPUT+"=output file path (optional - default is stdout)\n");
        stringBuffer.append("\t"+MotuRequestParametersConstant.PARAM_LOGIN+"=login authentification if needed (optional)\n");
        stringBuffer.append("\t"+MotuRequestParametersConstant.PARAM_PWD+"=password authentification if needed (optional)\n");
        stringBuffer.append("\t"+MotuRequestParametersConstant.PARAM_AUTHENTIFICATION_MODE+"=authentification mode (optional - default is '" + AuthentificationMode.CAS.toString() + "' - valid values: ");
        stringBuffer.append(AuthentificationMode.getAvailableValues().toString());
        stringBuffer.append(")\n");
        stringBuffer.append("\t"+MotuRequestParametersConstant.PARAM_XML_FILE+"=TDS Catalog file name (optional - default is '" + Organizer.TDS_CATALOG_FILENAME + "')\n");
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

        if ((user.getLogin() != null) && (user.getAuthentificationMode().equals(AuthentificationMode.NONE) )) {
            user.setAuthentificationMode(AuthentificationMode.CAS);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getUser() - exiting");
        }
        return user;

    }

    /**
     * Exec request.
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

        Writer writer = null;

        // Gets output file parameter (default stdout)
        String output = mapParams.get(MotuRequestParametersConstant.PARAM_OUTPUT);
        if (Main.isNullOrEmpty(output)) {
            writer = new PrintWriter(new OutputStreamWriter(System.out, Main.charsetName), true);
        } else {
            writer = new FileWriter(output);
        }

        // Gets and sets user parameters or null
        AuthentificationHolder.setUser(Main.getUser());   
        
        // Get the TDS Catalog file name
        String xmlFile = Main.getXmlFile();
        
        Organizer organizer = new Organizer();
        
        // Executes request
        organizer.getProductMetadataInfo(data, xmlFile, writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionDescribeProduct(String) - exiting");
        }
        return true;
    }
}
