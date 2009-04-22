package fr.cls.atoll.motu.processor.wps;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.2 $, $Date: 2009-04-22 14:40:15 $
 */
public class ExtractedProductUrl extends MotuWPSProcess {

    /**
     * Constructeur.
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
            getExtractedUrl();
        } catch (MotuException e) {
            setReturnCode(e);
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
    }
    


 
     /**
      * Gets the extracted url.
      * 
      * @throws MotuException the motu exception
      */
     private void getExtractedUrl() throws MotuException  {
         
         long requestId = getRequestIdAsLong();
         
         StatusModeResponse statusModeResponse = requestManagement.getResquestStatusMap(requestId);
         if (statusModeResponse == null) {
             setReturnCode(new MotuInvalidRequestIdException(requestId));
             return;            
         }
         
         setReturnCode(statusModeResponse);
         setUrl(statusModeResponse);
    }


 
}
