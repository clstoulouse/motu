package fr.cls.atoll.motu.api.client;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.AuthentificationMode;
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
        // System.out.println("fr.cls.atoll.motu.api.client OK");
        // System.out.println(System.getProperties());
        // System.out.println("fr.cls.atoll.motu.api.client OK");
        //        
        // String strClassPath = System.getProperty("java.class.path");
        // System.out.println("Classpath is " + strClassPath);
        // System.out.println(Organizer.class.getName());
        int status = 0;

        try {
            Main.loadArgs(args);

            Main.execRequest();

        } catch (MotuExceptionBase e) {
            System.err.println(e.notifyException());
            Main.printUsage();
            status = -1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            Main.printUsage();
            status = -1;
        }

        System.exit(status);

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

    }

    /**
     * Load args.
     * 
     * @param args the args
     * 
     * @throws MotuException the motu exception
     */
    private static void loadArgs(String[] args) throws MotuException {
        for (String arg : args) {
            System.out.println(arg);
            String[] argArray = arg.split("=");
            if (argArray.length != 2) {
                String msg = String.format("Invalid parameter form: '%s'. Valid form is key=value", arg);
                throw new MotuException(msg);
            }
            mapParams.put(argArray[0], argArray[1]);
        }

    }

    /**
     * Gets the action.
     * 
     * @return the action
     */
    private static String getAction() {
        String action = mapParams.get(MotuRequestParametersConstant.PARAM_ACTION);
        if (Main.isNullOrEmpty(action)) {
            action = MotuRequestParametersConstant.ACTION_DESCRIBE_PRODUCT;
        }

        return action;

    }
    
    /**
     * Gets the action.
     * 
     * @return the action
     * @throws MotuException 
     */
    private static User getUser() throws MotuException {
        
        User user = new User();   
        
        user.setLogin(mapParams.get(MotuRequestParametersConstant.PARAM_LOGIN));
        user.setPwd(mapParams.get(MotuRequestParametersConstant.PARAM_PWD));
        user.setAuthentificationMode(mapParams.get(MotuRequestParametersConstant.PARAM_AUTHENTIFICATION_MODE));

        return user;

    }

    /**
     * Exec request.
     * @throws IOException 
     * @throws MotuExceptionBase 
     * @throws MotuMarshallException 
     */
    private static void execRequest() throws MotuMarshallException, MotuExceptionBase, IOException {

        String action = Main.getAction();
        if (Main.isActionDescribeCoverage(action)) {
            // Nothing to do
        } else {
            throw new MotuException(String.format("request '%s' is not implemented", action));
        }
    }

    /**
     * Checks if is action describe coverage.
     * 
     * @param action the action
     * 
     * @return true, if is action describe coverage
     * @throws MotuExceptionBase
     * @throws MotuMarshallException
     * @throws IOException
     */
    private static boolean isActionDescribeCoverage(String action) throws MotuMarshallException, MotuExceptionBase, IOException {

        if (!action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_DESCRIBE_PRODUCT)) {
            return false;
        }
        String data = mapParams.get(MotuRequestParametersConstant.PARAM_DATA);
        if (Main.isNullOrEmpty(data)) {
            throw new MotuException(String.format("the required parameter '%' has not bee provided for request '%s'",
                                                  MotuRequestParametersConstant.PARAM_DATA,
                                                  action));
        }

        Writer writer = null;

        String output = mapParams.get(MotuRequestParametersConstant.PARAM_OUTPUT);
        if (Main.isNullOrEmpty(output)) {
            writer = new PrintWriter(new OutputStreamWriter(System.out, Main.charsetName), true);
        } else {
            writer = new FileWriter(output);
        }

        User user = getUser();
        
        Organizer organizer = new Organizer(user);
        organizer.getProductMetadataInfo(data, writer);

        return true;
    }
}
