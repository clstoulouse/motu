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
package fr.cls.atoll.motu.library.misc.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class ServicePersistent {

    /**
     * Constructor.
     * 
     * @param name the name of the service
     */
    public ServicePersistent(String name) {
        this.name = name;
        init();

    }

    /**
     * Initialization.
     */
    private void init() {

        productsPersistent = new HashMap<String, ProductPersistent>();

    }

    /** Name of the service. */
    private String name;

    /**
     * Getter of the property <tt>name</tt>.
     * 
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter of the property <tt>name</tt>.
     * 
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /** The products persistent. */
    private Map<String, ProductPersistent> productsPersistent;

    /**
     * Getter of the property <tt>productsPersistent</tt>.
     * 
     * @return Returns the productsPersistent.
     */
    public Map<String, ProductPersistent> getProductsPersistent() {
        return this.productsPersistent;
    }

    /**
     * Returns a set view of the keys contained in this map.
     * 
     * @return a set view of the keys contained in this map.
     * @see java.util.Map#keySet()
     */
    public Set<String> productsPersistentKeySet() {
        return this.productsPersistent.keySet();
    }

    /**
     * Returns a collection view of the values contained in this map.
     * 
     * @return a collection view of the values contained in this map.
     * @see java.util.Map#values()
     */
    public Collection<ProductPersistent> productsPersistentValues() {
        return this.productsPersistent.values();
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     * 
     * @param key key whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     * @see java.util.Map#containsKey(Object)
     */
    public boolean productsPersistentContainsKey(String key) {
        return this.productsPersistent.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
     * 
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the specified value.
     * @see java.util.Map#containsValue(Object)
     */
    public boolean productsPersistentContainsValue(ProductPersistent value) {
        return this.productsPersistent.containsValue(value);
    }

    /**
     * Returns the value to which this map maps the specified key.
     * 
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or <tt>null</tt> if the map contains no
     *         mapping for this key.
     * @see java.util.Map#get(Object)
     */
    public ProductPersistent getProductsPersistent(Object key) {
        return this.productsPersistent.get(key);
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @return <tt>true</tt> if this map contains no key-value mappings.
     * @see java.util.Map#isEmpty()
     */
    public boolean isProductsPersistentEmpty() {
        return this.productsPersistent.isEmpty();
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return the number of key-value mappings in this map.
     * @see java.util.Map#size()
     */
    public int productsPersistentSize() {
        return this.productsPersistent.size();
    }

    /**
     * Setter of the property <tt>products</tt>.
     * 
     * @param value the productsPersistent to set.
     * 
     */
    public void setProductsPersistent(Map<String, ProductPersistent> value) {
        this.productsPersistent = value;
    }

    /**
     * Associates the specified value with the specified key in this map (optional operation).
     * 
     * @param value value to be associated with the specified key.
     * @param key key with which the specified value is to be associated.
     * 
     * @return previous value associated with specified key, or <tt>null</tt>
     * 
     * @see java.util.Map#put(Object,Object)
     */
    public ProductPersistent putProductsPersistent(String key, ProductPersistent value) {
        return this.productsPersistent.put(key, value);
    }

    /**
     * Removes the mapping for this key from this map if it is present (optional operation).
     * 
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.
     * @see java.util.Map#remove(Object)
     */
    public ProductPersistent removeProductsPersistent(String key) {
        return this.productsPersistent.remove(key);
    }

    /**
     * Removes all mappings from this map (optional operation).
     * 
     * @see java.util.Map#clear()
     */
    public void clearProductsPersistent() {
        this.productsPersistent.clear();
    }

}
