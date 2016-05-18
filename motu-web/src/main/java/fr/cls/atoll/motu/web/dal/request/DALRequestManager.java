package fr.cls.atoll.motu.web.dal.request;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.ExtractionParameters;
import fr.cls.atoll.motu.web.dal.request.netcdf.ProductDeferedExtractNetcdfThread;

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
public class DALRequestManager implements IDALRequestManager {

    /**
     * Product defered extract netcdf.
     * 
     * @param organizer the organizer
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     * 
     * @return the status mode response
     * 
     * @throws MotuException the motu exception
     */
    @Override
    public void processRequest(StatusModeResponse statusModeResponse, ExtractionParameters extractionParameters) {
        new ProductDeferedExtractNetcdfThread(statusModeResponse, extractionParameters).start();
    }
}
