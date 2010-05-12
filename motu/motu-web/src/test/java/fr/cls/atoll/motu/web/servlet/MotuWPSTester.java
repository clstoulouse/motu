package fr.cls.atoll.motu.web.servlet;

import java.io.IOException;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuWPSTester {

    /**
     * .
     * 
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            String cmd = "c:/curl/curl -v  -X POST --header \"Content-Type: text/xml\" -d @C:/Java/apache-tomcat-6.0.9/save/client/requests/wps/example/Execute/xml/TestProductTimeCoverage.xml http://localhost:8080/atoll-motuservlet/services";
            Runtime.getRuntime().exec(cmd);

            System.out.println(System.getProperties().toString());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
