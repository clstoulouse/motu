package fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator;

import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.AbstractHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.actions.Constants;

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
public class FormatHTTPParameterValidator extends AbstractHTTPParameterValidator<String> {

    public FormatHTTPParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    public FormatHTTPParameterValidator(String parameterName_, String parameterValue_, String defaultValue_) {
        this(parameterName_, parameterValue_);
        if (StringUtils.isNullOrEmpty(parameterValue_)) {
            setParameterValue(defaultValue_);
        }
    }

    @Override
    public String onValidateAction() throws InvalidHTTPParameterException {
        String formatStr = getParameterValue();
        if (formatStr == null) {
            if (!isParameterOptional()) {
                throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
            } else {
                formatStr = Constants.NETCDF_MIME_TYPE;
            }
        } else if ("".equals(formatStr)) {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }

        if (!Constants.NETCDF_MIME_TYPE.equals(formatStr)) {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }

        return formatStr;
    }

    @Override
    protected String getParameterBoundaries() {
        String formatStr = getParameterValue();
        if (formatStr == null) {
            if (!isParameterOptional()) {
                return "Can not be empty";
            }
        } else if ("".equals(formatStr)) {
            if (isParameterOptional()) {
                return "Optional but can not be empty if is provided";
            } else {
                return "Can not be empty";
            }
        }
        return "The available format is only \"" + Constants.NETCDF_MIME_TYPE + "\"";
    }
}
