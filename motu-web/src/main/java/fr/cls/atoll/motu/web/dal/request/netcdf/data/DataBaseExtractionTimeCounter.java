package fr.cls.atoll.motu.web.dal.request.netcdf.data;

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
public class DataBaseExtractionTimeCounter {

    /** The reading time in nanoSeconds (ns). */
    protected long readingTime = 0L;

    /** The writing time in nanoSeconds (ns). */
    protected long writingTime = 0L;

    /** The copying time in nanoSeconds (ns). */
    protected long copyingTime = 0L;

    /** The compressing time in nanoSeconds (ns). */
    protected long compressingTime = 0L;

    /**
     * Gets the reading time.
     * 
     * @return the reading time in nanoSeconds (ns)
     */
    public long getReadingTime() {
        return this.readingTime;
    }

    /**
     * Sets the reading time.
     *
     * @param readingTime the new reading time in nanoSeconds (ns)
     */
    public void setReadingTime(long readingTime) {
        this.readingTime = readingTime;
    }

    /**
     * Adds the reading time.
     *
     * @param readingTime the reading time in nanoSeconds (ns)
     */
    public void addReadingTime(long readingTime) {
        this.readingTime += readingTime;
    }

    /**
     * Gets the writing time.
     *
     * @return the writing time in nanoSeconds (ns)
     */
    public long getWritingTime() {
        return writingTime;
    }

    /**
     * Adds the writing time.
     *
     * @param writingTime the writing time in nanoSeconds (ns)
     */
    public void addWritingTime(long writingTime) {
        this.writingTime += writingTime;
    }

    /**
     * Gets the copying time.
     *
     * @return the copying time in nanoSeconds (ns)
     */
    public long getCopyingTime() {
        return copyingTime;
    }

    /**
     * Sets the copying time.
     *
     * @param copyingTime the new copying time in nanoSeconds (ns)
     */
    public void setCopyingTime(long copyingTime) {
        this.copyingTime = copyingTime;
    }

    /**
     * Adds the copying time.
     *
     * @param copyingTime the copying time in nanoSeconds (ns)
     */
    public void addCopyingTime(long copyingTime) {
        this.copyingTime += copyingTime;
    }

    /**
     * Gets the compressing time.
     *
     * @return the compressing time in nanoSeconds (ns)
     */
    public long getCompressingTime() {
        return compressingTime;
    }

    /**
     * Sets the compressing time.
     *
     * @param compressingTime the new compressing time in nanoSeconds (ns)
     */
    public void setCompressingTime(long compressingTime) {
        this.compressingTime = compressingTime;
    }

    /**
     * Adds the compressing time.
     * 
     * @param compressingTime the compressing time in nanoSeconds (ns)
     */
    public void addCompressingTime(long compressingTime) {
        this.compressingTime += compressingTime;
    }

}
