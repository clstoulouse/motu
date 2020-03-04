package fr.cls.atoll.motu.web.usl.response.xml.converter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Interval;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.ObjectFactory;
import fr.cls.atoll.motu.api.message.xml.ProductMetadataInfo;
import fr.cls.atoll.motu.api.message.xml.RequestSize;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.api.message.xml.TimeCoverage;
import fr.cls.atoll.motu.api.utils.JAXBWriter;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.ExceptionUtils;
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
import fr.cls.atoll.motu.web.bll.request.status.data.DownloadStatus;
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
public class XMLConverter {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    private XMLConverter() {
        // Nothing to do
    }

    private static String convertToStringWithJAXBtoString(Object objTowWrite) throws MotuException {
        String xmlResult = null;
        try (StringWriter sw = new StringWriter()) {
            JAXBWriter.getInstance().write(objTowWrite, sw);
            xmlResult = sw.getBuffer().toString();
        } catch (IOException | JAXBException e) {
            throw new MotuException(ErrorType.SYSTEM, "Error during JAXB serialisation", e);
        }
        return xmlResult;
    }

    /**
     * .
     * 
     * @param requestSize
     * @return
     * @throws MotuException
     */
    public static String toXMLString(RequestSize requestSize) throws MotuException {
        return convertToStringWithJAXBtoString(requestSize);
    }

    /**
     * .
     * 
     * @param rs
     * @param requestId
     * @param actionCode
     * @return
     * @throws MotuException
     */
    public static String toXMLString(DownloadStatus rs, String requestId, String actionCode) throws MotuException {
        return convertToStringWithJAXBtoString(convertStatusModeResponse(actionCode, requestId, rs));
    }

    /**
     * .
     * 
     * @param motuInvalidRequestIdException
     * @param actionCode
     * @return
     * @throws MotuException
     */
    public static String toXMLString(MotuInvalidRequestIdException motuInvalidRequestIdException, String actionCode) throws MotuException {
        return convertToStringWithJAXBtoString(createStatusModeResponse(motuInvalidRequestIdException, actionCode));
    }

    /**
     * Creates the status mode response.
     * 
     * @param e the e
     * 
     * @return the status mode response
     */
    private static StatusModeResponse createStatusModeResponse(Exception e, String actionCode) {
        ObjectFactory objectFactory = new ObjectFactory();
        StatusModeResponse statusModeResponse = objectFactory.createStatusModeResponse();
        ExceptionUtils.setStatusModeResponseException(actionCode, e, statusModeResponse);
        return statusModeResponse;
    }

    /**
     * .
     * 
     * @param timeCoverage
     * @param actionCode
     * @return
     * @throws MotuException
     */
    public static String toXMLString(Interval interval, String actionCode) throws MotuException {
        return convertToStringWithJAXBtoString(getTimeCoverage(interval, actionCode));
    }

    /**
     * Inits the time coverage.
     * 
     * @param datePeriod the date period
     * 
     * @return the time coverage
     * 
     * @throws MotuException the motu exception
     */
    private static TimeCoverage getTimeCoverage(Interval datePeriod, String actionCode) throws MotuException {
        TimeCoverage timeCoverage = createTimeCoverage(actionCode);
        if (datePeriod == null) {
            return timeCoverage;
        }

        Date start = datePeriod.getStart().toDate();
        Date end = datePeriod.getEnd().toDate();

        timeCoverage.setStart(dateToXMLGregorianCalendar(start));
        timeCoverage.setEnd(dateToXMLGregorianCalendar(end));
        timeCoverage.setCode(StringUtils.getErrorCode(actionCode, ErrorType.OK));
        timeCoverage.setMsg(ErrorType.OK.toString());

        return timeCoverage;
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
    private static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) throws MotuException {
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.setTime(date);
        XMLGregorianCalendar xmlGregorianCalendar;
        try {
            xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
        } catch (DatatypeConfigurationException e) {
            throw new MotuException(ErrorType.INVALID_DATE, "ERROR in dateToXMLGregorianCalendar", e);
        }
        return xmlGregorianCalendar;
    }

