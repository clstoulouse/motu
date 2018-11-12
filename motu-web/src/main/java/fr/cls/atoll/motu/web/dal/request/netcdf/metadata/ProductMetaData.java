/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.web.dal.request.netcdf.metadata;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import fr.cls.atoll.motu.library.converter.DateUtils;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.bll.request.model.RequestProductParameters;
import fr.cls.atoll.motu.web.bll.request.model.metadata.DataProvider;
import fr.cls.atoll.motu.web.bll.request.model.metadata.Delivery;
import fr.cls.atoll.motu.web.bll.request.model.metadata.DocMetaData;
import fr.cls.atoll.motu.web.bll.request.model.metadata.ParameterCategory;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.CoordSysBuilderYXLatLon;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfWriter;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.tds.ncss.model.Property;
import fr.cls.atoll.motu.web.dal.tds.ncss.model.Variables;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.unidata.geoloc.LatLonRect;

//CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * This class represents the metadata of a product. The metadata are similar to all products.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.4 $ - $Date: 2010-03-01 16:01:17 $
 */

/**
 * The Class ProductMetaData.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class ProductMetaData {

    /** The Constant PRODUCT_TYPE_ALONG_TRACK. */
    public static final String PRODUCT_TYPE_ALONG_TRACK = "ALONG_TRACK_PRODUCT";

    /** The Constant QUICKLOOK_KEY. */
    public static final String QUICKLOOK_KEY = "quicklook";

    /** The Constant LAS_URL_KEY. */
    public static final String LAS_URL_KEY = "las";

    /** The Constant FTP_URL_KEY. */
    public static final String FTP_URL_KEY = "ftp";

    /** The Constant PAGE_SITE_WEB_URL_KEY. */
    public static final String PAGE_SITE_WEB_URL_KEY = "info";

    /** The Constant BULLETIN_SITE_URL_KEY. */
    public static final String BULLETIN_SITE_URL_KEY = "bulletinsite";

    /** The Constant QUICKLOOK_FILEEXT. */
    public static final String QUICKLOOK_FILEEXT = ".gif";

    /** The Constant TYPE_KEY. */
    public static final String TYPE_KEY = "type";

    /** The Constant MEDIA_KEY. */
    public static final String MEDIA_KEY = "media";

    /** The Constant TYPE_VALUE_ATP. */
    public static final String TYPE_VALUE_ATP = "along track product";

    /** The Constant TYPE_VALUE_GRID. */
    @SuppressWarnings("unused")
    public static final String TYPE_VALUE_GRID = "gridded product";

    /** The list tds meta data property. */
    private List<Property> listTDSMetaDataProperty = null;

    private boolean hasGeoYAxisWithLatEquivalence;

    /** The tds url path. */
    private String tdsUrlPath;

    /** Type of product. */
    private String productType;

    /** The parameter meta datas map. */
    private Map<String, ParameterMetaData> parameterMetaDatasMap;

    /**
     * Default constructor.
     */
    public ProductMetaData() {
        setTdsUrlPath("");
        setProductType("");
        setCoordinateAxes(new HashMap<AxisType, CoordinateAxis>());
        parameterMetaDatasMap = new HashMap<>();
    }

    /**
     * Compares content of product subtypes list with another product subtypes list.
     * 
     * @param productSubTypesTmp list to be compared
     * 
     * @return the index from where the content is not equal.
     */
    public int compareSubTypes(List<String> productSubTypesTmp) {
        if (productSubTypes.size() != productSubTypesTmp.size()) {
            return -1;
        }
        int i = 0;
        for (i = 0; i < productSubTypes.size(); i++) {
            if (productSubTypes.get(i) != productSubTypesTmp.get(i)) {
                break;
            }
        }
        return i;
    }

    /**
     * Tests if the product is an 'along track' product : (if product's category equals
     * PRODUCT_TYPE_ALONG_TRACK constant or catalog service 'type' equals TYPE_VALUE_ATP constant.
     * 
     * @return Returns true if product type is an 'along track' product.
     */
    public boolean isProductAlongTrack() {
        return productCategory.equalsIgnoreCase(PRODUCT_TYPE_ALONG_TRACK) || getProductTypeServiceValue().equalsIgnoreCase(TYPE_VALUE_ATP);
    }

    /**
     * Checks if is ftp media.
     * 
     * @return true, if is ftp media
     */
    public boolean isFtpMedia() {
        return getProductMediaValue().equalsIgnoreCase(CatalogData.CatalogType.FILE.name());
    }

    /** product id or short name. */
    private String productId = "";

    /**
     * Getter of the property <tt>id</tt>.
     * 
     * @return Returns the id.
     * 
     * @uml.property name="productId"
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Setter of the property <tt>id</tt>.
     * 
     * @param productId The id to set.
     * 
     * @uml.property name="productId"
     */
    public void setProductId(String productId) {
        this.productId = null;
        if (productId != null) {
            this.productId = productId.trim();
        }
    }

    /**
     * Gets the product id encoded.
     * 
     * @return the product id encoded
     */
    public String getProductIdEncoded() {
        return getProductIdEncoded("UTF-8");
    }

    /**
     * Gets the product id encoded.
     * 
     * @param enc the enc
     * @return the product id encoded
     */
    public String getProductIdEncoded(String enc) {
        if (StringUtils.isNullOrEmpty(getProductId())) {
            return "Unknown_product_Id";
        }

        try {
            return URLEncoder.encode(getProductId(), enc);
        } catch (UnsupportedEncodingException e) {
            return productId;
        }
    }

    /**
     * Gets the tds url path.
     * 
     * @return the tds url path
     */
    public String getTdsUrlPath() {
        return tdsUrlPath;
    }

    /**
     * Sets the tds url path.
     * 
     * @param tdsUrlPath the new tds url path
     */
    public void setTdsUrlPath(String tdsUrlPath) {
        this.tdsUrlPath = tdsUrlPath;
    }

    /**
     * Getter of the property <tt>productType</tt>.
     * 
     * @return Returns the productType.
     * 
     * @uml.property name="productType"
     */
    public String getProductType() {
        return productType;
    }

    /**
     * Setter of the property <tt>productType</tt>.
     * 
     * @param productType The productType to set.
     * 
     * @uml.property name="productType"
     */
    public void setProductType(String productType) {
        this.productType = productType;
    }

    /** Category of product ('along track', 'gridded'). */
    private String productCategory = "";

    /**
     * Getter of the property <tt>productCategory</tt>.
     * 
     * @return Returns the productCategory.
     * 
     * @uml.property name="productCategory"
     */
    public String getProductCategory() {
        return productCategory;
    }

    /**
     * Setter of the property <tt>productCategory</tt>.
     * 
     * @param productCategory The productCategory to set.
     * 
     * @uml.property name="productCategory"
     */
    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    /** Level of product (along track or gridded). */
    private String outputType = "";

    /**
     * Getter of the property <tt>outputType</tt>.
     * 
     * @return Returns the outputType.
     * 
     * @uml.property name="outputType"
     */
    public String getOutputType() {
        return outputType;
    }

    /**
     * Setter of the property <tt>outputType</tt>.
     * 
     * @param outputType The outputType to set.
     * 
     * @uml.property name="outputType"
     */
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    /** Spatial resolution in various units (degrees, Km, ...). */
    private String spatialResolution = "";

    /**
     * Getter of the property <tt>spatialResolution</tt>.
     * 
     * @return Returns the spatialResolution.
     * 
     * @uml.property name="spatialResolution"
     */
    public String getSpatialResolution() {
        return spatialResolution;
    }

    /**
     * Setter of the property <tt>spatialResolution</tt>.
     * 
     * @param spatialResolution The spatialResolution to set.
     * 
     * @uml.property name="spatialResolution"
     */
    public void setSpatialResolution(String spatialResolution) {
        this.spatialResolution = spatialResolution;
    }

    /** Temporal resolution - Could be different from process frequency. */
    private String temporalResolution = "";

    /**
     * Getter of the property <tt>temporalResolution</tt>.
     * 
     * @return Returns the temporalResolution.
     * 
     * @uml.property name="temporalResolution"
     */
    public String getTemporalResolution() {
        return temporalResolution;
    }

    /**
     * Setter of the property <tt>temporalResolution</tt>.
     * 
     * @param temporalResolution The temporalResolution to set.
     * 
     * @uml.property name="temporalResolution"
     */
    public void setTemporalResolution(String temporalResolution) {
        this.temporalResolution = temporalResolution;
    }

    /** Frequency of data processings. */
    private String updated = "";

    /**
     * Getter of the property <tt>updated</tt>.
     * 
     * @return Returns the updated.
     * 
     * @uml.property name="updated"
     */
    public String getUpdated() {
        return updated;
    }

    /**
     * Setter of the property <tt>updated</tt>.
     * 
     * @param updated The updated to set.
     * 
     * @uml.property name="updated"
     */
    public void setUpdated(String updated) {
        this.updated = updated;
    }

    /** Last update of a dataset. */
    private String lastUpdate = "Not Available";

    /**
     * Getter of the property <tt>lastUpdate</tt>.
     * 
     * @return Returns the lastUpdate.
     * 
     * @uml.property name="lastUpdate"
     */
    public String getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Setter of the property <tt>lastUpdate</tt>.
     * 
     * @param lastUpdate The updated to set.
     * 
     * @uml.property name="lastUpdate"
     */
    public void setLastUpdate(String lastupdate) {
        this.lastUpdate = lastupdate;
    }

    /** The parameter categories. */
    private Collection<ParameterCategory> parameterCategories;

    /**
     * Getter of the property <tt>parameterCategories</tt>.
     * 
     * @return Returns the parameterCategories.
     * 
     * @uml.property name="parameterCategories"
     */
    public Collection<ParameterCategory> getParameterCategories() {
        return parameterCategories;
    }

    /**
     * Returns an iterator over the elements in this collection.
     * 
     * @return an <tt>Iterator</tt> over the elements in this collection
     * 
     * @see java.util.Collection#iterator()
     * @uml.property name="parameterCategories"
     */
    public Iterator<ParameterCategory> parameterCategoriesIterator() {
        return parameterCategories.iterator();
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     * 
     * @return <tt>true</tt> if this collection contains no elements
     * 
     * @see java.util.Collection#isEmpty()
     * @uml.property name="parameterCategories"
     */
    public boolean isParameterCategoriesEmpty() {
        return parameterCategories.isEmpty();
    }

    /**
     * Contains parameter categories.
     * 
     * @param element whose presence in this collection is to be tested.
     * 
     * @return <tt>true</tt> if this collection contains the specified element.
     * 
     * @see java.util.Collection#contains(Object)
     * @uml.property name="parameterCategories"
     */
    public boolean containsParameterCategories(ParameterCategory element) {
        return parameterCategories.contains(element);
    }

    /**
     * Contains all parameter categories.
     * 
     * @param elements collection to be checked for containment in this collection.
     * 
     * @return <tt>true</tt> if this collection contains all of the elements in the specified collection.
     * 
     * @see java.util.Collection#containsAll(Collection)
     * @uml.property name="parameterCategories"
     */
    public boolean containsAllParameterCategories(Collection<? extends ParameterCategory> elements) {
        return this.parameterCategories.containsAll(elements);
    }

    /**
     * Returns the number of elements in this collection.
     * 
     * @return the number of elements in this collection
     * 
     * @see java.util.Collection#size()
     * @uml.property name="parameterCategories"
     */
    public int parameterCategoriesSize() {
        return parameterCategories.size();
    }

    /**
     * Returns all elements of this collection in an array.
     * 
     * @return an array containing all of the elements in this collection
     * 
     * @see java.util.Collection#toArray()
     * @uml.property name="parameterCategories"
     */
    public ParameterCategory[] parameterCategoriesToArray() {
        return parameterCategories.toArray(new ParameterCategory[parameterCategories.size()]);
    }

    /**
     * Returns an array containing all of the elements in this collection; the runtime type of the returned
     * array is that of the specified array.
     * 
     * @param a the array into which the elements of this collection are to be stored.
     * 
     * @return an array containing all of the elements in this collection
     * 
     * @see java.util.Collection#toArray(Object[])
     * @uml.property name="parameterCategories"
     */
    public <T extends ParameterCategory> T[] parameterCategoriesToArray(T[] a) {
        return this.parameterCategories.toArray(a);
    }

    /**
     * Ensures that this collection contains the specified element (optional operation).
     * 
     * @param element whose presence in this collection is to be ensured.
     * 
     * @return true if this collection changed as a result of the call.
     * 
     * @see java.util.Collection#add(Object)
     * @uml.property name="parameterCategories"
     */
    public boolean addParameterCategories(ParameterCategory element) {
        return parameterCategories.add(element);
    }

    /**
     * Setter of the property <tt>parameterCategories</tt>.
     * 
     * @param parameterCategories the parameterCategories to set.
     * 
     * @uml.property name="parameterCategories"
     */
    public void setParameterCategories(Collection<ParameterCategory> parameterCategories) {
        this.parameterCategories = parameterCategories;
    }

    /**
     * Removes a single instance of the specified element from this collection, if it is present (optional
     * operation).
     * 
     * @param element to be removed from this collection, if present.
     * 
     * @return true if this collection changed as a result of the call.
     * 
     * @see java.util.Collection#add(Object)
     * @uml.property name="parameterCategories"
     */
    public boolean removeParameterCategories(ParameterCategory element) {
        return parameterCategories.remove(element);
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * 
     * @see java.util.Collection#clear()
     * @uml.property name="parameterCategories"
     */
    public void clearParameterCategories() {
        parameterCategories.clear();
    }

    /** title or long name of the product. */
    private String title = "";

    /**
     * Getter of the property <tt>title</tt>.
     * 
     * @return Returns the title.
     * 
     * @uml.property name="title"
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter of the property <tt>title</tt>.
     * 
     * @param title The title to set.
     * 
     * @uml.property name="title"
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Textual description of the product. */
    private String description = "";

    /**
     * Getter of the property <tt>description</tt>.
     * 
     * @return Returns the description.
     * 
     * @uml.property name="description"
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter of the property <tt>description</tt>.
     * 
     * @param description The description to set.
     * 
     * @uml.property name="description"
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** The coordinate system. */
    private CoordinateSystem coordinateSystem;

    /**
     * Getter of the property <tt>coordinateSystem</tt>.
     * 
     * @return Returns the coordinateSystem.
     * 
     * @uml.property name="coordinateSystem"
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Setter of the property <tt>coordinateSystem</tt>.
     * 
     * @param coordinateSystem The coordinateSystem to set.
     * 
     * @uml.property name="coordinateSystem"
     */
    public void setCoordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    /** The data provider. */
    private DataProvider dataProvider;

    /**
     * Getter of the property <tt>dataProvider</tt>.
     * 
     * @return Returns the dataProvider.
     * 
     * @uml.property name="dataProvider"
     */
    public DataProvider getDataProvider() {
        return dataProvider;
    }

    /**
     * Setter of the property <tt>dataProvider</tt>.
     * 
     * @param dataProvider The dataProvider to set.
     * 
     * @uml.property name="dataProvider"
     */
    public void setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    /** The deliveries. */
    private Collection<Delivery> deliveries = null;

    /**
     * Getter of the property <tt>deliveries</tt>.
     * 
     * @return Returns the deliveries.
     * 
     * @uml.property name="deliveries"
     */
    public Collection<Delivery> getDeliveries() {
        return deliveries;
    }

    /**
     * Returns an iterator over the elements in this collection.
     * 
     * @return an <tt>Iterator</tt> over the elements in this collection
     * 
     * @see java.util.Collection#iterator()
     * @uml.property name="deliveries"
     */
    public Iterator<Delivery> deliveriesIterator() {
        return deliveries.iterator();
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     * 
     * @return <tt>true</tt> if this collection contains no elements
     * 
     * @see java.util.Collection#isEmpty()
     * @uml.property name="deliveries"
     */
    public boolean isDeliveriesEmpty() {
        return deliveries.isEmpty();
    }

    /**
     * Contains deliveries.
     * 
     * @param element whose presence in this collection is to be tested.
     * 
     * @return <tt>true</tt> if this collection contains the specified element.
     * 
     * @see java.util.Collection#contains(Object)
     * @uml.property name="deliveries"
     */
    public boolean containsDeliveries(Delivery element) {
        return deliveries.contains(element);
    }

    /**
     * Contains all deliveries.
     * 
     * @param elements collection to be checked for containment in this collection.
     * 
     * @return <tt>true</tt> if this collection contains all of the elements in the specified collection.
     * 
     * @see java.util.Collection#containsAll(Collection)
     * @uml.property name="deliveries"
     */
    public boolean containsAllDeliveries(Collection<? extends Delivery> elements) {
        return this.deliveries.containsAll(elements);
    }

    /**
     * Returns the number of elements in this collection.
     * 
     * @return the number of elements in this collection
     * 
     * @see java.util.Collection#size()
     * @uml.property name="deliveries"
     */
    public int deliveriesSize() {
        return deliveries.size();
    }

    /**
     * Returns all elements of this collection in an array.
     * 
     * @return an array containing all of the elements in this collection
     * 
     * @see java.util.Collection#toArray()
     * @uml.property name="deliveries"
     */
    public Delivery[] deliveriesToArray() {
        return deliveries.toArray(new Delivery[deliveries.size()]);
    }

    /**
     * Returns an array containing all of the elements in this collection; the runtime type of the returned
     * array is that of the specified array.
     * 
     * @param a the array into which the elements of this collection are to be stored.
     * 
     * @return an array containing all of the elements in this collection
     * 
     * @see java.util.Collection#toArray(Object[])
     * @uml.property name="deliveries"
     */
    public <T extends Delivery> T[] deliveriesToArray(T[] a) {
        return this.deliveries.toArray(a);
    }

    /**
     * Ensures that this collection contains the specified element (optional operation).
     * 
     * @param element whose presence in this collection is to be ensured.
     * 
     * @return true if this collection changed as a result of the call.
     * 
     * @see java.util.Collection#add(Object)
     * @uml.property name="deliveries"
     */
    public boolean addDeliveries(Delivery element) {
        return deliveries.add(element);
    }

    /**
     * Setter of the property <tt>deliveries</tt>.
     * 
     * @param deliveries the deliveries to set.
     * 
     * @uml.property name="deliveries"
     */
    public void setDeliveries(Collection<Delivery> deliveries) {
        this.deliveries = deliveries;
    }

    /**
     * Removes a single instance of the specified element from this collection, if it is present (optional
     * operation).
     * 
     * @param element to be removed from this collection, if present.
     * 
     * @return true if this collection changed as a result of the call.
     * 
     * @see java.util.Collection#add(Object)
     * @uml.property name="deliveries"
     */
    public boolean removeDeliveries(Delivery element) {
        return deliveries.remove(element);
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * 
     * @see java.util.Collection#clear()
     * @uml.property name="deliveries"
     */
    public void clearDeliveries() {
        deliveries.clear();
    }

    /** The documentations. */
    private List<DocMetaData> documentations = null;

    /**
     * Getter of the property <tt>documentations</tt>.
     * 
     * @return Returns the documentations.
     * 
     * @uml.property name="documentations"
     */
    public List<DocMetaData> getDocumentations() {
        return documentations;
    }

    /**
     * Returns an iterator over the elements in this collection.
     * 
     * @return an <tt>Iterator</tt> over the elements in this collection
     * 
     * @see java.util.Collection#iterator()
     * @uml.property name="documentations"
     */
    public Iterator<DocMetaData> documentationsIterator() {
        return documentations.iterator();
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     * 
     * @return <tt>true</tt> if this collection contains no elements
     * 
     * @see java.util.Collection#isEmpty()
     * @uml.property name="documentations"
     */
    public boolean isDocumentationsEmpty() {
        return documentations.isEmpty();
    }

    /**
     * Contains documentations.
     * 
     * @param element whose presence in this collection is to be tested.
     * 
     * @return <tt>true</tt> if this collection contains the specified element.
     * 
     * @see java.util.Collection#contains(Object)
     * @uml.property name="documentations"
     */
    public boolean containsDocumentations(DocMetaData element) {
        return documentations.contains(element);
    }

    /**
     * Contains all documentations.
     * 
     * @param elements collection to be checked for containment in this collection.
     * 
     * @return <tt>true</tt> if this collection contains all of the elements in the specified collection.
     * 
     * @see java.util.Collection#containsAll(Collection)
     * @uml.property name="documentations"
     */
    public boolean containsAllDocumentations(List<? extends DocMetaData> elements) {
        return this.documentations.containsAll(elements);
    }

    /**
     * Returns the number of elements in this collection.
     * 
     * @return the number of elements in this collection
     * 
     * @see java.util.Collection#size()
     * @uml.property name="documentations"
     */
    public int documentationsSize() {
        return documentations.size();
    }

    /**
     * Returns all elements of this collection in an array.
     * 
     * @return an array containing all of the elements in this collection
     * 
     * @see java.util.Collection#toArray()
     * @uml.property name="documentations"
     */
    public DocMetaData[] documentationsToArray() {
        return documentations.toArray(new DocMetaData[documentations.size()]);
    }

    /**
     * Returns an array containing all of the elements in this collection; the runtime type of the returned
     * array is that of the specified array.
     * 
     * @param a the array into which the elements of this collection are to be stored.
     * 
     * @return an array containing all of the elements in this collection
     * 
     * @see java.util.Collection#toArray(Object[])
     * @uml.property name="documentations"
     */
    public <T extends DocMetaData> T[] documentationsToArray(T[] a) {
        return this.documentations.toArray(a);
    }

    /**
     * Ensures that this collection contains the specified element (optional operation).
     * 
     * @param element whose presence in this collection is to be ensured.
     * 
     * @return true if this collection changed as a result of the call.
     * 
     * @see java.util.Collection#add(Object)
     * @uml.property name="documentations"
     */
    public boolean addDocumentations(DocMetaData element) {
        return documentations.add(element);
    }

    /**
     * Setter of the property <tt>documentations</tt>.
     * 
     * @param documentations the documentations to set.
     * 
     * @uml.property name="documentations"
     */
    public void setDocumentations(List<DocMetaData> documentations) {
        this.documentations = documentations;
    }

    /**
     * Removes a single instance of the specified element from this collection, if it is present (optional
     * operation).
     * 
     * @param element to be removed from this collection, if present.
     * 
     * @return true if this collection changed as a result of the call.
     * 
     * @see java.util.Collection#add(Object)
     * @uml.property name="documentations"
     */
    public boolean removeDocumentations(DocMetaData element) {
        return documentations.remove(element);
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * 
     * @see java.util.Collection#clear()
     * @uml.property name="documentations"
     */
    public void clearDocumentations() {
        documentations.clear();
    }

    /** is global, regional or local. */
    private String geographicalScale = "";

    /**
     * Getter of the property <tt>geographicalScale</tt>.
     * 
     * @return Returns the geographicalScale.
     * 
     * @uml.property name="geographicalScale"
     */
    public String getGeographicalScale() {
        return geographicalScale;
    }

    /**
     * Setter of the property <tt>geographicalScale</tt>.
     * 
     * @param geographicalScale The geographicalScale to set.
     * 
     * @uml.property name="geographicalScale"
     */
    public void setGeographicalScale(String geographicalScale) {
        this.geographicalScale = geographicalScale;
    }

    /**
     * Reads parameters (variables) metadata from a dataset (netCDF files). Only the first file of the dataset
     * is used to get metadata.
     * 
     * @param dataset dataset to read variables metadata.
     */
    public void readParameterMetaData(RequestProductParameters dataset) {

    }

    /**
     * Reads parameters (variables) metadata from an XML file.
     * 
     * @param url url of the XML file that contains metadata
     */
    public void readParameterMetaData(String url) {

    }

    /**
     * Reads product metadata from an XML file.
     * 
     * @param url url of the XML file that contains metadata
     */
    public void readProductMetaData(String url) {

    }

    /** Type of data calculation (ie. Mercator: hindcast, analysis/outcast, forecast). */
    private String computeType = "";

    /**
     * Getter of the property <tt>computeType</tt>.
     * 
     * @return Returns the computeType.
     * 
     * @uml.property name="computeType"
     */
    public String getComputeType() {
        return this.computeType;
    }

    /**
     * Setter of the property <tt>computeType</tt>.
     * 
     * @param computeType The computeType to set.
     * 
     * @uml.property name="computeType"
     */
    public void setComputeType(String computeType) {
        this.computeType = computeType;
    }

    /** Sub-types of the product (stored hierarchically in the list). */
    private List<String> productSubTypes;

    /**
     * Getter of the property <tt>productSubTypes</tt>.
     * 
     * @return Returns the productSubTypes.
     * 
     * @uml.property name="productSubTypes"
     */
    public List<String> getProductSubTypes() {
        if (productSubTypes == null) {
            productSubTypes = new ArrayList<String>();
        }
        return this.productSubTypes;
    }

    /**
     * Setter of the property <tt>productSubTypes</tt>.
     * 
     * @param productSubTypes The productSubTypes to set.
     * 
     * @uml.property name="productSubTypes"
     */
    public void setProductSubTypes(List<String> productSubTypes) {
        this.productSubTypes = productSubTypes;
    }

    /** The coordinate axes map. */
    private Map<AxisType, CoordinateAxis> coordinateAxisMap = null;

    /**
     * Getter of the property <tt>coordinateAxes</tt>.
     * 
     * @return Returns the coordinateAxesMap.
     * 
     * @uml.property name="coordinateAxes"
     */
    public Map<AxisType, CoordinateAxis> getCoordinateAxisMap() {
        return this.coordinateAxisMap;
    }

    /**
     * Setter of the property <tt>coordinateAxes</tt>.
     * 
     * @param coordinateAxes the coordinateAxesMap to set.
     * 
     * @uml.property name="coordinateAxes"
     */
    public void setCoordinateAxes(Map<AxisType, CoordinateAxis> coordinateAxes) {
        this.coordinateAxisMap = coordinateAxes;
    }

    /**
     * Getter of the property <tt>parameterMetaDatas</tt>.
     * 
     * @return Returns the parameterMetaDatasMap.
     * 
     * @uml.property name="parameterMetaDatas"
     */
    public Map<String, ParameterMetaData> getParameterMetaDataMap() {
        return this.parameterMetaDatasMap;
    }

    /**
     * Gets the parameter meta datas filtered.
     * 
     * @return the parameter meta datas filtered
     */
    public Map<String, ParameterMetaData> getParameterMetaDatasFiltered() {
        Map<String, ParameterMetaData> map = new HashMap<>();
        for (Entry<String, ParameterMetaData> entry : getParameterMetaDataMap().entrySet()) {
            ParameterMetaData parameterMetaData = entry.getValue();
            if (parameterMetaData != null && !parameterMetaData.getName().startsWith(CoordSysBuilderYXLatLon.LAT_LON_COORDINATE_SYSTEM_PREFIX)) {
                map.put(entry.getKey(), parameterMetaData);
            }
        }

        return map;
    }

    /**
     * Returns a set view of the keys contained in this map.
     * 
     * @return a set view of the keys contained in this map.
     * 
     * @see java.util.Map#keySet()
     * @uml.property name="parameterMetaDatas"
     */
    public Set<String> parameterMetaDatasKeySet() {
        return this.parameterMetaDatasMap.keySet();
    }

    /**
     * Gets the parameter meta data from standard name.
     * 
     * @param name the name
     * @return the parameter meta data from standard name
     */
    public ParameterMetaData getParameterMetaDataFromStandardName(String name) {
        ParameterMetaData parameterMetaData = null;

        Collection<ParameterMetaData> list = getParameterMetaDataMap().values();
        for (ParameterMetaData p : list) {
            String standardNameValue = p.getStandardName();
            if (!StringUtils.isNullOrEmpty(standardNameValue)) {
                if (standardNameValue.equals(name)) {
                    parameterMetaData = p;
                    break;
                }

            }
        }
        return parameterMetaData;
    }

    /**
     * Checks for lat lon axis.
     * 
     * @return true if axes collection contains latitude and longitude axes.
     */
    public boolean hasLatLonAxis() {
        return (getLatAxis() != null) && (getLonAxis() != null);
    }

    /**
     * Checks for lat lon axis2 d.
     * 
     * @return true, if successful
     */
    public boolean hasLatLonAxis2D() {
        if (!hasLatLonAxis()) {
            return false;
        }
        return (getLatAxis() instanceof CoordinateAxis2D) && (getLonAxis() instanceof CoordinateAxis2D);
    }

    /**
     * Checks for geo XY axis.
     * 
     * @return true if axes collection contains GeoX and GeoY axes.
     */
    public boolean hasGeoXYAxis() {
        return (getGeoXAxis() != null) && (getGeoYAxis() != null);
    }

    /**
     * Checks for geo XY axis with lon lat equivalence.
     * 
     * @param netCdfReader the net cdf reader
     * 
     * @return true if axes collection contains GeoX with Longitude equivalence and GeoY with Latitude
     *         equivalenceaxes.
     * 
     * @throws MotuException the motu exception
     */
    public boolean hasGeoXYAxisWithLonLatEquivalence() {
        return (hasGeoXAxisWithLonEquivalence() && hasGeoYAxisWithLatEquivalence());
    }

    /**
     * Checks for geographical axis.
     * 
     * @return true if at least one of the axes is a geographical axis.
     */
    public boolean hasGeographicalAxis() {
        return (getZAxis() != null) || (getLatAxis() != null) || (getLonAxis() != null) || (getGeoXAxis() != null) || (getGeoYAxis() != null);
    }

    /**
     * Checks for unknown axis.
     * 
     * @return true if axis type is unknown.
     */
    public boolean hasUnknownAxis() {
        return (getTimeAxis() == null) && (getZAxis() == null) && (getLatAxis() == null) && (getLonAxis() == null) && (getGeoXAxis() == null)
                && (getGeoYAxis() == null);
    }

    /**
     * Checks for time axis.
     * 
     * @return true if time axis exists among coordinate axes.
     */
    public boolean hasTimeAxis() {
        return getTimeAxis() != null;
    }

    /**
     * Gets the time coordinate axis if exists.
     * 
     * @return Time axis if exists, or null.
     */
    public CoordinateAxis getTimeAxis() {
        return getCoordinateAxisMap().get(AxisType.Time);
    }

    /**
     * Gets the minimum value of the time axis.
     * 
     * @return the minimum value as a Date, or null if no time axis
     * 
     * @throws MotuException the motu exception
     */
    public Date getTimeAxisMinValue() throws MotuException {
        CoordinateAxis axis = getTimeAxis();
        if (axis == null) {
            return null;
        }
        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
        return NetCdfReader.getDate(minMax.min, axis.getUnitsString());
    }

    /**
     * Gets the minimum value of the time axis.
     * 
     * @return the minimum value as a double, or Double.NaN if no time axis
     */
    public double getTimeAxisMinValueAsDouble() {
        CoordinateAxis axis = getTimeAxis();
        if (axis == null) {
            return Double.NaN;
        }
        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
        return minMax.min;
    }

    /**
     * Gets the maximum value of the time axis.
     * 
     * @return the maximum value as a Date, or null if no time axis
     * 
     * @throws MotuException the motu exception
     */
    public Date getTimeAxisMaxValue() throws MotuException {
        Date d = null;
        CoordinateAxis axis = getTimeAxis();
        if (axis != null) {
            MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
            d = NetCdfReader.getDate(minMax.max, axis.getUnitsString());
        }
        return d;
    }

    /**
     * Gets the maximum value of the time axis.
     * 
     * @return the maximum value as a double, or Double.NaN if no time axis
     */
    public double getTimeAxisMaxValueAsDouble() {
        double d;
        CoordinateAxis axis = getTimeAxis();
        if (axis == null) {
            d = Double.NaN;
        } else {
            MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
            d = minMax.max;
        }
        return d;
    }

    /**
     * Gets the minimum value of the time axis.
     * 
     * @return the string representation of the minimum value, or null if no time axis
     * 
     * @throws MotuException the motu exception
     */
    public String getTimeAxisMinValueAsString() throws MotuException {
        CoordinateAxis axis = getTimeAxis();
        if (axis == null) {
            return null;
        }
        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
        return NetCdfReader.getDateAsGMTString(minMax.min, axis.getUnitsString());
    }

    /**
     * Gets the maximum value of the time axis.
     * 
     * @return the string representation of the minimum value, or null if no time axis
     * 
     * @throws MotuException the motu exception
     */
    public String getTimeAxisMaxValueAsString() throws MotuException {
        CoordinateAxis axis = getTimeAxis();
        if (axis == null) {
            return null;
        }
        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
        return NetCdfReader.getDateAsGMTString(minMax.max, axis.getUnitsString());
    }

    /**
     * Gets the time axis min value as utc string.
     *
     * @return the time axis min value as utc string
     * @throws MotuException the motu exception
     */
    public String getTimeAxisMinValueAsUTCString() throws MotuException {
        return getTimeAxisMinValueAsUTCString(DateUtils.DATETIME_PATTERN3);
    }

    /**
     * Gets the time axis min value as utc string.
     *
     * @param pattern the pattern
     * @return the time axis min value as utc string
     * @throws MotuException the motu exception
     */
    public String getTimeAxisMinValueAsUTCString(String pattern) throws MotuException {
        Date value = getTimeAxisMinValue();
        return DateUtils.getDateTimeAsUTCString(value, pattern);
    }

    /**
     * Gets the time axis max value as utc string.
     *
     * @return the time axis max value as utc string
     * @throws MotuException the motu exception
     */
    public String getTimeAxisMaxValueAsUTCString() throws MotuException {
        return getTimeAxisMaxValueAsUTCString(DateUtils.DATETIME_PATTERN3);
    }

    /**
     * Gets the time axis max value as utc string.
     *
     * @param pattern the pattern
     * @return the time axis max value as utc string
     * @throws MotuException the motu exception
     */
    public String getTimeAxisMaxValueAsUTCString(String pattern) throws MotuException {
        Date value = getTimeAxisMaxValue();
        return DateUtils.getDateTimeAsUTCString(value, pattern);
    }

    /**
     * Checks for Z axis.
     * 
     * @return true if 'Z' axis (Height or GeoZ) exists among coordinate axes.
     */
    public boolean hasZAxis() {
        return getZAxis() != null;
    }

    /**
     * Gets the 'Z' coordinate axis if exists.
     * 
     * @return 'Z' axis if exists, or null.
     */
    public CoordinateAxis getZAxis() {
        CoordinateAxis axis = getCoordinateAxisMap().get(AxisType.Height);
        if (axis == null) {
            axis = getCoordinateAxisMap().get(AxisType.GeoZ);
        }
        return axis;
    }

    /**
     * Gets the minimum value of the Z axis.
     * 
     * @return the minimum value, or Double.MIN_VALUE if no Z axis
     */

    public double getZAxisMinValue() {
        double zMinValue;
        CoordinateAxis zAxis = getZAxis();
        if (zAxis == null) {
            zMinValue = Double.MIN_VALUE;
        } else {
            MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(zAxis, null);
            zMinValue = minMax.min;
        }
        return zMinValue;
    }

    /**
     * Gets the maximum value of the Z axis.
     * 
     * @return the maximum value, or Double.MAX_VALUE if no Z axis
     */
    public double getZAxisMaxValue() {
        return getMaxValueForAxis(getZAxis());
    }

    /**
     * Gets the minimum value of the Z axis.
     * 
     * @return the string representation of the minimum value, or null if no Z axis
     */
    public String getZAxisMinValueAsString() {
        return getMinValForAxisAsString(getZAxis());
    }

    /**
     * Gets the maximum value of the Z axis.
     * 
     * @return the string representation of the maximum value, or null if no Z axis
     */
    public String getZAxisMaxValueAsString() {
        return getMaxValForAxisAsString(getZAxis());
    }

    /**
     * Gets the axis min max value.
     * 
     * @param axisType the axis type
     * 
     * @return the axis min max value
     */
    public MAMath.MinMax getAxisMinMaxValue(AxisType axisType) {
        MAMath.MinMax mm = null;
        CoordinateAxis axis = getCoordinateAxisMap().get(axisType);
        if (axis != null) {
            mm = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
        }
        return mm;
    }

    /**
     * Checks for lat axis.
     * 
     * @return true if Latitude axis exists among coordinate axes.
     */
    public boolean hasLatAxis() {
        return getLatAxis() != null;
    }

    /**
     * Gets the Latitude coordinate axis if exists.
     * 
     * @return Latitude axis if exists, or null.
     */
    public CoordinateAxis getLatAxis() {
        return getCoordinateAxisMap().get(AxisType.Lat);
    }

    /**
     * Gets the minimum value of the latitude axis.
     * 
     * @return the minimum value, or Double.MIN_VALUE if no latitude axis
     */
    public double getLatAxisMinValue() {
        return getMinValueForAxis(getLatAxis());
    }

    /**
     * Gets the maximum value of the latitude axis.
     * 
     * @return the maximum value, or Double.MAX_VALUE if no latitude axis
     */
    public double getLatAxisMaxValue() {
        return getMaxValueForAxis(getLatAxis());
    }

    /**
     * Gets the minimum value of the latitude axis.
     * 
     * @return the minimum value normalized between +/-90, or Double.MIN_VALUE if no latitude axis
     */
    public double getLatNormalAxisMinValue() {
        return NetCdfReader.getLatNormal(getLatAxisMinValue());
    }

    /**
     * Gets the minimum value of the latitude axis.
     * 
     * @return the string representation of the minimum value, or null if no latitude axis
     */
    public String getLatAxisMinValueAsString() {
        return getMinValForAxisAsString(getLatAxis());
    }

    /**
     * Gets the maximum value of the latitude axis.
     * 
     * @return the string representation of the maximum value, or null if no latitude axis
     */
    public String getLatAxisMaxValueAsString() {
        return getMaxValForAxisAsString(getLatAxis());
    }

    /**
     * Gets the maximum value of the latitude axis.
     * 
     * @return the maximum value normalized between +/-90, or Double.MAX_VALUE if no latitude axis
     */
    public double getLatNormalAxisMaxValue() {
        return NetCdfReader.getLatNormal(getLatAxisMaxValue());
    }

    /**
     * Checks for lon axis.
     * 
     * @return true if Longitude axis exists among coordinate axes.
     */
    public boolean hasLonAxis() {
        return getLonAxis() != null;
    }

    /**
     * Gets the Longitude coordinate axis if exists.
     * 
     * @return Longitude axis if exists, or null.
     */
    public CoordinateAxis getLonAxis() {
        return getCoordinateAxisMap().get(AxisType.Lon);
    }

    /**
     * Gets the minimum value of the longitude axis.
     * 
     * @return the minimum value, or Double.MIN_VALUE if no longitude axis
     */
    public double getLonAxisMinValue() {
        return getMinValueForAxis(getLonAxis());
    }

    /**
     * Gets the minimum value of the longitude axis.
     * 
     * @return the minimum value normalized between +/-180, or Double.MIN_VALUE if no longitude axis
     */
    public double getLonNormalAxisMinValue() {
        return NetCdfReader.getLonNormal(getLonAxisMinValue());
    }

    /**
     * Gets the minimum value of the longitude axis.
     * 
     * @return the string representation of the minimum value, or null if no longitude axis
     */
    public String getLonAxisMinValueAsString() {
        return getMinValForAxisAsString(getLonAxis());
    }

    /**
     * Gets the maximum value of the longitude axis.
     * 
     * @return the string representation of the maximum value, or null if no longitude axis
     */
    public String getLonAxisMaxValueAsString() {
        return getMaxValForAxisAsString(getLonAxis());
    }

    /**
     * Gets the maximum value of the longitude axis.
     * 
     * @return the maximum value, or Double.MAX_VALUE if no longitude axis
     */
    public double getLonAxisMaxValue() {
        return getMaxValueForAxis(getLonAxis());
    }

    /**
     * Gets the maximum value of the longitude axis.
     * 
     * @return the maximum value normalized between +/-180, or Double.MAX_VALUE if no longitude axis
     */
    public double getLonNormalAxisMaxValue() {
        // Do not set it between 180 & +180, keep original value; could be 0;359.xxx
        // NetCdfReader.getLonNormal(getLonAxisMaxValue());
        return getLonAxisMaxValue();
    }

    /**
     * Checks for geo X axis.
     * 
     * @return true if GeoX axis exists among coordinate axes.
     */
    public boolean hasGeoXAxis() {
        return getGeoXAxis() != null;
    }

    /**
     * Checks for geo Y axis with lat equivalence.
     * 
     * @return true if GeoX axis exists among coordinate axes and if there is a longitude variable equivalence
     *         (Variable whose name is 'longitude' and with at least two dimensions X/Y).
     * 
     */
    public boolean hasGeoYAxisWithLatEquivalence() {
        return hasGeoYAxisWithLatEquivalence;
    }

    public boolean hasGeoXAxisWithLonEquivalence() {
        return hasGeoXAxisWithLonEquivalence;
    }

    /**
     * Finds ParameterMetaData corresponding to a longitude name .
     * 
     * @return ParameterMetaData instance if found, otherwise null
     */
    public ParameterMetaData findLongitudeIgnoreCase() {

        ParameterMetaData parameterMetaData = null;
        for (String name : NetCdfReader.LONGITUDE_NAMES) {
            parameterMetaData = getParameterMetaDataMap().get(name);
            if (parameterMetaData != null) {
                break;
            }

            parameterMetaData = getParameterMetaDataFromStandardName(name);

            if (parameterMetaData != null) {
                break;
            }

        }
        return parameterMetaData;
    }

    /**
     * Finds ParameterMetaData corresponding to a latitude name .
     * 
     * @return ParameterMetaData instance if found, otherwise null
     */
    public ParameterMetaData findLatitudeIgnoreCase() {

        ParameterMetaData parameterMetaData = null;
        for (String name : NetCdfReader.LATITUDE_NAMES) {
            parameterMetaData = getParameterMetaDataMap().get(name);
            if (parameterMetaData != null) {
                break;
            }

            parameterMetaData = getParameterMetaDataFromStandardName(name);

            if (parameterMetaData != null) {
                break;
            }
        }
        return parameterMetaData;
    }

    public ParameterMetaData findVariable(String variableName) {
        ParameterMetaData parameterMetaData = null;
        parameterMetaData = getParameterMetaDataMap().get(variableName);
        if (parameterMetaData == null) {
            parameterMetaData = getParameterMetaDataFromStandardName(variableName);
        }

        return parameterMetaData;
    }

    /**
     * Find coordinate axis.
     * 
     * @param axisName the axis name
     * @return the coordinate axis
     */
    public CoordinateAxis findCoordinateAxis(String axisName) {
        if (this.coordinateAxisMap == null) {
            return null;
        }

        Collection<CoordinateAxis> axes = coordinateAxisMap.values();
        for (CoordinateAxis axis : axes) {
            if (axis.getFullName().equalsIgnoreCase(axisName)) {
                return axis;
            }
        }
        return null;
    }

    /**
     * Gets the coordinate axis type.
     * 
     * @param axisName the axis name
     * @return the coordinate axis type
     */
    public AxisType getCoordinateAxisType(String axisName) {
        CoordinateAxis axis = findCoordinateAxis(axisName);
        if (axis == null) {
            return null;
        }
        return axis.getAxisType();
    }

    /**
     * Gets the GeoX coordinate axis if exists.
     * 
     * @return GeoX axis if exists, or null.
     */
    public CoordinateAxis getGeoXAxis() {
        return getCoordinateAxisMap().get(AxisType.GeoX);
    }

    /**
     * Finds longitude variable associated to X axis. (Use for Netcdf product with X/Y axis, ie Mercator Artic
     * area)
     * 
     * @param product product instance of the correspnding metadata.
     * 
     * @return Longitude variable, or null if not found.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuException the motu exception
     */
    public Variable getGeoXAxisAsLon(Product product) throws MotuException, NetCdfVariableNotFoundException {
        CoordinateAxis coord = getGeoXAxis();
        if (coord == null) {
            return null;
        }

        ParameterMetaData parameterMetaData = findLongitudeIgnoreCase();

        if (parameterMetaData == null) {
            return null;
        }

        return product.findVariable(parameterMetaData.getName());

    }

    /**
     * Gets the minimum value of the GeoX axis.
     * 
     * @return the minimum value, or Double.MIN_VALUE if no GeoX axis
     */
    public double getGeoXAxisMinValue() {
        CoordinateAxis axis = getGeoXAxis();
        if (axis == null) {
            return Double.MIN_VALUE;
        }

        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);

        return minMax.min;
    }

    /**
     * Gets the minimum value of the GeoX axis as Longitude.
     * 
     * @param product product instance of the correspnding metadata.
     * 
     * @return the minimum value, or Double.MIN_VALUE if no GeoX axis or no Longitude variable.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public double getGeoXAxisMinValueAsLon(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        Variable variable = getGeoXAxisAsLon(product);
        return product.getMinValue(variable);
    }

    /**
     * Gets the minimum value of the GeoX axis as Longitude.
     * 
     * @param product product instance of the correspnding metadata.
     * 
     * @return the minimum value normalized between +/-180, or Double.MIN_VALUE if no latitude axis
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public double getGeoXAxisMinValueAsLonNormal(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        return NetCdfReader.getLonNormal(getGeoXAxisMinValueAsLon(product));
    }

    /**
     * Gets the minimum value of the GeoX axis.
     * 
     * @return the string representation of the minimum value, or null if no GeoX axis
     */
    public String getGeoXAxisMinValueAsString() {
        CoordinateAxis axis = getGeoXAxis();
        if (axis == null) {
            return null;
        }

        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);

        return NetCdfReader.getStandardGeoXYAsString(minMax.min, axis.getUnitsString());
    }

    /**
     * Gets the minimum value of the GeoX axis as Longitude.
     * 
     * @param product product instance of the corresponding metadata.
     * 
     * @return the string representation of the minimum value, or null if no GeoX axis
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public String getGeoXAxisMinValueAsLonString(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        double min = getGeoXAxisMinValueAsLon(product);
        if (min == Double.MIN_VALUE) {
            return null;
        }
        return NetCdfReader.getStandardLonAsString(min);
    }

    /**
     * Gets the maximum value of the GeoX axis.
     * 
     * @return the maximum value, or Double.MAX_VALUE if no GeoX axis
     */
    public double getGeoXAxisMaxValue() {
        CoordinateAxis axis = getGeoXAxis();
        if (axis == null) {
            return Double.MAX_VALUE;
        }

        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
        return minMax.max;
    }

    /**
     * Gets the maximum value of the GeoX axis as Longitude.
     * 
     * @param product product instance of the corresponding metadata.
     * 
     * @return the maximum value, or Double.MAX_VALUE if no GeoX axis or no Longitude variable.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public double getGeoXAxisMaxValueAsLon(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        Variable variable = getGeoXAxisAsLon(product);
        return product.getMaxValue(variable);
    }

    /**
     * Gets the maximum value of the GeoX axis as Longitude.
     * 
     * @param product product instance of the correspnding metadata.
     * 
     * @return the maximum value normalized between +/-180, or Double.MAX_VALUE if no latitude axis
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public double getGeoXAxisMaxValueAsLonNormal(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        return NetCdfReader.getLonNormal(getGeoXAxisMaxValueAsLon(product));
    }

    /**
     * Gets the maximum value of the GeoX axis.
     * 
     * @return the string representation of the maximum value, or null if no GeoX axis
     */
    public String getGeoXAxisMaxValueAsString() {
        CoordinateAxis axis = getGeoXAxis();
        if (axis == null) {
            return null;
        }

        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
        return NetCdfReader.getStandardGeoXYAsString(minMax.max, axis.getUnitsString());
    }

    /**
     * Gets the maximum value of the GeoX axis as Longitude.
     * 
     * @param product product instance of the corresponding metadata.
     * 
     * @return the string representation of the maximum value, or null if no GeoX axis
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public String getGeoXAxisMaxValueAsLonString(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        double max = getGeoXAxisMaxValueAsLon(product);
        if (max == Double.MAX_VALUE) {
            return null;
        }

        return NetCdfReader.getStandardLonAsString(max);
    }

    /**
     * Checks for geo Y axis.
     * 
     * @return true if GeoY axis exists among coordinate axes.
     */
    public boolean hasGeoYAxis() {
        return getGeoYAxis() != null;
    }

    /**
     * Gets the GeoY coordinate axis if exists.
     * 
     * @return GeoY axis if exists, or null.
     */
    public CoordinateAxis getGeoYAxis() {
        return getCoordinateAxisMap().get(AxisType.GeoY);
    }

    /**
     * Finds latitude variable associated to Y axis. (Use for Netcdf product with X/Y axis, ie Mercator Artic
     * area)
     * 
     * @param product product instance of the correspnding metadata.
     * 
     * @return Latitude variable, or null if not found.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuException the motu exception
     */
    public Variable getGeoYAxisAsLat(Product product) throws MotuException, NetCdfVariableNotFoundException {
        CoordinateAxis coord = getGeoYAxis();
        if (coord == null) {
            return null;
        }

        ParameterMetaData parameterMetaData = findLatitudeIgnoreCase();

        if (parameterMetaData == null) {
            return null;
        }

        return product.findVariable(parameterMetaData.getName());

    }

    /**
     * Gets the minimum value of the GeoY axis.
     * 
     * @return the minimum value, or Double.MIN_VALUE if no GeoY axis
     */
    public double getGeoYAxisMinValue() {
        CoordinateAxis axis = getGeoYAxis();
        if (axis == null) {
            return Double.MIN_VALUE;
        }

        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);

        return minMax.min;
    }

    /**
     * Gets the minimum value of the GeoY axis as Latitude.
     * 
     * @param product product instance of the correspnding metadata.
     * 
     * @return the minimum value, or Double.MIN_VALUE if no GeoY axis or no Latitude variable.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public double getGeoYAxisMinValueAsLat(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        Variable variable = getGeoYAxisAsLat(product);
        return product.getMinValue(variable);
    }

    /**
     * Gets the minimum value of the GeoY axis as Latitude.
     * 
     * @param product product instance of the correspnding metadata.
     * 
     * @return the minimum value normalized between +/-90, or Double.MIN_VALUE if no latitude axis
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public double getGeoYAxisMinValueAsLatNormal(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        return NetCdfReader.getLatNormal(getGeoYAxisMinValueAsLat(product));
    }

    /**
     * Gets the minimum value of the GeoY axis.
     * 
     * @return the string representation of the minimum value, or null if no GeoY axis
     */
    public String getGeoYAxisMinValueAsString() {
        CoordinateAxis axis = getGeoYAxis();
        if (axis == null) {
            return null;
        }

        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);

        return NetCdfReader.getStandardGeoXYAsString(minMax.min, axis.getUnitsString());
    }

    /**
     * Gets the minimum value of the GeoY axis as Latitude.
     * 
     * @param product product instance of the corresponding metadata.
     * 
     * @return the string representation of the minimum value, or null if no GeoX axis
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public String getGeoYAxisMinValueAsLatString(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        double min = getGeoYAxisMinValueAsLat(product);
        if (min == Double.MIN_VALUE) {
            return null;
        }
        return NetCdfReader.getStandardLatAsString(min);
    }

    /**
     * Gets the maximum value of the GeoY axis.
     * 
     * @return the string representation of the maximum value, or Double.MAX_VALUE if no GeoY axis
     */
    public double getGeoYAxisMaxValue() {
        CoordinateAxis axis = getGeoYAxis();
        if (axis == null) {
            return Double.MAX_VALUE;
        }

        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
        return minMax.max;
    }

    /**
     * Gets the maximum value of the GeoY axis as Latitude.
     * 
     * @param product product instance of the correspnding metadata.
     * 
     * @return the maximum value, or Double.MIN_VALUE if no GeoY axis or no Latitude variable.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public double getGeoYAxisMaxValueAsLat(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        Variable variable = getGeoYAxisAsLat(product);
        return product.getMaxValue(variable);
    }

    /**
     * Gets the maximum value of the GeoY axis as Latitude.
     * 
     * @param product product instance of the correspnding metadata.
     * 
     * @return the maximum value normalized between +/-90, or Double.MAX_VALUE if no latitude axis
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public double getGeoYAxisMaxValueAsLatNormal(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        return NetCdfReader.getLatNormal(getGeoYAxisMaxValueAsLat(product));
    }

    /**
     * Gets the maximum value of the GeoY axis.
     * 
     * @return the string representation of the maximum value, or null if no GeoY axis
     */
    public String getGeoYAxisMaxValueAsString() {
        CoordinateAxis axis = getGeoYAxis();
        if (axis == null) {
            return null;
        }

        MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
        return NetCdfReader.getStandardGeoXYAsString(minMax.max, axis.getUnitsString());
    }

    /**
     * Gets the maximum value of the GeoY axis as Latitude.
     * 
     * @param product product instance of the corresponding metadata.
     * 
     * @return the string representation of the maximum value, or null if no GeoX axis
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public String getGeoYAxisMaxValueAsLatString(Product product) throws MotuException, NetCdfVariableNotFoundException, NetCdfVariableException {
        double max = getGeoYAxisMaxValueAsLat(product);
        if (Double.compare(max, Double.MAX_VALUE) == 0) {
            return null;
        }
        return NetCdfReader.getStandardLatAsString(max);
    }

    /**
     * Gets the Latitude and Longitude coordinate axes if both exists.
     * 
     * @return list of Latitude and Longitude coordinate axes if both exists. Otherwise, null.
     */
    public List<CoordinateAxis> getLatLonAxis() {
        List<CoordinateAxis> list = null;
        if (hasLatLonAxis()) {
            list = new ArrayList<>();
            list.add(getLatAxis());
            list.add(getLonAxis());
        }

        return list;
    }

    /**
     * Gets the GeoX and GeoY coordinate axes if both exists.
     * 
     * @return list of GeoX and GeoY coordinate axes if both exists. Otherwise, null.
     */
    public List<CoordinateAxis> getGeoXYAxis() {
        List<CoordinateAxis> list = null;
        if (hasGeoXYAxis()) {
            list = new ArrayList<>();
            list.add(getGeoXAxis());
            list.add(getGeoYAxis());
        }

        return list;
    }

    /**
     * Gets the an instance of DocMetaData from the Documentation collection.
     * 
     * @param name title of the documentation.
     * 
     * @return an instance of DocMetaData, or null if not found. If more than one documentations have If
     *         several documentations have the same title, the first found is returned
     */
    public DocMetaData getDocumentation(String name) {
        DocMetaData docMetaData = null;

        if (getDocumentations() != null) {
            boolean found = false;
            for (Iterator<DocMetaData> it = getDocumentations().iterator(); it.hasNext();) {
                docMetaData = it.next();
                if (docMetaData.getTitle().equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                docMetaData = null;
            }
        }

        return docMetaData;
    }

    // CSOFF: MultipleStringLiterals : avoid message
    /**
     * Gets the URL path to the 'quicklook' image of a variable. The URL path is : 'quicklook' href base + "/"
     * + datasetName (productId) + "/" + variable name (in lowercase) + ".gif" The 'quicklook' href is
     * searched among the Documentation collection of the product's metadata. If the quicklook url doesn't
     * exist, an empty string is returned
     * 
     * @param parameterMetaData instance of a ParameterMetaData (variable) from which to get the quicklook.
     * 
     * @return the URL of the quicklook, or empty string if not found.
     */
    public String getQuickLook(ParameterMetaData parameterMetaData) {
        StringBuilder quickLook = new StringBuilder();
        if (parameterMetaData == null) {
            return "";
        }

        DocMetaData docMetaData = getDocumentation(QUICKLOOK_KEY);
        if (docMetaData == null) {
            return "";
        }

        String base = docMetaData.getResource();
        quickLook.append(base);
        if (!base.endsWith("/")) {
            quickLook.append("/");
        }

        quickLook.append(getProductId());
        quickLook.append("/");
        quickLook.append(parameterMetaData.getName().toLowerCase());
        quickLook.append(QUICKLOOK_FILEEXT);
        // try to open URL
        // if error then return an empty string.
        String urlPath = quickLook.toString();
        InputStream in = null;
        try {
            URL url = new URL(urlPath);
            URLConnection conn = url.openConnection();
            in = conn.getInputStream();
        } catch (Exception e) {
            urlPath = "";
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception io) {
            io.getMessage();
        }

        return urlPath;
    }

    // CSON: MultipleStringLiterals

    /**
     * Gets the URL path to the 'quicklook' image of a variable. The URL path is : 'quicklook' href base +
     * datasetName (productId) + "_" + variable name + ".gif" The 'quicklook' href is searched among the
     * Documentation collection of the product's metadata. If the quicklook url doesn't exist, an empty string
     * is returned
     * 
     * @param varName name of the variable from which to get the quicklook.
     * 
     * @return the URL of the quicklook, or empty string if not found.
     */
    public String getQuickLook(String varName) {
        ParameterMetaData parameterMetaData = getParameterMetaDataMap().get(varName);
        return getQuickLook(parameterMetaData);
    }

    /**
     * Gets the FTP service URL.
     * 
     * @return Returns the URL path to FTP service, or empty string if not found.
     */
    public String getFTPServiceURL() {

        DocMetaData docMetaData = getDocumentation(FTP_URL_KEY);
        if (docMetaData == null) {
            return "";
        }

        return docMetaData.getResource();
    }

    /**
     * Gets the product type service value.
     * 
     * @return Returns the product type service, or empty string if not found.
     */
    public String getProductTypeServiceValue() {

        DocMetaData docMetaData = getDocumentation(TYPE_KEY);
        if (docMetaData == null) {
            return "";
        }

        return docMetaData.getResource();
    }

    /**
     * Gets the product media value.
     * 
     * @return the product media value
     */
    public String getProductMediaValue() {

        DocMetaData docMetaData = getDocumentation(MEDIA_KEY);
        if (docMetaData == null) {
            return "";
        }

        return docMetaData.getResource();
    }

    /**
     * Gets the LAS viewing service URL.
     * 
     * @return Returns the URL path to LAS viewing service, or empty string if not found.
     */
    public String getLASViewingServiceURL() {

        DocMetaData docMetaData = getDocumentation(LAS_URL_KEY);
        if (docMetaData == null) {
            return "";
        }

        return docMetaData.getResource();
    }

    /**
     * Gets the page site web URL.
     * 
     * @return Returns the URL path to Page Site Web, or empty string if not found.
     */
    public String getPageSiteWebURL() {

        DocMetaData docMetaData = getDocumentation(PAGE_SITE_WEB_URL_KEY);
        if (docMetaData == null) {
            return "";
        }

        return docMetaData.getResource();
    }

    /**
     * Gets the bulletin site URL.
     * 
     * @return Returns the URL path to Bulletin Site, or empty string if not found.
     */
    public String getBulletinSiteURL() {

        DocMetaData docMetaData = getDocumentation(BULLETIN_SITE_URL_KEY);
        if (docMetaData == null) {
            return "";
        }

        return docMetaData.getResource();
    }

    /** The time coverage. */
    private Interval timeCoverage = null;

    /**
     * Checks for time coverage.
     * 
     * @return true, if checks for time coverage
     */
    public boolean hasTimeCoverage() {
        return (timeCoverage != null);
    }

    /**
     * Gets the start time coverage as string.
     *
     * @return the start time coverage as string
     */
    public String getStartTimeCoverageAsUTCString() {
        return getStartTimeCoverageAsUTCString(DateUtils.DATETIME_PATTERN3);
    }

    /**
     * Gets the end time coverage as string.
     *
     * @return the end time coverage as string
     */
    public String getEndTimeCoverageAsUTCString() {
        return getEndTimeCoverageAsUTCString(DateUtils.DATETIME_PATTERN3);
    }

    /**
     * Gets the start time coverage as utc string.
     *
     * @param pattern the pattern
     * @return the start time coverage as utc string
     */
    public String getStartTimeCoverageAsUTCString(String pattern) {
        String value = "";
        if (timeCoverage == null) {
            return value;
        }
        return DateUtils.getDateTimeAsUTCString(timeCoverage.getStart(), pattern);
    }

    /**
     * Gets the end time coverage as string.
     *
     * @param pattern the pattern
     * @return the end time coverage as string
     */
    public String getEndTimeCoverageAsUTCString(String pattern) {
        String value = "";
        if (timeCoverage == null) {
            return value;
        }
        return DateUtils.getDateTimeAsUTCString(timeCoverage.getEnd(), pattern);
    }

    /**
     * Gets the time coverage.
     * 
     * @return the time coverage
     */
    public Interval getTimeCoverage() {
        return timeCoverage;
    }

    /**
     * Sets the time coverage.
     * 
     * @param timeEnd the time end
     * @param timeStart the time start
     */
    public void setTimeCoverage(DateTime timeStart, DateTime timeEnd) {
        this.timeCoverage = new Interval(timeStart, timeEnd);
    }

    /**
     * Sets the time coverage.
     * 
     * @param timeEnd the time end
     * @param timeStart the time start
     */
    public void setTimeCoverage(Date timeStart, Date timeEnd) {
        this.timeCoverage = new Interval(timeStart.getTime(), timeEnd.getTime());
    }

    /**
     * Sets the time coverage.
     * 
     * @param timeCoverage the time coverage
     */
    public void setTimeCoverage(Interval timeCoverage) {
        this.timeCoverage = timeCoverage;
    }

    /** The time coverage resolution. */
    private String timeCoverageResolution = null;

    /**
     * Gets the time coverage resolution.
     * 
     * @return the time coverage resolution
     */
    public String getTimeCoverageResolution() {
        return timeCoverageResolution;
    }

    /**
     * Sets the time coverage resolution.
     * 
     * @param timeCoverageResolution the new time coverage resolution
     */
    public void setTimeCoverageResolution(String timeCoverageResolution) {
        this.timeCoverageResolution = timeCoverageResolution;
    }

    /** The geo b box. */
    private LatLonRect geoBBox = null;

    /**
     * Checks for geo b box.
     *
     * @return true, if checks for geo b box
     */
    public boolean hasGeoBBox() {
        return (geoBBox != null);
    }

    /**
     * Gets the geo b box lon max as string.
     *
     * @return the geo b box lon max as string
     */
    public String getGeoBBoxLonMaxAsString() {
        String value = "";
        if (geoBBox == null) {
            return value;
        }

        return Double.toString(geoBBox.getLonMax());
    }

    /**
     * Gets the geo b box lon min as string.
     *
     * @return the geo b box lon min as string
     */
    public String getGeoBBoxLonMinAsString() {
        String value = "";
        if (geoBBox == null) {
            return value;
        }

        return Double.toString(geoBBox.getLonMin());
    }

    /**
     * Gets the geo b box lat max as string.
     *
     * @return the geo b box lat max as string
     */
    public String getGeoBBoxLatMaxAsString() {
        String value = "";
        if (geoBBox == null) {
            return value;
        }

        return Double.toString(geoBBox.getLatMax());
    }

    /**
     * Gets the geo b box lat min as string.
     *
     * @return the geo b box lat min as string
     */
    public String getGeoBBoxLatMinAsString() {
        String value = "";
        if (geoBBox == null) {
            return value;
        }

        return Double.toString(geoBBox.getLatMin());
    }

    /**
     * Gets the geo b box.
     * 
     * @return the geo b box
     */
    public LatLonRect getGeoBBox() {
        return geoBBox;
    }

    /**
     * Sets the geo b box.
     * 
     * @param geoBBox the new geo b box
     */
    public void setGeoBBox(LatLonRect geoBBox) {
        this.geoBBox = geoBBox;
    }

    /** The depth coverage. */
    private MinMax depthCoverage = null;

    /**
     * Checks for depth coverage.
     *
     * @return true, if checks for depth coverage
     */
    public boolean hasDepthCoverage() {
        return (depthCoverage != null);
    }

    /**
     * Gets the depth max as string.
     *
     * @return the depth max as string
     */
    public String getDepthMaxAsString() {
        String value = "";
        if (depthCoverage == null) {
            return value;
        }

        return Double.toString(depthCoverage.max);
    }

    /**
     * Gets the depth min as string.
     *
     * @return the depth min as string
     */
    public String getDepthMinAsString() {
        String value = "";
        if (depthCoverage == null) {
            return value;
        }

        return Double.toString(depthCoverage.min);
    }

    /**
     * Gets the depth coverage.
     * 
     * @return the depth coverage
     */
    public MinMax getDepthCoverage() {
        return depthCoverage;
    }

    /**
     * Sets the depth coverage.
     * 
     * @param depthCoverage the new depth coverage
     */
    public void setDepthCoverage(MinMax depthCoverage) {
        this.depthCoverage = depthCoverage;
    }

    /** The north south resolution. */
    private Double northSouthResolution = null;

    /** The north south units. */
    private String northSouthUnits = null;

    /** The east west resolution. */
    private Double eastWestResolution = null;

    /** The east west units. */
    private String eastWestUnits = null;

    /** The depth resolution. */
    private Double depthResolution = null;

    /** The depth units. */
    private String depthUnits = null;

    /**
     * Gets the north south resolution.
     * 
     * @return the north south resolution
     */
    public Double getNorthSouthResolution() {
        return northSouthResolution;
    }

    /**
     * Sets the north south resolution.
     * 
     * @param northSouthResolution the new north south resolution
     */
    public void setNorthSouthResolution(Double northSouthResolution) {
        this.northSouthResolution = northSouthResolution;
    }

    /**
     * Gets the north south units.
     * 
     * @return the north south units
     */
    public String getNorthSouthUnits() {
        return northSouthUnits;
    }

    /**
     * Sets the north south units.
     * 
     * @param northSouthUnits the new north south units
     */
    public void setNorthSouthUnits(String northSouthUnits) {
        this.northSouthUnits = northSouthUnits;
    }

    /**
     * Gets the east west resolution.
     * 
     * @return the east west resolution
     */
    public Double getEastWestResolution() {
        return eastWestResolution;
    }

    /**
     * Sets the east west resolution.
     * 
     * @param eastWestResolution the new east west resolution
     */
    public void setEastWestResolution(Double eastWestResolution) {
        this.eastWestResolution = eastWestResolution;
    }

    /**
     * Gets the east west units.
     * 
     * @return the east west units
     */
    public String getEastWestUnits() {
        return eastWestUnits;
    }

    /**
     * Sets the east west units.
     * 
     * @param eastWestUnits the new east west units
     */
    public void setEastWestUnits(String eastWestUnits) {
        this.eastWestUnits = eastWestUnits;
    }

    /**
     * Gets the depth resolution.
     * 
     * @return the depth resolution
     */
    public Double getDepthResolution() {
        return depthResolution;
    }

    /**
     * Sets the depth resolution.
     * 
     * @param depthResolution the new depth resolution
     */
    public void setDepthResolution(Double depthResolution) {
        this.depthResolution = depthResolution;
    }

    /**
     * Gets the depth units.
     * 
     * @return the depth units
     */
    public String getDepthUnits() {
        return depthUnits;
    }

    /**
     * Sets the depth units.
     * 
     * @param depthUnits the new depth units
     */
    public void setDepthUnits(String depthUnits) {
        this.depthUnits = depthUnits;
    }

    /** The variables vocabulary. */
    private Variables variablesVocabulary = null;

    private boolean hasGeoXAxisWithLonEquivalence;

    /**
     * Gets the variables vocabulary.
     * 
     * @return the variables vocabulary
     */
    public Variables getVariablesVocabulary() {
        return variablesVocabulary;
    }

    /**
     * Sets the variables vocabulary.
     * 
     * @param variablesVocabulary the new variables vocabulary
     */
    public void setVariablesVocabulary(Variables variablesVocabulary) {
        this.variablesVocabulary = variablesVocabulary;
    }

    /**
     * Gets the list tds meta data property.
     * 
     * @return the list tds meta data property
     */
    public List<Property> getListTDSMetaDataProperty() {
        return listTDSMetaDataProperty;
    }

    /**
     * Sets the list tds meta data property.
     * 
     * @param listTDSMetaDataProperty the new list tds meta data property
     */
    public void setListTDSMetaDataProperty(List<Property> listTDSMetaDataProperty) {
        this.listTDSMetaDataProperty = listTDSMetaDataProperty;
    }

    /**
     * Adds the list tds meta data property.
     * 
     * @param property the property
     */
    public void addListTDSMetaDataProperty(Property property) {
        if (property == null) {
            return;
        }
        if (listTDSMetaDataProperty == null) {
            listTDSMetaDataProperty = new ArrayList<>();
        }

        listTDSMetaDataProperty.add(property);
    }

    public void setGeoYAxisWithLatEquivalence(boolean hasGeoYDimensions) {
        hasGeoYAxisWithLatEquivalence = hasGeoYDimensions;
    }

    public void setGeoXAxisWithLatEquivalence(boolean hasGeoXDimensions) {
        hasGeoXAxisWithLonEquivalence = hasGeoXDimensions;
    }

    public String getMinValForAxisAsString(CoordinateAxis axis) {
        String minValStr = null;
        if (axis != null) {
            MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
            minValStr = NetCdfReader.getStandardLatAsString(minMax.min);
        }
        return minValStr;
    }

    public String getMaxValForAxisAsString(CoordinateAxis axis) {
        String maxValStr = null;
        if (axis != null) {
            MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
            maxValStr = NetCdfReader.getStandardLatAsString(minMax.max);
        }
        return maxValStr;
    }

    public double getMinValueForAxis(CoordinateAxis axis) {
        double minVal;
        if (axis == null) {
            minVal = Double.MIN_VALUE;
        } else {
            MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
            return minMax.min;
        }
        return minVal;
    }

    public double getMaxValueForAxis(CoordinateAxis axis) {
        double zMaxValue;
        if (axis == null) {
            zMaxValue = Double.MAX_VALUE;
        } else {
            MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
            zMaxValue = minMax.max;
        }
        return zMaxValue;
    }
}
// CSON: MultipleStringLiterals
