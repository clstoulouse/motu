/**
 * 
 */
package fr.cls.atoll.motu.library.misc.converter.jaxb;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * URI adapter that converts a xs:anyURI into a {@link URI} and vice-versa.
 * 
 * @author ccamel
 * @version $Revision: 1.2 $ - $Date: 2009-10-29 10:51:20 $ - $Author: dearith $
 */
public class ListObject extends XmlAdapter<String, List<Object>> {
    /**
     * Constructeur.
     */
    public ListObject() {
    }

    //@XmlJavaTypeAdapter(ListObject.class)

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
