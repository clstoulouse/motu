package fr.cls.atoll.motu.web.usl.request.actions;

import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_END_DATE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_HIGH_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_LOW_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_START_DATE;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasig.cas.client.util.AssertionHolder;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.ObjectFactory;
import fr.cls.atoll.motu.api.message.xml.RequestSize;
import fr.cls.atoll.motu.api.utils.JAXBWriter;
import fr.cls.atoll.motu.library.misc.data.ProductPersistent;
import fr.cls.atoll.motu.library.misc.data.ServicePersistent;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingQueueDataCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.queueserver.QueueServerManagement;
import fr.cls.atoll.motu.web.bll.exception.ExceptionUtils;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DatasetFtp;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.DepthHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LatitudeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LongitudeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.TemporalHTTPParameterValidator;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites) <br>
 * <br>
 * This interface is used to download data with subsetting.<br>
 * Operation invocation consists in performing an HTTP GET request.<br>
 * Input parameters are the following: [x,y] is the cardinality<br>
 * <ul>
 * <li><b>action</b>: [1]: {@link #ACTION_NAME}</li>
 * <li><b>service</b>: [1]: identifier of the service that provides the desired data set to order.</li>
 * <li><b>product</b>: [1]: identifier of the desired data set to order.</li>
 * <li><b>variable</b>: [0,n]: physical variables to be extracted from the product. no variable is set, all
 * the variables of the dataset are extracted.</li>
 * <li><b>y_lo</b>: [0,1]: low latitude of a geographic extraction. Default value is -90.</li>
 * <li><b>y_hi</b>: [0,1]: high latitude of a geographic extraction. Default value is 90.</li>
 * <li><b>x_lo</b>: [0,1]: low longitude of a geographic extraction. Default value is -180.</li>
 * <li><b>x_hi</b>: [0,1]: high longitude of a geographic extraction. Default value is 180.</li>
 * <li><b>z_lo</b>: [0,1]: low vertical depth . Default value is 0.</li>
 * <li><b>z_hi</b>: [0,1]: high vertical depth. Default value is 180.</li>
 * <li><b>t_lo</b>: [0,1]: Start date of a temporal extraction. If not set, the default value is the first
 * date/time available for the dataset. Format is yyy-mm-dd or yyyy-dd h:m:s or yyyy-ddTh:m:s.</li>
 * <li><b>t_hi</b>: [0,1]: End date of a temporal extraction. If not set, the default value is the last
 * date/time available for the dataset. Format is yyy-mm-dd or yyyy-dd h:m:s or yyyy-ddTh:m:s.</li>
 * </ul>
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class GetSizeAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "getsize";

    private DepthHTTPParameterValidator depthLowHTTPParameterValidator;
    private DepthHTTPParameterValidator depthHighHTTPParameterValidator;
    private LatitudeHTTPParameterValidator latitudeLowHTTPParameterValidator;
    private LatitudeHTTPParameterValidator latitudeHighHTTPParameterValidator;
    private LongitudeHTTPParameterValidator longitudeLowHTTPParameterValidator;
    private LongitudeHTTPParameterValidator longitudeHighHTTPParameterValidator;

    private TemporalHTTPParameterValidator startDateTemporalHTTPParameterValidator;
    private TemporalHTTPParameterValidator endDateTemporalHighHTTPParameterValidator;

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;

    /**
     * 
     * @param actionName_
     */
    public GetSizeAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, request, response, session);

        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SERVICE,
                CommonHTTPParameters.getServiceFromRequest(getRequest()));

        latitudeLowHTTPParameterValidator = new LatitudeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_LOW_LAT,
                CommonHTTPParameters.getLatitudeLowFromRequest(getRequest()),
                "-90");
        latitudeHighHTTPParameterValidator = new LatitudeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_HIGH_LAT,
                CommonHTTPParameters.getLatitudeLowFromRequest(getRequest()),
                "90");
        longitudeLowHTTPParameterValidator = new LongitudeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_LOW_LON,
                CommonHTTPParameters.getLongitudeLowFromRequest(getRequest()),
                "-180");
        longitudeHighHTTPParameterValidator = new LongitudeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_HIGH_LON,
                CommonHTTPParameters.getLongitudeHighFromRequest(getRequest()),
                "180");

        depthLowHTTPParameterValidator = new DepthHTTPParameterValidator(PARAM_LOW_Z, CommonHTTPParameters.getDepthLowFromRequest(getRequest()), "0");
        String depthHighParameterValue = CommonHTTPParameters.getDepthHighFromRequest(getRequest());
        if (StringUtils.isNullOrEmpty(depthHighParameterValue)) {
            depthHighParameterValue = depthLowHTTPParameterValidator.getParameterValue();
        }
        depthHighHTTPParameterValidator = new DepthHTTPParameterValidator(PARAM_HIGH_Z, depthHighParameterValue);

        startDateTemporalHTTPParameterValidator = new TemporalHTTPParameterValidator(
                PARAM_START_DATE,
                CommonHTTPParameters.getStartDateFromRequest(getRequest()));
        endDateTemporalHighHTTPParameterValidator = new TemporalHTTPParameterValidator(
                PARAM_END_DATE,
                CommonHTTPParameters.getEndDateFromRequest(getRequest()));
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ServletException
     */
    @Override
    protected void process() throws MotuException {
        retrieveSize();
    }

    private void retrieveSize() throws MotuException {
        try {
            getAmountDataSize(createExtractionParameters(), getResponse());
        } catch (ServletException | IOException | JAXBException e) {
            throw new MotuException("Error while computing getAmountDataSize", e);
        }
    }

    private ExtractionParameters createExtractionParameters() throws IOException {
        Writer out = null;
        OutputFormat responseFormat = null;

        out = getResponse().getWriter();
        responseFormat = OutputFormat.HTML;

        ExtractionParameters extractionParameters = new ExtractionParameters(
                serviceHTTPParameterValidator.getParameterValueValidated(),
                CommonHTTPParameters.getDataFromParameter(getRequest()),
                CommonHTTPParameters.getVariablesAsListFromParameter(getRequest()),

                startDateTemporalHTTPParameterValidator.getParameterValue(),
                endDateTemporalHighHTTPParameterValidator.getParameterValue(),

                longitudeLowHTTPParameterValidator.getParameterValueValidated(),
                longitudeHighHTTPParameterValidator.getParameterValueValidated(),
                latitudeLowHTTPParameterValidator.getParameterValueValidated(),
                latitudeHighHTTPParameterValidator.getParameterValueValidated(),

                depthLowHTTPParameterValidator.getParameterValueValidated(),
                depthHighHTTPParameterValidator.getParameterValueValidated(),

                getProductId(),

                getLoginOrUserHostname(),
                isAnAnonymousUser());

        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());
        return extractionParameters;
    }

    /**
     * Gets the amount data size.
     *
     * @param extractionParameters the extraction parameters
     * @param response the response
     * @return the amount data size
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     * @throws JAXBException
     */
    private void getAmountDataSize(ExtractionParameters extractionParameters, HttpServletResponse response)
            throws ServletException, IOException, JAXBException {

        try {
            getAmountDataSize(extractionParameters);
        } catch (MotuMarshallException e) {
            response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        } catch (MotuExceptionBase e) {
            // Do nothing error is in response error code
            // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        }
    }

    /**
     * Gets the amount data size.
     * 
     * @param batchQueue the batch queue
     * @param listVar the list var
     * @param locationData the location data
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param out the out
     * @param productId the product id
     * 
     * @return the amount data size
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuMarshallException the motu marshall exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     * @throws JAXBException
     */
    public Product getAmountDataSize(String locationData,
                                     List<String> listVar,
                                     List<String> listTemporalCoverage,
                                     List<String> listLatLonCoverage,
                                     List<String> listDepthCoverage,
                                     Writer out,
                                     boolean batchQueue,
                                     String productId) throws MotuException, MotuMarshallException, MotuInvalidDateException,
                                             MotuInvalidDepthException, MotuInvalidLatitudeException, MotuInvalidLongitudeException,
                                             MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
                                             MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException, NetCdfVariableException,
                                             MotuNoVarException, NetCdfVariableNotFoundException, JAXBException, IOException {
        Product product = null;

        RequestSize requestSize = null;
        try {
            product = getAmountDataSize(locationData, productId, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage);
            requestSize = initRequestSize(product, batchQueue);
        } catch (MotuException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDateException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDepthException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLatitudeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLongitudeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDateRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuExceedingCapacityException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuNotImplementedException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLatLonRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDepthRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (NetCdfVariableException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuNoVarException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (NetCdfVariableNotFoundException e) {
            marshallRequestSize(e, out);
            throw e;
        }
        marshallRequestSize(requestSize, batchQueue, out);

        return product;
    }

    /**
     * Inits the request size.
     * 
     * @param batchQueue the batch queue
     * @param size the size
     * @param isFtp type of request
     * 
     * @return the request size
     */
    public static RequestSize initRequestSize(double size, boolean batchQueue, boolean isFtp) {

        RequestSize requestSize = createRequestSize();

        requestSize.setSize(size);
        requestSize.setCode(ErrorType.OK);
        requestSize.setMsg(ErrorType.OK.toString());

        if (size < 0) {
            ExceptionUtils.setError(requestSize, new MotuException("size can't be computed and the cause is unspecified"));
            return requestSize;
        }

        double maxAllowedSizeToSet = 0d;
        double maxAllowedSize = 0d;

        try {
            if (isFtp)
                maxAllowedSize = Organizer.convertFromMegabytesToBytes(Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue());
            else
                maxAllowedSize = Organizer.convertFromMegabytesToBytes(Organizer.getMotuConfigInstance().getMaxSizePerFileTDS().doubleValue());
        } catch (MotuException e) {
            ExceptionUtils.setError(requestSize, e);
            return requestSize;
        }

        MotuExceptionBase exceptionBase = null;

        if (size > maxAllowedSize) {
            exceptionBase = new MotuExceedingCapacityException(
                    Organizer.convertFromBytesToMegabytes(size),
                    Organizer.convertFromBytesToMegabytes(maxAllowedSize));
        }

        maxAllowedSizeToSet = maxAllowedSize;

        if (QueueServerManagement.hasInstance()) {
            double maxDataThreshold = 0d;
            try {
                maxDataThreshold = Organizer.convertFromMegabytesToBytes(QueueServerManagement.getInstance().getMaxDataThreshold(batchQueue));
            } catch (MotuException e) {
                ExceptionUtils.setError(requestSize, e);
                return requestSize;
            }
            if (size > maxDataThreshold) {
                exceptionBase = new MotuExceedingQueueDataCapacityException(
                        Organizer.convertFromBytesToMegabytes(size),
                        maxDataThreshold,
                        batchQueue);
            }
            maxAllowedSizeToSet = maxAllowedSizeToSet > maxDataThreshold ? maxDataThreshold : maxAllowedSizeToSet;
        }

        requestSize.setMaxAllowedSize(maxAllowedSizeToSet);

        if (exceptionBase != null) {
            ExceptionUtils.setError(requestSize, exceptionBase);
        }
        if (size > maxAllowedSize) {
            exceptionBase = new MotuExceedingCapacityException(
                    Organizer.convertFromBytesToMegabytes(size),
                    Organizer.convertFromBytesToMegabytes(maxAllowedSize));
        }

        return requestSize;
    }

    /**
     * Inits the request size.
     * 
     * @param product the product
     * @param batchQueue the batch queue
     * 
     * @return the request size
     * 
     * @throws MotuException the motu exception
     */
    public static RequestSize initRequestSize(Product product, boolean batchQueue) throws MotuException {
        if (product == null) {
            throw new MotuException("ERROR in Organizer.initRequestSize- Product is null");
        }

        // Check type (TDS/FTP) --> different maximum request sizes
        boolean isFtp = false;
        if (product.getDataset() instanceof DatasetFtp) {
            isFtp = true;
        }

        return initRequestSize(product.getAmountDataSizeAsBytes(), batchQueue, isFtp);
    }

    /**
     * Gets the amount data size.
     * 
     * @param params the params
     * 
     * @return the amount data size
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuInconsistencyException the motu inconsistency exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuMarshallException the motu marshall exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     * @throws JAXBException
     */
    public Product getAmountDataSize(ExtractionParameters params)
            throws MotuInconsistencyException, MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException,
            MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
            MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException, NetCdfVariableException, MotuNoVarException, NetCdfAttributeException,
            NetCdfVariableNotFoundException, MotuMarshallException, JAXBException, IOException {

        params.verifyParameters();
        Product product = null;

        if (!Organizer.isNullOrEmpty(params.getLocationData())) {
            product = getAmountDataSize(params.getLocationData(),
                                        params.getListVar(),
                                        params.getListTemporalCoverage(),
                                        params.getListLatLonCoverage(),
                                        params.getListDepthCoverage(),
                                        params.getOut(),
                                        params.isBatchQueue(),
                                        null);
        } else if (!Organizer.isNullOrEmpty(params.getServiceName()) && !Organizer.isNullOrEmpty(params.getProductId())) {
            product = getAmountDataSize(params.getServiceName(),
                                        params.getListVar(),
                                        params.getListTemporalCoverage(),
                                        params.getListLatLonCoverage(),
                                        params.getListDepthCoverage(),
                                        params.getProductId(),
                                        params.getOut(),
                                        params.isBatchQueue());
        } else {
            throw new MotuInconsistencyException(String.format("ERROR in getAmountDataSize: inconsistency parameters : %s", params.toString()));
        }

        return product;
    }

    /**
     * Gets the amount data size.
     * 
     * @param listVar the list var
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param serviceName the service name
     * @param productId the product id
     * 
     * @return the amount data size
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
     */
    public Product getAmountDataSize(String serviceName,
                                     List<String> listVar,
                                     List<String> listTemporalCoverage,
                                     List<String> listLatLonCoverage,
                                     List<String> listDepthCoverage,
                                     String productId) throws MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException,
                                             MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
                                             MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException,
                                             MotuInvalidDepthRangeException, NetCdfVariableException, MotuNoVarException, NetCdfAttributeException,
                                             NetCdfVariableNotFoundException {

        ServicePersistent servicePersistent = null;
        if (!Organizer.servicesPersistentContainsKey(serviceName)) {
            loadCatalogInfo(serviceName);
        }

        setCurrentService(serviceName);

        servicePersistent = Organizer.getServicesPersistent(serviceName);

        ProductPersistent productPersistent = servicePersistent.getProductsPersistent(productId);
        if (productPersistent == null) {
            throw new MotuException(String.format("ERROR in getAmountDataSize - product '%s' not found", productId));
        }

        String locationData = getLocationData(productPersistent);

        Product product = getAmountDataSize(locationData, productId, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage);

        return product;

    }

    /**
     * Gets the amount data size.
     * 
     * @param batchQueue the batch queue
     * @param listVar the list var
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param out the out
     * @param productId the product id
     * @param serviceName the service name
     * 
     * @return the amount data size
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuMarshallException the motu marshall exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     * @throws JAXBException
     */
    public Product getAmountDataSize(String serviceName,
                                     List<String> listVar,
                                     List<String> listTemporalCoverage,
                                     List<String> listLatLonCoverage,
                                     List<String> listDepthCoverage,
                                     String productId,
                                     Writer out,
                                     boolean batchQueue) throws MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException,
                                             MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
                                             MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException,
                                             MotuInvalidDepthRangeException, NetCdfVariableException, MotuNoVarException, NetCdfAttributeException,
                                             NetCdfVariableNotFoundException, MotuMarshallException, JAXBException, IOException {
        Product product = null;

        RequestSize requestSize = null;
        try {
            product = getAmountDataSize(serviceName, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage, productId);
            requestSize = initRequestSize(product, batchQueue);
        } catch (MotuException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDateException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDepthException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLatitudeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLongitudeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDateRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuExceedingCapacityException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuNotImplementedException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLatLonRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDepthRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (NetCdfVariableException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuNoVarException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (NetCdfVariableNotFoundException e) {
            marshallRequestSize(e, out);
            throw e;
        }
        marshallRequestSize(requestSize, batchQueue, out);

        return product;
    }

    /**
     * Marshall request size.
     * 
     * @param ex the ex
     * @param writer the writer
     * 
     * @throws MotuMarshallException the motu marshall exception
     * @throws JAXBException
     * @throws IOException
     */
    public static void marshallRequestSize(MotuExceptionBase ex, Writer writer) throws MotuMarshallException, JAXBException, IOException {

        if (writer == null) {
            return;
        }

        RequestSize requestSize = createRequestSize(ex);
        JAXBWriter.getInstance().write(requestSize, writer);
        writer.flush();
        writer.close();
    }

    /**
     * Creates the request size.
     * 
     * @return the request size
     */
    public static RequestSize createRequestSize() {

        ObjectFactory objectFactory = new ObjectFactory();

        RequestSize requestSize = objectFactory.createRequestSize();
        requestSize.setSize(-1d);
        ExceptionUtils.setError(requestSize, new MotuException("If you see that message, the request has failed and the error has not been filled"));
        return requestSize;

    }

    /**
     * Creates the request size.
     * 
     * @param e the e
     * 
     * @return the request size
     */
    public static RequestSize createRequestSize(MotuExceptionBase e) {

        RequestSize requestSize = createRequestSize();
        ExceptionUtils.setError(requestSize, e);
        return requestSize;

    }

    /**
     * Marshall request size.
     * 
     * @param batchQueue the batch queue
     * @param requestSize the request size
     * @param writer the writer
     * 
     * @throws MotuMarshallException the motu marshall exception
     * @throws JAXBException
     * @throws IOException
     */
    public static void marshallRequestSize(RequestSize requestSize, boolean batchQueue, Writer writer)
            throws MotuMarshallException, JAXBException, IOException {
        if (writer == null) {
            return;
        }

        if (requestSize == null) {
            requestSize = initRequestSize(-1d, batchQueue, false);
        }
        JAXBWriter.getInstance().write(requestSize, writer);
        writer.flush();
        writer.close();
    }

    /**
     * Gets the amount data size.
     * 
     * @param listVar the list var
     * @param locationData the location data
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param productId the product id
     * 
     * @return the amount data size
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public Product getAmountDataSize(String locationData,
                                     String productId,
                                     List<String> listVar,
                                     List<String> listTemporalCoverage,
                                     List<String> listLatLonCoverage,
                                     List<String> listDepthCoverage) throws MotuInvalidDateException, MotuInvalidDepthException,
                                             MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException,
                                             MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
                                             MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException, NetCdfVariableException,
                                             MotuNoVarException, NetCdfVariableNotFoundException {
        Product product = null;
        try {
            product = getProductInformation(locationData);
            if (!Organizer.isNullOrEmpty(productId)) {
                product.setProductId(productId);
            }

            currentService.computeAmountDataSize(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage);
        } catch (NetCdfAttributeException e) {
            // Do nothing;
        }

        return product;
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        getServiceHTTPParameterValidator().validate();

        getLatitudeLowHTTPParameterValidator().validate();
        getLatitudeHighHTTPParameterValidator().validate();
        getLongitudeLowHTTPParameterValidator().validate();
        getLongitudeHighHTTPParameterValidator().validate();

        getDepthLowHTTPParameterValidator().validate();
        getDepthHighHTTPParameterValidator().validate();

        getStartDateTemporalHTTPParameterValidator().validate();
        getEndDateTemporalHighHTTPParameterValidator().validate();
    }

    /**
     * Valeur de depthLowHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public DepthHTTPParameterValidator getDepthLowHTTPParameterValidator() {
        return depthLowHTTPParameterValidator;
    }

    /**
     * Valeur de depthHighHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public DepthHTTPParameterValidator getDepthHighHTTPParameterValidator() {
        return depthHighHTTPParameterValidator;
    }

    /**
     * Valeur de latitudeLowHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public LatitudeHTTPParameterValidator getLatitudeLowHTTPParameterValidator() {
        return latitudeLowHTTPParameterValidator;
    }

    /**
     * Valeur de latitudeHighHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public LatitudeHTTPParameterValidator getLatitudeHighHTTPParameterValidator() {
        return latitudeHighHTTPParameterValidator;
    }

    /**
     * Valeur de longitudeLowHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public LongitudeHTTPParameterValidator getLongitudeLowHTTPParameterValidator() {
        return longitudeLowHTTPParameterValidator;
    }

    /**
     * Valeur de longitudeHighHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public LongitudeHTTPParameterValidator getLongitudeHighHTTPParameterValidator() {
        return longitudeHighHTTPParameterValidator;
    }

    /**
     * Valeur de startDateTemporalHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public TemporalHTTPParameterValidator getStartDateTemporalHTTPParameterValidator() {
        return startDateTemporalHTTPParameterValidator;
    }

    /**
     * Valeur de endDateTemporalHighHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public TemporalHTTPParameterValidator getEndDateTemporalHighHTTPParameterValidator() {
        return endDateTemporalHighHTTPParameterValidator;
    }

    /**
     * Valeur de serviceHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public ServiceHTTPParameterValidator getServiceHTTPParameterValidator() {
        return serviceHTTPParameterValidator;
    }
}
