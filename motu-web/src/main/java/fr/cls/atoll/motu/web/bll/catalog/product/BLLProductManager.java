package fr.cls.atoll.motu.web.bll.catalog.product;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public ProductMetaData getProductMetaData(String catalogType, String productId, String locationData) throws MotuException {
        return DALManager.getInstance().getCatalogManager().getProductManager()
                .getMetadata(catalogType,
                             productId,
                             locationData,
                             BLLManager.getInstance().getConfigManager().getMotuConfig().getUseAuthentication());
    }

    @Override
    public Product getProductFromLocation(String catalogName, String URLPath) throws MotuException {
        Product productFound = null;
        for (ConfigService c : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            String currentCatalogName = c.getCatalog().getName();
            if (currentCatalogName.equals(catalogName)) {
                CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(c);
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

        return productFound;
    }

    @Override
    public Product getProductFromLocation(String URLPath) throws MotuException {
        Product productFound = null;
        for (ConfigService c : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(c);
            Map<String, Product> products = cd.getProducts();
            for (Map.Entry<String, Product> product : products.entrySet()) {
                if (product.getValue().getTdsUrlPath().equals(URLPath)) {
                    productFound = product.getValue();
                    break;
                }
            }
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

    @Override
    public String datasetIdFromProductLocation(String locationData) {
        String patternExpression = "(http://.*thredds/)(dodsC/)(.*)";

        Pattern pattern = Pattern.compile(patternExpression);
        Matcher matcher = pattern.matcher(locationData);

        matcher.find();

        return matcher.group(matcher.groupCount());
    }

}
