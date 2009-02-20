package fr.cls.atoll.motu.queueserver;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import fr.cls.atoll.motu.data.Product;
import fr.cls.atoll.motu.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.exception.MotuException;
import fr.cls.atoll.motu.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.exception.MotuMarshallException;
import fr.cls.atoll.motu.exception.MotuNoVarException;
import fr.cls.atoll.motu.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.exception.NetCdfVariableException;
import fr.cls.atoll.motu.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.intfce.ExtractionParameters;
import fr.cls.atoll.motu.intfce.Organizer;
import fr.cls.atoll.motumsg.xml.ErrorType;
import fr.cls.atoll.motumsg.xml.StatusModeResponse;
import fr.cls.atoll.motumsg.xml.StatusModeType;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:26 $
 */

public class RunnableExtraction implements Runnable, Comparable<RunnableExtraction> {

    /** Logger for this class. */
    protected static final Logger LOG = Logger.getLogger(RunnableExtraction.class);

    /** The Constant LOGQUEUE. */
    protected static final Logger LOGQUEUE = Logger.getLogger("atoll.motu.queueserver");

    /** The priority. */
    protected int priority;

    /** The range. */
    protected int range = -1;

    /** The product. */
    protected Product product = null;

    /** The extraction parameters. */
    protected ExtractionParameters extractionParameters = null;

    /** The status mode response. */
    protected StatusModeResponse statusModeResponse = null;

    /** The organizer. */
    protected Organizer organizer = null;

    /** The queue log info. */
    protected QueueLogInfo queueLogInfo = new QueueLogInfo();

    // private QueueLogError queueLogError = null;

    
    /**
     * The Constructor.
     * 
     * @param range the range
     * @param priority the priority
     * @param organizer the organizer
     * @param extractionParameters the extraction parameters
     */
    public RunnableExtraction(int priority, int range, Organizer organizer, ExtractionParameters extractionParameters) {

        init();

        this.priority = priority;
        this.range = range;
        this.extractionParameters = extractionParameters;
        this.organizer = organizer;

        this.queueLogInfo.setExtractionParameters(extractionParameters);
    }

    /**
     * The Constructor.
     * 
     * @param priority the priority
     * @param organizer the organizer
     * @param extractionParameters the extraction parameters
     */
    public RunnableExtraction(int priority, Organizer organizer, ExtractionParameters extractionParameters) {

        this(priority, -1, organizer, extractionParameters);

    }

    /**
     * Init.
     */
    private void init() {
        statusModeResponse = Organizer.createStatusModeResponse();
        setStatusPending();

    }

    /**
     * Gets the user id.
     * 
     * @return the user id
     * 
     * @throws MotuException the motu exception
     */
    public String getUserId() throws MotuException {
        if (extractionParameters == null) {
            MotuException e = new MotuException("ERROR in RunnableExtraction.getUserId : extraction parameter is null");
            setError(e);
            throw e;
        }

        String userId = extractionParameters.getUserId();
        if (userId == null) {
            MotuException e = new MotuException("ERROR in RunnableExtraction.getUserId : no user id filled - user id is null");
            setError(e);
            throw e;
        }
        return userId;
    }

    /**
     * Checks if is anonymous user.
     * 
     * @return true, if is anonymous user
     * 
     * @throws MotuException the motu exception
     */
    public boolean isAnonymousUser() throws MotuException {
        if (extractionParameters == null) {
            MotuException e = new MotuException("ERROR in RunnableExtraction.isAnonymousUser : extraction parameter is null");
            setError(e);
            throw e;
        }

        return extractionParameters.isAnonymousUser();
    }

    // /**
    // * Sets the status error.
    // *
    // * @param e the e
    // * @param errorType the error type
    // */
    // private void setStatusError(Exception e, ErrorType errorType) {
    //        
    // statusModeResponse.setStatus(StatusModeType.ERROR);
    // if (e instanceof MotuExceptionBase) {
    // MotuExceptionBase e2 = (MotuExceptionBase) e;
    // statusModeResponse.setMsg(e2.notifyException());
    // } else {
    // statusModeResponse.setMsg(e.getMessage());
    // }
    // statusModeResponse.setCode(errorType);
    //
    // }

    /**
     * Sets the status in progress.
     */
    private void setStatusInProgress() {
        statusModeResponse.setStatus(StatusModeType.INPROGRESS);
        statusModeResponse.setMsg(StatusModeType.INPROGRESS.toString());
        statusModeResponse.setCode(ErrorType.OK);

    }

