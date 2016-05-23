package fr.cls.atoll.motu.web.dal.request;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.ExtractionParameters;

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
     * @param statusModeResponse
     * @param organizer
     * @param extractionParameters
     * @throws MotuException
     */
    void processRequest(StatusModeResponse statusModeResponse, ExtractionParameters extractionParameters);

    /**
     * Retrieve the Size of the provided Product.
     * 
     * @return
     */
    double processProductDataSizeRequest(ExtractionParameters extractionParameters);

}
