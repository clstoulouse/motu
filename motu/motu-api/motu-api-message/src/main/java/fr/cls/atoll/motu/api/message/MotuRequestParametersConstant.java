package fr.cls.atoll.motu.api.message;

/**
 * Constants that declares the parameter names available for the motu download interface.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public interface MotuRequestParametersConstant {
    /** The Constant ACTION_DELETE. */
    final String ACTION_DELETE = "delete";

    /** Action servlet parameter value to get the metadata of a product (temporal, geographic...) */
    final String ACTION_DESCRIBE_PRODUCT = "describeProduct";

    /** Action servlet parameter value to get the coverage of a dataset (temporal, geographic...) */
    final String ACTION_DESCRIBE_COVERAGE = "describeCoverage";

    /** The Constant ACTION_GET_REQUEST_STATUS. */
    final String ACTION_GET_REQUEST_STATUS = "getreqstatus";

    /** Action servlet parameter value to get size of the extracted data. */
    final String ACTION_GET_SIZE = "getsize";

    /** Action servlet parameter value to get time coverage of a product for a service. */
    final String ACTION_GET_TIME_COVERAGE = "gettimecov";

    /** Action servlet parameter value to list catalog. */
    final String ACTION_LIST_CATALOG = "listcatalog";

    /** Action servlet parameter value to list product metadata. */
    final String ACTION_LIST_PRODUCT_METADATA = "listproductmetadata";

    /** Action servlet parameter value to list catalog. */
    final String ACTION_LIST_SERVICES = "listservices";

    /** Action servlet parameter value just to ping the servlet. */
    final String ACTION_PING = "ping";

    /** Action servlet parameter value to download. */
    final String ACTION_PRODUCT_DOWNLOAD = "productdownload";

    /** Action servlet parameter value to get download product home page. */
    final String ACTION_PRODUCT_DOWNLOADHOME = "productdownloadhome";

    /** Action servlet parameter value to refresh HTML page. */
    final String ACTION_REFRESH = "refresh";

    /** Action servlet parameter name. */
    final String PARAM_ACTION = "action";

    /** The Constant PARAM_FORWARDED_FOR (Real user Ip). */
    final String PARAM_ANONYMOUS = "anonymous";

    /** The Constant PARAM_ANONYMOUS_USER_VALUE. */
    final String PARAM_ANONYMOUS_USER_VALUE = "anonymous";

    /** The Constant PARAM_BATCH. */
    final String PARAM_BATCH = "batch";

    /** Data parameter name. */
    final String PARAM_DATA = "data";

    /** The PARAM output. */
    final String PARAM_OUTPUT = "output";

    /** The PARA m_ ca s_ res t_ suffi x_ url. */
    final String PARAM_CAS_REST_SUFFIX_URL = "cas_rest_suff_url";

    /** The PARAM authentification mode. */
    final String PARAM_AUTHENTIFICATION_MODE = "authmode";

    /** The PARAM authentification mode. */
    final String PARAM_XML_FILE = "xmlfile";

    /**
     * Dataset servlet parameter id. This is an alias of {@link #PARAM_PRODUCT} for the
     * {@link #ACTION_DESCRIBE_COVERAGE} action.
     */
    final String PARAM_DATASET_ID = "datasetID";

    /** End date servlet paremeter name. */
    final String PARAM_END_DATE = "t_hi";

    // /** High Priority Value. */
    // final int HIGH_PRIORITY_VALUE = 1;
    //
    // /** Low Priority Value. */
    // final int LOW_PRIORITY_VALUE = 2;

    /** The Constant PARAM_ANONYMOUS. */
    final String PARAM_FORWARDED_FOR = "forwarded_for";

    /** High latitude servlet paremeter name. */
    final String PARAM_HIGH_LAT = "y_hi";

    /** High longitude servlet paremeter name. */
    final String PARAM_HIGH_LON = "x_hi";

    /** High depth servlet paremeter name. */
    final String PARAM_HIGH_Z = "z_hi";

    /** Language servlet paremeter name. */
    final String PARAM_LANGUAGE = "lang";

    /** Login servlet parameter name. */
    final String PARAM_LOGIN = "login";

    /** Low latitude servlet paremeter name. */
    final String PARAM_LOW_LAT = "y_lo";

    /** Low longitude servlet paremeter name. */
    final String PARAM_LOW_LON = "x_lo";

    /** Low depth servlet paremeter name. */
    final String PARAM_LOW_Z = "z_lo";

    /** The Constant PARAM_MAX_POOL_ANONYMOUS. */
    final String PARAM_MAX_POOL_ANONYMOUS = "maxpoolanonymous";

    /** The Constant PARAM_MAX_POOL_AUTHENTICATE. */
    final String PARAM_MAX_POOL_AUTHENTICATE = "maxpoolauth";

    /** Mode parameter name. */
    final String PARAM_MODE = "mode";

    /** Mode Console parameter value. */
    final String PARAM_MODE_CONSOLE = "console";

    /** Mode Status parameter value. */
    final String PARAM_MODE_STATUS = "status";

    /** Mode Url parameter value. */
    final String PARAM_MODE_URL = "url";

    /** Priority servlet parameter name. */
    final String PARAM_PRIORITY = "priority";

    /** Product servlet parameter name. */
    final String PARAM_PRODUCT = "product";

    /** Password servlet parameter name. */
    final String PARAM_PWD = "pwd";

    /** The Constant PARAM_REQUEST_ID. */
    final String PARAM_REQUEST_ID = "requestid";

    /** Service servlet parameter name. */
    final String PARAM_SERVICE = "service";

    /**
     * Service servlet parameter id. This is an alias of {@link #PARAM_SERVICE} for the
     * {@link #ACTION_DESCRIBE_COVERAGE} action.
     */
    final String PARAM_SERVICE_ID = "serviceID";

    /** Start date servlet paremeter name. */
    final String PARAM_START_DATE = "t_lo";

    /** Variable servlet paremeter name. */
    final String PARAM_VARIABLE = "variable";

    /** The PARA m_ extr a_ metadata. */
    final String PARAM_EXTRA_METADATA = "extraMetadata";

}
