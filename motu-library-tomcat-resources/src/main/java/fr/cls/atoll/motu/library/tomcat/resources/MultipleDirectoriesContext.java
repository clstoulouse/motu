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
package fr.cls.atoll.motu.library.tomcat.resources;

import fr.cls.atoll.motu.library.tomcat.util.VariableSubstitutionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Multiple Filesystem Directory Context.
 * <p>
 * Configured with the following attributes:
 * <ul>
 * <li>virtualDocBase: List the pathes containing the static resources. Multiple pathes must be separated with
 * a separator (';' by default). Attribute supports system variables expressed like ${variable}. The special
 * '.' path directory denotes the default docBase (containing the WEB-INF of the current servlet).</li>
 * <li>separator: Allows to specify a custom separator to use.</li>
 * <li>checkVariables: Allows to specify if the variables existence should be tested or not. <code>true</code>
 * by default.</li>
 * </ul>
 * <p>
 * 
 * <code>
 * &lt;Context docBase="\webapps\mydocbase">
 *   &lt;Resources className="fr.cls.atoll.motu.library.naming.resources.MultipleDirectoriesContext"
 *              virtualDocBase=".;/path/to/another/docbase1;/path/to/another/docbase2"/>
 * &lt;/Resources>
 * </code>
 * <p>
 * Order of declaration is important. A resource is search into each declared path while not found.
 * 
 * @author ccamel
 * @version $Revision: 1.12 $ - $Date: 2010/02/08 13:32:34 $ - $Author: ccamel $
 */
public class MultipleDirectoriesContext extends FileDirContextAdapter {
    private static org.apache.juli.logging.Log LOGGER = org.apache.juli.logging.LogFactory.getLog(MultipleDirectoriesContext.class);

    /**
     * Separator to use.
     */
    private String separator = ";";

    /**
     * Whether or not check the existence of variables.
     */
    private boolean checkVariables = false;

    /**
     * Set of virtual contexts configured (keep the insertion order).
     */
    private final List<FileDirContextAdapter> virtualContexts = new ArrayList<FileDirContextAdapter>();

    public MultipleDirectoriesContext() {
        super();
    }

    public MultipleDirectoriesContext(Hashtable env) {
        super(env);
    }

    /**
     * @return the checkVariables
     */
    public boolean getCheckVariables() {
        return checkVariables;
    }

    /**
     * @param checkVariables the checkVariables to set
     */
    public void setCheckVariables(boolean checkVariables) {
        this.checkVariables = checkVariables;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.naming.resources.FileDirContext#setDocBase(java.lang.String)
     */
    @Override
    public void setDocBase(String docBase) {
        LOGGER.info("Setting docBase to " + docBase);
        super.setDocBase(docBase);
    }

    public void setVirtualDocBase(String virtualDocBase) {
        // first we replace the variables if necessary
        virtualDocBase = VariableSubstitutionUtils.substituteString(virtualDocBase, getCheckVariables());

        // then parse it.
        final StringTokenizer st = new StringTokenizer(virtualDocBase, getSeparator());
        while (st.hasMoreTokens()) {
            final String docBase = st.nextToken().trim();
            if (docBase.length() > 0) {
                if (docBase.equals(".")) {
                    virtualContexts.add(this);
                }
                FileDirContextAdapter currentContext = new FileDirContextAdapter();
                LOGGER.info("Setting docBase to " + docBase);
                currentContext.setDocBase(docBase);
                virtualContexts.add(currentContext);
            }
        }
        if (virtualContexts.isEmpty()) {
            String msg = "virtual context list is empty. Set at least the '.' path.";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    protected File file(String fileName) {
        // return the first resolved file found
        for (FileDirContextAdapter virtualContext : virtualContexts) {
            final File file = virtualContext == this ? super.file(fileName) : virtualContext.file(fileName);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    @Override
    public void release() {
        for (FileDirContextAdapter virtualContext : virtualContexts) {
            if (virtualContext != this) {
                virtualContext.release();
            } else {
                super.release();
            }
        }
    }

    @Override
    public void allocate() {
        for (FileDirContextAdapter virtualContext : virtualContexts) {
            if (virtualContext != this) {
                virtualContext.setCached(this.isCached());
                virtualContext.setCacheTTL(this.getCacheTTL());
                virtualContext.setCacheMaxSize(this.getCacheMaxSize());
                virtualContext.setCaseSensitive(this.isCaseSensitive());
                virtualContext.setAllowLinking(this.getAllowLinking());
                virtualContext.allocate();
            } else {
                super.allocate();
            }

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
    public List<FileDirContextAdapter> getVirtualContexts() {
        return virtualContexts;
    }

}
