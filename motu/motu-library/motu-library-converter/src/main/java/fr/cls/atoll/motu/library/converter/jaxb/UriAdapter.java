package fr.cls.atoll.motu.library.converter.jaxb;

import java.net.URI;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * URI adapter that converts a xs:anyURI into a {@link URI} and vice-versa.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:ccamel@cls.fr">Christophe Camel</a>
 */
public class UriAdapter extends XmlAdapter<String, URI> {
    /**
     * Constructeur.
     */
    public UriAdapter() {
    }

    /**
     * Convert a given uri into a string representation.
     * 
     * @param uri the uri
     * 
     * @return the string representation.
     */
    @Override
    public String marshal(URI uri) {
        if (uri == null) {
            return null;
        }
        return uri.toString();
    }

    /**
     * Convert a given string uri representation into an instance of {@link URI}.
     * 
     * @param s the string to convert into an uri.
     * @return a {@link URI} instance.
     */
    @Override
    public URI unmarshal(String s) {
        if (s == null) {
            return null;
        }
        return URI.create(s);
    }

}
