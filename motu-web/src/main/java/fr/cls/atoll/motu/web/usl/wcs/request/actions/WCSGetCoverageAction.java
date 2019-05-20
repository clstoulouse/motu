package fr.cls.atoll.motu.web.usl.wcs.request.actions;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.bll.request.model.ProductResult;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
import fr.cls.atoll.motu.web.usl.request.actions.AbstractAction;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.wcs.Utils;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.WCSHTTPParameters;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.exception.InvalidSubsettingException;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.CoverageIdHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.FormatHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.RangeSubsetHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.RequestHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.SubsetHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.VersionHTTPParameterValidator;
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
public class WCSGetCoverageAction extends AbstractAction {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "GetCoverage";

    public static final int SUBSET_MIN_INDEX = 0;
    public static final int SUBSET_MAX_INDEX = 1;

    public static final double DEFAULT_LAT_MIN = -90;
    public static final double DEFAULT_LAT_MAX = 90;
    public static final double DEFAULT_LON_MIN = -180;
    public static final double DEFAULT_LON_MAX = 180;

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;
    private VersionHTTPParameterValidator versionHTTPParameterValidator;
    private RequestHTTPParameterValidator requestHTTPParameterValidator;
    private CoverageIdHTTPParameterValidator coverageIdHTTPParameterValidator;
    private List<SubsetHTTPParameterValidator> subsetHTTPParameterValidator;
    private RangeSubsetHTTPParameterValidator rangeSubsetHTTParameterValidator;
    private FormatHTTPParameterValidator formatHTTPParameterValidator;

    private Map<String, BigDecimal[]> subsetValues;
    // TODO Add Subset, format, mediaType, ...

