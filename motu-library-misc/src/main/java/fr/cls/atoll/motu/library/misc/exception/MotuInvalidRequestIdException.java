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
public class MotuInvalidRequestIdException extends MotuExceptionBase {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(MotuInvalidRequestIdException.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The Constructor.
     */
    public MotuInvalidRequestIdException() {
        this(-1);
    }

    /**
     * The Constructor.
     * 
     * @param id the id
     */
    public MotuInvalidRequestIdException(long id) {
        super("Invalid request Id.");
        this.id = id;
        notifyLogException();
    }

    /** The id. */
    final private long id;

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public long getId() {
        return this.id;
    }

    /**
     * Gets the id as string.
     * 
     * @return the id as string
     */
    public String getIdAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Long.toString(id));
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

        stringBuffer.append("The request id '.");
        stringBuffer.append(getIdAsString());
        stringBuffer.append(" ' (or null if negative value) is not valid or unknown.");
        return stringBuffer.toString();
    }
}
