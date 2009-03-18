package fr.cls.atoll.motu.api;

/**
 * Constantes des noms et valeurs des paramètres pour une requête. <br>
 * <br>
 * Copyright : Copyright (c) 2007 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Jean-Michel FARENC
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */
public interface MotuRequestParametersConstant {

    /** Action servlet parameter name. */
    public static final String PARAM_ACTION = "action";

    /** Login servlet parameter name. */
    public static final String PARAM_LOGIN = "login";

    /** Password servlet parameter name. */
    public static final String PARAM_PWD = "pwd";

    /** Service servlet parameter name. */
    public static final String PARAM_SERVICE = "service";

    /** Product servlet parameter name. */
    public static final String PARAM_PRODUCT = "product";

    /** Variable servlet paremeter name. */
    public static final String PARAM_VARIABLE = "variable";

    /** Start date servlet paremeter name. */
    public static final String PARAM_START_DATE = "t_lo";

    /** End date servlet paremeter name. */
    public static final String PARAM_END_DATE = "t_hi";

    /** Low depth servlet paremeter name. */
    public static final String PARAM_LOW_Z = "z_lo";

    /** High depth servlet paremeter name. */
    public static final String PARAM_HIGH_Z = "z_hi";

    /** Low latitude servlet paremeter name. */
    public static final String PARAM_LOW_LAT = "y_lo";

    /** Low longitude servlet paremeter name. */
    public static final String PARAM_LOW_LON = "x_lo";

    /** High latitude servlet paremeter name. */
    public static final String PARAM_HIGH_LAT = "y_hi";

    /** High longitude servlet paremeter name. */
    public static final String PARAM_HIGH_LON = "x_hi";

    /** Language servlet paremeter name. */
    public static final String PARAM_LANGUAGE = "lang";
    
    /** Priority servlet parameter name. */
    public static final String PARAM_PRIORITY = "priority";

    /** The Constant PARAM_MAX_POOL_ANONYMOUS. */
    public static final String PARAM_MAX_POOL_ANONYMOUS = "maxpoolanonymous";
    
    /** The Constant PARAM_MAX_POOL_AUTHENTICATE. */
    public static final String PARAM_MAX_POOL_AUTHENTICATE = "maxpoolauth";
    
//    /** High Priority Value. */
//    public static final int HIGH_PRIORITY_VALUE = 1;
//
//    /** Low Priority Value. */
//    public static final int LOW_PRIORITY_VALUE = 2;


    /** Data parameter name. */
    public static final String PARAM_DATA = "data";

    /** Mode parameter name. */
    public static final String PARAM_MODE = "mode";

    /** Mode Console parameter value. */
    public static final String PARAM_MODE_CONSOLE = "console";

    /** Mode Url parameter value. */
    public static final String PARAM_MODE_URL = "url";

    /** Mode Status parameter value. */
    public static final String PARAM_MODE_STATUS = "status";
    
    /** The Constant PARAM_REQUEST_ID. */
    public static final String PARAM_REQUEST_ID = "requestid";

    /** The Constant PARAM_BATCH. */
    public static final String PARAM_BATCH = "batch";
    
    /** The Constant PARAM_FORWARDED_FOR (Real user Ip). */
    public static final String PARAM_ANONYMOUS = "anonymous";

    /** The Constant PARAM_ANONYMOUS. */
    public static final String PARAM_FORWARDED_FOR = "forwarded_for";

    /** Action servlet parameter value to refresh HTML page. */
    public static final String ACTION_REFRESH = "refresh";

    /** Action servlet parameter value to list catalog. */
    public static final String ACTION_LIST_SERVICES = "listservices";

    /** Action servlet parameter value to list catalog. */
    public static final String ACTION_LIST_CATALOG = "listcatalog";
    
    /** The Constant ACTION_DELETE. */
    public static final String ACTION_DELETE = "delete";

    /** Action servlet parameter value to list product metadata. */
    public static final String ACTION_LIST_PRODUCT_METADATA = "listproductmetadata";

    /** Action servlet parameter value to get download product home page. */
    public static final String ACTION_PRODUCT_DOWNLOADHOME = "productdownloadhome";

    /** Action servlet parameter value to download. */
    public static final String ACTION_PRODUCT_DOWNLOAD = "productdownload";
    
    /** Action servlet parameter value just to ping the servlet. */
    public static final String ACTION_PING = "ping";
    
    /** Action servlet parameter value to get time coverage of a product for a service. */
    public static final String ACTION_GET_TIME_COVERAGE = "gettimecov";
    
    /** Action servlet parameter value to get size of the extracted data. */
    public static final String ACTION_GET_SIZE = "getsize";
    
    /** The Constant ACTION_GET_REQUEST_STATUS. */
    public static final String ACTION_GET_REQUEST_STATUS = "getreqstatus";

    /** The Constant PARAM_ANONYMOUS_USER_VALUE. */
    public static final String PARAM_ANONYMOUS_USER_VALUE = "anonymous";

}
