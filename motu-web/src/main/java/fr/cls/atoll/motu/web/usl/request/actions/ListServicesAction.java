package fr.cls.atoll.motu.web.usl.request.actions;

import java.util.ArrayList;
import java.util.List;

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
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.CatalogTypeParameterValidator;
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

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "listservices";

    private CatalogTypeParameterValidator catalogTypeParameterValidator;

    /**
     * 
     * @param actionName_
     */
    public ListServicesAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, request, response, session);

        catalogTypeParameterValidator = new CatalogTypeParameterValidator(
                MotuRequestParametersConstant.PARAM_CATALOG_TYPE,
                CommonHTTPParameters.getCatalogTypeFromRequest(getRequest()),
                "");
        catalogTypeParameterValidator.setOptional(true);
    }

    @Override
    public void process() throws MotuException {
        String catalogType = catalogTypeParameterValidator.getParameterValueValidated();
        writeResponseWithVelocity(filterConfigService(catalogType));
    }

    private List<ConfigService> filterConfigService(String catalogType) {
        List<ConfigService> csList = new ArrayList<ConfigService>();
        for (ConfigService cs : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            if (StringUtils.isNullOrEmpty(catalogType)
                    || (!StringUtils.isNullOrEmpty(catalogType) && cs.getCatalog().getType().equalsIgnoreCase(catalogType))) {
                csList.add(cs);
            }
        }
        return csList;
    }

    private void writeResponseWithVelocity(List<ConfigService> csList_) throws MotuException {
        VelocityContext context = VelocityTemplateManager.getPrepopulatedVelocityContext();
        context.put("body_template", VelocityTemplateManager.getTemplatePath(ACTION_NAME, VelocityTemplateManager.DEFAULT_LANG));
        context.put("serviceList", VelocityModelConverter.converServiceList(csList_));

        try {
            Template template = VelocityTemplateManager.getInstance().initVelocityEngineWithGenericTemplate(null);
            template.merge(context, getResponse().getWriter());
        } catch (Exception e) {
            throw new MotuException("Error while using velocity template", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        catalogTypeParameterValidator.validate();
    }

}
