package fr.cls.atoll.motu.api.message;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum for different modes of authentication.
 */
public enum AuthenticationMode {

    /** No Authentification. */
    NONE(0),

    /** CAS Authentification. */
    CAS(0);

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
     * @return the authentification mode
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
