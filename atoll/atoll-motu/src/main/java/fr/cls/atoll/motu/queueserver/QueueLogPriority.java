package fr.cls.atoll.motu.queueserver;

import java.util.Date;

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
public class QueueLogPriority {

    /**
     * The Constructor.
     * 
     * @param range the range
     * @param priority the priority
     * @param date the date
     */
    public QueueLogPriority(int priority, int range, Date date) {
        this.priority = priority;
        this.range = range;
        this.date = date;
    }

    /** The priority. */
    private int priority;

    /** The range. */
    private int range;

    /** The date. */
    private Date date;

    /**
     * Gets the date.
     * 
     * @return the date
     */
    public Date getDate() {
        return date;
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
     * Gets the range.
     * 
     * @return the range
     */
    public int getRange() {
        return range;
    }
}
