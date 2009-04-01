//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

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
 * @version $Revision: 1.5 $, $Date: 2009-04-01 08:41:01 $
 */
public class ProductTimeCoverageProcess extends MotuWPSProcess {

    /**
     * Constructeur.
     */
    public ProductTimeCoverageProcess() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(ProductTimeCoverageProcess.class);

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        super.process(in, out, info);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN TimeCoverageProcess.process(), context: " + OGCFrontController.getContext());
        }

        LiteralInput serviceNameParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_SERVICE);
        LiteralInput locationDataParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_URL);
        LiteralInput productIdParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_PRODUCT);

        if (MotuWPSProcess.isNullOrEmpty(locationDataParam) && MotuWPSProcess.isNullOrEmpty(productIdParam)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" empty locationData and empty productId");
                LOG.debug("END TimeCoverageProcess.process()");
            }

            String msg = String.format("ERROR: neither '%s' nor '%s' parameters are filled - Choose one of them",
                                       MotuWPSProcess.PARAM_URL,
                                       PARAM_PRODUCT);

            setReturnCode(ErrorType.INCONSISTENCY, msg);
            throw new ProcessletException(msg);
        }

        if (!MotuWPSProcess.isNullOrEmpty(locationDataParam) && !MotuWPSProcess.isNullOrEmpty(productIdParam)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" non empty locationData and non empty productId");
                LOG.debug("END TimeCoverageProcess.process()");
            }
            String msg = String.format("ERROR: '%s' and '%s' parameters are not compatible - Choose only one of them",
                                       MotuWPSProcess.PARAM_URL,
                                       MotuWPSProcess.PARAM_PRODUCT);

            setReturnCode(ErrorType.INCONSISTENCY, msg);
            throw new ProcessletException(msg);
        }

        if (MotuWPSProcess.isNullOrEmpty(serviceNameParam) && !MotuWPSProcess.isNullOrEmpty(productIdParam)) {
            if (LOG.isDebugEnabled()) {
                LOG.info("empty serviceName  and non empty productId");
                LOG.debug("END TimeCoverageProcess.process()");
            }
            String msg = String.format("ERROR: '%s' parameter is filled but '%s' is empty. You have to fill it.", PARAM_PRODUCT, PARAM_SERVICE);

            setReturnCode(ErrorType.INCONSISTENCY, msg);
            throw new ProcessletException(msg);
        }

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
            // TODO Auto-generated catch block
            throw new ProcessletException(e.notifyException());
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

        LiteralOutput startParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_START);
        LiteralOutput endParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_END);
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
