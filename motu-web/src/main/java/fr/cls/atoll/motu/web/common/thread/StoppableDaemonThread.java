package fr.cls.atoll.motu.web.common.thread;

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

    private boolean isDaemonStoppingASAP;

    public StoppableDaemonThread(String threadName) {
        super(threadName);
        setDaemon(true);
        setDaemonStoppingASAP(false);
    }

    /**
     * .
     * 
     * @param b
     */
    public void setDaemonStoppingASAP(boolean isDaemonStoppingASAP_) {
        isDaemonStoppingASAP = isDaemonStoppingASAP_;
    }

    /**
     * Valeur de shallDaemonStoppedASAP.
     * 
     * @return la valeur.
     */
    public boolean isDaemonStoppedASAP() {
        return isDaemonStoppingASAP;
    }
}
