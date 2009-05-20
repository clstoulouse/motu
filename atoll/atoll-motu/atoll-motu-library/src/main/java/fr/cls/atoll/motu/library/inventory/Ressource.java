//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.05.19 at 04:42:21 PM CEST 
//


package fr.cls.atoll.motu.library.inventory;

import java.net.URI;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fr.cls.atoll.motu.library.converter.jaxb.UriAdapter;


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
 *         &lt;element ref="{}geospatialCoverage"/>
 *         &lt;element ref="{}timePeriod"/>
 *         &lt;element ref="{}variables"/>
 *       &lt;/sequence>
 *       &lt;attribute name="urn" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "geospatialCoverage",
    "timePeriod",
    "variables"
})
@XmlRootElement(name = "ressource")
public class Ressource {

    @XmlElement(required = true)
    protected GeospatialCoverage geospatialCoverage;
    @XmlElement(required = true)
    protected TimePeriod timePeriod;
    @XmlElement(required = true)
    protected Variables variables;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(UriAdapter.class)
    @XmlSchemaType(name = "anyURI")
    protected URI urn;

    /**
     * Gets the value of the geospatialCoverage property.
     * 
     * @return
     *     possible object is
     *     {@link GeospatialCoverage }
     *     
     */
    public GeospatialCoverage getGeospatialCoverage() {
        return geospatialCoverage;
    }

    /**
     * Sets the value of the geospatialCoverage property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeospatialCoverage }
     *     
     */
    public void setGeospatialCoverage(GeospatialCoverage value) {
        this.geospatialCoverage = value;
    }

    /**
     * Gets the value of the timePeriod property.
     * 
     * @return
     *     possible object is
     *     {@link TimePeriod }
     *     
     */
    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    /**
     * Sets the value of the timePeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimePeriod }
     *     
     */
    public void setTimePeriod(TimePeriod value) {
        this.timePeriod = value;
    }

    /**
     * Gets the value of the variables property.
     * 
     * @return
     *     possible object is
     *     {@link Variables }
     *     
     */
    public Variables getVariables() {
        return variables;
    }

    /**
     * Sets the value of the variables property.
     * 
     * @param value
     *     allowed object is
     *     {@link Variables }
     *     
     */
    public void setVariables(Variables value) {
        this.variables = value;
    }

    /**
     * Gets the value of the urn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public URI getUrn() {
        return urn;
    }

    /**
     * Sets the value of the urn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrn(URI value) {
        this.urn = value;
    }

}