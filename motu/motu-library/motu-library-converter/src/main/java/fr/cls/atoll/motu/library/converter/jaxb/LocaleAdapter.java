/**
 * 
 */
package fr.cls.atoll.motu.library.misc.converter.jaxb;

import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Locale adapter that converts a xs:string into a {@link Locale} and vice-versa.
 * 
 * @author ccamel
 * @version $Revision: 1.1 $ - $Date: 2009-05-19 13:28:44 $ - $Author: dearith $
 */
public class LocaleAdapter extends XmlAdapter<String, Locale> {
    
    /**
     * Constructeur.
     */
    public LocaleAdapter() {
    }

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleAdapter.class);

    /**
     * Convert a given uri into a string representation.
     * 
     * @param locale the locale
     * 
     * @return the string representation.
     */
    @Override
    public String marshal(Locale locale) {
        if (locale == null) {
            return null;
        }
        return StringUtils.toLanguageTag(locale);
    }

    /**
     * Convert a given string locale representation into an instance of {@link Locale}.
     * 
     * @param s the string to convert into an locale.
     * 
     * @return a {@link Locale} instance.
     */
    @Override
    public Locale unmarshal(String s) {
        if (s == null) {
            return null;
        }
        return StringUtils.parseLocaleString(s);
    }

}
