package fr.cls.atoll.motu.web.bll.request;

import java.util.List;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.bll.request.model.ProductResult;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.bll.request.queueserver.IQueueServerManager;
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
public interface IBLLRequestManager {

    /**
     * .
     * 
     * @return
     */
    List<Long> getRequestIds();

    /**
     * .
     * 
     * @param requestId
     * @return
     */
    RequestDownloadStatus getResquestStatus(Long requestId_);

    /**
     * .
     * 
     * @return
     */
    long getNewRequestId();

    /**
     * Return the QueueServerManagement object
     * 
     * @return The QueueServerManagement Object.
     */
    IQueueServerManager getQueueServerManager();

    /**
     * .
     * 
     * @param cs_
     * @param product_
     * @param extractionParameters
     * @return
     */
    ProductResult download(ConfigService cs_, Product product_, ExtractionParameters extractionParameters);

    /**
     * .
     * 
     * @param cs_
     * @param product_
     * @param extractionParameters
     * @return
     */
    long downloadAsynchonously(ConfigService cs_, Product product_, ExtractionParameters extractionParameters);

    /**
     * Delete the files associated to the provided URL. .
     * 
     * @param urls url list of the files to delete
     * @return The status of the deletion for each provided files.
     */
    boolean[] deleteFiles(String[] urls);

    /**
     * .
     * 
     * @param product
     * @return
     * @throws MotuExceptionBase
     */
    double getProductMaxAllowedDataSizeIntoByte(Product product) throws MotuException;

    /**
     * .
     * 
     * @param product
     * @param listVar
     * @param listTemporalCoverage
     * @param listLatLongCoverage
     * @param listDepthCoverage
     * @return
     * @throws MotuExceptionBase
     */
    double getProductDataSizeIntoByte(Product product,
                                      List<String> listVar,
                                      List<String> listTemporalCoverage,
                                      List<String> listLatLongCoverage,
                                      List<String> listDepthCoverage) throws MotuException;

    /**
     * .
     * 
     * @throws MotuException
     */
    void init() throws MotuException;
}
