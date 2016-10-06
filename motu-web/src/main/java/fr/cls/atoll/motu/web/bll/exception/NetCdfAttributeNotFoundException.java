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

import ucar.nc2.Variable;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Exception class for NetCDF attribute 'not found' exception.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class NetCdfAttributeNotFoundException extends MotuExceptionBase {

    private static final long serialVersionUID = -1;

    /**
     * NetCDF variable whose attribute is not found.
     */
    final private Variable netCdfVariable;

    /**
     * NetCDF attribute which causes the exception.
     */
    final private String attrName;

    /**
     * @param attrName name of the 'not found' attribute.
     */
    public NetCdfAttributeNotFoundException(String attrName) {
        this(null, attrName);
    }

    /**
     * @param netCdfVariable NetCDF variable that causes the exception
     * @param attrName name of the 'not found' attribute.
     */
    public NetCdfAttributeNotFoundException(Variable netCdfVariable, String attrName) {
        super(getErrorMessage(netCdfVariable, attrName));
        this.netCdfVariable = netCdfVariable;
        this.attrName = attrName;
    }

    public static String getErrorMessage(Variable netCdfVariable, String attrName) {
        StringBuffer stringBuffer = new StringBuffer("NetCdf attribute not found.");

        if (netCdfVariable != null) {
            stringBuffer.append(String.format("\nNetCdf variable name: %s\n", netCdfVariable.getName()));
        }
        stringBuffer.append(String.format("\nAttribute name: %s\n", attrName));
        return stringBuffer.toString();
    }

}
// CSON: MultipleStringLiterals
