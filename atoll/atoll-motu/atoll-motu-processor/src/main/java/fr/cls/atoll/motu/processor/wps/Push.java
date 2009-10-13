package fr.cls.atoll.motu.processor.wps;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ReferencedComplexInput;

import fr.cls.atoll.motu.library.data.Product;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.utils.Zip;
import fr.cls.atoll.motu.msg.xml.ErrorType;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.3 $, $Date: 2009-10-13 14:07:58 $
 */
public class Push extends MotuWPSProcess {

    /**
     * Constructor.
     */
    public Push() {
    }

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(Push.class);

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN CompressExtraction.process(), context: " + OGCFrontController.getContext());
        }

        super.process(in, out, info);

        try {
            push(in);
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
       * Push.
       * 
       * @param in the in
       * 
       * @throws ProcessletException the processlet exception
       * @throws MotuException the motu exception
       */
      private void push(ProcessletInputs in) throws ProcessletException, MotuException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        String from = MotuWPSProcess.getComplexInputValueFromBinaryStream(motuWPSProcessData.getFromParamIn());

        // Either the the source url (from) or the request id
        // have to be set as input parameter
        // If 'from' is null or empty : use resquest id and get the its remote url as 'from' parameter
        if (MotuWPSProcess.isNullOrEmpty(from)) {
            long requestId = -1;

            requestId = getRequestIdAsLong(in);

            StatusModeResponse statusModeResponse = waitForResponse(motuWPSProcessData.getRequestIdParamIn(), requestId);

            if (statusModeResponse == null) {
//                MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), new MotuInvalidRequestIdException(requestId), motuWPSProcessData
//                                             .getRequestIdParamIn() instanceof ReferencedComplexInput);
                MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), new MotuInvalidRequestIdException(requestId), true);
                return;
            }

            if (MotuWPSProcess.isStatusDone(statusModeResponse)) {
                from = statusModeResponse.getRemoteUri();
            } else {
                MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(),
                                             statusModeResponse,
                                             motuWPSProcessData.getRequestIdParamIn() instanceof ReferencedComplexInput);
                return;
            }
            
            
        }
        
        String to = MotuWPSProcess.getComplexInputValueFromBinaryStream(motuWPSProcessData.getToParamIn());

        String userFrom = MotuWPSProcess.getLiteralInputValue(motuWPSProcessData.getUserFromParamIn());
        String pwdFrom = MotuWPSProcess.getLiteralInputValue(motuWPSProcessData.getPwdFromParamIn());

        String userTo = MotuWPSProcess.getLiteralInputValue(motuWPSProcessData.getUserToParamIn());
        String pwdTo = MotuWPSProcess.getLiteralInputValue(motuWPSProcessData.getPwdToParamIn());
        
        boolean remove = MotuWPSProcess.getLiteralInputValueAsBoolean(motuWPSProcessData.getRemoveParamIn(), false);

        boolean rename = MotuWPSProcess.getLiteralInputValueAsBoolean(motuWPSProcessData.getRenameParamIn(), false);
        
        
        if (!rename) {
            File fileTmp  = new File(from);
            to = String.format("%s/%s", to, fileTmp.getName());
        }
        
        try {
            if ((MotuWPSProcess.isNullOrEmpty(userFrom)) && (MotuWPSProcess.isNullOrEmpty(userTo))) {
                Organizer.copyFile(from, to);                
            } else {
                Organizer.copyFile(from, to, userFrom, pwdFrom, userTo, pwdTo);                
            }
            
        } catch (Exception e) {
            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(),
                                         e,
                                         false);
            return;
        }
        
        StringBuffer stringBuffer = new StringBuffer();
        
        stringBuffer.append(String.format("Uri '%s' have been transfered to '%s'", from, to));
        
        ErrorType errorType = ErrorType.OK;
        
        if (remove) {
            try {
                Organizer.deleteFile(from);
                stringBuffer.append(String.format("Uri '%s' have been deleted", from));                
            } catch (MotuException e) {
                errorType = ErrorType.SYSTEM;
                stringBuffer.append(e.notifyException());
            }
        }
        
        MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), errorType, stringBuffer.toString(), false);
        
    }
    
 
}
