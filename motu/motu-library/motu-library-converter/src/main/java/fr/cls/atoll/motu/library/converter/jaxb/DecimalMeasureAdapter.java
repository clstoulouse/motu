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
