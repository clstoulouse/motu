package fr.cls.atoll.motu.web.usl.wcs.data;

import java.math.BigInteger;
import java.util.List;

public class DescribeCoverageData {
    private String coverageId;
    private List<String> axisLabels;
    private List<String> uomLabels;
    private List<Double> lowerCorner;
    private List<Double> upperCorner;
    private List<BigInteger> lowerValues;
    private List<BigInteger> upperValues;
    private BigInteger dimension;
    private String gridId;
    private List<String> fieldNames;
    private List<String> fieldUoms;

    public DescribeCoverageData() {
        coverageId = "";
    }

    public String getCoverageId() {
        return coverageId;
    }

    public void setCoverageId(String coverageId) {
        this.coverageId = coverageId;
    }

    public List<String> getAxisLabels() {
        return axisLabels;
    }

    public void setAxisLabels(List<String> labels) {
        axisLabels = labels;
    }

    public List<String> getUomLabels() {
        return uomLabels;
    }

    public void setUomLabels(List<String> uomLabels) {
        this.uomLabels = uomLabels;
    }

    public List<Double> getLowerCorner() {
        return lowerCorner;
    }

    public void setLowerCorner(List<Double> lowerCorner) {
        this.lowerCorner = lowerCorner;
    }

    public List<Double> getUpperCorner() {
        return upperCorner;
    }

    public void setUpperCorner(List<Double> upperCorner) {
        this.upperCorner = upperCorner;
    }

    public List<BigInteger> getLowerValues() {
        return lowerValues;
    }

    public void setLowerValues(List<BigInteger> lowerValues) {
        this.lowerValues = lowerValues;
    }

    public List<BigInteger> getUpperValues() {
        return upperValues;
    }

    public void setUpperValues(List<BigInteger> upperValues) {
        this.upperValues = upperValues;
    }

    public BigInteger getDimension() {
        return dimension;
    }

    public void setDimension(BigInteger dimension) {
        this.dimension = dimension;
    }

    public String getGridId() {
        return gridId;
    }

    public void setGridId(String gridId) {
        this.gridId = gridId;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public List<String> getFieldUoms() {
        return fieldUoms;
    }

    public void setFieldUoms(List<String> fieldUoms) {
        this.fieldUoms = fieldUoms;
    }
}
