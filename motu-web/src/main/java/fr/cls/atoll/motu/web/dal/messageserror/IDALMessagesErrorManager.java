package fr.cls.atoll.motu.web.dal.messageserror;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;

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
public interface IDALMessagesErrorManager {

    public static final String MESSAGES_ERROR_FILE_NAME = "MessagesError.properties";
    public static final ErrorType SYSTEM_ERROR_CODE = ErrorType.SYSTEM;
    public static final String SYSTEM_ERROR_MESSAGE = "A system error happend. Please contact the administrator of the site.";

    /**
     * .
     * 
     * @throws MotuException
     */
    void init() throws MotuException;

    /**
     * .
     * 
     * @return
     */
    String getMessageError(ErrorType errorCode) throws MotuException;
}
