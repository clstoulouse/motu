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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.naming.resources.FileDirContext;

/**
 * Multiple Filesystem Directory Context.
 * <p>
 * Configured with the following attributes:
 * <ul>
 * <li>virtualDocBase: List the pathes containing the static resources. Multiple pathes must be separated with
 * a separator (';' by default).</li>
 * <li>separator: Allows to specify a custom separator to use.</li>
 * </ul>
 * <p>
 * 
 * <code>
 * &lt;Context docBase="\webapps\mydocbase">
 *   &lt;Resources className="fr.cls.atoll.motu.library.naming.resources.MultipleDirectoriesContext"
 *              virtualDocBase="/path/to/another/docbase1;/path/to/another/docbase2"/>
 * &lt;/Resources>
 * </code>
 * 
 * @author ccamel
 * @version $Revision: 607568 $ $Date: 2007-12-30 18:56:50 +0100 (Sun, 30 Dec 2007) $
 */
public class MultipleDirectoriesContext extends FileDirContext {
    /**
     * Separator to use.
     */
    private String separator = ";";

    /**
     * Set of virtual contexts configured.
     */
    private final Set<FileDirContextAdapter> virtualContexts = new LinkedHashSet<FileDirContextAdapter>();

    public MultipleDirectoriesContext() {
        super();
    }

    public MultipleDirectoriesContext(Hashtable env) {
        super(env);
    }

    public void setVirtualDocBase(final String virtualDocBase) {
        final StringTokenizer st = new StringTokenizer(virtualDocBase, getSeparator());
        while (st.hasMoreTokens()) {
            FileDirContextAdapter currentContext = new FileDirContextAdapter();
            currentContext.setDocBase(st.nextToken());
            virtualContexts.add(currentContext);
        }
    }

    @Override
    protected File file(String fileName) {
        File file = super.file(fileName);

        if (file == null) {
            // return the first resolved file found
            for (FileDirContextAdapter virtualContext : virtualContexts) {
                file = virtualContext.file(fileName);
                if (file != null) {
                    return file;
                }
            }
        }
        return file;
    }

    @Override
    public void release() {
        super.release();
        for (FileDirContextAdapter virtualContext : virtualContexts) {
            virtualContext.release();
        }
    }

    @Override
    public void allocate() {
        super.allocate();
        for (FileDirContextAdapter virtualContext : virtualContexts) {
            virtualContext.setCached(this.isCached());
            virtualContext.setCacheTTL(this.getCacheTTL());
            virtualContext.setCacheMaxSize(this.getCacheMaxSize());
            virtualContext.setCaseSensitive(this.isCaseSensitive());
            virtualContext.setAllowLinking(this.getAllowLinking());
            virtualContext.allocate();
        }
    }

    /**
     * @return the separator
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * @param separator the separator to set
     */
    public void setSeparator(String separator) {
        if ((separator == null) || (separator.trim().length() == 0)) {
            throw new IllegalArgumentException("Separator is null or empty.");
        }
        this.separator = separator;
    }

    /**
     * @return the virtualContexts
     */
    public Set<FileDirContextAdapter> getVirtualContexts() {
        return virtualContexts;
    }
}
