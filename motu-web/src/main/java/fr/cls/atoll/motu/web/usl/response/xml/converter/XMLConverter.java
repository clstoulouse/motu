package fr.cls.atoll.motu.web.usl.response.xml.converter;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceedingQueueCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceedingQueueDataCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceedingUserCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidQueuePriorityException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.web.bll.exception.MotuNoVarException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.common.utils.UnitUtils;

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
public class XMLConverter {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    public static StatusModeResponse convertStatusModeResponse(RequestDownloadStatus requestDownloadStatus) throws MotuException {
        return getResponse(String.valueOf(convertErrorCode(requestDownloadStatus.getRunningException())), requestDownloadStatus);
    }

    public static StatusModeResponse convertStatusModeResponse(String actionCode, RequestDownloadStatus requestDownloadStatus) throws MotuException {
        return getResponse(actionCode, requestDownloadStatus);
    }

    private static StatusModeResponse getResponse(String actionCode, RequestDownloadStatus requestDownloadStatus) throws MotuException {
        StatusModeResponse smr = new StatusModeResponse();
        smr.setCode(StringUtils.getErrorCode(actionCode,
                                             requestDownloadStatus.getRunningException() == null ? ErrorType.OK
                                                     : requestDownloadStatus.getRunningException().getErrorType()));
        smr.setDateProc(dateToXMLGregorianCalendar(requestDownloadStatus.getStartProcessingDateTime()));
        smr.setDateSubmit(dateToXMLGregorianCalendar(requestDownloadStatus.getCreationDateTime()));
        String msg = "";
        if (requestDownloadStatus.getRunningException() != null) {
            if (requestDownloadStatus.getRunningException() instanceof MotuException) {
                if (requestDownloadStatus.getRunningException().getCause() instanceof MotuExceptionBase) {
                    msg = StringUtils.getLogMessage(actionCode,
                                                    requestDownloadStatus.getRunningException().getErrorType(),
                                                    BLLManager.getInstance().getMessagesErrorManager()
                                                            .getMessageError(requestDownloadStatus.getRunningException().getErrorType(),
                                                                             requestDownloadStatus.getRunningException().getCause().getMessage()));
                } else {
                    msg = StringUtils.getLogMessage(actionCode,
                                                    requestDownloadStatus.getRunningException().getErrorType(),
                                                    BLLManager.getInstance().getMessagesErrorManager()
                                                            .getMessageError(requestDownloadStatus.getRunningException().getErrorType(),
                                                                             requestDownloadStatus.getRunningException().getMessage()));
                }
            }
        }
        smr.setMsg(msg);
        smr.setLocalUri(BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath() + "/"
                + requestDownloadStatus.getExtractFilename());
        smr.setRemoteUri(BLLManager.getInstance().getConfigManager().getMotuConfig().getDownloadHttpUrl() + "/"
                + requestDownloadStatus.getExtractFilename());
        smr.setRequestId(requestDownloadStatus.getRequestId());

        smr.setSize(UnitUtils.toMegaBytes(requestDownloadStatus.getSizeInBits()));
        smr.setStatus(convertStatusModeResponse(requestDownloadStatus.getRequestStatus()));
        smr.setUserHost(requestDownloadStatus.getUserHost());
        smr.setUserId(requestDownloadStatus.getUserId());
        smr.setScriptVersion(requestDownloadStatus.getScriptVersion());
        return smr;
    }

