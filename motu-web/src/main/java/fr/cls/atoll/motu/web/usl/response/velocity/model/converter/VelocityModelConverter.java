package fr.cls.atoll.motu.web.usl.response.velocity.model.converter;

import java.util.ArrayList;
import java.util.List;

import fr.cls.atoll.motu.library.misc.configuration.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import fr.cls.atoll.motu.web.usl.response.velocity.VelocityTemplateManager;
import fr.cls.atoll.motu.web.usl.response.velocity.model.IService;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.ICatalog;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.ICatalogProduct;
import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.ICatalogProductMetaData;

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

    public static List<IService> converServiceList(List<ConfigService> cfgList_) {
        List<IService> isList = new ArrayList<IService>(cfgList_.size());
        for (final ConfigService cs : cfgList_) {
            isList.add(convertToService(cs));
        }
        return isList;
    }

    public static IService convertToService(final ConfigService cs) {
        return convertToService(cs, null);
    }

    public static IService convertToService(final ConfigService cs, final CatalogData c) {
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
                return VelocityTemplateManager.encodeString(cs.getCatalog().getType());
            }

            @Override
            public ICatalog getCatalog() {
                return convertToICatalog(c);
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

}
