package fr.cls.atoll.motu.web.usl.wcs.request.actions;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ParameterMetaData;
import fr.cls.atoll.motu.web.usl.request.actions.AbstractAction;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.wcs.Utils;
import fr.cls.atoll.motu.web.usl.wcs.data.DescribeCoverageData;
import fr.cls.atoll.motu.web.usl.wcs.data.DescribeCoveragesData;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.WCSHTTPParameters;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.CoverageIdHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.RequestHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.VersionHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.responses.DescribeCoverage;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;

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
public class WCSDescribeCoverageAction extends AbstractAction {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "DescribeCoverage";

    private static final String DATE_DESCRIPTION = "date in seconds since 1970, 1 jan";

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;
    private VersionHTTPParameterValidator versionHTTPParameterValidator;
    private RequestHTTPParameterValidator requestHTTPParameterValidator;
    private CoverageIdHTTPParameterValidator coverageIdHTTPParameterValidator;

    /**
     * Constructeur.
     * 
     * @param actionName_
     * @param actionCode_
     * @param request_
     * @param response_
     */
    public WCSDescribeCoverageAction(String actionCode_, HttpServletRequest request_, HttpServletResponse response_) {
        super(ACTION_NAME, actionCode_, request_, response_);
        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                WCSHTTPParameters.SERVICE,
                WCSHTTPParameters.getServiceFromRequest(getRequest()));
        versionHTTPParameterValidator = new VersionHTTPParameterValidator(
                WCSHTTPParameters.ACCEPT_VERSIONS,
                WCSHTTPParameters.getAcceptVersionsFromRequest(getRequest()));
        requestHTTPParameterValidator = new RequestHTTPParameterValidator(
                WCSHTTPParameters.REQUEST,
                WCSHTTPParameters.getRequestFromRequest(getRequest()));
        coverageIdHTTPParameterValidator = new CoverageIdHTTPParameterValidator(
                WCSHTTPParameters.COVERAGE_ID,
                WCSHTTPParameters.getCoverageIdFromRequest(getRequest()));
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        serviceHTTPParameterValidator.validate();
        versionHTTPParameterValidator.validate();
        requestHTTPParameterValidator.validate();
        coverageIdHTTPParameterValidator.validate();
    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws MotuException {
        String coverageIds = coverageIdHTTPParameterValidator.getParameterValueValidated();
        if (coverageIds.length() != 0) {
            String[] coverageList = coverageIds.split(",");
            List<DescribeCoverageData> listOfCoverageDescriptionData = new ArrayList<>();
            boolean onError = false;
            String coverageOnError = "";

            for (String coverageId : coverageList) {
                String[] coverageIdSplited = coverageId.split("@");
                if (coverageIdSplited.length == 2) {
                    String serviceName = coverageIdSplited[0];
                    String productId = coverageIdSplited[1];
                    DescribeCoverageData currentCoverageDescriptionData = buildCoverageDescription(coverageId, serviceName, productId);
                    if (currentCoverageDescriptionData != null) {
                        listOfCoverageDescriptionData.add(currentCoverageDescriptionData);
                    } else {
                        onError = true;
                        coverageOnError = coverageId;
                        break;
                    }
                } else {
                    onError = true;
                    coverageOnError = coverageId;
                    break;
                }
            }

            if (!onError) {
                DescribeCoveragesData currentDescribeCoverageData = new DescribeCoveragesData();
                currentDescribeCoverageData.setCoverageDescriptions(listOfCoverageDescriptionData);

                String xmlResponses;
                try {
                    xmlResponses = DescribeCoverage.getInstance().buildResponse(currentDescribeCoverageData);
                    getResponse().getWriter().write(xmlResponses);
                } catch (JAXBException | IOException e) {
                    throw new MotuException(ErrorType.SYSTEM, e);
                }
            } else {
                noSuchCoverageError(coverageOnError);
            }
        } else {
            emptyCoverageIdListError();
        }
    }

    private DescribeCoverageData buildCoverageDescription(String coverageId, String serviceName, String productId) throws MotuException {
        DescribeCoverageData coverageDescriptions = null;

        ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(serviceName);

        if (cs != null) {
            String catalogType;
            catalogType = BLLManager.getInstance().getCatalogManager().getCatalogType(cs);
            Product product = BLLManager.getInstance().getCatalogManager().getProductManager().getProduct(cs.getName(), productId);
            if (product != null) {
                switch (catalogType.toUpperCase()) {
                case "FILE":
                case "FTP":
                    coverageDescriptions = buildDGFDescribeCoverage(coverageId, product);
                    break;
                case "TDS":
                    coverageDescriptions = buildNetCDFDescribecoverage(coverageId, product);
                    break;
                default:
                    break;
                }
            }
        }

        return coverageDescriptions;

    }

    private void noSuchCoverageError(String coverageId) throws MotuException {
        Utils.onError(getResponse(), getActionCode(), coverageId, Constants.NO_SUCH_COVERAGE_CODE, ErrorType.WCS_NO_SUCH_COVERAGE, coverageId);
    }

    private void emptyCoverageIdListError() throws MotuException {
        Utils.onError(getResponse(), getActionCode(), Constants.EMPTY_COVERAGE_ID_LIST_CODE, ErrorType.WCS_EMPTY_COVERAGE_ID_LIST);
    }

