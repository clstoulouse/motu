//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.05 at 04:08:03 PM CEST 
//


package fr.cls.atoll.motu.processor.opengis.wps100;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SupportedComplexDataInputType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SupportedComplexDataInputType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/wps/1.0.0}SupportedComplexDataType">
 *       &lt;attribute name="maximumMegabytes" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SupportedComplexDataInputType")
public class SupportedComplexDataInputType
    extends SupportedComplexDataType
{

    @XmlAttribute
    protected BigInteger maximumMegabytes;

    /**
     * Gets the value of the maximumMegabytes property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaximumMegabytes() {
        return maximumMegabytes;
    }

    /**
     * Sets the value of the maximumMegabytes property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaximumMegabytes(BigInteger value) {
        this.maximumMegabytes = value;
    }

}
