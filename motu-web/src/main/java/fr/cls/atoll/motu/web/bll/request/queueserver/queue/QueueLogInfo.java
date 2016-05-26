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
package fr.cls.atoll.motu.web.bll.request.queueserver.queue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import fr.cls.atoll.motu.library.misc.queueserver.QueueLogError;
import fr.cls.atoll.motu.library.misc.queueserver.QueueLogPriority;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;

// TODO: Auto-generated Javadoc
/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites).
 *
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class QueueLogInfo {
    /**
     * Logger for this class
     */
    private static final Logger LOG = LogManager.getLogger();

    /** CSV separator */
    private static final String CSV_SEPARATOR = ";";

    /** Messages constants */
    private static final String OK_STRING = "OK";
    private static final String ERR_STRING = "ERR";

    /** Types of format */
    public static final String TYPE_XML = "xml";
    public static final String TYPE_CSV = "csv";

    /** Date format for log txt files */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    /** Log format (configuration) */
    private String logFormat = "";

    /** The queue id. */
    private String queueId = "";

    /** The queue desc. */
    private String queueDesc = "";

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
    private double amountDataSize = 0d;

    /** The total io time. */
    private long totalIOTime = 0L;

    /** The preparing time. */
    private long preparingTime = 0L;

    /** The reading time in milliseconds (ms). */
    private long readingTime = 0L;

    /** The writing time in milliseconds (ms). */
    private long writingTime = 0L;

    /** The copying time in milliseconds (ms). */
    protected long copyingTime = 0L;

    /** The compressing time in milliseconds (ms). */
    protected long compressingTime = 0L;

    /** The queue log error. */
    private QueueLogError queueLogError = null;

    /** The extraction parameters. */
    private ExtractionParameters extractionParameters = null;

    /** The priority info. */
    private final List<QueueLogPriority> priorities = new ArrayList<QueueLogPriority>();

    /** The download url path. */
    private String downloadUrlPath = "";

    /** The extract location data. */
    private String extractLocationData = "";

    /** The x stream. */
    private final XStream xStream = new XStream();

    /** The output stream. */
    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    /** The writer. */
    private Writer writer = null;
    /** The encoding. */
    private String encoding = "UTF-8";

    /**
     * Sets the encoding.
     * 
     * @param encoding the encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the log format.
     * 
     * @param encoding the log format
     */
    public void setLogFormat(String logF) {
        this.logFormat = logF;
    }

    /**
     * Gets the encoding.
     * 
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Gets the log Format.
     * 
     * @return the log format
     */
    public String getLogFormat() {
        return logFormat;
    }

    /**
     * Constructor.
     */
    public QueueLogInfo() {
        try {
            writer = new OutputStreamWriter(outputStream, encoding);
        } catch (UnsupportedEncodingException e) {
            // Do Nothing
        }

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

        // xStream.useAttributeFor("priority", int.class);
        // xStream.useAttributeFor("range", int.class);

        xStream.useAttributeFor(QueueLogPriority.class, "priority");
        xStream.useAttributeFor(QueueLogPriority.class, "range");

        xStream.useAttributeFor(ExtractionParameters.class, "userId");
        // xStream.useAttributeFor(ExtractionParameters.class, "user");
        // xStream.aliasAttribute("userId", "user");
        // xStream.registerConverter(new ExtractionParameters.UserBaseConverter());

        xStream.useAttributeFor(ExtractionParameters.class, "userHost");
        xStream.useAttributeFor(ExtractionParameters.class, "locationData");
        xStream.useAttributeFor(ExtractionParameters.class, "productId");
        xStream.useAttributeFor(ExtractionParameters.class, "serviceName");
        xStream.useAttributeFor(ExtractionParameters.class, "temporalCoverageInDays");

        xStream.omitField(ExtractionParameters.class, "dataOutputFormat");
        xStream.omitField(ExtractionParameters.class, "out");
        xStream.omitField(ExtractionParameters.class, "assertion");

        xStream.omitField(this.getClass(), "outputStream");
        xStream.omitField(this.getClass(), "writer");
        xStream.omitField(this.getClass(), "encoding");

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

        // WARNING : reset the output stream before serialization
        // otherwise the content will be duplicate if multiple calls to 'toXML' are done.
        outputStream.reset();
        // return xStream.toXML(this);
        xStream.toXML(this, writer);
        try {
            return outputStream.toString(encoding);
        } catch (UnsupportedEncodingException e) {
            LOG.error("toXML()", e);
            e.printStackTrace(System.err);
        }
        return "";
    }

    /**
     * To CSV.
     * 
     * @return the string
     */
    public String toCSV() {

        // WARNING : reset the output stream before serialization
        // otherwise the content will be duplicate if multiple calls to 'toCSV' are done.
        outputStream.reset();

        if (queueLogError == null) {
            xStream.alias("motuQueueServerLog", this.getClass());
        } else {
            xStream.alias("motuQueueServerLogError", this.getClass());
            xStream.useAttributeFor(QueueLogError.class, "errorCode");
            xStream.useAttributeFor(QueueLogError.class, "message");
        }

        try {
            // Success or Error (specific code)
            if (queueLogError == null) {
                outputStream.write(OK_STRING.concat(CSV_SEPARATOR).getBytes(encoding));
            } else {
                outputStream.write(ERR_STRING.concat(CSV_SEPARATOR).getBytes(encoding));
                outputStream.write(queueLogError.getErrorCode().concat(CSV_SEPARATOR).getBytes(encoding));
                outputStream.write(queueLogError.getMessage().concat(CSV_SEPARATOR).getBytes(encoding));
                outputStream.write(DATE_FORMAT.format(queueLogError.getDateError()).concat(CSV_SEPARATOR).getBytes(encoding));
            }

            // Queue information
            outputStream.write(queueId.concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(queueDesc.concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(Long.toString(requestId).concat(CSV_SEPARATOR).getBytes(encoding));

            // Time information (Long)
            outputStream.write(Long.toString(elapsedWaitQueueTime).concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(Long.toString(elapsedRunTime).concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(Long.toString(elapsedTotalTime).concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(Long.toString(totalIOTime).concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(Long.toString(preparingTime).concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(Long.toString(readingTime).concat(CSV_SEPARATOR).getBytes(encoding));

            // Time information (Date)
            if (inQueueTime != null)
                outputStream.write(DATE_FORMAT.format(inQueueTime).concat(CSV_SEPARATOR).getBytes(encoding));
            if (startTime != null)
                outputStream.write(DATE_FORMAT.format(startTime).concat(CSV_SEPARATOR).getBytes(encoding));
            if (endTime != null)
                outputStream.write(DATE_FORMAT.format(endTime).concat(CSV_SEPARATOR).getBytes(encoding));

            // Size of request (bytes)
            outputStream.write(Double.toString(amountDataSize).concat(CSV_SEPARATOR).getBytes(encoding));

            // Urls and location
            outputStream.write(downloadUrlPath.concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(extractLocationData.concat(CSV_SEPARATOR).getBytes(encoding));

            // Extraction parameters
            outputStream.write(extractionParameters.getServiceName().concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(Integer.toString(extractionParameters.getTemporalCoverageInDays()).concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(extractionParameters.getProductId().concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(extractionParameters.getUserId().concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(extractionParameters.getUserHost().concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(Boolean.toString(extractionParameters.isAnonymousUser()).concat(CSV_SEPARATOR).getBytes(encoding));
            outputStream.write(Boolean.toString(extractionParameters.isBatchQueue()).concat(CSV_SEPARATOR).getBytes(encoding));

            // List of variables
            for (String v : extractionParameters.getListVar()) {
                outputStream.write(v.concat(CSV_SEPARATOR).getBytes(encoding));
            }
            outputStream.write((CSV_SEPARATOR).getBytes(encoding));

            // Temporal coverage
            for (String t : extractionParameters.getListTemporalCoverage()) {
                outputStream.write(t.concat(CSV_SEPARATOR).getBytes(encoding));
            }
            outputStream.write((CSV_SEPARATOR).getBytes(encoding));

            // Geographical coverage
            for (String g : extractionParameters.getListLatLonCoverage()) {
                outputStream.write(g.concat(CSV_SEPARATOR).getBytes(encoding));
            }
            outputStream.write((CSV_SEPARATOR).getBytes(encoding));

            // Depth coverage
            for (String d : extractionParameters.getListDepthCoverage()) {
                outputStream.write(d.concat(CSV_SEPARATOR).getBytes(encoding));
            }
            outputStream.write((CSV_SEPARATOR).getBytes(encoding));

            // Priorities
            for (QueueLogPriority p : priorities) {
                outputStream.write(Integer.toString(p.getPriority()).concat(CSV_SEPARATOR).getBytes(encoding));
                outputStream.write(Integer.toString(p.getRange()).concat(CSV_SEPARATOR).getBytes(encoding));
                outputStream.write(DATE_FORMAT.format(p.getDate()).concat(CSV_SEPARATOR).getBytes(encoding));
            }
            outputStream.write((CSV_SEPARATOR).getBytes(encoding));

            // Return CSV line to String
            return outputStream.toString(encoding);

        } catch (UnsupportedEncodingException e) {
            LOG.error("toCSV()", e);
            e.printStackTrace(System.err);
        } catch (IOException e) {
            LOG.error("toCSV()", e);
            e.printStackTrace(System.err);
        }

        // Empty return
        return "";
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
        this.elapsedRunTime = (this.endTime.getTime() - this.startTime.getTime()) + this.preparingTime;
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
        this.elapsedTotalTime = (this.endTime.getTime() - this.inQueueTime.getTime()) + this.preparingTime;
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
        if (elapsedWaitQueueTime < 0) {
            elapsedWaitQueueTime = 0L;
        }
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
        computeTotalIO();
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
     * Compute total io.
     */
    public void computeTotalIO() {
        this.totalIOTime = this.readingTime + this.writingTime + this.copyingTime + this.compressingTime;
    }

    /**
     * Gets the preparing time.
     *
     * @return the preparing time in ms
     */
    public long getPreparingTime() {
        return preparingTime;
    }

    /**
     * Sets the preparing time.
     *
     * @param preparingTime the new preparing time in ms
     */
    public void setPreparingTime(long preparingTime) {
        this.preparingTime = preparingTime;
    }

    /**
     * Adds the preparing time.
     *
     * @param preparingTime the preparing time in ms
     */
    public void addPreparingTime(long preparingTime) {
        this.preparingTime += preparingTime;
    }

    /**
     * Gets the reading time.
     *
     * @return the reading time in ms
     */
    public double getReadingTime() {
        return this.readingTime;
    }

    /**
     * Sets the reading time.
     *
     * @param readingTime the new reading time in ms.
     */
    public void setReadingTime(long readingTime) {
        this.readingTime = readingTime;
    }

    /**
     * Adds the reading time.
     *
     * @param readingTime the reading time in ms.
     */
    public void addReadingTime(long readingTime) {
        this.readingTime += readingTime;
    }

    /**
     * Gets the writing time.
     *
     * @return the writing time in ms
     */
    public long getWritingTime() {
        return writingTime;
    }

    /**
     * Sets the writing time.
     *
     * @param writingTime the new writing time in ms.
     */
    public void setWritingTime(long writingTime) {
        this.writingTime = writingTime;
    }

    /**
     * Adds the writing time.
     *
     * @param writingTime the writing time in ms.
     */
    public void addWritingTime(long writingTime) {
        this.writingTime += writingTime;
    }

    /**
     * Gets the copying time.
     *
     * @return the copying time in ms
     */
    public long getCopyingTime() {
        return copyingTime;
    }

    /**
     * Sets the copying time.
     *
     * @param copyingTime the new copying time in ms
     */
    public void setCopyingTime(long copyingTime) {
        this.copyingTime = copyingTime;
    }

    /**
     * Adds the copying time.
     *
     * @param copyingTime the copying time in ms
     */
    public void addCopyingTime(long copyingTime) {
        this.copyingTime += copyingTime;
    }

    /**
     * Gets the compressing time.
     *
     * @return the compressing time in ms
     */
    public long getCompressingTime() {
        return compressingTime;
    }

    /**
     * Sets the compressing time.
     *
     * @param compressingTime the new compressing time in ms
     */
    public void setCompressingTime(long compressingTime) {
        this.compressingTime = compressingTime;
    }

    /**
     * Adds the compressing time.
     *
     * @param compressingTime the compressing time in ms
     */
    public void addCompressingTime(long compressingTime) {
        this.compressingTime = compressingTime;
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
     * @param priority the priority
     * @param range the range
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
    public QueueLogPriority getMostRecentPriority() {
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
