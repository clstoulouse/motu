package fr.cls.atoll.motu.web.usl.response.velocity.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDatetime;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDepth;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.common.utils.URLUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ParameterMetaData;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import fr.cls.atoll.motu.web.usl.USLManager;
import fr.cls.atoll.motu.web.usl.response.velocity.VelocityTemplateManager;
import fr.cls.atoll.motu.web.usl.response.velocity.model.IService;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.IAxis;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.ICatalog;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.ICatalogProduct;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.ICatalogProductMetaData;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.IDateTime;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.IDepth;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.IParameterMetadata;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.IProduct;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.IProductMetadata;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.ITimeAxis;
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
public class VelocityModelConverter {

    private final static Logger LOGGER = LogManager.getLogger();

    public static List<IService> converServiceList(MotuConfig mc, List<ConfigService> cfgList_) {
        List<IService> isList = new ArrayList<IService>(cfgList_.size());
        for (final ConfigService cs : cfgList_) {
            isList.add(convertToService(mc, cs));
        }
        return isList;
    }

    public static IService convertToService(MotuConfig mc, ConfigService cs) {
        return convertToService(mc, cs, null);
    }

    public static IService convertToService(final MotuConfig mc_, final ConfigService cs, final CatalogData catalogData) {
        return new IService() {

            @Override
            public String getNameEncoded() {
                return VelocityTemplateManager.encodeString(cs.getName());
            }

            @Override
            public String getGroup() {
                return VelocityTemplateManager.encodeString(cs.getGroup());
            }

            @Override
            public String getDescription() {
                return VelocityTemplateManager.encodeString(cs.getDescription());
            }

            @Override
            public String getCatalogType() {
                try {
                    String catalogType = VelocityTemplateManager.encodeString(BLLManager.getInstance().getCatalogManager().getCatalogType(cs));
                    switch (catalogType.toUpperCase()) {
                    case "FILE":
                    case "FTP":
                        return "DGF";
                    case "TDS":
                        return "Subsetter";
                    default:
                        return "";
                    }
                } catch (MotuException e) {
                    LOGGER.error("Impossible to retrieve the type of the catalog for the service : " + cs.getName(), e);
                }
                return null;
            }

            @Override
            public ICatalog getCatalog() {
                return convertToICatalog(catalogData);
            }

            @Override
            public String getHttpBaseRef() {
                return StringUtils.isNullOrEmpty(cs.getHttpBaseRef()) ? mc_.getHttpBaseRef() : cs.getHttpBaseRef();
            }

            @Override
            public boolean isDownloadOnTop() {
                return cs.getDownloadOnTop();
            }

            @Override
            public String getCatalogLocation() {
                return URLUtils.concatUrlPaths(cs.getCatalog().getUrlSite(), cs.getCatalog().getName());
            }

        };
    }

    public static ICatalog convertToICatalog(final CatalogData c) {
        return new ICatalog() {

            @Override
            public String getTitle() {
                return c.getTitle();
            }

            @Override
            public List<ICatalogProduct> getCatalogProductList() {
                return convertToICatalogProductList(new ArrayList<Product>(c.getProducts().values()));
            }

            @Override
            public List<List<ICatalogProduct>> getListProductTypeDataset() {
                return convertToICatalogProductListList(c.getListProductTypeDataset());
            }

        };
    }

    public static List<List<ICatalogProduct>> convertToICatalogProductListList(final List<List<Product>> pListList) {
        List<List<ICatalogProduct>> l = new ArrayList<List<ICatalogProduct>>();
        for (List<Product> pList : pListList) {
            l.add(convertToICatalogProductList(pList));
        }
        return l;
    }

    public static List<ICatalogProduct> convertToICatalogProductList(final List<Product> pList) {
        List<ICatalogProduct> l = new ArrayList<ICatalogProduct>();
        for (Product p : pList) {
            l.add(convertToICatalogProduct(p));
        }
        return l;
    }

    public static ICatalogProduct convertToICatalogProduct(final Product p) {
        return new ICatalogProduct() {

            @Override
            public ICatalogProductMetaData getProductMetaData() {
                return convertToICatalogProductMetaData(p.getProductMetaData());
            }

            @Override
            public String getLocationData() {
                return p.getLocationData();
            }

        };
    }

