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

        // "http://atoll-dev.cls.fr:31080/mis-gateway-servlet/Motu?action=productdownload&service=http://purl.org/myocean/ontology/service/database#cls-toulouse-fr-armor-motu-rest&product=dataset-armor-3d-ran-v1-myocean&nexturl=+&x_lo=0.0&x_hi=20&y_lo=-15&y_hi=10&output=netcdf&region=0.0%2C20.0%2C-15.0%2C10.0&yhi_text=10&xlo_text=0&xhi_text=20&ylo_text=-15&t_lo_0=2009-12-16&t_lo=2009-12-16&t_hi_0=2009-12-30&t_hi=2009-12-30&z_lo_0=Surface&z_lo=Surface&z_hi_0=Surface&z_hi=Surface&variable=salinity&variable=temperature&mode=console";        
        
        System.out.println("START");
        List<Client> clients = new ArrayList<Client>();
        
        MotuServletTester motuServletTester = new MotuServletTester();
        String servletUrl = "http://atoll-dev.cls.fr:31080/mis-gateway-servlet/Motu";
        String cmd = "c:\\temp\\wget \""
        + servletUrl
        + "?action=describeProduct&data=http://atoll-misgw.vlandata.cls.fr:42080/thredds/dodsC/dataset-armor-3d-ran-v1-myocean&xmlfile=http://atoll-misgw.vlandata.cls.fr:42080/thredds/cls-myocean/armor-3d-ran-v1.xml\"";
        
        Client client1 = motuServletTester.new Client("CLIENT 1", 1000, servletUrl, cmd);
        //clients.add(client1);
        
        cmd = "c:\\temp\\wget -O c:\\temp\\test.nc \""
            + servletUrl
            //+ "?action=productdownload&service=http://purl.org/myocean/ontology/service/database#cls-toulouse-fr-armor-motu-rest&product=dataset-armor-3d-ran-v1-myocean&nexturl=+&x_lo=0.0&x_hi=20&y_lo=-15&y_hi=10&output=netcdf&region=0.0%2C20.0%2C-15.0%2C10.0&yhi_text=10&xlo_text=0&xhi_text=20&ylo_text=-15&t_lo_0=2009-12-16&t_lo=2009-12-16&t_hi_0=2009-12-30&t_hi=2009-12-30&z_lo_0=Surface&z_lo=Surface&z_hi_0=Surface&z_hi=Surface&variable=salinity&variable=temperature&mode=console\"";
            //+ "?action=productdownload&service=http%3A%2F%2Fpurl.org%2Fmyocean%2Fontology%2Fservice%2Fdatabase%23cls-toulouse-fr-armor-motu-rest&product=dataset-armor-3d-ran-v1-myocean&nexturl=+&x_lo=0.0&x_hi=20&y_lo=-15&y_hi=10&output=netcdf&region=0.0%2C20.0%2C-15.0%2C10.0&yhi_text=10&xlo_text=0&xhi_text=20&ylo_text=-15&t_lo_0=2009-12-16&t_lo=2009-12-16&t_hi_0=2009-12-30&t_hi=2009-12-30&z_lo_0=Surface&z_lo=Surface&z_hi_0=Surface&z_hi=Surface&variable=salinity&variable=temperature&mode=status\"";
            + "?action=productdownload&service=http%3A%2F%2Fpurl.org%2Fmyocean%2Fontology%2Fservice%2Fdatabase%23cls-toulouse-fr-armor-motu-rest&product=dataset-armor-3d-ran-v1-myocean&nexturl=+&x_lo=0.0&x_hi=20&y_lo=-15&y_hi=10&output=netcdf&region=0.0%2C20.0%2C-15.0%2C10.0&yhi_text=10&xlo_text=0&xhi_text=20&ylo_text=-15&t_lo_0=2009-04-08&t_lo=2009-04-08&t_hi_0=2009-12-30&t_hi=2009-12-30&z_lo_0=Surface&z_lo=Surface&z_hi_0=50&z_hi=50&variable=salinity&variable=temperature&mode=status\"";        
        
        Client client2 = motuServletTester.new Client("CLIENT 2", 1000, servletUrl, cmd);
        clients.add(client2);

        Client client2b = motuServletTester.new Client("CLIENT 2b", 1000, servletUrl, cmd);
        clients.add(client2b);

        for (Client c : clients) {
            c.start();
        }
        System.out.println("STARTED");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("ENDING");

        for (Client c : clients) {
            c.running = false;
        }

        System.out.println("ENDED");

    }

    public class Client extends Thread {
        MotuRequest motuRequestGetStatus = null;

        private int interval = 1000;
        private String name = "";
        private boolean running = true;
        private String servletUrl = "";
        private String cmd = "";

        public Client(String name, int interval, String servletUrl, String cmd) {
            this.servletUrl = servletUrl;
            this.name = name;
            this.interval = interval;
            this.cmd = cmd;
        }

        @Override
        public void run() {

            while (running) {
                //String cmd = "c:\\temp\\wget -o c:\\temp\\log.txt -O c:\\temp\\log.xml \""
//                String cmd = "c:\\temp\\wget -O " + result + " \""
//                        + servletUrl
//                        + "?action=describeProduct&data=http://atoll-misgw.vlandata.cls.fr:42080/thredds/dodsC/dataset-armor-3d-ran-v1-myocean&xmlfile=http://atoll-misgw.vlandata.cls.fr:42080/thredds/cls-myocean/armor-3d-ran-v1.xml\"";
                try {
                    Process process = Runtime.getRuntime().exec(cmd);
                    
                    IOUtils.copy(process.getErrorStream(), System.out);
                    Thread.sleep(interval);
                } catch (Exception e) {
                    e.printStackTrace();
                    running = false;
                }
            }

        }

    }
}
