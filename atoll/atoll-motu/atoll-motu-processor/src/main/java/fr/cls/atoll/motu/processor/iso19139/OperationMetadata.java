package fr.cls.atoll.motu.processor.iso19139;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataType;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-09-15 14:28:53 $
 */
public class OperationMetadata  {

    public OperationMetadata(SVOperationMetadataType svOperationMetadataType) {
        super();
        this.svOperationMetadataType = svOperationMetadataType;
    }

    SVOperationMetadataType svOperationMetadataType = null;

    public SVOperationMetadataType getSvOperationMetadataType() {
        return svOperationMetadataType;
    }

    
    public String getOperationName() {
        
        if (this.getSvOperationMetadataType() == null) {
            return null;
        }

        if (this.getSvOperationMetadataType().getOperationName() == null) {
            return null;
        }
        if (this.getSvOperationMetadataType().getOperationName().getCharacterString() == null) {
            return null;
        }
        return (String) this.getSvOperationMetadataType().getOperationName().getCharacterString().getValue();
    }
    public String getInvocationName() {
        
        if (this.getSvOperationMetadataType() == null) {
            return null;
        }

        if (this.getSvOperationMetadataType().getInvocationName() == null) {
            return null;
        }
        if (this.getSvOperationMetadataType().getInvocationName().getCharacterString() == null) {
            return null;
        }
        return (String) this.getSvOperationMetadataType().getInvocationName().getCharacterString().getValue();
    }

    @Override
    public String toString() {
        return String.format("%s/%s", this.getOperationName(), this.getInvocationName());
    }

    public void equals(Object object, EqualsBuilder equalsBuilder) {
        if (object == null) {
            equalsBuilder.appendSuper(false);
            return;
        }
        if (!(object instanceof OperationMetadata)) {
            equalsBuilder.appendSuper(false);
            return;
        }
        if (this == object) {
            return;
        }
        final OperationMetadata that = ((OperationMetadata) object);
//        String thatOperationName = that.getOperationName();
//        String thisOperationName = this.getOperationName();

//        if (thatOperationName == null) {
//            equalsBuilder.appendSuper(false);
//            return;
//        }
//        if (thisOperationName == null) {
//            equalsBuilder.appendSuper(false);
//            return;
//        }

        equalsBuilder.append(this.getOperationName(), that.getOperationName());
        equalsBuilder.append(this.getInvocationName(), that.getInvocationName());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof OperationMetadata)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equals(object, equalsBuilder);
        return equalsBuilder.isEquals();
    }
    public void hashCode(HashCodeBuilder hashCodeBuilder) {
        hashCodeBuilder.append(this.getOperationName());
        hashCodeBuilder.append(this.getInvocationName());
        
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCode(hashCodeBuilder);
        return hashCodeBuilder.toHashCode();
    }


}
