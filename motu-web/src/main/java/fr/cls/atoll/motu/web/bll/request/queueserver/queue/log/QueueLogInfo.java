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
package fr.cls.atoll.motu.web.bll.request.queueserver.queue.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.common.utils.TimeUtils;

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
    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");

    /** The queue id. */
    private String queueId;

    /** The queue desc. */
    private String queueDesc;

    /** The request id. */
    private String requestId;

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

    /** The end time. */
    private Date endTime = null;

    /** The amount data size in MegaBytes */
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

    /** The version of the script if provided */
    protected String scriptVersion;

    /** The queue log error. */
    private QueueLogError queueLogError = null;

    /** The extraction parameters. */
    private ExtractionParameters extractionParameters;

    /** The download url path. */
    private String downloadUrlPath;

    /** The extract location data. */
    private String extractLocationData;

    /** The x stream. */
    private final XStream xStream = new XStream();

    /** The output stream. */
    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    /** The writer. */
    private Writer writer = null;
    /** The encoding. */
    private static final String ENCODING = StandardCharsets.UTF_8.name();

    /**
     * Constructor.
     * 
     * @param rds
     */
    public QueueLogInfo(RequestDownloadStatus rds) {
        try {
            writer = new OutputStreamWriter(outputStream, ENCODING);
        } catch (UnsupportedEncodingException e) {
            // Do Nothing
        }

        MotuException me = rds.getRunningException();
        if (me != null) {
            setQueueLogError(new QueueLogError(me.getErrorType(), me.getMessage()));
        }

        queueId = rds.getQueueId();
        queueDesc = rds.getQueueDescription();
        requestId = rds.getRequestId();
        amountDataSize = Double.parseDouble(rds.getSize());

        extractionParameters = rds.getRequestProduct().getExtractionParameters();
        downloadUrlPath = BLLManager.getInstance().getCatalogManager().getProductManager()
                .getProductDownloadHttpUrl(rds.getRequestProduct().getRequestProductParameters().getExtractFilename());
        extractLocationData = rds.getRequestProduct().getRequestProductParameters().getExtractLocationData();
        scriptVersion = rds.getRequestProduct().getExtractionParameters().getScriptVersion();

        initTimes(rds);
        initXStreamOptions();
    }

    private void initTimes(RequestDownloadStatus rds) {
        Calendar c = Calendar.getInstance();

        c.setTimeInMillis(rds.getCreationDateTime());
        inQueueTime = c.getTime();

        if (rds.getStartProcessingDateTime() > 0) {
            c.setTimeInMillis(rds.getStartProcessingDateTime());
            startTime = c.getTime();
        }

        if (rds.getEndProcessingDateTime() > 0) {
            c.setTimeInMillis(rds.getEndProcessingDateTime());
            endTime = c.getTime();
        }

        readingTime = rds.getDataBaseExtractionTimeCounter().getReadingTime();
        preparingTime = readingTime;
        writingTime = rds.getDataBaseExtractionTimeCounter().getWritingTime();
        copyingTime = rds.getDataBaseExtractionTimeCounter().getCopyingTime();
        compressingTime = rds.getDataBaseExtractionTimeCounter().getCompressingTime();

        totalIOTime = computeTotalIOTime();
        elapsedRunTime = computeElapsedRunTime();
        elapsedTotalTime = computeElapsedTotalTime();
        elapsedWaitQueueTime = computeElapsedWaitQueueTime();
    }

    /**
     * Inits the X stream options.
     */
    public void initXStreamOptions() {

        // xStream.alias("priority", QueueLogPriority.class);
        xStream.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss.S", new String[] { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss" }));
        xStream.useAttributeFor(Date.class);
        xStream.useAttributeFor(long.class);
        xStream.useAttributeFor(boolean.class);
        xStream.useAttributeFor(double.class);
        xStream.useAttributeFor(this.getClass(), "queueId");
        xStream.useAttributeFor(this.getClass(), "queueDesc");
        xStream.useAttributeFor(this.getClass(), "requestId");
        xStream.useAttributeFor(this.getClass(), "extractLocationData");
        xStream.useAttributeFor(this.getClass(), "downloadUrlPath");
        xStream.useAttributeFor(this.getClass(), "scriptVersion");

        xStream.useAttributeFor(ExtractionParameters.class, "userId");

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
        xStream.omitField(this.getClass(), "dateFormat");

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
        xStream.toXML(this, writer);
        try {
            return outputStream.toString(ENCODING);
        } catch (UnsupportedEncodingException e) {
            LOG.error("toXML()", e);
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
                outputStream.write(OK_STRING.concat(CSV_SEPARATOR).getBytes(ENCODING));
            } else {
                outputStream.write(ERR_STRING.concat(CSV_SEPARATOR).getBytes(ENCODING));
                outputStream.write(queueLogError.getErrorCode().concat(CSV_SEPARATOR).getBytes(ENCODING));
                outputStream.write(queueLogError.getMessage().concat(CSV_SEPARATOR).getBytes(ENCODING));
                outputStream.write(dateFormat.format(queueLogError.getDateError()).concat(CSV_SEPARATOR).getBytes(ENCODING));
            }

            // Queue information
            outputStream.write(queueId.concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(queueDesc.concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(requestId.concat(CSV_SEPARATOR).getBytes(ENCODING));

            // Time information (Long)
            outputStream.write(Long.toString(elapsedWaitQueueTime).concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(Long.toString(elapsedRunTime).concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(Long.toString(elapsedTotalTime).concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(Long.toString(totalIOTime).concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(Long.toString(readingTime).concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(Long.toString(TimeUtils.nanoToMillisec(readingTime)).concat(CSV_SEPARATOR).getBytes(ENCODING));

            // Time information (Date)
            if (inQueueTime != null)
                outputStream.write(dateFormat.format(inQueueTime).concat(CSV_SEPARATOR).getBytes(ENCODING));
            if (startTime != null)
                outputStream.write(dateFormat.format(startTime).concat(CSV_SEPARATOR).getBytes(ENCODING));
            if (endTime != null)
                outputStream.write(dateFormat.format(endTime).concat(CSV_SEPARATOR).getBytes(ENCODING));

            // Size of request (mega bytes)
            outputStream.write(Double.toString(amountDataSize).concat(CSV_SEPARATOR).getBytes(ENCODING));

            // Urls and location
            outputStream.write(downloadUrlPath.concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(extractLocationData.concat(CSV_SEPARATOR).getBytes(ENCODING));

            // Extraction parameters
            outputStream.write(extractionParameters.getServiceName().concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(Integer.toString(extractionParameters.getTemporalCoverageInDays()).concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(extractionParameters.getProductId().concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(extractionParameters.getUserId().concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(extractionParameters.getUserHost().concat(CSV_SEPARATOR).getBytes(ENCODING));
            outputStream.write(Boolean.toString(extractionParameters.isAnonymousUser()).concat(CSV_SEPARATOR).getBytes(ENCODING));

            // List of variables
            for (String v : extractionParameters.getListVar()) {
                outputStream.write(v.concat(CSV_SEPARATOR).getBytes(ENCODING));
            }

            // Temporal coverage
            for (String t : extractionParameters.getListTemporalCoverage()) {
                outputStream.write(t.concat(CSV_SEPARATOR).getBytes(ENCODING));
            }

            // Geographical coverage
            for (String g : extractionParameters.getListLatLonCoverage()) {
                outputStream.write(g.concat(CSV_SEPARATOR).getBytes(ENCODING));
            }

            // Depth coverage
            for (String d : extractionParameters.getListDepthCoverage()) {
                outputStream.write(d.concat(CSV_SEPARATOR).getBytes(ENCODING));
            }

            // ScriptVersion
            outputStream.write(scriptVersion.concat(CSV_SEPARATOR).getBytes(ENCODING));

            // Priorities
            // for (QueueLogPriority p : priorities) {
            // outputStream.write(Integer.toString(p.getPriority()).concat(CSV_SEPARATOR).getBytes(encoding));
            // outputStream.write(Integer.toString(p.getRange()).concat(CSV_SEPARATOR).getBytes(encoding));
            // outputStream.write(DATE_FORMAT.format(p.getDate()).concat(CSV_SEPARATOR).getBytes(encoding));
            // }
            // outputStream.write((CSV_SEPARATOR).getBytes(encoding));

            // Return CSV line to String
            return outputStream.toString(ENCODING);

        } catch (IOException e) {
            LOG.error("toCSV()", e);
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
    private long computeElapsedRunTime() {
        if ((endTime == null) || (startTime == null)) {
            return 0;
        } else {
            return (this.endTime.getTime() - this.startTime.getTime()) + readingTime;
        }
    }

    /**
     * Gets the elapsed total time.
     * 
     * @return the elapsed total time
     */
    private long computeElapsedTotalTime() {
        if ((endTime == null) || (inQueueTime == null)) {
            return 0;
        } else {
            return (this.endTime.getTime() - this.inQueueTime.getTime()) + readingTime;
        }
    }

    /**
     * Gets the elapsed wait queue time.
     * 
     * @return the elapsed wait queue time
     */
    private long computeElapsedWaitQueueTime() {
        if ((startTime == null) || (inQueueTime == null)) {
            return 0;
        } else {
            return Math.max(0, this.startTime.getTime() - this.inQueueTime.getTime());
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
     * Gets the in queue time.
     * 
     * @return the in queue time
     */
    public Date getInQueueTime() {
        return inQueueTime;
    }

    /**
     * Gets the start time.
     * 
     * @return the start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Compute total io.
     * 
     * @return
     */
    private long computeTotalIOTime() {
        return TimeUtils.nanoToMillisec(readingTime + writingTime + copyingTime + compressingTime);
    }

}
