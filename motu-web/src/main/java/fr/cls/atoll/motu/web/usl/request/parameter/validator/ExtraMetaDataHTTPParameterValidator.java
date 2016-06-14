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
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class ExtraMetaDataHTTPParameterValidator extends AbstractHTTPParameterValidator<Boolean> {

    /**
     * Constructeur.
     * 
     * @param parameterName_
     * @param parameterValue_
     */
    public ExtraMetaDataHTTPParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    public ExtraMetaDataHTTPParameterValidator(String parameterName_, String parameterValue_, String defaultValue_) {
        this(parameterName_, parameterValue_);
        if (StringUtils.isNullOrEmpty(parameterValue_)) {
            setParameterValue(defaultValue_);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Boolean onValidateAction() throws InvalidHTTPParameterException {
        if (StringUtils.isNullOrEmpty(getParameterValue())) {
            return true;
        }

        return getParameterValue().equalsIgnoreCase("true") || getParameterValue().equalsIgnoreCase("1");
    }

    @Override
    protected String getParameterBoundaries() {
        return "[A non empty boolean value equals to \"true\", \"false\". \"1\" is considered as \"true\" and other values as \"false\"]";
    }
}
