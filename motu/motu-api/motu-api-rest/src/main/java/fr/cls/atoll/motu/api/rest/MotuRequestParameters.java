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
package fr.cls.atoll.motu.api.rest;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Classe permettant de stocker tous les paramètres d'une requête. <br>
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuRequestParameters implements MotuRequestParametersConstant {

    /**
     * The Constructor.
     */
    public MotuRequestParameters() {

    }

    /** The mono valued param map. */
    Map<String, Object> monoValuedParamMap = new HashMap<String, Object>();

    /** The multi valued param map. */
    Map<String, List<?>> multiValuedParamMap = new HashMap<String, List<?>>();

    /**
     * Récupère un paramètre à partir de son nom.
     * 
     * @param paramName le nom du paramètre
     * 
     * @return la valeur du paramètre
     */
    public Object getParameter(String paramName) {
        return monoValuedParamMap.get(paramName);
    }

    /**
     * Positionne un paramètre.
     * 
     * @param value la valeur du paramètre
     * @param paramName le nom du paramètre
     */
    public void setParameter(String paramName, Object value) {
        monoValuedParamMap.put(paramName, value.toString());
    }

    /**
     * Récupère les valeurs d'un paramètre multivalué à partir de son nom.
     * 
     * @param paramName le nom du paramètre
     * 
     * @return la liste des valeurs du paramètre
     */
    public List<?> getMultiValuedParameter(String paramName) {
        return multiValuedParamMap.get(paramName);
    }

    /**
     * Positionne un paramètre multivalué.
     * 
     * @param valueList la liste des valeurs du paramètre
     * @param paramName le nom du paramètre
     */
    public void setMultiValuedParameter(String paramName, List<?> valueList) {
        multiValuedParamMap.put(paramName, valueList);
    }

    /**
     * Positionne un paramètre multivalué.
     * 
     * @param values le tableau des valeurs du paramètre
     * @param paramName le nom du paramètre
     */
    public void setMultiValuedParameter(String paramName, Object[] values) {
        List<Object> valueList = new ArrayList<Object>();
        for (int i = 0; i < values.length; ++i) {
            valueList.add(values[i]);
        }
        multiValuedParamMap.put(paramName, valueList);
    }

    /**
     * Clear parameters.
     */
    public void clearParameters() {
        monoValuedParamMap.clear();
        multiValuedParamMap.clear();
    }

    /**
     * To string.
     * 
     * @return the string
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer(50);
        sb.append("{");
        for (Iterator<Map.Entry<String, Object>> it = monoValuedParamMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue().toString());
            if (it.hasNext()) {
                sb.append(",");
            }
        }

        if (!multiValuedParamMap.isEmpty()) {
            sb.append(",");

            for (Iterator<Map.Entry<String, List<?>>> it1 = multiValuedParamMap.entrySet().iterator(); it1.hasNext();) {
                Map.Entry<String, List<?>> entry = it1.next();
                sb.append(entry.getKey());
                sb.append("=");

                for (Iterator<?> it2 = entry.getValue().iterator(); it2.hasNext();) {
                    sb.append(it2.next().toString());
                    if (it2.hasNext()) {
                        sb.append(",");
                    }
                }
                if (it1.hasNext()) {
                    sb.append(",");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }

}
