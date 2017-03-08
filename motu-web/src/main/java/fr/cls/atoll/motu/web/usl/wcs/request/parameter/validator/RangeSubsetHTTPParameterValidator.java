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
public class RangeSubsetHTTPParameterValidator extends AbstractHTTPParameterValidator<String> {

    public RangeSubsetHTTPParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    public RangeSubsetHTTPParameterValidator(String parameterName_, String parameterValue_, String defaultValue_) {
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
        String rangeStr = getParameterValue();
        if (isParameterOptional()) {
            if (rangeStr == null || !"".equals(rangeStr)) {
                return rangeStr;
            } else {
                throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
            }
        } else if (rangeStr == null || "".equals(rangeStr)) {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        } else {
            return rangeStr;
        }
    }

    @Override
    protected String getParameterBoundaries() {
        if (isParameterOptional()) {
            return "Optional but can not be empty if is provided";
        } else {
            return "Can not be empty";
        }
    }
}
