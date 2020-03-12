package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

/**
 * Provide the method used to retrieve the data needed to refresh the cache of teh catalog and the product .
 * <br>
 * <br>
 * Copyright : Copyright (c) 2017 <br>
 * <br>
 * Company : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1456 $ - $Date: 2011-04-08 18:37:34 +0200 $
 */
public class CacheUpdateService {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The cache of the catalog
     */
    private ICatalogCache catalogCache;
    /**
     * The cache of the product
     */
    private IProductCache productCache;

    /**
     * Initialize the service instance.
     * 
     * @param catalogCache The instance of catalog cache that needs to be update by the service
     * @param productCache The instance of product cache that needs to be update by the service
     */
    public CacheUpdateService(ICatalogCache catalogCache, IProductCache productCache) {
        this.catalogCache = catalogCache;
        this.productCache = productCache;
    }

    /**
     * Execute the update of the provided ConfigService.
     * 
     * @param configServiceToUpdate The ConfigService that needs to be updated
     */
    public boolean updateConfigService(ConfigService configServiceToUpdate) {
        boolean doneWithoutAnyIssue = true;
        try {
            // The getCatalogData prepare new the CatalogData with new Product and ProductMetaData
            CatalogData cd = DALManager.getInstance().getCatalogManager().getCatalogData(configServiceToUpdate);
            if (cd != null) {
                catalogCache.putCatalog(configServiceToUpdate.getName(), cd);
                Map<String, Product> products = cd.getProducts();
                for (Map.Entry<String, Product> currentProductEntry : products.entrySet()) {
                    Product currentProduct = currentProductEntry.getValue();
                    doneWithoutAnyIssue = updateProduct(configServiceToUpdate, currentProduct);
                }
            } else {
                doneWithoutAnyIssue = false;
                LOGGER.error("Unable to read catalog data for config service " + configServiceToUpdate.getName());
            }
        } catch (MotuException e) {
            doneWithoutAnyIssue = false;
            LOGGER.error("Error during refresh of the describe product cache", e);
        }
        return doneWithoutAnyIssue;
    }

    private boolean updateProduct(ConfigService configServiceToUpdate, Product currentProduct) {
        try {
            DALManager.getInstance().getCatalogManager().getProductManager()
                    .updateMetadata(BLLManager.getInstance().getCatalogManager().getCatalogType(configServiceToUpdate),
                                    currentProduct.getProductId(),
                                    currentProduct.getLocationData(),
                                    currentProduct.getProductMetaData());
            Product oldProduct = productCache.getProduct(configServiceToUpdate.getName(), currentProduct.getProductId());
            currentProduct.finalizeCreation(oldProduct);
            productCache.setProduct(configServiceToUpdate.getName(), currentProduct);
        } catch (Exception e) {
            LOGGER.error("Error during refresh of the describe product cache, config service=" + configServiceToUpdate.getName() + ", productId="
                    + currentProduct.getProductId(), e);
            return false;
        }
        return true;
    }

}
