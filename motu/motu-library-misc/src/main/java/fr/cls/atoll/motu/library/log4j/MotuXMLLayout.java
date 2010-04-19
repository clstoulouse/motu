package fr.cls.atoll.motu.library.log4j;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.xml.XMLLayout;

import fr.cls.atoll.motu.library.queueserver.QueueLogInfo;
import fr.cls.atoll.motu.library.queueserver.RunnableExtraction;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */
public class MotuXMLLayout extends XMLLayout {

    // public final static String DEFAULT_XML_ENCODING = "UTF-8";
    //
    // public final static String DEFAULT_XML_VERSION = "1.0";
    // public final static String DEFAULT_XML_ROOT_TAG = "motu";
    private static final int DEFAULT_SIZE = 256;
    private static final int UPPER_LIMIT = 2048;

    private StringBuffer buf = new StringBuffer(DEFAULT_SIZE);

    /**
     * Constructor.
     */
    public MotuXMLLayout() {
        // this(DEFAULT_XML_VERSION, DEFAULT_XML_ENCODING, DEFAULT_XML_ROOT_TAG);
    }

    /**
     * The Constructor.
     * 
     * @param encoding the encoding
     * @param rootTagName the root tag name
     * @param version the version
     */
    public MotuXMLLayout(String version, String encoding, String rootTagName) {
        // setVersion(version);
        // setEncoding(encoding);
        // setRootTagName(rootTagName);
    }

    /** The encoding. */
    private String encoding;

    /**
     * Sets the encoding.
     * 
     * @param encoding the encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets the encoding.
     * 
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /** The version. */
    private String version;

    /**
     * Sets the version.
     * 
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the version.
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /** The root tag name. */
    private String rootTagName;

    /**
     * Gets the root tag name.
     * 
     * @return the root tag name
     */
    public String getRootTagName() {
        return rootTagName;
    }

    /**
     * Sets the root tag name.
     * 
     * @param rootTagName the root tag name
     */
    public void setRootTagName(String rootTagName) {
        this.rootTagName = rootTagName;
    }

    /**
     * {@inheritDoc}.
     * 
     * @param event the event
     * 
     * @return the string
     */
    @Override
    public String format(LoggingEvent event) {
        // Reset working buffer. If the buffer is too large, then we need a new
        // one in order to avoid the penalty of creating a large array.
        if (buf.capacity() > UPPER_LIMIT) {
            buf = new StringBuffer(DEFAULT_SIZE);
        } else {
            buf.setLength(0);
        }
        if (event.getMessage() == null) {
            return "MotuXMLLayout : message object is null";
        }

        if (event.getMessage() instanceof RunnableExtraction) {
            RunnableExtraction runnableExtraction = (RunnableExtraction) event.getMessage();
            buf.append(runnableExtraction.getQueuelogInfoAsXML());
            buf.append(Layout.LINE_SEP);

            return buf.toString();
        }

        if (event.getMessage() instanceof QueueLogInfo) {
            QueueLogInfo queueLogInfo = (QueueLogInfo) event.getMessage();
            buf.append(queueLogInfo.toXML());
            buf.append(Layout.LINE_SEP);

            return buf.toString();
        }

        return super.format(event);

    }

    // /** {@inheritDoc} */
    // @Override
    // public String getFooter() {
    // StringBuffer sbuf = new StringBuffer();
    // sbuf.append("</");
    // sbuf.append(rootTagName);
    // sbuf.append(">");
    // sbuf.append(Layout.LINE_SEP);
    //
    // return sbuf.toString();
    //
    // }

    // /** {@inheritDoc} */
    // @Override
    // public String getHeader() {
    //        
    // StringBuffer sbuf = new StringBuffer();
    // sbuf.append("<?xml version=\"");
    // sbuf.append(version);
    // sbuf.append("\" encoding=\"");
    // sbuf.append(encoding);
    // sbuf.append("\"?>");
    // sbuf.append(Layout.LINE_SEP);
    // sbuf.append("<");
    // sbuf.append(rootTagName);
    // sbuf.append(">");
    // sbuf.append(Layout.LINE_SEP);
    //
    // }

}
