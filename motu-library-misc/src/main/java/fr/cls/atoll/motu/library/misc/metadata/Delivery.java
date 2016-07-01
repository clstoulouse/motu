/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.library.misc.metadata;

/**
 * Delivery resource class.
 * 
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class Delivery {

    /**
     * Default constructor.
     */
    public Delivery() {

    }

    /**
     * Ressource of delivery or viewing service (protocol and online resource).
     * 
     * @uml.property name="resource"
     */
    private String resource;

    /**
     * Getter of the property <tt>Resource</tt>.
     * 
     * @return Returns the resource.
     * @uml.property name="resource"
     */
    public String getResource() {
        return resource;
    }

    /**
     * Setter of the property <tt>Resource</tt>.
     * 
     * @param resource The resource to set.
     * @uml.property name="resource"
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Estimated volume (in bytes, Kb or Mb...) of each release.
     * 
     * @uml.property name="volume"
     */
    private String volume = "";

    /**
     * Getter of the property <tt>volume</tt>.
     * 
     * @return Returns the volume.
     * @uml.property name="volume"
     */
    public String getVolume() {
        return volume;
    }

    /**
     * Setter of the property <tt>volume</tt>.
     * 
     * @param volume The volume to set.
     * @uml.property name="volume"
     */
    public void setVolume(String volume) {
        this.volume = volume;
    }

    /**
     * Delivery or viewing format or type of application or high level protocol which makes the product
     * available. Can be : image, OpenDAP, LAS, netCDF...
     * 
     * @uml.property name="format"
     */
    private String format = "";

    /**
     * Getter of the property <tt>format</tt>.
     * 
     * @return Returns the format.
     * @uml.property name="format"
     */
    public String getFormat() {
        return format;
    }

    /**
     * Setter of the property <tt>format</tt>.
     * 
     * @param format The format to set.
     * @uml.property name="format"
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Licence/Condition : textual information explaining if service access is free and, otherwise, how to get
     * authorization (can be an URL pointed to a textual explanation).
     * 
     * @uml.property name="condition"
     */
    private String condition = "";

    /**
     * Getter of the property <tt>condition</tt>.
     * 
     * @return Returns the condition.
     * @uml.property name="condition"
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Setter of the property <tt>condition</tt>.
     * 
     * @param condition The condition to set.
     * @uml.property name="condition"
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * @uml.property name="distributor"
     * @uml.associationEnd inverse="delivery:fr.cls.atoll.motu.library.metadata.TEP"
     * @uml.association name="Delivery - TEP"
     */
    private TEP distributor;

    /**
     * Getter of the property <tt>distributor</tt>.
     * 
     * @return Returns the distributor.
     * @uml.property name="distributor"
     */
    public TEP getDistributor() {
        return distributor;
    }

    /**
     * Setter of the property <tt>distributor</tt>.
     * 
     * @param distributor The distributor to set.
     * @uml.property name="distributor"
     */
    public void setDistributor(TEP distributor) {
        this.distributor = distributor;
    }

}
