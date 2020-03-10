package fr.cls.atoll.motu.web.usl.response.velocity.model.catalog;

import java.util.List;
import java.util.Map;

import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;

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
public interface IProduct {

    String getProductId();

    //// Methods used only for the download part when a file has been extracted
    boolean hasDownloadUrlPath();

    String getDownloadUrlPath();

    String getExtractFilename();

    boolean isAutoDownloadTimeOutEnable();

    int getAutoDownloadTimeOut();

    boolean isProductDownloadable();

    String getLocationData();

    String getLocationMetaData();

    boolean isProductAlongTrack();

    boolean hasGeoXAxisWithLonEquivalence();

    boolean hasGeoYAxisWithLatEquivalence();

    IProductMetadata getProductMetaData();

    boolean hasCriteriaDateTime();

    IDateTime getCriteriaDateTime();

    boolean hasCriteriaDepth();

    IDepth getCriteriaDepth();

    List<String> getZAxisRoundedDownDataAsString(int desiredDecimalNumberDigits);

    List<String> getZAxisRoundedUpDataAsString(int desiredDecimalNumberDigits);

    List<String> getTimeAxisDataAsString();

    Map<String, List<String>> getListTimeByDate();

    List<String> getTimeCoverageFromDataFiles();

    boolean hasLastError();

    String getLastError();

    boolean hasCriteriaLatLon();

    ExtractCriteriaLatLon getCriteriaLatLon();

    String computeDateFromDateTime(String dateTime);

    String getLatResolution();

    String getLonResolution();

    String getDepthResolution();
}
