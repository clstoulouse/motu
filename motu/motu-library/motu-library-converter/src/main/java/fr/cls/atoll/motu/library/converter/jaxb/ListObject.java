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
