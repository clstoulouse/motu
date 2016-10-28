package fr.cls.atoll.motu.web.bll.catalog.product;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

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

    @Override
    public Product getProduct(String productId) {
        return BLLManager.getInstance().getCatalogManager().getProductCacheManager().getProduct(productId);
    }

    @Override
    public Product getProductFromLocation(String catalogName, String URLPath) throws MotuException {
        Product productFound = null;
        for (ConfigService c : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            String currentCatalogName = c.getCatalog().getName();
            if (currentCatalogName.equals(catalogName)) {
                CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(c);
                if (cd != null) {
                    Map<String, Product> products = cd.getProducts();
                    for (Map.Entry<String, Product> product : products.entrySet()) {
                        if (product.getValue().getTdsUrlPath().equals(URLPath)) {
                            productFound = product.getValue();
                            break;
                        }
                    }
                    break;
                }
            }
        }

        Product pWithMetadata = getProduct(productFound.getProductId());
        return pWithMetadata != null ? pWithMetadata : productFound;
    }

    @Override
    public Product getProductFromLocation(String URLPath) throws MotuException {
        Product productFound = null;
        for (ConfigService c : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(c);
            if (cd != null) {
                Map<String, Product> products = cd.getProducts();
                for (Map.Entry<String, Product> product : products.entrySet()) {
                    if (product.getValue().getTdsUrlPath().equals(URLPath)) {
                        productFound = product.getValue();
                        break;
                    }
                }
            }
        }

        Product pWithMetadata = getProduct(productFound.getProductId());
        return pWithMetadata != null ? pWithMetadata : productFound;
    }

    @Override
    public String datasetIdFromProductLocation(String locationData) {
        String patternExpression = "(http://.*thredds/)(dodsC/)(.*)";

        Pattern pattern = Pattern.compile(patternExpression);
        Matcher matcher = pattern.matcher(locationData);

        return matcher.find() ? matcher.group(matcher.groupCount()) : null;
    }

    @Override
    public String getProductDownloadHttpUrl(String productFileName_) {
        String productDownloadHttpUrl = BLLManager.getInstance().getConfigManager().getProductDownloadHttpUrl();
        if (!(productDownloadHttpUrl.endsWith("/"))) {
            productDownloadHttpUrl += "/";
        }
        productDownloadHttpUrl += productFileName_;
        return productDownloadHttpUrl;
    }

}
