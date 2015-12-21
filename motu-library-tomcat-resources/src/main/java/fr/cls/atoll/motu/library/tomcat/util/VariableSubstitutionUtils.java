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
package fr.cls.atoll.motu.library.tomcat.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitary class for performing variable substitution inside strings.
 * 
 * @author ccamel
 * 
 */
public abstract class VariableSubstitutionUtils {
    private static org.apache.juli.logging.Log LOGGER = org.apache.juli.logging.LogFactory.getLog(VariableSubstitutionUtils.class);

    public static final Pattern VARIABLE_REPLACE_MATCH = Pattern
            .compile("(^(?:\\\\\\\\)*|.*?[^\\\\](?:\\\\\\\\)*|(?<=})(?:\\\\\\\\)*)\\$\\{([a-zA-Z0-9.\\-_]+)\\}");

    /**
     * Performs a variable substitution with existence variable checking.
     * 
     * @param str the string in which replace the variables.
     * @return the substitued string.
     * @return IllegalArgumentException if a variable can be found in {@link System#getenv(String)}.
     */
    public static String substituteString(String str) {
        return substituteString(str, true);
    }

    /**
     * @param str the string in which replace the variables.
     * @param checkVariableExistence if the variable checks should be done.
     * @return the substitued string.
     * @return IllegalArgumentException if a variable can be found in {@link System#getenv(String)} when
     *         checkVaribleExistence is set to true.
     */
    public static String substituteString(String str, boolean checkVariableExistence) {
        final Matcher substitutionMatcher = VARIABLE_REPLACE_MATCH.matcher(str);
        if (substitutionMatcher.find()) {
            StringBuilder buffer = new StringBuilder();
            int lastLocation = 0;
            do {
                // Find prefix, preceding ${var} construct
                String prefix = substitutionMatcher.group(1);
                buffer.append(prefix);
                // Retrieve value of variable
                String key = substitutionMatcher.group(2);
                String value = getProperty(key, checkVariableExistence);
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

    protected static String getProperty(String key, boolean checkVariables) {
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
