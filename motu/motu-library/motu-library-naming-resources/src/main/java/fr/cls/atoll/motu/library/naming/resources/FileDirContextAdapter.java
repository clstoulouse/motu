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

import org.apache.naming.resources.FileDirContext;

/**
 * Adpater class that does nothing except allowing resources context declared in this package to access the
 * protected method {@link #file(String)}.
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
     * Call the super method without doing more.
     * <p>
     * Allows any classes declared in this package to call it.
     */
    @Override
    protected File file(String fileName) {
        return super.file(fileName);
    }
}
