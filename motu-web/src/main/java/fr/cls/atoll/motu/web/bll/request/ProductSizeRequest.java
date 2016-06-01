package fr.cls.atoll.motu.web.bll.request;

import java.util.ArrayList;
import java.util.List;

import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteria;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDatetime;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDepth;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class ProductSizeRequest {

    public static void computeAmountDataSize(Product product,
                                             List<String> listVar,
                                             List<String> listTemporalCoverage,
                                             List<String> listLatLonCoverage,
                                             List<String> listDepthCoverage) throws MotuInvalidDateException, MotuInvalidDepthException,
                                                     MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException,
                                                     MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
                                                     MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException, NetCdfVariableException,
                                                     MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException {

        product.clearExtractFilename();
        product.clearLastError();

        // Converts criteria
        List<ExtractCriteria> criteria = new ArrayList<ExtractCriteria>();
        createCriteriaList(listTemporalCoverage, listLatLonCoverage, listDepthCoverage, criteria);

        computeAmountDataSize(product, listVar, criteria);

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
    public static void createCriteriaList(List<String> listTemporalCoverage,
                                          List<String> listLatLonCoverage,
                                          List<String> listDepthCoverage,
                                          List<ExtractCriteria> criteria) throws MotuInvalidDateException, MotuInvalidDepthException,
                                                  MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException {

        if (criteria == null) {
            throw new MotuException("Error in ServiceData createCriteriaList - criteria is null");
        }

        // distinct try/catch exception to add all correct criteria
        // and to avoid to stop on the first criteria exception
        MotuInvalidDateException exceptionDate = null;
        MotuInvalidLatitudeException exceptionLat = null;
        MotuInvalidLongitudeException exceptionLon = null;
        MotuInvalidDepthException exceptionDepth = null;

        // adds temporal criteria
        try {
            addCriteriaTemporal(listTemporalCoverage, criteria);
        } catch (MotuInvalidDateException e) {
            exceptionDate = e;
        }

        // adds geographical Lat/Lon criteria
        try {
            addCriteriaLatLon(listLatLonCoverage, criteria);
        } catch (MotuInvalidLatitudeException e) {
            exceptionLat = e;
        } catch (MotuInvalidLongitudeException e) {
            exceptionLon = e;
        }

        // adds geographical Depth criteria
        try {
            addCriteriaDepth(listDepthCoverage, criteria);
        } catch (MotuInvalidDepthException e) {
            exceptionDepth = e;
        }

        if (exceptionDate != null) {
            throw exceptionDate;
        } else if (exceptionLat != null) {
            throw exceptionLat;
        } else if (exceptionLon != null) {
            throw exceptionLon;
        } else if (exceptionDepth != null) {
            throw exceptionDepth;
        }
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
    public static void addCriteriaTemporal(List<String> listTemporalCoverage, List<ExtractCriteria> criteria)
            throws MotuInvalidDateException, MotuException {
        if (criteria == null) {
            throw new MotuException("Error in ServiceData addCriteriaTemporal - criteria is null");
        }
        if (listTemporalCoverage != null) {
            if (!listTemporalCoverage.isEmpty()) {
                ExtractCriteriaDatetime c = null;
                c = new ExtractCriteriaDatetime();
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
    public static void addCriteriaLatLon(List<String> listLatLonCoverage, List<ExtractCriteria> criteria)
            throws MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException {
        if (criteria == null) {
            throw new MotuException("Error in ServiceData addCriteriaLatLon - criteria is null");
        }

        if (listLatLonCoverage != null) {
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
    public static void addCriteriaDepth(List<String> listDepthCoverage, List<ExtractCriteria> criteria)
            throws MotuInvalidDepthException, MotuException {
        if (criteria == null) {
            throw new MotuException("Error in ServiceData addCriteriaDepth - criteria is null");
        }
        if (listDepthCoverage != null) {
            if (!listDepthCoverage.isEmpty()) {
                criteria.add(new ExtractCriteriaDepth(listDepthCoverage));
            }
        }
    }

    /**
     * Compute amount data size.
     * 
     * @param product the product
     * @param listVar the list var
     * @param criteria the criteria
     * 
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public static void computeAmountDataSize(Product product, List<String> listVar, List<ExtractCriteria> criteria) throws MotuException,
            MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidDepthRangeException,
            MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException, NetCdfVariableNotFoundException {

        if (product == null) {
            throw new MotuException("Error in extractData - product is null");
        }

        // updates variables collection to download
        updateVariables(product, listVar);

        // updates criteria collection
        updateCriteria(product, criteria);

        product.computeAmountDataSize();

    }

    /**
     * Updates the variable collection to download.
     * 
     * @param product instance of the product to extract.
     * @param listVar list of variables (parameters) or expressions to extract.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public static void updateVariables(Product product, List<String> listVar) throws MotuException, MotuNotImplementedException {
        if (product == null) {
            throw new MotuException("Error in updateVariables - product is null");
        }
        product.updateVariables(listVar);
    }

    /**
     * Updates the variable collection to download.
     * 
     * @param product instance of the product to extract.
     * @param criteria list of criteria (geographical coverage, temporal coverage ...)
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public static void updateCriteria(Product product, List<ExtractCriteria> criteria) throws MotuException, MotuNotImplementedException {
        if (product == null) {
            throw new MotuException("Error in updateCriteria - product is null");
        }

        if (criteria == null) {
            product.clearCriteria();
        } else {
            product.updateCriteria(criteria);
        }
    }

}
