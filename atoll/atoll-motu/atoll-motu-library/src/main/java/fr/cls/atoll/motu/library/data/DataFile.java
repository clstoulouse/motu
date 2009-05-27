package fr.cls.atoll.motu.library.data;

import org.joda.time.DateTime;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-05-27 16:02:50 $
 */
public class DataFile {

    /**
     * Instantiates a new data file.
     */
    public DataFile() {
    }
    
    
    /** The name. */
    String name;
    
    /** The start coverage date. */
    DateTime startCoverageDate;
    
    /** The end coverage date. */
    DateTime endCoverageDate;    

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



}
