package fr.cls.atoll.motu.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.cls.atoll.motu.api.MotuRequest;
import fr.cls.atoll.motu.api.MotuRequestException;
import fr.cls.atoll.motu.api.MotuRequestParameters;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */
public class TestCharge {

    /**
     * .
     * 
     * @param args
     */

    private List<Long> requestIDs = Collections.synchronizedList(new ArrayList<Long>());
    private List<Long> requestIDs2 = Collections.synchronizedList(new ArrayList<Long>());
    private List<Long> requestIDs3 = Collections.synchronizedList(new ArrayList<Long>());
    //private List<Long> requestIDs = new ArrayList<Long>();

    public Client client = null;
    public Client client2 = null;
    public Client client3 = null;

    public static void main(String[] args) {

        // String hostName = "10.1.253.61";
        // try {
        // hostName = InetAddress.getByName(hostName).getHostName();
        // } catch (UnknownHostException e) {
        // // Do Nothing
        // }
        // System.out.println(hostName);
        TestCharge testCharge = new TestCharge();

        testCharge.testQueueServer();

        while (testCharge.requestIDs.size() > 0) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        testCharge.client.running = false;
        testCharge.client2.running = false;
        testCharge.client3.running = false;

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testQueueServer() {

        // String servletUrl = "http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso";
        // String servletUrl = "http://localhost:8080/atoll-motu-servlet/Aviso";
        String servletUrl = "http://atoll-dev.cls.fr:30080/atoll-motuservlet/Aviso";
        //String servletUrl = "http://atoll-qt2.cls.fr:31080/atoll-motu-servlet/Motu";
        
        client = this.new Client("CLIENT 1", 500, servletUrl, requestIDs);
        client2 = this.new Client("CLIENT 2", 500, servletUrl, requestIDs2);
        client3 = this.new Client("CLIENT 3", 500, servletUrl, requestIDs3);
        client.start();
        client2.start();
        client3.start();


        try {
            for (int i = 0 ; i < 5 ; i++) {

                    launchMotuRequest(servletUrl);
                    // Thread.sleep(10000);
                    // return;

            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void launchMotuRequest(String servletUrl) {

        MotuRequestParameters motuRequestParameters = new MotuRequestParameters();

        MotuRequest motuRequest = new MotuRequest(servletUrl, motuRequestParameters);

        // motuRequest.setServletUrl("http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso");
        // motuRequest.setServletUrl("http://localhost:8080/atoll-motu-servlet/Aviso");

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOGIN, "Test User");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_PRODUCT_DOWNLOAD);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_SERVICE, "Mercator");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PRODUCT, "mercatorPsy3v1R1v_glo_mean_best_estimate");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_VARIABLE, "temperature");

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_START_DATE, "2008-05-27");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_END_DATE, "2008-05-27");

 
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE, MotuRequestParameters.PARAM_MODE_STATUS);

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_BATCH, "true");

        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PRIORITY, "1");
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MAX_POOL_ANONYMOUS, "30");
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MAX_POOL_AUTHENTICATE, "15");

        motuRequest.setMotuRequestParameters(motuRequestParameters);

        StatusModeResponse statusModeResponse = null;

        try {
            statusModeResponse = (StatusModeResponse) motuRequest.executeAsXML();
            if (statusModeResponse == null) {
                System.out.println("ERROR - no status found");
                return;
            }

            Long requestId = statusModeResponse.getRequestId();

            if (requestId == null) {
                System.out.println("ERROR - no request id set");
                return;
            }
            if (requestId.longValue() < 0) {
                System.out.println("ERROR - no request id set (< 0) ");
                return;
            }

            synchronized (requestIDs) {
                requestIDs.add(requestId);
            }
            synchronized (requestIDs2) {
                requestIDs2.add(requestId);
            }
            synchronized (requestIDs3) {
                requestIDs3.add(requestId);
            }
 
        } catch (Exception e) {
            System.out.print("ERROR in launchMotuRequest : ");
            System.out.println(e.getMessage());
        }

    }

    public class Client extends Thread {
        public Client(String name, int interval, String servletUrl, List<Long> requestIDs) {
            this.servletUrl = servletUrl;
            this.requestIDs = requestIDs;
            this.name = name;
            this.interval = interval;
            this.motuRequestGetStatus = new MotuRequest(this.servletUrl);
        }

        private boolean running = true;
        private List<Long> requestIDs = null;
        private String servletUrl = "";
        private String name = "";
        private int interval = 1000;
        MotuRequest motuRequestGetStatus = null;
        public void run() {

            while (running) {
                List<Long> requestIDsToDelete = new ArrayList<Long>();

                synchronized (requestIDs) {
                    System.out.print(">>>>>>>>>>>>>>>> ");
                    System.out.print(name);
                    System.out.println(" - Begin Status <<<<<<<<<<<<<<<<<<<<");
                    for (Long requestId : requestIDs) {
                        StatusModeResponse statusModeResponse = null;
                        try {
                            statusModeResponse = motuRequestGetStatus.executeActionGetStatusParams(requestId);
                        } catch (MotuRequestException e) {
                            System.out.print(String.format("%s - ERROR while getting request status (request ID %d:", name, requestId));
                            System.out.println(e.notifyException());

                            requestIDsToDelete.add(requestId);

                            continue;
                        }

                        if (MotuRequest.isStatusError(statusModeResponse)) {

                            System.out.print(String.format("%s - request %d in ERROR: ", name, requestId.longValue()));
                            System.out.println(statusModeResponse.getMsg());

                            requestIDsToDelete.add(requestId);

                        } else if (MotuRequest.isStatusDone(statusModeResponse)) {

                            System.out.print(String.format("%s - request %d in DONE and extracted file is: ", name, requestId.longValue()));
                            System.out.println(statusModeResponse.getMsg());

                            requestIDsToDelete.add(requestId);

                        } else {
                            System.out.print(String.format("%s - request %d: ", name, requestId.longValue()));
                            System.out.println(statusModeResponse.getMsg());
                        }
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            // Do nothing
                        }
                    }

                    System.out.print(">>>>>>>>>>>>>>>> ");
                    System.out.print(name);
                    System.out.println(" - End Status <<<<<<<<<<<<<<<<<<<<");
                    requestIDs.removeAll(requestIDsToDelete);
                }

//                try {
//                    Thread.sleep(interval);
//                } catch (InterruptedException e) {
//                    // Do nothing
//                }
            }

        }
    }

}
