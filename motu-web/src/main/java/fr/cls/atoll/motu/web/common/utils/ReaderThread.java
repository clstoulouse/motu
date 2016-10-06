package fr.cls.atoll.motu.web.common.utils;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;

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
public abstract class ReaderThread implements Runnable {

    private BufferedReader reader = null;

    private static final Logger LOGGER = LogManager.getLogger();

    public ReaderThread(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {
        if (reader != null) {
            try {
                String line = "";
                try {
                    while ((line = reader.readLine()) != null) {
                        treatement(line);
                    }
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                LOGGER.error(ErrorType.SYSTEM, e);
            }
        }
    }

    public abstract void treatement(String data);

}
