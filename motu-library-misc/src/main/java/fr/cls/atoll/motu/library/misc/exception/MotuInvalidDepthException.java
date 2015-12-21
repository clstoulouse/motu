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

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.
/**
 * Depth invalid value exception class.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuInvalidDepthException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuInvalidDepthException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param depth depth representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDepthException(String depth, Throwable cause) {
        super("Invalid depth.", cause);
        this.depthString = depth;
        this.depth = Double.MAX_VALUE;
        notifyLogException();

    }

    /**
     * @param depth depth representation which causes the exception
     */
    public MotuInvalidDepthException(String depth) {
        super("Invalid depth.");
        this.depthString = depth;
        this.depth = Double.MAX_VALUE;
        notifyLogException();
    }

    /**
     * @param depth depth representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDepthException(double depth, Throwable cause) {
        super("Invalid depth.", cause);
        this.depth = depth;
        this.depthString = "?";
        notifyLogException();
    }

    /**
     * @param depth depth representation which causes the exception
     */
    public MotuInvalidDepthException(double depth) {
        super("Invalid depth.");
        this.depth = depth;
        this.depthString = "?";
        notifyLogException();
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
     * @return exception information.
     */
    @Override
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(super.notifyException());
        stringBuffer.append("\nDepth:");
        if (depth != Double.MAX_VALUE) {
            stringBuffer.append(Double.toString(depth));
        } else {
            stringBuffer.append(depthString);
        }
        return stringBuffer.toString();
    }

    /**
     * String depth representation which causes the exception..
     */
    final private String depthString;

    /**
     * @return the depthString
     */
    public String getDepthString() {
        return this.depthString;
    }

    /**
     * Dpth representation which causes the exception.
     */
    final private double depth;

    /**
     * @return the depth
     */
    public double getDepth() {
        return this.depth;
    }

    /**
     * @return depth as string
     */
    public String getDepthAsString() {

        if (depth != Double.MAX_VALUE) {
            return Double.toString(depth);
        } else {
            return depthString;
        }
    }

}
// CSON: MultipleStringLiterals