    private fr.cls.atoll.motu.web.usl.wcs.data.DescribeCoverageData buildDGFDescribeCoverage(String coverageId, Product product)
            throws MotuException {
        List<String> labels = new ArrayList<>();
        List<String> uomLabels = new ArrayList<>();
        List<Double> lowersCorner = new ArrayList<>();
        List<Double> upperCorner = new ArrayList<>();
        List<BigInteger> lowerValues = new ArrayList<>();
        List<BigInteger> upperValues = new ArrayList<>();

        labels.add(Constants.TIME_AXIS.name());
        uomLabels.add(DATE_DESCRIPTION);

        lowersCorner.add(Long.valueOf(product.getProductMetaData().getTimeCoverage().getStart().getMillis() / 1000).doubleValue());
        upperCorner.add(Long.valueOf(product.getProductMetaData().getTimeCoverage().getEnd().getMillis() / 1000).doubleValue());
        lowerValues.add(BigInteger.valueOf(product.getProductMetaData().getTimeCoverage().getStart().getMillis() / 1000));
        upperValues.add(BigInteger.valueOf(product.getProductMetaData().getTimeCoverage().getEnd().getMillis() / 1000));

        List<String> fieldNames = new ArrayList<>();
        List<String> fieldUoms = new ArrayList<>();

        fr.cls.atoll.motu.web.usl.wcs.data.DescribeCoverageData coverageDescription = new fr.cls.atoll.motu.web.usl.wcs.data.DescribeCoverageData();
        coverageDescription.setAxisLabels(labels);
        coverageDescription.setCoverageId(coverageId);
        coverageDescription.setLowerCorner(lowersCorner);
        coverageDescription.setUpperCorner(upperCorner);
        coverageDescription.setLowerValues(lowerValues);
        coverageDescription.setUpperValues(upperValues);
        coverageDescription.setUomLabels(uomLabels);
        coverageDescription.setFieldNames(fieldNames);
        coverageDescription.setFieldUoms(fieldUoms);
        coverageDescription.setDimension(BigInteger.valueOf(labels.size()));
        coverageDescription.setGridId("Grid000");

        return coverageDescription;
    }

    private fr.cls.atoll.motu.web.usl.wcs.data.DescribeCoverageData buildNetCDFDescribecoverage(String coverageId, Product product)
            throws MotuException {
        List<String> labels = new ArrayList<>();
        List<String> uomLabels = new ArrayList<>();
        List<Double> lowersCorner = new ArrayList<>();
        List<Double> upperCorner = new ArrayList<>();
        List<BigInteger> lowerValues = new ArrayList<>();
        List<BigInteger> upperValues = new ArrayList<>();

        // p.getTimeAxisData()

        for (AxisType currentAxisType : Constants.SUBSETTER_AVAILABLE_AXIS) {
            CoordinateAxis currentCoordinateAxis = product.getProductMetaData().getCoordinateAxes().get(currentAxisType);
            if (currentCoordinateAxis != null) {
                BigInteger minValue = BigInteger
                        .valueOf(BigDecimal.valueOf(currentCoordinateAxis.getMinValue()).setScale(0, BigDecimal.ROUND_HALF_DOWN).intValue());
                BigInteger maxValue = BigInteger
                        .valueOf(BigDecimal.valueOf(currentCoordinateAxis.getMaxValue()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
                labels.add(currentAxisType.name());
                uomLabels.add(currentCoordinateAxis.getDimensionsString());
                lowersCorner.add(minValue.doubleValue());
                upperCorner.add(maxValue.doubleValue());
                lowerValues.add(minValue);
                upperValues.add(maxValue);
            }
        }

        CoordinateAxis currentCoordinateAxis = product.getProductMetaData().getCoordinateAxes().get(AxisType.Time);
        labels.add(Constants.TIME_AXIS.name());
        uomLabels.add(DATE_DESCRIPTION);
        currentCoordinateAxis.getMinValue();
        currentCoordinateAxis.getDescription();
        lowersCorner.add((double) product.getProductMetaData().getTimeAxisMinValue().getTime() / 1000);
        upperCorner.add((double) product.getProductMetaData().getTimeAxisMaxValue().getTime() / 1000);
        lowerValues.add(BigInteger.valueOf(product.getProductMetaData().getTimeAxisMinValue().getTime() / 1000));
        upperValues.add(BigInteger.valueOf(product.getProductMetaData().getTimeAxisMaxValue().getTime() / 1000));

        Map<String, ParameterMetaData> parameters = product.getProductMetaData().getParameterMetaDatas();

        List<String> fieldNames = new ArrayList<>();
        List<String> fieldUoms = new ArrayList<>();

        for (Map.Entry<String, ParameterMetaData> parameter : parameters.entrySet()) {
            fieldNames.add(parameter.getValue().getName());
            fieldUoms.add(parameter.getValue().getUnit());
        }

        fr.cls.atoll.motu.web.usl.wcs.data.DescribeCoverageData coverageDescription = new fr.cls.atoll.motu.web.usl.wcs.data.DescribeCoverageData();
        coverageDescription.setAxisLabels(labels);
        coverageDescription.setCoverageId(coverageId);
        coverageDescription.setLowerCorner(lowersCorner);
        coverageDescription.setUpperCorner(upperCorner);
        coverageDescription.setLowerValues(lowerValues);
        coverageDescription.setUpperValues(upperValues);
        coverageDescription.setUomLabels(uomLabels);
        coverageDescription.setFieldNames(fieldNames);
        coverageDescription.setFieldUoms(fieldUoms);
        coverageDescription.setDimension(BigInteger.valueOf(labels.size()));
        coverageDescription.setGridId("Grid000");

        return coverageDescription;
    }
}
