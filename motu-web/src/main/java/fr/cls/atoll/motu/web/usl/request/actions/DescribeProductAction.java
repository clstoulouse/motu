package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.ProductMetadataInfo;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.catalog.product.IBLLProductManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.AbstractHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ExtraMetaDataHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.XMLFileHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.response.xml.converter.ProductMetadataInfoConverter;
import fr.cls.atoll.motu.web.usl.response.xml.converter.XMLConverter;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * Request example: Motu?action=describeProduct &data=http://$tdsServer/thredds/dodsC/path_HR_MOD
 * &xmlfile=http://$tdsServer/thredds/m_HR_MOD.xml
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class DescribeProductAction extends AbstractProductInfoAction {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "describeproduct";
    public static final String ACTION_CODE = "006";
    private XMLFileHTTPParameterValidator xmlFileParameterValidator;
    private ExtraMetaDataHTTPParameterValidator extraMetaDataHTTPParameterValidator;

    /**
     * Constructeur.
     * 
     * @param request
     * @param response
     */
    public DescribeProductAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, ACTION_CODE, request, response, session);

        xmlFileParameterValidator = new XMLFileHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_XML_FILE,
                CommonHTTPParameters.getXmlFileFromRequest(getRequest()),
                AbstractHTTPParameterValidator.EMPTY_VALUE);
        xmlFileParameterValidator.setOptional(true);

        extraMetaDataHTTPParameterValidator = new ExtraMetaDataHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_EXTRA_METADATA,
                CommonHTTPParameters.getExtraMetaDataFromRequest(getRequest()),
                AbstractHTTPParameterValidator.EMPTY_VALUE);
        extraMetaDataHTTPParameterValidator.setOptional(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        super.checkHTTPParameters();
        xmlFileParameterValidator.validate();
        extraMetaDataHTTPParameterValidator.validate();

        try {
            String hasProductIdentifierErrMsg = hasProductIdentifier();
            if (hasProductIdentifierErrMsg != null) {
                throw new InvalidHTTPParameterException(hasProductIdentifierErrMsg, null, null, null);
            }
        } catch (MotuException e) {
            LOGGER.error("Error while calling hasProductIdentifier", e);
        }

        String locationData = CommonHTTPParameters.getDataFromParameter(getRequest());
        String xmlFile = xmlFileParameterValidator.getParameterValueValidated();

        if (!(StringUtils.isNullOrEmpty(locationData) || AbstractHTTPParameterValidator.EMPTY_VALUE.equals(locationData))
                && !(StringUtils.isNullOrEmpty(xmlFile) || AbstractHTTPParameterValidator.EMPTY_VALUE.equals(xmlFile))) {
            String urlPath = BLLManager.getInstance().getCatalogManager().getProductManager().datasetIdFromProductLocation(locationData);
            if (urlPath == null) {
                throw new InvalidHTTPParameterException(
                        "Parameter " + MotuRequestParametersConstant.PARAM_DATA + " value " + locationData
                                + " does not exists. Its pattern is an HTTP URL to thredds server.",
                        MotuRequestParametersConstant.PARAM_DATA,
                        locationData,
                        null);
            }
        }
    }

    @Override
    protected void process() throws MotuException {
        String hasProductIdentifierErrMsg = hasProductIdentifier();
        if (hasProductIdentifierErrMsg == null) {
            try {
                String httpParameterProductId = getProductId();
                Product currentProduct = getProduct();
                if (checkProduct(currentProduct, httpParameterProductId)) {
                    try {
                        ConfigService cs = BLLManager.getInstance().getConfigManager()
                                .getConfigService(getServiceHTTPParameterValidator().getParameterValueValidated());
                        // Do not check ConfigService because can be requested from locationData and has a
                        // null value in this case
                        // if (checkConfigService(cs, getServiceHTTPParameterValidator())) {
                        ProductMetadataInfo pmdi = ProductMetadataInfoConverter.getProductMetadataInfo(cs, currentProduct);
                        if (checkProductMetaDataInfo(pmdi, httpParameterProductId)) {
                            String response = XMLConverter.toXMLString(pmdi);
                            writeResponse(response, HTTPUtils.CONTENT_TYPE_XML_UTF8);
                        }
                    } catch (MotuExceptionBase e) {
                        throw new MotuException(ErrorType.SYSTEM, e);
                    }
                }
            } catch (IOException e) {
                throw new MotuException(ErrorType.SYSTEM, e);
            }
        } else {
            throw new MotuException(ErrorType.BAD_PARAMETERS, new InvalidHTTPParameterException(hasProductIdentifierErrMsg, null, null, null));
        }
    }

    @Override
    protected Product getProduct() throws MotuException {
        Product currentProduct;
        String locationData = CommonHTTPParameters.getDataFromParameter(getRequest());
        String xmlFile = AbstractHTTPParameterValidator.EMPTY_VALUE.equalsIgnoreCase(xmlFileParameterValidator.getParameterValueValidated()) ? null
                : xmlFileParameterValidator.getParameterValueValidated();

        if (!StringUtils.isNullOrEmpty(locationData) && !StringUtils.isNullOrEmpty(xmlFile)) {
            String catalogName = xmlFile.substring(xmlFile.lastIndexOf("/") + 1, xmlFile.length());
            String urlPath = BLLManager.getInstance().getCatalogManager().getProductManager().datasetIdFromProductLocation(locationData);
            if (urlPath == null) {
                throw new MotuException(
                        ErrorType.BAD_PARAMETERS,
                        "Parameter " + MotuRequestParametersConstant.PARAM_DATA + " value " + locationData + " does not exist.");
            }
            IBLLProductManager productManager = BLLManager.getInstance().getCatalogManager().getProductManager();
            currentProduct = productManager.getProductFromLocation(catalogName, urlPath);
        } else {
            currentProduct = super.getProduct();
        }

        return currentProduct;
    }

    protected String hasProductIdentifier() throws MotuException {
        String productId = getProductId();
        String locationData = CommonHTTPParameters.getDataFromParameter(getRequest());
        String serviceName = getServiceHTTPParameterValidator().getParameterValueValidated();
        String hasProductIdentifierErrMsg = null;
        if (StringUtils.isNullOrEmpty(locationData) && StringUtils.isNullOrEmpty(productId)) {
            hasProductIdentifierErrMsg = String.format("ERROR: Parameter '%s' or '%s' has to be set",
                                                       MotuRequestParametersConstant.PARAM_DATA,
                                                       MotuRequestParametersConstant.PARAM_PRODUCT);

        }

        if (!StringUtils.isNullOrEmpty(locationData) && !StringUtils.isNullOrEmpty(productId)) {
            hasProductIdentifierErrMsg = String.format("ERROR: Parameters '%s' and '%s' are not compatible - Set only one of them",
                                                       MotuRequestParametersConstant.PARAM_DATA,
                                                       MotuRequestParametersConstant.PARAM_PRODUCT);
        }

        if (AbstractHTTPParameterValidator.EMPTY_VALUE.equals(serviceName) && !StringUtils.isNullOrEmpty(productId)) {
            hasProductIdentifierErrMsg = String.format("ERROR: '%s' parameter is present but '%s' is empty. You have to set it.",
                                                       MotuRequestParametersConstant.PARAM_PRODUCT,
                                                       MotuRequestParametersConstant.PARAM_SERVICE);
        }

        return hasProductIdentifierErrMsg;
    }

    /**
     * Gets the product id.
     *
     * @param paramId the product id
     * @param request the request
     * @param response the response
     * @return the product id
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ServletException the servlet exception
     * @throws MotuException the motu exception
     */
    @Override
    protected String getProductId() throws MotuException {
        String paramId = getProductHTTPParameterValidator().getParameterValueValidated();
        String serviceName = getServiceHTTPParameterValidator().getParameterValueValidated();

        if (!AbstractHTTPParameterValidator.EMPTY_VALUE.equals(paramId)) {
            if ((StringUtils.isNullOrEmpty(serviceName)) || (StringUtils.isNullOrEmpty(paramId))) {
                return paramId;
            }

            String uri = paramId;
            String[] split = uri.split(".*#");
            if (split.length <= 1) {
                return uri;
            }
            return split[1];
        } else {
            return "";
        }
    }

}
