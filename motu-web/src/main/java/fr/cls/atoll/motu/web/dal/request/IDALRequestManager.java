package fr.cls.atoll.motu.web.dal.request;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
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
public interface IDALRequestManager {

    /**
     * .
     * 
     * @param cs
     * @param p
     * @param dataOutputFormat
     * @throws MotuException
     */
    void downloadProduct(ConfigService cs, Product p, OutputFormat dataOutputFormat) throws MotuException;
}
