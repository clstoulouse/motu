package fr.cls.atoll.motu.web.usl.response.velocity.model.catalog;

import java.util.List;

import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

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
public interface IProductMetadata {

    String getTitle();

    List<IParameterMetadata> getParameterMetaDatasFiltered();

    String getQuickLook(IParameterMetadata parameterMetadata);

    String getPageSiteWebURL();

    String getLASViewingServiceURL();

    String getFTPServiceURL();

    String getBulletinSiteURL();

    String getProductIdEncoded();

    boolean hasTimeAxis();

    String getTimeAxisMinValueAsString();

    String getTimeAxisMaxValueAsString();

    boolean hasTimeCoverage();

    String getStartTimeCoverageAsUTCString();

    String getEndTimeCoverageAsUTCString();

    String getTimeAxisMinValueAsUTCString(String datePattern);

    String getTimeAxisMaxValueAsUTCString(String datePattern);

    String getLastUpdate();

    boolean hasLatLonAxis();

    boolean hasGeoXYAxis();

    boolean hasLatAxis();

    IAxis getLatAxis();

    boolean hasGeoYAxis();

    IAxis getGeoYAxisAsLat(IProduct product);

    IAxis getGeoYAxis();

    boolean hasGeoBBox();

    boolean hasLonAxis();

    IAxis getLonAxis();

    boolean hasGeoXAxis();

    IAxis getGeoXAxisAsLon(IProduct product);

    IAxis getGeoXAxis();

    boolean hasZAxis();

    IAxis getZAxis();

    boolean hasDepthBBox();

    String getLatAxisMinValueAsString();

    String getLonAxisMinValueAsString();

    String getGeoYAxisMinValueAsLatString(IProduct product);

    String getGeoYAxisMinValueAsString();

    String getGeoBBoxLatMinAsString();

    String getGeoXAxisMinValueAsLonString(IProduct product);

    String getGeoXAxisMinValueAsString();

    String getGeoBBoxLonMinAsString();

    String getZAxisMinValueAsString();

    String getDepthMinAsString();

    String getLatAxisMaxValueAsString();

    String getLonAxisMaxValueAsString();

    String getGeoYAxisMaxValueAsLatString(IProduct product);

    String getGeoYAxisMaxValueAsString();

    String getGeoBBoxLatMaxAsString();

    String getGeoXAxisMaxValueAsLonString(IProduct product);

    String getGeoXAxisMaxValueAsString();

    String getGeoBBoxLonMaxAsString();

    String getZAxisMaxValueAsString();

    String getDepthMaxAsString();

    boolean isCoordinateAxesEmpty();

    boolean hasGeographicalAxis();

    ITimeAxis getTimeAxis();

    String getLonNormalAxisMinValue();

    String getLonNormalAxisMaxValue();

    String getLatNormalAxisMinValue();

    String getLatNormalAxisMaxValue();

    String getGeoXAxisMinValueAsLonNormal(Product p);

    String getGeoXAxisMaxValueAsLonNormal(Product p);

    String getGeoYAxisMinValueAsLatNormal(Product p);

    String getGeoYAxisMaxValueAsLatNormal(Product p);

    String getGeoXAxisMinValue();

    String getGeoXAxisMaxValue();

    String getGeoYAxisMinValue();

    String getGeoYAxisMaxValue();

}
