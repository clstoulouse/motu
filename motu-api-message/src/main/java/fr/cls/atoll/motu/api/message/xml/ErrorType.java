//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.09.19 at 09:56:17 AM CEST 
//


package fr.cls.atoll.motu.api.message.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for errorType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="errorType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}unsignedShort">
 *     &lt;enumeration value="0"/>
 *     &lt;enumeration value="1"/>
 *     &lt;enumeration value="2"/>
 *     &lt;enumeration value="3"/>
 *     &lt;enumeration value="4"/>
 *     &lt;enumeration value="5"/>
 *     &lt;enumeration value="6"/>
 *     &lt;enumeration value="7"/>
 *     &lt;enumeration value="8"/>
 *     &lt;enumeration value="9"/>
 *     &lt;enumeration value="10"/>
 *     &lt;enumeration value="11"/>
 *     &lt;enumeration value="12"/>
 *     &lt;enumeration value="13"/>
 *     &lt;enumeration value="14"/>
 *     &lt;enumeration value="15"/>
 *     &lt;enumeration value="16"/>
 *     &lt;enumeration value="17"/>
 *     &lt;enumeration value="18"/>
 *     &lt;enumeration value="19"/>
 *     &lt;enumeration value="20"/>
 *     &lt;enumeration value="21"/>
 *     &lt;enumeration value="22"/>
 *     &lt;enumeration value="23"/>
 *     &lt;enumeration value="24"/>
 *     &lt;enumeration value="25"/>
 *     &lt;enumeration value="26"/>
 *     &lt;enumeration value="27"/>
 *     &lt;enumeration value="28"/>
 *     &lt;enumeration value="29"/>
 *     &lt;enumeration value="30"/>
 *     &lt;enumeration value="31"/>
 *     &lt;enumeration value="32"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "errorType")
@XmlEnum(Integer.class)
public enum ErrorType {

    @XmlEnumValue("0")
    OK(0),
    @XmlEnumValue("1")
    SYSTEM(1),
    @XmlEnumValue("2")
    INCONSISTENCY(2),
    @XmlEnumValue("3")
    INVALID_DATE(3),
    @XmlEnumValue("4")
    INVALID_LATITUDE(4),
    @XmlEnumValue("5")
    INVALID_LONGITUDE(5),
    @XmlEnumValue("6")
    INVALID_DATE_RANGE(6),
    @XmlEnumValue("7")
    EXCEEDING_CAPACITY(7),
    @XmlEnumValue("8")
    INVALID_LAT_LON_RANGE(8),
    @XmlEnumValue("9")
    INVALID_DEPTH_RANGE(9),
    @XmlEnumValue("10")
    NOT_IMPLEMENTED(10),
    @XmlEnumValue("11")
    NETCDF_VARIABLE(11),
    @XmlEnumValue("12")
    NO_VARIABLE(12),
    @XmlEnumValue("13")
    NETCDF_VARIABLE_NOT_FOUND(13),
    @XmlEnumValue("14")
    NETCDF_ATTRIBUTE(14),
    @XmlEnumValue("15")
    EXCEEDING_QUEUE_CAPACITY(15),
    @XmlEnumValue("16")
    EXCEEDING_USER_CAPACITY(16),
    @XmlEnumValue("17")
    INVALID_DEPTH(17),
    @XmlEnumValue("18")
    INVALID_QUEUE_PRIORITY(18),
    @XmlEnumValue("19")
    UNKNOWN_REQUEST_ID(19),
    @XmlEnumValue("20")
    EXCEEDING_QUEUE_DATA_CAPACITY(20),
    @XmlEnumValue("21")
    SHUTTING_DOWN(21),
    @XmlEnumValue("22")
    MOTU_CONFIG(22),
    @XmlEnumValue("23")
    LOADING_CATALOG(23),
    @XmlEnumValue("24")
    LOADING_MESSAGE_ERROR(24),
    @XmlEnumValue("25")
    NETCDF_LOADING(25),
    @XmlEnumValue("26")
    BAD_PARAMETERS(26),
    @XmlEnumValue("27")
    NETCDF_GENERATION(27),
    @XmlEnumValue("28")
    UNKNOWN_ACTION(28),
    @XmlEnumValue("29")
    UNKNOWN_PRODUCT(29),
    @XmlEnumValue("30")
    UNKNOWN_SERVICE(30),
    @XmlEnumValue("31")
    TOO_DEPTH_REQUESTED(31),
    @XmlEnumValue("32")
    NETCDF4_NOT_SUPPORTED_BY_TDS(32);
    private final int value;

    ErrorType(int v) {
        value = v;
    }

    public int value() {
        return value;
    }

    public static ErrorType fromValue(int v) {
        for (ErrorType c: ErrorType.values()) {
            if (c.value == v) {
                return c;
            }
        }
        throw new IllegalArgumentException(String.valueOf(v));
    }

}
