package fr.cls.atoll.motu.queueserver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.thoughtworks.xstream.XStream;

import fr.cls.atoll.motu.intfce.ExtractionParameters;

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
public class QueueLogInfo {

    /** The queue id. */
    private String queueId = null;

    /** The queue desc. */
    private String queueDesc = null;

    /** The request id. */
    private long requestId = -1;
    
    /** The elapse wait queue time. */
    private long elapsedWaitQueueTime = 0L;

    /** The elapse run time. */
    private long elapsedRunTime = 0L;

    /** The elapse total time. */
    private long elapsedTotalTime = 0L;

    /** The in queue time. */
    private Date inQueueTime = null;

    /** The start time. */
    private Date startTime = null;
    // private Date outQueueTime = null;
    /** The end time. */
    private Date endTime = null;

    /** The amount data size. */
    private double amountDataSize = 0f;

    /** The queue log error. */
    private QueueLogError queueLogError = null;

    /** The extraction parameters. */
    private ExtractionParameters extractionParameters = null;

    /** The priority info. */
    private List<QueueLogPriority> priorities = new ArrayList<QueueLogPriority>();

    /** The download url path. */
    private String downloadUrlPath = null;

    /** The extract location data. */
    private String extractLocationData = null;

    /** The x stream. */
    private XStream xStream = new XStream();

    /**
     * Constructor.
     */
    public QueueLogInfo() {
        //Field[] fields = this.getClass().getDeclaredFields();
        // for (int i = 0 ; i < fields.length ; i++) {
        // xStream.useAttributeFor(this.getClass(), fields[i].getName());
        // }
        
        initXStreamOptions();

    }

    /**
     * Inits the X stream options.
     */
    public void initXStreamOptions() {
        
        xStream.alias("priority", QueueLogPriority.class);

        xStream.useAttributeFor(Date.class);
        xStream.useAttributeFor(long.class);
        xStream.useAttributeFor(boolean.class);
        xStream.useAttributeFor(double.class);
        xStream.useAttributeFor(this.getClass(), "queueId");
        xStream.useAttributeFor(this.getClass(), "queueDesc");
        xStream.useAttributeFor(this.getClass(), "extractLocationData");
        xStream.useAttributeFor(this.getClass(), "downloadUrlPath");

//        xStream.useAttributeFor("priority", int.class);
//        xStream.useAttributeFor("range", int.class);
        
        xStream.useAttributeFor(QueueLogPriority.class, "priority");
        xStream.useAttributeFor(QueueLogPriority.class, "range");
        
        xStream.useAttributeFor(ExtractionParameters.class, "userId");
        xStream.useAttributeFor(ExtractionParameters.class, "userHost");
        xStream.useAttributeFor(ExtractionParameters.class, "locationData");
        xStream.useAttributeFor(ExtractionParameters.class, "productId");
        xStream.useAttributeFor(ExtractionParameters.class, "serviceName");
        xStream.useAttributeFor(ExtractionParameters.class, "temporalCoverageInDays");

        xStream.omitField(ExtractionParameters.class, "dataOutputFormat");
        xStream.omitField(ExtractionParameters.class, "out");
        
        xStream.omitField(this.getClass(), "xStream");


    }
    
    /**
     * To XML.
     * 
     * @return the string
     */
    public String toXML() {

        if (queueLogError == null) {
            xStream.alias("motuQueueServerLog", this.getClass());
        } else {
            xStream.alias("motuQueueServerLogError", this.getClass());
            xStream.useAttributeFor(QueueLogError.class, "errorCode");
            xStream.useAttributeFor(QueueLogError.class, "message");
        }

        return xStream.toXML(this);

    }

    /**
     * Gets the queue log error.
     * 
     * @return the queue log error
     */
    public QueueLogError getQueueLogError() {
        return queueLogError;
        
    }

    /**
     * Sets the queue log error.
     * 
     * @param queueLogError the queue log error
     */
    public void setQueueLogError(QueueLogError queueLogError) {
        this.queueLogError = queueLogError;
    }

    /**
     * Gets the elapsed run time.
     * 
     * @return the elapsed run time
     */
    public long getElapsedRunTime() {
        setElapsedRunTime();
        return elapsedRunTime;
    }

    // /**
    // * Sets the elapse run time.
    // *
    // * @param elapsedRunTime the elapse run time
    // */
    // public void setElapseRunTime(long elapsedRunTime) {
    // this.elapseRunTime = elapsedRunTime;
    // }

    /**
     * Sets the elapse run time.
     */
    private void setElapsedRunTime() {
        if ((endTime == null) || (startTime == null)) {
            return;
        }
        this.elapsedRunTime = this.endTime.getTime() - this.startTime.getTime();
    }

    /**
     * Gets the elapsed total time.
     * 
     * @return the elapsed total time
     */
    public long getElapsedTotalTime() {
        setElapsedTotalTime();
        return elapsedTotalTime;
    }

    // /**
    // * Sets the elapse total time.
    // *
    // * @param elapsedTotalTime the elapse total time
    // */
    // public void setElapseTotalTime(long elapsedTotalTime) {
    // setElapseTotalTime();
    // this.elapseTotalTime = elapsedTotalTime;
    // }

