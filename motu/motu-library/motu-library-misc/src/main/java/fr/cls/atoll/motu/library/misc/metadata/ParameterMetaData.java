package fr.cls.atoll.motu.library.misc.metadata;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ucar.nc2.Dimension;

/**
 * This class represents the metadata of a parameter (variable).
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */

public class ParameterMetaData {
    // CSOFF: StrictDuplicateCode : normal duplication code.

    /** Name of the parameter. */
    private String name = "";

    /**
     * Getter of the property <tt>name</tt>.
     * 
     * @return Returns the name.
     * 
     * @uml.property name="name"
     */
    public String getName() {
        return name;
    }

    // CSON: StrictDuplicateCode

    /**
     * Setter of the property <tt>name</tt>.
     * 
     * @param name The name to set.
     * 
     * @uml.property name="name"
     */
    public void setName(String name) {
        if (name == null) {
            return;
        }
        this.name = name;
    }

    /** Description. */
    private String label = "";

    /**
     * Getter of the property <tt>label</tt>.
     * 
     * @return Returns the label.
     * 
     * @uml.property name="label"
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setter of the property <tt>label</tt>.
     * 
     * @param label The label to set.
     * 
     * @uml.property name="label"
     */
    public void setLabel(String label) {
        if (label == null) {
            return;
        }
        this.label = label;
    }

    /** Unit. */
    private String unit = "";

    /**
     * Getter of the property <tt>unit</tt>.
     * 
     * @return Returns the unit.
     * 
     * @uml.property name="unit"
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Setter of the property <tt>unit</tt>.
     * 
     * @param unit The unit to set.
     * 
     * @uml.property name="unit"
     */
    public void setUnit(String unit) {
        if (unit == null) {
            return;
        }
        this.unit = unit;
    }

    /**
     * Default constructor.
     */
    public ParameterMetaData() {

    }

    /** type of the data contains in the parameter: longitude, latitude, date/time, data. */
    private String dataType = "";

    /**
     * Getter of the property <tt>dataType</tt>.
     * 
     * @return Returns the dataType.
     * 
     * @uml.property name="dataType"
     */
    public String getDataType() {
        return this.dataType;
    }

    /**
     * Setter of the property <tt>dataType</tt>.
     * 
     * @param dataType The dataType to set.
     * 
     * @uml.property name="dataType"
     */
    public void setDataType(String dataType) {
        if (dataType == null) {
            return;
        }
        this.dataType = dataType;
    }

    /** long unit name. */
    private String unitLong = "";

    /**
     * Getter of the property <tt>unitLong</tt>.
     * 
     * @return Returns the unitLong.
     * 
     * @uml.property name="unitLong"
     */
    public String getUnitLong() {
        return this.unitLong;
    }

    /**
     * Setter of the property <tt>unitLong</tt>.
     * 
     * @param unitLong The unitLong to set.
     * 
     * @uml.property name="unitLong"
     */
    public void setUnitLong(String unitLong) {
        if (unitLong == null) {
            return;
        }
        this.unitLong = unitLong;
    }

    /** Internatioanl standard name. */
    private String standardName = "";

    /**
     * Getter of the property <tt>standardName</tt>.
     * 
     * @return Returns the standardName.
     * 
     * @uml.property name="standardName"
     */
    public String getStandardName() {
        return this.standardName;
    }

    /**
     * Setter of the property <tt>standardName</tt>.
     * 
     * @param standardName The standardName to set.
     * 
     * @uml.property name="standardName"
     */
    public void setStandardName(String standardName) {
        if (standardName == null) {
            return;
        }
        this.standardName = standardName;
    }

    /** The long name. */
    private String longName = "";

    /**
     * Gets the long name.
     * 
     * @return the long name
     */
    public String getLongName() {
        return longName;
    }

    /**
     * Sets the long name.
     * 
     * @param longName the new long name
     */
    public void setLongName(String longName) {
        this.longName = longName;
    }

    /** The dimensions. */
    private List<Dimension> dimensions;

    /**
     * Getter of the property <tt>dimensions</tt>.
     * 
     * @return Returns the dimensions.
     * 
     * @uml.property name="dimensions"
     */
    public List<Dimension> getDimensions() {
        return this.dimensions;
    }

