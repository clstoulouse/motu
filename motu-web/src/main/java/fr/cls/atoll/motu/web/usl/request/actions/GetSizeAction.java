package fr.cls.atoll.motu.web.usl.request.actions;

import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_END_DATE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_HIGH_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_LOW_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_START_DATE;

import java.io.IOException;
import java.io.Writer;

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
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.ExceptionUtils;
import fr.cls.atoll.motu.web.bll.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.exception.MotuMarshallException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.DepthHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LatitudeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LongitudeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ProductHTTPParameterValidator;
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

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;
    private ProductHTTPParameterValidator productHTTPParameterValidator;

    private DepthHTTPParameterValidator depthLowHTTPParameterValidator;
    private DepthHTTPParameterValidator depthHighHTTPParameterValidator;
    private LatitudeHTTPParameterValidator latitudeLowHTTPParameterValidator;
    private LatitudeHTTPParameterValidator latitudeHighHTTPParameterValidator;
    private LongitudeHTTPParameterValidator longitudeLowHTTPParameterValidator;
    private LongitudeHTTPParameterValidator longitudeHighHTTPParameterValidator;

    private TemporalHTTPParameterValidator startDateTemporalHTTPParameterValidator;
    private TemporalHTTPParameterValidator endDateTemporalHighHTTPParameterValidator;

    /**
     * 
     * Constructor of the GetSizeAction class.
     * 
     * @param request The GetSize request to manage
     * @param response The response object used to return the response of the request
     * @param session The session object of the request
     */
    public GetSizeAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, request, response, session);

        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SERVICE,
                CommonHTTPParameters.getServiceFromRequest(getRequest()));
        productHTTPParameterValidator = new ProductHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_PRODUCT,
                CommonHTTPParameters.getProductFromRequest(getRequest()));
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
        try {
            getAmountDataSize(createExtractionParameters(), getResponse());
        } catch (IOException | JAXBException e) {
            throw new MotuException("Error while computing getAmountDataSize", e);
        }
    }

    private ExtractionParameters createExtractionParameters() throws IOException {
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

                productHTTPParameterValidator.getParameterValueValidated(),
                OutputFormat.NETCDF,
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
            throws MotuException, JAXBException, IOException {

        try {
            Product p = null;
            if (!StringUtils.isNullOrEmpty(extractionParameters.getLocationData())) {
                p = BLLManager.getInstance().getCatalogManager().getProductManager().getProduct(extractionParameters.getLocationData());
            } else if (!StringUtils.isNullOrEmpty(extractionParameters.getServiceName())
                    && !StringUtils.isNullOrEmpty(extractionParameters.getProductId())) {
                p = BLLManager.getInstance().getCatalogManager().getProductManager().getProduct(extractionParameters.getServiceName(),
                                                                                                extractionParameters.getProductId());
            }

            ProductMetaData pmd = BLLManager.getInstance().getCatalogManager().getProductManager().getProductMetaData(p.getProductId(),
                                                                                                                      p.getLocationData());
            p.setProductMetaData(pmd);

            if (p != null) {
                double productDataSize = BLLManager.getInstance().getRequestManager()
                        .getProductDataSizeIntoByte(p,
                                                    extractionParameters.getListVar(),
                                                    extractionParameters.getListTemporalCoverage(),
                                                    extractionParameters.getListLatLonCoverage(),
                                                    extractionParameters.getListDepthCoverage());
                double productMaxAllowedDataSize = BLLManager.getInstance().getRequestManager().getProductMaxAllowedDataSizeIntoByte(p);
                RequestSize requestSize = initRequestSize(productDataSize, productMaxAllowedDataSize);
                marshallRequestSize(requestSize, getResponse().getWriter());
            } else {
                throw new MotuException("Product not found : " + productHTTPParameterValidator.getParameterValue());
            }
        } catch (MotuExceptionBase e) {
            try {
                marshallRequestSize(e, extractionParameters.getOut());
            } catch (MotuMarshallException e2) {
                response.sendError(500, String.format("ERROR: %s", e2.getMessage()));
            }
        }
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
    private static RequestSize initRequestSize(double size, double maxAllowedSize) {

        RequestSize requestSize = createRequestSize();

        requestSize.setSize(size);
        requestSize.setCode(ErrorType.OK);
        requestSize.setMsg(ErrorType.OK.toString());

        if (size < 0) {
            ExceptionUtils.setError(requestSize, new MotuException("size can't be computed and the cause is unspecified"));
            return requestSize;
        }

        MotuExceptionBase exceptionBase = null;

        if (size > maxAllowedSize) {
            exceptionBase = new MotuExceedingCapacityException(convertFromBytesToMegabytes(size), convertFromBytesToMegabytes(maxAllowedSize));
        }

        if (exceptionBase != null) {
            ExceptionUtils.setError(requestSize, exceptionBase);
        }

        requestSize.setMaxAllowedSize(maxAllowedSize);

        return requestSize;
    }

    /**
     * Convert from bytes to kilobytes.
     * 
     * @param value the value
     * 
     * @return the double
     */
    public static double convertFromBytesToKilobytes(double value) {
        return value / 1024d;
    }

    /**
     * Convert from bytes to megabytes.
     * 
     * @param value the value
     * 
     * @return the double
     */
    public static double convertFromBytesToMegabytes(double value) {
        return convertFromBytesToKilobytes(value / 1024d);
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
    public static void marshallRequestSize(RequestSize requestSize, Writer writer) throws MotuMarshallException, JAXBException, IOException {
        if (writer == null) {
            return;
        }

        if (requestSize == null) {
            requestSize = initRequestSize(-1d, -1d);
        }
        JAXBWriter.getInstance().write(requestSize, writer);
        writer.flush();
        writer.close();
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        getServiceHTTPParameterValidator().validate();
        getProductHTTPParameterValidator().validate();

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
     * Valeur de productHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public ProductHTTPParameterValidator getProductHTTPParameterValidator() {
        return productHTTPParameterValidator;
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