    /**
     * Creates the time coverage.
     * 
     * @return the time coverage
     */
    private static TimeCoverage createTimeCoverage(String actionCode) {
        ObjectFactory objectFactory = new ObjectFactory();

        TimeCoverage timeCoverage = objectFactory.createTimeCoverage();
        timeCoverage.setStart(null);
        timeCoverage.setEnd(null);
        ExceptionUtils
                .setError(actionCode,
                          timeCoverage,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return timeCoverage;

    }

    public static String toXMLString(ProductMetadataInfo productMetaDataInfo) throws MotuException {
        return convertToStringWithJAXBtoString(productMetaDataInfo);
    }

    public static String toXMLString(String requestId, String actionCode, String scriptVersion) throws MotuException {
        return convertToStringWithJAXBtoString(createStatusModeResponse(requestId, actionCode, scriptVersion));
    }

    private static StatusModeResponse createStatusModeResponse(String requestId, String actionCode, String scriptVersion) {
        ObjectFactory objectFactory = new ObjectFactory();
        StatusModeResponse statusModeResponse = objectFactory.createStatusModeResponse();
        statusModeResponse.setCode(StringUtils.getErrorCode(actionCode, ErrorType.OK));
        statusModeResponse.setStatus(StatusModeType.INPROGRESS);
        statusModeResponse.setMsg("request in progress");
        statusModeResponse.setRequestId(requestId);
        statusModeResponse.setScriptVersion(scriptVersion);
        return statusModeResponse;
    }

    /**
     * .
     * 
     * @param motuException
     * @return
     * @throws MotuException
     */
    public static String toXMLString(MotuException motuException, String actionCode, String scriptVersion) throws MotuException {
        return convertToStringWithJAXBtoString(createStatusModeResponseInError(motuException, actionCode, scriptVersion));
    }

    private static StatusModeResponse createStatusModeResponseInError(MotuException motuException, String actionCode, String scriptVersion) {
        ObjectFactory objectFactory = new ObjectFactory();
        StatusModeResponse statusModeResponse = objectFactory.createStatusModeResponse();
        statusModeResponse.setCode(StringUtils.getErrorCode(actionCode, ErrorType.OK));
        statusModeResponse.setStatus(StatusModeType.ERROR);
        statusModeResponse.setMsg(StringUtils.getLogMessage(actionCode,
                                                            motuException.getErrorType(),
                                                            BLLManager.getInstance().getMessagesErrorManager()
                                                                    .getMessageError(motuException.getErrorType(), motuException.getMessage())));
        statusModeResponse.setRequestId(null);
        statusModeResponse.setScriptVersion(scriptVersion);
        return statusModeResponse;
    }

    public static StatusModeResponse convertStatusModeResponse(String actionCode, String requestId, DownloadStatus rs) throws MotuException {
        StatusModeResponse smr = new StatusModeResponse();
        smr.setCode(StringUtils.getErrorCode(actionCode, rs.getErrorType()));
        smr.setDateProc(dateToXMLGregorianCalendar(rs.getStartProcessingDateTime()));
        smr.setDateSubmit(dateToXMLGregorianCalendar(rs.getCreationDateTime()));
        smr.setMsg(rs.getMessage());
        smr.setLocalUri(rs.getLocalUri());
        smr.setRemoteUri(rs.getRemoteUri());
        smr.setRequestId(requestId);

        smr.setSize(Double.parseDouble(rs.getSize()));
        smr.setStatus(convertStatusModeResponse(Integer.parseInt(rs.getStatusCode())));
        smr.setUserHost(rs.getUserHost());
        smr.setUserId(rs.getUserId());
        smr.setScriptVersion(rs.getScriptVersion());
        return smr;
    }

    public static ErrorType convertErrorCode(Exception e) {
        ErrorType errorType = null;
        if (e == null) {
            errorType = ErrorType.OK;
        } else {
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
