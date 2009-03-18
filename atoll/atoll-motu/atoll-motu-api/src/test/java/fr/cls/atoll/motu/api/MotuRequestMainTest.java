package fr.cls.atoll.motu.api;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.XMLGregorianCalendar;

import fr.cls.atoll.motu.api.MotuRequest;
import fr.cls.atoll.motu.api.MotuRequestException;
import fr.cls.atoll.motu.api.MotuRequestParameters;
import fr.cls.atoll.motu.api.MotuRequestParametersConstant;
import fr.cls.atoll.motu.msg.xml.RequestSize;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;
import fr.cls.atoll.motu.msg.xml.StatusModeType;
import fr.cls.atoll.motu.msg.xml.TimeCoverage;

/**
 * Programme de test de l'API motu. <br>
 * <br>
 * Copyright : Copyright (c) 2007 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Jean-Michel FARENC
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */
public class MotuRequestMainTest {

    /**
     * Main du programme de test
     * 
     * @param args non utilisé
     * @throws Exception si une erreur se produit
     */

    public static void main(String[] args) throws Exception {

        // testMode();
        // testMode2();
        // testModeStatusAsString();
        // testModeStatusAsXMLFile();
        testModeStatusAsXML();
//        testGetSize();
//        testGetSize();
//        testGetTimeCoverage();
//        testGetTimeCoverage();
        //testDeleteFile();
        // testSynchronized();
        //testUrlWithUserPwd();
        
    }

