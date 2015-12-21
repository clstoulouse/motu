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
package fr.cls.atoll.motu.processor.jgraht;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class OperationRelationshipEdge<T> extends DefaultEdge {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(OperationRelationshipEdge.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7185722770182907863L;

    /** The param out start vertex. */
    protected Collection<T> paramOutStartVertex = null;

    /** The param in start vertex. */
    protected Collection<T> paramInStartVertex = null;

    /** The label. */
    protected String label;

    /**
     * Gets the label.
     * 
     * @return the label
     */
    public String getLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabel() - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabel() - exiting");
        }
        return label;
    }

    /**
     * Sets the label.
     * 
     * @param label the new label
     */
    public void setLabel(String label) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLabel(String) - entering");
        }

        this.label = label;

        if (LOG.isDebugEnabled()) {
            LOG.debug("setLabel(String) - exiting");
        }
    }

    /**
     * Sets the param out start vertex.
     * 
     * @param paramOutStartVertex the new param out start vertex
     */
    public void setParamOutStartVertex(Collection<T> paramOutStartVertex) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setParamOutStartVertex(Collection<T>) - entering");
        }

        this.paramOutStartVertex = paramOutStartVertex;

        if (LOG.isDebugEnabled()) {
            LOG.debug("setParamOutStartVertex(Collection<T>) - exiting");
        }
    }

    /**
     * Sets the param in start vertex.
     * 
     * @param paramInStartVertex the new param in start vertex
     */
    public void setParamInStartVertex(Collection<T> paramInStartVertex) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setParamInStartVertex(Collection<T>) - entering");
        }

        this.paramInStartVertex = paramInStartVertex;

        if (LOG.isDebugEnabled()) {
            LOG.debug("setParamInStartVertex(Collection<T>) - exiting");
        }
    }

    /**
     * Gets the param out start vertex.
     * 
     * @return the param out start vertex
     */
    public Collection<T> getParamOutStartVertex() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParamOutStartVertex() - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getParamOutStartVertex() - exiting");
        }
        return paramOutStartVertex;
    }

    /**
     * Gets the param in start vertex.
     * 
     * @return the param in start vertex
     */
    public Collection<T> getParamInStartVertex() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParamInStartVertex() - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getParamInStartVertex() - exiting");
        }
        return paramInStartVertex;
    }

    /**
     * Instantiates a new operation relationship edge.
     */
    public OperationRelationshipEdge() {
    }

    /**
     * Instantiates a new operation relationship edge.
     * 
     * @param paramInStartVertex the param in start vertex
     * @param paramOutStartVertex the param out start vertex
     */
    public OperationRelationshipEdge(Collection<T> paramInStartVertex, Collection<T> paramOutStartVertex) {
        this.paramInStartVertex = paramInStartVertex;
        this.paramOutStartVertex = paramOutStartVertex;
        this.label = "";
    }

    /**
     * Instantiates a new operation relationship edge.
     * 
     * @param paramInStartVertex the param in start vertex
     * @param paramOutStartVertex the param out start vertex
     * @param label the label
     */
    public OperationRelationshipEdge(Collection<T> paramInStartVertex, Collection<T> paramOutStartVertex, String label) {
        this.paramInStartVertex = paramInStartVertex;
        this.paramOutStartVertex = paramOutStartVertex;
        this.label = label;
    }

}
