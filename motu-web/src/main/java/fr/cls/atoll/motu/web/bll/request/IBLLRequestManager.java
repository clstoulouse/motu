package fr.cls.atoll.motu.web.bll.request;

import java.util.List;

import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.request.model.ProductResult;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.bll.request.queueserver.IQueueServerManager;
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
    List<Long> getRequestIds();

    /**
     * .
     * 
     * @param requestId
     * @return
     */
    RequestDownloadStatus getDownloadRequestStatus(Long requestId_);

    /**
     * Return the status of the request associated with the provided request id.
     * 
     * @param requestId_ The id of the request
     * @return The status of the request.
     */
    StatusModeType getRequestStatus(Long requestId_);

    /**
     * Return the action of the request associated with the provided request id.
     * 
     * @param requestId_ The id of the request
     * @return The action of the request.
     */
    AbstractAction getRequestAction(Long requestId_);

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
    ProductResult download(ConfigService cs_, RequestProduct product_, AbstractAction action);

    /**
     * .
     * 
     * @param cs_
     * @param product_
     * @param extractionParameters
     * @return
     */
    long downloadAsynchonously(ConfigService cs_, RequestProduct product_, AbstractAction action);

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
    void deleteRequest(Long requestId);

    /**
     * Initialize the maps which manage the requests.
     * 
     * @param action The action object associated to the request
     * @return The computed id of the current new request.
     */
    Long initRequest(AbstractAction action);

    /**
     * Sets the new status of the request associated with the provided request Id.
     * 
     * @param requestId The id of the request to update the status
     * @param status The new status of the request.
     */
    void setActionStatus(Long requestId, StatusModeType status);

    /**
     * .
     */
    void stop();
}
