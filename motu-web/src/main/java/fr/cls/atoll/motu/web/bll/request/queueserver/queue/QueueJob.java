package fr.cls.atoll.motu.web.bll.request.queueserver.queue;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;

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
public class QueueJob implements IQueueJob, Comparable<IQueueJob> {

    private QueueJobListener queueJobListener;
    private RequestDownloadStatus rds;
    private ConfigService cs;

    public QueueJob(ConfigService cs_, RequestDownloadStatus rds_, QueueJobListener queueJobListener_) {
        cs = cs_;
        queueJobListener = queueJobListener_;
        rds = rds_;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        try {
            onJobStarted();
            processJob();
            onJobStopped();
        } catch (MotuException e) {
            onJobException(e);
        } catch (Exception e) {
            onJobException(new MotuException(ErrorType.SYSTEM, e));
        }
    }

    /**
     * .
     */
    private void processJob() throws MotuException {
        DALManager.getInstance().getRequestManager().downloadProduct(cs, rds);
    }

    private void onJobStarted() throws MotuException {
        queueJobListener.onJobStarted();
    }

    private void onJobStopped() {
        queueJobListener.onJobStopped();
    }

    @Override
    public void onJobException(MotuException e) {
        queueJobListener.onJobException(e);
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(IQueueJob o) {
        // In version 2.x Motu managed priorities, now it does not.
        // But as a priority queue, is already used, we keep this comparable implementation
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void stop() {
        // Here there is nothing to do, we let this request ends in a normal way

    }

    /**
     * Valeur de rds.
     * 
     * @return la valeur.
     */
    @Override
    public RequestDownloadStatus getRequestDownloadStatus() {
        return rds;
    }

}
