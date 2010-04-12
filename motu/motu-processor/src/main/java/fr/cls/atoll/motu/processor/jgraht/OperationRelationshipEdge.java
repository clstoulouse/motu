package fr.cls.atoll.motu.processor.jgraht;

import org.apache.log4j.Logger;

import java.util.Collection;

import org.jgrapht.graph.DefaultEdge;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * 
 * @param <T>  * 
 * @author $Author: dearith $
 * @version $Revision: 1.3 $ - $Date: 2009-09-29 14:09:19 $
 */
public class OperationRelationshipEdge<T> extends  DefaultEdge {
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
