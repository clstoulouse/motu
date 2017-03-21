package fr.cls.atoll.motu.web.usl.wcs.request.actions;

import ucar.nc2.constants.AxisType;

public class Constants {

    public static final String WCS_SERVICE_NAME = "WCS";
    public static final String WCS_VERSION_VALUE = "2.0.1";

    public static final String MISSING_PARAMETER_VALUE_CODE = "MissingParameterValue";
    public static final String VERSION_NEGOTIATION_FAILED_CODE = "VersionNegotiationFailed";
    public static final String INVALID_PARAMETER_VALUE_CODE = "InvalidParameterValue";
    public static final String NO_APPLICABLE_CODE_CODE = "NoApplicableCode";
    public static final String INVALID_SUBSETTING_CODE = "InvalidSubsetting";
    public static final String NO_SUCH_COVERAGE_CODE = "NoSuchCoverage";
    public static final String EMPTY_COVERAGE_ID_LIST_CODE = "emptyCoverageIdList";
    public static final String INVALID_AXIS_LABEL_CODE = "InvalidAxisLabel";

    public static final AxisType TIME_AXIS = AxisType.Time;
    public static final AxisType LAT_AXIS = AxisType.Lat;
    public static final AxisType LON_AXIS = AxisType.Lon;
    public static final AxisType HEIGHT_AXIS = AxisType.Height;

    public static final AxisType[] SUBSETTER_AVAILABLE_AXIS = { LAT_AXIS, LON_AXIS, HEIGHT_AXIS };
    public static final AxisType[] DGF_AVAILABLE_AXIS = { TIME_AXIS };
    public static final AxisType[] ALL_AVAILABLE_AXIS = { TIME_AXIS, LAT_AXIS, LON_AXIS, HEIGHT_AXIS };

    public static final String NETCDF_MIME_TYPE = "application/netcdf";
}
