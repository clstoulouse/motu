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

            product = extractData(extractionParameters.getServiceName(),
                                  extractionParameters.getLocationData(),
                                  null,
                                  extractionParameters.getListVar(),
                                  extractionParameters.getListTemporalCoverage(),
                                  extractionParameters.getListLatLonCoverage(),
                                  extractionParameters.getListDepthCoverage(),
                                  null,
                                  extractionParameters.getDataOutputFormat(),
                                  extractionParameters.getOut(),
                                  extractionParameters.getResponseFormat(),
                                  null);
        } else if (!StringUtils.isNullOrEmpty(extractionParameters.getServiceName())
                && !StringUtils.isNullOrEmpty(extractionParameters.getProductId())) {
            product = extractData(extractionParameters.getServiceName(),
                                  extractionParameters.getListVar(),
                                  extractionParameters.getListTemporalCoverage(),
                                  extractionParameters.getListLatLonCoverage(),
                                  extractionParameters.getListDepthCoverage(),
                                  extractionParameters.getProductId(),
                                  null,
                                  extractionParameters.getDataOutputFormat(),
                                  extractionParameters.getOut(),
                                  extractionParameters.getResponseFormat());
        } else {
            throw new MotuInconsistencyException(
                    String.format("ERROR in extractData: inconsistency parameters : %s", extractionParameters.toString()));
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
