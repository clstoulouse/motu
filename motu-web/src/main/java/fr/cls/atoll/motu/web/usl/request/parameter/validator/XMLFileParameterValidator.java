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
public class XMLFileParameterValidator extends AbstractHTTPParameterValidator<String> {

    /**
     * Constructeur.
     * 
     * @param parameterName_
     * @param parameterValue_
     */
    public XMLFileParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    public XMLFileParameterValidator(String parameterName_, String parameterValue_, String defaultValue_) {
        this(parameterName_, parameterValue_);
        if (StringUtils.isNullOrEmpty(parameterValue_)) {
            setParameterValue(defaultValue_);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected String onValidateAction() throws InvalidHTTPParameterException {
        if (!AbstractHTTPParameterValidator.EMPTY_VALUE.equals(getParameterValue())
                && (StringUtils.isNullOrEmpty(getParameterValue()) || !getParameterValue().endsWith(".xml"))) {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }

        return getParameterValue();
    }

    @Override
    protected String getParameterBoundaries() {
        return "[A non empty xml file name]";
    }
}
