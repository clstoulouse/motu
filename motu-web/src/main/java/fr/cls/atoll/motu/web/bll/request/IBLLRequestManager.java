package fr.cls.atoll.motu.web.bll.request;

import java.util.List;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;

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
    StatusModeResponse getResquestStatus(Long requestId_);

    /**
     * .
     * 
     * @return
     */
    long getNewRequestId();

    /**
     * .
     * 
     * @param extractionParameters
     * @return
     */
    StatusModeResponse processRequest(ExtractionParameters extractionParameters);

}
