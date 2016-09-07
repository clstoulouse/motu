package fr.cls.atoll.motu.web.bll.messageserror;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.DALManager;

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
public class BLLMessagesErrorManager implements IBLLMessagesErrorManager {

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public String getMessageError(ErrorType errorCode) throws MotuException {
        return DALManager.getInstance().getMessagesErrorManager().getMessageError(errorCode);
    }

    @Override
    public String getMessageError(ErrorType errorCode, Exception e) throws MotuException {
        return DALManager.getInstance().getMessagesErrorManager().getMessageError(errorCode, e);
    }
}
