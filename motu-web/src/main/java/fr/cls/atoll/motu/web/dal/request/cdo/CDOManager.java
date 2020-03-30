package fr.cls.atoll.motu.web.dal.request.cdo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.BLLRequestManager;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.dal.request.IDALRequestManager;
import fr.cls.atoll.motu.web.dal.tds.ncss.NetCdfSubsetService;

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
public class CDOManager implements ICDOManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int BLOCKING_QUEUE_CAPACITY = 100;
    private static final int CORE_POOL_SIZE = 1;
    private BlockingQueue<Runnable> cdoJobsQueue;
    private ThreadPoolExecutor tpe;
    private boolean isStopping;

    public CDOManager() {
        cdoJobsQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        isStopping = false;
        tpe = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                BLOCKING_QUEUE_CAPACITY,
                BLLRequestManager.REQUEST_TIMEOUT_MSEC,
                TimeUnit.MILLISECONDS,
                cdoJobsQueue);
        tpe.prestartCoreThread();
    }

    @Override
    public void stop() {
        tpe.shutdown();
    }

    @Override
    public void runRequestWithCDOMergeTool(RequestProduct rp,
                                           NetCdfSubsetService ncss,
                                           ExtractCriteriaLatLon latlon,
                                           String extractDirPath,
                                           String fname,
                                           IDALRequestManager dalRequestManager)
            throws Exception {
        CDOJob job = new CDOJob(rp, ncss, latlon, extractDirPath, fname, dalRequestManager) {

            /** {@inheritDoc} */
            @Override
            protected void onJobEnds() {
                synchronized (CDOManager.this) {
                    super.onJobEnds();
                    CDOManager.this.notifyAll();
                }
            }

        };

        try {
            cdoJobsQueue.add(job);
            LOGGER.info("CDO job added, ProductId=" + rp.getProduct().getProductId() + ", cdoJobsQueue size=" + cdoJobsQueue.size());
            synchronized (this) {
                long startWaitTime = System.currentTimeMillis();
                long waitTime = BLLRequestManager.REQUEST_TIMEOUT_MSEC;
                while (!job.isJobEnded() && !isStopping) {
                    try {
                        wait(waitTime);
                    } catch (InterruptedException e) {
                        LOGGER.error("Error in CDO download execution while waiting the job ended notification", e);
                    }
                    waitTime = BLLRequestManager.REQUEST_TIMEOUT_MSEC - (System.currentTimeMillis() - startWaitTime);
                    if (waitTime <= 0) {
                        waitTime = 1;
                    }
                }
            }

            if (job.getRunningException() != null) {
                throw job.getRunningException();
            }

        } catch (IllegalStateException e) {
            throw new MotuException(ErrorType.SYSTEM, "CDO job queue is full", e);
        }

    }

}
