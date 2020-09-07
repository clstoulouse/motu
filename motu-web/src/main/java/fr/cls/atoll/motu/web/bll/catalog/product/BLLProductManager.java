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
    public Product getProduct(String configServiceName, String productId) {
        return BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().getProductCache().getProduct(configServiceName,
                                                                                                                            productId);
    }

    @Override
    public Product getProductFromLocation(String configServiceCatalogName, String URLPath) throws MotuException {
        Product productFound = null;
        ConfigService cFound = null;
        for (ConfigService c : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            cFound = c;
            String curConfigServiceCatalogName = c.getCatalog().getName();
            if (curConfigServiceCatalogName.equals(configServiceCatalogName)) {
                CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().getCatalogCache()
                        .getCatalog(c.getName());
                if (cd != null) {
                    productFound = cd.getProductsByTdsUrl(URLPath);
                    if (productFound != null) {
                        break;
                    }
                    // Map<String, Product> products = cd.getProducts();
                    // for (Product curP : products.values()){
                    // if (curP.getTdsUrlPath().equals(URLPath)) {
                    // productFound = curP;
                    // break;
                    // }
                    // }
                    // break;
                }
            }
        }

        Product pWithMetadata = null;
        if (cFound != null && productFound != null) {
            pWithMetadata = getProduct(cFound.getName(), productFound.getProductId());
        }
        return pWithMetadata != null ? pWithMetadata : productFound;
    }

    @Override
    public Product getProductFromLocation(String URLPath) throws MotuException {
        Product productFound = null;
        ConfigService cFound = null;
        for (ConfigService c : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            cFound = c;
            CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().getCatalogCache()
                    .getCatalog(c.getName());
            if (cd != null) {
                Map<String, Product> products = cd.getProducts();
                for (Map.Entry<String, Product> product : products.entrySet()) {
                    if (product.getValue().getTdsUrlPath().equals(URLPath)) {
                        // TDS Case
                        productFound = product.getValue();
                        break;
                    } else if (product.getValue().getLocationMetaData().equals(URLPath)) {
                        // DGF Case
                        productFound = product.getValue();
                        break;
                    }
                }
            }
        }

        Product pWithMetadata = null;
        if (cFound != null && productFound != null) {
            pWithMetadata = getProduct(cFound.getName(), productFound.getProductId());
        }
        return pWithMetadata != null ? pWithMetadata : productFound;
    }

    @Override
    public String datasetIdFromProductLocation(String locationData) {
        String patternExpression = "(http://.*thredds/)(dodsC/)(.*)";

        Pattern pattern = Pattern.compile(patternExpression);
        Matcher matcher = pattern.matcher(locationData);

        if (matcher.find()) {
            // TDS URL
            return matcher.group(matcher.groupCount());
        } else {
            // DGF URL
            return locationData;
        }
    }

    @Override
    public String getProductDownloadHttpUrl(String productFileName) {
        String productDownloadHttpUrl = BLLManager.getInstance().getConfigManager().getProductDownloadHttpUrl();
        if (!(productDownloadHttpUrl.endsWith("/"))) {
            productDownloadHttpUrl += "/";
        }
        productDownloadHttpUrl += productFileName;
        return productDownloadHttpUrl;
    }

    @Override
    public String getProductPhysicalFilePath(String productFileName) {
        String productFilePath = BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath();
        if (!(productFilePath.endsWith("/"))) {
            productFilePath += "/";
        }
        productFilePath += productFileName;
        return productFilePath;
    }
}
