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
package fr.cls.atoll.motu.web.bll.exception;

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

    private static final long serialVersionUID = -1L;

    /**
     * Dpth representation which causes the exception.
     */
    final private double depth;

    /**
     * String depth representation which causes the exception..
     */
    final private String depthString;

    public MotuInvalidDepthException(String depthStr, Double depth, Throwable cause) {
        super(getErrorMessage(depthStr, depth), cause);
        this.depthString = depthStr == null ? "?" : depthStr;
        this.depth = depth == null ? Double.MAX_VALUE : depth;
    }

    /**
     * @param depth depth representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDepthException(String depth, Throwable cause) {
        this(depth, null, cause);
    }

    /**
     * @param depth depth representation which causes the exception
     */
    public MotuInvalidDepthException(String depth) {
        this(depth, null, null);
    }

    /**
     * @param depth depth representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDepthException(double depth, Throwable cause) {
        this(null, depth, cause);
    }

    /**
     * @param depth depth representation which causes the exception
     */
    public MotuInvalidDepthException(double depth) {
        this(null, depth, null);
    }

    public static String getErrorMessage(String depthString, Double depth) {
        StringBuffer stringBuffer = new StringBuffer("Invalid depth. ");
        stringBuffer.append("Depth:");
        if (depth != Double.MAX_VALUE) {
            stringBuffer.append(Double.toString(depth));
        } else {
            stringBuffer.append(depthString);
        }
        return stringBuffer.toString();
    }

    /**
     * @return the depthString
     */
    public String getDepthString() {
        return this.depthString;
    }

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
