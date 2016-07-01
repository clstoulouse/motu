package fr.cls.atoll.motu.web.bll.catalog.product;

import java.util.Map;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;

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
public class BLLProductManager implements IBLLProductManager {

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public ProductMetaData getProductMetaData(String productId, String locationData) throws MotuException {
        return DALManager.getInstance().getCatalogManager().getProductManager()
                .getMetadata(productId, locationData, BLLManager.getInstance().getConfigManager().getMotuConfig().getUseAuthentication());
    }

    @Override
    public Product getProductFromLocation(String catalogName, String URLPath) throws MotuException {
        Product productFound = null;
        for (ConfigService c : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            String currentCatalogName = c.getCatalog().getName();
            // System.out.println("CatalogName : " + currentCatalogName);
            // System.out.println("providedCatalogName : " + catalogName);
            if (currentCatalogName.equals(catalogName)) {
                CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(c);
                // System.out.println("DataSetName : " + datasetName);
                Map<String, Product> products = cd.getProducts();
                for (Map.Entry<String, Product> product : products.entrySet()) {
                    if (product.getValue().getTdsUrlPath().equals(URLPath)) {
                        productFound = product.getValue();
                        break;
                    }
                }
                // System.out.println("MyProduct : " + myProduct);
                // System.out.println("Product Id : " + myProduct.getProductId());
                // System.out.println("Location Data : " + myProduct.getLocationData());
                break;
            }
        }

        return productFound;
    }

    public Product getProductFromLocation(String URLPath) throws MotuException {
        Product productFound = null;
        for (ConfigService c : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            // System.out.println("CatalogName : " + currentCatalogName);
            // System.out.println("providedCatalogName : " + catalogName);
            CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(c);
            // System.out.println("DataSetName : " + datasetName);
            Map<String, Product> products = cd.getProducts();
            for (Map.Entry<String, Product> product : products.entrySet()) {
                if (product.getValue().getTdsUrlPath().equals(URLPath)) {
                    productFound = product.getValue();
                    break;
                }
            }
            // System.out.println("MyProduct : " + myProduct);
            // System.out.println("Product Id : " + myProduct.getProductId());
            // System.out.println("Location Data : " + myProduct.getLocationData());
        }

        return productFound;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public Product getProduct(String serviceName, String productId) throws MotuException {
        ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(serviceName);
        CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(cs);
        Product p = cd.getProducts().get(productId);

        return p;
    }

}
