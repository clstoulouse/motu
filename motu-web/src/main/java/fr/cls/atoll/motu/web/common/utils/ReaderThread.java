package fr.cls.atoll.motu.web.common.utils;

import java.io.BufferedReader;
import java.io.IOException;

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
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public abstract void treatement(String data);

}
