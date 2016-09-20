package fr.cls.atoll.motu.web.bll.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.AvailableDepths;
import fr.cls.atoll.motu.api.message.xml.AvailableTimes;
import fr.cls.atoll.motu.api.message.xml.Axis;
import fr.cls.atoll.motu.api.message.xml.DataGeospatialCoverage;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.GeospatialCoverage;
import fr.cls.atoll.motu.api.message.xml.ProductMetadataInfo;
import fr.cls.atoll.motu.api.message.xml.RequestSize;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.api.message.xml.TimeCoverage;
import fr.cls.atoll.motu.api.message.xml.Variable;
import fr.cls.atoll.motu.api.message.xml.VariableVocabulary;
import fr.cls.atoll.motu.api.message.xml.Variables;
import fr.cls.atoll.motu.api.message.xml.VariablesVocabulary;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.messageserror.BLLMessagesErrorManager;
import fr.cls.atoll.motu.web.bll.messageserror.IBLLMessagesErrorManager;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.log.RunnableExtraction;
import fr.cls.atoll.motu.web.common.utils.StringUtils;

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
public class ExceptionUtils {

    private static final Logger LOGGER = LogManager.getLogger();

    private static String getErrorMessage(ErrorType errorType) throws MotuException {
        return BLLManager.getInstance().getMessagesErrorManager().getMessageError(errorType);
    }

