package fr.cls.atoll.motu.processor.wps;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.ReferencedComplexInput;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.1 $, $Date: 2009-05-04 16:16:35 $
 */
public class CompressExtraction extends MotuWPSProcess {

    /**
     * Constructor.
     */
    public CompressExtraction() {
    }

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(CompressExtraction.class);

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN CompressExtraction.process(), context: " + OGCFrontController.getContext());
        }

        super.process(in, out, info);

        try {
            zip(in);
        } catch (MotuException e) {
            setReturnCode(out, e, false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("CompressExtraction#destroy() called");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("CompressExtraction#init() called");
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
    private void zip(ProcessletInputs in) throws MotuException, ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);
        long requestId = -1;

        requestId = getRequestIdAsLong(in);
        
        StatusModeResponse statusModeResponse = waitForResponse(motuWPSProcessData.getRequestIdParamIn(), requestId);

        if (statusModeResponse == null) {
            setReturnCode(motuWPSProcessData.getProcessletOutputs(), new MotuInvalidRequestIdException(requestId),
                          motuWPSProcessData.getRequestIdParamIn() instanceof ReferencedComplexInput);
            return;
        }
        

        if ( !MotuWPSProcess.isStatusDone(statusModeResponse)) {
            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), statusModeResponse);
            MotuWPSProcess.setUrl(motuWPSProcessData.getProcessletOutputs(), statusModeResponse);            
        } else {
            MotuWPSProcess.setUrl(motuWPSProcessData.getProcessletOutputs(), "");            
        }
    }

}