    public static void testUrlWithUserPwd() {
        // (http://)(.*)\:(.*)\@(.*)
        String url = "http://evetkar:p61mlkm@atoll-motu.mercator-ocean.fr";
        String patternExpression = "(http://)(.*)\\:(.*)\\@(.*)";

        Pattern pattern = Pattern.compile(patternExpression);
        Matcher matcher = pattern.matcher(url);
        System.out.println(matcher.groupCount());
        while (matcher.find()) {
            for (int i = 1 ; i <= matcher.groupCount() ; i++) {
                CharSequence line = matcher.group(i);
                System.out.println(line);
            }
        }
        
        Map<String, String> userInfo = MotuRequest.searchUrlUserPwd(url);
        
        if (userInfo != null) {
            System.out.println(userInfo.get(MotuRequestParametersConstant.PARAM_LOGIN));
            System.out.println(userInfo.get(MotuRequestParametersConstant.PARAM_PWD));
            System.out.println(userInfo.get(MotuRequestParametersConstant.PARAM_MODE_URL));
        }

    }
    public static void testMode() throws Exception {
        MotuRequest motuRequest = new MotuRequest();

        motuRequest.setServletUrl("http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso");

        MotuRequestParameters motuRequestParameters = new MotuRequestParameters();

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOGIN, "toto");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PWD, "pass");

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_PRODUCT_DOWNLOAD);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_SERVICE, "AvisoNRT");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_DATA,
                                           "http://opendap.aviso.oceanobs.com/thredds/dodsC/duacs_global_nrt_madt_merged_h");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_VARIABLE, "Grid_0001");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_START_DATE, "2007-08-19");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_END_DATE, "2007-08-19");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LAT, "-40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LON, "-40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LAT, "40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LON, "40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE, MotuRequestParameters.PARAM_MODE_CONSOLE);
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE,
        // MotuRequestParameters.PARAM_MODE_URL);
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE,
        // MotuRequestParameters.PARAM_MODE_STATUS);

        motuRequest.setMotuRequestParameters(motuRequestParameters);

        InputStream in = motuRequest.execute();
        OutputStream out = new FileOutputStream("c:\\temp\\data.nc");
        int c;
        int nb = 0;
        while ((c = in.read()) >= 0) {
            out.write(c);
            ++nb;
        }
        System.out.println(nb);

        in.close();
        out.close();

    }

    public static void testMode2() throws Exception {
        MotuRequest motuRequest = new MotuRequest();

        motuRequest.setServletUrl("https://themisoceano.cls.fr/atoll-motuservlet/Catsat");

        List<String> variableList = new ArrayList<String>();
        variableList.add("sea_water_potential_temperature");

        MotuRequestParameters motuRequestParameters = new MotuRequestParameters();

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_Z, 300);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_Z, 0);

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOGIN, "jmz");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PWD, "zmj");

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_PRODUCT_DOWNLOAD);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_SERVICE, "Mercator");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_DATA,
                                           "http://opendap.mercator-ocean.fr/thredds/dodsC/mercatorPsy3v1R1v_glo_mean_best_estimate");
        motuRequestParameters.setMultiValuedParameter(MotuRequestParameters.PARAM_VARIABLE, variableList);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_START_DATE, "2008-02-06");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_END_DATE, "2008-02-06");
        /*
         * motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LAT, "-10.0");
         * motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LON, "-10.0");
         * motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LAT, "10.0");
         * motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LON, "10.0");
         */
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE, MotuRequestParameters.PARAM_MODE_CONSOLE);
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE,
        // MotuRequestParameters.PARAM_MODE_URL);
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE,
        // MotuRequestParameters.PARAM_MODE_STATUS);

        motuRequest.setMotuRequestParameters(motuRequestParameters);

        InputStream in = motuRequest.execute();
        OutputStream out = new FileOutputStream("c:\\temp\\data2.nc");
        int c;
        int nb = 0;
        while ((c = in.read()) >= 0) {
            out.write(c);
            ++nb;
        }
        System.out.println(nb);

        in.close();
        out.close();

    }

    public static void testModeStatusAsString() throws Exception {
        MotuRequest motuRequest = new MotuRequest();

        motuRequest.setServletUrl("http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso");

        MotuRequestParameters motuRequestParameters = new MotuRequestParameters();

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOGIN, "toto");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PWD, "pass");

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_PRODUCT_DOWNLOAD);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_SERVICE, "AvisoNRT");
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_DATA,
        // "http://opendap.aviso.oceanobs.com/thredds/dodsC/duacs_global_nrt_madt_merged_h");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PRODUCT, "duacs_global_nrt_madt_merged_h");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_VARIABLE, "Grid_0005");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_START_DATE, "2007-08-19");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_END_DATE, "2007-08-19");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LAT, "-40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LON, "-40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LAT, "40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LON, "40.0");
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE,
        // MotuRequestParameters.PARAM_MODE_CONSOLE);
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE,
        // MotuRequestParameters.PARAM_MODE_URL);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE, MotuRequestParameters.PARAM_MODE_STATUS);

        motuRequest.setMotuRequestParameters(motuRequestParameters);

        // InputStream in = motuRequest.execute();
        // OutputStream out = new FileOutputStream("c:\\temp\\data.nc");
        // int c;
        // int nb = 0;
        // while ((c = in.read()) >= 0) {
        // out.write(c);
        // ++nb;
        // }
        // System.out.println(nb);
        //
        //        
        // in.close();
        // out.close();

        String str = motuRequest.executeAsString();
        System.out.println(str);

        while (true) {
            URL url = new URL(str);
            InputStream in = url.openStream();
            int c;
            while ((c = in.read()) >= 0) {
                System.out.print((char) c);
            }
            System.out.println();
            Thread.sleep(1000);
        }

    }

    public static void testModeStatusAsXMLFile() {
        MotuRequest motuRequest = new MotuRequest();

        motuRequest.setServletUrl("http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso");
        // motuRequest.setServletUrl("http://localhost:8080/atoll-motu-servlet/Aviso");

        MotuRequestParameters motuRequestParameters = new MotuRequestParameters();

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOGIN, "toto");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PWD, "pass");

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_PRODUCT_DOWNLOAD);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_SERVICE, "AvisoNRT");
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_DATA,
        // "http://opendap.aviso.oceanobs.com/thredds/dodsC/duacs_global_nrt_madt_merged_h");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PRODUCT, "duacs_global_nrt_madt_merged_h");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_VARIABLE, "Grid_0001");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_START_DATE, "2007-08-19");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_END_DATE, "2007-08-19");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LAT, "-40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LON, "-40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LAT, "40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LON, "40.0");
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE,
        // MotuRequestParameters.PARAM_MODE_CONSOLE);
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE,
        // MotuRequestParameters.PARAM_MODE_URL);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE, MotuRequestParameters.PARAM_MODE_STATUS);

        motuRequest.setMotuRequestParameters(motuRequestParameters);

        // InputStream in = motuRequest.execute();
        // OutputStream out = new FileOutputStream("c:\\temp\\data.nc");
        // int c;
        // int nb = 0;
        // while ((c = in.read()) >= 0) {
        // out.write(c);
        // ++nb;
        // }
        // System.out.println(nb);
        //
        //        
        // in.close();
        // out.close();

        // ---------------- Utilisation 1 -------------------------
        String str;
        StatusModeResponse statusModeResponse;
        try {
            str = motuRequest.executeAsString();
            System.out.println(str);
            statusModeResponse = (StatusModeResponse) MotuRequest.getMessageAsXML(str);

            while (statusModeResponse.getStatus().compareTo(StatusModeType.INPROGRESS) == 0) {
                System.out.print("request is in progress and msg is : ");
                System.out.println(statusModeResponse.getMsg());
                Thread.sleep(2000);
                statusModeResponse = (StatusModeResponse) MotuRequest.getMessageAsXML(str);
            }

            if (statusModeResponse.getStatus().compareTo(StatusModeType.ERROR) == 0) {
                System.out.print("request error is : ");
                System.out.println(statusModeResponse.getMsg());
            }
            if (statusModeResponse.getStatus().compareTo(StatusModeType.DONE) == 0) {
                System.out.print("request is done and extracted file is : ");
                System.out.println(statusModeResponse.getMsg());
            }
        } catch (Exception e) {
            System.out.print("ERROR in TestGetTimeCoverage : ");
            System.out.println(e.getMessage());
        }
        // ---------------- Utilisation 2 -------------------------
        try {
            str = motuRequest.executeAsString();
            System.out.println(str);

            while ((statusModeResponse = MotuRequest.getStatusInProgress(str)) != null) {
                System.out.print("request is in progress and msg is : ");
                System.out.println(statusModeResponse.getMsg());
                Thread.sleep(2000);
            }
            statusModeResponse = MotuRequest.getStatusError(str);
            if (statusModeResponse != null) {
                System.out.print("request error is : ");
                System.out.println(statusModeResponse.getMsg());
            }

            statusModeResponse = MotuRequest.getStatusDone(str);
            if (statusModeResponse != null) {
                System.out.print("request is done and extracted file is : ");
                System.out.println(statusModeResponse.getMsg());
            }
        } catch (Exception e) {
            System.out.print("ERROR in TestGetTimeCoverage : ");
            System.out.println(e.getMessage());
        }

        // ---------------- Utilisation 3 -------------------------
        try {
            str = motuRequest.executeAsString();
            System.out.println(str);

            while (motuRequest.getStatusDoneOrError(str) == null) {
                Thread.sleep(2000);
            }
            statusModeResponse = MotuRequest.getStatusError(str);
            if (statusModeResponse != null) {
                System.out.print("request error is : ");
                System.out.println(statusModeResponse.getMsg());
            }

            statusModeResponse = MotuRequest.getStatusDone(str);
            if (statusModeResponse != null) {
                System.out.print("request is done and extracted file is : ");
                System.out.println(statusModeResponse.getMsg());
            }
        } catch (Exception e) {
            System.out.print("ERROR in TestGetTimeCoverage : ");
            System.out.println(e.getMessage());
        }

    }

    public static void testModeStatusAsXML() {
        MotuRequest motuRequest = new MotuRequest();

        //String servletUrl = "http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso";
        //String servletUrl = "http://localhost:8080/atoll-motu-servlet/Motu";
        String servletUrl = "http://atoll-dev.cls.fr:30080/atoll-motuservlet/Aviso";
        //String servletUrl = "http://evetkar:p61mlkm@atoll-motu.mercator-ocean.fr/";

        motuRequest.setServletUrl(servletUrl);

        MotuRequestParameters motuRequestParameters = new MotuRequestParameters();


        // motuRequest.setServletUrl("http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso");
        // motuRequest.setServletUrl("http://localhost:8080/atoll-motu-servlet/Aviso");

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOGIN, "toto");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PWD, "pass");
        //motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ANONYMOUS, "true");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ANONYMOUS, "false");

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_PRODUCT_DOWNLOAD);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_SERVICE, "Aviso");
//        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_SERVICE, "Mercator");
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_DATA,
        // "http://opendap.aviso.oceanobs.com/thredds/dodsC/duacs_global_nrt_madt_merged_h");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PRODUCT, "duacs_global_nrt_madt_merged_h");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_VARIABLE, "Grid_0001");
