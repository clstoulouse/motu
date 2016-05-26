package fr.cls.atoll.motu.web.bll.request.queueserver.queue;

import fr.cls.atoll.motu.library.misc.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
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

    public QueueJob(ExtractionParameters extractionParameters_, QueueJobListener queueJobListener_) {
        extractionParameters = extractionParameters_;
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
    private void processJob() {
        Product product = null;

        // -------------------------------------------------
        // Data extraction OPENDAP
        // -------------------------------------------------
        if (!StringUtils.isNullOrEmpty(getExtractionParameters().getLocationData())) {

            product = extractData(params.getServiceName(),
                                  params.getLocationData(),
                                  null,
                                  params.getListVar(),
                                  params.getListTemporalCoverage(),
                                  params.getListLatLonCoverage(),
                                  params.getListDepthCoverage(),
                                  null,
                                  params.getDataOutputFormat(),
                                  params.getOut(),
                                  params.getResponseFormat(),
                                  null);
        } else if (!StringUtils.isNullOrEmpty(params.getServiceName()) && !StringUtils.isNullOrEmpty(params.getProductId())) {
            product = extractData(params.getServiceName(),
                                  params.getListVar(),
                                  params.getListTemporalCoverage(),
                                  params.getListLatLonCoverage(),
                                  params.getListDepthCoverage(),
                                  params.getProductId(),
                                  null,
                                  params.getDataOutputFormat(),
                                  params.getOut(),
                                  params.getResponseFormat());
        } else {
            throw new MotuInconsistencyException(String.format("ERROR in extractData: inconsistency parameters : %s", params.toString()));
        }

        return product;
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
