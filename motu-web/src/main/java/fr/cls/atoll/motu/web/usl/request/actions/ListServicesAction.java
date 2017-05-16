package fr.cls.atoll.motu.web.usl.request.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.CatalogTypeHTTPParameterValidator;
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
 * <li><b>catalogtype</b>: [0,1]: Catalog type: TDS, FTP. Used to filter only a specific catalog type</li>
 * </ul>
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class ListServicesAction extends AbstractAuthorizedAction {

    public static final String ACTION_NAME = "listservices";

    private CatalogTypeHTTPParameterValidator catalogTypeParameterValidator;

    /**
     * 
     * @param actionName_
     */
    public ListServicesAction(String actionCode_, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, actionCode_, request, response, session);

        catalogTypeParameterValidator = new CatalogTypeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_CATALOG_TYPE,
                CommonHTTPParameters.getCatalogTypeFromRequest(getRequest()),
                "");
        catalogTypeParameterValidator.setOptional(true);
    }

    @Override
    public void process() throws MotuException {
        MotuConfig mc = BLLManager.getInstance().getConfigManager().getMotuConfig();
        String catalogType = catalogTypeParameterValidator.getParameterValueValidated();
        writeResponseWithVelocity(mc, filterConfigService(catalogType));
    }

    private List<ConfigService> filterConfigService(String catalogType) throws MotuException {
        List<ConfigService> csList = new ArrayList<ConfigService>();
        for (ConfigService cs : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            if (StringUtils.isNullOrEmpty(catalogType) || (!StringUtils.isNullOrEmpty(catalogType)
                    && BLLManager.getInstance().getCatalogManager().getCatalogType(cs).equalsIgnoreCase(catalogType))) {
                csList.add(cs);
            }
        }
        return csList;
    }

    private void writeResponseWithVelocity(MotuConfig mc, List<ConfigService> csList_) throws MotuException {
        Map<String, Object> velocityContext = new HashMap<String, Object>(2);
        velocityContext.put("body_template", VelocityTemplateManager.getTemplatePath(ACTION_NAME, VelocityTemplateManager.DEFAULT_LANG));
        velocityContext.put("serviceList", VelocityModelConverter.converServiceList(mc, csList_));

        String response = VelocityTemplateManager.getInstance().getResponseWithVelocity(velocityContext, null, null);
        try {
            writeResponse(response, HTTPUtils.CONTENT_TYPE_HTML_UTF8);
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while using velocity template", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        catalogTypeParameterValidator.validate();
    }

}
