package fr.cls.atoll.motu.library.misc.data;

import org.joda.time.DateTime;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class DataFile {

    /**
     * Instantiates a new data file.
     */
    public DataFile() {
    }

    /** The name. */
    String name;

    /** The path. */
    String path;

    /** The start coverage date. */
    DateTime startCoverageDate;

    /** The end coverage date. */
    DateTime endCoverageDate;

    /** The weight. */
    double weight;

    /**
     * Gets the weight.
     * 
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Sets the weight.
     * 
     * @param weight the new weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the start coverage date.
     * 
     * @return the start coverage date
     */
    public DateTime getStartCoverageDate() {
        return startCoverageDate;
    }

    /**
     * Sets the start coverage date.
     * 
     * @param startCoverageDate the new start coverage date
     */
    public void setStartCoverageDate(DateTime startCoverageDate) {
        this.startCoverageDate = startCoverageDate;
    }

    /**
     * Gets the end coverage date.
     * 
     * @return the end coverage date
     */
    public DateTime getEndCoverageDate() {
        return endCoverageDate;
    }

    /**
     * Sets the end coverage date.
     * 
     * @param endCoverageDate the new end coverage date
     */
    public void setEndCoverageDate(DateTime endCoverageDate) {
        this.endCoverageDate = endCoverageDate;
    }

    /**
     * Gets the path.
     * 
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path.
     * 
     * @param path the new path
     */
    public void setPath(String path) {
        this.path = path;
    }

}
