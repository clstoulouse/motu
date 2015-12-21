/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.processor.wps;

import fr.cls.atoll.motu.api.message.xml.RequestSize;
import fr.cls.atoll.motu.library.misc.data.Product;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.intfce.ExtractionParameters;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.output.LiteralOutput;
import org.jasig.cas.client.util.AssertionHolder;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.5 $, $Date: 2010-02-26 14:09:43 $
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
        try {

            MotuWPSProcessData motuWPSProcessData = getProductInfoParameters(in);

            ExtractionParameters extractionParameters = new ExtractionParameters(motuWPSProcessData.getServiceName(), motuWPSProcessData
                    .getLocationData(), getVariables(in), getTemporalCoverage(in), getGeoCoverage(in), getDepthCoverage(in), motuWPSProcessData
                    .getProductId(), getDataFormat(in), null, null, null, true);

            extractionParameters.setBatchQueue(isBatch(in));

            // Set assertion to manage CAS.
            extractionParameters.setAssertion(AssertionHolder.getAssertion());

            getAmountDataSize(in, extractionParameters);
        } catch (MotuExceptionBase e) {
            setReturnCode(out, e, true);
        } finally {
            super.afterProcess(in, out, info);
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
