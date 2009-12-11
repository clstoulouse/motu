/**
 * 
 */
package fr.cls.atoll.motu.library.intfce;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBElement;

import org.apache.commons.vfs.impl.DecoratedFileObject;
import org.apache.log4j.Logger;

import ucar.ma2.StructureData;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.grid.GridCoordSys;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.TrajectoryFeatureCollection;
import ucar.nc2.ft.point.writer.CFPointObWriter;
import ucar.nc2.ft.point.writer.WriterCFPointObsDataset;
import ucar.unidata.geoloc.EarthLocation;
import fr.cls.atoll.motu.library.configuration.ConfigService;
import fr.cls.atoll.motu.library.configuration.MotuConfig;
import fr.cls.atoll.motu.library.configuration.QueueServerType;
import fr.cls.atoll.motu.library.configuration.QueueType;
import fr.cls.atoll.motu.library.data.DataFile;
import fr.cls.atoll.motu.library.data.Product;
import fr.cls.atoll.motu.library.data.ServiceData;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.inventory.CatalogOLA;
import fr.cls.atoll.motu.library.inventory.GeospatialCoverage;
import fr.cls.atoll.motu.library.inventory.Inventory;
import fr.cls.atoll.motu.library.inventory.Resource;
import fr.cls.atoll.motu.library.inventory.ResourceOLA;
import fr.cls.atoll.motu.library.inventory.TimePeriod;
import fr.cls.atoll.motu.library.metadata.ProductMetaData;
import fr.cls.atoll.motu.library.netcdf.NetCdfReader;
import fr.cls.atoll.motu.library.sdtnameequiv.StandardName;
import fr.cls.atoll.motu.library.sdtnameequiv.StandardNames;
import fr.cls.atoll.motu.library.threadpools.TestTheadPools;

/**
 * @author $Author: dearith $
 * @version $Revision: 1.23 $ - $Date: 2009-12-11 15:13:07 $
 * 
 */
public class TestIntfce {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(TestIntfce.class);
    private static final Logger LOGQUEUE = Logger.getLogger("atoll.motu.queueserver");

    /**
     * @param args
     */
    public static void main(String[] args) {
               
//        System.out.println(Organizer.getDatasetIdFromURI("//http://atoll.cls.fr/2009/resource/metadata/environmental-resource#dataset-identifiant"));            
//        System.out.println(Organizer.getDatasetIdFromURI("//http://atoll.cls.fr/2009/resource/metadata/environmental-resource#identifiant"));            
//        System.out.println(Organizer.getDatasetIdFromURI("bidon"));            
//
//        System.out.println(Organizer.getVariableIdFromURI("bidon"));            
//        System.out.println(Organizer.getVariableIdFromURI("#bidon"));            
//        System.out.println(Organizer.getVariableIdFromURI("sfddfjjjsd#bidon"));            

        // if (LOGQUEUE.isInfoEnabled()) {
        // LOGQUEUE.info("main(String[]) - xxx jentering");
        // }

        
//        try {
//            VFSManager.createLocalFile("c:/tgaga/test.txt");
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        
        
//        URI uri = null;
//        try {
//            uri = new URI("sftp://atoll:atoll@catsat-data1.cls.fr/data/atoll/applications/archive/temperature/infrared_timestamp/med/nrt_med_infrared_sst_timestamp_FTP_TEST.xml");
//        } catch (URISyntaxException e) {
//            throw new MotuException(String.format("Data extraction - Invalid URI '%s'", uriFile), e);
//        }
//        System.out.println(uri.getFragment());
        
           
//        String temp = "sdfsdf:tt!sdf.sdf.txt";
//        System.out.println(temp.replaceAll("[^\\p{ASCII}]", ""));
//        System.out.println(temp.replaceAll("[\\W&&[^\\.]]", "-"));
//        Zip.unAccent(temp);
        
//
//         try {
//         String uriStr =         "sftp://atoll:atoll@catsat-data1.cls.fr/data/atoll/applications/archive/temperature/infrared_timestamp/med/nrt_med_infrared_sst_timestamp_FTP_TEST.xml";
//         //String uriStr = "atoll:service:ftp-catsat-ftp:ftp";
//         //String uriStr = "c:/tempVFS/nrt_med_infrared_sst_timestamp_FTP_20090516.xml";
//                    
//         URI uri = new URI(uriStr);
//         if (uri.isOpaque()) {
//         System.out.println("This is an opaque URI.");
//         System.out.println("The scheme is " + uri.getScheme( ));
//         System.out.println("The scheme specific part is "
//         + uri.getSchemeSpecificPart( ));
//         System.out.println("The fragment ID is " + uri.getFragment( ));
//        
//         }
//         File ff = new File(uriStr);
//         System.out.println(ff.getName().endsWith(".xml"));
//                    
//         System.out.println(uri.getScheme());
//         System.out.println(uri.getUserInfo());
//         System.out.println(uri.getHost());
//         System.out.println(uri.getPort());
//         System.out.println(uri.getAuthority());
//         System.out.println("Path");
//         System.out.println(uri.getPath());
//         System.out.println(uri.getQuery());
//         System.out.println(uri.getSchemeSpecificPart());
//         System.out.println(uri);
//         
//         System.out.println("Fragment");
//         System.out.println(uri.getFragment());
//       
//                    
//         System.out.println(uri.normalize());
//
//         File srcFilePath = new File(uri.getPath());
//         
//         System.out.println("srcFilePath");
//         System.out.println(srcFilePath.getParent());
//         System.out.println("srcFileName");
//         System.out.println(srcFilePath.getName());
//
//         
//         } catch (URISyntaxException e) {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//         }

        // DecimalMeasure x = DecimalMeasure.valueOf("10052");
        // System.out.println(x);

        // Period p = JodaPeriodAdapter.PERIOD_FORMATER.parsePeriod("P10D");
        // System.out.println(p.getDays());

        // System.out.println (Organizer.Format.valueOf("NETCDFRRRRRR"));
        // System.out.println (Organizer.Format.fromValue(2));
        // testFraction();

        // Mandatory if there is a proxy to access Opendap server.
        // System.setProperty("http.proxyHost", "proxy.cls.fr"); // adresse IP
        // System.setProperty("http.proxyPort", "8080");

        /*
         * System.setProperty("http.proxyHost", "proxy.cls.fr"); // adresse IP
         * System.setProperty("http.proxyPort", "8080"); System.setProperty("socksProxyHost", "proxy.cls.fr");
         * System.setProperty("socksProxyPort", "1080");
         */

        // Authenticator.setDefault(new MyAuthenticator());
        // try {
        // DetectProxy();
        // } catch (Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // try {
        //            
        // String extractionPath = Organizer.getMotuConfigInstance().getExtractionPath();
        // String downloadHttpUrl = Organizer.getMotuConfigInstance().getDownloadHttpUrl();
        //
        // String fileName =
        // "http://localhost:8080/motu-file-extract/duacs_global_nrt_madt_merged_h_1204111211733.nc";
        // String newFileName = fileName.replace(downloadHttpUrl, extractionPath);
        //            
        // File file = new File(newFileName);
        // LOG.debug(file.getPath());
        // LOG.debug(file.getName());
        // LOG.debug(file.delete());
        // } catch (Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // Exception e = new MotuException("tetsd");
        //        
        // if (e instanceof MotuExceptionBase) {
        // System.out.println("YES");
        // }
        // try {
        // URL url = new URL("http://user:password@localhost:8080/atoll-motu-servlet/Aviso");
        // System.out.println(url);
        // } catch (MalformedURLException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // Locale locale = Locale.getDefault();
        // System.out.println(DecimalFormatSymbols.getInstance().getDecimalSeparator());
        // System.out.println(locale.toString());
        // Locale.setDefault(Locale.US);
        // System.out.println(DecimalFormatSymbols.getInstance().getDecimalSeparator());
        // //testReadFileInJar();
        // String x = "-19,123";
        // double y = Double.parseDouble(x);
        // System.out.println(Double.toString(y));
        // System.out.println(Double.doubleToLongBits(25.5d));
        // System.out.println(ExtractCriteriaLatLon.getMinOrMaxLon(360, 0, false));
        // System.out.println(LatLonPointImpl.lonNormal(-50, 0));
        // System.out.println(LatLonPointImpl.lonNormal(-50, 180));
        // System.out.println(LatLonPointImpl.lonNormal(-50, 360));
        // try {
        // NetCdfReader.unconvertLon("180W");
        // NetCdfReader.unconvertLon("180 W");
        // NetCdfReader.unconvertLon("360 W");
        // NetCdfReader.unconvertLon("180W", false);
        // NetCdfReader.unconvertLon("180 W", false);
        // NetCdfReader.unconvertLon("360 W", false);
        // NetCdfReader.unconvertLon("360", false);
        // NetCdfReader.unconvertLon("360");
        // } catch (MotuInvalidLongitudeException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // ExtractCriteriaLatLon l1 = new ExtractCriteriaLatLon(-90, -180, 90, 180);
        // ExtractCriteriaLatLon l2 = new ExtractCriteriaLatLon(-90, 360, 90, 720);
        // ExtractCriteriaLatLon l1 = null;
        // ExtractCriteriaLatLon l2 = null;
        // try {
        // l1 = new ExtractCriteriaLatLon("-90", "-180", "90", "180");
        // l2 = new ExtractCriteriaLatLon("-90", "360", "90", "720");
        // } catch (MotuInvalidLatitudeException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (MotuInvalidLongitudeException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // LatLonRect r1 = l1.getLatLonRect();
        // LatLonRect r2 = l2.getLatLonRect();
        //        
        // System.out.print("r1 contains in r2:");
        // System.out.println(r1.containedIn(r2));
        // System.out.print("r2 contains in r1:");
        // System.out.println(r1.containedIn(r1));
        //        
        // System.out.println (LatLonPointImpl.lonNormal360(-175));
        // System.out.println (LatLonPointImpl.lonNormal(235));
        // int[] in = new int[] {0,1,20,131};
        // int[] dim = new int[] {1,2,21,132};
        // int[] in = new int[] {131};
        // int[] dim = new int[] {132};
        // int[] out = NetCdfWriter.getNextOrigin(in, dim);
        //        
        // for (int i = 0 ; i < in.length ; i++) {
        // System.out.println(String.format("out[%d]=%d",i, out[i]));
        //            
        // }
        // MAMath.MinMax minMax = NetCdfReader.getMinMaxLonNormal(r1, r2, r1Values, r2Values)
        // listServices();
        // catalogInformation();
        // try {
        // ServiceData.Language test = ServiceData.Language.valueOf("ee");
        // } catch (RuntimeException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // testLoadMotuConfig();
        // testgetMotuConfigSchema();
        // productInformation();
        // productInformationFromLocationData();
        // productExtractDataMersea();
        // productDownloadInfo();
        // productExtractDataHTMLAviso();
        // productExtractDataAviso();
        // productExtractDataAvisofromProductId();
        // productInformationFromLocationData();
        // productExtractDataAviso2();
        //productExtractDataMercator();
        // productExtractDataHTMLMercator();
        // productExtractDataCls();
         productExtractDiversity();
        // testProjection();
        // testJason2Local();
        // testProjection2();
        // testReadFromListe();
        // testFeatures();
        // try {
        // String date = NetCdfReader.getDateAsGMTString(0, "days since 0000-01-01 00:00");
        // } catch (MotuException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // testLoadStdNameEquiv();
        //testGetAmountDataSize();
        // testSynchronized();
        //productExtractDataCatsat();
        // productExtractDataAvisofromExtractionParameters();
        // productExtractDataMerseaFromHttp();
        //testLoadInventoryOLA();
        // testLoadCatalogOLA();
        //productInformationFromInventory();
        //productExtractDataFromInventory();

    }

