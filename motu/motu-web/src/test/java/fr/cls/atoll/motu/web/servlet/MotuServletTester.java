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
package fr.cls.atoll.motu.web.servlet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.rest.MotuRequest;
import fr.cls.atoll.motu.api.rest.MotuRequestException;
import fr.cls.atoll.motu.web.servlet.TestQueueServer.Client;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuServletTester {

    public static void main(String[] args) throws Exception {
        // // String [] envp = new String[2];
        // // envp[0] = "http.proxyHost=proxy-bureautique.cls.fr";
        // // envp[1] = "http.proxyPort=8080";
        //String cmd = "c:\\temp\\wget -O c:\\temp\\log.xml \"http://atoll-dev.cls.fr:31080/mis-gateway-servlet/Motu?action=describeProduct&data=http://atoll-misgw.vlandata.cls.fr:42080/thredds/dodsC/dataset-armor-3d-ran-v1-myocean&xmlfile=http://atoll-misgw.vlandata.cls.fr:42080/thredds/cls-myocean/armor-3d-ran-v1.xml\"";
        //String cmd = "c:\\temp\\wget --help";
        // //Runtime.getRuntime().exec(cmd, envp);
        //Process process = Runtime.getRuntime().exec(cmd);
        
        //IOUtils.copy(process.getErrorStream(), System.out);

        //        
        //
        // System.out.println(System.getProperties().toString());

        System.out.println("START");
        MotuServletTester motuServletTester = new MotuServletTester();
        String servletUrl = "http://atoll-dev.cls.fr:31080/mis-gateway-servlet/Motu";
        Client client1 = motuServletTester.new Client("CLIENT 1", 1000, servletUrl);
        Client client2 = motuServletTester.new Client("CLIENT 2", 1000, servletUrl);
        client1.start();
        //client2.start();
        System.out.println("STARTED");

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("ENDING");

        client1.running = false;
        client2.running = false;
        System.out.println("ENDED");

    }

    public class Client extends Thread {
        MotuRequest motuRequestGetStatus = null;

        private int interval = 1000;
        private String name = "";
        private boolean running = true;
        private String servletUrl = "";

        public Client(String name, int interval, String servletUrl) {
            this.servletUrl = servletUrl;
            this.name = name;
            this.interval = interval;
        }

        @Override
        public void run() {

            while (running) {
                //String cmd = "c:\\temp\\wget -o c:\\temp\\log.txt -O c:\\temp\\log.xml \""
                String result = "c:\\temp\\" + getName() + ".xml";
                String cmd = "c:\\temp\\wget -O " + result + " \""
                        + servletUrl
                        + "?action=describeProduct&data=http://atoll-misgw.vlandata.cls.fr:42080/thredds/dodsC/dataset-armor-3d-ran-v1-myocean&xmlfile=http://atoll-misgw.vlandata.cls.fr:42080/thredds/cls-myocean/armor-3d-ran-v1.xml\"";
                try {
                    Process process = Runtime.getRuntime().exec(cmd);
                    
                    //IOUtils.copy(process.getErrorStream(), System.out);
                    Thread.sleep(interval);
                } catch (Exception e) {
                    e.printStackTrace();
                    running = false;
                }
            }

        }

    }
}
