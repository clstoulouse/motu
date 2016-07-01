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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * URI adapter that converts a xs:anyURI into a {@link URI} and vice-versa.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:ccamel@cls.fr">Christophe Camel</a>
 */
public class ListObject extends XmlAdapter<String, List<Object>> {
    /**
     * Constructeur.
     */
    public ListObject() {
    }

    // @XmlJavaTypeAdapter(ListObject.class)

    /**
     * Convert a given uri into a string representation.
     * 
     * @param value the value
     * 
     * @return the string representation.
     */
    @Override
    public String marshal(List<Object> value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * Convert a given string uri representation into an instance of {@link URI}.
     * 
     * @param s the string to convert into an uri.
     * @return a {@link URI} instance.
     */
    @Override
    public List<Object> unmarshal(String s) {
        if (s == null) {
            return null;
        }
        List list = new ArrayList<Object>();
        list.add(s);
        return list;
    }

}
