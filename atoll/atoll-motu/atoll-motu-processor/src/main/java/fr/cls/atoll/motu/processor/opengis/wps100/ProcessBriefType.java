//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.05 at 04:08:03 PM CEST 
//


package fr.cls.atoll.motu.processor.opengis.wps100;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProcessBriefType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessBriefType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/wps/1.0.0}DescriptionType">
 *       &lt;sequence>
 *         &lt;element name="Profile" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wps/1.0.0}WSDL" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://www.opengis.net/wps/1.0.0}processVersion use="required""/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessBriefType", propOrder = {
    "profile",
    "wsdl"
})
@XmlSeeAlso({
    ProcessDescriptionType.class
})
public class ProcessBriefType
    extends DescriptionType
{

    @XmlElement(name = "Profile", namespace = "http://www.opengis.net/wps/1.0.0")
    @XmlSchemaType(name = "anyURI")
    protected List<String> profile;
    @XmlElement(name = "WSDL", namespace = "http://www.opengis.net/wps/1.0.0")
    protected WSDL wsdl;
    @XmlAttribute(namespace = "http://www.opengis.net/wps/1.0.0", required = true)
    protected String processVersion;

    /**
     * Gets the value of the profile property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the profile property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProfile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getProfile() {
        if (profile == null) {
            profile = new ArrayList<String>();
        }
        return this.profile;
    }

    /**
     * Location of a WSDL document which describes this process.
     * 
     * @return
     *     possible object is
     *     {@link WSDL }
     *     
     */
    public WSDL getWSDL() {
        return wsdl;
    }

    /**
     * Sets the value of the wsdl property.
     * 
     * @param value
     *     allowed object is
     *     {@link WSDL }
     *     
     */
    public void setWSDL(WSDL value) {
        this.wsdl = value;
    }

    /**
     * Gets the value of the processVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessVersion() {
        return processVersion;
    }

    /**
     * Sets the value of the processVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessVersion(String value) {
        this.processVersion = value;
    }

}
