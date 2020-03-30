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
package fr.cls.atoll.motu.web.dal.request.netcdf.data;

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
    long weight;

    /**
     * Gets the weight.
     * 
     * @return the weight
     */
    public long getWeight() {
        return weight;
    }

    /**
     * Sets the weight.
     * 
     * @param weight the new weight
     */
    public void setWeight(long weight) {
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