    public static void listServices() {

        try {
            Organizer organizer = new Organizer();
            FileWriter writer = new FileWriter("resultListCatalog.html");
            organizer.getAvailableServices(writer, Organizer.Format.HTML);
            writer.flush();
            writer.close();
        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void catalogInformation() {

        try {
            // String serviceName = "aviso_dt";
            // String serviceName = "Mercator";
            String serviceName = "MercatorIBI";
            // String serviceName = "Catsat";
            // String serviceName = "AvisoNRT";
            // String serviceName = "aviso_dt";
            FileWriter writer = new FileWriter("./target/resultCatalogInfo.html");
            Organizer organizer = new Organizer();
            // organizer.setCurrentLanguage("uk");
            organizer.getCatalogInformation(serviceName, writer, Organizer.Format.HTML);
            writer.flush();
            writer.close();
        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static Product productInformationFromInventory() {
        // String xmlUri = "C:/tempVFS/nrt_med_infrared_sst_timestamp_FTP_20090516.xml";
        // String service = "atoll:service:ftp-catsat:ftp";
        //
        // return productInformationFromLocationData(service, xmlUri);

        String service = "http://atoll.cls.fr/2009/resource/individual/atoll#atoll:service:ftp-catsat:ftp";
        String productId = "http://atoll.cls.fr/2009/resource/individual/atoll#datafile-nrt-med-infrared-sst-timestamp";
        Product product = null;
        try {
            Organizer organizer = new Organizer();
            product = organizer.getProductInformation(service, productId, null, null);
            System.out.println(product.getProductId());

            System.out.println(product.getLocationData());
            System.out.println(product.getLocationMetaData());
            System.out.println(product.getProductMetaData().getTimeCoverage().toString());
            System.out.println(product.getProductMetaData().getParameterMetaDatas().toString());

            List<DataFile> files = product.getDataFiles();
            if (files != null) {
                for (DataFile file : files) {
                    System.out.print(file.getPath());
                    System.out.print(" ");
                    System.out.print(file.getName());
                    System.out.print(" ");
                    System.out.print(file.getStartCoverageDate());
                    System.out.print(" ");
                    System.out.print(file.getEndCoverageDate());
                    System.out.print(" ");
                    System.out.println("");

                }
            }
        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return product;

    }

    public static Product productInformationFromLocationData() {
        // String productId = "duacs_global_nrt_madt_merged_h";
        // String locationData = "http://opendap-nrt.aviso.oceanobs.com/thredds/dodsC/" + productId;
        // String productId = "MERSEA_CLS_DEMO_COLOR-D20070423-Z-180.0_180.0_-70.0_70.0-S0.1.nc";
        // String productId = "Test_Galapagos_20070901_ssh.nc";
        // String productId = "mercatorPsy3v2R1v_glo_mean_best_estimate_1182752515507.nc";
        // String locationData = "C:/Java/dev/" + productId;
        // String locationData = "J:/dev/" + productId;
        // String productId = "dt_upd_med_e1_sla_vfec";
        // String locationData = "http://opendap-dt.aviso.oceanobs.com/thredds/dodsC/" + productId;
        // String productId = "dt_upd_med_tp_sla_vfec_19920925_19920930_20050914.nc";
        // String locationData = "C:/BratData/netCDF/" + productId;
        String productId = "extlink_source.h5";
        String locationData = "C:/Documents and Settings/dearith/" + productId;

        try {
            NetcdfDataset.acquireDataset(locationData, null);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return productInformationFromLocationData(locationData);
    }

    public static Product productInformationFromLocationData(String service, String locationData) {
        Product product = null;
        try {
            Organizer organizer = new Organizer();
            product = organizer.getProductInformation(service, locationData);
            System.out.println(product.getProductId());
            try {
                System.out.println(product.getProductMetaData().getLatAxisMinValueAsString());
                System.out.println(product.getProductMetaData().getLatAxisMaxValueAsString());
                System.out.println(product.getProductMetaData().getLonAxisMinValueAsString());
                System.out.println(product.getProductMetaData().getLonAxisMaxValueAsString());
                System.out.println(product.getProductMetaData().getTimeAxisMinValueAsString());
                System.out.println(product.getProductMetaData().getTimeAxisMaxValueAsString());
                System.out.println(product.hasGeoXAxisWithLonEquivalence());
                System.out.println(product.hasGeoYAxisWithLatEquivalence());

                System.out.println(product.getProductMetaData().getGeoXAxisMinValueAsLonString(product));
                System.out.println(product.getProductMetaData().getGeoXAxisMaxValueAsLonString(product));

                System.out.println(product.getProductMetaData().getGeoYAxisMinValueAsLatString(product));
                System.out.println(product.getProductMetaData().getGeoYAxisMaxValueAsLatString(product));
            } catch (Exception e) {
                // Do nothing
            }

            System.out.println(product.getProductMetaData().getTimeCoverage().toString());
            System.out.println(product.getProductMetaData().getParameterMetaDatas().toString());

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return product;
    }

    public static Product productInformationFromLocationData(String locationData) {
        Product product = null;
        try {
            Organizer organizer = new Organizer();
            product = organizer.getProductInformation(locationData);
            System.out.println(product.getProductId());

            try {
                System.out.println(product.getProductMetaData().getLatAxisMinValueAsString());
                System.out.println(product.getProductMetaData().getLatAxisMaxValueAsString());
                System.out.println(product.getProductMetaData().getLonAxisMinValueAsString());
                System.out.println(product.getProductMetaData().getLonAxisMaxValueAsString());
                System.out.println(product.getProductMetaData().getTimeAxisMinValueAsString());
                System.out.println(product.getProductMetaData().getTimeAxisMaxValueAsString());
                System.out.println(product.hasGeoXAxisWithLonEquivalence());
                System.out.println(product.hasGeoYAxisWithLatEquivalence());

                System.out.println(product.getProductMetaData().getGeoXAxisMinValueAsLonString(product));
                System.out.println(product.getProductMetaData().getGeoXAxisMaxValueAsLonString(product));

                System.out.println(product.getProductMetaData().getGeoYAxisMinValueAsLatString(product));
                System.out.println(product.getProductMetaData().getGeoYAxisMaxValueAsLatString(product));
            } catch (Exception e) {
                // Do nothing
            }

            System.out.println(product.getProductMetaData().getTimeCoverage().toString());
            System.out.println(product.getProductMetaData().getParameterMetaDatas().toString());

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return product;
    }

    public static void productInformation() {

        try {
            // String serviceName = "avisoNRT";
            // String productId = "duacs_regional-gomex_nrt_g2_slaext";
            // String serviceName = "dev";
            // String productId = "res_oer_g2";
            String serviceName = "mercator";
            // String serviceName = "cls";
            // String serviceName = "AvisoDT";
            String productId = "mercatorPsy3v2_nat_mean_best_estimate";
            // String productId = "mercatorPsy3v2R1v_med_levitus_1998";
            // String productId = "global_sst";
            // String productId = "dt_ref_global_merged_madt_h";

            FileWriter writer = new FileWriter("./target/resultProductInfo.html");
            Organizer organizer = new Organizer();
            organizer.setCurrentLanguage("uk");
            organizer.getProductInformation(serviceName, productId, writer, Organizer.Format.HTML);
            // Product product = organizer.getProductInformation(serviceName, productId);
            writer.flush();
            writer.close();
        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void productDownloadInfo() {

        try {
            String serviceName = "MercatorIBI";
            // String serviceName = "avisoNRT";
            // String productId = "duacs_global_nrt_madt_merged_h";
            String productId = "mercatorPsy2v3_ibi_mean_best_estimate";
            // String serviceName = "mercator";
            // String serviceName = "mercator";
            // String productId = "mercatorPsy3v1R1v_nat_mean_best_estimate";
            // String productId = "mercatorPsy3v1R1v_arc_mean_best_estimate";
            FileWriter writer = new FileWriter("./target/resultProductDownloadInfo.html");
            Organizer organizer = new Organizer();
            // organizer.setCurrentLanguage("uk");
            organizer.getProductDownloadInfo(serviceName, productId, writer, Organizer.Format.HTML);
            writer.flush();
            writer.close();
        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void productExtractDataFromInventory() {
        String prefix = "http://atoll.cls.fr/2009/resource/individual/atoll#";
        String productId = prefix + "datafile-nrt-med-infrared-sst-timestamp";
        String service = prefix +  "ftp-catsat:ftp";

        // add temporal criteria
        // first element is start date
        // second element is end date (optional)
        // if only start date is set, end date equals start date
        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("2007-04-06");
        listTemporalCoverage.add("2007-04-14");

        ExtractionParameters extractionParameters = new ExtractionParameters(
                service,
                null,
                null,
                listTemporalCoverage,
                null,
                null,
                productId,
                Organizer.Format.NETCDF,
                //Organizer.Format.URL,
                null,
                null,
                "dearith",
                true);

        Product product = null;

        try {
            Organizer organizer = new Organizer();

            product = organizer.extractData(extractionParameters);

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void productExtractDataAviso() {

        // String productId = "duacs_global_nrt_madt_merged_h";
        String productId = "dt_ref_global_merged_madt_h";
        String serviceName = "aviso";

        // String locationData = "http://opendap-nrt.aviso.oceanobs.com/thredds/dodsC/" + productId;
        String locationData = "http://opendap.aviso.oceanobs.com/thredds/dodsC/" + productId;

        List<String> listVar = new ArrayList<String>();
        // add variable to extract
        // listVar.add("Grid_0001");
        // listVar.add("surface_northward_geostrophic_sea_water_velocity");
        listVar.add("sea_surface_height_above_geoid");

        // add temporal criteria
        // first element is start date
        // second element is end date (optional)
        // if only start date is set, end date equals start date
        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("2007-09-24");
        listTemporalCoverage.add("2007-09-24");

        // add Lat/Lon criteria
        // first element is low latitude
        // second element is low longitude
        // third element is high latitude
        // fourth element is high longitude
        List<String> listLatLonCoverage = new ArrayList<String>();
        // listLatLonCoverage.add("46");
        // listLatLonCoverage.add("-20");
        // listLatLonCoverage.add("30");
        // listLatLonCoverage.add("-10");

        // add depth (Z) criteria
        // first element is low depth
        // second element is high depth (optional)
        // if only low depth is set, high depth equals low depth value
        List<String> listDepthCoverage = null;
        // listDepthCoverage = new ArrayList<String>();
        // listDepthCoverage.add("0");
        // listDepthCoverage.add("125");

        Product product = null;

        try {
            Organizer organizer = new Organizer();

            product = organizer.extractData(locationData,
                                            listVar,
                                            listTemporalCoverage,
                                            listLatLonCoverage,
                                            listDepthCoverage,
                                            null,
                                            Organizer.Format.NETCDF);

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void productExtractDataAviso2() {

        String locationData = "C:\\BratData\\out\\res_oer_g2_20129_20148.nc";
        List<String> listVar = new ArrayList<String>();
        // add variable to extract
        listVar.add("SLA");

        // add temporal criteria
        // first element is start date
        // second element is end date (optional)
        // if only start date is set, end date equals start date
        List<String> listTemporalCoverage = null;
        // listTemporalCoverage = new ArrayList<String>();
        // listTemporalCoverage.add("2001-08-22");
        // listTemporalCoverage.add("2001-08-30");

        // add Lat/Lon criteria
        // first element is low latitude
        // second element is low longitude
        // third element is high latitude
        // fourth element is high longitude
        List<String> listLatLonCoverage = new ArrayList<String>();
        listLatLonCoverage.add("46");
        listLatLonCoverage.add("-20");
        listLatLonCoverage.add("30");
        listLatLonCoverage.add("-10");

        // add depth (Z) criteria
        // first element is low depth
        // second element is high depth (optional)
        // if only low depth is set, high depth equals low depth value
        List<String> listDepthCoverage = null;
        // listDepthCoverage = new ArrayList<String>();
        // listDepthCoverage.add("0");
        // listDepthCoverage.add("125");

        Product product = null;

        try {
            Organizer organizer = new Organizer();

            product = organizer.extractData(locationData,
                                            listVar,
                                            listTemporalCoverage,
                                            listLatLonCoverage,
                                            listDepthCoverage,
                                            null,
                                            Organizer.Format.NETCDF);

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void productExtractDataAvisofromExtractionParameters() {
        ExtractionParameters extractionParameters = TestTheadPools.getAvisoRequests().get(0);
        Product product = null;
        FileWriter writer = null;

        try {
            Organizer organizer = new Organizer();

            writer = new FileWriter("./target/resultProductData1.html");

            extractionParameters.setOut(writer);
            extractionParameters.setResponseFormat(Organizer.Format.HTML);
            // extractionParameters.setListVar(null);
            // extractionParameters.setLocationData(null);
            // extractionParameters.setProductId(null);

            product = organizer.extractData(extractionParameters);

            writer.flush();
            writer.close();

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void productExtractDataAvisofromProductId() {
        String productId = "dt_ref_global_merged_madt_h";
        // String locationData = "http://opendap-nrt.aviso.oceanobs.com/thredds/dodsC/" + productId;
        String serviceName = "aviso";

        List<String> listVar = new ArrayList<String>();
        // add variable to extract
        // listVar.add("Grid_0001");
        listVar.add("sea_surface_height_above_geoid ");

        // add temporal criteria
        // first element is start date
        // second element is end date (optional)
        // if only start date is set, end date equals start date
        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("2007-05-23");
        listTemporalCoverage.add("2007-05-23");

        // add Lat/Lon criteria
        // first element is low latitude
        // second element is low longitude
        // third element is high latitude
        // fourth element is high longitude
        List<String> listLatLonCoverage = new ArrayList<String>();
        // listLatLonCoverage.add("46");
        // listLatLonCoverage.add("-20");
        // listLatLonCoverage.add("30");
        // listLatLonCoverage.add("-10");

        // add depth (Z) criteria
        // first element is low depth
        // second element is high depth (optional)
        // if only low depth is set, high depth equals low depth value
        List<String> listDepthCoverage = null;
        // listDepthCoverage = new ArrayList<String>();
        // listDepthCoverage.add("0");
        // listDepthCoverage.add("125");

        Product product = null;
        FileWriter writer = null;

        try {
            Organizer organizer = new Organizer();

            writer = new FileWriter("./target/resultProductData1.html");

            product = organizer.extractData(serviceName,
                                            listVar,
                                            listTemporalCoverage,
                                            listLatLonCoverage,
                                            listDepthCoverage,
                                            productId,
                                            null,
                                            Organizer.Format.NETCDF,
                                            writer,
                                            Organizer.Format.HTML);
            writer.flush();
            writer.close();

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void productExtractDataHTMLAviso() {
        // String serviceName = "aviso_nrt";
        String serviceName = "avisoNRT";

        String productId = "duacs_global_nrt_madt_merged_h";
        String locationData = "http://opendap.aviso.oceanobs.com/thredds/dodsC/" + productId;

        // String locationData = "http://rdp2-jaune.cls.fr:8880/thredds/dodsC/" + productId;

        List<String> listVar = new ArrayList<String>();
        listVar.add("Grid_0001");

        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("2006-09-24");
        listTemporalCoverage.add("2006-09-24");

        List<String> listLatLonCoverage = new ArrayList<String>();
        // listLatLonCoverage.add("46");
        // listLatLonCoverage.add("-20");
        // listLatLonCoverage.add("30");
        // listLatLonCoverage.add("-10");

        List<String> listDepthCoverage = null;
        // listDepthCoverage = new ArrayList<String>();
        // listDepthCoverage.add("0");
        // listDepthCoverage.add("125");

        Product product = null;
        FileWriter writer = null;
        String extractLocationData = null;
        String urlExtractPath = null;
        String extractError = null;

        try {
            Organizer organizer = new Organizer();

            // writer = new FileWriter("./target/resultProductData1.html");
            // product = organizer.extractData(locationData,
            // listVar,
            // listTemporalCoverage,
            // listLatLonCoverage,
            // listDepthCoverage,
            // null,
            // Organizer.Format.NETCDF,
            // writer,
            // Organizer.Format.HTML);
            // writer.flush();
            // writer.close();
            //
            // writer = new FileWriter("./target/resultProductData2.html");
            //
            // product = organizer.extractData(serviceName,
            // locationData,
            // listVar,
            // listTemporalCoverage,
            // listLatLonCoverage,
            // listDepthCoverage,
            // null,
            // Organizer.Format.NETCDF,
            // writer,
            // Organizer.Format.HTML);
            //
            // writer.flush();
            // writer.close();
            //            
            // String extractLocationData = product.getExtractLocationData();
            // String urlExtractPath = product.getDownloadUrlPath();
            // String extractError = product.getLastError();
            // if (extractError.equals("")) {
            // System.out.println(String.format("Product file is stored on the server in %s",
            // extractLocationData));
            // System.out.println(String.format("Product %s can be downloaded with http at %s",
            // product.getProductId(), urlExtractPath));
            // } else {
            // System.out.println(String.format("An error as occured : %s", extractError));
            // }
            writer = new FileWriter("./target/resultProductData3.html");

            product = organizer.extractData(serviceName,
                                            listVar,
                                            listTemporalCoverage,
                                            listLatLonCoverage,
                                            listDepthCoverage,
                                            productId,
                                            null,
                                            Organizer.Format.NETCDF,
                                            writer,
                                            Organizer.Format.HTML);

            writer.flush();
            writer.close();

            extractLocationData = product.getExtractLocationData();
            urlExtractPath = product.getDownloadUrlPath();
            extractError = product.getLastError();
            if (extractError.equals("")) {
                System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
                System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));
            } else {
                System.out.println(String.format("An error as occured : %s", extractError));
            }
        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void productExtractDataHTMLMercator() {
        String serviceName = "mercator";
        String productId = "mercatorPsy3v2R1v_med_levitus_1998";
        String locationData = "http://opendap.mercator-ocean.fr/thredds/dodsC/" + productId;

        List<String> listVar = new ArrayList<String>();
        listVar.add("temperature");

        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("0000-01-01");
        listTemporalCoverage.add("0000-01-01");

        List<String> listLatLonCoverage = new ArrayList<String>();
        // listLatLonCoverage.add("46");
        // listLatLonCoverage.add("-20");
        // listLatLonCoverage.add("30");
        // listLatLonCoverage.add("-10");

        List<String> listDepthCoverage = null;
        // listDepthCoverage = new ArrayList<String>();
        // listDepthCoverage.add("0");
        // listDepthCoverage.add("125");

        Product product = null;
        FileWriter writer = null;

        try {
            Organizer organizer = new Organizer();

            writer = new FileWriter("./target/resultProductData1.html");
            product = organizer.extractData(locationData,
                                            listVar,
                                            listTemporalCoverage,
                                            listLatLonCoverage,
                                            listDepthCoverage,
                                            null,
                                            Organizer.Format.NETCDF,
                                            writer,
                                            Organizer.Format.HTML,
                                            null);
            writer.flush();
            writer.close();

            // writer = new FileWriter("./target/resultProductData2.html");
            //
            // product = organizer.extractData(serviceName,
            // locationData,
            // listVar,
            // listTemporalCoverage,
            // listLatLonCoverage,
            // listDepthCoverage,
            // null,
            // Organizer.Format.NETCDF,
            // writer,
            // Organizer.Format.HTML);
            //
            // writer.flush();
            // writer.close();

            String extractLocationData = product.getExtractLocationData();
            String urlExtractPath = product.getDownloadUrlPath();
            String extractError = product.getLastError();
            if (extractError.equals("")) {
                System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
                System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));
            } else {
                System.out.println(String.format("An error as occured : %s", extractError));
            }
        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void testgetMotuConfigSchema() {
        // URL url = Organizer.getMotuConfigSchema();
        try {
            Organizer.validateMotuConfig();
        } catch (MotuException e) {
            System.out.println("Exception : \n");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testLoadMotuConfig() {

        MotuConfig config = null;

        try {
            config = Organizer.getMotuConfigInstance();
        } catch (MotuException e) {
            System.out.println("Exception : \n");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        for (int i = 0; i < config.getConfigService().size(); i++) {
            ConfigService confServ = config.getConfigService().get(i);
            System.out.println(confServ.getName());
            System.out.println(confServ.getCatalog().getUrlSite());
            System.out.println(confServ.getCatalog().getName());
            System.out.println(confServ.getCatalog().getType());

        }
        QueueServerType queueServer = config.getQueueServerConfig();
        System.out.print("queueServer.getMaxPoolAnonymous()");
        System.out.println(queueServer.getMaxPoolAnonymous());
        System.out.print("queueServer.getMaxPoolAuth()");
        System.out.println(queueServer.getMaxPoolAuth());

        List<QueueType> queues = queueServer.getQueues();

        for (QueueType queue : queues) {

            System.out.println(queue.getDescription());
            System.out.print("queue.getDataThreshold()");
            System.out.println(queue.getDataThreshold());
            System.out.print("queue.getMaxThreads()");
            System.out.println(queue.getMaxThreads());
            System.out.print("queue.getLowPriorityWaiting()");
            System.out.println(queue.getLowPriorityWaiting());
            System.out.print("queue.getMaxPoolSize()");
            System.out.println(queue.getMaxPoolSize());
        }

        System.out.println("End testLoadMotuConfig : \n");
    }

    public static void testLoadCatalogOLA() {

        CatalogOLA catalogOLA = null;
        // String xmlUri = "C:/tempVFS/catalogCatsatFTP.xml";
        String xmlUri = "sftp://atoll:atoll@catsat-data1.cls.fr/home/atoll//atoll-distrib/HOA_Catsat/Interface_ATOLL/catalogCatsatFTP.xml";

        try {
            catalogOLA = Organizer.getCatalogOLA(xmlUri);
        } catch (MotuException e) {
            System.out.println("Exception : \n");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        for (ResourceOLA  resourceOLA : catalogOLA.getResourcesOLA().getResourceOLA()) {
            System.out.print(resourceOLA.getUrn());
            System.out.print(" ");
            System.out.print(resourceOLA.getInventoryUrl());
            System.out.println("");

        }

        System.out.println("End testLoadCatalogOLA : \n");
    }

    public static void testLoadInventoryOLA() {

        Inventory inventoryOLA = null;
        String xmlUri = "C:/tempVFS/nrt_med_st_chlorophyll_FTP_TEST.xml";
        //String xmlUri = "sftp://atoll:atoll@catsat-data1.cls.fr/home/atoll//atoll-distrib/HOA_Catsat/Interface_ATOLL/nrt_med_infrared_sst_timestamp_FTP_TEST.xml";
        try {
            inventoryOLA = Organizer.getInventoryOLA(xmlUri);
            // Organizer.getFileSystemManager().close();
            // Organizer.getFileSystemManager().freeUnusedResources();
            // Organizer.freeResources();

        } catch (MotuException e) {
            System.out.println("Exception : \n");
            System.out.println(e.notifyException());
            e.printStackTrace();
            return;
        }

        System.out.println(inventoryOLA.getLastModificationDate());
        System.out.println(inventoryOLA.getResource().getUrn());

        Resource resource = inventoryOLA.getResource();

        GeospatialCoverage geospatialCoverage = resource.getGeospatialCoverage();
        System.out.println(geospatialCoverage.getType());
        System.out.println(geospatialCoverage.getUnits());
        System.out.println(geospatialCoverage.getEast());
        System.out.println(geospatialCoverage.getNorth());
        System.out.println(geospatialCoverage.getSouth());
        System.out.println(geospatialCoverage.getWest());
        System.out.println(geospatialCoverage.getEastResolution());
        System.out.println(geospatialCoverage.getNorthResolution());

        TimePeriod timePeriod = resource.getTimePeriod();
        System.out.println(timePeriod.getStart());
        System.out.println(timePeriod.getEnd());
//        System.out.println(timePeriod.getStep());
//        System.out.println(timePeriod.getStep().toStandardDuration().getMillis());
//        System.out.println(timePeriod.getStep().getMillis());

        fr.cls.atoll.motu.library.inventory.Variables variables = resource.getVariables();
        for (fr.cls.atoll.motu.library.inventory.Variable variable : variables.getVariable()) {
            System.out.print(variable.getName());
            System.out.print(" ");
            System.out.print(variable.getUnits());
            System.out.print(" ");
            System.out.print(variable.getVocabularyName());
            System.out.println("");

        }

        for (fr.cls.atoll.motu.library.inventory.File file : inventoryOLA.getFiles().getFile()) {
            System.out.print(file.getName());
            System.out.print(" ");
            System.out.print(file.getWeight());
            System.out.print(" ");
            System.out.print(file.getModelPrediction());
            System.out.print(" ");
            System.out.print(file.getStartCoverageDate());
            System.out.print(" ");
            System.out.print(file.getEndCoverageDate());
            System.out.print(" ");
            System.out.print(file.getCreationDate());
            System.out.print(" ");
            System.out.print(file.getAvailabilityServiceDate());
            System.out.print(" ");
            System.out.print(file.getAvailabilitySIDate());
            System.out.print(" ");
            System.out.print(file.getTheoreticalAvailabilityDate());
            System.out.println("");

        }

        System.out.println("End testLoadInventoryOLA : \n");
    }

    public static void testLoadStdNameEquiv() {
        StandardNames stdNames = null;
        try {
            stdNames = Organizer.getStdNameEquiv();
        } catch (MotuException e) {
            e.printStackTrace();
        }

        if (stdNames == null) {
            return;
        }
        List<StandardName> listStd = stdNames.getStandardName();
        for (StandardName std : listStd) {
            System.out.println(std.getName());
            List<JAXBElement<String>> ncVars = std.getNetcdfName();
            for (JAXBElement<String> ncVar : ncVars) {

                System.out.print("\t");
                System.out.println(ncVar.getValue());
            }
        }

    }

    public static void productExtractDataMercator() {
        String productId = "mercatorPsy3v2_nat_mean_best_estimate";
        //String productId = "mercatorPsy3v2_glo_mean_best_estimate";
        // String productId = "mercatorPsy3v2R1v_med_levitus_1998";
        String locationData = "http://opendap.mercator-ocean.fr/thredds/dodsC/" + productId;
        // String locationData = "http://rdp1-jaune.cls.fr:8880/thredds/dodsC/" + productId;
        
        // String productId = "mercatorPsy3v2R1v_glo_mean_best_estimate_1182752515507.nc";
        // String locationData = "C:/Java/dev/" + productId;

        // String productId = "mercatorPsy3v2R1v_glo_mean_best_estimate_1182752515507_1183371396796.nc";
        // String locationData = "C:/apache-tomcat-5.5.16/webapps/motu-file-extract/" + productId;

        // String productId = "mercatorPsy3v1R1v_arc_mean_20060628_R20060712_1170678793644.nc";
        // String locationData = "C:/apache-tomcat-5.5.16/webapps/motu-file-extract/" + productId;
        List<String> listVar = new ArrayList<String>();
        // add variable to extract
        // listVar.add("salinity");
        // listVar.add("u");
        listVar.add("temperature");
        // listVar.add("sea_water_salinity");
        // listVar.add("sea_surface_elevation");
        // listVar.add("ocean_mixed_layer_thickness");

        // add temporal criteria
        // first element is start date
        // second element is end date (optional)
        // if only start date is set, end date equals start date
        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("2009-10-21");
        listTemporalCoverage.add("2009-10-21");

        // add Lat/Lon criteria
        // first element is low latitude
        // second element is low longitude
        // third element is high latitude
        // fourth element is high longitude
        List<String> listLatLonCoverage = new ArrayList<String>();
//        listLatLonCoverage.add("1");
//        listLatLonCoverage.add("-47");
//        listLatLonCoverage.add("-1");
//        listLatLonCoverage.add("44");
        //
        // listLatLonCoverage.add("-10");
        // listLatLonCoverage.add("-1");
        // listLatLonCoverage.add("30");
        // listLatLonCoverage.add("-50");

        // add depth (Z) criteria
        // first element is low depth
        // second element is high depth (optional)
        // if only low depth is set, high depth equals low depth value
        List<String> listDepthCoverage = new ArrayList<String>();
        listDepthCoverage.add("0");
        listDepthCoverage.add("0");

        Product product = null;

        try {
            Organizer organizer = new Organizer();

            product = organizer.extractData(locationData,
                                            listVar,
                                            listTemporalCoverage,
                                            listLatLonCoverage,
                                            listDepthCoverage,
                                            null,
                                            Organizer.Format.NETCDF);

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void productExtractDataMersea() {
        // String productId = "mercatorPsy3v1R1v_glo_mean_best_estimate";
        String productId = "MERSEA_CLS_DEMO_COLOR-D20070423-Z-180.0_180.0_-70.0_70.0-S0.1.nc";
        String locationData = "C:/Java/dev/" + productId;

        // String productId = "mercatorPsy3v1R1v_arc_mean_20060628_R20060712_1170678793644.nc";
        // String locationData = "C:/apache-tomcat-5.5.16/webapps/motu-file-extract/" + productId;
        List<String> listVar = new ArrayList<String>();
        // add variable to extract
        // listVar.add("salinity");
        // listVar.add("temperature");
        listVar.add("Grid_0001");

        // add temporal criteria
        // first element is start date
        // second element is end date (optional)
        // if only start date is set, end date equals start date
        List<String> listTemporalCoverage = new ArrayList<String>();
        // listTemporalCoverage.add("0001-01-01");
        // listTemporalCoverage.add("0001-01-01");

        // add Lat/Lon criteria
        // first element is low latitude
        // second element is low longitude
        // third element is high latitude
        // fourth element is high longitude
        List<String> listLatLonCoverage = new ArrayList<String>();
        listLatLonCoverage.add("-10");
        listLatLonCoverage.add("-100");
        listLatLonCoverage.add("45");
        listLatLonCoverage.add("120");

        // add depth (Z) criteria
        // first element is low depth
        // second element is high depth (optional)
        // if only low depth is set, high depth equals low depth value
        List<String> listDepthCoverage = new ArrayList<String>();
        // listDepthCoverage.add("0");
        // listDepthCoverage.add("1500");

        Product product = null;

        try {
            Organizer organizer = new Organizer();

            product = organizer.extractData(locationData,
                                            listVar,
                                            listTemporalCoverage,
                                            listLatLonCoverage,
                                            listDepthCoverage,
                                            null,
                                            Organizer.Format.NETCDF);

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void productExtractDiversity() {
        // String productId = "Test_Galapagos_20070901_ssh.nc";
        // String locationData = "J:/dev/" + productId;
        //String productId = "dt_upd_global_merged_msla_h_20050831_20050831_20060206.nc";
        //String locationData = "C:/BratData/hangzhou/" + productId;
        //String productId = "GLB_CO03_21880.cdf";
        String productId = "GLB_CO03_21880.nc";
        String locationData = "C:/Downloads/" + productId;

        // String productId = "mercatorPsy3v1R1v_arc_mean_20060628_R20060712_1170678793644.nc";
        // String locationData = "C:/apache-tomcat-5.5.16/webapps/motu-file-extract/" + productId;
        List<String> listVar = new ArrayList<String>();
        // add variable to extract
        // listVar.add("salinity");
        // listVar.add("temperature");
        listVar.add("Grid_0001");

        // add temporal criteria
        // first element is start date
        // second element is end date (optional)
        // if only start date is set, end date equals start date
        List<String> listTemporalCoverage = new ArrayList<String>();
        // listTemporalCoverage.add("2007-09-01");
        // listTemporalCoverage.add("2007-09-01");

        // add Lat/Lon criteria
        // first element is low latitude
        // second element is low longitude
        // third element is high latitude
        // fourth element is high longitude
        List<String> listLatLonCoverage = new ArrayList<String>();
        listLatLonCoverage.add("90");
        listLatLonCoverage.add("-120");
        listLatLonCoverage.add("-90");
        listLatLonCoverage.add("-100");

        // add depth (Z) criteria
        // first element is low depth
        // second element is high depth (optional)
        // if only low depth is set, high depth equals low depth value
        List<String> listDepthCoverage = new ArrayList<String>();
        // listDepthCoverage.add("0");
        // listDepthCoverage.add("1500");

        Product product = null;

        try {
            Organizer organizer = new Organizer();

            product = organizer.extractData(locationData,
                                            listVar,
                                            listTemporalCoverage,
                                            listLatLonCoverage,
                                            listDepthCoverage,
                                            null,
                                            Organizer.Format.NETCDF);

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void testJason2Local() {
        String productId = "JA2_IPN_2PTP010_001_20081009_070651_20081009_080304";
        String locationData = "C:/data/jason-2/igdr/StandardDataset/" + productId;
        // String locationData = "http://atoll-dev.cls.fr:60080/thredds/dodsC/Jason2_ATP_IGDR_REDUCED";

        // String productId = "mercatorPsy3v1R1v_arc_mean_20060628_R20060712_1170678793644.nc";
        // String locationData = "C:/apache-tomcat-5.5.16/webapps/motu-file-extract/" + productId;
        List<String> listVar = new ArrayList<String>();
        // add variable to extract
        // listVar.add("salinity");
        // listVar.add("temperature");
        listVar.add("swh_ku");

        // add temporal criteria
        // first element is start date
        // second element is end date (optional)
        // if only start date is set, end date equals start date
        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("2008-01-26 08:50:37");
        listTemporalCoverage.add("2009-01-26 08:50:40");

        // add Lat/Lon criteria
        // first element is low latitude
        // second element is low longitude
        // third element is high latitude
        // fourth element is high longitude
        List<String> listLatLonCoverage = new ArrayList<String>();
        // listLatLonCoverage.add("-10");
        // listLatLonCoverage.add("-50");
        // listLatLonCoverage.add("10");
        // listLatLonCoverage.add("-30");

        // add depth (Z) criteria
        // first element is low depth
        // second element is high depth (optional)
        // if only low depth is set, high depth equals low depth value
        List<String> listDepthCoverage = new ArrayList<String>();
        // listDepthCoverage.add("0");
        // listDepthCoverage.add("1500");

        Product product = null;

        try {
            Organizer organizer = new Organizer();

            product = organizer.extractData(locationData,
                                            listVar,
                                            listTemporalCoverage,
                                            listLatLonCoverage,
                                            listDepthCoverage,
                                            null,
                                            Organizer.Format.NETCDF);

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void productExtractData(Product product) {
        List<String> listVar = new ArrayList<String>();
        // add variable to extract
        // listVar.add("salinity");
        // listVar.add("temperature");
        // listVar.add("ssh");
        listVar.add("ext_link/win_speed_alt");

        // add temporal criteria
        // first element is start date
        // second element is end date (optional)
        // if only start date is set, end date equals start date
        List<String> listTemporalCoverage = new ArrayList<String>();
        // listTemporalCoverage.add("2006-08-30");
        // listTemporalCoverage.add("2006-08-31");

        // add Lat/Lon criteria
        // first element is low latitude
        // second element is low longitude
        // third element is high latitude
        // fourth element is high longitude
        List<String> listLatLonCoverage = new ArrayList<String>();
        listLatLonCoverage.add("75");
        listLatLonCoverage.add("150");
        listLatLonCoverage.add("76");
        listLatLonCoverage.add("151");

        // add depth (Z) criteria
        // first element is low depth
        // second element is high depth (optional)
        // if only low depth is set, high depth equals low depth value
        List<String> listDepthCoverage = new ArrayList<String>();
        listDepthCoverage.add("0");
        listDepthCoverage.add("15");

        try {
            Organizer organizer = new Organizer();

            organizer.extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage, null, Organizer.Format.NETCDF);

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void testProjection() {
        // String productId = "mercatorPsy3v1R1v_arc_mean_20060628_R20060712.nc";
        // String locationData = "C:/Java/dev/atoll-motu/" + productId;
        // String productId = "JA2_IPN_2PTP010_001_20081009_070651_20081009_080304";
        // String locationData = "C:/data/jason-2/igdr/StandardDataset/" + productId;
        // String productId = "extlink_source.h5";
        // String locationData = "C:/Documents and Settings/dearith/" + productId;
        String productId = "TestHDF5.h5";
        String locationData = "C:/" + productId;

        Product product = productInformationFromLocationData(locationData);
        NetCdfReader netCdfReader = product.getNetCdfReader();
        NetcdfDataset ncDataset = product.getNetCdfReaderDataset();
        ProductMetaData productMetaData = product.getProductMetaData();

        // try {
        // Variable v = netCdfReader.getVariable("ssh");
        // //v.addAttribute(new Attribute(_Coordinate.Systems, "ProjectionCoordinateSystem
        // LatLonCoordinateSystem"));
        // v.addAttribute(new Attribute(_Coordinate.Systems, "LatLonCoordinateSystem"));
        //
        // v = netCdfReader.getVariable("latitude");
        // v.addAttribute(new Attribute(_Coordinate.AxisType, "Lat"));
        // v = netCdfReader.getVariable("longitude");
        // v.addAttribute(new Attribute(_Coordinate.AxisType, "Lon"));
        // } catch (NetCdfVariableNotFoundException e1) {
        // e1.printStackTrace();
        // }
        //
        // NetCdfCancelTask ct = new NetCdfCancelTask();
        // CoordSysBuilderYXLatLon conv = new CoordSysBuilderYXLatLon();
        // conv.augmentDataset(ncDataset, ct);
        // if (ct.hasError()) {
        // System.out.println(ct.getError());
        // return;
        // }
        // conv.buildCoordinateSystems(ncDataset);

        List<CoordinateSystem> listCoordinateSystems = (List<CoordinateSystem>) ncDataset.getCoordinateSystems();

        for (CoordinateSystem cs : listCoordinateSystems) {
            System.out.println("Coordinate Systems");
            System.out.print("\tName:\t");
            System.out.print(cs.getName());
            System.out.print("\tRankDomain:\t");
            System.out.print(cs.getRankDomain());
            System.out.print("\tRankRange:\t");
            System.out.print(cs.getRankRange());
            System.out.print("\tImplicit:\t");
            System.out.print(cs.isImplicit());
            System.out.print("\tGeoreferencing:\t");
            System.out.println(cs.isGeoReferencing());
        }

        // Gets coordinate axes metadata.
        List<CoordinateAxis> coordinateAxes = netCdfReader.getCoordinateAxes();

        if (productMetaData.getCoordinateAxes() == null) {
            productMetaData.setCoordinateAxes(new HashMap<AxisType, CoordinateAxis>());
        }

        for (Iterator<CoordinateAxis> it = coordinateAxes.iterator(); it.hasNext();) {
            CoordinateAxis coordinateAxis = (CoordinateAxis) it.next();
            AxisType axisType = coordinateAxis.getAxisType();
            if (axisType != null) {
                if (!(productMetaData.coordinateAxesContainsKey(axisType))) {
                    productMetaData.putCoordinateAxes(axisType, coordinateAxis);
                }
                System.out.println("Coordinate Axes");
                System.out.print("\tType:\t");
                System.out.print(axisType.toString());
                System.out.print("\tName and dims:\t");
                System.out.println(coordinateAxis.getNameAndDimensions());
            }
        }

        List<Variable> listVars = netCdfReader.getVariables();
        for (Variable v : listVars) {
            System.out.print("Variable name and dims: ");
            System.out.println(v.getNameAndDimensions());
            System.out.print("\tisCaching:\t");
            System.out.print(v.isCaching());
            System.out.print("\tDimensions:\t");
            System.out.println(v.getDimensions().toString());

        }
        try {
            // NetCdfWriter netCdfWriter = new NetCdfWriter("C:/Java/dev/atoll-motu/testMercatorArc.nc",
            // true);
            // List<Attribute> listAttr = netCdfReader.getAttributes();
            //
            // netCdfWriter.writeGlobalAttributes(listAttr);
            // // GridDataset gds = new GridDataset(product.getNetCdfReaderDataset());
            // // System.out.println(gds.getInfo());
            // netCdfWriter.writeVariables(ncDataset);
            // netCdfWriter.finish(null);

            productExtractData(product);

            // netCdfReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // testProjection2();
        // testProjection3(product);
    }

    public static void testProjection2() {
        String productId = "testMercatorArc.nc";
        String locationData = "C:/Java/dev/atoll-motu/" + productId;

        Product product = productInformationFromLocationData(locationData);
        testProjection3(product);
    }

    public static void testProjection3(Product product) {
        NetCdfReader netCdfReader = product.getNetCdfReader();
        NetcdfDataset ncDataset = product.getNetCdfReaderDataset();
        ProductMetaData productMetadata = product.getProductMetaData();

        // CoordinateSystem cs = new CoordinateSystem(ncDataset, productMetadata.getGeoXYAxis(), null);
        CoordinateSystem cs = new CoordinateSystem(ncDataset, productMetadata.getLatLonAxis(), null);
        Formatter errMessages = new Formatter();
        GridCoordSys gcs = new GridCoordSys(cs, errMessages);

        System.out.println(productMetadata.getLatAxisMinValueAsString());
        System.out.println(productMetadata.getLatAxisMaxValueAsString());
        System.out.println(productMetadata.getLonAxisMinValueAsString());
        System.out.println(productMetadata.getLonAxisMaxValueAsString());
    }

    public static void testReadFromListe() {
        int count = 0;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            FileWriter file = new FileWriter("./target/resultListe.txt");

            BufferedReader in = new BufferedReader(new FileReader("C:/Java.OLD/dev/atoll-motu/liste.txt"));
            String line;
            count = 0;
            while ((line = in.readLine()) != null) {
                if (line == null) {
                    continue;
                }
                if (line.equals("")) {
                    continue;
                }
                // System.out.println(line); // Print the line

                // http://opendap.aviso.oceanobs.com/data/SSH/duacs/regional-mfstep/dt/ref/sla/e1/
                // http://opendap.aviso.oceanobs.com/data/SSH/duacs/global/nrt/msla/merged/h/
                // String locationData =
                // "http://opendap.aviso.oceanobs.com/data/SSH/duacs/regional-mfstep/dt/upd/sla/e2/" + line;
                String locationData = "http://opendap.aviso.oceanobs.com/data/SSH/duacs/global/nrt/msla/merged/h/" + line;
                // String locationData = "C:/BratData/netCDF/" + line;
                Product product = productInformationFromLocationData(locationData);

                List<Dimension> dims = product.getNetCdfReader().getDimensionList();
                for (Dimension dim : dims) {
                    file.append("Fichier : ");
                    file.append(line);
                    file.append("\tdimension ");
                    file.append(dim.getName());
                    file.append(": ");
                    file.append(new Integer(dim.getLength()).toString());
                    file.append("\n");
                }
                count++;
            }
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print("count=");
        System.out.println(count);
        // System.out.println(stringBuffer.toString());

    }

    public static void productExtractDataCatsat() {
        // String productId = "mercatorPsy3v1R1v_glo_mean_best_estimate";
        // String productId = "nrt_glo_st_chlorophyll";
        //String productId = "duacs_global_nrt_msla_merged_h_uv";
        String productId = "nrt_atl_oa_chlorophyll";
        // String productId = "global_sst";
        // String productId = "GLB_TE01_20550.nc";

        // http://themismotu.cls.fr:8280/atoll-motuservlet/Catsat?action=productdownload&service=Catsat&data=http%3A%2F%2Fcatsatopendap.cls.fr%3A8080%2Fthredds%2FdodsC%2Fnrt_atl_oa_chlorophyll&nexturl=+&x_lo=-60&x_hi=20&y_lo=-15&y_hi=25&output=netcdf&region=-60.0%2C20.0%2C-15.0%2C25.0&yhi_text=25&xlo_text=-60&xhi_text=20&ylo_text=-15&t_lo_0=2008-02-09&t_lo=2008-02-09&t_hi_0=2008-02-09&t_hi=2008-02-09&variable=Grid_0001

        //String locationData = "http://catsatopendap.cls.fr:8080/thredds/dodsC/" + productId;
        String locationData = "http://catsat-data1.cls.fr:43080/thredds/dodsC/" + productId;
        // String productId = "mercatorPsy3v1R1v_arc_mean_20060628_R20060712.nc";
        // String locationData = "C:/Java/dev/" + productId;

        // String productId = "mercatorPsy3v1R1v_arc_mean_20060628_R20060712_1170678793644.nc";
        // String locationData = "C:/apache-tomcat-5.5.16/webapps/motu-file-extract/" + productId;
        List<String> listVar = new ArrayList<String>();
        // add variable to extract
        // listVar.add("salinity");
        // listVar.add("temperature");
        listVar.add("Grid_0001");

        // add temporal criteria
        // first element is start date
        // second element is end date (optional)
        // if only start date is set, end date equals start date
        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("2009-10-22");
        listTemporalCoverage.add("2009-10-22");

        // add Lat/Lon criteria
        // first element is low latitude
        // second element is low longitude
        // third element is high latitude
        // fourth element is high longitude
        List<String> listLatLonCoverage = new ArrayList<String>();
//        listLatLonCoverage.add("30");
//        listLatLonCoverage.add("20");
//        listLatLonCoverage.add("40");
//        listLatLonCoverage.add("5");

        // add depth (Z) criteria
        // first element is low depth
        // second element is high depth (optional)
        // if only low depth is set, high depth equals low depth value
        List<String> listDepthCoverage = new ArrayList<String>();
        // listDepthCoverage.add("0");
        // listDepthCoverage.add("1500");

        Product product = null;

        try {
            Organizer organizer = new Organizer();

            product = organizer.extractData(locationData,
                                            listVar,
                                            listTemporalCoverage,
                                            listLatLonCoverage,
                                            listDepthCoverage,
                                            null,
                                            Organizer.Format.NETCDF);

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void productExtractDataMerseaFromHttp() {

        // http://themismotu.cls.fr:8280/atoll-motuservlet/Catsat?action=productdownload&service=Catsat&data=http%3A%2F%2Fcatsatopendap.cls.fr%3A8080%2Fthredds%2FdodsC%2Fnrt_atl_oa_chlorophyll&nexturl=+&x_lo=-60&x_hi=20&y_lo=-15&y_hi=25&output=netcdf&region=-60.0%2C20.0%2C-15.0%2C25.0&yhi_text=25&xlo_text=-60&xhi_text=20&ylo_text=-15&t_lo_0=2008-02-09&t_lo=2008-02-09&t_hi_0=2008-02-09&t_hi=2008-02-09&variable=Grid_0001

        String locationData = "http://mersea.dmi.dk/thredds/dodsC/mersea/BalticBestEstimate";
        // String productId = "mercatorPsy3v1R1v_arc_mean_20060628_R20060712.nc";
        // String locationData = "C:/Java/dev/" + productId;

        // String productId = "mercatorPsy3v1R1v_arc_mean_20060628_R20060712_1170678793644.nc";
        // String locationData = "C:/apache-tomcat-5.5.16/webapps/motu-file-extract/" + productId;
        List<String> listVar = new ArrayList<String>();
        // add variable to extract
        // listVar.add("salinity");
        // listVar.add("temperature");
        listVar.add("wvel");

        // add temporal criteria
        // first element is start date
        // second element is end date (optional)
        // if only start date is set, end date equals start date
        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("2008-05-27");
        listTemporalCoverage.add("2008-05-27");

        // add Lat/Lon criteria
        // first element is low latitude
        // second element is low longitude
        // third element is high latitude
        // fourth element is high longitude
        List<String> listLatLonCoverage = new ArrayList<String>();
        // listLatLonCoverage.add("25");
        // listLatLonCoverage.add("-60");
        // listLatLonCoverage.add("-15");
        // listLatLonCoverage.add("20");

        // add depth (Z) criteria
        // first element is low depth
        // second element is high depth (optional)
        // if only low depth is set, high depth equals low depth value
        List<String> listDepthCoverage = new ArrayList<String>();
        // listDepthCoverage.add("0");
        // listDepthCoverage.add("1500");

        Product product = null;

        try {
            Organizer organizer = new Organizer();

            product = organizer.extractData(locationData,
                                            listVar,
                                            listTemporalCoverage,
                                            listLatLonCoverage,
                                            listDepthCoverage,
                                            null,
                                            Organizer.Format.NETCDF);

            // get the output full file name (with path)
            String extractLocationData = product.getExtractLocationData();
            // get the url to download the output file.
            String urlExtractPath = product.getDownloadUrlPath();

            System.out.println(String.format("Product file is stored on the server in %s", extractLocationData));
            System.out.println(String.format("Product %s can be downloaded with http at %s", product.getProductId(), urlExtractPath));

        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void testReadFileInJar() {
        try {
            URL url = new URL(
                    "jar:file:\\C:\\Java\\apache-tomcat-5.5.20\\webapps\\atoll-motu-servlet\\WEB-INF\\lib\\atoll-motu-1.0.0.10.jar!/howToGetExceedData.aviso.info");
            URLConnection urlc = url.openConnection();
            InputStream is = urlc.getInputStream();
            InputStreamReader eisr = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(eisr);
            String nextLine = "";
            StringBuffer stringBuffer = new StringBuffer();
            while ((nextLine = in.readLine()) != null) {
                stringBuffer.append(nextLine);
            }
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ServiceData s = new ServiceData();
        s.setGroup("aviso");
        StringBuffer stringBuffer = new StringBuffer();

        try {
            s.getHowTogetExceededData(stringBuffer);
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void testGetAmountDataSize() {
        // String serviceName = "aviso_nrt";
        //String serviceName = "avisoNRT";
        String serviceName = "Catsat";

        //String productId = "duacs_global_nrt_madt_merged_h";
        String productId = "nrt_glo_hr_infrared_sst";
        // String locationData = "http://opendap.aviso.oceanobs.com/thredds/dodsC/" + productId;

        // String locationData = "http://rdp2-jaune.cls.fr:8880/thredds/dodsC/" + productId;

        List<String> listVar = new ArrayList<String>();
        listVar.add("Grid_0001");

        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("2009-10-25");
         listTemporalCoverage.add("2009-10-26");

        List<String> listLatLonCoverage = new ArrayList<String>();
        // listLatLonCoverage.add("46");
        // listLatLonCoverage.add("-20");
        // listLatLonCoverage.add("30");
        // listLatLonCoverage.add("-10");

        List<String> listDepthCoverage = null;
        // listDepthCoverage = new ArrayList<String>();
        // listDepthCoverage.add("0");
        // listDepthCoverage.add("125");

        Product product = null;
        FileWriter writer = null;

        try {
            Organizer organizer = new Organizer();

            writer = new FileWriter("./target/resultGetamountDataSize.html");

            product = organizer.getAmountDataSize(serviceName,
                                                  listVar,
                                                  listTemporalCoverage,
                                                  listLatLonCoverage,
                                                  listDepthCoverage,
                                                  productId,
                                                  writer,
                                                  false);

//            writer.flush();
//            writer.close();

            double size = product.getAmountDataSizeAsBytes();
            System.out.print("size is ");
            System.out.print(size);
            System.out.println(" bytes");
        } catch (MotuExceptionBase e) {
            System.out.println(e.notifyException());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void testSynchronized() {

        TestIntfce testIntfce = new TestIntfce();
        TestIntfce.Client[] c = { testIntfce.new Client("Client1"), testIntfce.new Client("Client2"), };
        for (int i = 0; i < c.length; i++) {
            c[i].start();
        }
        TestIntfce.Client2[] c2 = { testIntfce.new Client2("ClientA"), testIntfce.new Client2("ClientB") };
        for (int i = 0; i < c2.length; i++) {
            c2[i].start();
        }
        TestIntfce.Client3[] c3 = { testIntfce.new Client3("ClientMercator1"), testIntfce.new Client3("ClientMercator2") };
        for (int i = 0; i < c3.length; i++) {
            c3[i].start();
        }

    }

    public class Client extends Thread {
        /**
         * Logger for this class
         */
        private final Logger LOG = Logger.getLogger(Client.class);

        public Client(String name) {
            this.name = name;
        }

        String name;

        public void run() {

            System.out.print("Start Client ");
            System.out.println(name);

            TestIntfce.testGetAmountDataSize();
            TestIntfce.testGetAmountDataSize();

            System.out.print("End Client ");
            System.out.println(name);

        }
    }
    public class Client2 extends Thread {
        /**
         * Logger for this class
         */
        private final Logger LOG = Logger.getLogger(Client2.class);

        public Client2(String name) {
            this.name = name;
        }

        String name;

        public void run() {
            System.out.print("Start Client2 ");
            System.out.println(name);

            TestIntfce.productExtractDataHTMLAviso();

            System.out.print("End Client2 ");
            System.out.println(name);
        }
    }
    public class Client3 extends Thread {
        /**
         * Logger for this class
         */
        private final Logger LOG = Logger.getLogger(Client3.class);

        public Client3(String name) {
            this.name = name;
        }

        String name;

        public void run() {
            System.out.print("Start Client3 ");
            System.out.println(name);

            TestIntfce.productInformation();

            System.out.print("End Client3 ");
            System.out.println(name);
        }
    }

    public static void DetectProxy() throws Exception {
        System.setProperty("proxyHost", "proxy.cls.fr"); // adresse IP
        System.setProperty("proxyPort", "8080");
        System.setProperty("socksProxyHost", "proxy.cls.fr");
        // System.setProperty("http.proxyHost", "http-proxy.ece.fr");
        // System.setProperty("http.proxyPort", "3128");
        // System.setProperty("java.net.useSystemProxies", "true");
        List<Proxy> proxyList = ProxySelector.getDefault().select(new URI("http://www.yahoo.com/"));
        for (Proxy proxy : proxyList) {
            System.out.println("Proxy type : " + proxy.type());
            InetSocketAddress addr = (InetSocketAddress) proxy.address();
            if (addr == null) {
                System.out.println("DIRECT CONXN");
            } else {
                System.out.println("Proxy hostname : " + addr.getHostName() + ":" + addr.getPort());
            }
        }
    }

    public static void testFraction() {
        String s = "0.494024";
        double number = 0.49402499198913574;
        // double number = 0.00;
        int in = (int) (number);
        double frac = number - in;

        double d = Double.valueOf(in).doubleValue();
        System.out.println("Integral is:=" + in);
        System.out.println("Fraction is:=" + frac);
        String[] res = new String[2];
        res = Double.toString(number).split("\\.");
        System.out.println(" The number of decimals :  " + res[1].length());
        System.out.println(" Frac is zero :  " + (frac == 0.));

        // DecimalFormat decimalFormat = new DecimalFormat("##0.#####", new DecimalFormatSymbols(Locale.US));
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        int desired_number_of_digits = 2;
        decimalFormat.setMinimumFractionDigits(desired_number_of_digits);
        decimalFormat.setMaximumFractionDigits(desired_number_of_digits);
        decimalFormat.setRoundingMode(RoundingMode.UP);
        System.out.println(Double.toString(number));
        System.out.println(decimalFormat.format(number));
        // int precision = 10^2;
        // System.out.println(precision);
        // System.out.println(Math.floor(number* precision )/precision);

    }

    public static void testFeatures() {
        String location = "C:/BratData/testFeatures.nc";
        File file = new File(location);
        try {
            CFPointObWriter.rewritePointFeatureDataset(location, "C:/TEMP/" + file.getName(), true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static boolean rewriteTrajectoryFeatureDataset(String fileIn, String fileOut, boolean inMemory) throws IOException {
        System.out.println("Rewrite2 .nc files from " + fileIn + " to " + fileOut + " inMemory= " + inMemory);

        long start = System.currentTimeMillis();

        // do it in memory for speed
        NetcdfFile ncfile = inMemory ? NetcdfFile.openInMemory(fileIn) : NetcdfFile.open(fileIn);
        NetcdfDataset ncd = new NetcdfDataset(ncfile);

        Formatter errlog = new Formatter();
        FeatureDataset fd = FeatureDatasetFactoryManager.wrap(FeatureType.ANY_POINT, ncd, null, errlog);
        if (fd == null)
            return false;

        if (fd instanceof FeatureDatasetPoint) {
            TestIntfce.writeTrajectoryFeatureCollection((FeatureDatasetPoint) fd, fileOut);
            fd.close();
            long took = System.currentTimeMillis() - start;
            System.out.println(" that took " + (took - start) + " msecs");
            return true;
        }

        return false;

    }

    /**
     * Write a ucar.nc2.ft.PointFeatureCollection in CF point format.
     * 
     * @param pfDataset find the first PointFeatureCollection, and write all data from it
     * @param fileOut write to this netcdf-3 file
     * @return number of records written
     * @throws IOException on read/write error, or if no PointFeatureCollection in pfDataset
     */
    public static int writeTrajectoryFeatureCollection(FeatureDatasetPoint pfDataset, String fileOut) throws IOException {
        // extract the TrajectoryFeatureCollection
        TrajectoryFeatureCollection pointFeatureCollection = null;
        List<FeatureCollection> featureCollectionList = pfDataset.getPointFeatureCollectionList();
        for (FeatureCollection featureCollection : featureCollectionList) {
            if (featureCollection instanceof TrajectoryFeatureCollection)
                pointFeatureCollection = (TrajectoryFeatureCollection) featureCollection;
        }
        if (null == pointFeatureCollection)
            throw new IOException("There is no PointFeatureCollection in  " + pfDataset.getLocation());

        long start = System.currentTimeMillis();

        FileOutputStream fos = new FileOutputStream(fileOut);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(fos, 10000));
        WriterCFPointObsDataset writer = null;

        /*
         * LOOK BAD List<VariableSimpleIF> dataVars = new ArrayList<VariableSimpleIF>(); ucar.nc2.NetcdfFile
         * ncfile = pfDataset.getNetcdfFile(); if ((ncfile == null) || !(ncfile instanceof NetcdfDataset)) {
         * dataVars.addAll(pfDataset.getDataVariables()); } else { NetcdfDataset ncd = (NetcdfDataset) ncfile;
         * for (VariableSimpleIF vs : pfDataset.getDataVariables()) { if (ncd.findCoordinateAxis(vs.getName())
         * == null) dataVars.add(vs); } }
         */

        int count = 0;
        pointFeatureCollection.resetIteration();
        while (pointFeatureCollection.hasNext()) {
            PointFeature pointFeature = (PointFeature) pointFeatureCollection.next();
            StructureData data = pointFeature.getData();
            if (count == 0) {
                EarthLocation loc = pointFeature.getLocation(); // LOOK we dont know this until we see the obs
                String altUnits = Double.isNaN(loc.getAltitude()) ? null : "meters"; // LOOK units may be
                // wrong
                writer = new WriterCFPointObsDataset(out, pfDataset.getGlobalAttributes(), altUnits);
                writer.writeHeader(pfDataset.getDataVariables(), -1);
            }
            writer.writeRecord(pointFeature, data);
            count++;
        }

        writer.finish();
        out.flush();
        out.close();

        long took = System.currentTimeMillis() - start;
        System.out.printf("Write %d records from %s to %s took %d msecs %n", count, pfDataset.getLocation(), fileOut, took);
        return count;
    }
}
