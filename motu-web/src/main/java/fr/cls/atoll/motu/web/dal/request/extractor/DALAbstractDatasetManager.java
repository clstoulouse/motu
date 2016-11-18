package fr.cls.atoll.motu.web.dal.request.extractor;

import java.io.File;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;

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
public abstract class DALAbstractDatasetManager implements IDALDatasetManager {

    private RequestProduct requestProduct;

    /** The amount data size of a request in Megabytes. */
    protected double amountDataSize = 0d;

    /**
     * Constructeur.
     * 
     * @param requestProduct
     */
    public DALAbstractDatasetManager(RequestProduct requestProduct) {
        super();
        this.requestProduct = requestProduct;
    }

    /**
     * Valeur de requestProduct.
     * 
     * @return la valeur.
     */
    public RequestProduct getRequestProduct() {
        return requestProduct;
    }

    /**
     * Valeur de requestProduct.
     * 
     * @param requestProduct nouvelle valeur.
     */
    public void setRequestProduct(RequestProduct requestProduct) {
        this.requestProduct = requestProduct;
    }

    /**
     * Gets the amount data size.
     * 
     * @return the amount data size
     */
    public double getAmountDataSize() {
        return amountDataSize;
    }

    /**
     * Gets the amount data size as bytes.
     * 
     * @return the amount data size as bytes
     */
    public double getAmountDataSizeAsBytes() {
        return getAmountDataSizeAsKBytes() * 1024d;
    }

    /**
     * Gets the amount data size as Kilo-bytes.
     * 
     * @return the amount data size as Kilo-bytes
     */
    public double getAmountDataSizeAsKBytes() {
        return getAmountDataSize() * 1024d;
    }

    /**
     * Gets the amount data size as Mega-bytes.
     * 
     * @return the amount data size as Mega-bytes
     */
    public double getAmountDataSizeAsMBytes() {
        return getAmountDataSize();
    }

    /**
     * Gets the amount data size as Giga-bytes.
     * 
     * @return the amount data size as Giga-bytes
     */
    public double getAmountDataSizeAsGBytes() {
        return getAmountDataSize() / 1024d;
    }

    /**
     * Move temp extract file to final extract.
     * 
     * @throws MotuException the motu exception
     */
    public void moveTempExtractFileToFinalExtractFile() throws MotuException {
        // Temporary File
        String locationTmp = getRequestProduct().getDataSetBase().getExtractLocationDataTemp();
        File fileTemp = new File(locationTmp);

        // Final File
        String locationFinal = getRequestProduct().getDataSetBase().getExtractLocationData();
        File fileFinal = new File(locationFinal);

        // Rename file
        boolean success = fileTemp.renameTo(fileFinal);
        if (!success) {
            throw new MotuException(ErrorType.SYSTEM, String.format("Unable to rename file '%s' to file '%s'.", locationTmp, locationFinal));
        }
    }

}
