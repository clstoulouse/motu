package fr.cls.atoll.motu.web.usl.wcs.request.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.util.AssertionHolder;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.bll.request.model.ProductResult;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
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

    private Map<String, BigInteger[]> subsetValues;
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
                if (Utils.contains(Constants.AVAILABLE_AXIS, subsetName)) {
                    BigInteger[] subsetMinMaxValues = new BigInteger[2];
                    subsetMinMaxValues[0] = new BigInteger(subsetValue.substring(startSepIndex + 1, middleSepIndex));
                    subsetMinMaxValues[1] = new BigInteger(subsetValue.substring(middleSepIndex + 1, endSepIndex));
                    subsetValues.put(subsetName, subsetMinMaxValues);
                } else {
                    throw new InvalidHTTPParameterException(subsetName, subsetValue, "");
                }
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
                                parameters = createExtractionParameters(serviceName, productId, p);
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
                            Utils.onError(getResponse(),
                                          getActionCode(),
                                          e.getParameterName(),
                                          Constants.INVALID_SUBSETTING_CODE,
                                          ErrorType.WCS_INVALID_SUBSETTING,
                                          e.getParameterName());
                        }
                    } else {
                        Utils.onError(getResponse(),
                                      getActionCode(),
                                      Constants.NO_SUCH_COVERAGE,
                                      ErrorType.WCS_NO_SUCH_COVERAGE,
                                      coverageId,
                                      coverageId);
                    }
                } else {
                    Utils.onError(getResponse(), getActionCode(), coverageId, Constants.NO_SUCH_COVERAGE, ErrorType.WCS_NO_SUCH_COVERAGE, coverageId);
                }
            } else {
                Utils.onError(getResponse(), getActionCode(), coverageId, Constants.NO_SUCH_COVERAGE, ErrorType.WCS_NO_SUCH_COVERAGE, coverageId);
            }
        } else {
            Utils.onError(getResponse(),
                          getActionCode(),
                          WCSHTTPParameters.COVERAGE_ID,
                          Constants.MISSING_PARAMETER_VALUE_CODE,
                          ErrorType.WCS_MISSING_PARAMETER_VALUE,
                          WCSHTTPParameters.COVERAGE_ID);
        }
    }

    private void uploadfile(String fileName) throws MotuException {
        getResponse().setContentType("application/netcdf");
        getResponse().setHeader("Content-Disposition", "attachment;filename=" + fileName);
        File file = new File(BLLManager.getInstance().getCatalogManager().getProductManager().getProductPhysicalFilePath(fileName));
        getResponse().setContentLength(Double.valueOf(file.length()).intValue());
        FileInputStream fileIn;
        try {
            fileIn = new FileInputStream(file);
            ServletOutputStream out = getResponse().getOutputStream();

            byte[] outputByte = new byte[4096];
            while (fileIn.read(outputByte, 0, 4096) != -1) {
                out.write(outputByte, 0, 4096);
            }
            fileIn.close();
            out.flush();
            out.close();

        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, e);
        }
    }

    private ExtractionParameters createExtractionParametersDGF(String serviceName, String productId, Product product)
            throws InvalidSubsettingException {
        Long minTime = product.getProductMetaData().getTimeCoverage().getStart().getMillis() / 1000;
        Long maxTime = product.getProductMetaData().getTimeCoverage().getEnd().getMillis() / 1000;
        Long subsetTimeLowValue = subsetValues.get(Constants.TIME_AXIS.name())[SUBSET_MIN_INDEX].longValue();
        Long subsetTimeHighValue = subsetValues.get(Constants.TIME_AXIS.name())[SUBSET_MAX_INDEX].longValue();

        manageSubsetting(Constants.TIME_AXIS, product, minTime, maxTime, subsetTimeLowValue, subsetTimeHighValue);
        ExtractionParameters extractionParameters = new ExtractionParameters(
                serviceName,
                null,
                new ArrayList(),
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
        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());
        return extractionParameters;
    }

    private ExtractionParameters createExtractionParameters(String serviceName, String productId, Product product)
            throws InvalidSubsettingException, MotuException {
        Long minTime = product.getProductMetaData().getTimeAxisMinValue().getTime() / 1000;
        Long maxTime = product.getProductMetaData().getTimeAxisMaxValue().getTime() / 1000;
        Long subsetTimeLowValue = subsetValues.get(Constants.TIME_AXIS.name())[SUBSET_MIN_INDEX].longValue();
        Long subsetTimeHighValue = subsetValues.get(Constants.TIME_AXIS.name())[SUBSET_MAX_INDEX].longValue();

        manageSubsetting(Constants.TIME_AXIS, product, minTime, maxTime, subsetTimeLowValue, subsetTimeHighValue);
        manageSubsettings(product);

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

                subsetValues.get(Constants.HEIGHT_AXIS.name())[SUBSET_MIN_INDEX].doubleValue(),
                subsetValues.get(Constants.HEIGHT_AXIS.name())[SUBSET_MAX_INDEX].doubleValue(),

                productId,
                computeOutputFormat(formatHTTPParameterValidator.getParameterValueValidated()),
                null,
                null,
                true,
                "");

        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());
        return extractionParameters;
    }

    private void manageSubsettings(Product product) throws InvalidSubsettingException {
        for (AxisType currentAxis : Constants.AVAILABLE_AXIS) {
            CoordinateAxis currentCoordinateAxis = product.getProductMetaData().getCoordinateAxes().get(currentAxis);
            if (currentCoordinateAxis != null) {
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
                && subSetLowValue < subSetHighValue)) {
            throw new InvalidSubsettingException(Constants.TIME_AXIS.name());
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
            return new ArrayList();
        }
    }

    private OutputFormat computeOutputFormat(String formatValue) {
        return OutputFormat.NETCDF;
    }

}
