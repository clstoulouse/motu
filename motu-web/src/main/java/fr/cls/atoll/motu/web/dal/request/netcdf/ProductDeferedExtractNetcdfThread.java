package fr.cls.atoll.motu.web.dal.request.netcdf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.ObjectFactory;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.api.utils.JAXBWriter;
import fr.cls.atoll.motu.library.misc.data.ProductPersistent;
import fr.cls.atoll.motu.library.misc.data.SelectData;
import fr.cls.atoll.motu.library.misc.data.ServiceData;
import fr.cls.atoll.motu.library.misc.data.ServicePersistent;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.web.bll.exception.ExceptionUtils;
import fr.cls.atoll.motu.web.bll.request.ExtractionParameters;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class ProductDeferedExtractNetcdfThread extends Thread {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** The extraction parameters. */
    ExtractionParameters extractionParameters = null;

    /** The status mode response. */
    StatusModeResponse statusModeResponse = null;

    /** The product defered extract netcdf status file path name. */
    private String productDeferedExtractNetcdfStatusFilePathName = null;

    // /** The location d

    /**
     * The Constructor.
     *
     * @param statusModeResponse the status mode response
     * @param organizer the organizer
     * @param extractionParameters the extraction parameters
     */
    public ProductDeferedExtractNetcdfThread(StatusModeResponse statusModeResponse, ExtractionParameters extractionParameters) {
        this.statusModeResponse = statusModeResponse;
        this.productDeferedExtractNetcdfStatusFilePathName = null;
        this.extractionParameters = extractionParameters;
    }

    /**
     * The Constructor.
     *
     * @param productDeferedExtractNetcdfStatusFilePathName the product defered extract netcdf status file
     *            path name
     * @param organizer the organizer
     * @param extractionParameters the extraction parameters
     */
    public ProductDeferedExtractNetcdfThread(String productDeferedExtractNetcdfStatusFilePathName, ExtractionParameters extractionParameters) {

        this.statusModeResponse = null;
        this.productDeferedExtractNetcdfStatusFilePathName = productDeferedExtractNetcdfStatusFilePathName;
        this.extractionParameters = extractionParameters;

    }

    /**
     * Gets the writer.
     * 
     * @return the writer
     * 
     * @throws IOException the IO exception
     */
    public Writer createWriter() throws IOException {
        return new FileWriter(productDeferedExtractNetcdfStatusFilePathName);
    }

    /**
     * Run.
     */
    @Override
    public void run() {

        execute();

        if (productDeferedExtractNetcdfStatusFilePathName == null) {
            return;
        }
        try {
            JAXBWriter.getInstance().write(statusModeResponse, createWriter());
        } catch (Exception e) {
            try {
                ObjectFactory objectFactory = new ObjectFactory();
                StatusModeResponse statusModeResponse = objectFactory.createStatusModeResponse();
                ExceptionUtils.setStatusModeResponseException(e, statusModeResponse);
                JAXBWriter.getInstance().write(statusModeResponse, createWriter());
            } catch (Exception e2) {
                LOGGER.error("status writing error - " + e2.getMessage(), e2);
            }
        }
    }

    /**
     * Sets the status done.
     * 
     * @param product the product
     * @param statusModeResponse the status mode response
     * 
     * @throws MotuException the motu exception
     */
    public static void setStatusDone(StatusModeResponse statusModeResponse, Product product) throws MotuException {

        String downloadUrlPath = product.getDownloadUrlPath();
        String locationData = product.getExtractLocationData();

        File fileData = new File(locationData);

        Long size = fileData.length();

        Date lastModified = new Date(fileData.lastModified());

        statusModeResponse.setStatus(StatusModeType.DONE);
        statusModeResponse.setMsg(downloadUrlPath);
        statusModeResponse.setSize(size.doubleValue());
        statusModeResponse.setDateProc(Organizer.dateToXMLGregorianCalendar(lastModified));
        statusModeResponse.setCode(ErrorType.OK);
        statusModeResponse.setRemoteUri(downloadUrlPath);
        statusModeResponse.setLocalUri(locationData);

    }

    /**
     * Execute.
     */
    private void execute() {
        try {
            setStatusDone(statusModeResponse, extractData(extractionParameters));
        } catch (Exception e) {
            LOGGER.error("execute()", e);
            ExceptionUtils.setStatusModeResponseException(e, statusModeResponse);
        }

    }

    /**
     * Extract data.
     * 
     * @param params the params
     * 
     * @return the product
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuInconsistencyException the motu inconsistency exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Product extractData(ExtractionParameters params) throws MotuInconsistencyException, MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfAttributeException, NetCdfVariableNotFoundException, IOException {

        // Verify input parameters and raise error if necessary
        params.verifyParameters();
        Product product = null;

        // -------------------------------------------------
        // Data extraction OPENDAP
        // -------------------------------------------------
        if (!StringUtils.isNullOrEmpty(params.getLocationData())) {
            product = extractData(params.getServiceName(),
                                  params.getLocationData(),
                                  null,
                                  params.getListVar(),
                                  params.getListTemporalCoverage(),
                                  params.getListLatLonCoverage(),
                                  params.getListDepthCoverage(),
                                  null,
                                  params.getDataOutputFormat(),
                                  params.getOut(),
                                  params.getResponseFormat(),
                                  null);
        } else if (!StringUtils.isNullOrEmpty(params.getServiceName()) && !StringUtils.isNullOrEmpty(params.getProductId())) {
            product = extractData(params.getServiceName(),
                                  params.getListVar(),
                                  params.getListTemporalCoverage(),
                                  params.getListLatLonCoverage(),
                                  params.getListDepthCoverage(),
                                  params.getProductId(),
                                  null,
                                  params.getDataOutputFormat(),
                                  params.getOut(),
                                  params.getResponseFormat());
        } else {
            throw new MotuInconsistencyException(String.format("ERROR in extractData: inconsistency parameters : %s", params.toString()));
        }

        return product;
    }

    /**
     * Extracts data from a location data (url , filename) according to criteria (geographical and/or temporal
     * and/or logical expression).
     * 
     * @param product product to be extracted
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the netcdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void extractData(Product product,
                             List<String> listVar,
                             List<String> listTemporalCoverage,
                             List<String> listLatLonCoverage,
                             List<String> listDepthCoverage,
                             SelectData selectData,
                             OutputFormat dataOutputFormat) throws MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException,
                                     MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
                                     MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
                                     NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException,
                                     IOException {
        // CSON: StrictDuplicateCode.

        if (this.currentService == null) {
            // Create a virtual service with default option
            createVirtualService();
        }

        extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage, selectData, dataOutputFormat, null, null);
    }

    // CSOFF: StrictDuplicateCode : normal duplication code.
    /**
     * Extracts data from a location data (url , filename) according to criteria (geographical and/or temporal
     * and/or logical expression).
     * 
     * @param product product to download
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param responseFormat response output format (HTML, XML, Ascii).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param out writer in which response of the extraction will be list.
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Product extractData(Product product,
                                List<String> listVar,
                                List<String> listTemporalCoverage,
                                List<String> listLatLonCoverage,
                                List<String> listDepthCoverage,
                                SelectData selectData,
                                OutputFormat dataOutputFormat,
                                Writer out,
                                OutputFormat responseFormat) throws MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException,
                                        MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
                                        MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
                                        NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException,
                                        IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("extractData() - entering");
        }

        // CSON: StrictDuplicateCode.

        if (this.currentService == null) {
            // Create a virtual service with default option
            createVirtualService();
        }

        if (responseFormat == null || out == null) {
            currentService.extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage, selectData, dataOutputFormat);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("extractData() - exiting");
            }
            return product;
        }

        switch (responseFormat) {

        case HTML:
            product = currentService.extractDataHTML(product,
                                                     listVar,
                                                     listTemporalCoverage,
                                                     listLatLonCoverage,
                                                     listDepthCoverage,
                                                     selectData,
                                                     dataOutputFormat,
                                                     out);
            /*
             * extractDataHTML(productId, listVar, geoCriteria, temporalCriteria, selectData, out,
             * dataOutputFormat);
             */
            break;

        case XML:
        case ASCII:
            throw new MotuNotImplementedException(String.format("extractData - Format %s not implemented", responseFormat.toString()));
            // break;

        default:
            throw new MotuException("extractData - Unknown Format");
            // break;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("extractData() - exiting");
        }
        return product;

    }

    // CSOFF: StrictDuplicateCode : normal duplication code.
    /**
     * Extracts data from a location data (url , filename) according to criteria (geographical and/or temporal
     * and/or logical expression).
     * 
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param locationData locaton of the data to download (url, filename)
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Product extractData(String locationData,
                                String locationDataNCSS,
                                List<String> listVar,
                                List<String> listTemporalCoverage,
                                List<String> listLatLonCoverage,
                                List<String> listDepthCoverage,
                                SelectData selectData,
                                OutputFormat dataOutputFormat) throws MotuInvalidDateException, MotuInvalidDepthException,
                                        MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
                                        MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException,
                                        MotuInvalidDepthRangeException, NetCdfVariableException, MotuNoVarException, NetCdfAttributeException,
                                        NetCdfVariableNotFoundException, IOException {
        // CSON: StrictDuplicateCode.

        return extractData(locationData,
                           locationDataNCSS,
                           listVar,
                           listTemporalCoverage,
                           listLatLonCoverage,
                           listDepthCoverage,
                           selectData,
                           dataOutputFormat,
                           null,
                           null,
                           null);
    }

    // CSOFF: StrictDuplicateCode : normal duplication code.
    /**
     * Extracts data from a location data (url , filename) according to criteria (geographical and/or temporal
     * and/or logical expression).
     * 
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param locationData locaton of the data to download (url, filename)
     * @param responseFormat response output format (HTML, XML, Ascii).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param out writer in which response of the extraction will be list.
     * @param productId the product id
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Product extractData(String locationData,
                                String locationDataNCSS,
                                List<String> listVar,
                                List<String> listTemporalCoverage,
                                List<String> listLatLonCoverage,
                                List<String> listDepthCoverage,
                                SelectData selectData,
                                OutputFormat dataOutputFormat,
                                Writer out,
                                OutputFormat responseFormat,
                                String productId) throws MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException,
                                        MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
                                        MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
                                        NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException,
                                        IOException {
        // CSON: StrictDuplicateCode.

        Product product = getProductInformation(locationData);
        // Update ID

        if (!StringUtils.isNullOrEmpty(productId)) {
            product.setProductId(productId);
        }
        // Update NCSS link
        product.setLocationDataNCSS(locationDataNCSS);

        extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage, selectData, dataOutputFormat, out, responseFormat);

        return product;
    }

    /**
     * Gets product's informations related to a service (AVISO, Mercator, ....).
     * 
     * @param locationData url of the product to load metadata
     * 
     * @return product instance with loaded metadata
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    private Product getProductInformation(String locationData) throws MotuException, MotuNotImplementedException, NetCdfAttributeException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getProductInformation() - entering");
        }

        if (this.currentService == null) {
            // Create a virtual service with default option
            createVirtualService();
        }

        Product product = currentService.getProductInformationFromLocation(locationData);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getProductInformation() - exiting");
        }
        return product;
    }

    /**
     * Extracts data from a service name and a product id and according to criteria (geographical and/or
     * temporal and/or logical expression).
     * 
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param serviceName name of the service for the product
     * @param productId id of the product
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Product extractData(String serviceName,
                                List<String> listVar,
                                List<String> listTemporalCoverage,
                                List<String> listLatLonCoverage,
                                List<String> listDepthCoverage,
                                String productId,
                                SelectData selectData,
                                OutputFormat dataOutputFormat) throws MotuInvalidDateException, MotuInvalidDepthException,
                                        MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
                                        MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException,
                                        MotuInvalidDepthRangeException, NetCdfVariableException, MotuNoVarException, NetCdfAttributeException,
                                        NetCdfVariableNotFoundException, IOException {
        // CSON: StrictDuplicateCode

        return extractData(serviceName,
                           listVar,
                           listTemporalCoverage,
                           listLatLonCoverage,
                           listDepthCoverage,
                           productId,
                           selectData,
                           dataOutputFormat,
                           null,
                           null);

    }

    // CSOFF: StrictDuplicateCode : normal duplication code.
    /**
     * Extracts data from a service name and a product id and according to criteria (geographical and/or
     * temporal and/or logical expression).
     * 
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param responseFormat response output format (HTML, XML, Ascii).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param serviceName name of the service for the product
     * @param productId id of the product
     * @param out writer in which response of the extraction will be list.
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Product extractData(String serviceName,
                                List<String> listVar,
                                List<String> listTemporalCoverage,
                                List<String> listLatLonCoverage,
                                List<String> listDepthCoverage,
                                String productId,
                                SelectData selectData,
                                OutputFormat dataOutputFormat,
                                Writer out,
                                OutputFormat responseFormat) throws MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException,
                                        MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
                                        MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
                                        NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException,
                                        IOException {
        // CSON: StrictDuplicateCode

        ServicePersistent servicePersistent = null;
        if (!Organizer.servicesPersistentContainsKey(serviceName)) {
            loadCatalogInfo(serviceName);
        }

        setCurrentService(serviceName);

        servicePersistent = Organizer.getServicesPersistent(serviceName);

        ProductPersistent productPersistent = servicePersistent.getProductsPersistent(productId);
        if (productPersistent == null) {
            throw new MotuException(String.format("ERROR in extractData - product '%s' not found", productId));
        }

        String locationData = getLocationData(productPersistent);
        String locationDataNCSS = productPersistent.getUrlNCSS();

        Product product = extractData(serviceName,
                                      locationData,
                                      locationDataNCSS,
                                      listVar,
                                      listTemporalCoverage,
                                      listLatLonCoverage,
                                      listDepthCoverage,
                                      selectData,
                                      dataOutputFormat,
                                      out,
                                      responseFormat,
                                      productId);
        return product;
    }

    /**
     * Gets the location data.
     * 
     * @param productPersistent the product persistent
     * @return the location data
     */
    protected String getLocationData(ProductPersistent productPersistent) {
        return getLocationData(this.currentService, productPersistent);
    }

    /**
     * Gets the location data.
     * 
     * @param service the service
     * @param productPersistent the product persistent
     * @return the location data
     */
    protected String getLocationData(ServiceData service, ProductPersistent productPersistent) {
        String locationData = "";

        if (service == null) {
            return locationData;
        }
        if (productPersistent == null) {
            return locationData;
        }

        if (service.getCatalogType() == CatalogData.CatalogType.FTP) {
            locationData = productPersistent.getUrlMetaData();
        } else {
            locationData = productPersistent.getUrl();
        }
        return locationData;
    }

    // CSOFF: StrictDuplicateCode : normal duplication code.
    /**
     * Extracts data from a location data (url , filename) and according to criteria (geographical and/or
     * temporal and/or logical expression).
     * 
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param locationData locaton of the data to download (url, filename)
     * @param responseFormat response output format (HTML, XML, Ascii).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param serviceName name of the service for the product
     * @param out writer in which response of the extraction will be list.
     * @param productId the product id
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Product extractData(String serviceName,
                                String locationData,
                                String locationDataNCSS,
                                List<String> listVar,
                                List<String> listTemporalCoverage,
                                List<String> listLatLonCoverage,
                                List<String> listDepthCoverage,
                                SelectData selectData,
                                OutputFormat dataOutputFormat,
                                Writer out,
                                OutputFormat responseFormat,
                                String productId) throws MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException,
                                        MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
                                        MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
                                        NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException,
                                        IOException {

        // CSON: StrictDuplicateCode
        if (!StringUtils.isNullOrEmpty(serviceName)) {
            setCurrentService(serviceName);
        }

        return extractData(locationData,
                           locationDataNCSS,
                           listVar,
                           listTemporalCoverage,
                           listLatLonCoverage,
                           listDepthCoverage,
                           selectData,
                           dataOutputFormat,
                           out,
                           responseFormat,
                           productId);
    }

}
