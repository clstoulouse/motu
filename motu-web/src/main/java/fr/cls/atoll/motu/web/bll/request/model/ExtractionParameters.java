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
package fr.cls.atoll.motu.web.bll.request.model;

import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasig.cas.client.validation.Assertion;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.library.cas.util.AssertionUtils;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.common.utils.TimeUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class ExtractionParameters implements Cloneable {

    private static final Logger LOGGER = LogManager.getLogger();

    public ExtractionParameters(
        String serviceName,
        String locationData,
        List<String> listVar,
        String startDateTemporalCoverage_,
        String endDateTemporalCoverage_,
        double x_lo_,
        double x_hi_,
        double y_lo_,
        double y_hi_,
        double z_lo_,
        double z_hi_,
        String productId,
        OutputFormat dataOutputFormat_,
        String userId,
        boolean anonymousUser) {
        this(
            serviceName,
            locationData,
            listVar,
            getTemporalCoverage(startDateTemporalCoverage_, endDateTemporalCoverage_),
            getGeoCoverage(x_lo_, x_hi_, y_lo_, y_hi_),
            getDepthCoverage(z_lo_, z_hi_),
            productId,
            dataOutputFormat_,
            null,
            null,
            userId,
            anonymousUser);

    }

    /**
     * Gets the temporal coverage from the request.
     * 
     * @param request servlet request
     * 
     * @return a list of temporable coverage, first start date, and then end date (they can be empty string)
     */
    private static List<String> getTemporalCoverage(String startDate, String endDate) {
        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add(startDate);
        listTemporalCoverage.add(endDate);
        return listTemporalCoverage;
    }

    /**
     * Gets the geographical coverage from the request.
     * 
     * @param request servlet request
     * 
     * @return a list of geographical coverage : Lat min, Lon min, Lat max, Lon max
     */
    private static List<String> getGeoCoverage(double x_lo_, double x_hi_, double y_lo_, double y_hi_) {
        List<String> listLatLonCoverage = new ArrayList<String>();
        listLatLonCoverage.add(Double.toString(y_lo_));
        listLatLonCoverage.add(Double.toString(x_lo_));
        listLatLonCoverage.add(Double.toString(y_hi_));
        listLatLonCoverage.add(Double.toString(x_hi_));
        return listLatLonCoverage;
    }

    /**
     * Gets the depth coverage from the request.
     * 
     * @param request servlet request
     * 
     * @return a list of deph coverage : first depth min, then depth max
     */
    protected static List<String> getDepthCoverage(double lowdepth, double highDepth) {
        List<String> listDepthCoverage = new ArrayList<String>();
        listDepthCoverage.add(Double.toString(lowdepth));
        listDepthCoverage.add(Double.toString(highDepth));
        return listDepthCoverage;
    }

    /**
     * The Constructor.
     * 
     * @param listVar the list var
     * @param dataOutputFormat the data output format
     * @param locationData the location data
     * @param responseFormat the response format
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param out the out
     */
    public ExtractionParameters(
        String locationData,
        List<String> listVar,
        List<String> listTemporalCoverage,
        List<String> listLatLonCoverage,
        List<String> listDepthCoverage,
        OutputFormat dataOutputFormat,
        Writer out,
        OutputFormat responseFormat) {

        this(
            null,
            locationData,
            listVar,
            listTemporalCoverage,
            listLatLonCoverage,
            listDepthCoverage,
            null,
            dataOutputFormat,
            out,
            responseFormat,
            null,
            true);

    }

    /**
     * The Constructor.
     * 
     * @param listVar the list var
     * @param dataOutputFormat the data output format
     * @param userId the user id
     * @param locationData the location data
     * @param responseFormat the response format
     * @param anonymousUser the anonymous user
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param out the out
     */
    public ExtractionParameters(
        String locationData,
        List<String> listVar,
        List<String> listTemporalCoverage,
        List<String> listLatLonCoverage,
        List<String> listDepthCoverage,
        OutputFormat dataOutputFormat,
        Writer out,
        OutputFormat responseFormat,
        String userId,
        boolean anonymousUser) {

        this(
            null,
            locationData,
            listVar,
            listTemporalCoverage,
            listLatLonCoverage,
            listDepthCoverage,
            null,
            dataOutputFormat,
            out,
            responseFormat,
            userId,
            anonymousUser);

    }

    /**
     * The Constructor.
     * 
     * @param listVar the list var
     * @param dataOutputFormat the data output format
     * @param locationData the location data
     * @param responseFormat the response format
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param serviceName the service name
     * @param out the out
     */
    public ExtractionParameters(
        String serviceName,
        String locationData,
        List<String> listVar,
        List<String> listTemporalCoverage,
        List<String> listLatLonCoverage,
        List<String> listDepthCoverage,
        OutputFormat dataOutputFormat,
        Writer out,
        OutputFormat responseFormat) {

        this(
            serviceName,
            locationData,
            listVar,
            listTemporalCoverage,
            listLatLonCoverage,
            listDepthCoverage,
            null,
            dataOutputFormat,
            out,
            responseFormat,
            null,
            true);

    }

    /**
     * The Constructor.
     * 
     * @param listVar the list var
     * @param dataOutputFormat the data output format
     * @param userId the user id
     * @param locationData the location data
     * @param responseFormat the response format
     * @param anonymousUser the anonymous user
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param out the out
     * @param serviceName the service name
     */
    public ExtractionParameters(
        String serviceName,
        String locationData,
        List<String> listVar,
        List<String> listTemporalCoverage,
        List<String> listLatLonCoverage,
        List<String> listDepthCoverage,
        OutputFormat dataOutputFormat,
        Writer out,
        OutputFormat responseFormat,
        String userId,
        boolean anonymousUser) {

        this(
            serviceName,
            locationData,
            listVar,
            listTemporalCoverage,
            listLatLonCoverage,
            listDepthCoverage,
            null,
            dataOutputFormat,
            out,
            responseFormat,
            userId,
            anonymousUser);

    }

    /**
     * The Constructor.
     * 
     * @param listVar the list var
     * @param dataOutputFormat the data output format
     * @param responseFormat the response format
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param serviceName the service name
     * @param productId the product id
     * @param out the out
     */
    public ExtractionParameters(
        String serviceName,
        List<String> listVar,
        List<String> listTemporalCoverage,
        List<String> listLatLonCoverage,
        List<String> listDepthCoverage,
        String productId,
        OutputFormat dataOutputFormat,
        Writer out,
        OutputFormat responseFormat) {

        this(
            serviceName,
            null,
            listVar,
            listTemporalCoverage,
            listLatLonCoverage,
            listDepthCoverage,
            productId,
            dataOutputFormat,
            out,
            responseFormat,
            null,
            true);

    }

    /**
     * The Constructor.
     * 
     * @param listVar the list var
     * @param dataOutputFormat the data output format
     * @param userId the user id
     * @param responseFormat the response format
     * @param anonymousUser the anonymous user
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param out the out
     * @param productId the product id
     * @param serviceName the service name
     */
    public ExtractionParameters(
        String serviceName,
        List<String> listVar,
        List<String> listTemporalCoverage,
        List<String> listLatLonCoverage,
        List<String> listDepthCoverage,
        String productId,
        OutputFormat dataOutputFormat,
        Writer out,
        OutputFormat responseFormat,
        String userId,
        boolean anonymousUser) {

        this(
            serviceName,
            null,
            listVar,
            listTemporalCoverage,
            listLatLonCoverage,
            listDepthCoverage,
            productId,
            dataOutputFormat,
            out,
            responseFormat,
            userId,
            anonymousUser);

    }

    /**
     * The Constructor.
     * 
     * @param listVar the list var
     * @param dataOutputFormat the data output format
     * @param userId the user id
     * @param locationData the location data
     * @param responseFormat the response format
     * @param anonymousUser the anonymous user
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param out the out
     * @param productId the product id
     * @param serviceName the service name
     */
    public ExtractionParameters(
        String serviceName,
        String locationData,
        List<String> listVar,
        List<String> listTemporalCoverage,
        List<String> listLatLonCoverage,
        List<String> listDepthCoverage,
        String productId,
        OutputFormat dataOutputFormat,
        Writer out,
        OutputFormat responseFormat,
        String userId,
        boolean anonymousUser) {

        setServiceName(serviceName);
        setLocationData(locationData);
        setListVar(listVar);
        setListTemporalCoverage(listTemporalCoverage);
        setListLatLonCoverage(listLatLonCoverage);
        setListDepthCoverage(listDepthCoverage);
        setProductId(productId);
        setDataOutputFormat(dataOutputFormat);
        setOut(out);
        setResponseFormat(responseFormat);
        setUserId(userId);
        setAnonymousUser(anonymousUser);

    }

    /**
     * {@inheritDoc}.
     * 
     * @return the object
     * 
     * @throws CloneNotSupportedException the clone not supported exception
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /** The service name. */
    private String serviceName = null;

    /** The list var. */
    private List<String> listVar = null;

    /** The list temporal coverage. */
    private List<String> listTemporalCoverage = null;

    /** The temporal coverage in days. */
    private int temporalCoverageInDays = -1;

    /** The list lat lon coverage. */
    private List<String> listLatLonCoverage = null;

    /** The list depth coverage. */
    private List<String> listDepthCoverage = null;

    /** The product id. */
    private String productId = null;

    /** The data output format. */
    private OutputFormat dataOutputFormat = OutputFormat.NETCDF;

    /** The out. */
    private Writer out = null;

    /** The response format. */
    private OutputFormat responseFormat = OutputFormat.HTML;

    /** The location data. */
    private String locationData = null;

    /** The user id. */
    private String userId = null;

    /** The user host. */
    private String userHost = null;

    /** The anonymous user. */
    private boolean anonymousUser = true;

    /** The batch queue. */
    // protected boolean batchQueue = false;

    /** The protocol scheme. */
    protected String protocolScheme = null;

    /** The assertion to manage CAS. */
    protected Assertion assertion = null;

    /**
     * Gets the assertion.
     * 
     * @return the assertion
     */
    public Assertion getAssertion() {
        return assertion;
    }

    /**
     * Sets the assertion.
     *
     * @param assertion the new assertion
     */
    public void setAssertion(Assertion assertion) {
        setAssertion(assertion, true);
    }

    /**
     * Sets the assertion.
     *
     * @param assertion the new assertion
     * @param overrideUserId : if true, override the user by the user in the stored in the assertion.
     */
    public void setAssertion(Assertion assertion, boolean overrideUserId) {
        this.assertion = assertion;
        // If assertion is not null
        // --> get he user name from AttributePrincipal and set the user name
        // --> set anonymous user to false
        if (overrideUserId) {
            String name = AssertionUtils.getAttributePrincipalName(assertion);
            if (!StringUtils.isNullOrEmpty(name)) {
                setUserId(name);
                setAnonymousUser(false);
            }
        }
    }

    /**
     * Test if a string is null or empty.
     * 
     * @return true if string is null or empty, otherwise false.
     */
    // static protected boolean isNullOrEmpty(String value) {
    // if (value == null) {
    // return true;
    // }
    // if (value.equals("")) {
    // return true;
    // }
    // return false;
    // }
    /**
     * Gets the data output format.
     * 
     * @return the data output format
     */
    public OutputFormat getDataOutputFormat() {
        return dataOutputFormat;
    }

    /**
     * Sets the data output format.
     * 
     * @param dataOutputFormat the data output format
     */
    public void setDataOutputFormat(OutputFormat dataOutputFormat) {
        this.dataOutputFormat = dataOutputFormat;
    }

    /**
     * Sets the protocol scheme.
     * 
     * @param protocolScheme the new protocol scheme
     */
    public void setProtocolScheme(String protocolScheme) {
        this.protocolScheme = protocolScheme;
    }

    /**
     * Gets the protocol scheme.
     * 
     * @return the protocol scheme
     * 
     * @throws MotuException the motu exception
     */
    public String getProtocolScheme() throws MotuException {
        if (!StringUtils.isNullOrEmpty(this.protocolScheme)) {
            return this.protocolScheme;
        }

        if (StringUtils.isNullOrEmpty(locationData)) {
            return null;
        }

        URI uri = null;
        try {
            uri = new URI(locationData.replace("\\", "/"));
        } catch (URISyntaxException e) {
            throw new MotuException(ErrorType.SYSTEM, String.format("ERROR: location data '%s' has not a valid syntax", locationData), e);
        }
        return uri.getScheme();
    }

    /**
     * Gets the list depth coverage.
     * 
     * @return the list depth coverage
     */
    public List<String> getListDepthCoverage() {
        return listDepthCoverage;
    }

    /**
     * Sets the list depth coverage.
     * 
     * @param listDepthCoverage the list depth coverage
     */
    public void setListDepthCoverage(List<String> listDepthCoverage) {
        this.listDepthCoverage = listDepthCoverage;
    }

    /**
     * Gets the list lat lon coverage.
     * 
     * @return the list lat lon coverage
     */
    public List<String> getListLatLonCoverage() {
        return listLatLonCoverage;
    }

    /**
     * Sets the list lat lon coverage.
     * 
     * @param listLatLonCoverage the list lat lon coverage
     */
    public void setListLatLonCoverage(List<String> listLatLonCoverage) {
        this.listLatLonCoverage = listLatLonCoverage;
    }

    /**
     * Gets the list temporal coverage.
     * 
     * @return the list temporal coverage
     */
    public List<String> getListTemporalCoverage() {
        return listTemporalCoverage;
    }

    /**
     * Sets the list temporal coverage.
     * 
     * @param listTemporalCoverage the list temporal coverage
     */
    public void setListTemporalCoverage(List<String> listTemporalCoverage) {
        this.listTemporalCoverage = listTemporalCoverage;
        computeTemporalCoverageAsDays();
    }

    /**
     * Compute temporal coverage as days.
     */
    private void computeTemporalCoverageAsDays() {
        if (listTemporalCoverage == null) {
            return;
        }
        if (listTemporalCoverage.isEmpty()) {
            return;
        }
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = NetCdfReader.parseDate(listTemporalCoverage.get(0));
            d2 = d1;
            if (listTemporalCoverage.size() > 1) {
                d2 = NetCdfReader.parseDate(listTemporalCoverage.get(1));
            }
        } catch (MotuInvalidDateException e) {
            // Do Nothing
        }

        if ((d1 != null) && (d2 != null)) {
            temporalCoverageInDays = (int) (java.lang.Math.abs(d1.getTime() - d2.getTime()) / TimeUtils.MILLISECS_PER_DAY) + 1;
        }
    }

    /**
     * Gets the list var.
     * 
     * @return the list var
     */
    public List<String> getListVar() {
        return listVar;
    }

    /**
     * Sets the list var.
     * 
     * @param listVar the list var
     */
    public void setListVar(List<String> listVar) {
        this.listVar = listVar;
    }

    /**
     * Gets the out.
     * 
     * @return the out
     */
    public Writer getOut() {
        return out;
    }

    /**
     * Sets the out.
     * 
     * @param out the out
     */
    public void setOut(Writer out) {
        this.out = out;
    }

    /**
     * Gets the product id.
     * 
     * @return the product id
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the product id.
     * 
     * @param productId the product id
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Gets the response format.
     * 
     * @return the response format
     */
    public OutputFormat getResponseFormat() {
        return responseFormat;
    }

    /**
     * Sets the response format.
     * 
     * @param responseFormat the response format
     */
    public void setResponseFormat(OutputFormat responseFormat) {
        this.responseFormat = responseFormat;
    }

    /**
     * Gets the service name.
     * 
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the service name.
     * 
     * @param serviceName the service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = null;
        if (serviceName != null) {
            this.serviceName = serviceName.trim();
        }
    }

    /**
     * Gets the location data.
     * 
     * @return the location data
     */
    public String getLocationData() {
        return locationData;
    }

    /**
     * Sets the location data.
     * 
     * @param locationData the location data
     */
    public void setLocationData(String locationData) {
        this.locationData = locationData;
    }

    /**
     * Verify parameters.
     * 
     * @throws MotuInconsistencyException the motu inconsistency exception
     */
    public void verifyParameters() throws MotuInconsistencyException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("verifyParameters() - entering");
        }

        if (StringUtils.isNullOrEmpty(locationData) && StringUtils.isNullOrEmpty(productId)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info(" empty locationData and empty productId");
                LOGGER.debug("verifyParameters() - exiting");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("verifyParameters() - exiting");
            }
            throw new MotuInconsistencyException("ERROR: neither location data nor product id parameters are filled - Choose one of them");
        }

        if (!StringUtils.isNullOrEmpty(locationData) && !StringUtils.isNullOrEmpty(productId)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info(" non empty locationData and non empty productId");
                LOGGER.debug("verifyParameters() - exiting");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("verifyParameters() - exiting");
            }
            throw new MotuInconsistencyException("ERROR: location data and product id parameters are not compatible - Choose only one of them");
        }

        if (StringUtils.isNullOrEmpty(serviceName) && !StringUtils.isNullOrEmpty(productId)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("empty serviceName  and non empty productId");
                LOGGER.debug("verifyParameters() - exiting");
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("verifyParameters() - exiting");
            }
            throw new MotuInconsistencyException("ERROR: product id parameter is filled but service name is empty. You have to fill it.");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("verifyParameters() - exiting");
        }
    }

    /**
     * To string.
     * 
     * @return the string
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ExtractionParameters[");
        buffer.append("serviceName:");
        buffer.append((serviceName == null ? "null" : serviceName));
        buffer.append(", listVar:");
        buffer.append((listVar == null ? "null" : listVar.toString()));
        buffer.append(", listTemporalCoverage:");
        buffer.append((listTemporalCoverage == null ? "null" : listTemporalCoverage.toString()));
        buffer.append(", listLatLonCoverage:");
        buffer.append((listLatLonCoverage == null ? "null" : listLatLonCoverage.toString()));
        buffer.append(", listDepthCoverage:");
        buffer.append((listLatLonCoverage == null ? "null" : listLatLonCoverage.toString()));
        buffer.append(", productId:");
        buffer.append((productId == null ? "null" : productId));
        buffer.append(", dataOutputFormat:");
        buffer.append((dataOutputFormat == null ? "null" : dataOutputFormat.toString()));
        buffer.append(", out:");
        buffer.append((out == null ? "null" : out.toString()));
        buffer.append(", responseFormat:");
        buffer.append((responseFormat == null ? "null" : responseFormat.toString()));
        buffer.append(", locationData:");
        buffer.append((locationData == null ? "null" : locationData));
        buffer.append(", userId:");
        buffer.append((userId == null ? "null" : userId));
        buffer.append(", anonymousUser:");
        buffer.append(anonymousUser);
        // buffer.append(", batchQueue:");
        // buffer.append(batchQueue);
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Gets the user id.
     * 
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     * 
     * @param userId the user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Checks if is anonymous user.
     * 
     * @return true, if is anonymous user
     */
    public boolean isAnonymousUser() {
        return anonymousUser;
    }

    /**
     * Sets the anonymous user.
     * 
     * @param anonymousUser the anonymous user
     */
    public void setAnonymousUser(boolean anonymousUser) {
        this.anonymousUser = anonymousUser;
    }

    /**
     * Gets the temporal coverage in days.
     * 
     * @return the temporal coverage in days
     */
    public int getTemporalCoverageInDays() {
        return temporalCoverageInDays;
    }

    // /**
    // * Checks if is batch queue.
    // *
    // * @return true, if is batch queue
    // */
    // public boolean isBatchQueue() {
    // return batchQueue;
    // }
    //
    // /**
    // * Sets the batch queue.
    // *
    // * @param batchQueue the batch queue
    // */
    // public void setBatchQueue(boolean batchQueue) {
    // this.batchQueue = batchQueue;
    // }

    /**
     * Gets the user host.
     * 
     * @return the user host
     */
    public String getUserHost() {
        return userHost;
    }

    /**
     * Sets the user host.
     * 
     * @param userHost the user host
     */
    public void setUserHost(String userHost) {
        this.userHost = userHost;
    }

}
