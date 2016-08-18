package fr.cls.atoll.motu.web.dal.catalog.opendap;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.library.cas.exception.MotuCasException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.metadata.DocMetaData;
import fr.cls.atoll.motu.web.dal.catalog.AbstractCatalogLoader;
import fr.cls.atoll.motu.web.dal.catalog.tds.JAXBTDSModel;
import fr.cls.atoll.motu.web.dal.config.xml.model.CatalogService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import fr.cls.atoll.motu.web.dal.tds.opendap.model.Catalog;
import fr.cls.atoll.motu.web.dal.tds.opendap.model.Dataset;
import fr.cls.atoll.motu.web.dal.tds.opendap.model.Service;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class OpenDapCatalogReader extends AbstractCatalogLoader {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Loads an Opendap catalog..
     * 
     * @param path path of the Xml catalog file.
     * 
     * @throws MotuException the motu exception
     */
    public CatalogData loadOpendapCatalog(CatalogService catalogService) throws MotuException {
        try {
            Catalog catalogXml = getCatalogFromOpenDap(catalogService);
            Dataset topLevelDataset = catalogXml.getDataset();
            List<Object> list = topLevelDataset.getDatasetOrCatalogRef();

            CatalogData catalogData = new CatalogData();
            catalogData.setUrlSite(catalogService.getUrlSite());
            catalogData.setCasAuthentication(false);
            catalogData.setTitle(topLevelDataset.getName());

            for (Iterator<Object> it = list.iterator(); it.hasNext();) {
                Object o = it.next();
                if (o != null) {
                    if (o instanceof Dataset) {
                        catalogData.setCurrentProductType("");
                        catalogData.getSameProductTypeDataset().clear();

                        loadOpendapProducts((Dataset) o, catalogData, catalogService.getCasAuthentication());

                        if (catalogData.getSameProductTypeDataset().size() > 0) {
                            catalogData.getListProductTypeDataset().add(catalogData.getSameProductTypeDataset());
                        }

                    }
                }
            }

            // Remove products that are not anymore in the catalog
            catalogData.productsKeySet().retainAll(catalogData.getProductsLoaded());

            return catalogData;
        } catch (Exception e) {
            throw new MotuException(ErrorType.LOADING_CATALOG, "Error while reading catalog from TDS", e);
        }
    }

    /**
     * Loads Opendap products from Opendap catalog.
     * 
     * @param dataset dataset (from Opendap catalog) from which information will be loaded Dataset contains a
     *            list of datasets, recursivity is use to load.
     * 
     * @throws MotuException the motu exception
     */
    private void loadOpendapProducts(Dataset dataset, CatalogData catalogData, boolean useSSO) throws MotuException {
        if (dataset == null) {
            return;
        }

        // When Url path of the dataset is not null or not empty,
        // we are at the last level --> Create Product and add to the list.
        if (dataset.getUrlPath() != null) {
            if (!dataset.getUrlPath().equals("")) {

                initializeProductFromOpendap(dataset, catalogData, useSSO);

                return;
            }
        }

        // Saves - the product type at the top level product (on the first call,
        // level 0)
        // - the product sub-type, if not top level product
        // (type or sub-type correspond to the dataser name)
        if (catalogData.getCurrentProductType() == null || catalogData.getCurrentProductType().length() <= 0) {
            catalogData.setCurrentProductType(dataset.getName());
        } else {
            catalogData.getCurrentProductSubTypes().add(dataset.getName());
        }

        List<Object> list = dataset.getDatasetOrCatalogRef();

        for (Iterator<Object> it = list.iterator(); it.hasNext();) {
            Object o = it.next();

            if (o != null) {
                if (o instanceof Dataset) {
                    loadOpendapProducts((Dataset) o, catalogData, useSSO);
                }
            }
        }
        if (catalogData.getSameProductTypeDataset().size() > 0) {
            catalogData.getListProductTypeDataset().add(catalogData.getSameProductTypeDataset());
            catalogData.getSameProductTypeDataset().clear();
        }

        int last = catalogData.getCurrentProductSubTypes().size() - 1;
        if (last >= 0) {
            catalogData.getCurrentProductSubTypes().remove(last);
        }

    }

    /**
     * Initializes product and its metadata from an Opendap dataset.
     * 
     * @param dataset Opendap dataset which have a non-empty url path.
     * 
     * @throws MotuException the motu exception
     */
    private void initializeProductFromOpendap(Dataset dataset, CatalogData cd, boolean useSSO) throws MotuException {

        if (dataset == null) {
            throw new MotuException(ErrorType.LOADING_CATALOG, "Error in intializeProductFromOpendap - Opendap dataset is null");
        }
        if (dataset.getUrlPath() == null) {
            throw new MotuException(
                    ErrorType.LOADING_CATALOG,
                    "Error in intializeProductFromOpendap - Invalid dataset branch - Opendap dataset has a null url path");
        }
        if (dataset.getUrlPath().equals("")) {
            throw new MotuException(
                    ErrorType.LOADING_CATALOG,
                    "Error in intializeProductFromOpendap - Invalid dataset branch - Opendap dataset has an empty url path");
        }

        String productId = "";
        String tdsUrlPath = "";
        boolean newProduct = true;

        if (dataset.getUrlPath() != null) {
            tdsUrlPath = dataset.getUrlPath();
        }

        if (dataset.getID() != null) {
            productId = dataset.getID();
        } else {
            productId = tdsUrlPath;
        }

        ProductMetaData productMetaData = null;

        Product product = cd.getProducts(productId);
        if (product == null) {
            product = new Product(useSSO);
            productMetaData = new ProductMetaData();
            productMetaData.setProductId(productId);
            productMetaData.setTdsUrlPath(tdsUrlPath);
        } else {
            newProduct = false;
            productMetaData = product.getProductMetaData();
        }

        cd.getProductsLoaded().add(productId);

        productMetaData.setProductType(cd.getCurrentProductType());

        List<String> productSubTypesWork = new ArrayList<String>();
        productSubTypesWork.addAll(cd.getCurrentProductSubTypes());

        productMetaData.setProductSubTypes(productSubTypesWork);
        productMetaData.setTitle(dataset.getName());

        // Loads href documentation of the dataset
        loadOpendapHrefDoc(dataset, productMetaData);

        product.setProductMetaData(productMetaData);

        StringBuffer locationData = new StringBuffer();
        locationData.append(cd.getUrlSite());
        locationData.append(dataset.getUrlPath());
        product.setLocationData(locationData.toString());

        if (newProduct) {
            putProducts(productMetaData.getProductId(), product, cd);
        }
        cd.getSameProductTypeDataset().add(product);

    }

    /**
     * Loads Opendap products documentations from Opendap catalog.
     * 
     * @param dataset dataset (from Opendap catalog) from which information will be loaded
     * @param productMetaData metadata in which to record href documentation
     */
    private void loadOpendapHrefDoc(Dataset dataset, ProductMetaData productMetaData) {
        List<Object> list = dataset.getServiceOrDocumentationOrMetadata();

        if (productMetaData.getDocumentations() == null) {
            productMetaData.setDocumentations(new ArrayList<DocMetaData>());
        }
        productMetaData.clearDocumentations();

        for (Iterator<Object> it = list.iterator(); it.hasNext();) {
            Object o = it.next();
            if (o == null) {
                continue;
            }
            if (!(o instanceof Service)) {
                continue;
            }
            Service service = (Service) o;

            String serviceName = service.getName();
            if (serviceName == null) {
                continue;
            }
            String base = service.getBase();
            if (base.equals("")) {
                continue;
            }
            DocMetaData docMetaData = new DocMetaData();
            docMetaData.setResource(base);
            docMetaData.setTitle(serviceName.toLowerCase());

            productMetaData.addDocumentations(docMetaData);
        }
    }

    private Catalog getCatalogFromOpenDap(CatalogService catalogService) throws MalformedURLException, IOException, MotuCasException, JAXBException {
        return getCatalogFromOpenDap(getCatalogURL(catalogService), catalogService.getCasAuthentication());
    }

    private Catalog getCatalogFromOpenDap(String catalogUrl, boolean useSSO_)
            throws MalformedURLException, IOException, MotuCasException, JAXBException {
        Catalog catalog = null;
        URL url = new URL(getUrlWithSSO(catalogUrl, useSSO_));
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        catalog = (Catalog) JAXBTDSModel.getInstance().getUnmarshallerTdsModel().unmarshal(in);
        in.close();
        return catalog;
    }

}
