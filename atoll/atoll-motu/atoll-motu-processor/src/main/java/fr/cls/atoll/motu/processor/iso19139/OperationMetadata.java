package fr.cls.atoll.motu.processor.iso19139;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.xerces.jaxp.datatype.DatatypeFactoryImpl;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterDirectionType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterType;
import org.kohsuke.rngom.dt.builtin.BuiltinDatatypeLibrary;
import org.kohsuke.rngom.dt.builtin.BuiltinDatatypeLibraryFactory;
import org.kohsuke.rngom.xml.util.WellKnownNamespaces;
import org.opengis.parameter.ParameterValue;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;

import com.sun.xml.bind.api.ClassResolver;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.opendap.server.DataType;
import fr.cls.atoll.motu.processor.wps.framework.WPSFactory;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.4 $ - $Date: 2009-09-23 14:21:08 $
 */
public class OperationMetadata {

    public static final Map<String, Class<?>> XML_JAVA_CLASS_MAPPING = new HashMap<String, Class<?>>();

    static {
        // Each XML Schema simple type corresponds to a Java class.
        XML_JAVA_CLASS_MAPPING.put("string", java.lang.String.class);
        XML_JAVA_CLASS_MAPPING.put("boolean", java.lang.Boolean.class);
        XML_JAVA_CLASS_MAPPING.put("decimal", java.math.BigDecimal.class);
        XML_JAVA_CLASS_MAPPING.put("dateTime", javax.xml.datatype.XMLGregorianCalendar.class);
        XML_JAVA_CLASS_MAPPING.put("datetime", javax.xml.datatype.XMLGregorianCalendar.class);
        XML_JAVA_CLASS_MAPPING.put("time", javax.xml.datatype.XMLGregorianCalendar.class);
        XML_JAVA_CLASS_MAPPING.put("date", javax.xml.datatype.XMLGregorianCalendar.class);
        XML_JAVA_CLASS_MAPPING.put("duration", javax.xml.datatype.Duration.class);
        XML_JAVA_CLASS_MAPPING.put("anyURI", java.net.URI.class);
        XML_JAVA_CLASS_MAPPING.put("Name", java.util.Date.class);
        XML_JAVA_CLASS_MAPPING.put("int", java.lang.Integer.class);
        XML_JAVA_CLASS_MAPPING.put("integer", java.lang.Integer.class);
        XML_JAVA_CLASS_MAPPING.put("long", java.lang.Long.class);
        XML_JAVA_CLASS_MAPPING.put("short", java.lang.Short.class);
        XML_JAVA_CLASS_MAPPING.put("float", java.lang.Float.class);
        XML_JAVA_CLASS_MAPPING.put("byte", java.lang.Byte.class);

        // custom type for bounding box : array of double number (latitude/longitude lower corner,
        // latitude/longitude upper corner).
        XML_JAVA_CLASS_MAPPING.put("boundingBox", double[].class);
        XML_JAVA_CLASS_MAPPING.put("boundingbox", double[].class);

    };

    public OperationMetadata() {
    }
    public OperationMetadata(SVOperationMetadataType svOperationMetadataType) {
        this.svOperationMetadataType = svOperationMetadataType;
    }

    SVOperationMetadataType svOperationMetadataType = null;

    Map<String, ParameterValue<?>> parameterValueMap = null;


    public void setSvOperationMetadataType(SVOperationMetadataType svOperationMetadataType) {
        this.svOperationMetadataType = svOperationMetadataType;
    }
    public void setParameterValueMap(Map<String, ParameterValue<?>> parameterValueMap) {
        this.parameterValueMap = parameterValueMap;
    }
    public Map<String, ParameterValue<?>> getParameterValueMap() {
        return parameterValueMap;
    }

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
        // String thatOperationName = that.getOperationName();
        // String thisOperationName = this.getOperationName();

        // if (thatOperationName == null) {
        // equalsBuilder.appendSuper(false);
        // return;
        // }
        // if (thisOperationName == null) {
        // equalsBuilder.appendSuper(false);
        // return;
        // }

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

    public List<SVParameterPropertyType> getParameters() {
        return svOperationMetadataType.getParameters();
    }

    public String getParameterName(SVParameterPropertyType parameterPropertyType) throws MotuException {
        return getParameterName(parameterPropertyType.getSVParameter());
    }

