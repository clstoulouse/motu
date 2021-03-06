package fr.cls.atoll.motu.web.usl.request.parameter.validator;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.NullOrEmptyInvalidHTTPParameterException;

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
public class ProductHTTPParameterValidator extends AbstractHTTPParameterValidator<String> {

    public ProductHTTPParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    public ProductHTTPParameterValidator(String parameterName_, String parameterValue_, String defaultValue_) {
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
        String productStr = getParameterValue();
        if (StringUtils.isNullOrEmpty(productStr)) {
            throw new NullOrEmptyInvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }
        try {
            return URLDecoder.decode(productStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }
    }

    @Override
    protected String getParameterBoundaries() {
        return "String should not be empty";
    }
}
