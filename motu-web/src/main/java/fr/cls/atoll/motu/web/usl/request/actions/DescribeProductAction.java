package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.ProductMetadataInfo;
import fr.cls.atoll.motu.api.utils.JAXBWriter;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.catalog.product.IBLLProductManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.exception.MotuMarshallException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.AbstractHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ExtraMetaDataHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.XMLFileHTTPParameterValidator;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * Request example: Motu?action=describeProduct
 * &data=http://misgw-ddo-qt.cls.fr:61080/thredds/dodsC/path_HR_MOD
 * &xmlfile=http://misgw-ddo-qt.cls.fr:61080/thredds/m_HR_MOD.xml
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class DescribeProductAction extends AbstractProductInfoAction {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "describeproduct";
    private XMLFileHTTPParameterValidator xmlFileParameterValidator;
    private ExtraMetaDataHTTPParameterValidator extraMetaDataHTTPParameterValidator;

    /**
     * Constructeur.
     * 
     * @param actionName_
     * @param request_
     * @param response_
     */
    public DescribeProductAction(String actionCode_, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, actionCode_, request, response, session);
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
    }

    @Override
    protected void process() throws MotuException {

        if (hasProductIdentifier()) {
            ProductMetadataInfo pmdi = null;
            try {
                Product currentProduct = getProduct();
                pmdi = BLLManager.getInstance().getDescribeProductCacheManager().getDescribeProduct(currentProduct.getProductId());

                marshallProductMetadata(pmdi, getResponse().getWriter());

                getResponse().setContentType(null);
            } catch (MotuExceptionBase | JAXBException | IOException e) {
                throw new MotuException(ErrorType.SYSTEM, e);
            }
        }
    }

    @Override
    protected Product getProduct() throws MotuException {
        Product currentProduct = null;
        String locationData = CommonHTTPParameters.getDataFromParameter(getRequest());
        String xmlFile = xmlFileParameterValidator.getParameterValueValidated();

        if (!StringUtils.isNullOrEmpty(locationData) && !StringUtils.isNullOrEmpty(xmlFile)) {
            String catalogName = xmlFile.substring(xmlFile.lastIndexOf("/") + 1, xmlFile.length());
            String urlPath = BLLManager.getInstance().getCatalogManager().getProductManager().datasetIdFromProductLocation(locationData);

            IBLLProductManager productManager = BLLManager.getInstance().getCatalogManager().getProductManager();
            currentProduct = productManager.getProductFromLocation(catalogName, urlPath);
        } else {
            currentProduct = super.getProduct();
        }

        return currentProduct;
    }

    @Override
    protected boolean hasProductIdentifier() throws MotuException {
        boolean hasproductIdentifier = true;
        String productId = getProductId();
        String locationData = CommonHTTPParameters.getDataFromParameter(getRequest());
        String serviceName = getServiceHTTPParameterValidator().getParameterValueValidated();
        try {
            if (StringUtils.isNullOrEmpty(locationData) && StringUtils.isNullOrEmpty(productId)) {
                getResponse().sendError(400,
                                        String.format("ERROR: neither '%s' nor '%s' parameters are filled - Choose one of them",
                                                      MotuRequestParametersConstant.PARAM_DATA,
                                                      MotuRequestParametersConstant.PARAM_PRODUCT));
                hasproductIdentifier = false;

            }

            if (!StringUtils.isNullOrEmpty(locationData) && !StringUtils.isNullOrEmpty(productId)) {
                getResponse().sendError(400,
                                        String.format("ERROR: '%s' and '%s' parameters are not compatible - Choose only one of them",
                                                      MotuRequestParametersConstant.PARAM_DATA,
                                                      MotuRequestParametersConstant.PARAM_PRODUCT));
                hasproductIdentifier = false;
            }

            if (AbstractHTTPParameterValidator.EMPTY_VALUE.equals(serviceName) && !StringUtils.isNullOrEmpty(productId)) {
                getResponse().sendError(400,
                                        String.format("ERROR: '%s' parameter is filled but '%s' is empty. You have to fill it.",
                                                      MotuRequestParametersConstant.PARAM_PRODUCT,
                                                      MotuRequestParametersConstant.PARAM_SERVICE));
                hasproductIdentifier = false;
            }
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, e);
        }

        return hasproductIdentifier;
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
        String serviceName = CommonHTTPParameters.getServiceFromRequest(getRequest());

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

    /**
     * Marshall Product MetaData.
     * 
     * @param batchQueue the batch queue
     * @param productMetatData the request size
     * @param writer the writer
     * 
     * @throws MotuMarshallException the motu marshall exception
     * @throws JAXBException
     * @throws IOException
     */
    public static void marshallProductMetadata(ProductMetadataInfo productMetatData, Writer writer)
            throws MotuMarshallException, JAXBException, IOException {
        if (writer == null) {
            return;
        }
        JAXBWriter.getInstance().write(productMetatData, writer);
        writer.flush();
        writer.close();
    }
}
