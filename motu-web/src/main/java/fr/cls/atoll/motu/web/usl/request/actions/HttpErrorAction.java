package fr.cls.atoll.motu.web.usl.request.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.HttpErrorCodeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.response.velocity.VelocityTemplateManager;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)<br>
 * <br>
 * <br>
 * This interface is used to display the version of the Motu entities.<br>
 * Operation invocation consists in performing an HTTP GET request.<br>
 * There is no input parameter<br>
 * The output response is a simple HTML web page.<br>
 * <br>
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class HttpErrorAction extends AbstractAction {

    public static final String ACTION_NAME = "httperror";

    private HttpErrorCodeHTTPParameterValidator httpErrorCodeHTTPParameterValidator;

    /**
     * Constructeur.
     * 
     * @param actionName_
     */
    public HttpErrorAction(String actionCode_, HttpServletRequest request, HttpServletResponse response) {
        super(ACTION_NAME, actionCode_, request, response);
        httpErrorCodeHTTPParameterValidator = new HttpErrorCodeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_HTTP_ERROR_CODE,
                CommonHTTPParameters.getHttpErrorCodeFromRequest(getRequest()),
                "404");
    }

    @Override
    public void process() throws MotuException {
        getResponse().setContentType(CONTENT_TYPE_HTML);

        VelocityContext context = VelocityTemplateManager.getPrepopulatedVelocityContext();
        context.put("body_template", VelocityTemplateManager.getTemplatePath(ACTION_NAME, VelocityTemplateManager.DEFAULT_LANG));
        context.put("httpErrorCode", httpErrorCodeHTTPParameterValidator.getParameterValueValidated());

        try {
            Template template = VelocityTemplateManager.getInstance().initVelocityEngineWithGenericTemplate(null, null);
            template.merge(context, getResponse().getWriter());
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while using velocity template", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        httpErrorCodeHTTPParameterValidator.validate();
    }

}
