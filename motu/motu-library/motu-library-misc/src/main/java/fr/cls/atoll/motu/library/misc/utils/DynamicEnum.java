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
/*
 * @(#)DynamicEnum.java     9 Feb 2009
 * 
 * Copyright © 2009 Andrew Phillips.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS FILE HAS BEEN MODIFIED BY CLS
 */

package fr.cls.atoll.motu.library.misc.utils;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A factory for values of a class that can be treated as a enumeration. The factory provides the
 * &quot;aggregate&quot; functionality inherent in a normal Java <code>Enum</code>, such as retrieval of all
 * known enumerated values of this type.
 * <p>
 * The underlying objects that give rise to the enumeration (which can be seen as a convenience representation
 * of these objects) should implement {@link DynamicEnumerable}.
 * <p>
 * Implementation of {@link DynamicEnum#compare(Object, Object)} should be aligned with the
 * &quot;natural&quot; ordering of the enum (as reflected by {@link #ordinal(Object)}).
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @param <E> the type of the enum values
 * @param <D> the type of the objects giving rise to the enum values
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public interface DynamicEnum<E, D extends DynamicEnumerable<E>> extends Comparator<E> {

    /**
     * Checks if an enum constant matching the given purported instance of the enum value class exists.
     * 
     * @param enumValue the instance of the enum value class that should be checked
     * @return <code>true</code> iff an enum constant equal to the given value exists.
     */
    boolean exists(E enumValue);

    /**
     * Returns the enum constant with the specified name, in analogy to {@link Enum#valueOf(Class, String)}.
     * <p>
     * The name must match the {@link DynamicEnumerable#name()} value of one of the enum constants.
     * 
     * @param name the name of the constant to return
     * @return the enum constant with the specified name
     * @throws IllegalArgumentException if no constant with the specified name exists
     * @throws NullPointerException if <code>name</code> is null
     */
    E valueOf(String name);

    /**
     * Returns all the enum constants of the enumeration, in the order of their
     * {@link #ordinal(DynamicEnumerable) ordinals}.
     * <p>
     * Analogous to the &quot;magic&quot; <code>values()</code> method provided by Java for each
     * <code>Enum</code> class.
     * 
     * @return all the enum constants in the order of their ordinals
     */
    List<E> values();

    /**
     * Returns the ordinal of the given enumeration constant, in analogy to {@link Enum#ordinal()}.
     * <p>
     * Implementing classes are free to decide on the ordering - and thus the ordinals - of the enumeration
     * values, provided that, for two enum values <code>e1</code> and <code>e2</code>,
     * <code>ordinal(e1) == ordinal(e2)</code> iff <code>e1.equals(e2)</code>.
     * 
     * @param enumValue the enum constant whose ordinal is required
     * @return the ordinal of the given enumeration constant
     * @throws IllegalArgumentException if no constant with the specified name exists
     * @throws NullPointerException if <code>enumValue</code> is null
     */
    int ordinal(E enumValue);

    /**
     * Creates an enum set initially containing all of the elements in the range defined by the two specified
     * endpoints, in analogy to {@link EnumSet#range(Enum, Enum)}.
     * <p>
     * The returned set will contain the endpoints themselves, which may be identical but must not be out of
     * order.
     * 
     * @param from the first element in the range
     * @param to the last element in the range
     * @return a set containing all of the elements in the range defined by the two specified endpoints
     * @throws IllegalArgumentException if either of the endpoints does not exist or
     *             <code>from.compareTo(to) &gt; 0</code>
     * @throws NullPointerException if <code>from</code> or <code>to</code> is null
     */
    Set<E> range(E from, E to);

    /**
     * Returns the object underlying the given enum value, i.e. the <code>DynamicEnumValue v</code> for which
     * <code>v.enumValue().equals(enumValue)</code>.
     * 
     * @param enumValue the enum value whose backing object is required
     * @return the backing object for the given enum value
     * @throws IllegalArgumentException if the given value is not an enum constant
     * @throws NullPointerException if <code>enumValue</code> is null
     */
    D backingValueOf(E enumValue);
}
