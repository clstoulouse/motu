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
    private boolean isWaitingBeforeRunProcess;

    public StoppableDaemonThread(String threadName, long refreshDelayInMs) {
        this(threadName, refreshDelayInMs, false);
    }

    public StoppableDaemonThread(String threadName, long refreshDelayInMs, boolean isWaitingBeforeRunProcess) {
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

            if (isWaitingBeforeRunProcess()) {
                runWait();
            }

            // Business processing is done there
            runProcess();

            if (!isWaitingBeforeRunProcess()) {
                runWait();
            }
        }
        onThreadStopped();
    }

    private void runWait() {
        if (!isDaemonStoppedASAP()) {
            try {
                synchronized (this) {
                    wait(getRefreshDelayInMSec());
                }
            } catch (InterruptedException e) {
                LOGGER.error("Error during refresh of daemon" + getName(), e);
            }
        }
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
        notify();
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

    public boolean isWaitingBeforeRunProcess() {
        return isWaitingBeforeRunProcess;
    }

    public void setWaitingBeforeRunProcess(boolean isWaitingBeforeRunProcess) {
        this.isWaitingBeforeRunProcess = isWaitingBeforeRunProcess;
    }

}
