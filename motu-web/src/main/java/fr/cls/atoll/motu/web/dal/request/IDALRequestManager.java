package fr.cls.atoll.motu.web.dal.request;

import java.io.IOException;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.status.IDALRequestStatusManager;
import fr.cls.atoll.motu.web.dal.tds.ncss.NetCdfSubsetService;

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

    void stop();

    void init() throws MotuException;

    /**
     * .
     * 
     * @param cs
     * @param p
     * @param dataOutputFormat
     * @throws MotuException
     */
    void downloadProduct(ConfigService cs, RequestDownloadStatus rds) throws MotuException;

    /**
     * .
     * 
     * @param p
     * @param ncss
     * @throws MotuInvalidDepthRangeException
     * @throws NetCdfVariableException
     * @throws MotuException
     * @throws IOException
     * @throws InterruptedException
     */
    void ncssRequest(RequestDownloadStatus rds, NetCdfSubsetService ncss)
            throws MotuInvalidDepthRangeException, NetCdfVariableException, MotuException, IOException, InterruptedException;

    IDALRequestStatusManager getDalRequestStatusManager();
}
