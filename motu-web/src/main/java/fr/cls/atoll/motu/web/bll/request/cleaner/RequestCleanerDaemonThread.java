package fr.cls.atoll.motu.web.bll.request.cleaner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.common.thread.StoppableDaemonThread;

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
public class RequestCleanerDaemonThread extends StoppableDaemonThread {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();
    private IRequestCleaner requestCleaner;

    public RequestCleanerDaemonThread() {
        super("Request cleaner daemon", BLLManager.getInstance().getConfigManager().getMotuConfig().getRunCleanInterval() * 60 * 1000);
        requestCleaner = new RequestCleaner();
    }

    /**
     * {@inheritDoc}
     * 
     * Overwrite because motu configuration paramater can be updated
     */
    @Override
    public long getRefreshDelayInMSec() {
        return BLLManager.getInstance().getConfigManager().getMotuConfig().getRunCleanInterval() * 60 * 1000;
    }

    /** {@inheritDoc} */
    @Override
    public void runProcess() {
        if (!isDaemonStoppedASAP()) {
            LOGGER.info("RequestCleanerDaemonThread triggered: cleanRequestStatus, cleanExtractedFile, cleanJavaTempFile");
            requestCleaner.cleanRequestStatus();
            requestCleaner.cleanExtractedFile();
            requestCleaner.cleanJavaTempFile();
        }
    }

}