//        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PRODUCT, "mercatorPsy2v2R1v_nat_mean_best_estimate");
//        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_VARIABLE, "temperature");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_START_DATE, "2007-08-19");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_END_DATE, "2007-08-19");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LAT, "-40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LON, "-40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LAT, "40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LON, "40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LON, "40.0");
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE,
        // MotuRequestParameters.PARAM_MODE_CONSOLE);
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE,
        // MotuRequestParameters.PARAM_MODE_URL);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MODE, MotuRequestParameters.PARAM_MODE_STATUS);

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PRIORITY, "1");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MAX_POOL_ANONYMOUS, "30");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_MAX_POOL_AUTHENTICATE, "15");
        
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_BATCH, "false");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_FORWARDED_FOR, "10.1.253.25");
        //motuRequestParameters.setParameter(MotuRequestParameters.PARAM_FORWARDED_FOR, "CLS-EARITH.pc.cls.fr");
        
        //motuRequestParameters.setParameter(MotuRequestParameters.PARAM_BATCH, "true");

        motuRequest.setMotuRequestParameters(motuRequestParameters);

        // InputStream in = motuRequest.execute();
        // OutputStream out = new FileOutputStream("c:\\temp\\data.nc");
        // int c;
        // int nb = 0;
        // while ((c = in.read()) >= 0) {
        // out.write(c);
        // ++nb;
        // }
        // System.out.println(nb);
        //
        //        
        // in.close();
        // out.close();

        // ---------------- Utilisation 1 -------------------------
        //String str;
        StatusModeResponse statusModeResponse = null;
        MotuRequest motuRequestGetStatus = new MotuRequest(servletUrl);

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

            while (MotuRequest.isStatusPendingOrInProgress(statusModeResponse)) {
                System.out.print("request is pending or in progress and msg is : ");
                System.out.println(statusModeResponse.getMsg());
                Thread.sleep(1000);
                statusModeResponse = motuRequestGetStatus.executeActionGetStatusParams(requestId);
            }

            if (MotuRequest.isStatusError(statusModeResponse)) {
                System.out.print("request error is : ");
                System.out.println(statusModeResponse.getMsg());
            }
            if (MotuRequest.isStatusDone(statusModeResponse)) {
                System.out.print("request is done and extracted file is : ");
                System.out.println(statusModeResponse.getMsg());
                System.out.print("file length is : ");
                if (statusModeResponse.getSize() != null) {
                    System.out.println(statusModeResponse.getSize());
                }else {
                    System.out.print("null");                    
                }
                System.out.print("file lastModified is : ");
                if (statusModeResponse.getDateProc() != null) {
                    XMLGregorianCalendar lastModified = statusModeResponse.getDateProc().normalize();
                    System.out.println(lastModified.toString());
                }else {
                    System.out.print("null");                    
                }
            }
        } catch (Exception e) {
            System.out.print("ERROR in testModeStatusAsXML : ");
            System.out.println(e.getMessage());
        }
        // ---------------- Utilisation 2 -------------------------
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

            while (!(MotuRequest.isStatusDoneOrError(statusModeResponse))) {
                System.out.print("request is neither done, neither on error and msg is : ");
                System.out.println(statusModeResponse.getMsg());
                Thread.sleep(1000);
                statusModeResponse = motuRequestGetStatus.executeActionGetStatusParams(requestId);
            }

            if (MotuRequest.isStatusError(statusModeResponse)) {
                System.out.print("request error is : ");
                System.out.println(statusModeResponse.getMsg());
            }
            if (MotuRequest.isStatusDone(statusModeResponse)) {
                System.out.print("request is done and extracted file is : ");
                System.out.println(statusModeResponse.getMsg());
                System.out.print("file length is : ");
                if (statusModeResponse.getSize() != null) {
                    System.out.println(statusModeResponse.getSize());
                }else {
                    System.out.print("null");                    
                }
                System.out.print("file lastModified is : ");
                if (statusModeResponse.getDateProc() != null) {
                    XMLGregorianCalendar lastModified = statusModeResponse.getDateProc().normalize();
                    System.out.println(lastModified.toString());
                }else {
                    System.out.print("null");                    
                }
            }

        } catch (Exception e) {
            System.out.print("ERROR in testModeStatusAsXML : ");
            System.out.println(e.getMessage());
        }

    }

    public static void testGetTimeCoverage() {
        MotuRequest motuRequest = new MotuRequest();

        motuRequest.setServletUrl("http://atoll-dev.cls.fr:30080/atoll-motuservlet/Aviso");
        //motuRequest.setServletUrl("http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso");
        //motuRequest.setServletUrl("http://localhost:8080/atoll-motu-servlet/Aviso");

        MotuRequestParameters motuRequestParameters = new MotuRequestParameters();

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOGIN, "toto");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PWD, "pass");

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_GET_TIME_COVERAGE);
        //motuRequestParameters.setParameter(MotuRequestParameters.PARAM_SERVICE, "AvisoNRT");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_SERVICE, "Aviso");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PRODUCT, "duacs_global_nrt_madt_merged_h");
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_DATA,
        // "http://opendap.aviso.oceanobs.com/thredds/dodsC/duacs_global_nrt_madt_merged_h");

        motuRequest.setMotuRequestParameters(motuRequestParameters);
        TimeCoverage timeCoverage = null;
        try {
            Calendar cal = Calendar.getInstance();
            long start1 = cal.getTimeInMillis();
            timeCoverage = (TimeCoverage) motuRequest.executeAsXML();
            cal = Calendar.getInstance();
            long stop1 = cal.getTimeInMillis();
            System.out.print("testGetTimeCoverage executed in : ");
            System.out.print((stop1 - start1));
            System.out.println(" milliseconds : ");

            if (timeCoverage != null) {
                XMLGregorianCalendar start = timeCoverage.getStart().normalize();
                XMLGregorianCalendar end = timeCoverage.getEnd().normalize();
                System.out.println("request is done and time coverage is : ");
                System.out.print("Start: ");
                System.out.println(start.toString());
                System.out.print("End: ");
                System.out.println(end.toString());
            } else {
                System.out.println("request is done and no time coverage found");

            }

        } catch (MotuRequestException e) {
            System.out.print("ERROR in TestGetTimeCoverage : ");
            System.out.println(e.getMessage());
        }

    }

    public static void testGetSize() {
        MotuRequest motuRequest = new MotuRequest();
        //motuRequest.setServletUrl("http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso");
        motuRequest.setServletUrl("http://atoll-dev.cls.fr:30080/atoll-motuservlet/Aviso");
        //motuRequest.setServletUrl("http://localhost:8080/atoll-motu-servlet/Aviso");

        MotuRequestParameters motuRequestParameters = new MotuRequestParameters();

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOGIN, "toto");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PWD, "pass");

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_GET_SIZE);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_SERVICE, "Aviso");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_PRODUCT, "duacs_global_nrt_madt_merged_h");
        // motuRequestParameters.setParameter(MotuRequestParameters.PARAM_DATA,
        // "http://opendap.aviso.oceanobs.com/thredds/dodsC/duacs_global_nrt_madt_merged_h");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_VARIABLE, "Grid_0001");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_START_DATE, "2007-08-19");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_END_DATE, "2007-08-19");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LAT, "-40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_LOW_LON, "-40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LAT, "40.0");
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_HIGH_LON, "40.0");


        motuRequest.setMotuRequestParameters(motuRequestParameters);
        RequestSize requestSize = null;
        try {
            Calendar cal = Calendar.getInstance();
            long start = cal.getTimeInMillis();
            requestSize = (RequestSize) motuRequest.executeAsXML();
            cal = Calendar.getInstance();
            long stop = cal.getTimeInMillis();
            System.out.print("TestGetSize executed in : ");
            System.out.print((stop - start));
            System.out.println(" milliseconds : ");

            if (requestSize != null) {
                double size = requestSize.getSize().doubleValue();
                System.out.print("request is done and size is : ");
                System.out.print(size);
                System.out.println(" bytes");
                System.out.print("Code is: ");
                System.out.println(requestSize.getCode().toString());
                System.out.print("Msg is: ");
                System.out.println(requestSize.getMsg());
            } else {
                System.out.println("request is done and no size found");

            }

        } catch (Exception e) {
            System.out.print("ERROR in TestGetSize : ");
            System.out.println(e.getMessage());
        }

    }

    public static void testSynchronized() {

        MotuRequestMainTest test = new MotuRequestMainTest();
        MotuRequestMainTest.Client[] c = { test.new Client("Client1"), test.new Client("Client2"), };
        for (int i = 0; i < c.length; i++) {
            c[i].start();
        }
        MotuRequestMainTest.Client2[] c2 = { test.new Client2("ClientA"), test.new Client2("ClientB") };
        for (int i = 0; i < c2.length; i++) {
            c2[i].start();
        }
        MotuRequestMainTest.Client3[] c3 = { test.new Client3("ClientM1"), test.new Client3("ClientM2") };
        for (int i = 0; i < c3.length; i++) {
            c3[i].start();
        }

    }

    public static void testDeleteFile() {
        // String servletUrl = "http://aviso-motu.cls.fr:8380/atoll-motuservlet/Aviso";
        String servletUrl = "http://localhost:8080/atoll-motu-servlet/Aviso";

        MotuRequest motuRequest = new MotuRequest(servletUrl);

       
        try {

            List<String> filesToDelete = new ArrayList<String>();
            filesToDelete.add("http://localhost:8080/motu-file-extract/test.nc");
            filesToDelete.add("http://localhost:8080/motu-file-extract/test2.nc");

            StatusModeResponse statusModeResponse = motuRequest.executeActionDeleteFile(filesToDelete);
            // ou
           //  String fileToDelete = "http://localhost:8080/motu-file-extract/test.nc";
           //  StatusModeResponse statusModeResponse2 = motuRequest.executeActionDeleteFile(fileToDelete);
            
            
            if (statusModeResponse == null) {
                System.out.println("ERROR - no status found");
                return;
            }

            if (MotuRequest.isStatusError(statusModeResponse)) {
                System.out.print("request error is : ");
                System.out.println(statusModeResponse.getCode());
                System.out.println(statusModeResponse.getMsg());
            }
            if (MotuRequest.isStatusDone(statusModeResponse)) {
                System.out.print("request is done : ");
                System.out.println(statusModeResponse.getCode());
                System.out.println(statusModeResponse.getMsg());
            }

        } catch (Exception e) {
            System.out.print("ERROR in testDeleteFile : ");
            System.out.println(e.getMessage());
        }

    }

    public class Client extends Thread {
        public Client(String name) {
            this.name = name;
        }

        String name;

        public void run() {

            System.out.print("Start Client ");
            System.out.println(name);

            MotuRequestMainTest.testGetSize();

            System.out.print("End Client ");
            System.out.println(name);

        }
    }
    public class Client2 extends Thread {
        public Client2(String name) {
            this.name = name;
        }

        String name;

        public void run() {
            System.out.print("Start Client2 ");
            System.out.println(name);

            MotuRequestMainTest.testGetTimeCoverage();

            System.out.print("End Client2 ");
            System.out.println(name);
        }
    }
    public class Client3 extends Thread {
        public Client3(String name) {
            this.name = name;
        }

        String name;

        public void run() {
            System.out.print("Start Client3 ");
            System.out.println(name);

            MotuRequestMainTest.testModeStatusAsXMLFile();

            System.out.print("End Client3 ");
            System.out.println(name);
        }
    }
    
//    public static void testQueueServer() {
//
//        MotuRequest motuRequest = new MotuRequest();
//        Organizer organizer = null;
//        try {
//            organizer = new Organizer();
//            Collection<ServiceData> services = organizer.servicesValues();
//
//            for (ServiceData service : services) {
//                if (!service.getName().equalsIgnoreCase("catsat")) {
//                    continue;
//                }
//                CatalogData catalog = service.getCatalog();
//                Collection<Product> products = catalog.productsValues();
//
//                for (Product product : products) {
//                    String productId = product.getProductId();
//                    product.loadOpendapGlobalMetaData();
//                    ProductMetaData productMetaData = product.getProductMetaData();
//                }
//
//            }
//
//        } catch (MotuException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//    }
}
