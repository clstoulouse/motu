package fr.cls.atoll.motu.web.usl.request.parameter.validator;

import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

/**
 * . <br>
 * <br>
 * Copyright : Copyright (c) 2017 <br>
 * <br>
 * Company : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1456 $ - $Date: 2011-04-08 18:37:34 +0200 $
 */
public class ConfigServiceNamesHTTPParameterValidator extends AbstractHTTPParameterValidator<String> {

    /**
     * Constructor.
     * 
     * @param parameterName_
     * @param parameterValue_
     */
    public ConfigServiceNamesHTTPParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    /** {@inheritDoc} */
    @Override
    protected String onValidateAction() throws InvalidHTTPParameterException {
        String cacheType = getParameterValue();
        if (StringUtils.isNullOrEmpty(cacheType)) {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }
        return cacheType;
    }

    @Override
    protected String getParameterBoundaries() {
        return "[A least one config service shall be defined. Several config service can be defined separated by a comma. Config service names are defined in config/motuConfiguration.xml#motuConfig/configService@name. The value can be ALL or ONLYAUTO for specific behaviour]";
    }

}
