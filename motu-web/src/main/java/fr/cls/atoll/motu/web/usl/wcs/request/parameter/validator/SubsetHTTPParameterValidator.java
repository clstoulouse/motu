package fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator;

import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.AbstractHTTPParameterValidator;

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
public class SubsetHTTPParameterValidator extends AbstractHTTPParameterValidator<String> {

    public SubsetHTTPParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    public SubsetHTTPParameterValidator(String parameterName_, String parameterValue_, String defaultValue_) {
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
        String subsetStr = getParameterValue();
        try {
            if (subsetStr == null || subsetStr.trim().length() <= 0) {
                throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
            }
            int startValuesPosition = subsetStr.indexOf('(');
            int separatorPosition = subsetStr.indexOf(',');
            int endValuesPosition = subsetStr.indexOf(')');
            if (!(startValuesPosition != 0 && startValuesPosition < separatorPosition && separatorPosition < endValuesPosition)) {
                throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
            }
            return subsetStr;
        } catch (Exception e) {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }
    }

    @Override
    protected String getParameterBoundaries() {
        return "[paramName(lowerValue,upperValue)]";
    }
}
