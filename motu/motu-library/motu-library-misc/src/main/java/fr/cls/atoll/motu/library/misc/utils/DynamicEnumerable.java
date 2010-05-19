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
 * @(#)DynamicEnumerable.java     9 Feb 2009
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
 * 
 */

package fr.cls.atoll.motu.library.misc.utils;

/**
 * Indicates that an implementing class belongs to a fixed set that may be treated as an enumeration. Each
 * instance of the class gives rise to an &quot;enum-like&quot; constant that can be used as a convenient
 * representative for the underlying object.
 * <p>
 * Useful in situations when a Java <code>Enum</code> cannot be used, e.g. if the set of enumerated values is
 * not known in advance and/or may change at runtime.
 * <p>
 * If the enum constants are of a non-primitive type, instances of this class should preferably only be
 * obtainable from a {@link DynamicEnum} factory, to reduce the risk of invalid enum values.
 * <p>
 * As enum constants, instances of the class are implicitly comparable. To compare two instances <i><u>as enum
 * constants</u></i>, use {@link DynamicEnum#compare(Object, Object)}. For a <code>Comparable</code> enum
 * constant type, the results of <code>compare(e1, e2)</code> should ideally match
 * <code>e1.compareTo(e2)</code>, but this is not guaranteed.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @param <E> the type of the enum constants
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public interface DynamicEnumerable<E> {

    /**
     * Returns the name of this enumerated value, in analogy to {@link Enum#name()}. The returned name must be
     * non-<code>null</code>.
     * <p>
     * Implementing classes must ensure that, for two instances <code>v1</code> and <code>v2</code>,
     * <code>v1.name().equals(v2.name())</code> iff <code>v1.equals(v2)</code>.
     * 
     * @return the name of this enumerated value
     */
    String name();

    /**
     * Returns the &quot;convenience&quot; object representing this enumerated value. The returned enum value
     * must be non-<code>null</code>.
     * <p>
     * Implementing classes must ensure that, for two instances <code>v1</code> and <code>v2</code>,
     * <code>v1.enumValue().equals(v2.enumValue())</code> iff <code>v1.equals(v2)</code>.
     * 
     * @return the object representing the underlying enumerated value
     */
    E enumValue();
}
