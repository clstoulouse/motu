package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.thread.StoppableDaemonThread;
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
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class ProductCacheThread extends StoppableDaemonThread {

    private static final Logger LOGGER = LogManager.getLogger();

    private ProductCacheManager describeProductCacheManager;

    /**
     * Constructeur.
     */
    public ProductCacheThread(ProductCacheManager describeProductCacheManager_) {
        super(
            "DescribeProduct Cache Thread Daemon",
            BLLManager.getInstance().getConfigManager().getMotuConfig().getDescribeProductCacheRefreshInMilliSec());
        describeProductCacheManager = describeProductCacheManager_;
    }

    /** {@inheritDoc} */
    @Override
    public void runProcess() {
        long startRefresh = System.currentTimeMillis();
        List<ConfigService> services = BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService();
        int i = 0;
        while (!isDaemonStoppedASAP() && i < services.size()) {
            ConfigService configService = services.get(i);
            processConfigService(configService);
            i++;
        }
        LOGGER.info("Describe product cache refreshed in "
                + fr.cls.atoll.motu.web.common.utils.DateUtils.getDurationMinSecMsec(System.currentTimeMillis() - startRefresh));
    }

    /**
     * .
     * 
     * @param configService
     */
    private void processConfigService(ConfigService configService) {
        try {
            CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(configService);
            if (cd != null) {
                Map<String, Product> products = cd.getProducts();
                for (Map.Entry<String, Product> currentProductEntry : products.entrySet()) {
                    Product currentProduct = currentProductEntry.getValue();

                    ProductMetaData pmd = DALManager.getInstance().getCatalogManager().getProductManager()
                            .getMetadata(BLLManager.getInstance().getCatalogManager().getCatalogType(configService),
                                         currentProduct.getProductId(),
                                         currentProduct.getLocationData(),
                                         BLLManager.getInstance().getConfigManager().getMotuConfig().getUseAuthentication());

                    if (pmd != null) {
                        currentProduct.setProductMetaData(pmd);
                    }
                    describeProductCacheManager.setProduct(currentProduct);
                }
            } else {
                LOGGER.error("Unable to read catalog data for config service " + configService.getName());
            }
        } catch (MotuException e) {
            LOGGER.error("Error during refresh of the describe product cache", e);
        }
    }

}
