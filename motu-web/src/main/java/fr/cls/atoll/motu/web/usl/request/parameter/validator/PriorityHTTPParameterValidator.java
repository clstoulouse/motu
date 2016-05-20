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
public class PriorityHTTPParameterValidator extends AbstractHTTPParameterValidator<Short> {

    public PriorityHTTPParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    public PriorityHTTPParameterValidator(String parameterName_, String parameterValue_, String defaultValue_) {
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
    public Short onValidateAction() throws InvalidHTTPParameterException {
        String priorityStr = getParameterValue();
        try {
            Short priority = Short.parseShort(priorityStr);
            if (priority < 1 || priority > 2) {
                throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
            }
            return priority;
        } catch (Exception e) {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }
    }

    @Override
    protected String getParameterBoundaries() {
        return "[1;2]";
    }
}
