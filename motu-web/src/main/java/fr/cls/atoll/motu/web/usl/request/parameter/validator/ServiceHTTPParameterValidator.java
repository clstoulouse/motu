package fr.cls.atoll.motu.web.usl.request.parameter.validator;

import fr.cls.atoll.motu.web.common.utils.StringUtils;
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
public class ServiceHTTPParameterValidator extends AbstractHTTPParameterValidator<String> {

    public ServiceHTTPParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    public ServiceHTTPParameterValidator(String parameterName_, String parameterValue_, String defaultValue_) {
        this(parameterName_, parameterValue_);
        if (StringUtils.isNullOrEmpty(parameterValue_)) {
            setParameterValue(defaultValue_);
        }
    }

    /**
     * .
     * 
     */
    @Override
    public String onValidateAction() throws InvalidHTTPParameterException {
        if (StringUtils.isNullOrEmpty(getParameterValue())) {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }

        return getParameterValue();
    }

    @Override
    protected String getParameterBoundaries() {
        return "[A non empty string]";
    }
}
