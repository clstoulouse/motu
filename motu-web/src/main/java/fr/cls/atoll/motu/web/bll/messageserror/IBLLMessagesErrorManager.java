package fr.cls.atoll.motu.web.bll.messageserror;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.messageserror.IDALMessagesErrorManager;

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
public interface IBLLMessagesErrorManager {

    public static final ErrorType SYSTEM_ERROR_CODE = IDALMessagesErrorManager.SYSTEM_ERROR_CODE;
    public static final String SYSTEM_ERROR_MESSAGE = IDALMessagesErrorManager.SYSTEM_ERROR_MESSAGE;

    /**
     * .
     * 
     * @return
     */
    String getMessageError(ErrorType errorCode) throws MotuException;

    /**
     * .
     * 
     * @param errorCode
     * @param e
     * @return
     * @throws MotuException
     */
    String getMessageError(ErrorType errorCode, Exception e) throws MotuException;

    /**
     * .
     * 
     * @param errorCode
     * @param messageArguments
     * @return
     * @throws MotuException
     */
    String getMessageError(ErrorType errorCode, Object... messageArguments) throws MotuException;
}
