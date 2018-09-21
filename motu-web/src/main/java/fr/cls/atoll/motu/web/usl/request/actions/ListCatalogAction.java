package fr.cls.atoll.motu.web.usl.request.actions;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.usl.USLManager;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
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
 * </ul>
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class ListCatalogAction extends AbstractAuthorizedAction {

    public static final String ACTION_NAME = "listcatalog";
    /**
     * Lock used to avoid NullPointerException in Apache Velocity which is not thread safe
     */
    private static final Object lock = new Object();

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;

    /**
     * 
     * @param actionName_
     */
    public ListCatalogAction(String actionCode_, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, actionCode_, request, response, session);

        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SERVICE,
                CommonHTTPParameters.getServiceFromRequest(getRequest()));
    }

    @Override
    public void process() throws MotuException {
        MotuConfig mc = BLLManager.getInstance().getConfigManager().getMotuConfig();
        ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(serviceHTTPParameterValidator.getParameterValueValidated());
        if (checkConfigService(cs, serviceHTTPParameterValidator)) {
            CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().getCatalogCache()
                    .getCatalog(cs.getName());
            if (cd != null) {
                writeResponseWithVelocity(mc, cs, cd);
            } else {
                throw new MotuException(ErrorType.SYSTEM, "Error while get catalog data for config service " + cs.getName());
            }
        }
    }

    private void writeResponseWithVelocity(MotuConfig mc_, ConfigService cs_, CatalogData cd) throws MotuException {
        Map<String, Object> velocityContext = new HashMap<String, Object>(2);
        velocityContext.put("body_template", VelocityTemplateManager.getTemplatePath(ACTION_NAME, VelocityTemplateManager.DEFAULT_LANG));
        velocityContext.put("service", VelocityModelConverter.convertToService(mc_, cs_, cd));
        velocityContext.put("user", USLManager.getInstance().getUserManager().getUserName());

        synchronized (lock) {
            String response = VelocityTemplateManager.getInstance().getResponseWithVelocity(velocityContext, null, cs_.getVeloTemplatePrefix());
            try {
                writeResponse(response);
            } catch (Exception e) {
                throw new MotuException(ErrorType.SYSTEM, "Error while using velocity template", e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        serviceHTTPParameterValidator.validate();
    }

}
