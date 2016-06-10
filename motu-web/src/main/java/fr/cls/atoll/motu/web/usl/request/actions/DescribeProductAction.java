package fr.cls.atoll.motu.web.usl.request.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ExtraMetaDataHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ProductHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.XMLFileParameterValidator;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class DescribeProductAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "describeProduct";

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;
    private ProductHTTPParameterValidator productHTTPParameterValidator;
    private XMLFileParameterValidator xmlFileParameterValidator;
    private ExtraMetaDataHTTPParameterValidator extraMetaDataHTTPParameterValidator;

    /**
     * Constructeur.
     * 
     * @param actionName_
     * @param request_
     * @param response_
     */
    public DescribeProductAction(String actionName_, HttpServletRequest request_, HttpServletResponse response_) {
        super(actionName_, request_, response_);
        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SERVICE,
                CommonHTTPParameters.getServiceFromRequest(getRequest()));
        productHTTPParameterValidator = new ProductHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_PRODUCT,
                CommonHTTPParameters.getProductFromRequest(getRequest()));
        xmlFileParameterValidator = new XMLFileParameterValidator(
                MotuRequestParametersConstant.PARAM_PRODUCT,
                CommonHTTPParameters.getXmlFileFromRequest(getRequest()));
        extraMetaDataHTTPParameterValidator = new ExtraMetaDataHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_EXTRA_METADATA,
                CommonHTTPParameters.getExtraMetaDataFromRequest(getRequest()));
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        serviceHTTPParameterValidator.validate();
        productHTTPParameterValidator.validate();
        xmlFileParameterValidator.validate();
        extraMetaDataHTTPParameterValidator.validate();
    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws MotuException {
        // String serviceName = serviceHTTPParameterValidator.getParameterValueValidated();
        // String locationData = CommonHTTPParameters.getDataFromParameter(getRequest());
        //
        // String productId = "";
        // productId = getProductId();
        //
        // if (StringUtils.isNullOrEmpty(locationData) && StringUtils.isNullOrEmpty(productId)) {
        // getResponse().sendError(400,
        // String.format("ERROR: neither '%s' nor '%s' parameters are filled - Choose one of them",
        // MotuRequestParametersConstant.PARAM_DATA,
        // MotuRequestParametersConstant.PARAM_PRODUCT));
        // }
        //
        // if (!StringUtils.isNullOrEmpty(locationData) && !StringUtils.isNullOrEmpty(productId)) {
        // getResponse().sendError(400,
        // String.format("ERROR: '%s' and '%s' parameters are not compatible - Choose only one of them",
        // MotuRequestParametersConstant.PARAM_DATA,
        // MotuRequestParametersConstant.PARAM_PRODUCT));
        // }
        //
        // if (StringUtils.isNullOrEmpty(serviceName) && !StringUtils.isNullOrEmpty(productId)) {
        // getResponse().sendError(400,
        // String.format("ERROR: '%s' parameter is filled but '%s' is empty. You have to fill it.",
        // MotuRequestParametersConstant.PARAM_PRODUCT,
        // MotuRequestParametersConstant.PARAM_SERVICE));
        // }
        //
        // String tdsCatalogFileName = xmlFileParameterValidator.getParameterValueValidated();
        //
        // boolean loadExtraMetadata = extraMetaDataHTTPParameterValidator.getParameterValueValidated();
        // // -------------------------------------------------
        // // get Time coverage
        // // -------------------------------------------------
        // if (!StringUtils.isNullOrEmpty(locationData)) {
        // productDescribeProduct(locationData, tdsCatalogFileName, loadExtraMetadata, getResponse());
        // } else if (!StringUtils.isNullOrEmpty(serviceName) && !StringUtils.isNullOrEmpty(productId)) {
        // productDescribeProduct(loadExtraMetadata, serviceName, productId, getResponse());
        // }
        //
        // response.setContentType(null);
    }
    //
    // /**
    // * Gets the product id.
    // *
    // * @param paramId the product id
    // * @param request the request
    // * @param response the response
    // * @return the product id
    // * @throws IOException Signals that an I/O exception has occurred.
    // * @throws ServletException the servlet exception
    // * @throws MotuException the motu exception
    // */
    // protected String getProductId() throws MotuException {
    // String paramId = productHTTPParameterValidator.getParameterValueValidated();
    // String serviceName = CommonHTTPParameters.getServiceFromRequest(getRequest());
    //
    // if ((StringUtils.isNullOrEmpty(serviceName)) || (StringUtils.isNullOrEmpty(paramId))) {
    // return paramId;
    // }
    //
    // String uri = paramId;
    // String[] split = uri.split(".*#");
    // if (split.length <= 1) {
    // return uri;
    // }
    // return split[1];
    // }
    //
    // /**
    // * Product describe product.
    // *
    // * @param locationData the location data
    // * @param tdsCatalogFileName the tds catalog file name
    // * @param loadExtraMetadata the load extra metadata
    // * @param response the response
    // * @throws ServletException the servlet exception
    // * @throws IOException Signals that an I/O exception has occurred.
    // */
    // private void productDescribeProduct(String locationData, String tdsCatalogFileName, boolean
    // loadExtraMetadata, HttpServletResponse response)
    // throws ServletException, IOException {
    // try {
    //
    // organizer.getProductMetadataInfo(locationData, tdsCatalogFileName, loadExtraMetadata,
    // response.getWriter());
    //
    // } catch (MotuMarshallException e) {
    // LOG.error("productDescribeProduct(String, String, HttpServletResponse)", e);
    //
    // response.sendError(500, String.format("ERROR: %s", e.getMessage()));
    // } catch (MotuExceptionBase e) {
    // LOG.error("productDescribeProduct(String, String, HttpServletResponse)", e);
    //
    // // Do nothing error is in response code
    // // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
    // }
    //
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("productDescribeProduct(String, String, HttpServletResponse) - exiting");
    // }
    // }
    //
    // /**
    // * Product describe product.
    // *
    // * @param loadExtraMetadata the load extra metadata
    // * @param serviceName the service name
    // * @param productId the product id
    // * @param response the response
    // * @throws ServletException the servlet exception
    // * @throws IOException Signals that an I/O exception has occurred.
    // */
    // private void productDescribeProduct(boolean loadExtraMetadata, String serviceName, String productId,
    // HttpServletResponse response)
    // throws ServletException, IOException {
    //
    // Organizer organizer = getOrganizer(null, response);
    // try {
    // organizer.getProductMetadataInfo(response.getWriter(), serviceName, productId, loadExtraMetadata);
    // } catch (MotuMarshallException e) {
    // response.sendError(500, String.format("ERROR: %s", e.getMessage()));
    // } catch (MotuExceptionBase e) {
    // // Do nothing error is in response code
    // // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
    // }
    //
    // }
    //
    // /**
    // * Gets the product metadata info.
    // *
    // * @param locationData the location data
    // * @param writer the writer
    // *
    // * @return the product metadata info
    // *
    // * @throws MotuExceptionBase the motu exception base
    // * @throws MotuMarshallException the motu marshall exception
    // */
    // public void getProductMetadataInfo(String locationData, String catalogFileName, boolean
    // loadTDSVariableVocabulary, Writer writer)
    // throws MotuExceptionBase, MotuMarshallException {
    //
    // ProductMetadataInfo productMetadataInfo = null;
    // try {
    // productMetadataInfo = getProductMetadataInfo(locationData, catalogFileName, loadTDSVariableVocabulary);
    // } catch (MotuExceptionBase e) {
    // Organizer.marshallProductMetadataInfo(e, writer);
    // throw e;
    // }
    // Organizer.marshallProductMetadataInfo(productMetadataInfo, writer);
    //
    // }
    //
    // /**
    // * Gets the product metadata info.
    // *
    // * @param locationData the location data
    // * @param catalogFileName the catalog file name
    // * @param loadTDSVariableVocabulary the load tds variable vocabulary
    // * @return the product metadata info
    // * @throws MotuExceptionBase the motu exception base
    // */
    // public ProductMetadataInfo getProductMetadataInfo(String locationData, String catalogFileName, boolean
    // loadTDSVariableVocabulary)
    // throws MotuExceptionBase {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("getProductMetadataInfo(String) - entering");
    // }
    // URI uri = null;
    //
    // try {
    // uri = new URI(locationData);
    // } catch (URISyntaxException e) {
    // throw new MotuException(
    // String.format("Organizer getProductMetadataInfo(String locationData) : location data seems not to be a
    // valid URI : '%s'",
    // locationData),
    // e);
    // }
    //
    // ProductMetadataInfo productMetadataInfo = null;
    //
    // // If uri is a file (netcdf file), don't load TDS (contained in TDS catalog) Metadata
    // if ((uri.getScheme().equalsIgnoreCase("http")) || (uri.getScheme().equalsIgnoreCase("https"))) {
    // productMetadataInfo = getProductMetadataInfoFromTDS(locationData, catalogFileName,
    // loadTDSVariableVocabulary);
    // } else {
    // productMetadataInfo = getProductMetadataInfoFromFile(locationData);
    // }
    //
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("getProductMetadataInfo(String) - exiting");
    // }
    // return productMetadataInfo;
    // }

}
