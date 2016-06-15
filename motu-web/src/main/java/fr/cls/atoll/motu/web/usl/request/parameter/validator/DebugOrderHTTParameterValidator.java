package fr.cls.atoll.motu.web.usl.request.parameter.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.cls.atoll.motu.api.message.xml.StatusModeType;
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
public class DebugOrderHTTParameterValidator extends AbstractHTTPParameterValidator<List<String>> {

    public static final String IN_PROGRESS = StatusModeType.INPROGRESS.toString();
    public static final String DONE = StatusModeType.DONE.toString();
    public static final String ERROR = StatusModeType.ERROR.toString();
    public static final String PENDING = StatusModeType.PENDING.toString();

    public static final String DEFAULT_VALUE = IN_PROGRESS + "," + PENDING + "," + ERROR + "," + DONE;
    public static final List<String> DEFAULT_LIST_ORDER = new ArrayList<String>() {
        {
            add(IN_PROGRESS);
            add(PENDING);
            add(ERROR);
            add(DONE);
        }
    };

    /**
     * Constructeur.
     * 
     * @param parameterName_
     * @param parameterValue_
     */
    public DebugOrderHTTParameterValidator(String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
        if (StringUtils.isNullOrEmpty(parameterValue_)) {
            setParameterValue(DEFAULT_VALUE);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> onValidateAction() throws InvalidHTTPParameterException {
        List<String> order = null;
        if (DEFAULT_VALUE.equals(getParameterValue())) {
            order = DEFAULT_LIST_ORDER;
        } else {
            order = Arrays.asList(getParameterValue().split(","));
            String notExistingItem = "";
            for (String currentItem : order) {
                if (!DEFAULT_LIST_ORDER.contains(currentItem)) {
                    notExistingItem += currentItem + ',';
                }
            }

            if (!StringUtils.isNullOrEmpty(notExistingItem)) {
                throw new InvalidHTTPParameterException(
                        getParameterName(),
                        getParameterValue(),
                        "The parameter(s) value(s) " + notExistingItem + " is not valid.\n" + getParameterBoundaries());
            }
        }
        return order;
    }

    @Override
    protected String getParameterBoundaries() {
        return "The value is the list of values " + DEFAULT_VALUE + " separated by a \",\".\n"
                + "The order of the values determine the display order of each section.\n" + "Each value have to be used only one time";
    }
}
