package fr.cls.atoll.motu.web.dal.request.cdo;

import java.io.IOException;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.dal.request.IDALRequestManager;
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
public interface ICDOManager {

    /**
     * .
     */
    void stop();

    /**
     * .
     * 
     * @param p
     * @param ncss
     * @param latlon
     * @param extractDirPath
     * @param fname
     * @param dalRequestManager
     * @throws IOException
     * @throws MotuInvalidDepthRangeException
     * @throws NetCdfVariableException
     * @throws MotuException
     * @throws InterruptedException
     * @throws Exception
     */
    void runRequestWithCDOMergeTool(RequestProduct requestProduct,
                                    NetCdfSubsetService ncss,
                                    ExtractCriteriaLatLon latlon,
                                    String extractDirPath,
                                    String fname,
                                    IDALRequestManager dalRequestManager) throws Exception;

}
