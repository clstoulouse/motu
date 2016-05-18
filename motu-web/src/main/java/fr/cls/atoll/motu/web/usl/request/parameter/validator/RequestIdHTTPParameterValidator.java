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
public class RequestIdHTTPParameterValidator extends AbstractHTTPParameterValidator {

    private String requestId;

    public RequestIdHTTPParameterValidator(String requestId_) {
        requestId = requestId_;
    }

    /**
     * .
     * 
     * @return null if mode is not valide, otherwise mode
     */
    public long validate() {
        long rqtId = 0L;
        try{
            rqtId = Long.parseLong(requestId);
        }
        
        return null;
    }

}
