package fr.cls.atoll.motu.web.bll.request.queueserver.queue;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

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
public class QueueJob implements IQueueJob {

    private QueueJobListener queueJobListener;
    private ExtractionParameters extractionParameters;
    private Product product;
    private ConfigService cs;
    private OutputFormat dataOutputFormat;

    public QueueJob(ConfigService cs_, Product product_, OutputFormat dataOutputFormat_, QueueJobListener queueJobListener_) {
        cs = cs_;
        product = product_;
        dataOutputFormat = dataOutputFormat_;
        queueJobListener = queueJobListener_;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        try {
            onJobStarted();
            processJob();
            onJobStopped();
        } catch (Exception e) {
            onJobException(e);
        }
    }

    /**
     * .
     */
    private void processJob() throws MotuException {
        DALManager.getInstance().getRequestManager().downloadProduct(cs, product, dataOutputFormat);
        // processRequest(requestDownloadStatus, extractionParameters);
    }

    private void onJobStarted() {
        queueJobListener.onJobStarted();
    }

    private void onJobStopped() {
        queueJobListener.onJobStopped();
    }

    private void onJobException(Exception e) {
        queueJobListener.onJobException(e);
    }

    /** {@inheritDoc} */
    @Override
    public ExtractionParameters getExtractionParameters() {
        return extractionParameters;
    }

}