    /**
     * Sets the elapsed total time.
     */
    private void setElapsedTotalTime() {
        if ((endTime == null) || (inQueueTime == null)) {
            return;
        }
        this.elapsedTotalTime = this.endTime.getTime() - this.inQueueTime.getTime();
    }

    /**
     * Gets the elapsed wait queue time.
     * 
     * @return the elapsed wait queue time
     */
    public long getElapsedWaitQueueTime() {
        setElapsedWaitQueueTime();
        return elapsedWaitQueueTime;
    }

    // /**
    // * Sets the elapse wait queue time.
    // *
    // * @param elapsedWaitQueueTime the elapse wait queue time
    // */
    // public void setElapseWaitQueueTime(long elapsedWaitQueueTime) {
    // this.elapseWaitQueueTime = elapsedWaitQueueTime;
    // }

    /**
     * Sets the elapsed wait queue time.
     */
    private void setElapsedWaitQueueTime() {
        if ((startTime == null) || (inQueueTime == null)) {
            return;
        }
        this.elapsedWaitQueueTime = this.startTime.getTime() - this.inQueueTime.getTime();
    }

    /**
     * Gets the end time.
     * 
     * @return the end time
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time.
     * 
     * @param endTime the end time
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        setElapsedTotalTime();
        setElapsedRunTime();
    }

    /**
     * Gets the in queue time.
     * 
     * @return the in queue time
     */
    public Date getInQueueTime() {
        return inQueueTime;
    }

    /**
     * Sets the in queue time.
     * 
     * @param inQueueTime the in queue time
     */
    public void setInQueueTime(Date inQueueTime) {
        this.inQueueTime = inQueueTime;
        setElapsedTotalTime();
        setElapsedWaitQueueTime();
        setElapsedRunTime();
    }

    // public Date getOutQueueTime() {
    // return outQueueTime;
    // }
    //
    // public void setOutQueueTime(Date outQueueTime) {
    // this.outQueueTime = outQueueTime;
    // setElapseTotalTime();
    // }

    /**
     * Gets the start time.
     * 
     * @return the start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     * 
     * @param startTime the start time
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        setElapsedWaitQueueTime();
        setElapsedRunTime();
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
     * Gets the amount data size.
     * 
     * @return the amount data size
     */
    public double getAmountDataSize() {
        return amountDataSize;
    }

    /**
     * Sets the amount data size.
     * 
     * @param amountDataSize the amount data size
     */
    public void setAmountDataSize(double amountDataSize) {
        this.amountDataSize = amountDataSize;
    }

    /**
     * Gets the priorities.
     * 
     * @return the priorities
     */
    public List<QueueLogPriority> getPriorities() {
        return priorities;
    }

    /**
     * Adds the priority.
     * 
     * @param range the range
     * @param priority the priority
     * @param date the date
     */
    public void addPriority(int priority, int range, Date date) {

        if (date == null) {
            Calendar cal = Calendar.getInstance();
            date = cal.getTime();
        }
        QueueLogPriority queueLogPriority = new QueueLogPriority(priority, range, date);
        this.priorities.add(queueLogPriority);
    }

    /**
     * Gets the most recent priority.
     * 
     * @return the most recent priority
     */
    public QueueLogPriority getMostRecentPriority () {
        if (priorities == null) {
            return null;
        }
        if (priorities.isEmpty()) {
            return null;
        }
        
        return priorities.get(priorities.size() - 1);
    }
    // /**
    // * Gets the priorities time.
    // *
    // * @return the priorities time
    // */
    // public List<Date> getPrioritiesTime() {
    // return prioritiesTime;
    // }

    /**
     * Gets the queue desc.
     * 
     * @return the queue desc
     */
    public String getQueueDesc() {
        return queueDesc;
    }

    /**
     * Sets the queue desc.
     * 
     * @param queueDesc the queue desc
     */
    public void setQueueDesc(String queueDesc) {
        this.queueDesc = queueDesc;
    }

    /**
     * Gets the queue id.
     * 
     * @return the queue id
     */
    public String getQueueId() {
        return queueId;
    }

    /**
     * Sets the queue id.
     * 
     * @param queueId the queue id
     */
    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    /**
     * Gets the download url path.
     * 
     * @return the download url path
     */
    public String getDownloadUrlPath() {
        return downloadUrlPath;
    }

    /**
     * Sets the download url path.
     * 
     * @param downloadUrlPath the download url path
     */
    public void setDownloadUrlPath(String downloadUrlPath) {
        this.downloadUrlPath = downloadUrlPath;
    }

    /**
     * Gets the extract location data.
     * 
     * @return the extract location data
     */
    public String getExtractLocationData() {
        return extractLocationData;
    }

    /**
     * Sets the extract location data.
     * 
     * @param extractLocationData the extract location data
     */
    public void setExtractLocationData(String extractLocationData) {
        this.extractLocationData = extractLocationData;
    }

    /**
     * Gets the request id.
     * 
     * @return the request id
     */
    public long getRequestId() {
        return requestId;
    }

    /**
     * Sets the request id.
     * 
     * @param requestId the request id
     */
    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    // long startTime = System.currentTimeMillis();
    // ....
    //
    // long currentTime = System.currentTimeMillis();
    // SimpleDateFormat dateFormat =
    // new SimpleDateFormat("HH:mm:ss");
    //
    // dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    // elapsed = currentTime - startTime;
    //
    // System.out.println(dateFormat.format(new Date(elapsed)));
}
