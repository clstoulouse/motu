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

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.rest.MotuRequest;
import fr.cls.atoll.motu.api.rest.MotuRequestException;
import fr.cls.atoll.motu.api.rest.MotuRequestParameters;
import fr.cls.atoll.motu.library.misc.data.ServiceData;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ParameterMetaData;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ucar.nc2.Dimension;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class TestQueueServer {

    public class Client extends Thread {
        MotuRequest motuRequestGetStatus = null;

        private int interval = 1000;
        private String name = "";
        private List<Long> requestIDs = null;
        private boolean running = true;
        private String servletUrl = "";

        public Client(String name, int interval, String servletUrl, List<Long> requestIDs) {
            this.servletUrl = servletUrl;
            this.requestIDs = requestIDs;
            this.name = name;
            this.interval = interval;
            this.motuRequestGetStatus = new MotuRequest(this.servletUrl);
        }

        @Override
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
                            System.out.println(e.getMessage());

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

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }

        }
    }

    public Client client = null;
    public Client client2 = null;

    public Client client3 = null;
    /**
     * .
     * 
     * @param args
     */

    private final List<Long> requestIDs = Collections.synchronizedList(new ArrayList<Long>());
    private final List<Long> requestIDs2 = Collections.synchronizedList(new ArrayList<Long>());

    private final List<Long> requestIDs3 = Collections.synchronizedList(new ArrayList<Long>());

    // private List<Long> requestIDs = new ArrayList<Long>();

    public static void main(String[] args) {

        // String hostName = "10.1.253.61";
        // try {
        // hostName = InetAddress.getByName(hostName).getHostName();
        // } catch (UnknownHostException e) {
        // // Do Nothing
        // }
        // System.out.println(hostName);
        TestQueueServer testQueueServer = new TestQueueServer();

        testQueueServer.testQueueServer();

        while (testQueueServer.requestIDs.size() > 0) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        testQueueServer.client.running = false;
        testQueueServer.client2.running = false;
        testQueueServer.client3.running = false;

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void launchMotuRequest(ExtractionParameters extractionParameters, String servletUrl, FileWriter fileWriter) {

        MotuRequestParameters motuRequestParameters = new MotuRequestParameters();

        MotuRequest motuRequest = new MotuRequest(servletUrl, motuRequestParameters);

        // motuRequest.setServletUrl("http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso");
        // motuRequest.setServletUrl("http://localhost:8080/atoll-motu-servlet/Aviso");

        if (!extractionParameters.isAnonymousUser()) {
            motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOGIN, extractionParameters.getUserId());
        }

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_PRODUCT_DOWNLOAD);
        if (extractionParameters.getServiceName() != null) {
            motuRequestParameters.setParameter(MotuRequestParameters.PARAM_SERVICE, extractionParameters.getServiceName());
        }
        if (extractionParameters.getProductId() != null) {
            motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PRODUCT, extractionParameters.getProductId());
        }
        if (extractionParameters.getLocationData() != null) {
            motuRequestParameters.setParameter(MotuRequestParameters.PARAM_DATA, extractionParameters.getLocationData());
        }
        motuRequestParameters.setMultiValuedParameter(MotuRequestParameters.PARAM_VARIABLE, extractionParameters.getListVar());

        if (extractionParameters.getListTemporalCoverage() != null) {
            motuRequestParameters.setParameter(MotuRequestParameters.PARAM_START_DATE, extractionParameters.getListTemporalCoverage().get(0));
            motuRequestParameters.setParameter(MotuRequestParameters.PARAM_END_DATE, extractionParameters.getListTemporalCoverage().get(1));
        }

        if (extractionParameters.getListLatLonCoverage() != null) {
            motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LAT, extractionParameters.getListLatLonCoverage().get(0));
            motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LON, extractionParameters.getListLatLonCoverage().get(1));
            motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LAT, extractionParameters.getListLatLonCoverage().get(2));
            motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LON, extractionParameters.getListLatLonCoverage().get(3));
        }

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE, MotuRequestParameters.PARAM_MODE_STATUS);

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_BATCH, Boolean.toString(extractionParameters.isBatchQueue()));

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

            if (!MotuRequest.isStatusError(statusModeResponse)) {
                // fileWriter.append(motuRequest.getRequestUrl());
                fileWriter.append("wget \"$SERVLET_URL?");
                if (!Organizer.isNullOrEmpty(extractionParameters.getServiceName())) {
                    fileWriter.append("wget -o ");
                    fileWriter.append(extractionParameters.getServiceName());
                    fileWriter.append("_out.log");
                }
                fileWriter.append(" \"$SERVLET_URL?");
                fileWriter.append(motuRequest.getRequestParams());
                fileWriter.append("\"");
                fileWriter.append("\n");
                fileWriter.flush();
            }
        } catch (Exception e) {
            System.out.print("ERROR in launchMotuRequest : ");
            System.out.println(e.getMessage());
        }

    }

    public void testQueueServer() {

        // String servletUrl = "http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso";
        // String servletUrl = "http://localhost:8080/atoll-motu-servlet/Aviso";
        String servletUrl = "http://atoll-dev.cls.fr:30080/atoll-motuservlet/Aviso";
        // String servletUrl = "http://atoll-qt2.cls.fr:31080/atoll-motu-servlet/Motu";

        client = this.new Client("CLIENT 1", 1000, servletUrl, requestIDs);
        client2 = this.new Client("CLIENT 2", 1000, servletUrl, requestIDs2);
        client3 = this.new Client("CLIENT 3", 1000, servletUrl, requestIDs3);
        client.start();
        client2.start();
        client3.start();

        Random random = new Random();

        Organizer organizer = null;

        try {
            organizer = new Organizer();
            Collection<ServiceData> services = organizer.servicesValues();

            for (ServiceData service : services) {
                if (service.getName().equalsIgnoreCase("catsat")) {
                    continue;
                }
                if (service.getName().equalsIgnoreCase("motu")) {
                    continue;
                }
                // if (service.getName().equalsIgnoreCase("mercator")) {
                // continue;
                // }
                if (service.getName().equalsIgnoreCase("aviso")) {
                    continue;
                }
                int iCount = 0;
                FileWriter fileWriter = new FileWriter(String.format("request_%s.txt", service.getName()));
                fileWriter.append("#!/bin/sh");
                fileWriter.append("\n");
                fileWriter.append("SERVLET_URL=");
                fileWriter.append(servletUrl);
                fileWriter.append("\n");

                CatalogData catalog = service.getCatalog();
                Collection<Product> products = catalog.productsValues();

                for (Product product : products) {
                    String productId = product.getProductId();
                    product.loadOpendapGlobalMetaData();
                    ProductMetaData productMetaData = product.getProductMetaData();

                    // ParameterMetaData[] variables = new
                    // ParameterMetaData[productMetaData.parameterMetaDatasValues().size()];
                    ParameterMetaData[] variables = productMetaData.parameterMetaDatasValues().toArray(new ParameterMetaData[productMetaData
                            .parameterMetaDatasValues().size()]);
                    List<String> varToExtract = new ArrayList<String>();
                    int maxVarToExtract = variables.length >= 2 ? 2 : variables.length;
                    // for (int i = 0; i < variables.length; i++) {
                    // System.out.println(variables[i].getName());
                    // }

                    for (int i = 0; i < maxVarToExtract; i++) {
                        int iVarToExtract = random.nextInt(variables.length);
                        if (iVarToExtract == variables.length) {
                            iVarToExtract--;
                        }
                        if (!varToExtract.contains(variables[i])) {
                            varToExtract.add(variables[i].getName());
                        }

                    }
                    // TimeCoverage timeCoverage = organizer.getTimeCoverage(product.getLocationData());
                    // XMLGregorianCalendar start = timeCoverage.getStart().normalize();
                    // XMLGregorianCalendar end = timeCoverage.getEnd().normalize();
                    // System.out.print("Start: ");
                    // System.out.println(start.toString());
                    // System.out.print("End: ");
                    // System.out.println(end.toString());
                    // random.setSeed(variables.size()-1);

                    Double tMin = productMetaData.getTimeAxisMinValueAsDouble();
                    Double tMax = productMetaData.getTimeAxisMaxValueAsDouble();

                    int nbDayToExtract = random.nextInt(tMax.intValue() - tMin.intValue() + 1);

                    System.out.println(tMin);
                    System.out.println(tMax);
                    System.out.println(nbDayToExtract);

                    tMin = tMax - nbDayToExtract;

                    List<String> dateToExtract = new ArrayList<String>();
                    dateToExtract.add(NetCdfReader.getDateAsGMTNoZeroTimeString(tMin, productMetaData.getTimeAxis().getUnitsString()));
                    dateToExtract.add(NetCdfReader.getDateAsGMTNoZeroTimeString(tMax, productMetaData.getTimeAxis().getUnitsString()));

                    Double zMin = null;
                    Double zMax = null;
                    int nbZToExtract = 0;

                    List<String> zToExtract = new ArrayList<String>();

                    if (productMetaData.hasZAxis()) {
                        // zMin = productMetaData.getZAxisMinValue();
                        // zMax = productMetaData.getZAxisMaxValue();
                        // nbZToExtract = random.nextInt(zMax.intValue() - zMin.intValue() + 1);

                        Dimension zDim = productMetaData.getZAxis().getDimension(0);
                        if (zDim != null) {
                            nbZToExtract = random.nextInt(zDim.getLength());
                            if (nbZToExtract <= 0) {
                                nbZToExtract = 1;
                            }
                            zToExtract.add(product.getZAxisDataAsString().get(0));
                            zToExtract.add(product.getZAxisDataAsString().get(nbZToExtract - 1));
                        } else {
                            zToExtract.add(product.getZAxisDataAsString().get(0));
                        }
                        System.out.println(0);
                        System.out.println(nbZToExtract);
                    }

                    boolean anonymous = false;
                    // anonymous = random.nextBoolean();

                    String userId = null;
                    if (!anonymous) {
                        iCount++;
                        userId = String.format("%s_%s", service.getName(), iCount);
                        // userId = String.format("%s_%s", service.getName(),
                        // Integer.toString(random.nextInt(5)));
                    }

                    // ExtractionParameters extractionParameters = new ExtractionParameters(
                    // product.getLocationData(),
                    // varToExtract,
                    // dateToExtract,
                    // null,
                    // zToExtract,
                    // OutputFormat.NETCDF,
                    // null,
                    // null,
                    // userId,
                    // anonymous);

                    ExtractionParameters extractionParameters = new ExtractionParameters(
                            service.getName(),
                            varToExtract,
                            dateToExtract,
                            null,
                            zToExtract,
                            productId,
                            OutputFormat.NETCDF,
                            null,
                            null,
                            userId,
                            anonymous);
                    extractionParameters.setBatchQueue(random.nextBoolean());

                    launchMotuRequest(extractionParameters, servletUrl, fileWriter);
                    // Thread.sleep(10000);
                    // return;
                }

                fileWriter.close();
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
