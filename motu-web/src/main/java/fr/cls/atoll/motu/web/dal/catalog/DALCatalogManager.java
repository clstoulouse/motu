package fr.cls.atoll.motu.web.dal.catalog;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.catalog.file.FileCatalogLoader;
import fr.cls.atoll.motu.web.dal.catalog.opendap.OpenDapCatalogReader;
import fr.cls.atoll.motu.web.dal.catalog.product.DALProductManager;
import fr.cls.atoll.motu.web.dal.catalog.product.IDALProductManager;
import fr.cls.atoll.motu.web.dal.catalog.tds.JAXBTDSModel;
import fr.cls.atoll.motu.web.dal.catalog.tds.TDSCatalogLoader;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;

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
            throw new MotuException(ErrorType.LOADING_CATALOG, "Error while initializing JAXB with TDS model", e);
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
            throw new MotuException(ErrorType.LOADING_CATALOG, String.format("Unknown catalog type %d ", cs.getCatalog().getType()));
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

}
