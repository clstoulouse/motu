package fr.cls.atoll.motu.web.common.format;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public enum OutputFormat {

    /** ascii format. */
    ASCII(0),

    /** html format. */
    HTML(1),

    /** NetCdf-3 format. */
    NETCDF(2),

    /** xml format. */
    XML(3),

    /** xml format. */
    URL(4),

    /** NetCdf-4 format. */
    NETCDF4(5);

    /** The value. */
    private final int value;

    /**
     * Instantiates a new format.
     * 
     * @param v the v
     */
    OutputFormat(int v) {
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
    public static OutputFormat fromValue(int v) {
        for (OutputFormat c : OutputFormat.values()) {
            if (c.value == v) {
                return c;
            }
        }
        throw new IllegalArgumentException(String.valueOf(v));
    }

    public static String valuesToString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (OutputFormat c : OutputFormat.values()) {
            stringBuffer.append(c.toString());
            stringBuffer.append(" ");
        }
        return stringBuffer.toString();
    }

    /**
     * Gets the default.
     * 
     * @return the default
     */
    public static OutputFormat getDefault() {
        return NETCDF;
    }

}
