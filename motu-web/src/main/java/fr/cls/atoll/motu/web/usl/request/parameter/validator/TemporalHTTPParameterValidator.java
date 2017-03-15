package fr.cls.atoll.motu.web.usl.request.parameter.validator;

import java.text.SimpleDateFormat;
import java.util.Date;

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
public class TemporalHTTPParameterValidator extends AbstractHTTPParameterValidator<Date> {

    private static final String[] DATE_FORMATS = new String[] { "yyyy-mm-dd h:m:s", "yyyy-mm-dd'T'h:m:s", "yyyy-mm-dd" };

    public TemporalHTTPParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
    }

    /**
     * .
     * 
     */
    @Override
    public Date onValidateAction() throws InvalidHTTPParameterException {
        Date date = null;
        int i = 0;
        while (date == null && i < DATE_FORMATS.length) {
            date = parseDate(DATE_FORMATS[i]);
            i++;
        }

        if (date == null) {
            throw new InvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }

        return date;
    }

    private Date parseDate(String dateFormat_) {
        SimpleDateFormat fmt = new SimpleDateFormat(dateFormat_);
        Date date = null;
        try {
            date = fmt.parse(getParameterValue());
        } catch (Exception e) {
            // noop
        }
        return date;
    }

    @Override
    protected String getParameterBoundaries() {
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < DATE_FORMATS.length; i++) {
            sb.append(DATE_FORMATS[i]);
            if (i < DATE_FORMATS.length - 1) {
                sb.append(";");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