    /**
     * Sets the status done.
     * 
     * @throws MotuException 
     */
    private void setStatusDone() throws MotuException {
        
        Organizer.setStatusDone(statusModeResponse, product);
        
    }

    /**
     * Sets the status pending.
     */
    private void setStatusPending() {
        statusModeResponse.setStatus(StatusModeType.PENDING);
        statusModeResponse.setMsg(StatusModeType.PENDING.toString());
        statusModeResponse.setCode(ErrorType.OK);

    }

    /**
     * Run.
     */
    public void run() {
        
//        try {
//            Thread.sleep(60000);
//        } catch (InterruptedException e1) {
//            e1.printStackTrace();
//        }
        if (LOG.isDebugEnabled()) {
            try {
                LOG.debug("RunnableExtraction run() - entering");
                LOG.debug(String.format("RunnableExtraction run() : user id: '%s' - request parameters '%s'", getUserId(), getExtractionParameters()
                        .toString()));
            } catch (Exception e) {
                // Do nothing
            }
        }

        setStatusInProgress();

        try {
            product = organizer.extractData(extractionParameters);
            
 
            setStatusDone();
        
        } catch (Exception e) {
            LOG.error("RunnableExtraction.run()", e);
            MotuException motuException = null;
            try {
                motuException = new MotuException(String.format("An error occurs during extraction (RunnableExtraction.run): user id: '%s' - request parameters '%s'", 
                                                                  getUserId(), getExtractionParameters().toString()), e);
            } catch (MotuException e1) {
                // Do Nothing
            }

            setError(motuException);

        } catch (Error e) {
            LOG.error("RunnableExtraction.run()", e);
            
            MotuException motuException = null;
            try {
                motuException = new MotuException(String.format("An error occurs during extraction (RunnableExtraction.run): user id: '%s' - request paramters '%s'", 
                                                                              getUserId(), getExtractionParameters().toString()), e);
            } catch (MotuException e1) {
                // Do Nothing
            }
            setError(motuException);

        }        

        if (LOG.isDebugEnabled()) {
            LOG.debug("run() - exiting");
        }
    }

    /**
     * Compare to.
     * 
     * @param obj the obj
     * 
     * @return the int
     */
    public int compareTo(RunnableExtraction obj) {
        // int retval = Integer.valueOf(priority).compareTo(Integer.valueOf(obj.getPriority()));
        int objPriority = obj.getPriority();
        int retval = 0;
        if (priority > objPriority) {
            return 1;
        }
        if (priority < objPriority) {
            return -1;
        }
        if (retval == 0) {
            retval = Integer.valueOf(range).compareTo(Integer.valueOf(obj.getRange()));
        }
        // System.out.println(priority + " compareTo " + obj.priority() + " retval: " + retval);
        return retval;
    }

    /**
     * Gets the priority.
     * 
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Increase priority.
     * 
     * @return true, if increase priority
     */
    public boolean increasePriority() {
        
        if (ExtractionThreadPoolExecutor.isHigherPriority(priority)) {
            return false;
        }

        priority--;
        range = -1;
        return true;
    }
    
    /**
     * Sets the priority.
     * 
     * @param rangeValue the range value
     * @param priorityValue the priority value
     */
    public void setPriority(int priorityValue, int rangeValue) {
        
        this.priority = priorityValue;
        this.range = rangeValue;
        
        queueLogInfo.addPriority(this.priority, this.range, null);
    }


    /**
     * Gets the range.
     * 
     * @return the range
     */
    public int getRange() {
        return range;
    }

//    /**
//     * Sets the range.
//     * 
//     * @param range the range
//     */
//    public void setRange(int range) {
//        this.range = range;
//    }

    /**
 * Gets the product.
 * 
 * @return the product
 */
    public Product getProduct() {
        return product;
    }

    /**
     * Gets the organizer.
     * 
     * @return the organizer
     */
    public Organizer getOrganizer() {
        return organizer;
    }

