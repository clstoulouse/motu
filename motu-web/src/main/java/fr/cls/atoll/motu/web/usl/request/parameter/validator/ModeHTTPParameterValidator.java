package fr.cls.atoll.motu.web.usl.request.parameter.validator;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class ModeHTTPParameterValidator extends AbstractHTTPParameterValidator<String> {

    public static final String DEFAULT_MODE = MotuRequestParametersConstant.PARAM_MODE_URL;

    public ModeHTTPParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    /**
     * .
     * 
     */
    @Override
    public String onValidateAction() throws InvalidHTTPParameterException {
        String mode = getParameterValue();
        if (mode != null && (mode.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_MODE_URL)
                || mode.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_MODE_CONSOLE)
                || mode.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_MODE_STATUS))) {
            return mode;
        } else {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }
    }

    @Override
    protected String getParameterBoundaries() {
        return "[" + MotuRequestParametersConstant.PARAM_MODE_URL + ";" + MotuRequestParametersConstant.PARAM_MODE_CONSOLE + ";"
                + MotuRequestParametersConstant.PARAM_MODE_STATUS + "]";
    }

}
