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

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static final Pattern VARIABLE_REPLACE_MATCH = Pattern
            .compile("(^(?:\\\\\\\\)*|.*?[^\\\\](?:\\\\\\\\)*|(?<=})(?:\\\\\\\\)*)\\$\\{([a-zA-Z0-9.\\-_]+)\\}");

    private static org.apache.juli.logging.Log LOGGER = org.apache.juli.logging.LogFactory.getLog(MultipleDirectoriesContext.class);

    /**
     * Separator to use.
     */
    private String separator = ";";

    /**
     * Whether or not check the existence of variables.
     */
    private boolean checkVariables = true;

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
        virtualDocBase = substituteString(virtualDocBase);

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

    private String substituteString(String str) {
        Matcher substitutionMatcher = VARIABLE_REPLACE_MATCH.matcher(str);
        if (substitutionMatcher.find()) {
            StringBuilder buffer = new StringBuilder();
            int lastLocation = 0;
            do {
                // Find prefix, preceding ${var} construct
                String prefix = substitutionMatcher.group(1);
                buffer.append(prefix);
                // Retrieve value of variable
                String key = substitutionMatcher.group(2);
                String value = getProperty(key);
                if (value == null) {
                    value = "";
                }
                buffer.append(value);

                // Update lastLocation
                lastLocation = substitutionMatcher.end();
            } while (substitutionMatcher.find(lastLocation));
            // Append final segment of the string
            buffer.append(str.substring(lastLocation));
            return buffer.toString();
        } else {
            return str;
        }
    }

    private String getProperty(String key) {
        String value = System.getenv(key);
        if ((value == null) || (value.trim().length() == 0)) {

            final StringBuilder builder = new StringBuilder();
            final Map<String, String> envVariables = System.getenv();

            builder.append("Variable ");
            builder.append(key);
            builder.append(" not found in system environment.");
            builder.append(" Following the accessible context (");
            builder.append(envVariables.size());
            builder.append(" entries)");
            builder.append('\n');

            for (String envKey : envVariables.keySet()) {
                builder.append(" - ");
                builder.append(envKey);
                builder.append(" = ");
                builder.append(envVariables.get(envKey));
                builder.append('\n');
            }
            String msg = builder.toString();
            if (checkVariables) {
                LOGGER.error(msg);
                throw new IllegalArgumentException(msg);
            } else {
                LOGGER.warn(msg);
                return null;
            }

        }
        LOGGER.info("Substitution key " + key + " by " + value);
        return value;
    }
}
