package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Interval;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.AbstractHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ProductHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.response.xml.converter.XMLConverter;

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
public class TimeCoverageAction extends AbstractProductInfoAction {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "gettimecov";

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;
    private ProductHTTPParameterValidator productHTTPParameterValidator;

    public TimeCoverageAction(String actionCode_, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, actionCode_, request, response, session);

        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SERVICE,
                CommonHTTPParameters.getServiceFromRequest(getRequest()),
                AbstractHTTPParameterValidator.EMPTY_VALUE);

        productHTTPParameterValidator = new ProductHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_PRODUCT,
                CommonHTTPParameters.getProductFromRequest(getRequest()),
                AbstractHTTPParameterValidator.EMPTY_VALUE);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        super.checkHTTPParameters();
        serviceHTTPParameterValidator.validate();
        productHTTPParameterValidator.validate();

        try {
            String hasProductIdentifierErrMsg = hasProductIdentifier();
            if (hasProductIdentifierErrMsg != null) {
                throw new InvalidHTTPParameterException(hasProductIdentifierErrMsg, null, null, null);
            }
        } catch (MotuException e) {
            LOGGER.error("Error while calling hasProductIdentifier", e);
        }
    }

    protected String hasProductIdentifier() throws MotuException {
        String productId = getProductId();
        String locationData = CommonHTTPParameters.getDataFromParameter(getRequest());
        String serviceName = serviceHTTPParameterValidator.getParameterValueValidated();
        String hasProductIdentifierErrMsg = null;
        if (StringUtils.isNullOrEmpty(locationData) && StringUtils.isNullOrEmpty(productId)) {
            hasProductIdentifierErrMsg = String.format("ERROR: neither '%s' nor '%s' parameters are filled - Choose one of them",
                                                       MotuRequestParametersConstant.PARAM_DATA,
                                                       MotuRequestParametersConstant.PARAM_PRODUCT);

        }

        if (!StringUtils.isNullOrEmpty(locationData) && !StringUtils.isNullOrEmpty(productId)) {
            hasProductIdentifierErrMsg = String.format("ERROR: '%s' and '%s' parameters are not compatible - Choose only one of them",
                                                       MotuRequestParametersConstant.PARAM_DATA,
                                                       MotuRequestParametersConstant.PARAM_PRODUCT);
        }

        if (AbstractHTTPParameterValidator.EMPTY_VALUE.equals(serviceName) && !StringUtils.isNullOrEmpty(productId)) {
            hasProductIdentifierErrMsg = String.format("ERROR: '%s' parameter is filled but '%s' is empty. You have to fill it.",
                                                       MotuRequestParametersConstant.PARAM_PRODUCT,
                                                       MotuRequestParametersConstant.PARAM_SERVICE);
        }

        return hasProductIdentifierErrMsg;
    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws MotuException {
        String hasProductIdentifierErrMsg = hasProductIdentifier();
        if (hasProductIdentifierErrMsg == null) {
            try {
                Product product = getProduct();
                if (checkProduct(product, getProductId())) {
                    Interval datePeriod = null;
                    if (product.getProductMetaData() != null) {
                        datePeriod = product.getProductMetaData().getTimeCoverage();
                        String response = XMLConverter.toXMLString(datePeriod, getActionCode());
                        writeResponse(response, HTTPUtils.CONTENT_TYPE_XML_UTF8);
                    } else {
                        throw new MotuException(
                                ErrorType.BAD_PARAMETERS,
                                new InvalidHTTPParameterException("Product Metadata not found", null, null, null));
                    }
                }
            } catch (IOException e) {
                throw new MotuException(ErrorType.SYSTEM, e);
            }
        } else {
            throw new MotuException(ErrorType.BAD_PARAMETERS, new InvalidHTTPParameterException(hasProductIdentifierErrMsg, null, null, null));
        }
    }

}
