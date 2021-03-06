//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.02.26 at 11:10:46 AM CET 
//

package fr.cls.atoll.motu.api.message.xml;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * Response on geospatial coverage of a product.
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
 *       &lt;attGroup ref="{}codeMsg"/>
 *       &lt;attribute name="north" type="{http://www.w3.org/2001/XMLSchema}decimal" default="90" />
 *       &lt;attribute name="south" type="{http://www.w3.org/2001/XMLSchema}decimal" default="-90" />
 *       &lt;attribute name="east" type="{http://www.w3.org/2001/XMLSchema}decimal" default="180" />
 *       &lt;attribute name="west" type="{http://www.w3.org/2001/XMLSchema}decimal" default="-180" />
 *       &lt;attribute name="northSouthResolution" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="northSouthUnits" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="eastWestResolution" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="eastWestUnits" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="depthMin" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="depthMax" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="depthResolution" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="depthUnits" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "north", "south", "east", "west", "northSouthResolution", "northSouthUnits", "eastWestResolution", "eastWestUnits", "depthMin", "depthMax",
        "depthResolution", "depthUnits", "code", "msg" })
@XmlRootElement(name = "geospatialCoverage")
public class GeospatialCoverage {

    @XmlAttribute(name = "north")
    protected BigDecimal north;
    @XmlAttribute(name = "south")
    protected BigDecimal south;
    @XmlAttribute(name = "east")
    protected BigDecimal east;
    @XmlAttribute(name = "west")
    protected BigDecimal west;
    @XmlAttribute(name = "northSouthResolution")
    protected BigDecimal northSouthResolution;
    @XmlAttribute(name = "northSouthUnits")
    protected String northSouthUnits;
    @XmlAttribute(name = "eastWestResolution")
    protected BigDecimal eastWestResolution;
    @XmlAttribute(name = "eastWestUnits")
    protected String eastWestUnits;
    @XmlAttribute(name = "depthMin")
    protected BigDecimal depthMin;
    @XmlAttribute(name = "depthMax")
    protected BigDecimal depthMax;
    @XmlAttribute(name = "depthResolution")
    protected BigDecimal depthResolution;
    @XmlAttribute(name = "depthUnits")
    protected String depthUnits;
    @XmlAttribute(name = "msg")
    protected String msg;
    @XmlAttribute(name = "code")
    protected String code;

    /**
     * Gets the value of the north property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getNorth() {
        if (north == null) {
            return new BigDecimal("90");
        } else {
            return north;
        }
    }

    /**
     * Sets the value of the north property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setNorth(BigDecimal value) {
        this.north = value;
    }

    /**
     * Gets the value of the south property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getSouth() {
        if (south == null) {
            return new BigDecimal("-90");
        } else {
            return south;
        }
    }

    /**
     * Sets the value of the south property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setSouth(BigDecimal value) {
        this.south = value;
    }

    /**
     * Gets the value of the east property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getEast() {
        if (east == null) {
            return new BigDecimal("180");
        } else {
            return east;
        }
    }

    /**
     * Sets the value of the east property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setEast(BigDecimal value) {
        this.east = value;
    }

    /**
     * Gets the value of the west property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getWest() {
        if (west == null) {
            return new BigDecimal("-180");
        } else {
            return west;
        }
    }

    /**
     * Sets the value of the west property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setWest(BigDecimal value) {
        this.west = value;
    }

    /**
     * Gets the value of the northSouthResolution property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getNorthSouthResolution() {
        return northSouthResolution;
    }

    /**
     * Sets the value of the northSouthResolution property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setNorthSouthResolution(BigDecimal value) {
        this.northSouthResolution = value;
    }

    /**
     * Gets the value of the northSouthUnits property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getNorthSouthUnits() {
        return northSouthUnits;
    }

    /**
     * Sets the value of the northSouthUnits property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setNorthSouthUnits(String value) {
        this.northSouthUnits = value;
    }

    /**
     * Gets the value of the eastWestResolution property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getEastWestResolution() {
        return eastWestResolution;
    }

    /**
     * Sets the value of the eastWestResolution property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setEastWestResolution(BigDecimal value) {
        this.eastWestResolution = value;
    }

    /**
     * Gets the value of the eastWestUnits property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getEastWestUnits() {
        return eastWestUnits;
    }

    /**
     * Sets the value of the eastWestUnits property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setEastWestUnits(String value) {
        this.eastWestUnits = value;
    }

    /**
     * Gets the value of the depthMin property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getDepthMin() {
        return depthMin;
    }

    /**
     * Sets the value of the depthMin property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setDepthMin(BigDecimal value) {
        this.depthMin = value;
    }

    /**
     * Gets the value of the depthMax property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getDepthMax() {
        return depthMax;
    }

    /**
     * Sets the value of the depthMax property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setDepthMax(BigDecimal value) {
        this.depthMax = value;
    }

    /**
     * Gets the value of the depthResolution property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getDepthResolution() {
        return depthResolution;
    }

    /**
     * Sets the value of the depthResolution property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setDepthResolution(BigDecimal value) {
        this.depthResolution = value;
    }

    /**
     * Gets the value of the depthUnits property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDepthUnits() {
        return depthUnits;
    }

    /**
     * Sets the value of the depthUnits property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setDepthUnits(String value) {
        this.depthUnits = value;
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
