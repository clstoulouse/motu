package fr.cls.atoll.motu.web.usl.request.actions;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.library.misc.configuration.ConfigService;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.web.bll.BLLManager;
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
 * <li><b>catalogtype</b>: [0,1]: Catalog type: TDS, FTP.</li>
 * </ul>
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class ListCatalogAction extends AbstractAuthorizedAction {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "listcatalog";

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;

    /**
     * 
     * @param actionName_
     */
    public ListCatalogAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, request, response, session);

        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SERVICE,
                CommonHTTPParameters.getServiceFromRequest(getRequest()));

    }

    @Override
    public void process() throws MotuException {
        ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(serviceHTTPParameterValidator.getParameterValueValidated());
        CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData();
        writeResponseWithVelocity(cs, cd);
    }

    private void writeResponseWithVelocity(ConfigService cs_, CatalogData cd) throws MotuException {
        VelocityContext context = VelocityTemplateManager.getPrepopulatedVelocityContext();
        context.put("body_template", VelocityTemplateManager.getTemplatePath(ACTION_NAME, VelocityTemplateManager.DEFAULT_LANG));
        context.put("service", VelocityModelConverter.convertToService(cs_, cd));
        context.put("user", USLManager.getInstance().getUserManager().getUserName());

        try {
            Template template = VelocityTemplateManager.getInstance().initVelocityEngineWithGenericTemplate(null);
            template.merge(context, getResponse().getWriter());
        } catch (Exception e) {
            throw new MotuException("Error while using velocity template", e);
        }
    }

    /**
     * @return a new context with some tools initialized.
     * 
     * @see NumberTool
     * @see DateTool
     * @see MathTool
     */
    public static VelocityContext getPrepopulatedVelocityContext() {
        final NumberTool numberTool = new NumberTool();
        final DateTool dateTool = new DateTool();
        final MathTool mathTool = new MathTool();

        VelocityContext vc = new VelocityContext();
        vc.put("numberTool", numberTool);
        vc.put("dateTool", dateTool);
        vc.put("mathTool", mathTool);
        vc.put("enLocale", Locale.ENGLISH);
        return vc;
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        serviceHTTPParameterValidator.validate();
    }

}
