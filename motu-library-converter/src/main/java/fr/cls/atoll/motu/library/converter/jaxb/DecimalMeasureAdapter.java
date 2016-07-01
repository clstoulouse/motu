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
package fr.cls.atoll.motu.library.converter.jaxb;

import javax.measure.DecimalMeasure;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAXB adapter that converts a xs:string into a {@link DecimalMeasure} and vice-versa, with preservation of
 * the unit (and quantity).
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:ccamel@cls.fr">Christophe Camel</a>
 */
public class DecimalMeasureAdapter extends XmlAdapter<String, DecimalMeasure<?>> {
    /**
     * Constructeur.
     */
    public DecimalMeasureAdapter() {
    }

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DecimalMeasureAdapter.class);

    /**
     * Convert a given measure into a string representation.
     * 
     * @param dm the decimal mesure to print.
     * @return the string representation.
     */
    @Override
    public String marshal(DecimalMeasure<?> dm) {
        return DatatypeConverter.printString(dm.toString());
    }

    /**
     * Convert a given string measure representation into an instance of {@link DecimalMeasure}.
     * 
     * @param s the string to convert into a measure.
     * @return a {@link DecimalMeasure} instance.
     */
    @Override
    public DecimalMeasure<?> unmarshal(String s) {
        try {
            return DecimalMeasure.valueOf(s);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to parse measure from " + s, e);
            // TODO how to handle this.
            return null;
        }
    }

}
