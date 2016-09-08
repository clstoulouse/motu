package fr.cls.atoll.motu.web.dal.config.version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;

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
public class DALVersionManager implements IDALVersionManager {

    private String readVersion(String filePath_) throws MotuException {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath_)));
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, "", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public String getDistributionVersion() throws MotuException {
        return readVersion(BLLManager.getInstance().getConfigManager().getMotuConfigurationFolderPath() + "/../" + "version-distribution.txt");
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public String getConfigurationVersion() throws MotuException {
        return readVersion(BLLManager.getInstance().getConfigManager().getMotuConfigurationFolderPath() + "/version-configuration.txt");
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public String getProductsVersion() throws MotuException {
        return readVersion(BLLManager.getInstance().getConfigManager().getMotuConfigurationFolderPath() + "/../products/" + "version-products.txt");
    }

}
