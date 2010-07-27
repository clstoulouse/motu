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
/**
 * 
 */
package fr.cls.atoll.motu.library.misc.data;

import fr.cls.atoll.motu.library.misc.MyAuthenticator;
import fr.cls.atoll.motu.library.misc.Test;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.metadata.ProductMetaData;
import fr.cls.atoll.motu.library.misc.netcdf.NetCdfReader;
import fr.cls.atoll.motu.library.misc.opendap.server.Aggregation;
import fr.cls.atoll.motu.library.misc.opendap.server.Catalog;
import fr.cls.atoll.motu.library.misc.opendap.server.Dataset;
import fr.cls.atoll.motu.library.misc.opendap.server.Metadata;
import fr.cls.atoll.motu.library.misc.opendap.server.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ucar.nc2.dataset.CoordinateAxis;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class TestData {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        // Mandatory if there is a proxy to access Opendap server.
        System.setProperty("proxyHost", "proxy.cls.fr"); // adresse IP
        System.setProperty("proxyPort", "8080");
        Authenticator.setDefault(new MyAuthenticator());

        //testLoadCatalog();
        testLoadOpendapMetaData();
    }

    public static Catalog testLoadCatalogFromFile(String path) {

        Catalog catalogXml = null;

        try {
            // catalogXml =
            // loadConfigOpendap("http://opendap.aviso.oceanobs.com/data/catalogConfig.xml");
            // catalogXml =
            // loadConfigOpendap("http://opendap.aviso.oceanobs.com:80/thredds/dodsC/catalogConfig.xml");
            catalogXml = loadConfigOpendapFromFile(path);
            // http://rdp2-jaune.cls.fr:8380/thredds/dodsC/catalogConfig.xml
            // catalogXml =
            // loadConfigOpendap("http://opendap.mercator-ocean.fr:80/thredds/dodsC/catalogConfig.xml");
        } catch (JAXBException e) {
            System.out.println("JAXBException :\n");
            System.out.println(e.getMessage());
            System.out.println(e.getErrorCode());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception : \n");
            e.printStackTrace();
        }

        System.out.println(catalogXml.getName());
        System.out.println(catalogXml.getVersion());

        Dataset dataset = catalogXml.getDataset();
        System.out.println(dataset.getName());
        System.out.println(dataset.getServiceName());
        System.out.println(dataset.getUrlPath());

        getDataset(catalogXml.getDataset());

        // List listDataSet = catalogXml.getDataset().getDatasetOrCatalogRef();
        // int countDataset = listDataSet.size();
        //        
        // for (int i = 0; i < countDataset; i++) {
        // Dataset d = (Dataset) dataset.getDatasetOrCatalogRef().get(i);
        // if (d instanceof Dataset) {
        // System.out.print("dataset:");
        // System.out.print(d.getName());
        // System.out.print(d.getServiceName());
        // System.out.println(d.getUrlPath());
        // }
        // }
        System.out.println("End Test : \n");
        return catalogXml;
    }

    public static Catalog testLoadCatalog() {

        Catalog catalogXml = null;

        try {
            // catalogXml =
            // loadConfigOpendap("http://opendap.aviso.oceanobs.com/data/catalogConfig.xml");
            // catalogXml =
            // loadConfigOpendap("http://opendap.aviso.oceanobs.com:80/thredds/dodsC/catalogConfig.xml");
            catalogXml = loadConfigOpendap("http://opendap-nrt.aviso.oceanobs.com/thredds/dodsC/catalogConfig.xml");
            // catalogXml =
            // loadConfigOpendap("http://rdp2-jaune.cls.fr:8380/thredds/dodsC/catalogConfig.xml");
            // http://rdp2-jaune.cls.fr:8380/thredds/dodsC/catalogConfig.xml
            // catalogXml =
            // loadConfigOpendap("http://opendap.mercator-ocean.fr:80/thredds/dodsC/catalogConfig.xml");
        } catch (JAXBException e) {
            System.out.println("JAXBException :\n");
            System.out.println(e.getMessage());
            System.out.println(e.getErrorCode());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception : \n");
            e.printStackTrace();
        }

        System.out.println(catalogXml.getName());
        System.out.println(catalogXml.getVersion());

        Dataset dataset = catalogXml.getDataset();
        System.out.println(dataset.getName());
        System.out.println(dataset.getServiceName());
        System.out.println(dataset.getUrlPath());

        getDataset(catalogXml.getDataset());

        // List listDataSet = catalogXml.getDataset().getDatasetOrCatalogRef();
        // int countDataset = listDataSet.size();
        //        
        // for (int i = 0; i < countDataset; i++) {
        // Dataset d = (Dataset) dataset.getDatasetOrCatalogRef().get(i);
        // if (d instanceof Dataset) {
        // System.out.print("dataset:");
        // System.out.print(d.getName());
        // System.out.print(d.getServiceName());
        // System.out.println(d.getUrlPath());
        // }
        // }
        System.out.println("End Test : \n");
        return catalogXml;
    }

    public static void getDataset(Dataset dataset) {
        if (dataset == null) {
            return;
        }
        if (dataset.getUrlPath() != null) {
            System.out.print("dataset:");
            System.out.print(dataset.getName());
            System.out.print(dataset.getServiceName());
            System.out.println(dataset.getUrlPath());

            List<Object> listMetaData = dataset.getServiceOrDocumentationOrMetadata();
            // Documentation documentation = new Documentation();
            // documentation.setType("history");
            // documentation.setHref("http//xxxxx");
            // documentation.setContent("Documentation contents blablabla");
            // listMetaData.add(documentation);

            Object o = null;

            for (int j = 0; j < listMetaData.size(); j++) {
                o = listMetaData.get(j);
                if (o == null) {
                    continue;
                }
                if (!(o instanceof Service)) {
                    continue;
                }
                Service s = (Service) o;
                System.out.print("\t\tService :");
                System.out.print(" name:");
                System.out.print(s.getName());
                System.out.print(" base:");
                System.out.print(s.getBase());
                System.out.print(" suffix:");
                System.out.print(s.getSuffix());
            }
            for (int j = 0; j < listMetaData.size(); j++) {
                o = listMetaData.get(j);
                if (o == null) {
                    continue;
                }
                if (!(o instanceof Metadata)) {
                    continue;
                }
                Metadata m = (Metadata) o;
                System.out.println("\t\tMetadata :");
                System.out.println(m.getMetadataType().toString());
                List<Object> content = m.getContent();
                for (int k = 0; k < content.size(); k++) {
                    o = content.get(k);
                    if (o == null) {
                        continue;
                    }
                    if (!(o instanceof Aggregation)) {
                        continue;
                    }
                    Aggregation aggr = (Aggregation) o;
                    System.out.println(aggr.getVarName());
                    System.out.println(aggr.getDateFormat());
                    for (fr.cls.atoll.motu.library.misc.opendap.server.Variable varAggr : aggr.getVariable()) {
                        System.out.println(varAggr.getName());
                    }
                    for (fr.cls.atoll.motu.library.misc.opendap.server.FileAccess fileAccess : aggr.getFileAccess()) {
                        System.out.print(fileAccess.getUrlPath());
                        System.out.print("\t");
                        System.out.println(fileAccess.getCoord());
                        NetCdfReader netCdfReader = new NetCdfReader(
                                "http://opendap-nrt.aviso.oceanobs.com/thredds/dodsC/" + fileAccess.getUrlPath(),
                                false);
                        try {
                            netCdfReader.open();
                        } catch (MotuExceptionBase e) {
                            System.out.println(e.notifyException());
                        }
                    }
                }
            }
            return;
        }

        List<Object> listService = dataset.getServiceOrDocumentationOrMetadata();
        for (Object o : listService) {
            if (!(o instanceof Service)) {
                continue;
            }
            Service service = (Service) o;
            if (service.getServiceType() != fr.cls.atoll.motu.library.misc.opendap.server.ServiceType.NET_CDF) {
                continue;
            }
            System.out.print(service.getServiceType().value());
            System.out.print("\t");
            System.out.print(service.getName());
            System.out.print("\t");
            System.out.println(service.getBase());
        }

        List<Object> list = dataset.getDatasetOrCatalogRef();
        int countDataset = list.size();

        for (int i = 0; i < countDataset; i++) {
            Object o = list.get(i);

            if (o != null) {
                if (o instanceof Dataset) {
                    getDataset((Dataset) o);
                }
            }
        }

    }

    public static Catalog loadConfigOpendapFromFile(String path) throws JAXBException, IOException {

        JAXBContext jc = JAXBContext.newInstance("fr.cls.atoll.motu.library.misc.opendap.server");
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        FileInputStream in = new FileInputStream(path);

        Catalog catalog = (Catalog) unmarshaller.unmarshal(in);

        try {
            in.close();
        } catch (IOException io) {
            io.getMessage();
        }

        return catalog;

    }

    public static Catalog loadConfigOpendap(String path) throws JAXBException, IOException {

        JAXBContext jc = JAXBContext.newInstance("fr.cls.atoll.motu.library.misc.opendap.server");
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        // InputStream in =
        // getClass().getClassLoader().getResourceAsStream(path.substring(path.indexOf("//")));
        // InputStream in =
        // getClass().getClassLoader().getResourceAsStream("opendap_aviso.xml");
        // FileInputStream in = new
        // FileInputStream(path.substring(path.indexOf("//")));

        // FileInputStream in = new FileInputStream(path);

        // System.setProperty("proxyHost", "proxy.cls.fr"); // adresse IP
        // System.setProperty("proxyPort", "8080");
        // String authString = "login" + ":" + "pwd";
        // String auth = "Basic " + new
        // sun.misc.BASE64Encoder().encode(authString.getBytes());
        //
        // Authenticator.setDefault(new MyAuthenticator());

        URL url = new URL(path);
        // URLConnection conn = url.openConnection(new Proxy(Type.HTTP,
        // new InetSocketAddress("proxy.cls.fr", 8080)));
        URLConnection conn = url.openConnection();
        // conn.setDoInput(true);
        // conn.setRequestProperty("Proxy-Authorization", auth);

        InputStream in = conn.getInputStream();

        Catalog catalog = (Catalog) unmarshaller.unmarshal(in);

        try {
            in.close();
        } catch (IOException io) {
            io.getMessage();
        }

        return catalog;
    }

    public static void generateXmlFile(Catalog catalogXml, String path) {
        OutputStream os = null;

        try {
            JAXBContext jc = JAXBContext.newInstance("fr.cls.atoll.motu.library.misc.opendap.server");

            // create a Marshaller and marshal to System.out
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            try {
                // os = new
                // FileOutputStream(path.substring(path.indexOf("//")));
                os = new FileOutputStream(path);
            } catch (FileNotFoundException ex) {
                ex.getMessage();

            }
            // valide le content tree cataloge modifi�
            /*
             * Validator validator =jc.createValidator(); boolean isvalidate =validator.validate(catalogXml);
             * System.out.println(isvalidate);
             */

            // m.marshal( catalogXml, System.out);
            m.marshal(catalogXml, os);
        } catch (JAXBException e) {
            System.out.println("JAXBException :\n");
            System.out.println(e.getMessage());
            System.out.println(e.getErrorCode());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception : \n");
            e.printStackTrace();
        }

        try {
            os.close();
        } catch (IOException io) {
            io.getMessage();

        }

    }

    public static void testLoadOpendapMetaData() {
        Product product = new Product(false);
        //String url = "http://opendap.mercator-ocean.fr/thredds/dodsC/mercatorPsy3v1R1v_glo_mean_bulletin_2006_03_29";
        String url = "http://opendap.mercator-ocean.fr/thredds/dodsC/mercatorPsy3v2_arc_mean_best_estimate";
        try {
            product.loadOpendapMetaData(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("---->PRODUCT");
        System.out.println(Test.dump(product));
        System.out.println("---->PRODUCTMETADATA");
        System.out.println(Test.dump(product.getProductMetaData()));
        
        ProductMetaData productMetaData = product.getProductMetaData();
        List<CoordinateAxis> axes =  productMetaData.getLatLonAxis();
        System.out.println(axes);
        CoordinateAxis axis =  productMetaData.getTimeAxis();
        System.out.println(axis);
        
        
    }

}
