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
package fr.cls.atoll.motu.library.misc.exception;

import org.apache.log4j.Logger;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuInvalidQueuePriorityException extends MotuExceptionBase {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(MotuInvalidQueuePriorityException.class);

    /** . */
    private static final long serialVersionUID = 1L;

    /**
     * The Constructor.
     * 
     * @param min the min
     * @param max the max
     * @param value the value
     */
    public MotuInvalidQueuePriorityException(int value, int min, int max) {
        super("Invalid queue priority.");
        this.max = max;
        this.min = min;
        this.value = value;
        notifyLogException();
    }

    /** The max. */
    final private int max;

    /** The min. */
    final private int min;

    /** The value. */
    final private int value;

    /**
     * Gets the max.
     * 
     * @return the max
     */
    public int getMax() {
        return this.max;
    }

    /**
     * Gets the min.
     * 
     * @return the min
     */
    public int getMin() {
        return this.min;
    }

    /**
     * Gets the value.
     * 
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the max as string.
     * 
     * @return the max as a string representation
     */
    public String getMinAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Integer.toString(min));
        return stringBuffer.toString();
    }

    /**
     * Gets the max as string.
     * 
     * @return the max as string
     */
    public String getMaxAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Integer.toString(max));
        return stringBuffer.toString();
    }

    /**
     * Gets the value as string.
     * 
     * @return the value as string
     */
    public String getValueAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Double.toString(value));
        return stringBuffer.toString();
    }

    /**
     * writes exception information into the log.
     */
    @Override
    public void notifyLogException() {

        super.notifyLogException();
        LOG.warn(notifyException());
    }

    /**
     * Notify exception.
     * 
     * @return exception information.
     */
    @Override
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(super.notifyException());

        stringBuffer.append("The priotity value ");
        stringBuffer.append(getValueAsString());
        stringBuffer.append(" is not a valid value ");
        stringBuffer.append("(Minimum is ");
        stringBuffer.append(getMinAsString());
        stringBuffer.append("(Maximum is ");
        stringBuffer.append(getMaxAsString());
        return stringBuffer.toString();
    }
}
