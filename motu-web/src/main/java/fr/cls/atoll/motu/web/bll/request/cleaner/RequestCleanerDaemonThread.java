package fr.cls.atoll.motu.web.bll.request.cleaner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;

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
public class RequestCleanerDaemonThread extends Thread {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();
    private long runCleanIntervalInMs;
    private IRequestCleaner requestCleaner;

    public RequestCleanerDaemonThread() {
        super("Request cleaner daemon");
        setDaemon(true);
        runCleanIntervalInMs = BLLManager.getInstance().getConfigManager().getMotuConfig().getRunCleanInterval() * 60 * 1000;
        requestCleaner = new RequestCleaner();
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        LOGGER.info("Start request cleaner daemon thread, trigerred each " + runCleanIntervalInMs + "ms");
        while (true) {
            try {
                Thread.sleep(runCleanIntervalInMs);
            } catch (InterruptedException e) {
                LOGGER.error("Error while waiting RequestCleanerDaemonThread", e);
            }

            LOGGER.info("RequestCleanerDaemonThread triggered: cleanRequestStatus, cleanExtractedFile, cleanJavaTempFile");
            requestCleaner.cleanRequestStatus();
            requestCleaner.cleanExtractedFile();
            requestCleaner.cleanJavaTempFile();
        }
    }

}
