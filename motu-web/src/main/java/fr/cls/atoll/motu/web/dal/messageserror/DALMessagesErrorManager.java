package fr.cls.atoll.motu.web.dal.messageserror;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class DALMessagesErrorManager implements IDALMessagesErrorManager {

    Properties messagesError = null;

    private static final Logger LOGGER = LogManager.getLogger();

    /** {@inheritDoc} */
    @Override
    public void init() throws MotuException {
        try {
            initMessagesError();
        } catch (FileNotFoundException e) {
            throw new MotuException(ErrorType.LOADING_MESSAGE_ERROR, "Error while initializing Messages Error: ", e);
        }
    }

    private void initMessagesError() throws MotuException, FileNotFoundException {
        File fMotuConfig = new File(DALManager.getInstance().getConfigManager().getMotuConfigurationFolderPath(), MESSAGES_ERROR_FILE_NAME);
        InputStream in = null;
        if (fMotuConfig.exists()) {
            in = new FileInputStream(fMotuConfig);
        } else {
            in = DALMessagesErrorManager.class.getClassLoader().getResourceAsStream(MESSAGES_ERROR_FILE_NAME);
        }

        try {
            messagesError = new Properties();
            messagesError.load(in);
        } catch (Exception e) {
            throw new MotuException(ErrorType.LOADING_MESSAGE_ERROR, "Impossible to load the MessagesError.properties file", e);
        }

        if (messagesError == null) {
            throw new MotuException(ErrorType.LOADING_MESSAGE_ERROR, "Impossible to load the MessagesError.properties file");
        }

        try {
            in.close();
        } catch (IOException io) {
            // Do nothing
        }
    }

    @Override
    public String getMessageError(ErrorType errorCode) throws MotuException {
        return getMessageError(errorCode, null);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public String getMessageError(ErrorType errorCode, Exception e) throws MotuException {
        if (SYSTEM_ERROR_CODE.equals(errorCode)) {
            return SYSTEM_ERROR_MESSAGE;
        } else {
            if (messagesError != null) {
                String messageError = messagesError.getProperty(String.valueOf(errorCode.value()));
                if (e != null) {
                    messageError = MessageFormat.format(messageError, e.getMessage());
                }
                if (messageError == null) {
                    throw new MotuException(
                            ErrorType.LOADING_MESSAGE_ERROR,
                            "Impossible to find the error code " + errorCode + " into the file " + MESSAGES_ERROR_FILE_NAME);
                }
                return messageError;
            } else {
                throw new MotuException(ErrorType.LOADING_MESSAGE_ERROR, "The messages error file reader is not initialized");
            }
        }
    }
}
