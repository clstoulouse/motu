package fr.cls.atoll.motu.web.usl.request.parameter.validator;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;

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
public class ModeHTTPParameterValidator {

    private String mode;
    public static final String DEFAULT_MODE = MotuRequestParametersConstant.PARAM_MODE_URL;

    public ModeHTTPParameterValidator(String mode_) {
        mode = mode_;
    }

    /**
     * .
     * 
     * @return null if mode is not valide, otherwise mode
     */
    public String validate() {
        if (mode != null) {
            if (mode.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_MODE_URL)
                    || mode.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_MODE_CONSOLE)
                    || mode.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_MODE_STATUS)) {
                return mode;
            }
        }
        return null;
    }

}