    public static ErrorType convertErrorCode(Exception e_) {
        ErrorType errorType = null;
        if (e_ == null) {
            errorType = ErrorType.OK;
        } else {
            if (e_ instanceof MotuInconsistencyException) {
                return ErrorType.INCONSISTENCY;
            } else if (e_ instanceof MotuInvalidRequestIdException) {
                return ErrorType.UNKNOWN_REQUEST_ID;
            } else if (e_ instanceof MotuExceedingQueueDataCapacityException) {
                return ErrorType.EXCEEDING_QUEUE_DATA_CAPACITY;
            } else if (e_ instanceof MotuExceedingQueueCapacityException) {
                return ErrorType.EXCEEDING_QUEUE_CAPACITY;
            } else if (e_ instanceof MotuExceedingUserCapacityException) {
                return ErrorType.EXCEEDING_USER_CAPACITY;
            } else if (e_ instanceof MotuInvalidQueuePriorityException) {
                return ErrorType.INVALID_QUEUE_PRIORITY;
            } else if (e_ instanceof MotuInvalidDateException) {
                return ErrorType.INVALID_DATE;
            } else if (e_ instanceof MotuInvalidDepthException) {
                return ErrorType.INVALID_DEPTH;
            } else if (e_ instanceof MotuInvalidLatitudeException) {
                return ErrorType.INVALID_LATITUDE;
            } else if (e_ instanceof MotuInvalidLongitudeException) {
                return ErrorType.INVALID_LONGITUDE;
            } else if (e_ instanceof MotuInvalidDateRangeException) {
                return ErrorType.INVALID_DATE_RANGE;
            } else if (e_ instanceof MotuExceedingCapacityException) {
                return ErrorType.EXCEEDING_CAPACITY;
            } else if (e_ instanceof MotuNotImplementedException) {
                return ErrorType.NOT_IMPLEMENTED;
            } else if (e_ instanceof MotuInvalidLatLonRangeException) {
                return ErrorType.INVALID_LAT_LON_RANGE;
            } else if (e_ instanceof MotuInvalidDepthRangeException) {
                return ErrorType.INVALID_DEPTH_RANGE;
            } else if (e_ instanceof NetCdfVariableException) {
                return ErrorType.NETCDF_VARIABLE;
            } else if (e_ instanceof MotuNoVarException) {
                return ErrorType.NO_VARIABLE;
            } else if (e_ instanceof NetCdfAttributeException) {
                return ErrorType.NETCDF_ATTRIBUTE;
            } else if (e_ instanceof NetCdfVariableNotFoundException) {
                return ErrorType.NETCDF_VARIABLE_NOT_FOUND;
            } else if (e_ instanceof MotuException) {
                return ErrorType.SYSTEM;
            } else if (e_ instanceof MotuExceptionBase) {
                return ErrorType.SYSTEM;
            }
            return ErrorType.SYSTEM;
        }

        return errorType;
    }

    public static StatusModeType convertStatusModeResponse(int requestDownloadStatusValue) {
        StatusModeType statusModeType = null;
        switch (requestDownloadStatusValue) {
        case RequestDownloadStatus.STATUS_DONE:
            statusModeType = StatusModeType.DONE;
            break;
        case RequestDownloadStatus.STATUS_IN_PROGRESS:
            statusModeType = StatusModeType.INPROGRESS;
            break;
        case RequestDownloadStatus.STATUS_PENDING:
            statusModeType = StatusModeType.PENDING;
            break;
        case RequestDownloadStatus.STATUS_ERROR:
            statusModeType = StatusModeType.ERROR;
            break;
        default:
            LOGGER.error("Unknown RequestDownloadStatus status value: " + requestDownloadStatusValue);
            statusModeType = StatusModeType.ERROR;
            break;
        }
        return statusModeType;
    }

    /**
     * Date to XML gregorian calendar.
     * 
     * @param date the date
     * 
     * @return the XML gregorian calendar
     * 
     * @throws MotuException the motu exception
     */
    public static XMLGregorianCalendar dateToXMLGregorianCalendar(long date) throws MotuException {
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.setTimeInMillis(date);
        XMLGregorianCalendar xmlGregorianCalendar;
        try {
            xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
        } catch (DatatypeConfigurationException e) {
            throw new MotuException(ErrorType.INVALID_DATE, "ERROR in dateToXMLGregorianCalendar", e);
        }
        return xmlGregorianCalendar;
    }
}
