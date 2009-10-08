package fr.cls.atoll.motu.processor.iso19139;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.isotc211.iso19139.d_2006_05_04.gmd.CIOnlineResourcePropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterDirectionType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterType;
import org.joda.time.DateTime;
import org.opengis.parameter.ParameterValue;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.processor.wps.framework.WPSFactory;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.10 $ - $Date: 2009-10-08 14:33:36 $
 */
public class OperationMetadata {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(OperationMetadata.class);

    /** The Constant XML_JAVA_CLASS_MAPPING. */
    public static final Map<String, Class<?>> XML_JAVA_CLASS_MAPPING = new HashMap<String, Class<?>>();

    static {
        // Each XML Schema simple type corresponds to a Java class.
        XML_JAVA_CLASS_MAPPING.put("string", java.lang.String.class);
        XML_JAVA_CLASS_MAPPING.put("boolean", java.lang.Boolean.class);
        XML_JAVA_CLASS_MAPPING.put("decimal", java.math.BigDecimal.class);
        XML_JAVA_CLASS_MAPPING.put("dateTime", org.joda.time.DateTime.class);
        XML_JAVA_CLASS_MAPPING.put("datetime", org.joda.time.DateTime.class);
        XML_JAVA_CLASS_MAPPING.put("time", org.joda.time.DateTime.class);
        XML_JAVA_CLASS_MAPPING.put("date", org.joda.time.DateTime.class);
        XML_JAVA_CLASS_MAPPING.put("duration", org.joda.time.Duration.class);
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

    /**
     * Instantiates a new operation metadata.
     */
    public OperationMetadata() {
    }

    /**
     * Instantiates a new operation metadata.
     * 
     * @param svOperationMetadataType the sv operation metadata type
     */
    public OperationMetadata(SVOperationMetadataType svOperationMetadataType) {
        this.svOperationMetadataType = svOperationMetadataType;
    }

    /** The sv operation metadata type. */
    SVOperationMetadataType svOperationMetadataType = null;

    /** The parameter value map. */
    Map<String, ParameterValue<?>> parameterValueMap = null;

    /** The operation name. */
    private String operationName = null;

    /** The invocation name. */
    private String invocationName = null;

    /**
     * Sets the invocation name.
     * 
     * @param invocationName the new invocation name
     */
    public void setInvocationName(String invocationName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInvocationName(String) - entering");
        }

        this.invocationName = invocationName;

        if (LOG.isDebugEnabled()) {
            LOG.debug("setInvocationName(String) - exiting");
        }
    }

    /**
     * Sets the operation name.
     * 
     * @param operationName the new operation name
     */
    public void setOperationName(String operationName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setOperationName(String) - entering");
        }

        this.operationName = operationName;

