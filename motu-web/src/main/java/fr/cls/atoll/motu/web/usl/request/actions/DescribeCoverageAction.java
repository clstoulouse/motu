package fr.cls.atoll.motu.web.usl.request.actions;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.Template;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ProductHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.response.velocity.VelocityTemplateManager;
import fr.cls.atoll.motu.web.usl.response.velocity.model.converter.VelocityModelConverter;

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
 * <li><b>service</b>: [1]: The selected service.</li>
 * <li><b>datasetID</b>: [1]: The dateset id. For Motu it represents a product ID.</li>
 * </ul>
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class DescribeCoverageAction extends AbstractAuthorizedAction {

    public static final String ACTION_NAME = "describecoverage";
    public static final String ACTION_CODE = "015";

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * Lock used to avoid NullPointerException in Apache Velocity which is not thread safe
     */
    private static final Object lock = new Object();
    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;
    private ProductHTTPParameterValidator productHTTPParameterValidator;

    /**
     * 
     */
    public DescribeCoverageAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, ACTION_CODE, request, response, session);

        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SERVICE,
                CommonHTTPParameters.getServiceFromRequest(getRequest()));
        productHTTPParameterValidator = new ProductHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_DATASET_ID,
                CommonHTTPParameters.getDatasetIdFromRequest(getRequest()));

    }

    @Override
    public void process() throws MotuException {
        MotuConfig mc = BLLManager.getInstance().getConfigManager().getMotuConfig();
        ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(serviceHTTPParameterValidator.getParameterValueValidated());
        if (checkConfigService(cs, serviceHTTPParameterValidator)) {
            CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().getCatalogCache()
                    .getCatalog(cs.getName());
            if (cd != null) {
                String productId = productHTTPParameterValidator.getParameterValueValidated();
                Product p = BLLManager.getInstance().getCatalogManager().getProductManager().getProduct(cs.getName(), productId);
                if (checkProduct(p, productId)) {
                    RequestProduct rp = new RequestProduct(p);
                    writeResponseWithVelocity(mc, cs, cd, rp);
                }
            } else {
                throw new MotuException(ErrorType.SYSTEM, "Error while get catalog data for config service " + cs.getName());
            }
        }
    }

    private void writeResponseWithVelocity(MotuConfig mc, ConfigService cs, CatalogData cd, RequestProduct requestProduct) throws MotuException {
        Map<String, Object> velocityContext = new HashMap<>(2);
        velocityContext.put("service", VelocityModelConverter.convertToService(mc, cs, cd));
        velocityContext.put("product", VelocityModelConverter.convertToProduct(requestProduct));
        synchronized (lock) {
            try {
                String templateName = VelocityTemplateManager.getTemplatePath(ACTION_NAME, VelocityTemplateManager.DEFAULT_LANG, true);
                Template template = VelocityTemplateManager.getInstance().getVelocityEngine().getTemplate(templateName);
                String response = VelocityTemplateManager.getInstance().getResponseWithVelocity(velocityContext, template);
                writeResponse(response, HTTPUtils.CONTENT_TYPE_XML_UTF8);
            } catch (Exception e) {
                throw new MotuException(ErrorType.SYSTEM, "Error while using velocity template", e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        serviceHTTPParameterValidator.validate();
        productHTTPParameterValidator.validate();
    }

}