    public static ICatalogProductMetaData convertToICatalogProductMetaData(final ProductMetaData p) {
        return new ICatalogProductMetaData() {

            @Override
            public String getProductTypeServiceValue() {
                return p.getProductTypeServiceValue();
            }

            @Override
            public String getProductIdEncoded() {
                return p.getProductIdEncoded();
            }

            @Override
            public String getTitle() {
                return p.getTitle();
            }

            @Override
            public String getLastUpdate() {
                return p.getLastUpdate();
            }

            @Override
            public String getProductType() {
                return p.getProductType();
            }

            @Override
            public List<String> getProductSubTypes() {
                return p.getProductSubTypes();
            }

            @Override
            public int compareSubTypes(List<String> subTypeList) {
                return p.compareSubTypes(subTypeList);
            }

        };
    }

    /**
     * .
     * 
     * @param product_
     * @return
     */
    public static IProduct convertToProduct(final RequestProduct requestProduct_) {
        IProduct ip = new IProduct() {

            @Override
            public String getProductId() {
                return requestProduct_.getProduct().getProductId();
            }

            @Override
            public boolean isProductDownloadable() {
                try {
                    return requestProduct_.getProduct().isProductDownloadable();
                } catch (MotuException e) {
                    LOGGER.error("Converting Product to be used in Velocity", e);
                    return true;
                }
            }

            @Override
            public String getLocationData() {
                return requestProduct_.getProduct().getLocationData();
            }

            @Override
            public String getLocationMetaData() {
                return requestProduct_.getProduct().getLocationMetaData();
            }

            @Override
            public boolean isProductAlongTrack() {
                try {
                    return requestProduct_.getProduct().isProductAlongTrack();
                } catch (MotuException e) {
                    LOGGER.error("Converting Product to be used in Velocity", e);
                    return false;
                }
            }

            @Override
            public boolean hasGeoXAxisWithLonEquivalence() {
                return requestProduct_.getProduct().getProductMetaData().hasGeoXAxisWithLonEquivalence();
            }

            @Override
            public boolean hasGeoYAxisWithLatEquivalence() {
                return requestProduct_.getProduct().getProductMetaData().hasGeoYAxisWithLatEquivalence();
            }

            @Override
            public IProductMetadata getProductMetaData() {
                return convertToProductMetadata(requestProduct_.getProduct(), requestProduct_.getProduct().getProductMetaData());
            }

            @Override
            public IDateTime getCriteriaDateTime() {
                return convertToDateTime(requestProduct_.getCriteriaDateTime());
            }

            @Override
            public List<String> getZAxisRoundedDownDataAsString(int desiredDecimalNumberDigits) {
                try {
                    return requestProduct_.getProduct().getZAxisRoundedDownDataAsString(desiredDecimalNumberDigits);
                } catch (MotuException e) {
                    LOGGER.error("Error while converting Product", e);
                    return null;
                }
            }

            @Override
            public List<String> getZAxisRoundedUpDataAsString(int desiredDecimalNumberDigits) {
                try {
                    return requestProduct_.getProduct().getZAxisRoundedUpDataAsString(desiredDecimalNumberDigits);
                } catch (MotuException e) {
                    LOGGER.error("Error while converting Product", e);
                    return null;
                }
            }

            @Override
            public boolean hasCriteriaDepth() {
                return requestProduct_.hasCriteriaDepth();
            }

            @Override
            public boolean hasCriteriaDateTime() {
                return requestProduct_.hasCriteriaDateTime();
            }

            @Override
            public IDepth getCriteriaDepth() {
                return convertToDepth(requestProduct_.getCriteriaDepth());
            }

            @Override
            public List<String> getTimeAxisDataAsString() {
                try {
                    return requestProduct_.getProduct().getTimeAxisDataAsString();
                } catch (MotuException e) {
                    LOGGER.error("Error while converting Product", e);
                    return null;
                }
            }

            @Override
            public List<String> getTimeCoverageFromDataFiles() {
                return requestProduct_.getProduct().getTimeCoverageFromDataFiles();
            }

            @Override
            public boolean hasLastError() {
                return requestProduct_.hasLastError();
            }

            @Override
            public String getLastError() {
                return requestProduct_.getLastError();
            }

            @Override
            public boolean hasDownloadUrlPath() {
                return requestProduct_ != null && requestProduct_.getRequestProductParameters() != null
                        && !StringUtils.isNullOrEmpty(requestProduct_.getRequestProductParameters().getExtractFilename());
            }

            @Override
            public String getDownloadUrlPath() {
                return USLManager.getInstance().getRequestManager().getProductDownloadUrlPath(requestProduct_);
            }

            @Override
            public String getExtractFilename() {
                return requestProduct_.getRequestProductParameters().getExtractFilename();
            }

            @Override
            public boolean isAutoDownloadTimeOutEnable() {
                return !StringUtils.isNullOrEmpty(requestProduct_.getRequestProductParameters().getExtractFilename());
            }

            @Override
            public int getAutoDownloadTimeOut() {
                return 3000;
            }

            @Override
            public boolean hasCriteriaLatLon() {
                return requestProduct_.hasCriteriaLatLon();
            }

            @Override
            public ExtractCriteriaLatLon getCriteriaLatLon() {
                return requestProduct_.getCriteriaLatLon();
            }

        };
        return ip;
    }

