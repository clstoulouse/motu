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
     * @param catalogCache_ The instance of catalog cache that needs to be update by the service
     * @param productCache_ The instance of product cache that needs to be update by the service
     */
    public CacheUpdateService(ICatalogCache catalogCache_, IProductCache productCache_) {
        catalogCache = catalogCache_;
        productCache = productCache_;
    }

    /**
     * Execute the update of the provided ConfigService.
     * 
     * @param configServiceToUpdate The ConfigService that needs to be updated
     */
    public boolean updateConfigService(ConfigService configServiceToUpdate) {
        boolean doneWithoutAnyIsse = true;
        try {
            CatalogData cd = DALManager.getInstance().getCatalogManager().getCatalogData(configServiceToUpdate);
            if (cd != null) {
                catalogCache.putCatalog(configServiceToUpdate.getName(), cd);
                Map<String, Product> products = cd.getProducts();
                for (Map.Entry<String, Product> currentProductEntry : products.entrySet()) {
                    Product currentProduct = currentProductEntry.getValue();

                    try {
                        DALManager.getInstance().getCatalogManager().getProductManager()
                                .updateMetadata(BLLManager.getInstance().getCatalogManager().getCatalogType(configServiceToUpdate),
                                                currentProduct.getProductId(),
                                                currentProduct.getLocationData(),
                                                currentProduct.getProductMetaData());
                        productCache.setProduct(configServiceToUpdate.getName(), currentProduct);
                    } catch (Exception e) {
                        doneWithoutAnyIsse = false;
                        LOGGER.error("Error during refresh of the describe product cache, config service=" + configServiceToUpdate.getName()
                                + ", productId=" + currentProduct.getProductId(), e);
                    }
                }
            } else {
                doneWithoutAnyIsse = false;
                LOGGER.error("Unable to read catalog data for config service " + configServiceToUpdate.getName());
            }
        } catch (MotuException e) {
            doneWithoutAnyIsse = false;
            LOGGER.error("Error during refresh of the describe product cache", e);
        }
        return doneWithoutAnyIsse;
    }
}
