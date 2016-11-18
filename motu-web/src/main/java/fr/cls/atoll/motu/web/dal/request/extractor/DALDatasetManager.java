package fr.cls.atoll.motu.web.dal.request.extractor;

import java.io.IOException;

import fr.cls.atoll.motu.web.bll.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuNoVarException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableNotFoundException;
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
public class DALDatasetManager {

    private RequestProduct requestProduct;

    /**
     * Constructeur.
     * 
     * @param datasetBase
     */
    public DALDatasetManager(RequestProduct requestProduct_) {
        super();
        this.requestProduct = requestProduct_;
    }

    public void extractData() throws MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
            MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, MotuException, IOException {
        if (getRequestProduct().getProduct().isFtpMedia()) {
            new DatasetFileManager(requestProduct).extractData();
        } else {
            new DatasetGridManager(requestProduct).extractData();
        }
    }

    /**
     * Compute amount data size.
     * 
     * @return
     *
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     */
    public double getAmountDataSize() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException {
        if (getRequestProduct().getProduct().isFtpMedia()) {
            return new DatasetFileManager(requestProduct).computeAmountDataSize();
        } else {
            return new DatasetGridManager(requestProduct).computeAmountDataSize();
        }
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

}