    /**
     * .
     * 
     * @param criteriaDepth
     * @return
     */
    protected static IDepth convertToDepth(final ExtractCriteriaDepth criteriaDepth) {
        return new IDepth() {

            @Override
            public String getFromAsString(String pattern_) {
                return criteriaDepth.getFromAsString(pattern_);
            }

            @Override
            public String getToAsString(String pattern_) {
                return criteriaDepth.getToAsString(pattern_);
            }

        };
    }

    /**
     * .
     * 
     * @param criteriaDateTime
     * @return
     */
    protected static IDateTime convertToDateTime(final ExtractCriteriaDatetime criteriaDateTime_) {
        return new IDateTime() {

            @Override
            public String getFromAsString() {
                return criteriaDateTime_.getFromAsString();
            }

            @Override
            public String getToAsString() {
                return criteriaDateTime_.getToAsString();
            }

        };
    }

    /**
     * .
     * 
     * @param product_
     * @return
     */
    public static IProductMetadata convertToProductMetadata(final Product orginalProduct_, final ProductMetaData productMetaData_) {
        return new IProductMetadata() {

            @Override
            public boolean isCoordinateAxesEmpty() {
                return productMetaData_.isCoordinateAxesEmpty();
            }

            @Override
            public boolean hasZAxis() {
                return productMetaData_.hasZAxis();
            }

            @Override
            public boolean hasTimeCoverage() {
                return productMetaData_.hasTimeCoverage();
            }

            @Override
            public boolean hasTimeAxis() {
                return productMetaData_.hasTimeAxis();
            }

            @Override
            public boolean hasLonAxis() {
                return productMetaData_.hasLonAxis();
            }

            @Override
            public boolean hasLatAxis() {
                return productMetaData_.hasLatAxis();
            }

            @Override
            public boolean hasGeographicalAxis() {
                return productMetaData_.hasGeographicalAxis();
            }

            @Override
            public boolean hasGeoYAxis() {
                return productMetaData_.hasGeoXYAxis();
            }

            @Override
            public boolean hasGeoXAxis() {
                return productMetaData_.hasGeoXAxis();
            }

            @Override
            public boolean hasGeoBBox() {
                return productMetaData_.hasGeoBBox();
            }

            @Override
            public boolean hasDepthBBox() {
                // SMA: Not really sure of this call
                return productMetaData_.hasDepthCoverage();
            }

            @Override
            public String getZAxisMinValueAsString() {
                return productMetaData_.getZAxisMinValueAsString();
            }

            @Override
            public String getZAxisMaxValueAsString() {
                return productMetaData_.getZAxisMaxValueAsString();
            }

            @Override
            public IAxis getZAxis() {
                return convertToAxis(productMetaData_.getZAxis());
            }

            @Override
            public String getTitle() {
                return productMetaData_.getTitle();
            }

            @Override
            public String getTimeAxisMinValueAsString() {
                try {
                    return productMetaData_.getTimeAxisMinValueAsString();
                } catch (MotuException e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                    return "";
                }
            }

            @Override
            public String getTimeAxisMaxValueAsString() {
                try {
                    return productMetaData_.getTimeAxisMaxValueAsString();
                } catch (MotuException e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                    return "";
                }
            }

            @Override
            public String getStartTimeCoverageAsUTCString() {
                return productMetaData_.getStartTimeCoverageAsUTCString();
            }

            @Override
            public String getStartTimeCoverageAsUTCString(String dateFormat) {
                return productMetaData_.getStartTimeCoverageAsUTCString(dateFormat);
            }

            @Override
            public String getQuickLook(IParameterMetadata parameterMetadata) {
                return productMetaData_.getQuickLook(parameterMetadata.getName());
            }

            @Override
            public String getProductIdEncoded() {
                return productMetaData_.getProductIdEncoded();
            }

            @Override
            public List<IParameterMetadata> getParameterMetaDatasFiltered() {
                List<IParameterMetadata> a = new ArrayList<IParameterMetadata>();
                for (ParameterMetaData pmd : productMetaData_.getParameterMetaDatasFiltered().values()) {
                    a.add(convertToParameterMetadata(pmd));
                }
                return a;
            }

            @Override
            public String getPageSiteWebURL() {
                return productMetaData_.getPageSiteWebURL();
            }

            @Override
            public String getLonAxisMinValueAsString() {
                return productMetaData_.getLonAxisMinValueAsString();
            }

            @Override
            public String getLonAxisMaxValueAsString() {
                return productMetaData_.getLonAxisMaxValueAsString();
            }

            @Override
            public IAxis getLonAxis() {
                return convertToAxis(productMetaData_.getLonAxis());
            }

            @Override
            public String getLatAxisMinValueAsString() {
                return productMetaData_.getLatAxisMinValueAsString();
            }

            @Override
            public String getLatAxisMaxValueAsString() {
                return productMetaData_.getLatAxisMaxValueAsString();
            }

            @Override
            public IAxis getLatAxis() {
                return convertToAxis(productMetaData_.getLatAxis());
            }

            @Override
            public String getLastUpdate() {
                return productMetaData_.getLastUpdate();
            }

            @Override
            public String getLASViewingServiceURL() {
                return productMetaData_.getLASViewingServiceURL();
            }

            @Override
            public String getGeoYAxisMinValueAsString() {
                return productMetaData_.getGeoYAxisMinValueAsString();
            }

            @Override
            public String getGeoYAxisMinValueAsLatString(IProduct product) {
                try {
                    return productMetaData_.getGeoYAxisMinValueAsLatString(orginalProduct_);
                } catch (Exception e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                }
                return "";
            }

            @Override
            public String getGeoYAxisMaxValueAsString() {
                return productMetaData_.getGeoYAxisMaxValueAsString();
            }

            @Override
            public String getGeoYAxisMaxValueAsLatString(IProduct product) {
                try {
                    return productMetaData_.getGeoYAxisMaxValueAsLatString(orginalProduct_);
                } catch (Exception e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                }
                return "";
            }

            @Override
            public IAxis getGeoYAxisAsLat(IProduct product) {
                return new IAxis() {

                    @Override
                    public String getName() {
                        try {
                            return productMetaData_.getGeoYAxisAsLat(orginalProduct_).getName();
                        } catch (Exception e) {
                            LOGGER.error("Converting Product metadata to be used in Velocity", e);
                        }
                        return "";
                    }

                    @Override
                    public String getUnitsString() {
                        try {
                            return productMetaData_.getGeoYAxisAsLat(orginalProduct_).getUnitsString();
                        } catch (Exception e) {
                            LOGGER.error("Converting Product metadata to be used in Velocity", e);
                        }
                        return "";
                    }

                };
            }

            @Override
            public IAxis getGeoYAxis() {
                return convertToAxis(productMetaData_.getGeoYAxis());
            }

            @Override
            public String getGeoXAxisMinValueAsString() {
                return productMetaData_.getGeoXAxisMinValueAsString();
            }

            @Override
            public String getGeoXAxisMinValueAsLonString(IProduct product) {
                try {
                    return productMetaData_.getGeoXAxisMinValueAsLonString(orginalProduct_);
                } catch (Exception e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                }
                return "";
            }

            @Override
            public String getGeoXAxisMaxValueAsString() {
                return productMetaData_.getGeoXAxisMaxValueAsString();
            }

            @Override
            public String getGeoXAxisMaxValueAsLonString(IProduct product) {
                try {
                    return productMetaData_.getGeoXAxisMaxValueAsLonString(orginalProduct_);
                } catch (Exception e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                }
                return "";
            }

            @Override
            public IAxis getGeoXAxisAsLon(IProduct product) {
                return new IAxis() {

                    @Override
                    public String getName() {
                        try {
                            return productMetaData_.getGeoXAxisAsLon(orginalProduct_).getName();
                        } catch (Exception e) {
                            LOGGER.error("Converting Product metadata to be used in Velocity", e);
                        }
                        return "";
                    }

                    @Override
                    public String getUnitsString() {
                        try {
                            return productMetaData_.getGeoXAxisAsLon(orginalProduct_).getUnitsString();
                        } catch (Exception e) {
                            LOGGER.error("Converting Product metadata to be used in Velocity", e);
                        }
                        return "";
                    }

                };
            }

            @Override
            public IAxis getGeoXAxis() {
                return convertToAxis(productMetaData_.getGeoXAxis());
            }

            @Override
            public String getGeoBBoxLonMinAsString() {
                return productMetaData_.getGeoBBoxLonMinAsString();
            }

            @Override
            public String getGeoBBoxLonMaxAsString() {
                return productMetaData_.getGeoBBoxLonMaxAsString();
            }

            @Override
            public String getGeoBBoxLatMinAsString() {
                return productMetaData_.getGeoBBoxLatMinAsString();
            }

            @Override
            public String getGeoBBoxLatMaxAsString() {
                return productMetaData_.getGeoBBoxLatMaxAsString();
            }

            @Override
            public String getFTPServiceURL() {
                return productMetaData_.getFTPServiceURL();
            }

            @Override
            public String getEndTimeCoverageAsUTCString() {
                return productMetaData_.getEndTimeCoverageAsUTCString();
            }

            @Override
            public String getEndTimeCoverageAsUTCString(String dateFormat) {
                return productMetaData_.getEndTimeCoverageAsUTCString(dateFormat);
            }

            @Override
            public String getDepthMinAsString() {
                return productMetaData_.getDepthMinAsString();
            }

            @Override
            public String getDepthMaxAsString() {
                return productMetaData_.getDepthMaxAsString();
            }

            @Override
            public String getBulletinSiteURL() {
                return productMetaData_.getBulletinSiteURL();
            }

            @Override
            public ITimeAxis getTimeAxis() {
                return convertToTimeAxis(productMetaData_.getTimeAxis());
            }

            @Override
            public boolean hasLatLonAxis() {
                return productMetaData_.hasLatLonAxis();
            }

            @Override
            public boolean hasGeoXYAxis() {
                return productMetaData_.hasGeoXYAxis();
            }

            @Override
            public String getLonNormalAxisMinValue() {
                return Double.toString(productMetaData_.getLonNormalAxisMinValue());
            }

            @Override
            public String getLonNormalAxisMaxValue() {
                return Double.toString(productMetaData_.getLonNormalAxisMaxValue());
            }

            @Override
            public String getLatNormalAxisMinValue() {
                return Double.toString(productMetaData_.getLatNormalAxisMinValue());
            }

            @Override
            public String getLatNormalAxisMaxValue() {
                return Double.toString(productMetaData_.getLatNormalAxisMaxValue());
            }

            @Override
            public String getGeoXAxisMinValueAsLonNormal(Product p) {
                try {
                    return Double.toString(productMetaData_.getGeoXAxisMinValueAsLonNormal(p));
                } catch (Exception e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                }
                return "";
            }

            @Override
            public String getGeoXAxisMaxValueAsLonNormal(Product p) {
                try {
                    return Double.toString(productMetaData_.getGeoXAxisMaxValueAsLonNormal(p));
                } catch (Exception e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                }
                return "";
            }

            @Override
            public String getGeoYAxisMinValueAsLatNormal(Product p) {
                try {
                    return Double.toString(productMetaData_.getGeoYAxisMinValueAsLatNormal(p));
                } catch (Exception e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                }
                return "";
            }

            @Override
            public String getGeoYAxisMaxValueAsLatNormal(Product p) {
                try {
                    return Double.toString(productMetaData_.getGeoYAxisMaxValueAsLatNormal(p));
                } catch (Exception e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                }
                return "";
            }

            @Override
            public String getGeoXAxisMinValue() {
                return Double.toString(productMetaData_.getGeoXAxisMinValue());
            }

            @Override
            public String getGeoXAxisMaxValue() {
                return Double.toString(productMetaData_.getGeoXAxisMaxValue());
            }

            @Override
            public String getGeoYAxisMinValue() {
                return Double.toString(productMetaData_.getGeoYAxisMinValue());
            }

            @Override
            public String getGeoYAxisMaxValue() {
                return Double.toString(productMetaData_.getGeoYAxisMaxValue());
            }

            @Override
            public String getTimeAxisMinValueAsUTCString(String datePattern) {
                try {
                    return productMetaData_.getTimeAxisMinValueAsUTCString(datePattern);
                } catch (Exception e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                }
                return "";
            }

            @Override
            public String getTimeAxisMaxValueAsUTCString(String datePattern) {
                try {
                    return productMetaData_.getTimeAxisMaxValueAsUTCString(datePattern);
                } catch (Exception e) {
                    LOGGER.error("Converting Product metadata to be used in Velocity", e);
                }
                return "";
            }

            @Override
            public String getDepthUnits() {
                String depthUnits = productMetaData_.getDepthUnits();
                return depthUnits == null ? "" : depthUnits;
            }

            @Override
            public boolean hasGeoYAxisWithLatEquivalence() {
                return productMetaData_.hasGeoYAxisWithLatEquivalence();
            }

            @Override
            public boolean hasGeoXAxisWithLonEquivalence() {
                return productMetaData_.hasGeoXAxisWithLonEquivalence();
            }

            @Override
            public String getProductType() {
                return productMetaData_.getProductType();
            }

        };
    }

