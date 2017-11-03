package fr.cls.atoll.motu.web.usl.response.xml.converter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import fr.cls.atoll.motu.api.message.xml.AvailableDepths;
import fr.cls.atoll.motu.api.message.xml.AvailableTimes;
import fr.cls.atoll.motu.api.message.xml.Axis;
import fr.cls.atoll.motu.api.message.xml.DataGeospatialCoverage;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.GeospatialCoverage;
import fr.cls.atoll.motu.api.message.xml.ObjectFactory;
import fr.cls.atoll.motu.api.message.xml.ProductMetadataInfo;
import fr.cls.atoll.motu.api.message.xml.TimeCoverage;
import fr.cls.atoll.motu.api.message.xml.Variable;
import fr.cls.atoll.motu.api.message.xml.VariableNameVocabulary;
import fr.cls.atoll.motu.api.message.xml.VariableVocabulary;
import fr.cls.atoll.motu.api.message.xml.Variables;
import fr.cls.atoll.motu.api.message.xml.VariablesVocabulary;
import fr.cls.atoll.motu.library.converter.DateUtils;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.ExceptionUtils;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DataFile;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ParameterMetaData;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import fr.cls.atoll.motu.web.dal.tds.ncss.model.Property;
import fr.cls.atoll.motu.web.dal.tds.ncss.model.VariableDesc;
import ucar.ma2.MAMath.MinMax;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.unidata.geoloc.LatLonRect;

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
public class ProductMetadataInfoConverter {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Creates the product metadata info.
     * 
     * @return the time coverage
     */
    private static ProductMetadataInfo createProductMetadataInfo() {
        ObjectFactory objectFactory = new ObjectFactory();

        ProductMetadataInfo productMetadataInfo = objectFactory.createProductMetadataInfo();
        productMetadataInfo.setAvailableTimes(null);
        productMetadataInfo.setGeospatialCoverage(null);
        productMetadataInfo.setProperties(null);
        productMetadataInfo.setTimeCoverage(null);
        productMetadataInfo.setVariables(null);
        productMetadataInfo.setVariablesVocabulary(null);
        ExceptionUtils
                .setError(productMetadataInfo,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return productMetadataInfo;

    }

    /**
     * Inits the product metadata info.
     * 
     * @param product the product
     * 
     * @return the product metadata info
     * 
     * @throws MotuExceptionBase the motu exception base
     * @throws MotuException
     */
    public static ProductMetadataInfo getProductMetadataInfo(ConfigService cs, Product product) throws MotuExceptionBase, MotuException {
        ProductMetadataInfo productMetadataInfo = createProductMetadataInfo();

        if (product == null) {
            return productMetadataInfo;
        }

        ProductMetaData productMetaData = product.getProductMetaData();

        if (productMetaData == null) {
            return productMetadataInfo;
        }

        productMetadataInfo.setId(product.getProductId());
        productMetadataInfo.setTitle(productMetaData.getTitle());
        productMetadataInfo.setLastUpdate(productMetaData.getLastUpdate());
        String url = null;
        if (cs != null) {
            url = "enabled".equalsIgnoreCase(cs.getCatalog().getNcss()) ? product.getLocationDataNCSS() : product.getLocationData();
        }
        productMetadataInfo.setUrl(url);
        productMetadataInfo.setGeospatialCoverage(initGeospatialCoverage(productMetaData));
        productMetadataInfo.setProperties(initProperties(productMetaData));
        productMetadataInfo.setTimeCoverage(initTimeCoverage(productMetaData));
        productMetadataInfo.setVariablesVocabulary(initVariablesVocabulary(productMetaData));

        productMetadataInfo.setVariables(initVariables(productMetaData));
        productMetadataInfo.setAvailableTimes(initAvailableTimes(product));
        productMetadataInfo.setAvailableDepths(initAvailableDepths(product));
        productMetadataInfo.setDataGeospatialCoverage(initDataGeospatialCoverage(product));

        productMetadataInfo.setCode(Integer.toString(ErrorType.OK.value()));
        productMetadataInfo.setMsg(ErrorType.OK.toString());

        return productMetadataInfo;
    }

    /**
     * Creates the data geospatial coverage.
     * 
     * @return the data geospatial coverage
     */
    private static DataGeospatialCoverage createDataGeospatialCoverage() {
        ObjectFactory objectFactory = new ObjectFactory();

        DataGeospatialCoverage dataGeospatialCoverage = objectFactory.createDataGeospatialCoverage();

        ExceptionUtils
                .setError(dataGeospatialCoverage,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return dataGeospatialCoverage;

    }

    /**
     * Inits the data geospatial coverage.
     * 
     * @param product the product
     * 
     * @return the data geospatial coverage
     * 
     * @throws MotuException the motu exception
     */
    private static DataGeospatialCoverage initDataGeospatialCoverage(Product product) throws MotuException {
        DataGeospatialCoverage dataGeospatialCoverage = createDataGeospatialCoverage();

        if (product == null) {
            return dataGeospatialCoverage;
        }

        ProductMetaData productMetaData = product.getProductMetaData();
        Collection<CoordinateAxis> coordinateAxes = productMetaData.coordinateAxesValues();

        if (coordinateAxes == null) {
            dataGeospatialCoverage.setCode(Integer.toString(ErrorType.OK.value()));
            dataGeospatialCoverage.setMsg(ErrorType.OK.toString());

            return dataGeospatialCoverage;
        }

        List<Axis> axisList = dataGeospatialCoverage.getAxis();

        if (axisList == null) {
            dataGeospatialCoverage.setCode(Integer.toString(ErrorType.OK.value()));
            dataGeospatialCoverage.setMsg(ErrorType.OK.toString());

            return dataGeospatialCoverage;
        }

        for (CoordinateAxis coordinateAxis : coordinateAxes) {
            axisList.add(initAxis(coordinateAxis, productMetaData));
        }

        dataGeospatialCoverage.setCode(Integer.toString(ErrorType.OK.value()));
        dataGeospatialCoverage.setMsg(ErrorType.OK.toString());

        return dataGeospatialCoverage;
    }

    /**
     * Inits the axis.
     * 
     * @param coordinateAxis the coordinate axis
     * @param productMetaData the product meta data
     * 
     * @return the axis
     * 
     * @throws MotuException the motu exception
     */
    private static Axis initAxis(CoordinateAxis coordinateAxis, ProductMetaData productMetaData) throws MotuException {

        Axis axis = createAxis();

        if (coordinateAxis == null) {
            return axis;
        }
        try {
            axis.setAxisType(coordinateAxis.getAxisType().toString());
            axis.setName(coordinateAxis.getName());
            axis.setDescription(coordinateAxis.getDescription());
            axis.setUnits(coordinateAxis.getUnitsString());

            ParameterMetaData parameterMetaData = productMetaData.getParameterMetaDatas(coordinateAxis.getName());

            if (parameterMetaData != null) {
                axis.setStandardName(parameterMetaData.getStandardName());
                axis.setLongName(parameterMetaData.getLongName());
            }

            MinMax minMax = productMetaData.getAxisMinMaxValue(coordinateAxis.getAxisType());
            if (minMax != null) {
                axis.setLower(new BigDecimal(minMax.min));
                axis.setUpper(new BigDecimal(minMax.max));
            }

            axis.setCode(Integer.toString(ErrorType.OK.value()));
            axis.setMsg(ErrorType.OK.toString());
        } catch (Exception e) {
            axis.setCode(Integer.toString(ErrorType.SYSTEM.value()));
            axis.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(ErrorType.SYSTEM));
            LOGGER.error(StringUtils.getLogMessage(ErrorType.SYSTEM,
                                                   "Error while getting geospatial coverage (axes) from TDS dataset: " + e.getMessage()
                                                           + ". Please, check your dataset"));
        }

        return axis;
    }

    /**
     * Creates the axis.
     * 
     * @return the axis
     */
    private static Axis createAxis() {
        ObjectFactory objectFactory = new ObjectFactory();

        Axis axis = objectFactory.createAxis();
        axis.setAxisType(null);
        axis.setName(null);
        axis.setDescription(null);
        axis.setLower(null);
        axis.setUpper(null);
        axis.setUnits(null);
        axis.setStandardName(null);
        axis.setLongName(null);

        ExceptionUtils
                .setError(axis,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return axis;

    }

    /**
     * Creates the available depth.
     * 
     * @return the available depths
     */
    private static AvailableDepths createAvailableDepth() {
        ObjectFactory objectFactory = new ObjectFactory();

        AvailableDepths availableDepths = objectFactory.createAvailableDepths();

        availableDepths.setValue(null);

        ExceptionUtils
                .setError(availableDepths,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return availableDepths;

    }

    /**
     * Inits the available depths.
     * 
     * @param product the product
     * 
     * @return the available depths
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    private static AvailableDepths initAvailableDepths(Product product) throws MotuException, NetCdfVariableException {
        AvailableDepths availableDepths = createAvailableDepth();

        if (product == null) {
            return availableDepths;
        }

        ProductMetaData productMetaData = product.getProductMetaData();
        if (productMetaData == null) {
            return availableDepths;
        }
        if (!productMetaData.hasZAxis()) {
            return null;
        }
        StringBuilder stringBuffer = new StringBuilder();
        Iterator<String> i = product.getZAxisDataAsString().iterator();
        if (i.hasNext()) {
            for (;;) {
                String value = i.next();
                stringBuffer.append(value);
                if (!i.hasNext()) {
                    break;
                }
                stringBuffer.append(";");
            }
        }
        availableDepths.setValue(stringBuffer.toString());
        availableDepths.setCode(Integer.toString(ErrorType.OK.value()));
        availableDepths.setMsg(ErrorType.OK.toString());

        return availableDepths;
    }

    /**
     * Creates the available times.
     * 
     * @return the available times
     */
    private static AvailableTimes createAvailableTimes() {
        ObjectFactory objectFactory = new ObjectFactory();

        AvailableTimes availableTimes = objectFactory.createAvailableTimes();

        availableTimes.setValue(null);

        ExceptionUtils
                .setError(availableTimes,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return availableTimes;

    }

    static class AvailablePeriod {
        private long startPeriod;
        private long endPeriod;
        private long stepPeriod;
        private int stepsNumber;

        public AvailablePeriod(long startPeriod, long endPeriod, long stepPeriod, int stepsNumber) {
            super();
            setStartPeriod(startPeriod);
            setEndPeriod(endPeriod);
            setStepPeriod(stepPeriod);
            setStepsNumber(stepsNumber);
        }

        public int getStepsNumber() {
            return stepsNumber;
        }

        public void setStepsNumber(int stepsNumber) {
            this.stepsNumber = stepsNumber;
        }

        public long getStartPeriod() {
            return startPeriod;
        }

        public void setStartPeriod(long startPeriod) {
            this.startPeriod = startPeriod;
        }

        public long getEndPeriod() {
            return endPeriod;
        }

        public void setEndPeriod(long endPeriod) {
            this.endPeriod = endPeriod;
        }

        public long getStepPeriod() {
            return stepPeriod;
        }

        public void setStepPeriod(long stepPeriod) {
            this.stepPeriod = stepPeriod;
        }

        @Override
        public String toString() {
            return "AvailablePeriod [startPeriod=" + startPeriod + ", endPeriod=" + endPeriod + ", stepPeriod=" + stepPeriod + "]";
        }

        public String toString(String dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            return sdf.format(new Date(startPeriod)) + "/" + sdf.format(new Date(endPeriod)) + "/" // + "R" +
                                                                                                   // stepsNumber
                                                                                                   // + "/"
                    + fr.cls.atoll.motu.web.common.utils.DateUtils.getDurationISO8601(stepPeriod);
            // + ", dataset duration="
            // + fr.cls.atoll.motu.web.common.utils.DateUtils.getDurationDayHourMinSecMsec(endPeriod -
            // startPeriod) + "]";
        }
    }

    /**
     * Used to create the durations with ISO_8601 format
     * 
     * @param listPeriods A list of string with format "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-dd"
     * @return Display the result on sysout
     */
    private static String buildDurations(List<String> listPeriods) {
        /** Date/time format. */
        String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter dtf = DateTimeFormat.forPattern(DATETIME_FORMAT);

        /** Date format. */
        String DATE_FORMAT = "yyyy-MM-dd";
        DateTimeFormatter df = DateTimeFormat.forPattern(DATE_FORMAT);
        List<Long> timeList = new ArrayList<>();
        for (String iDateTime : listPeriods) {
            try {
                Long dt = dtf.parseMillis(iDateTime);
                timeList.add(dt);
            } catch (java.lang.IllegalArgumentException e) {
                Long dt = df.parseMillis(iDateTime);
                timeList.add(dt);
            }
        }
        Collections.sort(timeList);

        List<AvailablePeriod> availablePeriodList = new ArrayList<>();
        Long periodStart = null;
        Long periodEnd = null;
        Long periodStep = null;
        int stepNumber = 0;
        for (int i = 0; i < timeList.size(); i++) {
            stepNumber++;
            Long iTime = timeList.get(i);
            if (periodStart == null) {
                periodStart = iTime;
                continue;
            }

            if (periodEnd == null) {
                periodEnd = iTime;
                periodStep = periodEnd - periodStart;
                continue;
            }

            Long newPeriodStep = iTime - periodEnd;
            if (newPeriodStep.equals(periodStep)) {
                periodEnd = iTime;
            } else {
                availablePeriodList.add(new AvailablePeriod(periodStart, periodEnd, periodStep, stepNumber));
                periodStart = iTime;
                periodEnd = null;
                periodStep = null;
                stepNumber = 0;
            }
        }
        // Last step
        availablePeriodList.add(new AvailablePeriod(periodStart, periodEnd, periodStep, stepNumber));

        System.out.println("########################");
        StringBuilder sb = new StringBuilder();
        for (AvailablePeriod ap : availablePeriodList) {
            sb.append(ap.toString("yyyy-MM-dd'T'HH:mm:ss'Z'") + ",");
        }
        System.out.println(sb.substring(0, sb.length() - 1));
        System.out.println("########################");
        System.out.println("");
        System.out.println("");
        System.out.println("");

        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Inits the available times.
     * 
     * @param product the product
     * 
     * @return the available times
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    private static AvailableTimes initAvailableTimes(Product product) throws MotuException, NetCdfVariableException {
        AvailableTimes availableTimes = createAvailableTimes();

        if (product == null) {
            return availableTimes;
        }

        StringBuffer stringBuffer = new StringBuffer();
        List<DataFile> df = product.getDataFiles();

        // TDS catalog
        if (df == null) {
            List<String> list = product.getTimeAxisDataAsString();
            buildDurations(list);
            Iterator<String> i = list.iterator();

            if (i.hasNext()) {
                for (;;) {
                    String value = i.next();
                    stringBuffer.append(value);
                    if (!i.hasNext()) {
                        break;
                    }
                    stringBuffer.append(";");
                }
            }
        }
        // FTP catalog
        else {
            Iterator<DataFile> d = df.iterator();

            if (d.hasNext()) {
                for (;;) {
                    String value = DateUtils.getDateTimeAsUTCString(d.next().getStartCoverageDate(), DateUtils.DATETIME_PATTERN2);
                    stringBuffer.append(value);
                    if (!d.hasNext()) {
                        break;
                    }
                    stringBuffer.append(";");
                }
            }
        }

        availableTimes.setValue(stringBuffer.toString());
        availableTimes.setCode(Integer.toString(ErrorType.OK.value()));
        availableTimes.setMsg(ErrorType.OK.toString());

        return availableTimes;
    }

    /**
     * Inits the variables.
     * 
     * @param productMetaData the product meta data
     * 
     * @return the fr.cls.atoll.motu.api.message.xml. variables
     * 
     * @throws MotuException the motu exception
     */
    private static Variables initVariables(ProductMetaData productMetaData) throws MotuException {
        Variables variables = createVariables();

        if (productMetaData == null) {
            return variables;
        }

        Collection<ParameterMetaData> parameterMetaDataList = productMetaData.parameterMetaDatasValues();

        if (parameterMetaDataList == null) {
            variables.setCode(Integer.toString(ErrorType.OK.value()));
            variables.setMsg(ErrorType.OK.toString());
            return variables;
        }

        List<Variable> variableList = variables.getVariable();

        for (ParameterMetaData parameterMetaData : parameterMetaDataList) {
            variableList.add(initVariable(parameterMetaData));
        }

        variables.setCode(Integer.toString(ErrorType.OK.value()));
        variables.setMsg(ErrorType.OK.toString());

        return variables;
    }

    /**
     * Creates the variables.
     * 
     * @return the fr.cls.atoll.motu.api.message.xml. variables
     */
    private static Variables createVariables() {
        ObjectFactory objectFactory = new ObjectFactory();

        Variables variables = objectFactory.createVariables();

        ExceptionUtils
                .setError(variables,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return variables;

    }

    /**
     * Inits the variable.
     * 
     * @param parameterMetaData the parameter meta data
     * 
     * @return the fr.cls.atoll.motu.api.message.xml. variable
     * 
     * @throws MotuException the motu exception
     */
    private static Variable initVariable(ParameterMetaData parameterMetaData) throws MotuException {
        Variable variable = createVariable();

        if (parameterMetaData == null) {
            return variable;
        }

        variable.setDescription(parameterMetaData.getLabel());
        variable.setLongName(parameterMetaData.getLongName());
        variable.setName(parameterMetaData.getName());
        variable.setStandardName(parameterMetaData.getStandardName());
        variable.setUnits(parameterMetaData.getUnit());

        variable.setCode(Integer.toString(ErrorType.OK.value()));
        variable.setMsg(ErrorType.OK.toString());

        return variable;
    }

    /**
     * Creates the variable.
     * 
     * @return the variable
     */
    private static Variable createVariable() {
        ObjectFactory objectFactory = new ObjectFactory();

        Variable variable = objectFactory.createVariable();

        variable.setDescription(null);
        variable.setLongName(null);
        variable.setName(null);
        variable.setStandardName(null);
        variable.setUnits(null);

        ExceptionUtils
                .setError(variable,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return variable;

    }

    /**
     * Inits the time coverage.
     * 
     * @param productMetaData the product meta data
     * 
     * @return the time coverage
     * 
     * @throws MotuException the motu exception
     */
    private static TimeCoverage initTimeCoverage(ProductMetaData productMetaData) throws MotuException {
        if (productMetaData == null) {
            return null;
        }
        Interval datePeriod = productMetaData.getTimeCoverage();
        return initTimeCoverage(datePeriod);
    }

    /**
     * Inits the time coverage.
     * 
     * @param datePeriod the date period
     * 
     * @return the time coverage
     * 
     * @throws MotuException the motu exception
     */
    private static TimeCoverage initTimeCoverage(Interval datePeriod) throws MotuException {
        TimeCoverage timeCoverage = createTimeCoverage();
        if (datePeriod == null) {
            return timeCoverage;
        }

        Date start = datePeriod.getStart().toDate();
        Date end = datePeriod.getEnd().toDate();

        timeCoverage.setStart(fr.cls.atoll.motu.web.common.utils.DateUtils.dateToXMLGregorianCalendar(start));
        timeCoverage.setEnd(fr.cls.atoll.motu.web.common.utils.DateUtils.dateToXMLGregorianCalendar(end));
        timeCoverage.setCode(Integer.toString(ErrorType.OK.value()));
        timeCoverage.setMsg(ErrorType.OK.toString());

        return timeCoverage;
    }

    /**
     * Inits the variables vocabulary.
     * 
     * @param productMetaData the product meta data
     * 
     * @return the variables vocabulary
     * 
     * @throws MotuException the motu exception
     */
    private static VariablesVocabulary initVariablesVocabulary(ProductMetaData productMetaData) throws MotuException {
        VariablesVocabulary variablesVocabulary = createVariablesVocabulary();

        if (productMetaData == null) {
            return variablesVocabulary;
        }

        fr.cls.atoll.motu.web.dal.tds.ncss.model.Variables variables = productMetaData.getVariablesVocabulary();
        if (variables == null) {
            variablesVocabulary.setCode(Integer.toString(ErrorType.OK.value()));
            variablesVocabulary.setMsg(ErrorType.OK.toString());
            return variablesVocabulary;
        }
        List<VariableDesc> variablesDescList = productMetaData.getVariablesVocabulary().getVariableDesc();

        if (variablesDescList == null) {
            variablesVocabulary.setCode(Integer.toString(ErrorType.OK.value()));
            variablesVocabulary.setMsg(ErrorType.OK.toString());
            return variablesVocabulary;
        }

        List<VariableVocabulary> variableVocabularyList = variablesVocabulary.getVariableVocabulary();

        for (VariableDesc variableDesc : variablesDescList) {
            variableVocabularyList.add(initVariableVocabulary(variableDesc));
        }

        variablesVocabulary.setVocabulary(VariableNameVocabulary.fromValue(variables.getVocabulary()));
        variablesVocabulary.setCode(Integer.toString(ErrorType.OK.value()));
        variablesVocabulary.setMsg(ErrorType.OK.toString());

        return variablesVocabulary;
    }

    /**
     * Creates the variables vocabulary.
     * 
     * @return the variables vocabulary
     */
    private static VariablesVocabulary createVariablesVocabulary() {
        ObjectFactory objectFactory = new ObjectFactory();

        VariablesVocabulary variablesVocabulary = objectFactory.createVariablesVocabulary();

        variablesVocabulary.setVocabulary(null);

        ExceptionUtils
                .setError(variablesVocabulary,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return variablesVocabulary;
    }

    /**
     * Inits the variable vocabulary.
     * 
     * @param variableDesc the variable desc
     * 
     * @return the variable vocabulary
     * 
     * @throws MotuException the motu exception
     */
    private static VariableVocabulary initVariableVocabulary(VariableDesc variableDesc) throws MotuException {
        VariableVocabulary variableVocabulary = createVariableVocabulary();

        if (variableDesc == null) {
            return variableVocabulary;
        }

        variableVocabulary.setName(variableDesc.getName());
        variableVocabulary.setUnits(variableDesc.getUnits());
        variableVocabulary.setValue(variableDesc.getContent());
        variableVocabulary.setVocabularyName(variableDesc.getVocabularyName());

        variableVocabulary.setCode(Integer.toString(ErrorType.OK.value()));
        variableVocabulary.setMsg(ErrorType.OK.toString());

        return variableVocabulary;
    }

    /**
     * Creates the variable vocabulary.
     * 
     * @return the variable vocabulary
     */
    private static VariableVocabulary createVariableVocabulary() {
        ObjectFactory objectFactory = new ObjectFactory();

        VariableVocabulary variableVocabulary = objectFactory.createVariableVocabulary();

        variableVocabulary.setName(null);
        variableVocabulary.setUnits(null);
        variableVocabulary.setValue(null);
        variableVocabulary.setVocabularyName(null);

        ExceptionUtils
                .setError(variableVocabulary,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return variableVocabulary;

    }

    /**
     * Creates the time coverage.
     * 
     * @return the time coverage
     */
    private static TimeCoverage createTimeCoverage() {
        ObjectFactory objectFactory = new ObjectFactory();

        TimeCoverage timeCoverage = objectFactory.createTimeCoverage();
        timeCoverage.setStart(null);
        timeCoverage.setEnd(null);
        ExceptionUtils
                .setError(timeCoverage,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return timeCoverage;

    }

    /**
     * Inits the geospatial coverage.
     * 
     * @param productMetaData the product meta data
     * 
     * @return the geospatial coverage
     * 
     * @throws MotuException the motu exception
     */
    private static GeospatialCoverage initGeospatialCoverage(ProductMetaData productMetaData) throws MotuException {
        GeospatialCoverage geospatialCoverage = createGeospatialCoverage();

        if (productMetaData == null) {
            return geospatialCoverage;
        }

        try {
            MinMax depthCoverage = productMetaData.getDepthCoverage();
            if (depthCoverage != null) {
                geospatialCoverage.setDepthMax(new BigDecimal(productMetaData.getDepthCoverage().max));
                geospatialCoverage.setDepthMin(new BigDecimal(productMetaData.getDepthCoverage().min));
            }
            if (productMetaData.getDepthResolution() != null) {
                geospatialCoverage.setDepthResolution(new BigDecimal(productMetaData.getDepthResolution()));
            }
            geospatialCoverage.setDepthUnits(productMetaData.getDepthUnits());

            LatLonRect geoBBox = productMetaData.getGeoBBox();
            if (geoBBox != null) {
                geospatialCoverage.setEast(new BigDecimal(productMetaData.getGeoBBox().getLonMax()));
                geospatialCoverage.setWest(new BigDecimal(productMetaData.getGeoBBox().getLonMin()));
                geospatialCoverage.setNorth(new BigDecimal(productMetaData.getGeoBBox().getLatMax()));
                geospatialCoverage.setSouth(new BigDecimal(productMetaData.getGeoBBox().getLatMin()));
            }
            if (productMetaData.getEastWestResolution() != null) {
                geospatialCoverage.setEastWestResolution(new BigDecimal(productMetaData.getEastWestResolution()));
            }
            geospatialCoverage.setEastWestUnits(productMetaData.getEastWestUnits());
            if (productMetaData.getNorthSouthResolution() != null) {
                geospatialCoverage.setNorthSouthResolution(new BigDecimal(productMetaData.getNorthSouthResolution()));
            }
            geospatialCoverage.setNorthSouthUnits(productMetaData.getNorthSouthUnits());

            geospatialCoverage.setCode(Integer.toString(ErrorType.OK.value()));
            geospatialCoverage.setMsg(ErrorType.OK.toString());
        } catch (Exception e) {
            geospatialCoverage.setCode(Integer.toString(ErrorType.SYSTEM.value()));
            geospatialCoverage.setMsg(BLLManager.getInstance().getMessagesErrorManager().getMessageError(ErrorType.SYSTEM));
            LOGGER.error(StringUtils.getLogMessage(ErrorType.SYSTEM,
                                                   "Error while getting geospatial coverage (N/S, E/W) from TDS configuration file: " + e.getMessage()
                                                           + ". Please, check your configuration file"));
        }

        return geospatialCoverage;
    }

    /**
     * Creates the geospatial coverage.
     * 
     * @return the geospatial coverage
     */
    private static GeospatialCoverage createGeospatialCoverage() {
        ObjectFactory objectFactory = new ObjectFactory();

        GeospatialCoverage geospatialCoverage = objectFactory.createGeospatialCoverage();
        geospatialCoverage.setDepthMax(null);
        geospatialCoverage.setDepthMin(null);
        geospatialCoverage.setDepthResolution(null);
        geospatialCoverage.setDepthUnits(null);
        geospatialCoverage.setEast(null);
        geospatialCoverage.setEastWestResolution(null);
        geospatialCoverage.setEastWestUnits(null);
        geospatialCoverage.setNorth(null);
        geospatialCoverage.setNorthSouthResolution(null);
        geospatialCoverage.setNorthSouthUnits(null);
        geospatialCoverage.setSouth(null);
        geospatialCoverage.setWest(null);

        ExceptionUtils
                .setError(geospatialCoverage,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return geospatialCoverage;

    }

    /**
     * Inits the properties.
     * 
     * @param productMetaData the product meta data
     * 
     * @return the fr.cls.atoll.motu.api.message.xml. properties
     * 
     * @throws MotuException the motu exception
     */
    private static fr.cls.atoll.motu.api.message.xml.Properties initProperties(ProductMetaData productMetaData) throws MotuException {
        fr.cls.atoll.motu.api.message.xml.Properties properties = createProperties();

        if (productMetaData == null) {
            return properties;
        }

        List<Property> listTDSMetaDataProperty = productMetaData.getListTDSMetaDataProperty();

        if (listTDSMetaDataProperty == null) {
            return null;
        }

        List<fr.cls.atoll.motu.api.message.xml.Property> propertyList = properties.getProperty();

        for (Property tdsMetaDataProperty : listTDSMetaDataProperty) {
            propertyList.add(initProperty(tdsMetaDataProperty));
        }

        properties.setCode(Integer.toString(ErrorType.OK.value()));
        properties.setMsg(ErrorType.OK.toString());

        return properties;
    }

    /**
     * Creates the properties.
     * 
     * @return the fr.cls.atoll.motu.api.message.xml. properties
     */
    private static fr.cls.atoll.motu.api.message.xml.Properties createProperties() {
        ObjectFactory objectFactory = new ObjectFactory();

        fr.cls.atoll.motu.api.message.xml.Properties properties = objectFactory.createProperties();

        ExceptionUtils
                .setError(properties,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return properties;
    }

    /**
     * Inits the property.
     * 
     * @param tdsProperty the tds property
     * 
     * @return the variable vocabulary
     * 
     * @throws MotuException the motu exception
     */
    private static fr.cls.atoll.motu.api.message.xml.Property initProperty(Property tdsProperty) throws MotuException {

        fr.cls.atoll.motu.api.message.xml.Property property = createProperty();

        if (tdsProperty == null) {
            return property;
        }

        property.setName(tdsProperty.getName());
        property.setValue(tdsProperty.getValue());

        property.setCode(Integer.toString(ErrorType.OK.value()));
        property.setMsg(ErrorType.OK.toString());

        return property;
    }

    /**
     * Creates the property.
     * 
     * @return the fr.cls.atoll.motu.api.message.xml. property
     */
    private static fr.cls.atoll.motu.api.message.xml.Property createProperty() {
        ObjectFactory objectFactory = new ObjectFactory();

        fr.cls.atoll.motu.api.message.xml.Property property = objectFactory.createProperty();

        property.setName(null);
        property.setValue(null);
        ExceptionUtils
                .setError(property,
                          new MotuException(ErrorType.SYSTEM, "If you see that message, the request has failed and the error has not been filled"));
        return property;
    }

}
