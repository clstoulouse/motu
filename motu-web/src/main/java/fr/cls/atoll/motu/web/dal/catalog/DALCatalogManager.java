package fr.cls.atoll.motu.web.dal.catalog;

import java.io.File;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.catalog.file.FileCatalogLoader;
import fr.cls.atoll.motu.web.dal.catalog.opendap.OpenDapCatalogReader;
import fr.cls.atoll.motu.web.dal.catalog.product.DALProductManager;
import fr.cls.atoll.motu.web.dal.catalog.product.IDALProductManager;
import fr.cls.atoll.motu.web.dal.catalog.tds.JAXBTDSModel;
import fr.cls.atoll.motu.web.dal.catalog.tds.TDSCatalogLoader;
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
public class DALCatalogManager implements IDALCatalogManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private IDALProductManager dalProductManager;

    public DALCatalogManager() {
        dalProductManager = new DALProductManager();
    }

    /** {@inheritDoc} */
    @Override
    public void init() throws MotuException {
        try {
            JAXBTDSModel.getInstance().init();
        } catch (JAXBException e) {
            throw new MotuException("Error while initializing JAXB with TDS model", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public CatalogData getCatalogData(ConfigService cs) throws MotuException {
        CatalogData cd = null;
        switch (BLLManager.getInstance().getCatalogManager().getCatalogType(cs).toUpperCase()) {
        case "OPENDAP":
            cd = new OpenDapCatalogReader().loadOpendapCatalog(cs.getCatalog());
            break;
        case "TDS":
            cd = new TDSCatalogLoader().loadTdsCatalog(cs.getCatalog());
            break;
        case "FILE":
            cd = new FileCatalogLoader().loadFtpCatalog(cs.getCatalog().getUrlSite() + File.separator + cs.getCatalog().getName());
            break;
        default:
            throw new MotuException(String.format("Unknown catalog type %d ", cs.getCatalog().getType()));
        }
        return cd;
    }

    /**
     * Valeur de dalProductManager.
     * 
     * @return la valeur.
     */
    @Override
    public IDALProductManager getProductManager() {
        return dalProductManager;
    }

    @Override
    public String getCatalogType(Product product) throws MotuException {
        ConfigService serviceFound = null;

        String locationData = product.getLocationData();

        if (!StringUtils.isNullOrEmpty(locationData)) {
            for (ConfigService c : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
                CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(c);
                Map<String, Product> products = cd.getProducts();
                for (Map.Entry<String, Product> currentProduct : products.entrySet()) {
                    if (currentProduct.getValue().getProductId().equals(product.getProductId())) {
                        serviceFound = c;
                        break;
                    }
                }
                if (serviceFound != null) {
                    break;
                }
            }
        }
        return getCatalogType(serviceFound);
    }

    @Override
    public String getCatalogType(ConfigService service) throws MotuException {
        String catalogType = service.getCatalog().getType().toUpperCase();
        // This is for retrocompatibility with the motu version anterior to 3.0
        // The catalog type FTP is left and only FILE is used even if FTP is set as catalog type
        if ("FTP".equals(catalogType.toUpperCase())) {
            catalogType = "FILE";
        }

        return catalogType.toUpperCase();
    }

}
