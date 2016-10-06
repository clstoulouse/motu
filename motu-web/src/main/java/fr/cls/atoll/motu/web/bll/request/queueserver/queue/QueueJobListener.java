package fr.cls.atoll.motu.web.bll.request.queueserver.queue;

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
public interface QueueJobListener {

    /**
     * .
     */
    void onJobStarted();

    /**
     * .
     */
    public void onJobStopped();

    /**
     * .
     * 
     * @param e
     */
    void onJobException(MotuException e);

    boolean isJobEnded();

}