    /**
     * Constructeur.
     * 
     * @param actionName_
     * @param actionCode_
     * @param request_
     * @param response_
     */
    public WCSGetCoverageAction(String actionCode_, HttpServletRequest request_, HttpServletResponse response_) {
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

        subsetHTTPParameterValidator = new ArrayList<>();
        subsetValues = new HashMap<>();
        List<String> subsets = WCSHTTPParameters.getSubsetFromRequest(getRequest());
        for (String subset : subsets) {
            subsetHTTPParameterValidator.add(new SubsetHTTPParameterValidator(WCSHTTPParameters.SUBSET, subset));
        }
        rangeSubsetHTTParameterValidator = new RangeSubsetHTTPParameterValidator(
                WCSHTTPParameters.RANGE_SUBSET,
                WCSHTTPParameters.getRangeSubsetFromRequest(getRequest()));
        rangeSubsetHTTParameterValidator.setOptional(true);
        formatHTTPParameterValidator = new FormatHTTPParameterValidator(
                WCSHTTPParameters.FORMAT,
                WCSHTTPParameters.getFormatFromRequest(getRequest()));
        formatHTTPParameterValidator.setOptional(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        serviceHTTPParameterValidator.validate();
        versionHTTPParameterValidator.validate();
        requestHTTPParameterValidator.validate();
        coverageIdHTTPParameterValidator.validate();
        rangeSubsetHTTParameterValidator.validate();
        formatHTTPParameterValidator.validate();
        managerSubsetParameter();
    }

    private void managerSubsetParameter() throws InvalidHTTPParameterException {
        for (SubsetHTTPParameterValidator subsetValidator : subsetHTTPParameterValidator) {
            subsetValidator.validate();

            String subsetValue = subsetValidator.getParameterValueValidated();
            int startSepIndex = subsetValue.indexOf('(');
            int middleSepIndex = subsetValue.indexOf(',');
            int endSepIndex = subsetValue.indexOf(')');

            if (startSepIndex > 0 && middleSepIndex > startSepIndex && endSepIndex > middleSepIndex) {
                String subsetName = subsetValue.substring(0, startSepIndex);
                BigDecimal[] subsetMinMaxValues = new BigDecimal[2];
                String subset0 = subsetValue.substring(startSepIndex + 1, middleSepIndex);
                subsetMinMaxValues[0] = new BigDecimal(subset0); // new BigInteger(subset0);
                String subset1 = subsetValue.substring(middleSepIndex + 1, endSepIndex);
                subsetMinMaxValues[1] = new BigDecimal(subset1); // new BigInteger(subset1);
                subsetValues.put(subsetName, subsetMinMaxValues);
            } else {
                throw new InvalidHTTPParameterException(
                        subsetValidator.getParameterName(),
                        subsetValidator.getParameterValue(),
                        subsetValidator.getParameterBoundariesAsString());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws MotuException {
        serviceHTTPParameterValidator.getParameterValueValidated();
        versionHTTPParameterValidator.getParameterValueValidated();
        requestHTTPParameterValidator.getParameterValueValidated();
        String coverageId = coverageIdHTTPParameterValidator.getParameterValueValidated();
        if (coverageId != null) {

            String[] coverageIdSplited = coverageId.split("@");
            if (coverageIdSplited.length == 2) {
                String serviceName = coverageIdSplited[0];
                String productId = coverageIdSplited[1];

                ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(serviceName);
                if (cs != null) {
                    Product p = BLLManager.getInstance().getCatalogManager().getProductManager().getProduct(cs.getName(), productId);
                    if (p != null) {
                        try {
                            String catalogType = BLLManager.getInstance().getCatalogManager().getCatalogType(cs);
                            ExtractionParameters parameters = null;
                            switch (catalogType.toUpperCase()) {
                            case "FILE":
                            case "FTP":
                                parameters = createExtractionParametersDGF(serviceName, productId, p);
                                break;
                            case "TDS":
                                parameters = createExtractionParametersSubsetter(serviceName, productId, p);
                                break;
                            default:
                                break;
                            }

                            RequestProduct rp = new RequestProduct(p, parameters);

                            ProductResult pr = BLLManager.getInstance().getRequestManager().download(cs, rp, this);
                            if (pr.getRunningException() == null) {
                                uploadfile(pr.getProductFileName());
                            } else {
                                throw new MotuException(ErrorType.SYSTEM, pr.getRunningException());
                            }
                        } catch (InvalidSubsettingException e) {
                            try {
                                String responseErr = Utils.onError(getActionCode(),
                                                                   e.getParameterName(),
                                                                   Constants.INVALID_SUBSETTING_CODE,
                                                                   ErrorType.WCS_INVALID_SUBSETTING,
                                                                   e.getParameterName());
                                writeResponse(responseErr, HTTPUtils.CONTENT_TYPE_XML_UTF8);
                            } catch (IOException e2) {
                                LOGGER.error("Error while processing HTTP request", e2);
                                throw new MotuException(ErrorType.SYSTEM, "Error while processing HTTP request", e2);
                            }
                        }
                    } else {
                        try {
                            String responseErr = Utils.onError(getActionCode(),
                                                               Constants.NO_SUCH_COVERAGE_CODE,
                                                               ErrorType.WCS_NO_SUCH_COVERAGE,
                                                               coverageId,
                                                               coverageId);
                            writeResponse(responseErr, HTTPUtils.CONTENT_TYPE_XML_UTF8);
                        } catch (IOException e2) {
                            LOGGER.error("Error while processing HTTP request", e2);
                            throw new MotuException(ErrorType.SYSTEM, "Error while processing HTTP request", e2);
                        }
                    }
                } else {
                    try {
                        String responseErr = Utils
                                .onError(getActionCode(), coverageId, Constants.NO_SUCH_COVERAGE_CODE, ErrorType.WCS_NO_SUCH_COVERAGE, coverageId);
                        writeResponse(responseErr, HTTPUtils.CONTENT_TYPE_XML_UTF8);
                    } catch (IOException e2) {
                        LOGGER.error("Error while processing HTTP request", e2);
                        throw new MotuException(ErrorType.SYSTEM, "Error while processing HTTP request", e2);
                    }
                }
            } else {
                try {
                    String responseErr = Utils
                            .onError(getActionCode(), coverageId, Constants.NO_SUCH_COVERAGE_CODE, ErrorType.WCS_NO_SUCH_COVERAGE, coverageId);
                    writeResponse(responseErr, HTTPUtils.CONTENT_TYPE_XML_UTF8);
                } catch (IOException e2) {
                    LOGGER.error("Error while processing HTTP request", e2);
                    throw new MotuException(ErrorType.SYSTEM, "Error while processing HTTP request", e2);
                }
            }
        } else {
            try {
                String responseErr = Utils.onError(getActionCode(),
                                                   WCSHTTPParameters.COVERAGE_ID,
                                                   Constants.MISSING_PARAMETER_VALUE_CODE,
                                                   ErrorType.WCS_MISSING_PARAMETER_VALUE,
                                                   WCSHTTPParameters.COVERAGE_ID);
                writeResponse(responseErr, HTTPUtils.CONTENT_TYPE_XML_UTF8);
            } catch (IOException e2) {
                LOGGER.error("Error while processing HTTP request", e2);
                throw new MotuException(ErrorType.SYSTEM, "Error while processing HTTP request", e2);
            }
        }
    }

    private void uploadfile(String fileName) throws MotuException {
        getResponse().setContentType("application/netcdf");
        getResponse().setHeader("Content-Disposition", "attachment;filename=" + fileName);
        File file = new File(BLLManager.getInstance().getCatalogManager().getProductManager().getProductPhysicalFilePath(fileName));
        getResponse().setContentLength(Double.valueOf(file.length()).intValue());
        try {
            Files.copy(file.toPath(), getResponse().getOutputStream());
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, e);
        }
    }

    private ExtractionParameters createExtractionParametersDGF(String serviceName, String productId, Product product)
            throws InvalidSubsettingException, MotuException {
        Long minTime = product.getProductMetaData().getTimeCoverage().getStart().getMillis() / 1000;
        Long maxTime = product.getProductMetaData().getTimeCoverage().getEnd().getMillis() / 1000;
        Long subsetTimeLowValue = subsetValues.get(Constants.TIME_AXIS.name())[SUBSET_MIN_INDEX].longValue();
        Long subsetTimeHighValue = subsetValues.get(Constants.TIME_AXIS.name())[SUBSET_MAX_INDEX].longValue();

        checkValidityOfSubsetParameterName(Constants.DGF_MANDATORY_AXIS, Constants.DGF_OPTIONNAL_AXIS);
        manageSubsetting(Constants.TIME_AXIS, product, minTime, maxTime, subsetTimeLowValue, subsetTimeHighValue);
        ExtractionParameters extractionParameters = new ExtractionParameters(
                serviceName,
                null,
                new ArrayList<String>(),
                computeDate(subsetTimeLowValue),
                computeDate(subsetTimeHighValue),
                -90,
                90,
                -180,
                180,
                0,
                0,
                productId,
                computeOutputFormat(formatHTTPParameterValidator.getParameterValueValidated()),
                null,
                null,
                true,
                "");
        return extractionParameters;
    }

    private ExtractionParameters createExtractionParametersSubsetter(String serviceName, String productId, Product product)
            throws InvalidSubsettingException, MotuException {
        Long minTime = product.getProductMetaData().getTimeAxisMinValue().getTime() / 1000;
        Long maxTime = product.getProductMetaData().getTimeAxisMaxValue().getTime() / 1000;
        Long subsetTimeLowValue = subsetValues.get(Constants.TIME_AXIS.name())[SUBSET_MIN_INDEX].longValue();
        Long subsetTimeHighValue = subsetValues.get(Constants.TIME_AXIS.name())[SUBSET_MAX_INDEX].longValue();

        checkValidityOfSubsetParameterName(Constants.SUBSETTER_MANDATORY_AXIS, Constants.SUBSETTER_OPTIONNAL_AXIS);
        manageSubsetting(Constants.TIME_AXIS, product, minTime, maxTime, subsetTimeLowValue, subsetTimeHighValue);
        manageSubsettings(product);

        double minHeightValue = 0.0;
        double maxHeightValue = 0.0;
        if (subsetValues.containsKey(Constants.HEIGHT_AXIS.name())) {
            minHeightValue = subsetValues.get(Constants.HEIGHT_AXIS.name())[SUBSET_MIN_INDEX].doubleValue();
            maxHeightValue = subsetValues.get(Constants.HEIGHT_AXIS.name())[SUBSET_MAX_INDEX].doubleValue();
        }

        ExtractionParameters extractionParameters = new ExtractionParameters(
                serviceName,
                null,
                computeVariableList(rangeSubsetHTTParameterValidator.getParameterValueValidated()),

                computeDate(subsetTimeLowValue),
                computeDate(subsetTimeHighValue),

                subsetValues.get(Constants.LON_AXIS.name())[SUBSET_MIN_INDEX].doubleValue(),
                subsetValues.get(Constants.LON_AXIS.name())[SUBSET_MAX_INDEX].doubleValue(),
                subsetValues.get(Constants.LAT_AXIS.name())[SUBSET_MIN_INDEX].doubleValue(),
                subsetValues.get(Constants.LAT_AXIS.name())[SUBSET_MAX_INDEX].doubleValue(),

                minHeightValue,
                maxHeightValue,

                productId,
                computeOutputFormat(formatHTTPParameterValidator.getParameterValueValidated()),
                null,
                null,
                true,
                "");

        return extractionParameters;
    }

    private void checkValidityOfSubsetParameterName(AxisType[] mandatoryParameters, AxisType[] optionnalParameters) throws MotuException {
        StringBuffer badSubsetParameterNames = new StringBuffer();
        for (Map.Entry<String, BigDecimal[]> subsetParam : subsetValues.entrySet()) {
            if (!(Utils.contains(mandatoryParameters, subsetParam.getKey())) && !(Utils.contains(optionnalParameters, subsetParam.getKey()))) {
                badSubsetParameterNames.append(subsetParam.getKey());
            }
        }
        for (AxisType currentAxisType : mandatoryParameters) {
            if (!subsetValues.containsKey(currentAxisType.name())) {
                badSubsetParameterNames.append(currentAxisType.name());
            }
        }
        if (badSubsetParameterNames.length() != 0) {
            throw new MotuException(ErrorType.WCS_INVALID_AXIS_LABEL, "", badSubsetParameterNames.toString());
        }
    }

    private void manageSubsettings(Product product) throws InvalidSubsettingException {
        for (AxisType currentAxis : Constants.SUBSETTER_AVAILABLE_AXIS) {

            CoordinateAxis currentCoordinateAxis = product.getProductMetaData().getCoordinateAxisMap().get(currentAxis);
            if (currentCoordinateAxis != null) {

                if (!subsetValues.containsKey(currentAxis.name())) {
                    BigDecimal[] minMaxValue = new BigDecimal[2];
                    minMaxValue[SUBSET_MIN_INDEX] = BigDecimal.valueOf(currentCoordinateAxis.getMinValue()).setScale(0, BigDecimal.ROUND_HALF_DOWN);
                    minMaxValue[SUBSET_MAX_INDEX] = BigDecimal.valueOf(currentCoordinateAxis.getMaxValue()).setScale(0, BigDecimal.ROUND_HALF_DOWN);
                    subsetValues.put(currentAxis.name(), minMaxValue);
                }

                manageSubsetting(currentAxis,
                                 product,
                                 BigDecimal.valueOf(currentCoordinateAxis.getMinValue()).setScale(0, BigDecimal.ROUND_HALF_DOWN).doubleValue(),
                                 BigDecimal.valueOf(currentCoordinateAxis.getMaxValue()).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue(),
                                 subsetValues.get(currentAxis.name())[SUBSET_MIN_INDEX].doubleValue(),
                                 subsetValues.get(currentAxis.name())[SUBSET_MAX_INDEX].doubleValue());
            }

        }
    }

    private void manageSubsetting(AxisType axis, Product product, double minValue, double maxValue, double subSetLowValue, double subSetHighValue)
            throws InvalidSubsettingException {
        if (!(subSetLowValue >= minValue && subSetLowValue <= maxValue && subSetHighValue >= minValue && subSetHighValue <= maxValue
                && subSetLowValue <= subSetHighValue)) {
            throw new InvalidSubsettingException(
                    axis.name() + " (which boundaries are [" + minValue + ";" + maxValue + "] and requested values are [" + subSetLowValue + ";"
                            + subSetHighValue + "])");
        }
    }

    private String computeDate(long timeStamp) {
        Date date = new Date(timeStamp * 1000);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    private List<String> computeVariableList(String rangeValue) {
        if (rangeValue != null) {
            return Arrays.asList(rangeValue.split(","));
        } else {
            return new ArrayList<String>();
        }
    }

    private OutputFormat computeOutputFormat(String formatValue) {
        return OutputFormat.NETCDF;
    }

}
