//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.05 at 04:08:03 PM CEST 
//


package fr.cls.atoll.motu.processor.opengis.wps100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Listing of the Coordinate Reference System (CRS) support for this process input or output. 
 * 
 * <p>Java class for SupportedCRSsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SupportedCRSsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Default">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="CRS" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Supported" type="{http://www.opengis.net/wps/1.0.0}CRSsType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SupportedCRSsType", propOrder = {
    "_default",
    "supported"
})
public class SupportedCRSsType {

    @XmlElement(name = "Default", required = true)
    protected SupportedCRSsType.Default _default;
    @XmlElement(name = "Supported", required = true)
    protected CRSsType supported;

    /**
     * Gets the value of the default property.
     * 
     * @return
     *     possible object is
     *     {@link SupportedCRSsType.Default }
     *     
     */
    public SupportedCRSsType.Default getDefault() {
        return _default;
    }

    /**
     * Sets the value of the default property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupportedCRSsType.Default }
     *     
     */
    public void setDefault(SupportedCRSsType.Default value) {
        this._default = value;
    }

    /**
     * Gets the value of the supported property.
     * 
     * @return
     *     possible object is
     *     {@link CRSsType }
     *     
     */
    public CRSsType getSupported() {
        return supported;
    }

    /**
     * Sets the value of the supported property.
     * 
     * @param value
     *     allowed object is
     *     {@link CRSsType }
     *     
     */
    public void setSupported(CRSsType value) {
        this.supported = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="CRS" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "crs"
    })
    public static class Default {

        @XmlElement(name = "CRS", required = true)
        @XmlSchemaType(name = "anyURI")
        protected String crs;

        /**
         * Gets the value of the crs property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCRS() {
            return crs;
        }

        /**
         * Sets the value of the crs property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCRS(String value) {
            this.crs = value;
        }

    }

}
