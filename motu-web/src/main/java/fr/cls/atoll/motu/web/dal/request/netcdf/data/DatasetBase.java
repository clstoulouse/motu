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
package fr.cls.atoll.motu.web.dal.request.netcdf.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteria;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDatetime;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDepth;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaGeo;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfWriter;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.ma2.Range;
import ucar.unidata.geoloc.LatLonPointImpl;

/**
 * Dataset class. A dataset refers to one product.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class DatasetBase {

    /** The product. */
    protected Product product = null;

    /** The variables map. */
    private Map<String, VarData> variablesMap;

    /** The list criteria. */
    private List<DataFile> listFiles;

    /** The list criteria. */
    private List<ExtractCriteria> listCriteria;

    /** The output location path and file name. */
    private String extractFilename = "";

    /** The temporary output location path and file name. */
    private String extractFilenameTemp = "";

    private DataBaseExtractionTimeCounter dataBaseExtractionTimeCounter;

    /**
     * Constructor.
     * 
     * @param product product to work with
     */
    public DatasetBase(Product product) {
        variablesMap = new HashMap<String, VarData>();
        listCriteria = new ArrayList<ExtractCriteria>();
        dataBaseExtractionTimeCounter = new DataBaseExtractionTimeCounter();
        setProduct(product);
    }

    /**
     * Valeur de dataBaseExtractionTimeCounter.
     * 
     * @return la valeur.
     */
    public DataBaseExtractionTimeCounter getDataBaseExtractionTimeCounter() {
        return dataBaseExtractionTimeCounter;
    }

    /**
     * Valeur de dataBaseExtractionTimeCounter.
     * 
     * @param dataBaseExtractionTimeCounter nouvelle valeur.
     */
    public void setDataBaseExtractionTimeCounter(DataBaseExtractionTimeCounter dataBaseExtractionTimeCounter) {
        this.dataBaseExtractionTimeCounter = dataBaseExtractionTimeCounter;
    }

    /** Last error encountered. */
    private String lastError = "";

    /**
     * Getter of the property <tt>lastError</tt>.
     * 
     * @return Returns the lastError.
     * 
     * @uml.property name="lastError"
     */
    public String getLastError() {
        return this.lastError;
    }

    /**
     * Setter of the property <tt>lastError</tt>.
     * 
     * @param lastError The lastError to set.
     * 
     * @uml.property name="lastError"
     */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /**
     * Clears <tt>lastError</tt>.
     * 
     * @uml.property name="lastError"
     */
    public void clearLastError() {
        this.lastError = "";
    }

    /**
     * Checks for last error.
     * 
     * @return true last error message string is not empty, false otherwise.
     */
    public boolean hasLastError() {
        return !StringUtils.isNullOrEmpty(getLastError());
    }

    /**
     * Adds the variables.
     * 
     * @param vars the vars
     * @return the list
     * @throws MotuException the motu exception
     */
    public void addVariables(String... vars) throws MotuException {
        addVariables(Arrays.asList(vars));
    }

    /**
     * Adds variables into the dataset. If variable already exists in the dataset, it will be replaced.
     * 
     * @param listVar list of variables to be added.
     * 
     * @return list of added variables.
     * 
     * @throws MotuException the motu exception
     */
    public void addVariables(List<String> listVar) throws MotuException {
        if (listVar != null && listVar.size() > 0) {
            NetCdfReader netCdfReader = new NetCdfReader(product.getLocationData(), false);
            netCdfReader.open(true);
            for (String standardName : listVar) {
                String trimmedStandardName = standardName.trim();

                List<String> listVarName;
                try {
                    listVarName = netCdfReader.getNetcdfVarNameByStandardName(trimmedStandardName);
                } catch (NetCdfAttributeException e) {
                    throw new MotuException(ErrorType.NETCDF_VARIABLE, "Error in addVariables - Unable to get netcdf variable name", e);
                }
                for (String varName : listVarName) {
                    VarData varData = new VarData(varName);
                    varData.setStandardName(trimmedStandardName);
                    if (getVariables().keySet().contains(varData.getVarName())) {
                        getVariables().remove(varData.getVarName());
                    }
                    getVariables().put(varData.getVarName(), varData);
                }
            }
        }
    }

    /**
     * Updates variables into the dataset. - Adds new variables - Updates the variables which already exist -
     * Remove the variables from the dataset which are not any more in the list
     * 
     * @param listVar list of variables to be updated.
     * 
     * @throws MotuException the motu exception
     */
    public void updateVariables(List<String> listVar) throws MotuException {
        addVariables(listVar);
    }

    /**
     * Finds a type of depth criteria into the dataset list of criteria.
     * 
     * @return criteria found or null if not found.
     */
    public ExtractCriteriaDepth findCriteriaDepth() {
        return (ExtractCriteriaDepth) findCriteria(ExtractCriteriaDepth.class);
    }

    /**
     * Finds a type of GeoX/GeoY criteria into the dataset list of criteria.
     * 
     * @return criteria found or null if not found.
     */
    public ExtractCriteriaGeo findCriteriaGeo() {
        return (ExtractCriteriaGeo) findCriteria(ExtractCriteriaGeo.class);
    }

    /**
     * Finds a type of Lat/Lon criteria into the dataset list of criteria.
     * 
     * @return criteria found or null if not found.
     */
    public ExtractCriteriaLatLon findCriteriaLatLon() {
        return (ExtractCriteriaLatLon) findCriteria(ExtractCriteriaLatLon.class);
    }

    /**
     * Finds a type of datetime criteria into the dataset list of criteria.
     * 
     * @return criteria found or null if not found.
     */
    public ExtractCriteriaDatetime findCriteriaDatetime() {
        return (ExtractCriteriaDatetime) findCriteria(ExtractCriteriaDatetime.class);
    }

    /**
     * Finds a type of criteria into the dataset listCriteria list.
     * 
     * @param cls type of criteria class to find.
     * 
     * @return criteria found or null if not found.
     */
    public ExtractCriteria findCriteria(Class<? extends ExtractCriteria> cls) {
        ExtractCriteria criteriaFound = null;
        if (listCriteria != null) {
            for (ExtractCriteria c : this.listCriteria) {
                if (c.getClass().isAssignableFrom(cls)) {
                    criteriaFound = c;
                    break;
                }
            }
        }

        return criteriaFound;
    }

    /**
     * Finds a type of criteria into the dataset listCriteria list.
     * 
     * @param criteria type of criteria to find.
     * 
     * @return criteria found or null if not found.
     */
    public ExtractCriteria findCriteria(ExtractCriteria criteria) {
        return criteria != null ? findCriteria(criteria.getClass()) : null;
    }

    /**
     * Adds listCriteria into the dataset. If a criterion already exists in the dataset, it will be replaced
     * if replace is true.
     *
     * @param list list of criteria to be added.
     * @param replace if true and criteria of the same type already exists, they will be replaced
     * @throws MotuException the motu exception
     */
    public void addCriteria(List<ExtractCriteria> list, boolean replace) throws MotuException {
        if (list == null) {
            throw new MotuException(ErrorType.BAD_PARAMETERS, "Error in addCriteria - List of listCriteria to be added is null");
        }

        if (listCriteria == null) {
            listCriteria = new ArrayList<ExtractCriteria>();
        }

        ExtractCriteria criteriaFound = null;
        for (ExtractCriteria c : list) {
            criteriaFound = findCriteria(c);
            if ((criteriaFound != null) && replace) {
                getListCriteria().remove(criteriaFound);
            }
            getListCriteria().add(c);
        }
    }

    public void setCriteria(List<String> listTemporalCoverage, List<String> listLatLonCoverage, List<String> listDepthCoverage)
            throws MotuException, MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException, MotuInvalidLongitudeException {
        createCriteriaList(listTemporalCoverage, listLatLonCoverage, listDepthCoverage, listCriteria);
    }

    /**
     * Creates a list of {@link ExtractCriteria} objects.
     *
     * @param criteria list of criteria (geographical coverage, temporal coverage ...)
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     *
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuInvalidDateException the motu invalid date exception
     */
    private static void createCriteriaList(List<String> listTemporalCoverage,
                                           List<String> listLatLonCoverage,
                                           List<String> listDepthCoverage,
                                           List<ExtractCriteria> criteria) throws MotuInvalidDateException, MotuInvalidDepthException,
                                                   MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException {
        addCriteriaTemporal(listTemporalCoverage, criteria);
        addCriteriaLatLon(listLatLonCoverage, criteria);
        addCriteriaDepth(listDepthCoverage, criteria);
    }

    /**
     * Add a temporal criteria to a list of {@link ExtractCriteria} objects.
     *
     * @param criteria list of criteria (geographical coverage, temporal coverage ...), if null list is
     *            created
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     *
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     */
    private static void addCriteriaTemporal(List<String> listTemporalCoverage, List<ExtractCriteria> criteria)
            throws MotuInvalidDateException, MotuException {
        if (criteria != null && listTemporalCoverage != null) {
            if (!listTemporalCoverage.isEmpty()) {
                ExtractCriteriaDatetime c = new ExtractCriteriaDatetime();
                criteria.add(c);
                c.setValues(listTemporalCoverage);
            }
        }
    }

    /**
     * Add a geographical Lat/Lon criteria to a list of {@link ExtractCriteria} objects.
     *
     * @param criteria list of criteria (geographical coverage, temporal coverage ...), if null list is
     *            created
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     *
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuException the motu exception
     */
    private static void addCriteriaLatLon(List<String> listLatLonCoverage, List<ExtractCriteria> criteria)
            throws MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException {
        if (criteria != null && listLatLonCoverage != null) {
            if (!listLatLonCoverage.isEmpty()) {
                criteria.add(new ExtractCriteriaLatLon(listLatLonCoverage));
            }
        }
    }

    /**
     * Add a geographical Depth criteria to a list of {@link ExtractCriteria} objects.
     *
     * @param criteria list of criteria (geographical coverage, temporal coverage ...), if null list is
     *            created
     * @param listDepthCoverage list contains low depth, high depth.
     *
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuException the motu exception
     */
    private static void addCriteriaDepth(List<String> listDepthCoverage, List<ExtractCriteria> criteria)
            throws MotuInvalidDepthException, MotuException {
        if (criteria != null && listDepthCoverage != null) {
            if (!listDepthCoverage.isEmpty()) {
                criteria.add(new ExtractCriteriaDepth(listDepthCoverage));
            }
        }
    }

    /**
     * Update files.
     * 
     * @param list the list
     * 
     * @throws MotuException the motu exception
     */
    public void updateFiles(List<DataFile> list) throws MotuException {
        if (list != null) {
            if (listFiles == null) {
                listFiles = new ArrayList<DataFile>();
            }
            listFiles.clear();
            listFiles.addAll(list);
        }
    }

    /**
     * Get min/max the longitude from two ranges.
     *
     * @param r1 first Longitude range
     * @param r2 second Longitude range
     * @param r1Values first Longitude range values
     * @param r2Values second Longitude range values
     * @return Normalized Min/Max of the Longitude ranges values
     */
    public static MinMax getMinMaxLonNormal(Range r1, Range r2, double[] r1Values, double[] r2Values) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        if (r1.first() > r2.first()) {
            min = r1Values[0];
            max = r2Values[1];
            // double center = ((r1Values[0] != 0.) ? r1Values[0] : r1Values[1]);
            double center = r1Values[0] + 180;
            max = LatLonPointImpl.lonNormal(r2Values[1], center);

        } else {
            min = r2Values[0];
            max = r1Values[1];
        }

        return new MAMath.MinMax(min, max);

    }

    /**
     * Compute average from a variable. A new variable containing the result of calculation is created and
     * added to the variable's collection.
     *
     * @param variable variable to compute.
     * @param dimensions dimensions on which to apply average.
     */
    public void computeAverage(VarData variable, String dimensions) {

    }

    /**
     * Compute variance from a variable. A new variable containing the result of calculation is created and
     * added to the variable's collection.
     * 
     * @param variable variable to compute.
     */
    public void computeVariance(VarData variable) {

    }

    /**
     * Compute interpolation from a variable. A new variable containing the result of calculation is created
     * and added to the variable's collection.
     * 
     * @param variable variable to compute.
     */
    public void computeSubSampling(VarData variable) {

    }

    /** The select data. */
    private SelectData selectData;

    /**
     * Getter of the property <tt>selectData</tt>.
     * 
     * @return Returns the selectData.
     * 
     * @uml.property name="selectData"
     */
    public SelectData getSelectData() {
        return this.selectData;
    }

    /**
     * Setter of the property <tt>selectData</tt>.
     * 
     * @param selectData The selectData to set.
     * 
     * @uml.property name="selectData"
     */
    public void setSelectData(SelectData selectData) {
        this.selectData = selectData;
    }

    /**
     * Getter of the property <tt>variables</tt>.
     * 
     * @return Returns the variablesMap.
     * 
     * @uml.property name="variables"
     */
    public Map<String, VarData> getVariables() {
        return this.variablesMap;
    }

    /**
     * Getter of the property <tt>listCriteria</tt>.
     * 
     * @return Returns the listCriteria.
     * 
     * @uml.property name="listCriteria"
     */
    public List<ExtractCriteria> getListCriteria() {
        return this.listCriteria;
    }

    /**
     * Clear files.
     */
    public void clearFiles() {
        this.listFiles.clear();
    }

    /**
     * Getter of the property <tt>product</tt>.
     * 
     * @return Returns the product.
     * 
     * @uml.property name="product"
     */
    public Product getProduct() {
        return this.product;
    }

    /**
     * Setter of the property <tt>product</tt>.
     * 
     * @param product The product to set.
     * 
     * @uml.property name="product"
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * The Class RangeComparator.
     */
    public static class RangeComparator implements Comparator<Range> {

        /** The ascending. */
        private boolean ascending = true;

        /**
         * Instantiates a new range comparator.
         */
        public RangeComparator() {
        }

        /**
         * Instantiates a new range comparator.
         *
         * @param ascending the ascending
         */
        public RangeComparator(boolean ascending) {
            this.ascending = ascending;
        }

        /**
         * Compare.
         *
         * @param r1 the r1
         * @param r2 the r2
         * @return the int {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public int compare(Range r1, Range r2) {
            if (r1.first() > r2.first()) {
                if (ascending) {
                    return 1;
                } else {
                    return -1;
                }
            }
            if (r1.first() < r2.first()) {
                if (ascending) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return 0;
        }

    }

    /**
     * Getter of the property <tt>extractFilename</tt>.
     * 
     * @return Returns the extractFilename.
     * 
     * @uml.property name="extractFilename"
     */
    public String getExtractFilename() {
        return this.extractFilename;
    }

    /**
     * Setter of the property <tt>extractFilename</tt>.
     * 
     * @param extractFilename The extractFilename to set.
     * 
     * @uml.property name="extractFilename"
     */
    public void setExtractFilename(String extractFilename) {
        this.extractFilename = extractFilename;
        this.extractFilenameTemp = this.extractFilename + NetCdfWriter.NETCDF_FILE_EXTENSION_EXTRACT;
    }

    /**
     * Getter of the property <tt>extractFilenameTemp</tt>.
     * 
     * @return Returns the extractFilenameTemp.
     * 
     * @uml.property name="extractFilenameTemp"
     */
    public String getExtractFilenameTemp() {
        return this.extractFilenameTemp;
    }

    /**
     * Setter of the property <tt>extractFilenameTemp</tt>.
     * 
     * @param extractFilenameTemp The extractFilenameTemp to set.
     * 
     * @uml.property name="extractFilenameTemp"
     */
    public void setExtractFilenameTemp(String extractFilenameTemp) {
        this.extractFilenameTemp = extractFilenameTemp;
    }

    /**
     * Clears <tt>extractFilename</tt>.
     * 
     * @uml.property name="extractFilename"
     */
    public void clearExtractFilename() {
        this.extractFilename = "";
        this.extractFilenameTemp = "";
    }

    /**
     * Gets the output full file name (with path).
     * 
     * @return the output full file name (with path).
     * 
     */
    public String getExtractLocationData() {
        return Product.getExtractLocationData(extractFilename);
    }

    /**
     * Gets the output temporary full file name (with path).
     * 
     * @return the output temporary full file name (with path).
     * 
     * @throws MotuException the motu exception
     */
    public String getExtractLocationDataTemp() throws MotuException {

        if (extractFilenameTemp.length() <= 0) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Product.getExtractionPath());
        stringBuffer.append(extractFilenameTemp);

        return stringBuffer.toString();
    }

}
