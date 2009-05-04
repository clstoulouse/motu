package fr.cls.atoll.motu.processor.wps;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ReferencedComplexInput;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.4 $, $Date: 2009-05-04 09:58:44 $
 */
public class ExtractedProductUrl extends MotuWPSProcess {

    /**
     * Constructor.
     */
    public ExtractedProductUrl() {
    }

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(ExtractedProductUrl.class);

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN ExtractedProductUrl.process(), context: " + OGCFrontController.getContext());
        }

        super.process(in, out, info);

        long threadId = Thread.currentThread().getId();
        
        try {
            getExtractedUrl(in);
        } catch (MotuException e) {
            setReturnCode(out, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ProductExtractionProcess#destroy() called");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ProductExtractionProcess#init() called");
        }
        super.init();

        // runnableWPS = new
    }

    /**
     * Gets the extracted url.
     * 
     * @throws MotuException the motu exception
     * @throws ProcessletException 
     */
    private void getExtractedUrl(ProcessletInputs in) throws MotuException, ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);
        long requestId = -1;

        requestId = getRequestIdAsLong(in);
        StatusModeResponse statusModeResponse = getRequestManagement().getResquestStatusMap(requestId);

        if (statusModeResponse == null) {
            setReturnCode(motuWPSProcessData.getProcessletOutputs(), new MotuInvalidRequestIdException(requestId));
            return;
        }

        if (motuWPSProcessData.getRequestIdParamIn() instanceof ReferencedComplexInput) {
            while (MotuWPSProcess.isStatusPendingOrInProgress(statusModeResponse)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), statusModeResponse);
        if ( MotuWPSProcess.isStatusDone(statusModeResponse)) {
            MotuWPSProcess.setUrl(motuWPSProcessData.getProcessletOutputs(), statusModeResponse);            
        } else {
            MotuWPSProcess.setUrl(motuWPSProcessData.getProcessletOutputs(), "");            
        }
    }

}
