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
 * Documentation product class.
 * 
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class DocMetaData {

    /**
     * Default constructor.
     */
    public DocMetaData() {

    }

    // CSOFF: StrictDuplicateCode : normal duplication code.

    /**
     * Title of the document.
     * 
     * @uml.property name="title"
     */
    private String title = "";

    /**
     * Getter of the property <tt>title</tt>.
     * 
     * @return Returns the title.
     * @uml.property name="title"
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter of the property <tt>title</tt>.
     * 
     * @param title The title to set.
     * @uml.property name="title"
     */
    public void setTitle(String title) {
        this.title = title;
    }

    // CSON: StrictDuplicateCode

    /**
     * online (eg. http) resource where the document is available.
     * 
     * @uml.property name="resource"
     */
    private String resource = "";

    /**
     * Getter of the property <tt>resource</tt>.
     * 
     * @return Returns the resource.
     * @uml.property name="resource"
     */
    public String getResource() {
        return resource;
    }

    /**
     * Setter of the property <tt>resource</tt>.
     * 
     * @param resource The resource to set.
     * @uml.property name="resource"
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Date of document's publication.
     * 
     * @uml.property name="publication"
     */
    private String publication = "";

    /**
     * Getter of the property <tt>publication</tt>.
     * 
     * @return Returns the publication.
     * @uml.property name="publication"
     */
    public String getPublication() {
        return publication;
    }

    /**
     * Setter of the property <tt>publication</tt>.
     * 
     * @param publication The publication to set.
     * @uml.property name="publication"
     */
    public void setPublication(String publication) {
        this.publication = publication;
    }

}