    /**
     * Returns the element at the specified position in this list.
     * 
     * @param i index of element to return.
     * 
     * @return the element at the specified position in this list.
     * 
     * @see java.util.List#get(int)
     * @uml.property name="dimensions"
     */
    public Dimension getDimensions(int i) {
        return (Dimension) this.dimensions.get(i);
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * 
     * @return an iterator over the elements in this list in proper sequence.
     * 
     * @see java.util.List#iterator()
     * @uml.property name="dimensions"
     */
    public Iterator<Dimension> dimensionsIterator() {
        return this.dimensions.iterator();
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     * 
     * @return <tt>true</tt> if this list contains no elements.
     * 
     * @see java.util.List#isEmpty()
     * @uml.property name="dimensions"
     */
    public boolean isDimensionsEmpty() {
        return this.dimensions.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * 
     * @param dimension element whose presence in this list is to be tested.
     * 
     * @return <tt>true</tt> if this list contains the specified element.
     * 
     * @see java.util.List#contains(Object)
     * @uml.property name="dimensions"
     */
    public boolean containsDimensions(Dimension dimension) {
        return this.dimensions.contains(dimension);
    }

    /**
     * Returns <tt>true</tt> if this list contains all of the elements of the specified collection.
     * 
     * @param elements collection to be checked for containment in this list.
     * 
     * @return <tt>true</tt> if this list contains all of the elements of the specified collection.
     * 
     * @see java.util.List#containsAll(Collection)
     * @uml.property name="dimensions"
     */
    public boolean containsAllDimensions(List<? extends Dimension> elements) {
        return this.dimensions.containsAll(elements);
    }

    /**
     * Returns the number of elements in this list.
     * 
     * @return the number of elements in this list.
     * 
     * @see java.util.List#size()
     * @uml.property name="dimensions"
     */
    public int dimensionsSize() {
        return this.dimensions.size();
    }

    /**
     * Returns an array containing all of the elements in this list in proper sequence.
     * 
     * @return an array containing all of the elements in this list in proper sequence.
     * 
     * @see java.util.List#toArray()
     * @uml.property name="dimensions"
     */
    public Dimension[] dimensionsToArray() {
        return this.dimensions.toArray(new Dimension[this.dimensions.size()]);
    }

    /**
     * Returns an array containing all of the elements in this list in proper sequence; the runtime type of
     * the returned array is that of the specified array.
     * 
     * @param a the array into which the elements of this list are to be stored.
     * 
     * @return an array containing all of the elements in this list in proper sequence.
     * 
     * @see java.util.List#toArray(Object[])
     * @uml.property name="dimensions"
     */
    public <T extends Dimension> T[] dimensionsToArray(T[] a) {
        return (T[]) this.dimensions.toArray(a);
    }

    /**
     * Inserts the specified element at the specified position in this list (optional operation).
     * 
     * @param index index at which the specified element is to be inserted.
     * @param dimension element to be inserted.
     * 
     * @see java.util.List#add(int,Object)
     * @uml.property name="dimensions"
     */
    public void addDimensions(int index, Dimension dimension) {
        this.dimensions.add(index, dimension);
    }

    /**
     * Appends the specified element to the end of this list (optional operation).
     * 
     * @param dimension element to be appended to this list.
     * 
     * @return <tt>true</tt> (as per the general contract of the <tt>Collection.add</tt> method).
     * 
     * @see java.util.List#add(Object)
     * @uml.property name="dimensions"
     */
    public boolean addDimensions(Dimension dimension) {
        return this.dimensions.add(dimension);
    }

    /**
     * Removes the element at the specified position in this list (optional operation).
     * 
     * @param index the index of the element to removed.
     * 
     * @return the element previously at the specified position.
     * 
     * @see java.util.List#remove(int)
     * @uml.property name="dimensions"
     */
    public Object removeDimensions(int index) {
        return this.dimensions.remove(index);
    }

    /**
     * Removes the first occurrence in this list of the specified element (optional operation).
     * 
     * @param dimension element to be removed from this list, if present.
     * 
     * @return <tt>true</tt> if this list contained the specified element.
     * 
     * @see java.util.List#remove(Object)
     * @uml.property name="dimensions"
     */
    public boolean removeDimensions(Dimension dimension) {
        return this.dimensions.remove(dimension);
    }

    /**
     * Removes all of the elements from this list (optional operation).
     * 
     * @see java.util.List#clear()
     * @uml.property name="dimensions"
     */
    public void clearDimensions() {
        this.dimensions.clear();
    }

    /**
     * Setter of the property <tt>dimensions</tt>.
     * 
     * @param dimensions the dimensions to set.
     * 
     * @uml.property name="dimensions"
     */
    public void setDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * Checks for dimensions.
     * 
     * @return true if at least one dimension.
     */
    public boolean hasDimensions() {
        return dimensionsSize() > 0;
    }

    /**
     * Gets the dimensions as string.
     * 
     * @return the dimensions as a string.
     */
    public String getDimensionsAsString() {
        StringBuffer buff = new StringBuffer();
        int count = dimensionsSize();
        if (count <= 0) {
            return "-";
        }
        buff.append("(");
        for (int i = 0; i < count; i++) {
            Dimension dim = this.dimensions.get(i);
            if (i > 0) {
                buff.append(", ");
            }
            buff.append(dim.getName());
        }
        buff.append(")");
        return buff.toString();
    }
}
