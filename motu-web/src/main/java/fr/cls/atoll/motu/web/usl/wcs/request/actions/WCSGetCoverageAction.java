package fr.cls.atoll.motu.web.usl.wcs.request.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.WCSHTTPParameters;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.CoverageIdHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.RangeSubsetHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.RequestHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.SubsetHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.VersionHTTPParameterValidator;

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

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;
    private VersionHTTPParameterValidator versionHTTPParameterValidator;
    private RequestHTTPParameterValidator requestHTTPParameterValidator;
    private CoverageIdHTTPParameterValidator coverageIdHTTPParameterValidator;
    private List<SubsetHTTPParameterValidator> subsetHTTPParameterValidator;
    private RangeSubsetHTTPParameterValidator rangeSubsetHTTParameterValidator;

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
        String[] subsets = WCSHTTPParameters.getRequestParametersIgnoreCase(getRequest(), WCSHTTPParameters.SUBSET);
        for (String subset : subsets) {
            subsetHTTPParameterValidator.add(new SubsetHTTPParameterValidator(WCSHTTPParameters.SUBSET, subset));
        }
        rangeSubsetHTTParameterValidator = new RangeSubsetHTTPParameterValidator(
                WCSHTTPParameters.RANGE_SUBSET,
                WCSHTTPParameters.getRangeSubsetFromRequest(getRequest()));
        rangeSubsetHTTParameterValidator.setOptional(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        serviceHTTPParameterValidator.validate();
        versionHTTPParameterValidator.validate();
        requestHTTPParameterValidator.validate();
        coverageIdHTTPParameterValidator.validate();
        rangeSubsetHTTParameterValidator.validate();
        managerSubsetParameter();
    }

    private void managerSubsetParameter() throws InvalidHTTPParameterException {
        for (SubsetHTTPParameterValidator subsetValidator : subsetHTTPParameterValidator) {
            subsetValidator.validate();

            String subsetValue = subsetValidator.getParameterValueValidated();
            int startSepIndex = subsetValue.indexOf('(');
            int middleSepIndex = subsetValue.indexOf(',');
            int endSepIndex = subsetValue.indexOf(')');

            String subsetName = subsetValue.substring(0, startSepIndex);
            BigInteger[] subsetMinMaxValues = new BigInteger[2];
            subsetMinMaxValues[0] = new BigInteger(subsetValue.substring(startSepIndex + 1, middleSepIndex));
            subsetMinMaxValues[1] = new BigInteger(subsetValue.substring(middleSepIndex + 1, endSepIndex));
            subsetValues.put(subsetName, subsetMinMaxValues);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws MotuException {
        serviceHTTPParameterValidator.getParameterValueValidated();
        versionHTTPParameterValidator.getParameterValueValidated();
        requestHTTPParameterValidator.getParameterValueValidated();
        String coverageId = coverageIdHTTPParameterValidator.getParameterValueValidated();

        String[] coverageIdSplited = coverageId.split("@");
        String serviceName = coverageIdSplited[0];
        String productId = coverageIdSplited[1];

        ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(serviceName);
        Product p = BLLManager.getInstance().getCatalogManager().getProductManager().getProduct(cs.getName(), productId);
        RequestProduct rp = new RequestProduct(p, createExtractionParameters(serviceName, productId));

        ProductResult pr = BLLManager.getInstance().getRequestManager().download(cs, rp, this);
        getResponse().setContentType("application/octet-stream");
        getResponse().setHeader("Content-Disposition", "attachment;filename=" + pr.getProductFileName());

        File file = new File(BLLManager.getInstance().getCatalogManager().getProductManager().getProductPhysicalFilePath(pr.getProductFileName()));
        FileInputStream fileIn;
        try {
            fileIn = new FileInputStream(file);
            ServletOutputStream out = getResponse().getOutputStream();

            byte[] outputByte = new byte[4096];
            // copy binary contect to output stream
            while (fileIn.read(outputByte, 0, 4096) != -1) {
                out.write(outputByte, 0, 4096);
            }
            fileIn.close();
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private ExtractionParameters createExtractionParameters(String serviceName, String productId) {
        ExtractionParameters extractionParameters = new ExtractionParameters(
                serviceName,
                null,
                computeVariableList(rangeSubsetHTTParameterValidator.getParameterValueValidated()),

                computeDate(subsetValues.get(Constants.TIME_AXIS.name())[SUBSET_MIN_INDEX]),
                computeDate(subsetValues.get(Constants.TIME_AXIS.name())[SUBSET_MAX_INDEX]),

                subsetValues.get(Constants.LON_AXIS.name())[SUBSET_MIN_INDEX].doubleValue(),
                subsetValues.get(Constants.LON_AXIS.name())[SUBSET_MAX_INDEX].doubleValue(),
                subsetValues.get(Constants.LAT_AXIS.name())[SUBSET_MIN_INDEX].doubleValue(),
                subsetValues.get(Constants.LAT_AXIS.name())[SUBSET_MAX_INDEX].doubleValue(),

                subsetValues.get(Constants.HEIGHT_AXIS.name())[SUBSET_MIN_INDEX].doubleValue(),
                subsetValues.get(Constants.HEIGHT_AXIS.name())[SUBSET_MAX_INDEX].doubleValue(),

                productId,
                OutputFormat.NETCDF,
                null,
                null,
                true,
                "");

        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());
        return extractionParameters;
    }

    private String computeDate(BigInteger timeStamp) {
        Date date = new Date(timeStamp.longValue());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    private List<String> computeVariableList(String rangeValue) {
        if (rangeValue != null) {
            return Arrays.asList(rangeValue.split(","));
        } else {
            return null;
        }
    }

}