        if (LOG.isDebugEnabled()) {
            LOG.debug("setOperationName(String) - exiting");
        }
    }

    /**
     * Sets the sv operation metadata type.
     * 
     * @param svOperationMetadataType the new sv operation metadata type
     */
    public void setSvOperationMetadataType(SVOperationMetadataType svOperationMetadataType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSvOperationMetadataType(SVOperationMetadataType) - entering");
        }

        this.svOperationMetadataType = svOperationMetadataType;

        if (LOG.isDebugEnabled()) {
            LOG.debug("setSvOperationMetadataType(SVOperationMetadataType) - exiting");
        }
    }

    /**
     * Sets the parameter value map.
     * 
     * @param parameterValueMap the parameter value map
     */
    public void setParameterValueMap(Map<String, ParameterValue<?>> parameterValueMap) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setParameterValueMap(Map<String,ParameterValue<?>>) - entering");
        }

        this.parameterValueMap = parameterValueMap;

        if (LOG.isDebugEnabled()) {
            LOG.debug("setParameterValueMap(Map<String,ParameterValue<?>>) - exiting");
        }
    }

    /**
     * Gets the parameter value map.
     * 
     * @return the parameter value map
     */
    public Map<String, ParameterValue<?>> getParameterValueMap() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterValueMap() - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterValueMap() - exiting");
        }
        return parameterValueMap;
    }

    /**
     * Gets the sv operation metadata type.
     * 
     * @return the sv operation metadata type
     */
    public SVOperationMetadataType getSvOperationMetadataType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSvOperationMetadataType() - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getSvOperationMetadataType() - exiting");
        }
        return svOperationMetadataType;
    }

    /**
     * Gets the operation name.
     * 
     * @return the operation name
     */
    public String getOperationName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationName() - entering");
        }

        if (operationName != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getOperationName() - exiting");
            }
            return operationName;
        }

        if (this.getSvOperationMetadataType() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getOperationName() - exiting");
            }
            return null;
        }

        if (this.getSvOperationMetadataType().getOperationName() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getOperationName() - exiting");
            }
            return null;
        }
        if (this.getSvOperationMetadataType().getOperationName().getCharacterString() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getOperationName() - exiting");
            }
            return null;
        }
        String returnString = (String) this.getSvOperationMetadataType().getOperationName().getCharacterString().getValue();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationName() - exiting");
        }
        return returnString;
    }

    /**
     * Gets the invocation name.
     * 
     * @return the invocation name
     */
    public String getInvocationName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInvocationName() - entering");
        }

        if (invocationName != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getInvocationName() - exiting");
            }
            return invocationName;
        }

        if (this.getSvOperationMetadataType() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getInvocationName() - exiting");
            }
            return null;
        }

        if (this.getSvOperationMetadataType().getInvocationName() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getInvocationName() - exiting");
            }
            return null;
        }
        if (this.getSvOperationMetadataType().getInvocationName().getCharacterString() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getInvocationName() - exiting");
            }
            return null;
        }
        String returnString = (String) this.getSvOperationMetadataType().getInvocationName().getCharacterString().getValue();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInvocationName() - exiting");
        }
        return returnString;
    }

    /**
     * Gets the connect point.
     * 
     * @param index the index
     * 
     * @return the connect point
     * 
     * @throws MotuException the motu exception
     */
    public String getConnectPoint(int index) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getConnectPoint(int) - entering");
        }

        if (this.getSvOperationMetadataType() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getConnectPoint(int) - exiting");
            }
            return null;
        }

        if (this.getSvOperationMetadataType().getConnectPoint() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getConnectPoint(int) - exiting");
            }
            return null;
        }

        List<CIOnlineResourcePropertyType> connectPointList = this.getSvOperationMetadataType().getConnectPoint();

        if (index < 0) {
            throw new MotuException(String
                    .format("ERROR in OperationMetadata#getConnectPoint : Out of bounds index %d - Connect point list size is %d.",
                            index,
                            connectPointList.size()));
        }

        if (index >= connectPointList.size()) {
            throw new MotuException(String
                    .format("ERROR in OperationMetadata#getConnectPoint : Out of bounds index %d - Connect point list size is %d.",
                            index,
                            connectPointList.size()));
        }

        CIOnlineResourcePropertyType connectPoint = connectPointList.get(index);

        if (connectPoint == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getConnectPoint(int) - exiting");
            }
            return null;
        }

        if (connectPoint.getCIOnlineResource() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getConnectPoint(int) - exiting");
            }
            return null;
        }
        if (connectPoint.getCIOnlineResource().getLinkage() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getConnectPoint(int) - exiting");
            }
            return null;
        }

        String returnString = connectPoint.getCIOnlineResource().getLinkage().getURL();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getConnectPoint(int) - exiting");
        }
        return returnString;

    }

    /**
     * Gets the connect point.
     * 
     * @return the connect point
     * 
     * @throws MotuException the motu exception
     */
    public List<String> getConnectPoint() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getConnectPoint() - entering");
        }

        if (this.getSvOperationMetadataType() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getConnectPoint() - exiting");
            }
            return null;
        }

        if (this.getSvOperationMetadataType().getConnectPoint() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getConnectPoint() - exiting");
            }
            return null;
        }

        List<String> connectPointList = new ArrayList<String>();

        for (int index = 0; index < connectPointList.size(); index++) {

            String connectPoint = getConnectPoint(index);

            if (connectPoint == null) {
                continue;
            }

            connectPointList.add(connectPoint);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getConnectPoint() - exiting");
        }
        return connectPointList;

    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("toString() - entering");
        }

        String returnString = String.format("%s/%s", this.getOperationName(), this.getInvocationName());
        if (LOG.isDebugEnabled()) {
            LOG.debug("toString() - exiting");
        }
        return returnString;
    }

    /**
     * Equals.
     * 
     * @param object the object
     * @param equalsBuilder the equals builder
     */
    public void equals(Object object, EqualsBuilder equalsBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("equals(Object, EqualsBuilder) - entering");
        }

        if (object == null) {
            equalsBuilder.appendSuper(false);

            if (LOG.isDebugEnabled()) {
                LOG.debug("equals(Object, EqualsBuilder) - exiting");
            }
            return;
        }
        if (!(object instanceof OperationMetadata)) {
            equalsBuilder.appendSuper(false);

            if (LOG.isDebugEnabled()) {
                LOG.debug("equals(Object, EqualsBuilder) - exiting");
            }
            return;
        }
        if (this == object) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("equals(Object, EqualsBuilder) - exiting");
            }
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("equals(Object, EqualsBuilder) - exiting");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object object) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("equals(Object) - entering");
        }

        if (object == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("equals(Object) - exiting");
            }
            return false;
        }
        if (!(object instanceof OperationMetadata)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("equals(Object) - exiting");
            }
            return false;
        }
        if (this == object) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("equals(Object) - exiting");
            }
            return true;
        }
        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equals(object, equalsBuilder);
        boolean returnboolean = equalsBuilder.isEquals();
        if (LOG.isDebugEnabled()) {
            LOG.debug("equals(Object) - exiting");
        }
        return returnboolean;
    }

    /**
     * Hash code.
     * 
     * @param hashCodeBuilder the hash code builder
     */
    public void hashCode(HashCodeBuilder hashCodeBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hashCode(HashCodeBuilder) - entering");
        }

        hashCodeBuilder.append(this.getOperationName());
        hashCodeBuilder.append(this.getInvocationName());

        if (LOG.isDebugEnabled()) {
            LOG.debug("hashCode(HashCodeBuilder) - exiting");
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hashCode() - entering");
        }

        final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCode(hashCodeBuilder);
        int returnint = hashCodeBuilder.toHashCode();
        if (LOG.isDebugEnabled()) {
            LOG.debug("hashCode() - exiting");
        }
        return returnint;
    }

    /**
     * Gets the parameters.
     * 
     * @return the parameters
     */
    public List<SVParameterPropertyType> getParameters() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameters() - entering");
        }

        List<SVParameterPropertyType> returnList = svOperationMetadataType.getParameters();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameters() - exiting");
        }
        return returnList;
    }

    /**
     * Gets the parameter name.
     * 
     * @param parameterPropertyType the parameter property type
     * 
     * @return the parameter name
     * 
     * @throws MotuException the motu exception
     */
    public String getParameterName(SVParameterPropertyType parameterPropertyType) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterName(SVParameterPropertyType) - entering");
        }

        String returnString = getParameterName(parameterPropertyType.getSVParameter());
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterName(SVParameterPropertyType) - exiting");
        }
        return returnString;
    }

    /**
     * Gets the parameter name.
     * 
     * @param parameterType the parameter type
     * 
     * @return the parameter name
     * 
     * @throws MotuException the motu exception
     */
    public String getParameterName(SVParameterType parameterType) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterName(SVParameterType) - entering");
        }

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

        String returnString = (String) parameterType.getName().getAName().getCharacterString().getValue();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterName(SVParameterType) - exiting");
        }
        return returnString;

    }

    /**
     * Gets the parameter type.
     * 
     * @param paramName the param name
     * 
     * @return the parameter type
     * 
     * @throws MotuException the motu exception
     */
    public SVParameterType getParameterType(String paramName) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterType(String) - entering");
        }

        List<SVParameterPropertyType> parameterPropertyTypeList = svOperationMetadataType.getParameters();

        for (SVParameterPropertyType parameterPropertyType : parameterPropertyTypeList) {

            SVParameterType parameterType = parameterPropertyType.getSVParameter();

            String name = getParameterName(parameterType);
            if (name.equals(paramName)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getParameterType(String) - exiting");
                }
                return parameterType;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterType(String) - exiting");
        }
        return null;
    }

    /**
     * Gets the parameter direction.
     * 
     * @param parameterPropertyType the parameter property type
     * 
     * @return the parameter direction
     * 
     * @throws MotuException the motu exception
     */
    public SVParameterDirectionType getParameterDirection(SVParameterPropertyType parameterPropertyType) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterDirection(SVParameterPropertyType) - entering");
        }

        SVParameterDirectionType returnSVParameterDirectionType = getParameterDirection(parameterPropertyType.getSVParameter());
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterDirection(SVParameterPropertyType) - exiting");
        }
        return returnSVParameterDirectionType;
    }

    /**
     * Gets the parameter direction.
     * 
     * @param parameterType the parameter type
     * 
     * @return the parameter direction
     * 
     * @throws MotuException the motu exception
     */
    public SVParameterDirectionType getParameterDirection(SVParameterType parameterType) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterDirection(SVParameterType) - entering");
        }

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

        SVParameterDirectionType returnSVParameterDirectionType = SVParameterDirectionType.fromValue(parameterType.getDirection()
                .getSVParameterDirection().value());
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterDirection(SVParameterType) - exiting");
        }
        return returnSVParameterDirectionType;

    }

    /**
     * Gets the parameter value type.
     * 
     * @param parameterPropertyType the parameter property type
     * 
     * @return the parameter value type
     * 
     * @throws MotuException the motu exception
     */
    public String getParameterValueType(SVParameterPropertyType parameterPropertyType) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterValueType(SVParameterPropertyType) - entering");
        }

        String returnString = getParameterValueType(parameterPropertyType.getSVParameter());
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterValueType(SVParameterPropertyType) - exiting");
        }
        return returnString;
    }

    /**
     * Gets the parameter value type.
     * 
     * @param parameterType the parameter type
     * 
     * @return the parameter value type
     * 
     * @throws MotuException the motu exception
     */
    public String getParameterValueType(SVParameterType parameterType) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterValueType(SVParameterType) - entering");
        }

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
        String returnString = (String) parameterType.getValueType().getTypeName().getAName().getCharacterString().getValue();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParameterValueType(SVParameterType) - exiting");
        }
        return returnString;

    }

    /**
     * Creates the parameter values.
     * 
     * @param inParameter the in parameter
     * @param outParameter the out parameter
     * 
     * @return the map< string, parameter value<?>>
     * 
     * @throws MotuException the motu exception
     */
    public Map<String, ParameterValue<?>> createParameterValues(boolean inParameter, boolean outParameter) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createParameterValues(boolean, boolean) - entering");
        }

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

        if (LOG.isDebugEnabled()) {
            LOG.debug("createParameterValues(boolean, boolean) - exiting");
        }
        return parameterValueMap;

    }

    /**
     * Creates the parameter values.
     * 
     * @return the map< string, parameter value<?>>
     * 
     * @throws MotuException the motu exception
     */
    public Map<String, ParameterValue<?>> createParameterValues() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createParameterValues() - entering");
        }

        Map<String, ParameterValue<?>> returnMap = createParameterValues(true, true);
        if (LOG.isDebugEnabled()) {
            LOG.debug("createParameterValues() - exiting");
        }
        return returnMap;
    }
    public ParameterValue<?> createParameterValue(String paramName) throws MotuException {
        return createParameterValue(paramName, true);
    }
    /**
     * Creates the parameter value.
     * 
     * @param paramName the param name
     * 
     * @return the parameter value<?>
     * 
     * @throws MotuException the motu exception
     */
    public ParameterValue<?> createParameterValue(String paramName, boolean allowCollection) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createParameterValue(String) - entering");
        }

        SVParameterType parameterType = getParameterType(paramName);
        if (parameterType == null) {
            throw new MotuException(String.format("ERROR - ISO 19139 parameter '%s' unknown in operation : '%s')", paramName, getOperationName()));
        }
        ParameterValue<?> returnParameterValue = createParameterValue(parameterType, allowCollection);
        if (LOG.isDebugEnabled()) {
            LOG.debug("createParameterValue(String) - exiting");
        }
        return returnParameterValue;
    }

    /**
     * Creates the parameter value.
     * 
     * @param parameterType the parameter type
     * 
     * @return the parameter value<?>
     * 
     * @throws MotuException the motu exception
     */
    public ParameterValue<?> createParameterValue(SVParameterType parameterType) throws MotuException {
        return createParameterValue(parameterType, true);
    }
    public ParameterValue<?> createParameterValue(SVParameterType parameterType, boolean allowCollection) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createParameterValue(SVParameterType) - entering");
        }

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
        ParameterValue<?> returnParameterValue = null;
        
        if (parameterType.getRepeatability().isBoolean() && allowCollection) {
            returnParameterValue = WPSFactory.createParameter(paramName, Collection.class, null);

        } else {
            returnParameterValue = WPSFactory.createParameter(paramName, clazz, null);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("createParameterValue(SVParameterType) - exiting");
        }
        return returnParameterValue;
    }

    public ParameterValue<?> getParameterValue(String name) throws MotuException {
        ParameterValue<?> parameterValue = parameterValueMap.get(name);

        if (parameterValue == null) {
            throw new MotuException(String.format("Error in OperationMetadata#getParameterValue - Operation '%s' - Parameter '%s' not found",
                                                  getInvocationName(),
                                                  name));
        }

        return parameterValue;
    }

    public void setParameterValue(String name, Object value) throws MotuException {
        ParameterValue<?> parameterValue = getParameterValue(name);
        parameterValue.setValue(value);

    }

    public void setParameterValue(String name, int value) throws MotuException {
        Integer v = value;
        setParameterValue(name, v);

    }

    public void setParameterValue(String name, double value) throws MotuException {
        Double v = value;
        setParameterValue(name, v);
    }

    public void setParameterValue(String name, long value) throws MotuException {
        Long v = value;
        setParameterValue(name, v);
    }

    public void setParameterValue(String name, boolean value) throws MotuException {
        Boolean v = value;
        setParameterValue(name, v);

    }

//    public void setParameterValue(String name, double[] value) throws MotuException {
//        ParameterValue<?> parameterValue = getParameterValue(name);
//        parameterValue.setValue((Object)value);
//
//    }

    public void setParameterValue(String name, String value) throws MotuExceptionBase {
        ParameterValue<?> parameterValue = getParameterValue(name);

        Object v = value;
        
        
        final Class<?> type = parameterValue.getDescriptor().getValueClass();
        
        if (DateTime.class.equals(type)) {
            v = WPSFactory.StringToDateTime(value);
        }
        
        if (Collection.class.equals(type)) {
            v = new ArrayList<String>();
            ((ArrayList<String>) v).add(value);
        }

        parameterValue.setValue(v);

    }

    /**
     * Dump.
     */
    public void dump() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("dump() - entering");
        }

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
                LOG.error("dump()", e);

                // TODO Auto-generated catch block
                System.out.print(e.notifyException());
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("dump() - exiting");
        }
    }
}
