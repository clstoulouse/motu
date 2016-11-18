package fr.cls.atoll.motu.web.bll.request.model;

import java.util.List;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.common.utils.ListUtils;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DatasetBase;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.SelectData;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class RequestProduct {

    private Product product;
    private DatasetBase dataset;
    private ExtractionParameters extractionParameters;

    /** Last error encountered. */
    private String lastError = "";

    /**
     * Constructeur.
     * 
     * @param p
     * @param createExtractionParameters
     * @throws MotuException
     */
    public RequestProduct(Product product_) {
        this.product = product_;
    }

    /**
     * Constructeur.
     * 
     * @param p
     * @param createExtractionParameters
     * @throws MotuException
     */
    public RequestProduct(Product product_, ExtractionParameters extractionParameters_) throws MotuException {
        this(product_);
        this.extractionParameters = extractionParameters_;
        initDataset();
    }

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
     * Valeur de product.
     * 
     * @return la valeur.
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Valeur de product.
     * 
     * @param product nouvelle valeur.
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Valeur de dataSetBase.
     * 
     * @return la valeur.
     */
    public DatasetBase getDataSetBase() {
        return dataset;
    }

    /**
     * Valeur de dataSetBase.
     * 
     * @param dataSetBase nouvelle valeur.
     */
    public void setDataSetBase(DatasetBase dataSetBase) {
        this.dataset = dataSetBase;
    }

    /**
     * Checks for criteria date time.
     * 
     * @return true if datetime criteria have been set, false otherwise.
     */
    public boolean hasCriteriaDateTime() {
        if (dataset == null) {
            return false;
        }
        ExtractCriteriaDatetime extractCriteriaDatetime = dataset.findCriteriaDatetime();
        return extractCriteriaDatetime != null;
    }

    /**
     * Gets the criteria date time.
     * 
     * @return DateTime criteria, null if none.
     */
    public ExtractCriteriaDatetime getCriteriaDateTime() {
        if (dataset == null) {
            return null;
        }
        return dataset.findCriteriaDatetime();
    }

    /**
     * Checks for criteria lat lon.
     * 
     * @return true if Lat/Lon criteria have been set, false otherwise.
     */
    public boolean hasCriteriaLatLon() {
        if (dataset == null) {
            return false;
        }
        ExtractCriteriaLatLon extractCriteriaLatLon = dataset.findCriteriaLatLon();
        return extractCriteriaLatLon != null;
    }

    /**
     * Gets the criteria lat lon.
     * 
     * @return Lat/Lon criteria, null if none.
     */
    public ExtractCriteriaLatLon getCriteriaLatLon() {
        if (dataset == null) {
            return null;
        }
        return dataset.findCriteriaLatLon();
    }

    /**
     * Checks for criteria depth.
     * 
     * @return true if depth criteria have been set, false otherwise.
     */
    public boolean hasCriteriaDepth() {
        if (dataset == null) {
            return false;
        }
        ExtractCriteriaDepth extractCriteriaDepth = dataset.findCriteriaDepth();
        return extractCriteriaDepth != null;
    }

    /**
     * Gets the criteria depth.
     * 
     * @return Depth criteria, null if none.
     */
    public ExtractCriteriaDepth getCriteriaDepth() {
        if (dataset == null) {
            return null;
        }
        return dataset.findCriteriaDepth();
    }

    /**
     * a mapping for the specified variable to extract.
     *
     * @param varName key whose presence in this map is to be tested.
     * @return Returns if this product contains a specified variable to be extracted.
     */
    public boolean hasVariableToBeExtracted(String varName) {
        if (dataset == null) {
            return false;
        }
        if (dataset.getVariables() == null) {
            return false;
        }
        return dataset.getVariables().containsKey(varName);
    }

    /**
     * Add variables to the dataset. If dataset doesn't exist, it creates it. If variable already exists in
     * the dataset, it will be replaced.
     *
     * @param listVar list of variables to be added.
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void addVariables(List<String> listVar) throws MotuException, MotuNotImplementedException {
        dataset.addVariables(listVar);
    }

    /**
     * Updates variables into the dataset. - Adds new variables - Updates the variables which already exist -
     * Remove the variables from the dataset which are not any more in the list If dataset doesn't exist, it
     * creates it.
     *
     * @param listVar list of variables to be updated.
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void updateVariables(List<String> listVar) throws MotuException, MotuNotImplementedException {
        // if list of variables to extract is no set,
        // get all variables form this product
        if (ListUtils.isNullOrEmpty(listVar)) {
            listVar = getProduct().getVariables();
        }

        dataset.updateVariables(listVar);
    }

    /**
     * Removes variables from the dataset.
     * 
     * @param listVar list of variables to be removed.
     * 
     * @throws MotuException the motu exception
     */
    public void removeVariables(List<String> listVar) throws MotuException {
        if (dataset != null && !dataset.getVariables().isEmpty()) {
            dataset.getVariables().keySet().removeAll(listVar);
        }
    }

    /**
     * Update files.
     * 
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void updateFiles() throws MotuException, MotuNotImplementedException {
        dataset.updateFiles(getProduct().getDataFiles());
    }

    /**
     * Clear files.
     */
    public void clearFiles() {
        if (dataset == null) {
            return;
        }

        dataset.clearFiles();
    }

    /**
     * Sets the select data. If dataset doesn't exist, it creates it.
     *
     * @param selectData to be updated.
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void setSelectData(SelectData selectData) throws MotuException, MotuNotImplementedException {
        dataset.setSelectData(selectData);
    }

    /**
     * Creates a new dataset.
     * 
     * @throws MotuException
     *
     */
    private void initDataset() throws MotuException {
        dataset = new DatasetBase(getProduct());

        getDataSetBase().updateVariables(extractionParameters.getListVar());
        try {
            getDataSetBase().setCriteria(extractionParameters.getListTemporalCoverage(),
                                         extractionParameters.getListLatLonCoverage(),
                                         extractionParameters.getListDepthCoverage());
        } catch (MotuInvalidDateException | MotuInvalidDepthException | MotuInvalidLatitudeException | MotuInvalidLongitudeException e) {
            throw new MotuException(ErrorType.SYSTEM, e);
        }

    }

    /**
     * Valeur de extractionParameters.
     * 
     * @return la valeur.
     */
    public ExtractionParameters getExtractionParameters() {
        return extractionParameters;
    }

    /**
     * Valeur de extractionParameters.
     * 
     * @param extractionParameters nouvelle valeur.
     */
    public void setExtractionParameters(ExtractionParameters extractionParameters) {
        this.extractionParameters = extractionParameters;
    }

}
