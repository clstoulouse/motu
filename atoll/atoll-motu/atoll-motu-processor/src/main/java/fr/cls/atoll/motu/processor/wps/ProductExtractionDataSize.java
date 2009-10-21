package fr.cls.atoll.motu.processor.wps;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.output.LiteralOutput;

import fr.cls.atoll.motu.library.data.Product;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.ExtractionParameters;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.msg.xml.RequestSize;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;
import fr.cls.atoll.motu.msg.xml.StatusModeType;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.3 $, $Date: 2009-10-21 09:08:23 $
 */
public class ProductExtractionDataSize extends MotuWPSProcess {

    /**
     * Constructeur.
     */
    public ProductExtractionDataSize() {
    }

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(ProductExtractionDataSize.class);

    // protected boolean isRequestIdSet = false;

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN ProductExtractionDataSize.process(), context: " + OGCFrontController.getContext());
        }

        super.process(in, out, info);

        MotuWPSProcessData motuWPSProcessData = getProductInfoParameters(in);

        ExtractionParameters extractionParameters = new ExtractionParameters(motuWPSProcessData.getServiceName(), motuWPSProcessData
                .getLocationData(), getVariables(in), getTemporalCoverage(in), getGeoCoverage(in), getDepthCoverage(in), motuWPSProcessData
                .getProductId(), getDataFormat(in), null, null, null, true);

        extractionParameters.setBatchQueue(isBatch(in));

        try {
            getAmountDataSize(in, extractionParameters);
        } catch (MotuExceptionBase e) {
            setReturnCode(out, e, true);
        }

 

    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ProductExtraction#destroy() called");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ProductExtraction#init() called");
        }
        super.init();
    }

    private void getAmountDataSize(ProcessletInputs in, ExtractionParameters extractionParameters) throws MotuExceptionBase, ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        Product product = null;
        RequestSize requestSize = null;
        try {
            Organizer organizer = getOrganizer(in);
            product = organizer.getAmountDataSize(extractionParameters);

            requestSize = Organizer.initRequestSize(product, extractionParameters.isBatchQueue());

            ProductExtractionDataSize.setRequestSize(motuWPSProcessData.getProcessletOutputs(), requestSize);
            ProductExtractionDataSize.setReturnCode(motuWPSProcessData.getProcessletOutputs(), requestSize, false);

        } catch (MotuExceptionBase e) {
            ProductExtractionDataSize.setRequestSize(motuWPSProcessData.getProcessletOutputs(), requestSize);
            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), e, true);
            throw e;
        }
    }

    public static void setRequestSize(ProcessletOutputs response, RequestSize requestSize) {

        if (requestSize != null) {
            ProductExtractionDataSize.setRequestSize(response, requestSize.getSize(), requestSize.getMaxAllowedSize());
        } else {
            ProductExtractionDataSize.setRequestSize(response, -1d, -1d);
        }
    }

    public static void setRequestSize(ProcessletOutputs response, double size, double maxAllowedSize) {
        synchronized (response) {

            if (response == null) {
                return;
            }

            LiteralOutput maxAllowedSizeParam = (LiteralOutput) response.getParameter(MotuWPSProcess.PARAM_MAX_ALLOWED_SIZE);

            if ((maxAllowedSizeParam != null)) {
                maxAllowedSizeParam.setValue(Double.toString(maxAllowedSize));
            }

            LiteralOutput sizeParam = (LiteralOutput) response.getParameter(MotuWPSProcess.PARAM_SIZE);

            if ((sizeParam != null)) {
                sizeParam.setValue(Double.toString(size));
            }
        }

    }

    public static void setReturnCode(ProcessletOutputs response, RequestSize requestSize, boolean throwProcessletException)
            throws ProcessletException {

        MotuWPSProcess.setReturnCode(response, requestSize.getCode(), requestSize.getMsg(), throwProcessletException);

    }


}
