package fr.cls.atoll.motu.web.common.utils;

import java.io.BufferedReader;

import org.apache.logging.log4j.Logger;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class ProcessOutputLogguer extends ReaderThread {

    private Logger logger;

    private Type type = Type.INFO;

    public enum Type {
        INFO, ERROR
    }

    /**
     * Constructeur.
     * 
     * @param reader
     */
    public ProcessOutputLogguer(BufferedReader reader, Logger logger, Type type) {
        super(reader);
        this.logger = logger;
        this.type = type;
    }

    /** {@inheritDoc} */
    @Override
    public void treatement(String data) {
        switch (type) {
        case INFO:
            logger.info(data);
            break;
        case ERROR:
            logger.error(data);
            break;
        default:
            break;
        }
    }
}
