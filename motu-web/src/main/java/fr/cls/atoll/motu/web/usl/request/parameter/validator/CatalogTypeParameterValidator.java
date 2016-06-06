package fr.cls.atoll.motu.web.usl.request.parameter.validator;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
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
public class CatalogTypeParameterValidator extends AbstractHTTPParameterValidator<String> {

    public CatalogTypeParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    public CatalogTypeParameterValidator(String parameterName_, String parameterValue_, String defaultValue_) {
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
        if ((isParameterOptional() && StringUtils.isNullOrEmpty(getParameterValue())) || (!StringUtils.isNullOrEmpty(getParameterValue())
                && (getParameterValue().equalsIgnoreCase(MotuRequestParametersConstant.PARAM_CATALOG_TYPE)
                        || getParameterValue().equalsIgnoreCase(MotuRequestParametersConstant.PARAM_CATALOG_TYPE_TDS)
                        || getParameterValue().equalsIgnoreCase(MotuRequestParametersConstant.PARAM_CATALOG_TYPE_FTP)))) {
            return getParameterValue();
        } else {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }
    }

    @Override
    protected String getParameterBoundaries() {
        return "[TDS,FTP]";
    }

}
