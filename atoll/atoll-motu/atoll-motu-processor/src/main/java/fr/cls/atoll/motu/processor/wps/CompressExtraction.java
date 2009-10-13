package fr.cls.atoll.motu.processor.wps;

import java.io.File;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ReferencedComplexInput;

import fr.cls.atoll.motu.library.data.Product;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.utils.Zip;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.5 $, $Date: 2009-10-13 14:07:58 $
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
            setReturnCode(out, e, true);
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
            // MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), new
            // MotuInvalidRequestIdException(requestId), motuWPSProcessData
            // .getRequestIdParamIn() instanceof ReferencedComplexInput);
            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), new MotuInvalidRequestIdException(requestId), true);
            return;
        }

        MotuWPSProcess.setRequestId(motuWPSProcessData.getProcessletOutputs(), requestId);

        if (MotuWPSProcess.isStatusDone(statusModeResponse)) {

            String fileName = Organizer.extractFileName(statusModeResponse.getRemoteUri());

            if (fileName.isEmpty()) {
                // MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(),
                // new
                // MotuException(String.format("Error in CompressExtraction#zip : no file to compress has been found from url %s",
                // statusModeResponse.getMsg())),
                // motuWPSProcessData.getRequestIdParamIn() instanceof ReferencedComplexInput);
                MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(),
                                             new MotuException(String
                                                     .format("Error in CompressExtraction#zip : no file to compress has been found from url %s",
                                                             statusModeResponse.getMsg())),
                                             true);
                return;
            }

            String localFileName = Product.getExtractLocationData(fileName);
            String zipFileName = String.format("%s%s", localFileName, Organizer.ZIP_EXTENSION);
            try {
                Zip.zip(zipFileName, localFileName, false);
            } catch (MotuException e) {
                // MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(),
                // e,
                // motuWPSProcessData.getRequestIdParamIn() instanceof ReferencedComplexInput);
                MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), e, true);
                return;
            }
            File fileTemp = new File(zipFileName);
            String httpUrl = Product.getDownloadUrlPath(fileTemp.getName());
            statusModeResponse.setMsg(httpUrl);
            statusModeResponse.setRemoteUri(httpUrl);
            statusModeResponse.setLocalUri(zipFileName);
            fileTemp = new File(localFileName);
            try {
                fileTemp.delete();
            } catch (Exception e) {
                // Do nothing
            }
        }

        MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(),
                                     statusModeResponse,
                                     motuWPSProcessData.getRequestIdParamIn() instanceof ReferencedComplexInput);
    }

}
