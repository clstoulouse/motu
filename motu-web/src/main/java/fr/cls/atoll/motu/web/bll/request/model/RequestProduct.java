package fr.cls.atoll.motu.web.bll.request.model;

import java.util.List;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.extractor.DALAbstractDatasetManager;
import fr.cls.atoll.motu.web.dal.request.extractor.DatasetFileManager;
import fr.cls.atoll.motu.web.dal.request.extractor.DatasetGridManager;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DataBaseExtractionTimeCounter;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

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
    private RequestProductParameters requestProductParameters;
    private ExtractionParameters extractionParameters;

    /** Last error encountered. */
    private String lastError = "";
    private String requestId;

    private DataBaseExtractionTimeCounter dataBaseExtractionTimeCounter = new DataBaseExtractionTimeCounter();
    private RequestDownloadStatus requestDownloadStatus;

    private DALAbstractDatasetManager datasetManager = null;

    /**
     * Constructeur.
     * 
     * @param p
     * @param createExtractionParameters
     * @throws MotuException
     */
    public RequestProduct(Product product) {
        this.product = product;
    }

    /**
     * Constructeur.
     * 
     * @param p
     * @param createExtractionParameters
     * @throws MotuException
     */
    public RequestProduct(Product product, ExtractionParameters extractionParameters) throws MotuException {
        this(product);
        this.extractionParameters = extractionParameters;
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
    public RequestProductParameters getRequestProductParameters() {
        return requestProductParameters;
    }

    /**
     * Valeur de dataSetBase.
     * 
     * @param dataSetBase nouvelle valeur.
     */
    public void setDataSetBase(RequestProductParameters dataSetBase) {
        this.requestProductParameters = dataSetBase;
    }

    /**
     * Checks for criteria date time.
     * 
     * @return true if datetime criteria have been set, false otherwise.
     */
    public boolean hasCriteriaDateTime() {
        if (requestProductParameters == null) {
            return false;
        }
        ExtractCriteriaDatetime extractCriteriaDatetime = requestProductParameters.findCriteriaDatetime();
        return extractCriteriaDatetime != null;
    }

    /**
     * Gets the criteria date time.
     * 
     * @return DateTime criteria, null if none.
     */
    public ExtractCriteriaDatetime getCriteriaDateTime() {
        if (requestProductParameters == null) {
            return null;
        }
        return requestProductParameters.findCriteriaDatetime();
    }

    /**
     * Checks for criteria lat lon.
     * 
     * @return true if Lat/Lon criteria have been set, false otherwise.
     */
    public boolean hasCriteriaLatLon() {
        if (requestProductParameters == null) {
            return false;
        }
        ExtractCriteriaLatLon extractCriteriaLatLon = requestProductParameters.findCriteriaLatLon();
        return extractCriteriaLatLon != null;
    }

    /**
     * Gets the criteria lat lon.
     * 
     * @return Lat/Lon criteria, null if none.
     */
    public ExtractCriteriaLatLon getCriteriaLatLon() {
        if (requestProductParameters == null) {
            return null;
        }
        return requestProductParameters.findCriteriaLatLon();
    }

    /**
     * Checks for criteria depth.
     * 
     * @return true if depth criteria have been set, false otherwise.
     */
    public boolean hasCriteriaDepth() {
        if (requestProductParameters == null) {
            return false;
        }
        ExtractCriteriaDepth extractCriteriaDepth = requestProductParameters.findCriteriaDepth();
        return extractCriteriaDepth != null;
    }

    /**
     * Gets the criteria depth.
     * 
     * @return Depth criteria, null if none.
     */
    public ExtractCriteriaDepth getCriteriaDepth() {
        if (requestProductParameters == null) {
            return null;
        }
        return requestProductParameters.findCriteriaDepth();
    }

    /**
     * a mapping for the specified variable to extract.
     *
     * @param varName key whose presence in this map is to be tested.
     * @return Returns if this product contains a specified variable to be extracted.
     */
    public boolean hasVariableToBeExtracted(String varName) {
        if (requestProductParameters == null) {
            return false;
        }
        if (requestProductParameters.getVariables() == null) {
            return false;
        }
        return requestProductParameters.getVariables().containsKey(varName);
    }

    /**
     * Removes variables from the dataset.
     * 
     * @param listVar list of variables to be removed.
     * 
     * @throws MotuException the motu exception
     */
    public void removeVariables(List<String> listVar) throws MotuException {
        if (requestProductParameters != null && !requestProductParameters.getVariables().isEmpty()) {
            requestProductParameters.getVariables().keySet().removeAll(listVar);
        }
    }

    /**
     * Creates a new dataset.
     * 
     * @throws MotuException
     *
     */
    private void initDataset() throws MotuException {
        requestProductParameters = new RequestProductParameters(this);

        getRequestProductParameters().addVariables(extractionParameters.getListVar(), getProduct());
        try {
            getRequestProductParameters().setCriteria(extractionParameters.getListTemporalCoverage(),
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

    /**
     * Valeur de dataBaseExtractionTimeCounter.
     * 
     * @return la valeur.
     */
    public DataBaseExtractionTimeCounter getDataBaseExtractionTimeCounter() {
        return dataBaseExtractionTimeCounter;
    }

    /**
     * Valeur de requestId.
     * 
     * @return la valeur.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Valeur de requestId.
     * 
     * @param requestId nouvelle valeur.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setExtractFilename(String extractFilename) {
        if (requestDownloadStatus != null) {
            requestDownloadStatus.setExtractFilename(extractFilename);
        }
    }

    public void setRequestDownloadStatus(RequestDownloadStatus requestDownloadStatus) {
        this.requestDownloadStatus = requestDownloadStatus;
    }

    /**
     * Gets the value of datasetManager.
     *
     * @return the value of datasetManager
     */
    public DALAbstractDatasetManager getDatasetManager() {
        if (datasetManager == null) {
            if (getProduct().isFtpMedia()) {
                datasetManager = new DatasetFileManager(this);
            } else {
                datasetManager = new DatasetGridManager(this);
            }
        }
        return datasetManager;
    }
}