    public String getParameterName(SVParameterType parameterType) throws MotuException {
        if (parameterType == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter is null (operation : '%s')", getOperationName()));
        }
        if (parameterType.getName() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no name (operation : '%s')", getOperationName()));
        }
        if (parameterType.getName().getAName() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no name (operation : '%s')", getOperationName()));
        }
        if (parameterType.getName().getAName().getCharacterString() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no name (operation : '%s')", getOperationName()));
        }

        return (String) parameterType.getName().getAName().getCharacterString().getValue();

    }
    public SVParameterDirectionType getParameterDirection(SVParameterPropertyType parameterPropertyType) throws MotuException {
        return getParameterDirection(parameterPropertyType.getSVParameter());
    }
    public SVParameterDirectionType getParameterDirection(SVParameterType parameterType) throws MotuException {
        if (parameterType == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter is null (operation : '%s')", getOperationName()));
        }
        if (parameterType.getDirection() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no direction (operation : '%s')", getOperationName()));
        }

        if (parameterType.getDirection().getSVParameterDirection() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no direction (operation : '%s')", getOperationName()));
        }

        if (parameterType.getDirection().getSVParameterDirection().value() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no direction (operation : '%s')", getOperationName()));
        }
        if (parameterType.getName().getAName().getCharacterString() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no direction (operation : '%s')", getOperationName()));
        }

        return SVParameterDirectionType.fromValue(parameterType.getDirection().getSVParameterDirection().value());

    }

    public String getParameterValueType(SVParameterPropertyType parameterPropertyType) throws MotuException {
        return getParameterValueType(parameterPropertyType.getSVParameter());
    }

    public String getParameterValueType(SVParameterType parameterType) throws MotuException {
        if (parameterType == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter is null (operation : '%s')", getOperationName()));
        }
        if (parameterType.getValueType() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no value type (operation : '%s')", getOperationName()));
        }
        if (parameterType.getValueType().getTypeName() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no value type (operation : '%s')", getOperationName()));
        }
        if (parameterType.getValueType().getTypeName().getAName() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no value type (operation : '%s')", getOperationName()));
        }
        if (parameterType.getValueType().getTypeName().getAName() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no value type (operation : '%s')", getOperationName()));
        }
        if (parameterType.getValueType().getTypeName().getAName().getCharacterString() == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter has no value type (operation : '%s')", getOperationName()));
        }
        return (String) parameterType.getValueType().getTypeName().getAName().getCharacterString().getValue();

    }
    public Map<String, ParameterValue<?>> createParameterValues(boolean inParameter, boolean outParameter) throws MotuException {
        
        parameterValueMap = new HashMap<String, ParameterValue<?>>();

        List<SVParameterPropertyType> parameterPropertyTypeList = svOperationMetadataType.getParameters();

        for (SVParameterPropertyType parameterPropertyType : parameterPropertyTypeList) {

            SVParameterType parameterType = parameterPropertyType.getSVParameter();

            if (getParameterDirection(parameterType).equals(SVParameterDirectionType.IN) && !inParameter) {
                continue;
            }
            if (getParameterDirection(parameterType).equals(SVParameterDirectionType.OUT) && !outParameter) {
                continue;
            }
            
            String paramName = getParameterName(parameterType);

            ParameterValue<?> parameterValue = createParameterValue(parameterType);
            parameterValueMap.put(paramName, parameterValue);
        }

        return parameterValueMap;
        
    }
    public Map<String, ParameterValue<?>> createParameterValues() throws MotuException {
        return createParameterValues(true, true);
    }

    public ParameterValue<?> createParameterValue(SVParameterType parameterType) throws MotuException {
        String paramName = getParameterName(parameterType);
        String paramValueType = getParameterValueType(parameterType);
        Class<?> clazz = XML_JAVA_CLASS_MAPPING.get(paramValueType);
        if (clazz == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 - operation '%s' - parameter '%s' has unknown type '%s'. Valid type are : %s",
                                                  getOperationName(),
                                                  paramName,
                                                  paramValueType,
                                                  XML_JAVA_CLASS_MAPPING.keySet().toString()));
        }
        return WPSFactory.createParameter(paramName, clazz, null);
    }

    public void dump() {
        System.out.println("Operation:");
        System.out.print(" Name=");
        System.out.print(getOperationName());
        System.out.print(" InvocationName=");
        System.out.println(getInvocationName());
        List<SVParameterPropertyType> parameterPropertyTypeList = svOperationMetadataType.getParameters();

        for (SVParameterPropertyType parameterPropertyType : parameterPropertyTypeList) {
            System.out.println("Parameter:");
            SVParameterType parameterType = parameterPropertyType.getSVParameter();
            try {
                System.out.print(" Name=");
                System.out.print(getParameterName(parameterType));
                System.out.print(" ValueType=");
                System.out.println(getParameterValueType(parameterType));

                // BuiltinDatatypeLibraryFactory builtinDatatypeLibrary = new
                // BuiltinDatatypeLibraryFactory(new DatatypeLibraryLoader());
                // //DatatypeLibrary datatypeLibrary =
                // builtinDatatypeLibrary.createDatatypeLibrary(WellKnownNamespaces.XML_SCHEMA_DATATYPES);
                // DatatypeLibrary datatypeLibrary = builtinDatatypeLibrary.createDatatypeLibrary("");
                // Datatype dataType = datatypeLibrary.createDatatype("string");

            } catch (MotuException e) {
                // TODO Auto-generated catch block
                System.out.print(e.notifyException());
            }
        }
    }
}
