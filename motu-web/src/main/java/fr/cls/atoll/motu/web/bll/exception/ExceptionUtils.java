package fr.cls.atoll.motu.web.bll.exception;

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
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingQueueCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingQueueDataCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingUserCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidQueuePriorityException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.bll.request.queueserver.RunnableExtraction;

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

    public static void setStatusModeResponseException(Exception e, StatusModeResponse statusModeResponse) {
        ErrorType errorType = getErrorType(e);
        statusModeResponse.setStatus(StatusModeType.ERROR);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            statusModeResponse.setMsg(e2.notifyException());
        } else {
            statusModeResponse.setMsg(e.getMessage());
        }
        statusModeResponse.setCode(errorType);
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
    public static void setError(RequestSize requestSize, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            requestSize.setMsg(e2.notifyException());
        } else {
            requestSize.setMsg(e.getMessage());
        }
        requestSize.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param statusModeResponse the status mode response
     * @param errorType the error type
     */
    public static void setError(StatusModeResponse statusModeResponse, ErrorType errorType) {
        statusModeResponse.setStatus(StatusModeType.ERROR);
        if (errorType.equals(ErrorType.SHUTTING_DOWN)) {
            statusModeResponse.setMsg(RunnableExtraction.SHUTDOWN_MSG);
        } else {
            statusModeResponse.setMsg(errorType.toString());
        }
        statusModeResponse.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param e the e
     * @param timeCoverage the time coverage
     */
    public static void setError(TimeCoverage timeCoverage, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            timeCoverage.setMsg(e2.notifyException());
        } else {
            timeCoverage.setMsg(e.getMessage());
        }
        timeCoverage.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param productMetadataInfo the product metadata info
     * @param e the e
     */
    public static void setError(ProductMetadataInfo productMetadataInfo, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            productMetadataInfo.setMsg(e2.notifyException());
        } else {
            productMetadataInfo.setMsg(e.getMessage());
        }
        productMetadataInfo.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param geospatialCoverage the geospatial coverage
     * @param e the e
     */
    public static void setError(GeospatialCoverage geospatialCoverage, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            geospatialCoverage.setMsg(e2.notifyException());
        } else {
            geospatialCoverage.setMsg(e.getMessage());
        }
        geospatialCoverage.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param dataGeospatialCoverage the data geospatial coverage
     * @param e the e
     */
    public static void setError(DataGeospatialCoverage dataGeospatialCoverage, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            dataGeospatialCoverage.setMsg(e2.notifyException());
        } else {
            dataGeospatialCoverage.setMsg(e.getMessage());
        }
        dataGeospatialCoverage.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param axis the axis
     * @param e the e
     */
    public static void setError(Axis axis, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            axis.setMsg(e2.notifyException());
        } else {
            axis.setMsg(e.getMessage());
        }
        axis.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param availableDepths the available depths
     * @param e the e
     */
    public static void setError(AvailableDepths availableDepths, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            availableDepths.setMsg(e2.notifyException());
        } else {
            availableDepths.setMsg(e.getMessage());
        }
        availableDepths.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param properties the properties
     * @param e the e
     */
    public static void setError(fr.cls.atoll.motu.api.message.xml.Properties properties, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            properties.setMsg(e2.notifyException());
        } else {
            properties.setMsg(e.getMessage());
        }
        properties.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param property the property
     * @param e the e
     */
    public static void setError(fr.cls.atoll.motu.api.message.xml.Property property, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            property.setMsg(e2.notifyException());
        } else {
            property.setMsg(e.getMessage());
        }
        property.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param variablesVocabulary the variables vocabulary
     * @param e the e
     */
    public static void setError(VariablesVocabulary variablesVocabulary, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            variablesVocabulary.setMsg(e2.notifyException());
        } else {
            variablesVocabulary.setMsg(e.getMessage());
        }
        variablesVocabulary.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param variableVocabulary the variable vocabulary
     * @param e the e
     */
    public static void setError(VariableVocabulary variableVocabulary, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            variableVocabulary.setMsg(e2.notifyException());
        } else {
            variableVocabulary.setMsg(e.getMessage());
        }
        variableVocabulary.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param variables the variables
     * @param e the e
     */
    public static void setError(Variables variables, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            variables.setMsg(e2.notifyException());
        } else {
            variables.setMsg(e.getMessage());
        }
        variables.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param variable the variable
     * @param e the e
     */
    public static void setError(Variable variable, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            variable.setMsg(e2.notifyException());
        } else {
            variable.setMsg(e.getMessage());
        }
        variable.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param availableTimes the available times
     * @param e the e
     */
    public static void setError(AvailableTimes availableTimes, Exception e) {
        ErrorType errorType = getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            availableTimes.setMsg(e2.notifyException());
        } else {
            availableTimes.setMsg(e.getMessage());
        }
        availableTimes.setCode(errorType);

    }
}
