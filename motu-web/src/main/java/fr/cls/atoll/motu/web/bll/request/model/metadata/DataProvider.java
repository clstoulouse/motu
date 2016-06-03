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
package fr.cls.atoll.motu.web.bll.request.model.metadata;

/**
 * This class represents a the data provider (Operator of the product).
 * 
 * The organization which is responsible for the product processing.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class DataProvider {

    /**
     * Default constructor.
     */
    public DataProvider() {

    }

    /**
     * Name.
     * 
     * @uml.property name="name"
     */
    private String name = "";

    /**
     * Getter of the property <tt>name</tt>.
     * 
     * @return Returns the name.
     * @uml.property name="name"
     */
    public String getName() {
        return name;
    }

    // CSOFF: StrictDuplicateCode : normal duplication code.

    /**
     * Setter of the property <tt>name</tt>.
     * 
     * @param name The name to set.
     * @uml.property name="name"
     */
    public void setName(String name) {
        this.name = name;
    }

    // CSON: StrictDuplicateCode

    /**
     * email adress.
     * 
     * @uml.property name="email"
     */
    private String email = "";

    /**
     * Getter of the property <tt>email</tt>.
     * 
     * @return Returns the email.
     * @uml.property name="email"
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter of the property <tt>email</tt>.
     * 
     * @param email The email to set.
     * @uml.property name="email"
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Web site URL.
     * 
     * @uml.property name="webSite"
     */
    private String webSite = "";

    /**
     * Getter of the property <tt>webSite</tt>.
     * 
     * @return Returns the webSite.
     * @uml.property name="webSite"
     */
    public String getWebSite() {
        return webSite;
    }

    /**
     * Setter of the property <tt>webSite</tt>.
     * 
     * @param webSite The webSite to set.
     * @uml.property name="webSite"
     */
    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

}
