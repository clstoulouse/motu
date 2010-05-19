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
package fr.cls.atoll.motu.processor.wps.framework;

import fr.cls.atoll.motu.library.misc.utils.DynamicEnumerable;
import fr.cls.atoll.motu.processor.wps.framework.MotuExecuteResponse.WPSStatusResponse;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-10-15 14:38:09 $
 */
public class MotuWPSStatusType implements DynamicEnumerable<WPSStatusResponse> {
    private final WPSStatusResponse status;

    private final String value;

    public MotuWPSStatusType(WPSStatusResponse statusResponse, String value) {

        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }

        this.status = statusResponse;
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public WPSStatusResponse enumValue() {
        return status;
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return value;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(status).append(value).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        } else if (!(obj instanceof MotuWPSStatusType)) {
            return false;
        }

        MotuWPSStatusType other = (MotuWPSStatusType) obj;
        return new EqualsBuilder().append(status, other.status).append(value, other.value).isEquals();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
