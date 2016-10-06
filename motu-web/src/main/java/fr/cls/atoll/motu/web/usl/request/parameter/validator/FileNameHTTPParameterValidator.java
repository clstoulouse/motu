package fr.cls.atoll.motu.web.usl.request.parameter.validator;

import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.NullOrEmptyInvalidHTTPParameterException;

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
public class FileNameHTTPParameterValidator extends AbstractHTTPParameterValidator<String> {

    private String fileNamePattern;

    /**
     * Constructeur.
     * 
     * @param parameterName_
     * @param parameterValue_
     */
    public FileNameHTTPParameterValidator(String fileNamePattern, String parameterName_, String parameterValue_) {
        super(parameterName_, parameterValue_);
        setFileNamePattern(fileNamePattern);
    }

    public FileNameHTTPParameterValidator(String fileNamePattern, String parameterName_, String parameterValue_, String defaultValue_) {
        this(fileNamePattern, parameterName_, parameterValue_);
        if (StringUtils.isNullOrEmpty(parameterValue_)) {
            setParameterValue(defaultValue_);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected String onValidateAction() throws InvalidHTTPParameterException {
        if (getParameterValue().equalsIgnoreCase(EMPTY_VALUE)) {
            return null;
        }

        if (!getParameterValue().matches(fileNamePattern)) {
            throw new NullOrEmptyInvalidHTTPParameterException(getParameterName(), getParameterValue(), getParameterBoundaries());
        }

        return getParameterValue();
    }

    @Override
    protected String getParameterBoundaries() {
        return "[A string matching pattern: " + fileNamePattern + " ]";
    }

    /**
     * Valeur de fileNamePattern.
     * 
     * @return la valeur.
     */
    public String getFileNamePattern() {
        return fileNamePattern;
    }

    /**
     * Valeur de fileNamePattern.
     * 
     * @param fileNamePattern nouvelle valeur.
     */
    public void setFileNamePattern(String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
    }

}
