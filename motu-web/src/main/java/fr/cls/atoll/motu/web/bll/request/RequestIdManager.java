package fr.cls.atoll.motu.web.bll.request;

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
public class RequestIdManager implements IRequestIdManager {

    private long lastRequestId;

    /** {@inheritDoc} */
    @Override
    public synchronized long getNewRequestId() {
        long newRqtId = System.currentTimeMillis();
        if (newRqtId == lastRequestId) {
            lastRequestId++;
        } else {
            lastRequestId = newRqtId;
        }
        return lastRequestId;
    }

}
