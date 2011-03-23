/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.library.misc.queueserver;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.library.misc.data.Product;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.misc.intfce.ExtractionParameters;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class RunnableExtraction implements Runnable, Comparable<RunnableExtraction> {

    /** Logger for this class. */
    protected static final Logger LOG = Logger.getLogger(RunnableExtraction.class);

    /** The Constant LOGQUEUE. */
    protected static final Logger LOGQUEUE = Logger.getLogger("atoll.motu.queueserver");

    public static final String SHUTDOWN_MSG = "For maintenance reasons, the application is shutting down. We apologize for the inconvenience. You may repeat your query later.";

    /** The extraction parameters. */
    protected ExtractionParameters extractionParameters = null;

    /** The organizer. */
    protected Organizer organizer = null;

    /** The priority. */
    protected int priority;

    /** The product. */
    protected Product product = null;

    /** The queue log info. */
    protected QueueLogInfo queueLogInfo = new QueueLogInfo();

    /** The range. */
    protected int range = -1;

    /** The status mode response. */
    protected StatusModeResponse statusModeResponse = null;


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
     * Shutdown the runnable
     */
    public void shutdown() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("shutdown() - start");
        }
        
        setError(ErrorType.SHUTTING_DOWN);        
        
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Extraction request below is shutting down :\n%s\n",
                                   getQueuelogInfoAsXML()));
        }

        aborted();

        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Extraction request above is shutdown.",
                                   getQueuelogInfoAsXML()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("shutdown() - end");
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
        if (priority > objPriority) {
            return 1;
        }
        if (priority < objPriority) {
            return -1;
        }
        return 0;
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
     * Gets the extraction parameters.
     * 
     * @return the extraction parameters
     */
    public ExtractionParameters getExtractionParameters() {
        return extractionParameters;
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
     * Gets the priority.
     * 
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Gets the product.
     * 
     * @return the product
     */
    public Product getProduct() {
        return product;
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
     * Gets the queuelog info as XML.
     * 
     * @return the queuelog info as XML
     */
    public String getQueuelogInfoAsXML() {

        return queueLogInfo.toXML();
    }

    /**
     * Gets the range.
     * 
     * @return the range
     */
    public int getRange() {
        return range;
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

    // /**
    // * Sets the range.
    // *
    // * @param range the range
    // */
    // public void setRange(int range) {
    // this.range = range;
    // }

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

        Date timeToCompare = null;

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
        long timeOut = 1000L * 60 * timeOutInMinutes;

        return elapsedTime >= timeOut;

    }

    /**
     * Run.
     */
    public void run() {

        // try {
        // Thread.sleep(60000);
        // } catch (InterruptedException e1) {
        // e1.printStackTrace();
        // }
        if (LOG.isDebugEnabled()) {
            try {
                LOG.debug("RunnableExtraction run() - entering");
                LOG.debug(String.format("RunnableExtraction run() : user id: '%s' - request parameters '%s'", getUserId(), getExtractionParameters()
                        .toString()));
            } catch (Exception e) {
                // Do nothing
            }
        }
        Assertion assertion = extractionParameters.getAssertion();
        if (assertion != null) {
            AssertionHolder.setAssertion(assertion);
        }

        try {
            setStatusInProgress();

            product = organizer.extractData(extractionParameters);

            setStatusDone();

        } catch (Exception e) {
            LOG.error("RunnableExtraction.run()", e);
            MotuException motuException = null;
            try {
                motuException = new MotuException(String
                        .format("An error occurs during extraction (RunnableExtraction.run): user id: '%s' - request parameters '%s'",
                                getUserId(),
                                getExtractionParameters().toString()), e);
            } catch (MotuException e1) {
                // Do Nothing
            }

            setError(motuException);

        } catch (Error e) {
            LOG.error("RunnableExtraction.run()", e);

            MotuException motuException = null;
            try {
                motuException = new MotuException(String
                        .format("An error occurs during extraction (RunnableExtraction.run): user id: '%s' - request paramters '%s'",
                                getUserId(),
                                getExtractionParameters().toString()), e);
            } catch (MotuException e1) {
                // Do Nothing
            }
            setError(motuException);

        } finally {
            if (assertion != null) {
                AssertionHolder.clear();
            }

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("run() - exiting");
        }
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
    }

    /**
     * Sets the error.
     *
     * @param errorType the new error
     */
    public void setError(ErrorType errorType) {
        Organizer.setError(statusModeResponse, errorType);
        queueLogInfo.setQueueLogError(new QueueLogError(statusModeResponse.getCode(), statusModeResponse.getMsg()));

        if (product != null) {
            product.setLastError(statusModeResponse.getMsg());
        } else {
            organizer.setCurrentProductLastError(statusModeResponse.getMsg());
        }
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
     * Sets the in queue.
     */
    public void setInQueue() {
        setInQueueTime();
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
        // queueLogInfo.addPriority(priority, range, date);

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
     * Sets the queue id.
     * 
     * @param queueId the queue id
     */
    public void setQueueId(String queueId) {
        queueLogInfo.setQueueId(queueId);
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
     * Sets the started.
     */
    public void setStarted() {
        setStartTime();
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
     * Init.
     */
    private void init() {

        statusModeResponse = Organizer.createStatusModeResponse();
        setStatusPending();

    }

    /**
     * Sets the status done.
     * 
     * @throws MotuException
     */
    protected void setStatusDone() throws MotuException {

        Organizer.setStatusDone(statusModeResponse, product);

    }

    /**
     * Sets the status in progress.
     * 
     * @throws MotuException
     */
    protected void setStatusInProgress() {
        statusModeResponse.setStatus(StatusModeType.INPROGRESS);
        statusModeResponse.setMsg(StatusModeType.INPROGRESS.toString());
        statusModeResponse.setCode(ErrorType.OK);

    }

    /**
     * Sets the status pending.
     * 
     * @throws MotuException
     */
    protected void setStatusPending() {
        statusModeResponse.setStatus(StatusModeType.PENDING);
        statusModeResponse.setMsg(StatusModeType.PENDING.toString());
        statusModeResponse.setCode(ErrorType.OK);

    }

    /**
     * Sets the status error.
     * 
     * @param msg the status error
     * 
     */
    protected void setStatusError(String msg) {
        statusModeResponse.setStatus(StatusModeType.ERROR);
        statusModeResponse.setMsg(msg);
        statusModeResponse.setCode(ErrorType.OK);

    }

}
