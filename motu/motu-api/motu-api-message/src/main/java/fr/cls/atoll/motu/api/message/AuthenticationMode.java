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
package fr.cls.atoll.motu.api.message;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum for different modes of authentication.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public enum AuthenticationMode {

    /** No Authentication. */
    NONE(0),

    /** CAS Authentication. */
    CAS(1),
    
    /** Basic Authentication. */
    BASIC(2);

    /** The value. */
    private final int value;

    /**
     * Instantiates a new format.
     * 
     * @param v the v
     */
    AuthenticationMode(int v) {
        value = v;
    }

    /**
     * Value.
     * 
     * @return the int
     */
    public int value() {
        return value;
    }

    /**
     * From value.
     * 
     * @param v the v
     * 
     * @return the format
     */
    public static AuthenticationMode fromValue(int v) {
        for (AuthenticationMode c : AuthenticationMode.values()) {
            if (c.value == v) {
                return c;
            }
        }
        throw new IllegalArgumentException(String.valueOf(v));
    }

    /**
     * From value.
     * 
     * @param v the v
     * 
     * @return the authentication mode
     */
    public static AuthenticationMode fromValue(String v) {
        for (AuthenticationMode c : AuthenticationMode.values()) {
            if (c.toString().equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(String.valueOf(v));
    }

    /**
     * Gets the default.
     * 
     * @return the default
     */
    public static AuthenticationMode getDefault() {
        return CAS;
    }

    /**
     * Gets the available values.
     * 
     * @return the available values
     */
    public static List<String> getAvailableValues() {
        List<String> list = new ArrayList<String>();

        for (AuthenticationMode c : AuthenticationMode.values()) {
            list.add(c.toString());
        }

        return list;
    }
}
