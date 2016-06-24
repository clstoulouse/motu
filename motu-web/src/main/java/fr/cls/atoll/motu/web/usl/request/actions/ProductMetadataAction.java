package fr.cls.atoll.motu.web.usl.request.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import fr.cls.atoll.motu.web.usl.USLManager;
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
 * <li><b>catalogtype</b>: [0,1]: Catalog type: TDS, FTP.</li>
 * </ul>
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class ProductMetadataAction extends AbstractAuthorizedAction {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "listproductmetadata";

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;
    private ProductHTTPParameterValidator productHTTPParameterValidator;

    /**
     * 
     * @param actionName_
     */
    public ProductMetadataAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, request, response, session);

        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SERVICE,
                CommonHTTPParameters.getServiceFromRequest(getRequest()));

        productHTTPParameterValidator = new ProductHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_PRODUCT,
                CommonHTTPParameters.getProductFromRequest(getRequest()));
    }

    @Override
    public void process() throws MotuException {
        MotuConfig mc = BLLManager.getInstance().getConfigManager().getMotuConfig();
        ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(serviceHTTPParameterValidator.getParameterValueValidated());
        CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(cs);
        String productId = productHTTPParameterValidator.getParameterValueValidated();
        Product p = cd.getProducts().get(productId);
        ProductMetaData pmd = BLLManager.getInstance().getCatalogManager().getProductManager().getProductMetaData(productId, p.getLocationData());
        p.setProductMetaData(pmd);

        writeResponseWithVelocity(mc, cs, cd, p);
    }

    private void writeResponseWithVelocity(MotuConfig mc, ConfigService cs_, CatalogData cd_, Product product_) throws MotuException {
        VelocityContext context = VelocityTemplateManager.getPrepopulatedVelocityContext();
        context.put("body_template", VelocityTemplateManager.getTemplatePath(ACTION_NAME, VelocityTemplateManager.DEFAULT_LANG));
        context.put("service", VelocityModelConverter.convertToService(mc, cs_, cd_));
        context.put("user", USLManager.getInstance().getUserManager().getUserName());
        context.put("product", VelocityModelConverter.convertToProduct(product_));

        try {
            Template template = VelocityTemplateManager.getInstance().initVelocityEngineWithGenericTemplate(null, cs_.getVeloTemplatePrefix());
            template.merge(context, getResponse().getWriter());
        } catch (Exception e) {
            throw new MotuException("Error while using velocity template", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        serviceHTTPParameterValidator.validate();
        productHTTPParameterValidator.validate();
    }

}
