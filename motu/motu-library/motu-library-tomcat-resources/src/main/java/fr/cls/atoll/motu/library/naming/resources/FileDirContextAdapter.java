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
package fr.cls.atoll.motu.library.naming.resources;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;

import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.RecyclableNamingEnumeration;

/**
 * Adpater class that add more properties for the file attributes (like path).
 * 
 * @author ccamel
 * @version $Revision: 1.12 $ - $Date: 2010/02/08 13:32:34 $ - $Author: ccamel $
 */
public class FileDirContextAdapter extends FileDirContext {
    public FileDirContextAdapter() {
    }

    public FileDirContextAdapter(Hashtable env) {
        super(env);
    }

    /**
     * Make the method visible.
     * 
     * @see org.apache.naming.resources.FileDirContext#file(java.lang.String)
     */
    @Override
    protected File file(String name) {
        return super.file(name);
    }

    /**
     * Retrieves selected attributes associated with a named object. See the class description regarding
     * attribute models, attribute type names, and operational attributes.
     * 
     * @return the requested attributes; never null
     * @param name the name of the object from which to retrieve attributes
     * @param attrIds the identifiers of the attributes to retrieve. null indicates that all attributes should
     *            be retrieved; an empty array indicates that none should be retrieved
     * @exception NamingException if a naming exception is encountered
     */
    @Override
    public Attributes getAttributes(String name, String[] attrIds) throws NamingException {

        // Building attribute list
        File file = file(name);

        if (file == null) {
            throw new NamingException(sm.getString("resources.notFound", name));
        }

        return new FileResourceAttributes(file) {

            /*
             * (non-Javadoc)
             * 
             * @see org.apache.naming.resources.ResourceAttributes#getAll()
             */
            @Override
            public NamingEnumeration getAll() {
                Vector attributes = new Vector();

                try {
                    NamingEnumeration ne = super.getAll();

                    while (ne.hasMore()) {
                        attributes.add(ne.next());
                    }
                } catch (Exception e) {

                }
                attributes.addElement(new BasicAttribute("canonicalPath", this.getCanonicalPath()));

                return new RecyclableNamingEnumeration(attributes);
            }
        };

    }
}
