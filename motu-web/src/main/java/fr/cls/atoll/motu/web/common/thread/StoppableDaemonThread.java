package fr.cls.atoll.motu.web.common.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class StoppableDaemonThread extends Thread {

    private static final Logger LOGGER = LogManager.getLogger();

    private boolean isDaemonStoppingASAP;
    private boolean isDaemonStopped;
    private long refreshDelayInMsec;

    public StoppableDaemonThread(String threadName, long refreshDelayInMs) {
        super(threadName);
        setDaemon(true);
        setDaemonStoppingASAP(false);
        setDaemonStopped(false);
        setRefreshDelayInMsec(refreshDelayInMs);
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        LOGGER.info("Start" + getName() + ", refresh period:" + getRefreshDelayInMSec() + "ms");
        while (!isDaemonStoppedASAP()) {

            // Business processing is done there
            runProcess();

            if (!isDaemonStoppedASAP()) {
                try {
                    sleep(getRefreshDelayInMSec());
                } catch (InterruptedException e) {
                    if (!isDaemonStoppedASAP()) {
                        LOGGER.error("Error during refresh of daemon" + getName(), e);
                    }
                }
            }
        }
        onThreadStopped();
    }

    /**
     * .
     */
    protected void runProcess() {
    }

    /**
     * Valeur de refreshDelay.
     * 
     * @return la valeur.
     */
    public long getRefreshDelayInMSec() {
        return refreshDelayInMsec;
    }

    /**
     * Valeur de refreshDelay.
     * 
     * @param refreshDelay nouvelle valeur.
     */
    public void setRefreshDelayInMsec(long refreshDelay) {
        this.refreshDelayInMsec = refreshDelay;
    }

    /**
     * .
     * 
     * @param b
     */
    public synchronized void setDaemonStoppingASAP(boolean isDaemonStoppingASAP_) {
        isDaemonStoppingASAP = isDaemonStoppingASAP_;
        // Unlock the sleep methods
        this.interrupt();
    }

    /**
     * Valeur de shallDaemonStoppedASAP.
     * 
     * @return la valeur.
     */
    public synchronized boolean isDaemonStoppedASAP() {
        return isDaemonStoppingASAP;
    }

    /**
     * Valeur de isDaemonStopped.
     * 
     * @return la valeur.
     */
    public boolean isDaemonStopped() {
        return isDaemonStopped;
    }

    /**
     * Valeur de isDaemonStopped.
     * 
     * @param isDaemonStopped nouvelle valeur.
     */
    public void setDaemonStopped(boolean isDaemonStopped) {
        this.isDaemonStopped = isDaemonStopped;
    }

    /**
     * .
     */
    public void onThreadStopped() {
        setDaemonStopped(true);
    }
}
