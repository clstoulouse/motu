//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.13 at 04:38:35 PM CEST 
//


package fr.cls.atoll.motu.processor.opengis.wps100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import fr.cls.atoll.motu.processor.opengis.ows110.DomainMetadataType;


/**
 * Listing of the Unit of Measure (U0M) support for this process input or output. 
 * 
 * <p>Java class for SupportedUOMsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SupportedUOMsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Default">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://www.opengis.net/ows/1.1}UOM"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Supported" type="{http://www.opengis.net/wps/1.0.0}UOMsType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SupportedUOMsType", propOrder = {
    "_default",
    "supported"
})
public class SupportedUOMsType {

    @XmlElement(name = "Default", namespace = "", required = true)
    protected SupportedUOMsType.Default _default;
    @XmlElement(name = "Supported", namespace = "", required = true)
    protected UOMsType supported;

    /**
     * Gets the value of the default property.
     * 
     * @return
     *     possible object is
     *     {@link SupportedUOMsType.Default }
     *     
     */
    public SupportedUOMsType.Default getDefault() {
        return _default;
    }

    /**
     * Sets the value of the default property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupportedUOMsType.Default }
     *     
     */
    public void setDefault(SupportedUOMsType.Default value) {
        this._default = value;
    }

    /**
     * Gets the value of the supported property.
     * 
     * @return
     *     possible object is
     *     {@link UOMsType }
     *     
     */
    public UOMsType getSupported() {
        return supported;
    }

    /**
     * Sets the value of the supported property.
     * 
     * @param value
     *     allowed object is
     *     {@link UOMsType }
     *     
     */
    public void setSupported(UOMsType value) {
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
     *         &lt;element ref="{http://www.opengis.net/ows/1.1}UOM"/>
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
        "uom"
    })
    public static class Default {

        @XmlElement(name = "UOM", namespace = "http://www.opengis.net/ows/1.1", required = true)
        protected DomainMetadataType uom;

        /**
         * Reference to the default UOM supported for this Input/Output
         * 
         * @return
         *     possible object is
         *     {@link DomainMetadataType }
         *     
         */
        public DomainMetadataType getUOM() {
            return uom;
        }

        /**
         * Sets the value of the uom property.
         * 
         * @param value
         *     allowed object is
         *     {@link DomainMetadataType }
         *     
         */
        public void setUOM(DomainMetadataType value) {
            this.uom = value;
        }

    }

}
