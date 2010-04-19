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
 * @version $Revision: 1.9 $, $Date: 2009-10-28 15:48:01 $
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

        try {
            getExtractedUrl(in);
        } catch (MotuException e) {
            setReturnCode(out, e, true);
        } finally {
            super.afterProcess(in, out, info);
        }

    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ExtractedProductUrl#destroy() called");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ExtractedProductUrl#init() called");
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

        long requestId = processRequestIdAsLong(in);
        
        if (requestId < 0) {
            return;
        }

        StatusModeResponse statusModeResponse = waitForResponse(motuWPSProcessData.getRequestIdParamIn(), requestId);

        if (statusModeResponse == null) {
//            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), new MotuInvalidRequestIdException(requestId), motuWPSProcessData
//                                         .getRequestIdParamIn() instanceof ReferencedComplexInput);
            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), new MotuInvalidRequestIdException(requestId), true);
            return;
        }

        if (MotuWPSProcess.isStatusDone(statusModeResponse)) {
            MotuWPSProcess.setUrl(motuWPSProcessData.getProcessletOutputs(), statusModeResponse);
            MotuWPSProcess.setLocalUrl(motuWPSProcessData.getProcessletOutputs(), statusModeResponse);
        } else {
            MotuWPSProcess.setUrl(motuWPSProcessData.getProcessletOutputs(), "");
            MotuWPSProcess.setLocalUrl(motuWPSProcessData.getProcessletOutputs(), "");
        }
        
        MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(),
                                     statusModeResponse,
                                     motuWPSProcessData.getRequestIdParamIn() instanceof ReferencedComplexInput);
    }

}