    /**
     * Sets the organizer.
     * 
     * @param organizer the organizer
     */
    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;

    }

    /**
     * Gets the status mode response.
     * 
     * @return the status mode response
     */
    public StatusModeResponse getStatusModeResponse() {
        return statusModeResponse;
    }

    /**
     * Gets the extraction parameters.
     * 
     * @return the extraction parameters
     */
    public ExtractionParameters getExtractionParameters() {
        return extractionParameters;
    }

    /**
     * Sets the extraction parameters.
     * 
     * @param extractionParameters the extraction parameters
     */
    public void setExtractionParameters(ExtractionParameters extractionParameters) {
        this.extractionParameters = extractionParameters;
    }

    /**
     * Gets the amount data size as megabytes.
     * 
     * @return the amount data size as megabytes
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws MotuInconsistencyException the motu inconsistency exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public double getAmountDataSizeAsMBytes() throws MotuInconsistencyException, MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException {

        ExtractionParameters extractionParametersClone = null;

        try {
            extractionParametersClone = (ExtractionParameters) extractionParameters.clone();
            extractionParametersClone.setOut(null);
        } catch (CloneNotSupportedException e) {
            throw new MotuException("Error in RunnableExtraction.getAmountDataSizeAsMBytes", e);
        }
        try {
            product = organizer.getAmountDataSize(extractionParametersClone);
        } catch (MotuMarshallException e) {
            // Do Nothing
        }

        double size = product.getAmountDataSizeAsMBytes();
        this.queueLogInfo.setAmountDataSize(product.getAmountDataSizeAsMBytes());

        return size;
    }
    
    /**
     * Sets the error according to an exception.
     * 
     * @param e the exception
     */
    public void setError(Exception e) {
        Organizer.setError(statusModeResponse, e);
        // queueLogError = new QueueLogError(statusModeResponse.getCode(), statusModeResponse.getMsg());
        queueLogInfo.setQueueLogError(new QueueLogError(statusModeResponse.getCode(), statusModeResponse.getMsg()));
        
        
        if (product != null) {
            product.setLastError(Organizer.getFormattedError(e, null));
        } else {
            organizer.setCurrentProductLastError(Organizer.getFormattedError(e, null));
        }

        // if (e instanceof MotuInconsistencyException) {
        // setStatusError(e, ErrorType.INCONSISTENCY);
        // } else if (e instanceof MotuExceedingQueueCapacityException) {
        // setStatusError(e, ErrorType.EXCEEDING_QUEUE_CAPACITY);
        // } else if (e instanceof MotuExceedingUserCapacityException) {
        // setStatusError(e, ErrorType.EXCEEDING_USER_CAPACITY);
        // } else if (e instanceof MotuInvalidQueuePriorityException) {
        // setStatusError(e, ErrorType.INVALID_QUEUE_PRIORITY);
        // } else if (e instanceof MotuInvalidDateException) {
        // setStatusError(e, ErrorType.INVALID_DATE);
        // } else if (e instanceof MotuInvalidDepthException) {
        // setStatusError(e, ErrorType.INVALID_DEPTH);
        // } else if (e instanceof MotuInvalidLatitudeException) {
        // setStatusError(e, ErrorType.INVALID_LATITUDE);
        // } else if (e instanceof MotuInvalidLongitudeException) {
        // setStatusError(e, ErrorType.INVALID_LONGITUDE);
        // } else if (e instanceof MotuInvalidDateRangeException) {
        // setStatusError(e, ErrorType.INVALID_DATE_RANGE);
        // } else if (e instanceof MotuExceedingCapacityException) {
        // setStatusError(e, ErrorType.EXCEEDING_CAPACITY);
        // } else if (e instanceof MotuNotImplementedException) {
        // setStatusError(e, ErrorType.NOT_IMPLEMENTED);
        // } else if (e instanceof MotuInvalidLatLonRangeException) {
        // setStatusError(e, ErrorType.INVALID_LAT_LON_RANGE);
        // } else if (e instanceof MotuInvalidDepthRangeException) {
        // setStatusError(e, ErrorType.INVALID_DEPTH_RANGE);
        // } else if (e instanceof NetCdfVariableException) {
        // setStatusError(e, ErrorType.NETCDF_VARIABLE);
        // } else if (e instanceof MotuNoVarException) {
        // setStatusError(e, ErrorType.NO_VARIABLE);
        // } else if (e instanceof NetCdfAttributeException) {
        // setStatusError(e, ErrorType.NETCDF_ATTRIBUTE);
        // } else if (e instanceof NetCdfVariableNotFoundException) {
        // setStatusError(e, ErrorType.NETCDF_VARIABLE_NOT_FOUND);
        // } else if (e instanceof MotuException) {
        // setStatusError(e, ErrorType.SYSTEM);
        // } else if (e instanceof Exception) {
        // setStatusError(e, ErrorType.SYSTEM);
        // }

    }

    /**
     * Gets the queuelog info as XML.
     * 
     * @return the queuelog info as XML
     */
    public String getQueuelogInfoAsXML() {

        return queueLogInfo.toXML();
    }

    /**
     * Sets the in queue time.
     */
    public void setInQueueTime() {
        Calendar cal = Calendar.getInstance();
        setInQueueTime(cal.getTime());

    }

    /**
     * Sets the in queue time.
     * 
     * @param date the date
     */
    public void setInQueueTime(Date date) {
        queueLogInfo.setInQueueTime(date);
        //queueLogInfo.addPriority(priority, range, date);

    }

    /**
     * Sets the start time.
     */
    public void setStartTime() {
        Calendar cal = Calendar.getInstance();
        setStartTime(cal.getTime());
    }

    /**
     * Sets the start time.
     * 
     * @param date the date
     */
    public void setStartTime(Date date) {
        queueLogInfo.setStartTime(date);
    }

    /**
     * Sets the end time.
     */
    public void setEndTime() {
        Calendar cal = Calendar.getInstance();
        setEndTime(cal.getTime());
    }

    /**
     * Sets the end time.
     * 
     * @param date the date
     */
    public void setEndTime(Date date) {
        queueLogInfo.setEndTime(date);
    }

    /**
     * Sets the queue id.
     * 
     * @param queueId the queue id
     */
    public void setQueueId(String queueId) {
        queueLogInfo.setQueueId(queueId);
    }

    /**
     * Sets the queue desc.
     * 
     * @param queueDesc the queue desc
     */
    public void setQueueDesc(String queueDesc) {
        queueLogInfo.setQueueDesc(queueDesc);
    }

    // public void addQueueInfoPriority(int priorityValue) {
    // queueLogInfo.addPriority(priorityValue);
    // }

    /**
     * Checks if is out of time.
     * 
     * @param timeOutInMinutes the time out in minutes
     * 
     * @return true, if is out of time
     */
    public boolean isOutOfTime(short timeOutInMinutes) {
        if (ExtractionThreadPoolExecutor.isHigherPriority(priority)) {
            return false;
        }
        
        Date timeToCompare= null;
        
        QueueLogPriority queueLogPriority = queueLogInfo.getMostRecentPriority();
        
        if (queueLogPriority != null) {
            timeToCompare = queueLogPriority.getDate();
        } else {
            timeToCompare = queueLogInfo.getInQueueTime();
        }

        if (timeToCompare == null) {
            return false;
        }

        Calendar cal = Calendar.getInstance();

        long elapsedTime = cal.getTime().getTime() - timeToCompare.getTime();
        long timeOut = timeOutInMinutes * 60 * 1000;

        return elapsedTime >= timeOut;

    }

    /**
     * Sets the in queue.
     */
    public void setInQueue() {
        setInQueueTime();
    }

    /**
     * Sets the started.
     */
    public void setStarted() {
        setStartTime();
    }

    /**
     * Sets the ended.
     */
    public void setEnded() {
        try {
            if (product != null) {
                queueLogInfo.setDownloadUrlPath(product.getDownloadUrlPath());
                queueLogInfo.setExtractLocationData(product.getExtractLocationData());
            }
            setEndTime();
            if (LOGQUEUE.isInfoEnabled()) {
                LOGQUEUE.info(this.queueLogInfo);
            }

        } catch (MotuException e) {
            // Do nothing
        }

    }
    
    /**
     * Aborted.
     */
    public void aborted() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("RunnableExtraction.aborted() - entering");
        }
        
        if (LOGQUEUE.isInfoEnabled()) {
            LOGQUEUE.info(this.queueLogInfo);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("RunnableExtraction.aborted() - exiting");
        }
    }
    
    /**
     * Sets the request id.
     * 
     * @param requestId the request id
     */
    public void setRequestId(long requestId) {
        if (queueLogInfo == null) {
            return;
        }
        queueLogInfo.setRequestId(requestId);
    }

    /**
     * Valeur de queueLogInfo.
     * 
     * @return la valeur.
     */
    public QueueLogInfo getQueueLogInfo() {
        return queueLogInfo;
    }

    /**
     * Checks if is batch queue.
     * 
     * @return true, if is batch queue
     */
    public boolean isBatchQueue() {
        if (extractionParameters == null) {
            return false;
        }
        
        return extractionParameters.isBatchQueue();
    }
    /**
     * Sets the batch queue.
     * 
     * @param batchQueue the batch queue
     */
    public void setBatchQueue(boolean batchQueue) {
        if (extractionParameters == null) {
            return;
        }

        extractionParameters.setBatchQueue(batchQueue);
    }


}