    public static void setStatusModeResponseException(String actionCode, Exception e, StatusModeResponse statusModeResponse) {
        try {
            ErrorType errorType = getErrorType(e);
            statusModeResponse.setStatus(StatusModeType.ERROR);
            statusModeResponse.setMsg(getErrorMessage(errorType));
            statusModeResponse.setCode(StringUtils.getErrorCode(actionCode, errorType));
        } catch (MotuException errorMessageException) {
            statusModeResponse.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            statusModeResponse.setCode(StringUtils.getErrorCode(actionCode, BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(actionCode, BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Gets the error type.
     * 
     * @param e the e
     * 
     * @return the error type
     */
    public static ErrorType getErrorType(Exception e) {

        if (e instanceof MotuInconsistencyException) {
            return ErrorType.INCONSISTENCY;
        } else if (e instanceof MotuInvalidRequestIdException) {
            return ErrorType.UNKNOWN_REQUEST_ID;
        } else if (e instanceof MotuExceedingQueueDataCapacityException) {
            return ErrorType.EXCEEDING_QUEUE_DATA_CAPACITY;
        } else if (e instanceof MotuExceedingQueueCapacityException) {
            return ErrorType.EXCEEDING_QUEUE_CAPACITY;
        } else if (e instanceof MotuExceedingUserCapacityException) {
            return ErrorType.EXCEEDING_USER_CAPACITY;
        } else if (e instanceof MotuInvalidQueuePriorityException) {
            return ErrorType.INVALID_QUEUE_PRIORITY;
        } else if (e instanceof MotuInvalidDateException) {
            return ErrorType.INVALID_DATE;
        } else if (e instanceof MotuInvalidDepthException) {
            return ErrorType.INVALID_DEPTH;
        } else if (e instanceof MotuInvalidLatitudeException) {
            return ErrorType.INVALID_LATITUDE;
        } else if (e instanceof MotuInvalidLongitudeException) {
            return ErrorType.INVALID_LONGITUDE;
        } else if (e instanceof MotuInvalidDateRangeException) {
            return ErrorType.INVALID_DATE_RANGE;
        } else if (e instanceof MotuExceedingCapacityException) {
            return ErrorType.EXCEEDING_CAPACITY;
        } else if (e instanceof MotuNotImplementedException) {
            return ErrorType.NOT_IMPLEMENTED;
        } else if (e instanceof MotuInvalidLatLonRangeException) {
            return ErrorType.INVALID_LAT_LON_RANGE;
        } else if (e instanceof MotuInvalidDepthRangeException) {
            return ErrorType.INVALID_DEPTH_RANGE;
        } else if (e instanceof NetCdfVariableException) {
            return ErrorType.NETCDF_VARIABLE;
        } else if (e instanceof MotuNoVarException) {
            return ErrorType.NO_VARIABLE;
        } else if (e instanceof NetCdfAttributeException) {
            return ErrorType.NETCDF_ATTRIBUTE;
        } else if (e instanceof NetCdfVariableNotFoundException) {
            return ErrorType.NETCDF_VARIABLE_NOT_FOUND;
        } else if (e instanceof MotuException) {
            return ErrorType.SYSTEM;
        } else if (e instanceof MotuExceptionBase) {
            return ErrorType.SYSTEM;
        }
        return ErrorType.SYSTEM;
    }

    // CSOFF: StrictDuplicateCode : normal duplication code.

    /**
     * Sets the error.
     * 
     * @param requestSize the request size
     * @param e the e
     */
    public static void setError(String actionCode, RequestSize requestSize, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            requestSize.setMsg(getErrorMessage(errorType));
            requestSize.setCode(StringUtils.getErrorCode(actionCode, errorType));
        } catch (MotuException errorMessageException) {
            requestSize.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            requestSize.setCode(StringUtils.getErrorCode(actionCode, BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(actionCode, BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param statusModeResponse the status mode response
     * @param errorType the error type
     */
    public static void setError(String actionCode, StatusModeResponse statusModeResponse, ErrorType errorType) {
        try {
            statusModeResponse.setStatus(StatusModeType.ERROR);
            String message = "";
            if (errorType.equals(ErrorType.SHUTTING_DOWN)) {
                message = RunnableExtraction.SHUTDOWN_MSG;
            } else {
                message = errorType.toString();
            }
            statusModeResponse.setMsg(getErrorMessage(errorType));
            statusModeResponse.setCode(StringUtils.getErrorCode(actionCode, errorType));
            LOGGER.error(StringUtils.getLogMessage(actionCode, errorType, message));
        } catch (MotuException errorMessageException) {
            statusModeResponse.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            statusModeResponse.setCode(StringUtils.getErrorCode(actionCode, BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(actionCode, BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }

    }

    /**
     * Sets the error.
     * 
     * @param e the e
     * @param timeCoverage the time coverage
     */
    public static void setError(TimeCoverage timeCoverage, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            timeCoverage.setMsg(getErrorMessage(errorType));
            timeCoverage.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            timeCoverage.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            timeCoverage.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param e the e
     * @param timeCoverage the time coverage
     */
    public static void setError(String actionCode, TimeCoverage timeCoverage, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            timeCoverage.setMsg(getErrorMessage(errorType));
            timeCoverage.setCode(StringUtils.getErrorCode(actionCode, errorType));
        } catch (MotuException errorMessageException) {
            timeCoverage.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            timeCoverage.setCode(StringUtils.getErrorCode(actionCode, BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(actionCode, BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param productMetadataInfo the product metadata info
     * @param e the e
     */
    public static void setError(ProductMetadataInfo productMetadataInfo, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            productMetadataInfo.setMsg(getErrorMessage(errorType));
            productMetadataInfo.setCode(String.valueOf(errorType.value()));
        } catch (MotuException errorMessageException) {
            productMetadataInfo.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            productMetadataInfo.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param geospatialCoverage the geospatial coverage
     * @param e the e
     */
    public static void setError(GeospatialCoverage geospatialCoverage, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            geospatialCoverage.setMsg(getErrorMessage(errorType));
            geospatialCoverage.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            geospatialCoverage.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            geospatialCoverage.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param dataGeospatialCoverage the data geospatial coverage
     * @param e the e
     */
    public static void setError(DataGeospatialCoverage dataGeospatialCoverage, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            dataGeospatialCoverage.setMsg(getErrorMessage(errorType));
            dataGeospatialCoverage.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            dataGeospatialCoverage
                    .setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            dataGeospatialCoverage.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param axis the axis
     * @param e the e
     */
    public static void setError(Axis axis, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            axis.setMsg(getErrorMessage(errorType));
            axis.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            axis.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            axis.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param availableDepths the available depths
     * @param e the e
     */
    public static void setError(AvailableDepths availableDepths, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            availableDepths.setMsg(getErrorMessage(errorType));
            availableDepths.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            availableDepths.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            availableDepths.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param properties the properties
     * @param e the e
     */
    public static void setError(fr.cls.atoll.motu.api.message.xml.Properties properties, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            properties.setMsg(getErrorMessage(errorType));
            properties.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            properties.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            properties.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param property the property
     * @param e the e
     */
    public static void setError(fr.cls.atoll.motu.api.message.xml.Property property, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            property.setMsg(getErrorMessage(errorType));
            property.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            property.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            property.setCode(String.valueOf(IBLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param variablesVocabulary the variables vocabulary
     * @param e the e
     */
    public static void setError(VariablesVocabulary variablesVocabulary, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            variablesVocabulary.setMsg(getErrorMessage(errorType));
            variablesVocabulary.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            variablesVocabulary.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            variablesVocabulary.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param variableVocabulary the variable vocabulary
     * @param e the e
     */
    public static void setError(VariableVocabulary variableVocabulary, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            variableVocabulary.setMsg(getErrorMessage(errorType));
            variableVocabulary.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            variableVocabulary.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            variableVocabulary.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param variables the variables
     * @param e the e
     */
    public static void setError(Variables variables, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            variables.setMsg(getErrorMessage(errorType));
            variables.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            variables.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            variables.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param variable the variable
     * @param e the e
     */
    public static void setError(Variable variable, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            variable.setMsg(getErrorMessage(errorType));
            variable.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            variable.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            variable.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }

    /**
     * Sets the error.
     * 
     * @param availableTimes the available times
     * @param e the e
     */
    public static void setError(AvailableTimes availableTimes, Exception e) {
        try {
            ErrorType errorType = getErrorType(e);
            availableTimes.setMsg(getErrorMessage(errorType));
            availableTimes.setCode(String.valueOf(errorType));
        } catch (MotuException errorMessageException) {
            availableTimes.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            availableTimes.setCode(String.valueOf(BLLMessagesErrorManager.SYSTEM_ERROR_CODE));
            LOGGER.error(StringUtils.getLogMessage(BLLMessagesErrorManager.SYSTEM_ERROR_CODE, errorMessageException.getMessage()),
                         errorMessageException);
        }
    }
}
