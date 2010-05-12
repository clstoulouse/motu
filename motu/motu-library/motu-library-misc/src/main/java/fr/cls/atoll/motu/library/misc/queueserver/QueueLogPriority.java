package fr.cls.atoll.motu.library.misc.queueserver;

import java.util.Date;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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
    private final int priority;

    /** The range. */
    private final int range;

    /** The date. */
    private final Date date;

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