    /**
     * .
     * 
     * @param timeAxis
     * @return
     */
    protected static ITimeAxis convertToTimeAxis(final CoordinateAxis timeAxis) {
        return new ITimeAxis() {

            @Override
            public String getName() {
                return timeAxis.getName();
            }

        };
    }

    /**
     * .
     * 
     * @param pmd
     * @return
     */
    protected static IParameterMetadata convertToParameterMetadata(final ParameterMetaData pmd) {
        return new IParameterMetadata() {

            @Override
            public String getName() {
                return pmd.getName();
            }

            @Override
            public String getLabel() {
                return pmd.getLabel();
            }

            @Override
            public String getStandardName() {
                return pmd.getStandardName();
            }

            @Override
            public String getUnit() {
                return pmd.getUnit();
            }

            @Override
            public String getUnitLong() {
                return pmd.getUnitLong();
            }

            @Override
            public boolean hasDimensions() {
                return pmd.hasDimensions();
            }

            @Override
            public String getDimensionsAsString() {
                return pmd.getDimensionsAsString();
            }

            @Override
            public String getId() {
                return pmd.getId();
            }

        };
    }

    /**
     * .
     * 
     * @param zAxis
     * @return
     */
    protected static IAxis convertToAxis(final CoordinateAxis zAxis) {
        return new IAxis() {

            @Override
            public String getName() {
                return zAxis.getName();
            }

            @Override
            public String getUnitsString() {
                return zAxis.getUnitsString();
            }

        };
    }
}
