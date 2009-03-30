package fr.cls.atoll.motu.web.servlet;

import java.io.IOException;
import java.util.ArrayList;

public class MotuWPSTester {

    /**
     * .
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
            try {
                String cmd  = "c:/curl/curl -v  -X POST --header \"Content-Type: text/xml\" -d @C:/Java/apache-tomcat-6.0.9/save/client/requests/wps/example/Execute/xml/TestProductTimeCoverage.xml http://localhost:8080/atoll-motuservlet/services";
                Runtime.getRuntime().exec(cmd);
                
                System.out.println(System.getProperties().toString());
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

    }

}
