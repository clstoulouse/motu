
package fr.cls.atoll.motu.processor.wps;

import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.LiteralOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.msg.xml.ErrorType;
import fr.cls.atoll.motu.msg.xml.TimeCoverage;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product
 * 
 * @author last edited by: $Author: dearith $
 * 
 * @version $Revision: 1.6 $, $Date: 2009-04-01 14:13:38 $
 */
public class ProductTimeCoverageProcess extends MotuWPSProcess {

    /**
     * Constructeur.
     */
    public ProductTimeCoverageProcess() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(ProductTimeCoverageProcess.class);

    /** {@inheritDoc} */
    @SuppressWarnings("null")
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        super.process(in, out, info);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN TimeCoverageProcess.process(), context: " + OGCFrontController.getContext());
        }
        LiteralInput serviceNameParam = null;
        LiteralInput locationDataParam = null;
        LiteralInput productIdParam = null;
        
        getProductInfo(serviceNameParam, locationDataParam, productIdParam);

        // -------------------------------------------------
        // get Time coverage
        // -------------------------------------------------
        try {
            if (!MotuWPSProcess.isNullOrEmpty(locationDataParam)) {
                productGetTimeCoverage(locationDataParam.getValue());
            } else if (!MotuWPSProcess.isNullOrEmpty(serviceNameParam) && !MotuWPSProcess.isNullOrEmpty(productIdParam)) {
                productGetTimeCoverage(serviceNameParam.getValue(), productIdParam.getValue());
            }
        } catch (MotuExceptionBase e) {
            //throw new ProcessletException(e.notifyException());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("END TimeCoverageProcess.process()");
        }

        return;
    }

    /**
     * Product get time coverage.
     * 
     * @param response the response
     * @param locationData the location data
     * 
     */
    private void productGetTimeCoverage(String locationData) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, ProcessletOutputs) - entering");
        }

        Organizer organizer = getOrganizer();
        if (organizer == null) {
            return;
        }

        TimeCoverage timeCoverage = null;
        try {
            timeCoverage = organizer.getTimeCoverage(locationData);
        } catch (MotuException e) {
            LOG.error("productGetTimeCoverage(String, ProcessletOutputs)", e);
            // Do nothing error is in response code
        }

        productGetTimeCoverage(timeCoverage);

        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, ProcessletOutputs) - exiting");
        }
    }

    /**
     * Product get time coverage.
     * 
     * @param response the response
     * @param serviceName the service name
     * @param productId the product id
     * @throws MotuExceptionBase
     * 
     */
    private void productGetTimeCoverage(String serviceName, String productId) throws MotuExceptionBase {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, String, ProcessletOutputs) - entering");
        }

        Organizer organizer = getOrganizer();
        if (organizer == null) {
            return;
        }

        TimeCoverage timeCoverage = null;
        try {
            timeCoverage = organizer.getTimeCoverage(serviceName, productId);
        } catch (MotuExceptionBase e) {

            LOG.error("productGetTimeCoverage(String, String, ProcessletOutputs", e);
            timeCoverage = Organizer.createTimeCoverage(e);
            throw e;
        }

        productGetTimeCoverage(timeCoverage);

        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, String, ProcessletOutputs) - exiting");
        }
    }

    /**
     * Product get time coverage.
     * 
     * @param timeCoverage the time coverage
     * @param out the out
     */
    private void productGetTimeCoverage(TimeCoverage timeCoverage) {

        if (timeCoverage == null) {
            return;
        }

        LiteralOutput startParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_STARTTIME);
        LiteralOutput endParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_ENDTIME);
        LiteralOutput codeParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_CODE);
        LiteralOutput msgParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_MESSAGE);

        if (startParam != null) {
            startParam.setValue(timeCoverage.getStart().normalize().toXMLFormat());
        }
        if (endParam != null) {
            endParam.setValue(timeCoverage.getEnd().normalize().toXMLFormat());
        }

        if (codeParam != null) {
            codeParam.setValue(timeCoverage.getCode().toString());
        }
        if (msgParam != null) {
            msgParam.setValue(timeCoverage.getMsg());
        }

    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ProductTimeCoverageProcess#destroy() called");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ProductTimeCoverageProcess#init() called");
        }
        super.init();
    }
}
