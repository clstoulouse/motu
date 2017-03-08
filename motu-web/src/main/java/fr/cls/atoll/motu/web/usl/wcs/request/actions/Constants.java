package fr.cls.atoll.motu.web.usl.wcs.request.actions;

import ucar.nc2.constants.AxisType;

public class Constants {

    public static final AxisType TIME_AXIS = AxisType.Time;
    public static final AxisType LAT_AXIS = AxisType.Lat;
    public static final AxisType LON_AXIS = AxisType.Lon;
    public static final AxisType HEIGHT_AXIS = AxisType.Height;

    public static final AxisType[] AVAILABLE_AXIS = { LAT_AXIS, LON_AXIS, HEIGHT_AXIS };

}
