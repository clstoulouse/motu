//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.02.26 at 11:10:46 AM CET 
//

package fr.cls.atoll.motu.api.message.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * Response on variable query of a product.
 * 
 * 
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{}variableAttributes"/>
 *       &lt;attGroup ref="{}codeMsg"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "name", "standardName", "longName", "units", "description", "code", "msg" })
@XmlRootElement(name = "variable")
public class Variable {

    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "standardName")
    protected String standardName;
    @XmlAttribute(name = "longName")
    protected String longName;
    @XmlAttribute(name = "units")
    protected String units;
    @XmlAttribute(name = "description")
    protected String description;
    @XmlAttribute(name = "msg")
    protected String msg;
    @XmlAttribute(name = "code")
    protected String code;

    /**
     * Gets the value of the name property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the standardName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getStandardName() {
        return standardName;
    }

    /**
     * Sets the value of the standardName property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setStandardName(String value) {
        this.standardName = value;
    }

    /**
     * Gets the value of the longName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getLongName() {
        return longName;
    }

    /**
     * Sets the value of the longName property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setLongName(String value) {
        this.longName = value;
    }

    /**
     * Gets the value of the units property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setUnits(String value) {
        this.units = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the msg property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Sets the value of the msg property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setMsg(String value) {
        this.msg = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setCode(String value) {
        this.code = value;
    }

}
