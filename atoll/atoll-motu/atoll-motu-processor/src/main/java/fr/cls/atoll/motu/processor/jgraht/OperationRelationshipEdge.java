package fr.cls.atoll.motu.processor.jgraht;

import java.util.Collection;

import org.jgrapht.graph.DefaultEdge;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-09-22 14:39:04 $
 * @param <T>
 */
public class OperationRelationshipEdge<T> extends  DefaultEdge {

    private static final long serialVersionUID = -7185722770182907863L;
    
    protected Collection<T> paramOutStartVertex = null;
    protected Collection<T> paramInStartVertex = null;

    public void setParamOutStartVertex(Collection<T> paramOutStartVertex) {
        this.paramOutStartVertex = paramOutStartVertex;
    }

    public void setParamInStartVertex(Collection<T> paramInStartVertex) {
        this.paramInStartVertex = paramInStartVertex;
    }

    public Collection<T> getParamOutStartVertex() {
        return paramOutStartVertex;
    }

    public Collection<T> getParamInStartVertex() {
        return paramInStartVertex;
    }

    public OperationRelationshipEdge() {
    }

    public OperationRelationshipEdge(Collection<T> paramOutStartVertex, Collection<T> paramInStartVertex) {
        this.paramOutStartVertex = paramOutStartVertex;
        this.paramInStartVertex = paramInStartVertex;
    }

    
}
