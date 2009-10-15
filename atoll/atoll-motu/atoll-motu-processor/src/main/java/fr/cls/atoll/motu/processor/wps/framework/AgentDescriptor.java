/*
 * @(#)AgentDescriptor.java     9 Feb 2009
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
 */
package fr.cls.atoll.motu.processor.wps.framework;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import fr.cls.atoll.motu.library.utils.DynamicEnumerable;


/**
 * A secret agent.
 * 
 * @author anphilli
 * @since 5 Feb 2009
 * 
 */
public class AgentDescriptor implements DynamicEnumerable<Integer> {
    private int codenumber;

    private String name;

    private boolean alive;

    /**
     * Creates an agent descriptor.
     * 
     * @param codenumber the agent's codenumber (00 number)
     * @param name the agent's name
     * @param alive <code>true</code> iff the agent is still alive
     */
    public AgentDescriptor(int codenumber, String name, boolean alive) {

        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }

        this.codenumber = codenumber;
        this.name = name;
        this.alive = alive;
    }

    public Integer enumValue() {
        return Integer.valueOf(codenumber);
    }

    public String name() {
        //return "00" + codenumber;
        return name;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(codenumber).append(name)
               .append(alive).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        } else if (!(obj instanceof AgentDescriptor)) {
            return false;
        }

        AgentDescriptor other = (AgentDescriptor) obj;
        return new EqualsBuilder().append(codenumber, other.codenumber)
               .append(name, other.name).append(alive, other.alive).isEquals();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
