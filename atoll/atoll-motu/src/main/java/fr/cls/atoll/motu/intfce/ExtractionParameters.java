package fr.cls.atoll.motu.intfce;

import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import fr.cls.atoll.motu.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.netcdf.NetCdfReader;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:26 $
 */
public class ExtractionParameters implements Cloneable {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(ExtractionParameters.class);

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
        Organizer.Format dataOutputFormat,
        Writer out,
        Organizer.Format responseFormat) {

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
        Organizer.Format dataOutputFormat,
        Writer out,
        Organizer.Format responseFormat,
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
        Organizer.Format dataOutputFormat,
        Writer out,
        Organizer.Format responseFormat) {

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
        Organizer.Format dataOutputFormat,
        Writer out,
        Organizer.Format responseFormat,
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
        Organizer.Format dataOutputFormat,
        Writer out,
        Organizer.Format responseFormat) {

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
        Organizer.Format dataOutputFormat,
        Writer out,
        Organizer.Format responseFormat,
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
        Organizer.Format dataOutputFormat,
        Writer out,
        Organizer.Format responseFormat,
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

    private int temporalCoverageInDays = -1;

    /** The list lat lon coverage. */
    private List<String> listLatLonCoverage = null;

    /** The list depth coverage. */
    private List<String> listDepthCoverage = null;

    /** The product id. */
    private String productId = null;

    /** The data output format. */
    private Organizer.Format dataOutputFormat = Organizer.Format.NETCDF;

    /** The out. */
    private Writer out = null;

    /** The response format. */
    private Organizer.Format responseFormat = Organizer.Format.HTML;

    /** The location data. */
    private String locationData = null;

    /** The user id. */
    private String userId = null;

    /** The user host. */
    private String userHost = null;


    /** The anonymous user. */
    private boolean anonymousUser = true;

    /** The batch queue. */
    protected boolean batchQueue = false;
    

    /**
     * Test if a string is null or empty.
     * 
     * @param value string to be tested.
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
    public Organizer.Format getDataOutputFormat() {
        return dataOutputFormat;
    }

    /**
     * Sets the data output format.
     * 
     * @param dataOutputFormat the data output format
     */
    public void setDataOutputFormat(Organizer.Format dataOutputFormat) {
        this.dataOutputFormat = dataOutputFormat;
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
            temporalCoverageInDays = (int) (java.lang.Math.abs(d1.getTime() - d2.getTime()) / Organizer.MILLISECS_PER_DAY) + 1;
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
    public Organizer.Format getResponseFormat() {
        return responseFormat;
    }

    /**
     * Sets the response format.
     * 
     * @param responseFormat the response format
     */
    public void setResponseFormat(Organizer.Format responseFormat) {
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("verifyParameters() - entering");
        }

        if (Organizer.isNullOrEmpty(locationData) && Organizer.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" empty locationData and empty productId");
                LOG.debug("verifyParameters() - exiting");
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("verifyParameters() - exiting");
            }
            throw new MotuInconsistencyException("ERROR: neither location data nor product id parameters are filled - Choose one of them");
        }

        if (!Organizer.isNullOrEmpty(locationData) && !Organizer.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" non empty locationData and non empty productId");
                LOG.debug("verifyParameters() - exiting");
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("verifyParameters() - exiting");
            }
            throw new MotuInconsistencyException("ERROR: location data and product id parameters are not compatible - Choose only one of them");
        }

        if (Organizer.isNullOrEmpty(serviceName) && !Organizer.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info("empty serviceName  and non empty productId");
                LOG.debug("verifyParameters() - exiting");
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("verifyParameters() - exiting");
            }
            throw new MotuInconsistencyException("ERROR: product id parameter is filled but service name is empty. You have to fill it.");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("verifyParameters() - exiting");
        }
    }

    /**
     * To string.
     * 
     * @return the string
     */
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
        buffer.append(", batchQueue:");
        buffer.append(batchQueue);
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
    
  /**
     * Checks if is batch queue.
     * 
     * @return true, if is batch queue
     */
    public boolean isBatchQueue() {
        return batchQueue;
    }

    /**
     * Sets the batch queue.
     * 
     * @param batchQueue the batch queue
     */
    public void setBatchQueue(boolean batchQueue) {
        this.batchQueue = batchQueue;
    }

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
