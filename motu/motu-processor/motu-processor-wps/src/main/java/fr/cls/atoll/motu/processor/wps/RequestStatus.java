package fr.cls.atoll.motu.processor.wps;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidRequestIdException;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ReferencedComplexInput;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.4 $, $Date: 2009-10-28 15:48:01 $
 */
public class RequestStatus extends MotuWPSProcess {

    /**
     * Constructor.
     */
    public RequestStatus() {
    }

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(RequestStatus.class);

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN RequestStatus.process(), context: " + OGCFrontController.getContext());
        }

        super.process(in, out, info);

        try {
            getRequestStatus(in);
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
            LOG.debug("RequestStatus#destroy() called");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("RequestStatus#init() called");
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
    private void getRequestStatus(ProcessletInputs in) throws MotuException, ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);
        long requestId = processRequestIdAsLong(in);

        if (requestId < 0) {
            return;
        }

        StatusModeResponse statusModeResponse = getRequestManagement().getResquestStatusMap(requestId);

        if (statusModeResponse == null) {
            MotuWPSProcess.setStatus(motuWPSProcessData.getProcessletOutputs(), "");
            // MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), new
            // MotuInvalidRequestIdException(requestId), motuWPSProcessData
            // .getRequestIdParamIn() instanceof ReferencedComplexInput);
            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), new MotuInvalidRequestIdException(requestId), true);
            return;
        }

        MotuWPSProcess.setStatus(motuWPSProcessData.getProcessletOutputs(), statusModeResponse.getStatus());
        MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(),
                                     statusModeResponse,
                                     motuWPSProcessData.getRequestIdParamIn() instanceof ReferencedComplexInput);

    }

}
