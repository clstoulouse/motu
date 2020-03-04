package fr.cls.atoll.motu.web.bll.request;

import java.util.Set;

import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.request.model.ProductResult;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.bll.request.queueserver.IQueueServerManager;
import fr.cls.atoll.motu.web.bll.request.status.IBLLRequestStatusManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.request.actions.AbstractAction;

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
    Set<String> getRequestIds();

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
     * @throws MotuException
     */
    ProductResult download(ConfigService cs, RequestProduct product, AbstractAction action) throws MotuException;

    /**
     * .
     * 
     * @param cs_
     * @param product_
     * @param extractionParameters
     * @return
     * @throws MotuException
     */
    RequestDownloadStatus downloadAsynchronously(ConfigService cs, RequestProduct product, AbstractAction action) throws MotuException;

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
    double getProductDataSizeIntoByte(RequestProduct requestProduct) throws MotuException;

    /**
     * .
     * 
     * @throws MotuException
     */
    void init() throws MotuException;

    /**
     * .
     * 
     * @param requestId
     */
    void deleteRequest(String requestId);

    /**
     * Initialize the maps which manage the requests.
     * 
     * @param action The action object associated to the request
     * @return The computed id of the current new request.
     * @throws MotuException
     */
    String initRequest(AbstractAction action) throws MotuException;

    /**
     * Sets the new status of the request associated with the provided request Id.
     * 
     * @param requestId The id of the request to update the status
     * @param status The new status of the request.
     */
    void setActionStatus(String requestId, StatusModeType status);

    /**
     * .
     */
    void stop();

    IBLLRequestStatusManager getBllRequestStatusManager();
}
