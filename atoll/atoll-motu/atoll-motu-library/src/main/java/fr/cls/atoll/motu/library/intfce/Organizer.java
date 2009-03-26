/**
 * 
 */
package fr.cls.atoll.motu.library.intfce;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import fr.cls.atoll.motu.library.configuration.ConfigService;
import fr.cls.atoll.motu.library.configuration.MotuConfig;
import fr.cls.atoll.motu.library.data.ExtractCriteria;
import fr.cls.atoll.motu.library.data.Product;
import fr.cls.atoll.motu.library.data.ProductPersistent;
import fr.cls.atoll.motu.library.data.SelectData;
import fr.cls.atoll.motu.library.data.ServiceData;
import fr.cls.atoll.motu.library.data.ServicePersistent;
import fr.cls.atoll.motu.library.data.VarData;
import fr.cls.atoll.motu.library.data.ServiceData.Language;
import fr.cls.atoll.motu.library.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.exception.MotuExceedingQueueCapacityException;
import fr.cls.atoll.motu.library.exception.MotuExceedingQueueDataCapacityException;
import fr.cls.atoll.motu.library.exception.MotuExceedingUserCapacityException;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.library.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.library.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.library.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.library.exception.MotuInvalidQueuePriorityException;
import fr.cls.atoll.motu.library.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.library.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.metadata.ProductMetaData;
import fr.cls.atoll.motu.library.queueserver.QueueServerManagement;
import fr.cls.atoll.motu.library.sdtnameequiv.StandardNames;
import fr.cls.atoll.motu.library.xml.XMLErrorHandler;
import fr.cls.atoll.motu.library.xml.XMLUtils;
import fr.cls.atoll.motu.msg.MotuMsgConstant;
import fr.cls.atoll.motu.msg.xml.ErrorType;
import fr.cls.atoll.motu.msg.xml.ObjectFactory;
import fr.cls.atoll.motu.msg.xml.RequestSize;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;
import fr.cls.atoll.motu.msg.xml.StatusModeType;
import fr.cls.atoll.motu.msg.xml.TimeCoverage;
import fr.cls.commons.util.PropertiesUtilities;
import fr.cls.commons.util.io.ConfigLoader;
import fr.cls.commons.util5.DatePeriod;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and
// trace log.

/**
 * This class allows to organize and control the sequences of the functions. It is the entry-point of the
 * application.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.3 $ - $Date: 2009-03-26 15:41:53 $
 */
public class Organizer {

    /**
     * Emumeration for available formats.
     */
    public enum Format {

        /** ascii format. */
        ASCII,

        /** html format. */
        HTML,

        /** NetCdf format. */
        NETCDF,

        /** xml format. */
        XML
    }

    /** Number of milliseconds per hour, except when a leap second is inserted. */
    public static final long MILLISECS_PER_HOUR = 60 * Organizer.MILLISECS_PER_MINUTE;

    /**
     * All minutes have this many milliseconds except the last minute of the day on a day defined with a leap
     * second.
     */
    public static final long MILLISECS_PER_MINUTE = 60 * 1000;

    /**
     * Number of leap seconds per day expect on <BR/>
     * 1. days when a leap second has been inserted, e.g. 1999 JAN 1. <BR/>
     * 2. Daylight-savings "spring forward" or "fall back" days.
     */
    protected static final long MILLISECS_PER_DAY = 24 * MILLISECS_PER_HOUR;

    /** The Constant CONFIG_SCHEMA_PACK_NAME. */
    private static final String CONFIG_SCHEMA_PACK_NAME = "fr.cls.atoll.motu.library.configuration";

    /** The Constant DEFAULT_MOTU_PROPS_NAME. */
    private static final String DEFAULT_MOTU_PROPS_NAME = "motu.properties";

    /** The is std name equiv loaded. */
    private static boolean isStdNameEquivLoaded = false;

    /** The jaxb context motu msg. */
    private static JAXBContext jaxbContextMotuMsg = null;

    /** The jaxb context opendap config. */
    private static JAXBContext jaxbContextOpendapConfig = null;

    /** The jaxb context tds config. */
    private static JAXBContext jaxbContextTdsConfig = null;

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(Organizer.class);

    /** The marshaller motu msg. */
    private static Marshaller marshallerMotuMsg = null;

    // /** The Constant MOTU_XSD_RESOURCEPATH. */
    // private static final String MOTU_XSD_RESOURCEPATH = "schema/";
    //
    // /** The Constant MOTU_XSD_FILENAME. */
    // private static final String MOTU_XSD_FILENAME = "MotuConfig.xsd";

    /** Application configuration. */
    private static MotuConfig motuConfig = null;

    /** The Constant OPENDAP_SCHEMA_PACK_NAME. */
    private static final String OPENDAP_SCHEMA_PACK_NAME = "fr.cls.atoll.motu.library.opendap.server";

    /** The props. */
    private static Properties props = null;

    /** The Constant PROPS_MOTU_CONFIG_FILE. */
    private static final String PROPS_MOTU_CONFIG_FILE = "configFile";

    /** The Constant PROPS_MOTU_CONFIG_SCHEMA. */
    private static final String PROPS_MOTU_CONFIG_SCHEMA = "configSchema";

    /** The Constant PROPS_STDNAMES_EQUIV_FILE. */
    private static final String PROPS_STDNAMES_EQUIV_FILE = "sdtNameEquiv";

    /** Persistent info about services and products. */
    private static Map<String, ServicePersistent> servicesPersistent = new HashMap<String, ServicePersistent>();

    /** The Constant STDNAME_EQUIV_PACK_NAME. */
    private static final String STDNAME_EQUIV_PACK_NAME = "fr.cls.atoll.motu.library.sdtnameequiv";

    /** Standard name equivalence (1 standard name --> n netcdf variable names). */
    private static StandardNames stdNameEquiv = null;

    /** The Constant TDS_SCHEMA_PACK_NAME. */
    private static final String TDS_SCHEMA_PACK_NAME = "fr.cls.atoll.motu.library.tds.server";

    /** The unmarshaller opendap config. */
    private static Unmarshaller unmarshallerOpendapConfig = null;;

    /** The unmarshaller tds config. */
    private static Unmarshaller unmarshallerTdsConfig = null;

    /** The current language (default is english). */
    private Language currentLanguage = null;

    /** The current service. */
    private ServiceData currentService = null;

    /** The default service name to use. */
    private String defaultServiceName = "";

    /** The services map. */
    private Map<String, ServiceData> servicesMap = null;

    /** The user. */
    private User user = null;

    /** Velocity template engine. */
    private VelocityEngine velocityEngine = null;

    /**
     * Default contructor.
     * 
     * @throws MotuException the motu exception
     */

    public Organizer() throws MotuException {
        init();
    }

    /**
     * Removes all mappings from this map (optional operation).
     * 
     * @see java.util.Map#clear()
     */
    public static void clearServicesPersistent() {
        Organizer.servicesPersistent.clear();
    }

    /**
     * Convert from bytes to kilobytes.
     * 
     * @param value the value
     * 
     * @return the double
     */
    public static double convertFromBytesToKilobytes(double value) {
        return value / 1024d;
    }

    /**
     * Convert from bytes to megabytes.
     * 
     * @param value the value
     * 
     * @return the double
     */
    public static double convertFromBytesToMegabytes(double value) {
        return Organizer.convertFromBytesToKilobytes(value / 1024d);
    }

    /**
     * Convert from kilobytes to bytes.
     * 
     * @param value the value
     * 
     * @return the double
     */
    public static double convertFromKilobytesToBytes(double value) {
        return value * 1024d;
    }

    /**
     * Convert from megabytes to bytes.
     * 
     * @param value the value
     * 
     * @return the double
     */
    public static double convertFromMegabytesToBytes(double value) {
        return Organizer.convertFromKilobytesToBytes(value * 1024d);
    }

    /**
     * Creates the request size.
     * 
     * @return the request size
     */
    public static RequestSize createRequestSize() {

        ObjectFactory objectFactory = new ObjectFactory();

        RequestSize requestSize = objectFactory.createRequestSize();
        requestSize.setSize(-1d);
        Organizer.setError(requestSize, new MotuException("If you see that message, the request has failed and the error has not been filled"));
        return requestSize;

    }

    /**
     * Creates the request size.
     * 
     * @param e the e
     * 
     * @return the request size
     */
    public static RequestSize createRequestSize(MotuExceptionBase e) {

        RequestSize requestSize = Organizer.createRequestSize();
        Organizer.setError(requestSize, e);
        return requestSize;

    }

    /**
     * Creates the status mode response.
     * 
     * @return the status mode response
     */
    public static StatusModeResponse createStatusModeResponse() {

        ObjectFactory objectFactory = new ObjectFactory();
        StatusModeResponse statusModeResponse = objectFactory.createStatusModeResponse();
        Organizer
                .setError(statusModeResponse, new MotuException("If you see that message, the request has failed and the error has not been filled"));
        return statusModeResponse;

    }

    /**
     * Creates the status mode response.
     * 
     * @param e the e
     * 
     * @return the status mode response
     */
    public static StatusModeResponse createStatusModeResponse(Exception e) {

        StatusModeResponse statusModeResponse = Organizer.createStatusModeResponse();
        Organizer.setError(statusModeResponse, e);
        return statusModeResponse;
    }

    /**
     * Creates the time coverage.
     * 
     * @return the time coverage
     */
    public static TimeCoverage createTimeCoverage() {
        ObjectFactory objectFactory = new ObjectFactory();

        TimeCoverage timeCoverage = objectFactory.createTimeCoverage();
        timeCoverage.setStart(null);
        timeCoverage.setEnd(null);
        Organizer.setError(timeCoverage, new MotuException("If you see that message, the request has failed and the error has not been filled"));
        return timeCoverage;

    }

    /**
     * Creates the time coverage.
     * 
     * @param e the e
     * 
     * @return the time coverage
     */
    public static TimeCoverage createTimeCoverage(MotuExceptionBase e) {

        TimeCoverage timeCoverage = Organizer.createTimeCoverage();
        Organizer.setError(timeCoverage, e);
        return timeCoverage;

    }

    // /**
    // * Gets the loaded product.
    // *
    // * @param productId the product id
    // * @param serviceName the service name
    // *
    // * @return the loaded product
    // *
    // * @throws MotuException the motu exception
    // */
    // public Product getLoadedProduct(String serviceName, String productId) throws MotuException {
    // if (currentService == null) {
    // return null;
    // }
    // if (!(currentService.getName().equalsIgnoreCase(serviceName))) {
    // return null;
    // }
    //
    // Product product = null;
    // product = currentService.getProduct(productId);
    // if (product == null) {
    // return null;
    // }
    //
    // return product;
    // }

    /**
     * Date to XML gregorian calendar.
     * 
     * @param date the date
     * 
     * @return the XML gregorian calendar
     * 
     * @throws MotuException the motu exception
     */
    public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) throws MotuException {
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.setTime(date);
        XMLGregorianCalendar xmlGregorianCalendar;
        try {
            xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
        } catch (DatatypeConfigurationException e) {
            throw new MotuException("ERROR in dateToXMLGregorianCalendar", e);
        }
        return xmlGregorianCalendar;
    }

    /**
     * Gets the error type.
     * 
     * @param e the e
     * 
     * @return the error type
     */
    public static ErrorType getErrorType(Exception e) {

        if (e instanceof MotuInconsistencyException) {
            return ErrorType.INCONSISTENCY;
        } else if (e instanceof MotuInvalidRequestIdException) {
            return ErrorType.UNKNOWN_REQUEST_ID;
        } else if (e instanceof MotuExceedingQueueDataCapacityException) {
            return ErrorType.EXCEEDING_QUEUE_DATA_CAPACITY;
        } else if (e instanceof MotuExceedingQueueCapacityException) {
            return ErrorType.EXCEEDING_QUEUE_CAPACITY;
        } else if (e instanceof MotuExceedingUserCapacityException) {
            return ErrorType.EXCEEDING_USER_CAPACITY;
        } else if (e instanceof MotuInvalidQueuePriorityException) {
            return ErrorType.INVALID_QUEUE_PRIORITY;
        } else if (e instanceof MotuInvalidDateException) {
            return ErrorType.INVALID_DATE;
        } else if (e instanceof MotuInvalidDepthException) {
            return ErrorType.INVALID_DEPTH;
        } else if (e instanceof MotuInvalidLatitudeException) {
            return ErrorType.INVALID_LATITUDE;
        } else if (e instanceof MotuInvalidLongitudeException) {
            return ErrorType.INVALID_LONGITUDE;
        } else if (e instanceof MotuInvalidDateRangeException) {
            return ErrorType.INVALID_DATE_RANGE;
        } else if (e instanceof MotuExceedingCapacityException) {
            return ErrorType.EXCEEDING_CAPACITY;
        } else if (e instanceof MotuNotImplementedException) {
            return ErrorType.NOT_IMPLEMENTED;
        } else if (e instanceof MotuInvalidLatLonRangeException) {
            return ErrorType.INVALID_LAT_LON_RANGE;
        } else if (e instanceof MotuInvalidDepthRangeException) {
            return ErrorType.INVALID_DEPTH_RANGE;
        } else if (e instanceof NetCdfVariableException) {
            return ErrorType.NETCDF_VARIABLE;
        } else if (e instanceof MotuNoVarException) {
            return ErrorType.NO_VARIABLE;
        } else if (e instanceof NetCdfAttributeException) {
            return ErrorType.NETCDF_ATTRIBUTE;
        } else if (e instanceof NetCdfVariableNotFoundException) {
            return ErrorType.NETCDF_VARIABLE_NOT_FOUND;
        } else if (e instanceof MotuException) {
            return ErrorType.SYSTEM;
        } else if (e instanceof MotuExceptionBase) {
            return ErrorType.SYSTEM;
        } else if (e instanceof Exception) {
            return ErrorType.SYSTEM;
        }
        return ErrorType.SYSTEM;
    }

    /**
     * Gets the formatted error.
     * 
     * @param e the e
     * @param service the service
     * 
     * @return the formatted error
     */
    public static String getFormattedError(Exception e, ServiceData service) {

        StringBuffer stringBuffer = new StringBuffer();
        if (e instanceof MotuInvalidDepthException) {
            MotuInvalidDepthException ex = (MotuInvalidDepthException) e;
            stringBuffer.append(ex.getDepthAsString());
            stringBuffer.append(" is an invalid depth");
        } else if (e instanceof MotuInvalidDepthRangeException) {
            MotuInvalidDepthRangeException ex = (MotuInvalidDepthRangeException) e;
            stringBuffer.append("Depth interval ");
            stringBuffer.append(ex.getInvalidRangeAsString());
            stringBuffer.append(" is out of range.\nDepth must intersect the interval ");
            stringBuffer.append(ex.getValidRangeAsString());

        } else if (e instanceof MotuInvalidDateException) {
            MotuInvalidDateException ex = (MotuInvalidDateException) e;
            stringBuffer.append(ex.getDateAsString());
            stringBuffer.append(" is an invalid date");

        } else if (e instanceof MotuInvalidDateRangeException) {
            MotuInvalidDateRangeException ex = (MotuInvalidDateRangeException) e;
            stringBuffer.append("Date ");
            stringBuffer.append(ex.getInvalidRangeAsString());
            stringBuffer.append(" is out of range.\nDate must intersect the interval ");
            stringBuffer.append(ex.getValidRangeAsString());

        } else if (e instanceof MotuInvalidLatitudeException) {
            MotuInvalidLatitudeException ex = (MotuInvalidLatitudeException) e;
            stringBuffer.append(ex.getLatAsString());
            stringBuffer.append(" is an invalid latitude");

        } else if (e instanceof MotuInvalidLongitudeException) {
            MotuInvalidLongitudeException ex = (MotuInvalidLongitudeException) e;
            stringBuffer.append(ex.getLonAsString());
            stringBuffer.append(" is an invalid longitude");

        } else if (e instanceof MotuInvalidLatLonRangeException) {
            MotuInvalidLatLonRangeException ex = (MotuInvalidLatLonRangeException) e;
            stringBuffer.append("Latitude/Longitude bounding box: ");
            stringBuffer.append(ex.getInvalidRectAsString());
            stringBuffer.append(" is out of range.\nLatitude/Longitude bounding box must intersect: ");
            stringBuffer.append(ex.getValidRectAsString());

        } else if (e instanceof MotuExceedingCapacityException) {
            MotuExceedingCapacityException ex = (MotuExceedingCapacityException) e;
            stringBuffer.append("The size of the data to download (");
            stringBuffer.append(ex.getActualAsString());
            stringBuffer.append(") exceeds the maximum allowed (");
            stringBuffer.append(ex.getMaxAsString());
            stringBuffer.append(").\n");
            if (service != null) {
                try {
                    service.getHowTogetExceededData(stringBuffer);
                } catch (MotuException e1) {
                    // Do nothing
                }
            }
        } else if (e instanceof MotuNoVarException) {
            // MotuNoVarException ex = (MotuNoVarException) e;
            stringBuffer.append("You have to select at least one variable to download");
        } else if (e instanceof MotuExceedingQueueDataCapacityException) {
            MotuExceedingQueueDataCapacityException ex = (MotuExceedingQueueDataCapacityException) e;
            stringBuffer.append("The size of the data to download (");
            stringBuffer.append(ex.getActualAsString());
            stringBuffer.append(") exceeds the maximum allowed (");
            stringBuffer.append(ex.getMaxAsString());
            stringBuffer.append(").\n");
            if (service != null) {
                try {
                    service.getHowTogetExceededData(stringBuffer);
                } catch (MotuException e1) {
                    // Do nothing
                }
            }
        } else if (e instanceof MotuExceedingQueueCapacityException) {
            stringBuffer.append("The server is too busy. Please, try to submit your request later");
        } else if (e instanceof MotuExceedingUserCapacityException) {
            stringBuffer.append("The server is too busy. Please, try to submit your request later");
        } else if (e instanceof MotuInvalidRequestIdException) {
            MotuInvalidRequestIdException ex = (MotuInvalidRequestIdException) e;
            stringBuffer.append("A system error has occured. The request id '");
            stringBuffer.append(ex.getIdAsString());
            stringBuffer.append("' is not valid or unknown.\n");
            if (service != null) {
                try {
                    service.getHowTogetExceededData(stringBuffer);
                } catch (MotuException e1) {
                    // Do nothing
                }
            }
        } else if (e instanceof MotuExceptionBase) {
            MotuExceptionBase ex = (MotuExceptionBase) e;
            stringBuffer.append(ex.notifyException());
        } else {
            stringBuffer.append(e.getMessage());
        }

        return stringBuffer.toString();

    }

    /**
     * Gets the jaxb context opendap config.
     * 
     * @return the jaxb context opendap config
     */
    public static JAXBContext getJaxbContextOpendapConfig() {
        return jaxbContextOpendapConfig;
    }

    /**
     * Gets the jaxb context tds config.
     * 
     * @return the jaxb context tds config
     */
    public static JAXBContext getJaxbContextTdsConfig() {
        return jaxbContextTdsConfig;
    }

    /**
     * Getter of the property <tt>motuConfig</tt>.
     * 
     * @return Returns the motuConfig.
     * 
     * @throws MotuException the motu exception
     * 
     * @uml.property name="motuConfig"
     */
    public static synchronized MotuConfig getMotuConfigInstance() throws MotuException {
        if (motuConfig == null) {

            List<String> errors = validateMotuConfig();
            if (errors.size() > 0) {
                StringBuffer stringBuffer = new StringBuffer();
                for (String str : errors) {
                    stringBuffer.append(str);
                    stringBuffer.append("\n");
                }
                throw new MotuException(String.format("ERROR - Motu configuration file '%s' is not valid - See errors below:\n%s", Organizer
                        .getMotuConfigXmlName(), stringBuffer.toString()));
            }

            InputStream in = Organizer.getMotuConfigXml();

            try {
                JAXBContext jc = JAXBContext.newInstance(CONFIG_SCHEMA_PACK_NAME);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                motuConfig = (MotuConfig) unmarshaller.unmarshal(in);
                motuConfig.setExtractionPath(PropertiesUtilities.replaceSystemVariable(motuConfig.getExtractionPath()));
                motuConfig.setDownloadHttpUrl(PropertiesUtilities.replaceSystemVariable(motuConfig.getDownloadHttpUrl()));
                motuConfig.setHttpDocumentRoot(PropertiesUtilities.replaceSystemVariable(motuConfig.getHttpDocumentRoot()));
            } catch (Exception e) {
                throw new MotuException("Error in getMotuConfigInstance", e);
            }

            if (motuConfig == null) {
                throw new MotuException("Unable to load Motu configuration (motuConfig is null)");
            }

            try {
                in.close();
            } catch (IOException io) {
                // Do nothing
            }
        }
        return motuConfig;
    }

    /**
     * Gets the motu config schema.
     * 
     * @return the motu config schema
     * 
     * @throws MotuException the motu exception
     */
    public static InputStream getMotuConfigSchema() throws MotuException {

        String configSchema = Organizer.getMotuConfigSchemaName();
        try {
            return ConfigLoader.getInstance().getAsStream(configSchema);
        } catch (IOException e) {
            throw new MotuException(String.format("'%s' config schema file has not be found", configSchema), e);
        }
        // return Organizer.class.getClassLoader().getResourceAsStream(configSchema);

        // StringBuffer stringBuffer = new StringBuffer();
        // stringBuffer.append(MOTU_XSD_RESOURCEPATH);
        // stringBuffer.append(MOTU_XSD_FILENAME);
        //        
        // return Organizer.class.getClassLoader().getResource(Organizer.getMotuConfigSchemaFullPath());
    }

    /**
     * Gets the motu config schema name.
     * 
     * @return the motu config schema name
     * 
     * @throws MotuException the motu exception
     */
    public static String getMotuConfigSchemaName() throws MotuException {

        return Organizer.getPropertiesInstance().getProperty(PROPS_MOTU_CONFIG_SCHEMA);
    }

    /**
     * Gets the motu config xml.
     * 
     * @return the motu config xml
     * 
     * @throws MotuException the motu exception
     */
    public static InputStream getMotuConfigXml() throws MotuException {

        String configXml = Organizer.getMotuConfigXmlName();
        try {
            return ConfigLoader.getInstance().getAsStream(configXml);
        } catch (IOException e) {
            throw new MotuException(String.format("'%s' config file has not be found", configXml), e);
        }
        // return Organizer.class.getClassLoader().getResourceAsStream(configXml);

        // StringBuffer stringBuffer = new StringBuffer();
        // stringBuffer.append(MOTU_XSD_RESOURCEPATH);
        // stringBuffer.append(MOTU_XSD_FILENAME);
        //        
        // return Organizer.class.getClassLoader().getResource(Organizer.getMotuConfigSchemaFullPath());
    }

    /**
     * Gets the motu config xml name.
     * 
     * @return the motu config xml name
     * 
     * @throws MotuException the motu exception
     */
    public static String getMotuConfigXmlName() throws MotuException {

        return Organizer.getPropertiesInstance().getProperty(PROPS_MOTU_CONFIG_FILE);
    }

    /**
     * Getter of the property <tt>props</tt>.
     * 
     * @return Returns the props.
     * 
     * @throws MotuException the motu exception
     * 
     * @uml.property name="props"
     */
    public static synchronized Properties getPropertiesInstance() throws MotuException {
        if (props == null) {
            // URL url =
            // Organizer.class.getClassLoader().getResource(DEFAULT_MOTU_PROPS_NAME);
            //
            // if (url == null) {
            // throw new MotuException("Motu properties file not found in
            // classpath");
            // }
            // // Read properties file.
            // props = new Properties();
            // try {
            // props.load(new FileInputStream(url.getPath()));
            // } catch (IOException e) {
            // throw new MotuException("Error in getPropertiesInstance",
            // (Throwable) e);
            // }
            // if (props == null) {
            // throw new MotuException(String.format("Unable to load properties
            // file (file:'%s')",
            // url.getPath()));
            // }

            // InputStream in = Organizer.class.getClassLoader().getResourceAsStream(DEFAULT_MOTU_PROPS_NAME);
            InputStream in = null;
            try {
                in = ConfigLoader.getInstance().getAsStream(DEFAULT_MOTU_PROPS_NAME);
            } catch (IOException e1) {
                throw new MotuException("Motu properties file not found in classpath");
            }

            if (in == null) {
                throw new MotuException("Motu properties file not found in classpath");
            }
            // Read properties file.
            props = new Properties();
            try {
                props.load(in);
            } catch (IOException e) {
                throw new MotuException("Error in getPropertiesInstance", e);
            }
            if (props == null) {
                throw new MotuException(String.format("Unable to load properties:'%s'", DEFAULT_MOTU_PROPS_NAME));
            }
        }
        return props;
    }

    /**
     * Returns the value to which this map maps the specified key.
     * 
     * @param key key whose associated value is to be returned.
     * 
     * @return the value to which this map maps the specified key, or <tt>null</tt> if the map contains no
     *         mapping for this key.
     * 
     * @see java.util.Map#get(Object)
     */
    public static ServicePersistent getServicesPersistent(String key) {
        String serviceName = key.toLowerCase();
        ServicePersistent servicePersistent = Organizer.servicesPersistent.get(serviceName);
        if (servicePersistent == null) {
            servicePersistent = new ServicePersistent(serviceName);
            Organizer.putServicesPersistent(serviceName, servicePersistent);
        }
        return servicePersistent;
    }

    /**
     * Gets the services persistent instance.
     * 
     * @return the services persistent instance
     */
    public static Map<String, ServicePersistent> getServicesPersistentInstance() {
        return Organizer.servicesPersistent;
    }

    /**
     * Getter of the property <tt>stdNameEquiv</tt>.
     * 
     * @return Returns the stdNameEquiv.
     * 
     * @throws MotuException the motu exception
     * 
     * @uml.property name="stdNameEquiv"
     */

    public static synchronized StandardNames getStdNameEquiv() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStdNameEquiv() - entering");
        }
        if (isStdNameEquivLoaded) {
            return stdNameEquiv;
        }
        if (stdNameEquiv == null) {
            String file = Organizer.getPropertiesInstance().getProperty(PROPS_STDNAMES_EQUIV_FILE);
            if (file == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getStdNameEquiv() - exiting - No file (file is null)");
                }
                isStdNameEquivLoaded = true;
                return null;
            }
            if (file.equalsIgnoreCase("")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getStdNameEquiv() - exiting - No file (file is empty)");
                }
                isStdNameEquivLoaded = true;
                return null;
            }
            // InputStream in = Organizer.class.getClassLoader().getResourceAsStream(file);
            InputStream in = null;
            try {
                in = ConfigLoader.getInstance().getAsStream(file);
            } catch (IOException e1) {
                // Do Nothing
            }
            if (in == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getStdNameEquiv() - exiting - No file (in is null)");
                }
                isStdNameEquivLoaded = true;
                return null;
            }

            try {
                JAXBContext jc = JAXBContext.newInstance(STDNAME_EQUIV_PACK_NAME);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                stdNameEquiv = (StandardNames) unmarshaller.unmarshal(in);
            } catch (Exception e) {
                LOG.error("getStdNameEquiv()", e);

                throw new MotuException("Error in getStdNameEquiv", e);
            }

            if (stdNameEquiv == null) {
                throw new MotuException("Unable to load standard names equivalence (stdNameEquiv is null)");
            }
            try {
                in.close();
            } catch (IOException io) {
                LOG.error("getStdNameEquiv()", io);

                // Do nothing
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getStdNameEquiv() - exiting");
        }

        isStdNameEquivLoaded = true;

        return stdNameEquiv;
    }

    /**
     * Gets the unmarshaller opendap config.
     * 
     * @return the unmarshaller opendap config
     */
    public static Unmarshaller getUnmarshallerOpendapConfig() {
        return unmarshallerOpendapConfig;
    }

    /**
     * Gets the unmarshaller tds config.
     * 
     * @return the unmarshaller tds config
     */
    public static Unmarshaller getUnmarshallerTdsConfig() {
        return unmarshallerTdsConfig;
    }

    /**
     * Inits the request size.
     * 
     * @param batchQueue the batch queue
     * @param size the size
     * 
     * @return the request size
     */
    public static RequestSize initRequestSize(double size, boolean batchQueue) {

        RequestSize requestSize = Organizer.createRequestSize();

        requestSize.setSize(size);
        requestSize.setCode(ErrorType.OK);
        requestSize.setMsg(ErrorType.OK.toString());

        if (size < 0) {
            Organizer.setError(requestSize, new MotuException("size can't be computed and the cause is unspecified"));
            return requestSize;
        }

        double maxAllowedSizeToSet = 0d;

        double maxAllowedSize = 0d;
        try {
            maxAllowedSize = Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue();
        } catch (MotuException e) {
            Organizer.setError(requestSize, e);
            return requestSize;
        }

        MotuExceptionBase exceptionBase = null;

        if (size > Organizer.convertFromMegabytesToBytes(maxAllowedSize)) {
            exceptionBase = new MotuExceedingCapacityException(Organizer.convertFromBytesToMegabytes(size), maxAllowedSize);
        }

        maxAllowedSizeToSet = maxAllowedSize;

        if (QueueServerManagement.hasInstance()) {
            double maxDataThreshold = 0d;
            try {
                maxDataThreshold = QueueServerManagement.getInstance().getMaxDataThreshold(batchQueue);
            } catch (MotuException e) {
                Organizer.setError(requestSize, e);
                return requestSize;
            }
            if (size > Organizer.convertFromMegabytesToBytes(maxDataThreshold)) {
                exceptionBase = new MotuExceedingQueueDataCapacityException(Organizer.convertFromBytesToMegabytes(size), maxDataThreshold, batchQueue);
            }
            maxAllowedSizeToSet = maxAllowedSizeToSet > maxDataThreshold ? maxDataThreshold : maxAllowedSizeToSet;
        }

        requestSize.setMaxAllowedSize(maxAllowedSizeToSet);

        if (exceptionBase != null) {
            Organizer.setError(requestSize, exceptionBase);
        }

        return requestSize;
    }

    /**
     * Inits the request size.
     * 
     * @param product the product
     * @param batchQueue the batch queue
     * 
     * @return the request size
     * 
     * @throws MotuException the motu exception
     */
    public static RequestSize initRequestSize(Product product, boolean batchQueue) throws MotuException {
        if (product == null) {
            throw new MotuException("ERROR in Organizer.initRequestSize- Product is null");
        }

        return Organizer.initRequestSize(product.getAmountDataSizeAsBytes(), batchQueue);

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
    public static TimeCoverage initTimeCoverage(DatePeriod datePeriod) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initTimeCoverage(DatePeriod) - entering");
        }

        TimeCoverage timeCoverage = Organizer.createTimeCoverage();

        if (datePeriod == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("initTimeCoverage(DatePeriod) - datePeriod is null and exiting");
            }
            return timeCoverage;
        }

        Date start = datePeriod.getBegin();
        Date end = datePeriod.getEnd();

        timeCoverage.setStart(Organizer.dateToXMLGregorianCalendar(start));
        timeCoverage.setEnd(Organizer.dateToXMLGregorianCalendar(end));
        timeCoverage.setCode(ErrorType.OK);
        timeCoverage.setMsg(ErrorType.OK.toString());

        if (LOG.isDebugEnabled()) {
            LOG.debug("initTimeCoverage(DatePeriod) - exiting");
        }
        return timeCoverage;

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
    public static TimeCoverage initTimeCoverage(ProductMetaData productMetaData) throws MotuException {
        if (productMetaData == null) {
            return null;
        }
        DatePeriod datePeriod = productMetaData.getTimeCoverage();
        return Organizer.initTimeCoverage(datePeriod);
    }

    /**
     * Test if a string is null or empty.
     * 
     * @param value string to be tested.
     * 
     * @return true if string is null or empty, otherwise false.
     */
    static public boolean isNullOrEmpty(String value) {
        if (value == null) {
            return true;
        }
        if (value.equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @return <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @see java.util.Map#isEmpty()
     */
    public static boolean isServicesPersistentEmpty() {
        return Organizer.servicesPersistent.isEmpty();
    }

    /**
     * Marshall request size.
     * 
     * @param ex the ex
     * @param writer the writer
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public static void marshallRequestSize(MotuExceptionBase ex, Writer writer) throws MotuMarshallException {

        if (writer == null) {
            return;
        }

        RequestSize requestSize = createRequestSize(ex);
        try {
            synchronized (Organizer.marshallerMotuMsg) {
                Organizer.marshallerMotuMsg.marshal(requestSize, writer);
                writer.flush();
                writer.close();
            }
        } catch (JAXBException e) {
            throw new MotuMarshallException("Error in Organizer - marshallRequestSize", e);
        } catch (IOException e) {
            throw new MotuMarshallException("Error in Organizer - marshallRequestSize", e);
        }
    }

    /**
     * Marshall request size.
     * 
     * @param batchQueue the batch queue
     * @param requestSize the request size
     * @param writer the writer
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public static void marshallRequestSize(RequestSize requestSize, boolean batchQueue, Writer writer) throws MotuMarshallException {
        if (writer == null) {
            return;
        }

        if (requestSize == null) {
            requestSize = initRequestSize(-1d, batchQueue);
        }
        try {
            synchronized (Organizer.marshallerMotuMsg) {
                Organizer.marshallerMotuMsg.marshal(requestSize, writer);
                writer.flush();
                writer.close();
            }
        } catch (JAXBException e) {
            throw new MotuMarshallException("Error in Organizer - marshallRequestSize", e);
        } catch (IOException e) {
            throw new MotuMarshallException("Error in Organizer - marshallRequestSize", e);
        }
    }

    /**
     * Marshall status mode response.
     * 
     * @param ex the ex
     * @param writer the writer
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public static void marshallStatusModeResponse(Exception ex, Writer writer) throws MotuMarshallException {

        if (writer == null) {
            return;
        }

        StatusModeResponse statusModeResponse = Organizer.createStatusModeResponse(ex);
        try {
            synchronized (Organizer.marshallerMotuMsg) {
                Organizer.marshallerMotuMsg.marshal(statusModeResponse, writer);
                writer.flush();
                writer.close();
            }
        } catch (JAXBException e) {
            throw new MotuMarshallException("Error in Organizer - marshallRequestSize", e);
        } catch (IOException e) {
            throw new MotuMarshallException("Error in Organizer - marshallRequestSize", e);
        }
    }

    /**
     * Marshall status mode response.
     * 
     * @param writer the writer
     * @param statusModeResponse the status mode response
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public static void marshallStatusModeResponse(StatusModeResponse statusModeResponse, Writer writer) throws MotuMarshallException {
        if (writer == null) {
            return;
        }

        if (statusModeResponse == null) {
            return;
        }
        try {
            synchronized (Organizer.marshallerMotuMsg) {
                Organizer.marshallerMotuMsg.marshal(statusModeResponse, writer);
                writer.flush();
                writer.close();
            }
        } catch (JAXBException e) {
            throw new MotuMarshallException("Error in Organizer - marshallStatusModeResponse", e);
        } catch (IOException e) {
            throw new MotuMarshallException("Error in Organizer - marshallStatusModeResponse", e);
        }
    }

    /**
     * Marshall time coverage.
     * 
     * @param ex the ex
     * @param writer the writer
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public static void marshallTimeCoverage(MotuExceptionBase ex, Writer writer) throws MotuMarshallException {
        if (writer == null) {
            return;
        }

        TimeCoverage timeCoverage = Organizer.createTimeCoverage(ex);
        try {
            synchronized (Organizer.marshallerMotuMsg) {
                Organizer.marshallerMotuMsg.marshal(timeCoverage, writer);
                writer.flush();
                writer.close();
            }
        } catch (JAXBException e) {
            throw new MotuMarshallException("Error in Organizer - marshallTimeCoverage", e);
        } catch (IOException e) {
            throw new MotuMarshallException("Error in Organizer - marshallTimeCoverage", e);
        }

    }

    /**
     * Marshall time coverage.
     * 
     * @param timeCoverage the time coverage
     * @param writer the writer
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public static void marshallTimeCoverage(TimeCoverage timeCoverage, Writer writer) throws MotuMarshallException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("marshallTimeCoverage(TimeCoverage, Writer) - entering");
        }
        if (writer == null) {
            return;
        }

        try {
            synchronized (Organizer.marshallerMotuMsg) {
                Organizer.marshallerMotuMsg.marshal(timeCoverage, writer);
                writer.flush();
                writer.close();
            }
        } catch (JAXBException e) {
            LOG.error("marshallTimeCoverage(TimeCoverage, Writer)", e);
            throw new MotuMarshallException("Error in Organizer - marshallTimeCoverage", e);
        } catch (IOException e) {
            LOG.error("marshallTimeCoverage(TimeCoverage, Writer)", e);
            throw new MotuMarshallException("Error in Organizer - marshallTimeCoverage", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("marshallTimeCoverage(TimeCoverage, Writer) - exiting");
        }
    }

    /**
     * Associates the specified value with the specified key in this map (optional operation).
     * 
     * @param value value to be associated with the specified key.
     * @param key key with which the specified value is to be associated.
     * 
     * @return previous value associated with specified key, or <tt>null</tt>
     * 
     * @see java.util.Map#put(Object,Object)
     */
    public static ServicePersistent putServicesPersistent(String key, ServicePersistent value) {
        return Organizer.servicesPersistent.put(key.toLowerCase(), value);
    }

    /**
     * Removes the mapping for this key from this map if it is present (optional operation).
     * 
     * @param key key whose mapping is to be removed from the map.
     * 
     * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.
     * 
     * @see java.util.Map#remove(Object)
     */
    public static ServicePersistent removeServicesPersistent(String key) {
        return Organizer.servicesPersistent.remove(key.toLowerCase());
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     * 
     * @param key key whose presence in this map is to be tested.
     * 
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     * 
     * @see java.util.Map#containsKey(Object)
     */
    public static boolean servicesPersistentContainsKey(String key) {
        return Organizer.servicesPersistent.containsKey(key.toLowerCase());
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
     * 
     * @param value value whose presence in this map is to be tested.
     * 
     * @return <tt>true</tt> if this map maps one or more keys to the specified value.
     * 
     * @see java.util.Map#containsValue(Object)
     */
    public static boolean servicesPersistentContainsValue(ServicePersistent value) {
        return Organizer.servicesPersistent.containsValue(value);
    }

    /**
     * Returns a set view of the keys contained in this map.
     * 
     * @return a set view of the keys contained in this map.
     * 
     * @see java.util.Map#keySet()
     */
    public static Set<String> servicesPersistentKeySet() {
        return Organizer.servicesPersistent.keySet();
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return the number of key-value mappings in this map.
     * 
     * @see java.util.Map#size()
     */
    public static int servicesPersistentSize() {
        return Organizer.servicesPersistent.size();
    }

    /**
     * Returns a collection view of the values contained in this map.
     * 
     * @return a collection view of the values contained in this map.
     * 
     * @see java.util.Map#values()
     */
    public static Collection<ServicePersistent> servicesPersistentValues() {
        return Organizer.servicesPersistent.values();
    }

    // CSOFF: StrictDuplicateCode : normal duplication code.

    /**
     * Sets the error.
     * 
     * @param requestSize the request size
     * @param e the e
     */
    public static void setError(RequestSize requestSize, Exception e) {
        ErrorType errorType = Organizer.getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            requestSize.setMsg(e2.notifyException());
        } else {
            requestSize.setMsg(e.getMessage());
        }
        requestSize.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param e the e
     * @param statusModeResponse the status mode response
     */
    public static void setError(StatusModeResponse statusModeResponse, Exception e) {
        ErrorType errorType = Organizer.getErrorType(e);
        statusModeResponse.setStatus(StatusModeType.ERROR);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            statusModeResponse.setMsg(e2.notifyException());
        } else {
            statusModeResponse.setMsg(e.getMessage());
        }
        statusModeResponse.setCode(errorType);

    }

    /**
     * Sets the error.
     * 
     * @param e the e
     * @param timeCoverage the time coverage
     */
    public static void setError(TimeCoverage timeCoverage, Exception e) {
        ErrorType errorType = Organizer.getErrorType(e);
        if (e instanceof MotuExceptionBase) {
            MotuExceptionBase e2 = (MotuExceptionBase) e;
            timeCoverage.setMsg(e2.notifyException());
        } else {
            timeCoverage.setMsg(e.getMessage());
        }
        timeCoverage.setCode(errorType);

    }

    /**
     * Sets the status done.
     * 
     * @param product the product
     * @param statusModeResponse the status mode response
     * 
     * @throws MotuException the motu exception
     */
    public static void setStatusDone(StatusModeResponse statusModeResponse, Product product) throws MotuException {

        String downloadUrlPath = product.getDownloadUrlPath();
        String locationData = product.getExtractLocationData();

        File fileData = new File(locationData);

        Long size = fileData.length();
        size.doubleValue();

        Date lastModified = new Date(fileData.lastModified());

        statusModeResponse.setStatus(StatusModeType.DONE);
        statusModeResponse.setMsg(downloadUrlPath);
        statusModeResponse.setSize(size.doubleValue());
        statusModeResponse.setDateProc(Organizer.dateToXMLGregorianCalendar(lastModified));
        statusModeResponse.setCode(ErrorType.OK);

    }

    /**
     * Validate motu config.
     * 
     * @return a list of XML validation errors (empty is no error)
     * 
     * @throws MotuException the motu exception
     */
    public static List<String> validateMotuConfig() throws MotuException {

        InputStream inSchema = Organizer.getMotuConfigSchema();
        if (inSchema == null) {
            throw new MotuException(String.format("ERROR in Organiser.validateMotuConfig - Motu configuration schema ('%s') not found:", Organizer
                    .getMotuConfigSchemaName()));
        }
        InputStream inXml = Organizer.getMotuConfigXml();
        if (inXml == null) {
            throw new MotuException(String.format("ERROR in Organiser.validateMotuConfig - Motu configuration xml ('%s') not found:", Organizer
                    .getMotuConfigXmlName()));
        }

        XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, inXml);

        if (errorHandler == null) {
            throw new MotuException("ERROR in Organiser.validateMotuConfig - Motu configuration schema : XMLErrorHandler is null");
        }
        return errorHandler.getErrors();

    }

    /**
     * Inits the JAXB.
     * 
     * @throws MotuException the motu exception
     */
    private static synchronized void initJAXB() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXB() - entering");
        }

        initJAXBMotuMsg();
        initJAXBTdsConfig();
        initJAXBOpendapConfig();

        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXB() - exiting");
        }
    }

    /**
     * Inits the JAXB motu msg.
     * 
     * @throws MotuException the motu exception
     */
    private static void initJAXBMotuMsg() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBMotuMsg() - entering");
        }
        if (Organizer.jaxbContextMotuMsg != null) {
            return;
        }

        try {
            Organizer.jaxbContextMotuMsg = JAXBContext.newInstance(MotuMsgConstant.MOTU_MSG_SCHEMA_PACK_NAME);
            Organizer.marshallerMotuMsg = Organizer.jaxbContextMotuMsg.createMarshaller();
            Organizer.marshallerMotuMsg.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        } catch (JAXBException e) {
            LOG.error("initJAXBMotuMsg()", e);
            throw new MotuException("Error in Organizer - initJAXBMotuMsg ", e);

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBMotuMsg() - exiting");
        }
    }

    /**
     * Inits the JAXB opendap config.
     * 
     * @throws MotuException the motu exception
     */
    private static void initJAXBOpendapConfig() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBOpendapConfig() - entering");
        }
        if (Organizer.jaxbContextOpendapConfig != null) {
            return;
        }

        try {
            Organizer.jaxbContextOpendapConfig = JAXBContext.newInstance(Organizer.OPENDAP_SCHEMA_PACK_NAME);
            Organizer.unmarshallerOpendapConfig = Organizer.jaxbContextOpendapConfig.createUnmarshaller();

        } catch (JAXBException e) {
            LOG.error("initJAXBOpendapConfig()", e);
            throw new MotuException("Error in Organizer - initJAXBOpendapConfig ", e);

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBOpendapConfig() - exiting");
        }
    }

    /**
     * Inits the JAXB tds config.
     * 
     * @throws MotuException the motu exception
     */
    private static void initJAXBTdsConfig() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBTdsConfig() - entering");
        }
        if (Organizer.jaxbContextTdsConfig != null) {
            return;
        }

        try {
            Organizer.jaxbContextTdsConfig = JAXBContext.newInstance(Organizer.TDS_SCHEMA_PACK_NAME);
            Organizer.unmarshallerTdsConfig = Organizer.jaxbContextTdsConfig.createUnmarshaller();

        } catch (JAXBException e) {
            LOG.error("initJAXBTdsConfig()", e);
            throw new MotuException("Error in Organizer - initJAXBTdsConfig ", e);

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBTdsConfig() - exiting");
        }
    }

    /**
     * Removes all mappings from this map (optional operation).
     * 
     * @see java.util.Map#clear()
     * @uml.property name="services"
     */
    public void clearServices() {
        this.servicesMap.clear();
    }

    /**
     * Extract data from a product related to the current service and according to criteria (geographical
     * and/or temporal and/or logical expression).
     * 
     * @param serviceName name of the service for the product
     * @param locationData locaton of the data to download (url, filename)
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param criteria list of criteria (geographical coverage, temporal coverage ...)
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param out writer in which response of the extraction will be list.
     * @param responseFormat response output format (HTML, XML, Ascii).
     * @return product object corresponding to the extraction
     * @throws NetCdfAttributeException
     * @throws MotuInvalidDateException
     * @throws MotuInvalidDepthException
     * @throws MotuInvalidLatitudeException
     * @throws MotuInvalidLongitudeException
     * @throws MotuException
     * @throws MotuExceedingCapacityException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidDateRangeException
     * @throws MotuInvalidDepthRangeException
     * @throws NetCdfVariableException
     * @throws MotuNoVarException
     */
    // public Product extractData(String serviceName,
    // String locationData,
    // List<String> listVar,
    // List<ExtractCriteria> criteria,
    // SelectData selectData,
    // Organizer.Format dataOutputFormat,
    // Writer out,
    // Organizer.Format responseFormat) throws MotuInvalidDateException,
    // MotuInvalidDepthException,
    // MotuInvalidLatitudeException, MotuInvalidLongitudeException,
    // MotuException,
    // MotuInvalidDateRangeException,
    // MotuExceedingCapacityException, MotuNotImplementedException,
    // MotuInvalidDepthRangeException,
    // NetCdfVariableException, MotuNoVarException,
    // NetCdfAttributeException {
    //
    // TLog.logger().entering(this.getClass().getName(),
    // "extractData",
    // new Object[] { locationData, listVar, criteria, selectData,
    // dataOutputFormat, responseFormat, });
    // setCurrentService(serviceName);
    // TLog.logger().exiting(this.getClass().getName(), "extractData");
    // return extractData(locationData, listVar, criteria, selectData,
    // dataOutputFormat, out, responseFormat);
    //
    // }
    /**
     * Performs average on a parameter (variable), according criteria selection (optional).
     * 
     * @param product instance of the product.
     * @param varName variable to average.
     * @param criteria extraction criteria (optional).
     * @param dimensions dimensions on which to apply average.
     * 
     * @return instance of a variable containing the result of calculation.
     */
    public VarData computeAverage(String varName, String dimensions, Product product, ExtractCriteria criteria) {
        return null;
    }

    /**
     * Performs variance on a parameter (variable), according criteria selection (optional). Step of
     * sub-sampling is always an integer value greater than 1
     * 
     * @param product instance of the product.
     * @param subSample step of the sub-sampling.
     * @param varName variable to average.
     * @param criteria extraction criteria (optional).
     * 
     * @return instance of a variable containing the result of calculation.
     */
    public VarData computeSubSampling(String varName, Product product, int subSample, ExtractCriteria criteria) {
        return null;
    }

    /**
     * Performs variance on a parameter (variable), according criteria selection (optional).
     * 
     * @param product instance of the product.
     * @param varName variable to average.
     * @param criteria extraction criteria (optional).
     * 
     * @return instance of a variable containing the result of calculation.
     */
    public VarData computeVariance(String varName, Product product, ExtractCriteria criteria) {
        return null;
    }

    /**
     * Loads application properties from a properties file (ie "filename.propterties").
     * 
     * @throws MotuException
     */
    // protected void loadProperties() throws MotuException {
    // TLog.logger().entering(this.getClass().getName(), "loadProperties");
    // URL url =
    // Organizer.class.getClassLoader().getResource(DEFAULT_MOTU_PROPS_NAME);
    //
    // if (url == null) {
    // throw new MotuException("Motu properties file not found in classpath");
    // }
    // // Read properties file.
    // props = new Properties();
    // try {
    // props.load(new FileInputStream(url.getPath()));
    // } catch (IOException e) {
    // throw new MotuException("Error in loadProperties", (Throwable) e);
    // }
    // TLog.logger().exiting(this.getClass().getName(), "loadProperties");
    //
    // }
    /**
     * Loads application's configuration file from Xml file described in PROPS_MOTU_CONFIG_FILE key into
     * application properties file.
     * 
     * @return a configuration object.
     * @throws MotuException
     */
    // public MotuConfig loadConfiguration() throws MotuException {
    // String configFile =
    // Organizer.getPropertiesInstance().getProperty(PROPS_MOTU_CONFIG_FILE);
    // URL url = Organizer.class.getClassLoader().getResource(configFile);
    //
    // return loadConfiguration(url.getPath());
    // }
    /**
     * Loads application's configuration file.
     * 
     * @param path path of the configuration file.
     * @return a configuration object.
     * @throws MotuException
     */
    // public MotuConfig loadConfiguration(String path) throws MotuException {
    // TLog.logger().info("Motu Configuration " + path + " initialisation ");
    //
    // TLog.logger().entering(this.getClass().getName(), "loadConfiguration");
    //
    // FileInputStream in;
    //
    // MotuConfig config;
    // try {
    // JAXBContext jc = JAXBContext.newInstance(CONFIG_SCHEMA_PACK_NAME);
    // Unmarshaller unmarshaller = jc.createUnmarshaller();
    // in = new FileInputStream(path);
    // config = (MotuConfig) unmarshaller.unmarshal(in);
    // } catch (Exception e) {
    // throw new MotuException("Error in loadConfiguration", (Throwable) e);
    // }
    //
    // if (config == null) {
    // throw new MotuException("Unable to load Motu configuration (motuConfig is
    // null)");
    // }
    // try {
    // in.close();
    // } catch (IOException io) {
    // io.getMessage();
    // }
    //
    // TLog.logger().info("Motu Configuration " + path + " initialisation done
    // ");
    // TLog.logger().exiting(this.getClass().getName(), "loadConfiguration");
    // return config;
    // }
    /**
     * Creates a virtual service.
     * 
     * @throws MotuException the motu exception
     */
    public void createVirtualService() throws MotuException {
        // Create a virtual service with default option
        currentService = new ServiceData();
        currentService.setVelocityEngine(this.velocityEngine);
    }

    /**
     * Extract data.
     * 
     * @param params the params
     * 
     * @return the product
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuInconsistencyException the motu inconsistency exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     */
    public Product extractData(ExtractionParameters params) throws MotuInconsistencyException, MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException, IOException {

        params.verifyParameters();

        Product product = null;
        // -------------------------------------------------
        // Data extraction
        // -------------------------------------------------
        if (!Organizer.isNullOrEmpty(params.getLocationData())) {
            product = extractData(params.getServiceName(), params.getLocationData(), params.getListVar(), params.getListTemporalCoverage(), params
                    .getListLatLonCoverage(), params.getListDepthCoverage(), null, params.getDataOutputFormat(), params.getOut(), params
                    .getResponseFormat());
        } else if (!Organizer.isNullOrEmpty(params.getServiceName()) && !Organizer.isNullOrEmpty(params.getProductId())) {
            product = extractData(params.getServiceName(),
                                  params.getListVar(),
                                  params.getListTemporalCoverage(),
                                  params.getListLatLonCoverage(),
                                  params.getListDepthCoverage(),
                                  params.getProductId(),
                                  null,
                                  params.getDataOutputFormat(),
                                  params.getOut(),
                                  params.getResponseFormat());
        } else {
            throw new MotuInconsistencyException(String.format("ERROR in extractData: inconsistency parameters : %s", params.toString()));
        }

        return product;
    }

    /**
     * Extracts data from a location data (url , filename) according to criteria (geographical and/or temporal
     * and/or logical expression).
     * 
     * @param product product to be extracted
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     */
    public void extractData(Product product,
                            List<String> listVar,
                            List<String> listTemporalCoverage,
                            List<String> listLatLonCoverage,
                            List<String> listDepthCoverage,
                            SelectData selectData,
                            Organizer.Format dataOutputFormat) throws MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException, IOException {
        // CSON: StrictDuplicateCode.

        if (this.currentService == null) {
            // Create a virtual service with default option
            createVirtualService();
        }

        extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage, selectData, dataOutputFormat, null, null);
    }

    // CSOFF: StrictDuplicateCode : normal duplication code.
    /**
     * Extracts data from a location data (url , filename) according to criteria (geographical and/or temporal
     * and/or logical expression).
     * 
     * @param product product to download
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param responseFormat response output format (HTML, XML, Ascii).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param out writer in which response of the extraction will be list.
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     */
    public Product extractData(Product product,
                               List<String> listVar,
                               List<String> listTemporalCoverage,
                               List<String> listLatLonCoverage,
                               List<String> listDepthCoverage,
                               SelectData selectData,
                               Organizer.Format dataOutputFormat,
                               Writer out,
                               Organizer.Format responseFormat) throws MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractData() - entering");
        }

        // CSON: StrictDuplicateCode.

        if (this.currentService == null) {
            // Create a virtual service with default option
            createVirtualService();
        }

        if (responseFormat == null || out == null) {
            currentService.extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage, selectData, dataOutputFormat);
            if (LOG.isDebugEnabled()) {
                LOG.debug("extractData() - exiting");
            }
            return product;
        }

        switch (responseFormat) {

        case HTML:
            product = currentService.extractDataHTML(product,
                                                     listVar,
                                                     listTemporalCoverage,
                                                     listLatLonCoverage,
                                                     listDepthCoverage,
                                                     selectData,
                                                     dataOutputFormat,
                                                     out);
            /*
             * extractDataHTML(productId, listVar, geoCriteria, temporalCriteria, selectData, out,
             * dataOutputFormat);
             */
            break;

        case XML:
        case ASCII:
            throw new MotuNotImplementedException(String.format("extractData - Format %s not implemented", responseFormat.toString()));
            // break;

        default:
            throw new MotuException("extractData - Unknown Format");
            // break;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("extractData() - exiting");
        }
        return product;

    }

    // CSOFF: StrictDuplicateCode : normal duplication code.
    /**
     * Extracts data from a location data (url , filename) according to criteria (geographical and/or temporal
     * and/or logical expression).
     * 
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param locationData locaton of the data to download (url, filename)
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     */
    public Product extractData(String locationData,
                               List<String> listVar,
                               List<String> listTemporalCoverage,
                               List<String> listLatLonCoverage,
                               List<String> listDepthCoverage,
                               SelectData selectData,
                               Organizer.Format dataOutputFormat) throws MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException, IOException {
        // CSON: StrictDuplicateCode.

        return extractData(locationData,
                           listVar,
                           listTemporalCoverage,
                           listLatLonCoverage,
                           listDepthCoverage,
                           selectData,
                           dataOutputFormat,
                           null,
                           null);
    }

    // CSOFF: StrictDuplicateCode : normal duplication code.
    /**
     * Extracts data from a location data (url , filename) according to criteria (geographical and/or temporal
     * and/or logical expression).
     * 
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param locationData locaton of the data to download (url, filename)
     * @param responseFormat response output format (HTML, XML, Ascii).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param out writer in which response of the extraction will be list.
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     */
    public Product extractData(String locationData,
                               List<String> listVar,
                               List<String> listTemporalCoverage,
                               List<String> listLatLonCoverage,
                               List<String> listDepthCoverage,
                               SelectData selectData,
                               Organizer.Format dataOutputFormat,
                               Writer out,
                               Organizer.Format responseFormat) throws MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException, IOException {
        // CSON: StrictDuplicateCode.

        Product product = getProductInformation(locationData);

        extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage, selectData, dataOutputFormat, out, responseFormat);

        return product;
    }

    /**
     * Extracts data from a service name and a product id and according to criteria (geographical and/or
     * temporal and/or logical expression).
     * 
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param serviceName name of the service for the product
     * @param productId id of the product
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     */

    public Product extractData(String serviceName,
                               List<String> listVar,
                               List<String> listTemporalCoverage,
                               List<String> listLatLonCoverage,
                               List<String> listDepthCoverage,
                               String productId,
                               SelectData selectData,
                               Organizer.Format dataOutputFormat) throws MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException, IOException {
        // CSON: StrictDuplicateCode

        // setCurrentService(serviceName);
        //
        // Product product = currentService.getProductInformation(productId);
        //
        // extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage,
        // selectData, dataOutputFormat, null, null);
        //
        // return product;
        return extractData(serviceName,
                           listVar,
                           listTemporalCoverage,
                           listLatLonCoverage,
                           listDepthCoverage,
                           productId,
                           selectData,
                           dataOutputFormat,
                           null,
                           null);

    }

    // CSOFF: StrictDuplicateCode : normal duplication code.
    /**
     * Extracts data from a service name and a product id and according to criteria (geographical and/or
     * temporal and/or logical expression).
     * 
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param responseFormat response output format (HTML, XML, Ascii).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param serviceName name of the service for the product
     * @param productId id of the product
     * @param out writer in which response of the extraction will be list.
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     */

    public Product extractData(String serviceName,
                               List<String> listVar,
                               List<String> listTemporalCoverage,
                               List<String> listLatLonCoverage,
                               List<String> listDepthCoverage,
                               String productId,
                               SelectData selectData,
                               Organizer.Format dataOutputFormat,
                               Writer out,
                               Organizer.Format responseFormat) throws MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException, IOException {
        // CSON: StrictDuplicateCode

        ServicePersistent servicePersistent = null;
        if (!Organizer.servicesPersistentContainsKey(serviceName)) {
            loadCatalogInfo(serviceName);
        }

        setCurrentService(serviceName);

        servicePersistent = Organizer.getServicesPersistent(serviceName);

        ProductPersistent productPersistent = servicePersistent.getProductsPersistent(productId);
        if (productPersistent == null) {
            throw new MotuException(String.format("ERROR in extractData - product '%s' not found in persistent map", productId));
        }

        String locationData = productPersistent.getUrl();

        Product product = extractData(serviceName,
                                      locationData,
                                      listVar,
                                      listTemporalCoverage,
                                      listLatLonCoverage,
                                      listDepthCoverage,
                                      selectData,
                                      dataOutputFormat,
                                      out,
                                      responseFormat);
        return product;
        //
        // setCurrentService(serviceName);
        // Product product = currentService.getProductInformation(productId);
        //
        // extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage,
        // selectData, dataOutputFormat, out, responseFormat);
        //
        // return product;

    }

    // CSOFF: StrictDuplicateCode : normal duplication code.
    /**
     * Extracts data from a location data (url , filename) and according to criteria (geographical and/or
     * temporal and/or logical expression).
     * 
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param locationData locaton of the data to download (url, filename)
     * @param responseFormat response output format (HTML, XML, Ascii).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param serviceName name of the service for the product
     * @param out writer in which response of the extraction will be list.
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     */

    public Product extractData(String serviceName,
                               String locationData,
                               List<String> listVar,
                               List<String> listTemporalCoverage,
                               List<String> listLatLonCoverage,
                               List<String> listDepthCoverage,
                               SelectData selectData,
                               Organizer.Format dataOutputFormat,
                               Writer out,
                               Organizer.Format responseFormat) throws MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException, IOException {

        // CSON: StrictDuplicateCode
        if (!Organizer.isNullOrEmpty(serviceName)) {
            setCurrentService(serviceName);
        }

        return extractData(locationData,
                           listVar,
                           listTemporalCoverage,
                           listLatLonCoverage,
                           listDepthCoverage,
                           selectData,
                           dataOutputFormat,
                           out,
                           responseFormat);
        // Product product = getProductInformation(locationData);
        //
        // extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage,
        // selectData, dataOutputFormat, out, responseFormat);
        //
        // return product;
    }

    /**
     * Gets the amount data size.
     * 
     * @param params the params
     * 
     * @return the amount data size
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuInconsistencyException the motu inconsistency exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuMarshallException the motu marshall exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public Product getAmountDataSize(ExtractionParameters params) throws MotuInconsistencyException, MotuInvalidDateException,
            MotuInvalidDepthException, MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException, MotuMarshallException {

        params.verifyParameters();
        Product product = null;

        if (!Organizer.isNullOrEmpty(params.getLocationData())) {
            product = getAmountDataSize(params.getLocationData(), params.getListVar(), params.getListTemporalCoverage(), params
                    .getListLatLonCoverage(), params.getListDepthCoverage(), params.getOut(), params.isBatchQueue());
        } else if (!Organizer.isNullOrEmpty(params.getServiceName()) && !Organizer.isNullOrEmpty(params.getProductId())) {
            product = getAmountDataSize(params.getServiceName(), params.getListVar(), params.getListTemporalCoverage(), params
                    .getListLatLonCoverage(), params.getListDepthCoverage(), params.getProductId(), params.getOut(), params.isBatchQueue());
        } else {
            throw new MotuInconsistencyException(String.format("ERROR in getAmountDataSize: inconsistency parameters : %s", params.toString()));
        }

        return product;
    }

    /**
     * Gets the amount data size.
     * 
     * @param listVar the list var
     * @param locationData the location data
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * 
     * @return the amount data size
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public Product getAmountDataSize(String locationData,
                                     List<String> listVar,
                                     List<String> listTemporalCoverage,
                                     List<String> listLatLonCoverage,
                                     List<String> listDepthCoverage) throws MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfVariableNotFoundException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAmountDataSize(String, List<String>, List<String>, List<String>, List<String>) - entering");
        }

        // CSON: StrictDuplicateCode.

        Product product = null;
        try {
            product = getProductInformation(locationData);
            currentService.computeAmountDataSize(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage);
        } catch (NetCdfAttributeException e) {
            // Do nothing;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getAmountDataSize(String, List<String>, List<String>, List<String>, List<String>) - exiting");
        }
        return product;

    }

    //
    // public static synchronized void validateMotuConfig2() throws MotuException {
    // try {
    // String configFile = Organizer.getPropertiesInstance().getProperty(PROPS_MOTU_CONFIG_FILE);
    // URL xml = Organizer.class.getClassLoader().getResource(configFile);
    // URL url = Organizer.getMotuConfigSchema();
    // XMLErrorHandler errorHandler = new XMLErrorHandler();
    // DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // //factory.setFeature("http://xml.org/sax/features/validation", true);
    // factory.setFeature("http://apache.org/xml/features/validation/schema", true);
    // factory.setAttribute("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
    // url.toString());
    // SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    // Schema schema = schemaFactory.newSchema(new StreamSource(new File(url.toURI())));
    // factory.setSchema(schema);
    // System.out.println("Schema created successfully.");
    //
    // DocumentBuilder documentBuilder = factory.newDocumentBuilder();
    // documentBuilder.setErrorHandler(errorHandler);
    //
    // try {
    // Document document = documentBuilder.parse(new File(xml.toURI()));
    // } catch (SAXException e) {
    // System.err.println(e.getMessage());
    // System.err.println("Validation failed..");
    // }
    // System.out.println("Total (warning, error, fatal) errors detected: " + errorHandler.getErrorCount());
    // } catch (Exception e) {
    // throw new MotuException(e);
    // // instance document is invalid!
    // }
    // }
    //

    /**
     * Gets the amount data size.
     * 
     * @param listVar the list var
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param serviceName the service name
     * @param productId the product id
     * 
     * @return the amount data size
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public Product getAmountDataSize(String serviceName,
                                     List<String> listVar,
                                     List<String> listTemporalCoverage,
                                     List<String> listLatLonCoverage,
                                     List<String> listDepthCoverage,
                                     String productId) throws MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException,
            MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
            MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException, NetCdfVariableException, MotuNoVarException, NetCdfAttributeException,
            NetCdfVariableNotFoundException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("getAmountDataSize(String, List<String>, List<String>, List<String>, List<String>, String) - entering");
        }

        ServicePersistent servicePersistent = null;
        if (!Organizer.servicesPersistentContainsKey(serviceName)) {
            loadCatalogInfo(serviceName);
        }

        servicePersistent = Organizer.getServicesPersistent(serviceName);

        ProductPersistent productPersistent = servicePersistent.getProductsPersistent(productId);
        if (productPersistent == null) {
            throw new MotuException(String.format("ERROR in getAmountDataSize - product '%s' not found in persistent map", productId));
        }

        String locationData = productPersistent.getUrl();

        Product product = getAmountDataSize(locationData, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getAmountDataSize(String, List<String>, List<String>, List<String>, List<String>, String) - exiting");
        }
        return product;

    }

    /**
     * Gets the amount data size.
     * 
     * @param batchQueue the batch queue
     * @param listVar the list var
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param out the out
     * @param productId the product id
     * @param serviceName the service name
     * 
     * @return the amount data size
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuMarshallException the motu marshall exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public Product getAmountDataSize(String serviceName,
                                     List<String> listVar,
                                     List<String> listTemporalCoverage,
                                     List<String> listLatLonCoverage,
                                     List<String> listDepthCoverage,
                                     String productId,
                                     Writer out,
                                     boolean batchQueue) throws MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException,
            MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
            MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException, NetCdfVariableException, MotuNoVarException, NetCdfAttributeException,
            NetCdfVariableNotFoundException, MotuMarshallException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAmountDataSize(String, List<String>, List<String>, List<String>, List<String>, String, Writer) - entering");
        }

        Product product = null;

        RequestSize requestSize = null;
        try {
            product = getAmountDataSize(serviceName, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage, productId);
            requestSize = initRequestSize(product, batchQueue);
        } catch (MotuException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDateException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDepthException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLatitudeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLongitudeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDateRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuExceedingCapacityException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuNotImplementedException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLatLonRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDepthRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (NetCdfVariableException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuNoVarException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (NetCdfVariableNotFoundException e) {
            marshallRequestSize(e, out);
            throw e;
        }
        marshallRequestSize(requestSize, batchQueue, out);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAmountDataSize(String, List<String>, List<String>, List<String>, List<String>, String, Writer) - exiting");
        }

        return product;

    }

    /**
     * Gets the amount data size.
     * 
     * @param batchQueue the batch queue
     * @param listVar the list var
     * @param locationData the location data
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @param out the out
     * 
     * @return the amount data size
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuMarshallException the motu marshall exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public Product getAmountDataSize(String locationData,
                                     List<String> listVar,
                                     List<String> listTemporalCoverage,
                                     List<String> listLatLonCoverage,
                                     List<String> listDepthCoverage,
                                     Writer out,
                                     boolean batchQueue) throws MotuException, MotuMarshallException, MotuInvalidDateException,
            MotuInvalidDepthException, MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfVariableNotFoundException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAmountDataSize(String, List<String>, List<String>, List<String>, List<String>, Writer) - entering");
        }

        // CSON: StrictDuplicateCode.

        Product product = null;

        RequestSize requestSize = null;
        try {
            product = getAmountDataSize(locationData, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage);
            requestSize = initRequestSize(product, batchQueue);
        } catch (MotuException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDateException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDepthException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLatitudeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLongitudeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDateRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuExceedingCapacityException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuNotImplementedException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidLatLonRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuInvalidDepthRangeException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (NetCdfVariableException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (MotuNoVarException e) {
            marshallRequestSize(e, out);
            throw e;
        } catch (NetCdfVariableNotFoundException e) {
            marshallRequestSize(e, out);
            throw e;
        }
        marshallRequestSize(requestSize, batchQueue, out);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getAmountDataSize(String, List<String>, List<String>, List<String>, List<String>, Writer) - exiting");
        }
        return product;

    }

    /**
     * Gets the available services (AVISO, Mercator, ....) in a specified format.
     * 
     * @param format output format (HTML, XML, Ascii).
     * @param out writer in which services will be list.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public void getAvailableServices(Writer out, Organizer.Format format) throws MotuException, MotuNotImplementedException {
        switch (format) {

        case HTML:
            getAvailableServicesHTML(out);
            break;

        case XML:
        case ASCII:
            throw new MotuNotImplementedException(String.format("getAvailableServices - Format %s not implemented", format.toString())); // break;

        default:
            throw new MotuException("getAvailableServices - Unknown Format");
            // break;
        }
    }

    /**
     * Gets the catalog's informations of a service (AVISO, Mercator, ....).
     * 
     * @param format output format (HTML, XML, Ascii).
     * @param serviceName involved service.
     * @param out writer in which catalog's information will be list.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public void getCatalogInformation(String serviceName, Writer out, Organizer.Format format) throws MotuException, MotuNotImplementedException,
            NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCatalogInformation() - entering");
        }

        setCurrentService(serviceName);

        switch (format) {

        case HTML:
            getCatalogInformationHTML(out);
            break;

        case XML:
        case ASCII:
            throw new MotuNotImplementedException(String.format("getCatalogInformation - Format %s not implemented", format.toString()));
            // break;

        default:
            throw new MotuException("getCatalogInformation - Unknown Format");
            // break;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getCatalogInformation() - exiting");
        }
    }

    /**
     * Gets the current language.
     * 
     * @return the currentLanguage.
     */
    public Language getCurrentLanguage() {
        return this.currentLanguage;
    }

    /**
     * Gets the current product.
     * 
     * @return the current product
     */
    public Product getCurrentProduct() {
        if (getCurrentService() == null) {
            return null;
        }
        return getCurrentService().getCurrentProduct();
    }

    /**
     * Gets the current service.
     * 
     * @return the currentService
     */
    public ServiceData getCurrentService() {
        return this.currentService;
    }

    /**
     * Getter of the property <tt>defaultServiceName</tt>.
     * 
     * @return Returns the defaultServiceName.
     * 
     * @uml.property name="defaultServiceName"
     */
    public String getDefaultServiceName() {
        return this.defaultServiceName;
    }

    /**
     * Get a list of the other services belonging to the same group of a service.
     * 
     * @param theService service from which to get the other service of the same group
     * 
     * @return a list of ServiceData with the same group (except 'theService').
     */
    public List<ServiceData> getOtherGroupServices(ServiceData theService) {
        List<ServiceData> list = new ArrayList<ServiceData>();
        for (ServiceData service : servicesValues()) {
            if (service == theService) {
                continue;
            }
            if (service.getGroup().equalsIgnoreCase(theService.getGroup())) {
                list.add(service);
            }
        }
        return list;
    }

    /**
     * Gets product's download informations related to a service (AVISO, Mercator, ....).
     * 
     * @param format output format (HTML, XML, Ascii).
     * @param serviceName involved service.
     * @param productId id of the product on which to get informations.
     * @param out writer in which product's information will be list.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public void getProductDownloadInfo(String serviceName, String productId, Writer out, Organizer.Format format) throws MotuException,
            MotuNotImplementedException, NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductDownloadInfo() - entering");
        }

        loadCatalogInfo(serviceName);

        getProductDownloadInfo(productId, out, format);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductDownloadInfo() - exiting");
        }
    }

    /**
     * Gets product's download informations related to the current service.
     * 
     * @param format output format (HTML, XML, Ascii).
     * @param productId id of the product on which to get informations.
     * @param out writer in which product's information will be list.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public void getProductDownloadInfo(String productId, Writer out, Organizer.Format format) throws MotuException, MotuNotImplementedException,
            NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductDownloadInfo() - entering");
        }

        if (this.currentService == null) {
            throw new MotuException("Service has not bee set (current service is null)");
        }
        switch (format) {

        case HTML:
            getProductDownloadInfoHTML(productId, out);
            break;

        case XML:
        case ASCII:
            throw new MotuNotImplementedException(String.format("getProductDownloadInfo - Format %s not implemented", format.toString()));
            // break;

        default:
            throw new MotuException("getProductInformation - Unknown Format");
            // break;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductDownloadInfo() - exiting");
        }
    }

    /**
     * Gets product's informations related to a service (AVISO, Mercator, ....).
     * 
     * @param locationData url of the product to load metadata
     * 
     * @return product instance with loaded metadata
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public Product getProductInformation(String locationData) throws MotuException, MotuNotImplementedException, NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - entering");
        }

        if (this.currentService == null) {
            // Create a virtual service with default option
            createVirtualService();
        }

        Product product = currentService.getProductInformationFromLocation(locationData);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - exiting");
        }
        return product;
    }

    /**
     * Gets product's informations related to a service (AVISO, Mercator, ....).
     * 
     * @param serviceName involved service.
     * @param productId id of the product on which to get informations.
     * 
     * @return product instance with loaded metadata
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public Product getProductInformation(String serviceName, String productId) throws MotuException, MotuNotImplementedException,
            NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - entering");
        }

        Product product = getProductInformation(productId, null, null);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - exiting");
        }
        return product;
    }

    /**
     * Gets product's informations related to a service (AVISO, Mercator, ....).
     * 
     * @param format output format (HTML, XML, Ascii).
     * @param serviceName involved service.
     * @param productId id of the product on which to get informations.
     * @param out writer in which product's information will be list.
     * 
     * @return product instance with loaded metadata
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public Product getProductInformation(String serviceName, String productId, Writer out, Organizer.Format format) throws MotuException,
            MotuNotImplementedException, NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - entering");
        }

        loadCatalogInfo(serviceName);

        Product product = getProductInformation(productId, out, format);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - exiting");
        }
        return product;
    }

    /**
     * Gets product's informations (metadata) related to the current service.
     * 
     * @param format output format (HTML, XML, Ascii).
     * @param productId id of the product on which to get informations.
     * @param out writer in which product's information will be list.
     * 
     * @return product instance with loaded metadata
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public Product getProductInformation(String productId, Writer out, Organizer.Format format) throws MotuException, MotuNotImplementedException,
            NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - entering");
        }

        if (this.currentService == null) {
            throw new MotuException("Service has not bee set (current service is null)");
        }

        Product product = currentService.getProductInformation(productId);

        if (format == null || out == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getProductInformation() - exiting");
            }
            return product;
        }

        switch (format) {

        case HTML:
            // Normalement à supprimer car c'est fait dans currentService.getProductInformation(productId)
            // currentService.getProductInformation(product);
            currentService.writeProductInformationHTML(product, out);
            break;

        case XML:
        case ASCII:
            throw new MotuNotImplementedException(String.format("getProductInformation - Format %s not implemented", format.toString()));
            // break;

        default:
            throw new MotuException("getProductInformation - Unknown Format");
            // break;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - exiting");
        }
        return product;
    }

    /**
     * Get a list of the other services belonging to the same group of a service.
     * 
     * @param theService service from which to get the other service of the same group
     * 
     * @return a list of ServiceData with the same group (included 'theService').
     */
    public List<ServiceData> getSameGroupServices(ServiceData theService) {
        List<ServiceData> list = new ArrayList<ServiceData>();
        for (ServiceData service : servicesValues()) {
            if (service.getGroup().equalsIgnoreCase(theService.getGroup())) {
                list.add(service);
            }
        }
        return list;
    }

    /**
     * Getter of the property <tt>services</tt>.
     * 
     * @return Returns the servicesMap.
     * 
     * @uml.property name="services"
     */
    public Map<String, ServiceData> getServices() {
        return this.servicesMap;
    }

    /**
     * Returns the value to which this map maps the specified key.
     * 
     * @param key key whose associated value is to be returned.
     * 
     * @return the value to which this map maps the specified key, or <tt>null</tt> if the map contains no
     *         mapping for this key.
     * 
     * @see java.util.Map#get(Object)
     * @uml.property name="services"
     */
    public ServiceData getServices(String key) {
        return this.servicesMap.get(key.trim());
    }

    /**
     * Gets the time coverage.
     * 
     * @param params the params
     * 
     * @throws MotuException the motu exception
     * @throws MotuInconsistencyException the motu inconsistency exception
     * @throws MotuMarshallException the motu marshall exception
     */
    public void getTimeCoverage(ExtractionParameters params) throws MotuException, MotuInconsistencyException, MotuMarshallException {

        params.verifyParameters();

        if (!Organizer.isNullOrEmpty(params.getLocationData())) {
            getTimeCoverage(params.getLocationData(), params.getOut());
        } else if (!Organizer.isNullOrEmpty(params.getServiceName()) && !Organizer.isNullOrEmpty(params.getProductId())) {
            getTimeCoverage(params.getServiceName(), params.getProductId(), params.getOut());
        } else {
            throw new MotuInconsistencyException(String.format("ERROR in getTimeCoverage: inconsistency parameters : %s", params.toString()));
        }

    }

    /**
     * Gets the time coverage.
     * 
     * @param locationData the location data
     * 
     * @return the time coverage
     * 
     * @throws MotuException the motu exception
     */
    public TimeCoverage getTimeCoverage(String locationData) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeCoverage(String) - entering");
        }
        Product product = null;
        try {
            product = getProductInformation(locationData);
        } catch (MotuNotImplementedException e) {
            LOG.error("getTimeCoverage(String)", e);

            throw new MotuException(String.format("ERROR in getTimeCoverage - location data is '%s' ", locationData), e);
        } catch (NetCdfAttributeException e) {
            LOG.error("getTimeCoverage(String)", e);

            // Do Nothing
        }
        if (product == null) {
            throw new MotuException(String.format("Unknown product from location data '%s' (getTimeCoverage)", locationData));
        }

        ProductMetaData productMetaData = product.getProductMetaData();
        if (productMetaData == null) {
            throw new MotuException(String.format("product from location data '%s' has no metadata (getTimeCoverage)", locationData));
        }

        TimeCoverage timeCoverage = initTimeCoverage(productMetaData);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeCoverage(String) - exiting");
        }
        return timeCoverage;
    }

    /**
     * Gets the time coverage.
     * 
     * @param serviceName the service name
     * @param productId the product id
     * 
     * @return the time coverage
     * 
     * @throws MotuException the motu exception
     */
    public TimeCoverage getTimeCoverage(String serviceName, String productId) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeCoverage(String, String) - entering");
        }

        ServicePersistent servicePersistent = null;
        if (!Organizer.servicesPersistentContainsKey(serviceName)) {
            loadCatalogInfo(serviceName);
        }

        servicePersistent = Organizer.getServicesPersistent(serviceName);

        ProductPersistent productPersistent = servicePersistent.getProductsPersistent(productId);
        if (productPersistent == null) {
            throw new MotuException(String.format("ERROR in getTimeCoverage - product '%s' not found in persistent map", productId));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeCoverage(String, String) - exiting");
        }
        return getTimeCoverage(productPersistent.getUrl());

        //        
        //
        // Product product = null;
        // ProductMetaData productMetaData = null;
        // loadCatalogInfo(serviceName);
        //
        // if (currentService == null) {
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("getTimeCoverage(String, String) - exiting");
        // }
        // return null;
        // }
        // product = currentService.getProduct(productId);
        // if (product == null) {
        // throw new MotuException(String.format("Unknown product id '%s' for service name '%s'
        // (getTimeCoverage)", productId, serviceName));
        // }
        // productMetaData = product.getProductMetaData();
        // if (productMetaData == null) {
        // throw new MotuException(String.format("product id '%s' of service name '%s' has no metadata
        // (getTimeCoverage)", productId, serviceName));
        // }
        //
        // TimeCoverage timeCoverage = initTimeCoverage(productMetaData);
        //
        // if (timeCoverage == null) {
        // timeCoverage = getTimeCoverage(product.getLocationData());
        // }
        //
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("getTimeCoverage(String, String) - exiting");
        // }
        // return timeCoverage;
    }

    /**
     * Gets the time coverage.
     * 
     * @param writer the writer
     * @param serviceName the service name
     * @param productId the product id
     * 
     * @throws MotuException the motu exception
     * @throws MotuMarshallException the motu marshall exception
     */
    public void getTimeCoverage(String serviceName, String productId, Writer writer) throws MotuException, MotuMarshallException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeCoverage(String, String, Writer) - entering");
        }

        TimeCoverage timeCoverage = null;
        try {
            timeCoverage = getTimeCoverage(serviceName, productId);
        } catch (MotuException e) {
            marshallTimeCoverage(e, writer);
            throw e;
        }
        marshallTimeCoverage(timeCoverage, writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeCoverage(String, String, Writer) - exiting");
        }
    }

    /**
     * Gets the time coverage.
     * 
     * @param locationData the location data
     * @param writer the writer
     * 
     * @throws MotuException the motu exception
     * @throws MotuMarshallException the motu marshall exception
     */
    public void getTimeCoverage(String locationData, Writer writer) throws MotuException, MotuMarshallException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeCoverage(String, String, Writer) - entering");
        }

        TimeCoverage timeCoverage = null;
        try {
            timeCoverage = getTimeCoverage(locationData);
        } catch (MotuException e) {
            marshallTimeCoverage(e, writer);
            throw e;
        }
        marshallTimeCoverage(timeCoverage, writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeCoverage(String, String, Writer) - exiting");
        }
    }
    
 
    /**
     * Getter of the property <tt>user</tt>.
     * 
     * @return Returns the user.
     * 
     * @uml.property name="user"
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Getter of the property <tt>velocityEngine</tt>.
     * 
     * @return Returns the velocityEngine.
     * 
     * @uml.property name="velocityEngine"
     */
    public VelocityEngine getVelocityEngine() {
        return this.velocityEngine;
    }

    /**
     * Checks if is generic service.
     * 
     * @return true, if is generic service
     */
    public boolean isGenericService() {
        if (getCurrentService() == null) {
            return true;
        }
        return getCurrentService().isGeneric();
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @return <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @see java.util.Map#isEmpty()
     * @uml.property name="services"
     */
    public boolean isServicesEmpty() {
        return this.servicesMap.isEmpty();
    }

    /**
     * Load catalog info.
     * 
     * @param serviceName the service name
     * 
     * @throws MotuException the motu exception
     */
    public void loadCatalogInfo(String serviceName) throws MotuException {

        setCurrentService(serviceName);

        currentService.loadCatalogInfo();

    }

    /**
     * Send an email to the user to tell him where to download result output. file
     */
    public void notifyUser() {

    }

    /**
     * Associates the specified value with the specified key in this map (optional operation).
     * 
     * @param value value to be associated with the specified key.
     * @param key key with which the specified value is to be associated.
     * 
     * @return previous value associated with specified key, or <tt>null</tt>
     * 
     * @see java.util.Map#put(Object,Object)
     * @uml.property name="services"
     */
    public ServiceData putServices(String key, ServiceData value) {
        return this.servicesMap.put(key.trim(), value);
    }

    /**
     * Refresh HTML page. Can be used to rewrite page when language is changed, for instance.
     * 
     * @param out writer in which page will be written.
     * 
     * @throws MotuException the motu exception
     */
    public void refreshHTML(Writer out) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("refreshHTML() - entering");
        }

        if (currentService == null) {
            LOG.info("currentService is null");

            if (LOG.isDebugEnabled()) {
                LOG.debug("refreshHTML() - exiting");
            }
            return;
        }

        currentService.setLanguage(currentLanguage);
        currentService.refreshHTML(out);

        if (LOG.isDebugEnabled()) {
            LOG.debug("refreshHTML() - exiting");
        }
    }

    /**
     * Removes the mapping for this key from this map if it is present (optional operation).
     * 
     * @param key key whose mapping is to be removed from the map.
     * 
     * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.
     * 
     * @see java.util.Map#remove(Object)
     * @uml.property name="services"
     */
    public ServiceData removeServices(String key) {
        return this.servicesMap.remove(key);
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     * 
     * @param key key whose presence in this map is to be tested.
     * 
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     * 
     * @see java.util.Map#containsKey(Object)
     * @uml.property name="services"
     */
    public boolean servicesContainsKey(String key) {
        return this.servicesMap.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
     * 
     * @param key value whose presence in this map is to be tested.
     * 
     * @return <tt>true</tt> if this map maps one or more keys to the specified value.
     * 
     * @see java.util.Map#containsValue(Object)
     * @uml.property name="services"
     */
    public boolean servicesContainsValue(ServiceData key) {
        return this.servicesMap.containsValue(key);
    }

    /**
     * Returns a set view of the keys contained in this map.
     * 
     * @return a set view of the keys contained in this map.
     * 
     * @see java.util.Map#keySet()
     * @uml.property name="services"
     */
    public Set<String> servicesKeySet() {
        return this.servicesMap.keySet();
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return the number of key-value mappings in this map.
     * 
     * @see java.util.Map#size()
     * @uml.property name="services"
     */
    public int servicesSize() {
        return this.servicesMap.size();
    }

    /**
     * Returns a collection view of the values contained in this map.
     * 
     * @return a collection view of the values contained in this map.
     * 
     * @see java.util.Map#values()
     * @uml.property name="services"
     */
    public Collection<ServiceData> servicesValues() {
        return this.servicesMap.values();
    }

    /**
     * Initializes the current language from the current language of the service.
     */
    public void setCurrentLangageFromService() {
        if (currentService != null) {
            currentLanguage = currentService.getLanguage();
        }
    }

    /**
     * Sets the current language.
     * 
     * @param language language to set
     */
    public void setCurrentLanguage(Language language) {
        currentLanguage = language;
    }

    /**
     * Sets the current language.
     * 
     * @param language language to set
     * 
     * @throws MotuException the motu exception
     */
    public void setCurrentLanguage(String language) throws MotuException {
        currentLanguage = ServiceData.stringToLanguage(language);
    }

    /**
     * Sets the current product last error.
     * 
     * @param error the error
     */
    public void setCurrentProductLastError(String error) {

        Product currentProduct = getCurrentProduct();
        if (currentProduct == null) {
            return;
        }
        currentProduct.setLastError(error);
        currentProduct.clearExtractFilename();

    }

    /**
     * Sets the current service.
     * 
     * @param currentService the currentService to set
     */
    public void setCurrentService(ServiceData currentService) {
        this.currentService = currentService;
        if (this.currentService != null) {
            this.currentService.setLanguage(currentLanguage);
        }

    }

    /**
     * Sets the current service.
     * 
     * @param currentService the currentService to set
     * 
     * @throws MotuException the motu exception
     */
    public void setCurrentService(String currentService) throws MotuException {
        setCurrentService(getServices(currentService.toLowerCase()));
        if (this.currentService == null) {
            throw new MotuException(String.format("Unknown service name %s (setCurrentService)", currentService));
        }

    }

    /**
     * Setter of the property <tt>defaultServiceName</tt>.
     * 
     * @param defaultServiceName The defaultServiceName to set.
     * 
     * @uml.property name="defaultServiceName"
     */
    public void setDefaultServiceName(String defaultServiceName) {
        if (defaultServiceName != null) {
            this.defaultServiceName = defaultServiceName;
        }
    }

    // /**
    // * Gets the motu config schema full path.
    // *
    // * @return the motu config schema full path
    // *
    // * @throws MotuException the motu exception
    // */
    // public static String getMotuConfigSchemaFullPath() {
    //
    // StringBuffer stringBuffer = new StringBuffer();
    // stringBuffer.append(MOTU_XSD_RESOURCEPATH);
    // stringBuffer.append(MOTU_XSD_FILENAME);
    //
    // return stringBuffer.toString();
    // }
    //    

    /**
     * Setter of the property <tt>services</tt>.
     * 
     * @param services the servicesMap to set.
     * 
     * @uml.property name="services"
     */
    public void setServices(Map<String, ServiceData> services) {
        this.servicesMap = services;
    }

    /**
     * Setter of the property <tt>user</tt>.
     * 
     * @param user The user to set.
     * 
     * @uml.property name="user"
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Setter of the property <tt>velocityEngine</tt>.
     * 
     * @param velocityEngine The velocityEngine to set.
     * 
     * @uml.property name="velocityEngine"
     */
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    /**
     * Performs formatting data and write data into a file, for the variable stored in the product's dataset.
     * 
     * @param product product on which to perform formatting
     * @param outputFile URL of the output file.
     * @param format output format (NetCDF, HDF5, Ascii).
     */
    public void write(String format, String outputFile, Product product) {

    }

    /**
     * fill in the services' list.
     * 
     * @throws MotuException the motu exception
     */
    private void fillServices() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fillServices() - entering");
        }

        setDefaultServiceName(getMotuConfigInstance().getDefaultService());
        int countServ = getMotuConfigInstance().getConfigService().size();
        for (int i = 0; i < countServ; i++) {
            ConfigService confServ = getMotuConfigInstance().getConfigService().get(i);

            ServiceData service = new ServiceData();
            service.setVelocityEngine(this.velocityEngine);
            service.setName(confServ.getName());
            service.setDescription(confServ.getDescription());
            service.setGroup(confServ.getGroup());
            service.setLanguage(confServ.getDefaultLanguage());
            service.setHttpBaseRef(confServ.getHttpBaseRef());
            service.setUrlSite(confServ.getCatalog().getUrlSite());
            service.setCatalogFileName(confServ.getCatalog().getName());
            service.setCatalogType(confServ.getCatalog().getType());
            service.setVeloTemplatePrefix(confServ.getVeloTemplatePrefix());
            putServices(service.getName().toLowerCase(), service);
        }

        // set list of service of the same group
        for (ServiceData s : servicesValues()) {
            s.setSameGroupServices(getSameGroupServices(s));

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("fillServices() - exiting");
        }
    }

    /**
     * Gets the available services (AVISO, Mercator, ....) in HTML format.
     * 
     * @param out writer in which services will be list.
     * 
     * @throws MotuException the motu exception
     */
    private void getAvailableServicesHTML(Writer out) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAvailableServicesHTML() - entering");
        }

        // resets the current page of the current service
        if (currentService != null) {
            currentService.resetHtmlCurrentPage();
        }

        // adds that list of services to a VelocityContext
        VelocityContext context = new VelocityContext();
        try {
            context.put("serviceList", servicesMap);
            Template t = velocityEngine.getTemplate(ServiceData.getAvailableServicesTemplateName(currentLanguage));
            t.merge(context, out);
        } catch (Exception e) {
            LOG.error("getAvailableServicesHTML()", e);

            throw new MotuException("Error in getAvailableServicesHTML", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getAvailableServicesHTML() - exiting");
        }
    }

    /**
     * Gets the catalog's informations of the current service in HTML format.
     * 
     * @param out writer in which catalog's information will be list.
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    private void getCatalogInformationHTML(Writer out) throws MotuException, NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCatalogInformationHTML() - entering");
        }

        if (this.currentService == null) {
            throw new MotuException("Error in getCatalogInformationHTML - No service has been initialized - currentService is null");
        }

        currentService.getCatalogInformationHTML(out);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getCatalogInformationHTML() - exiting");
        }
    }

    /**
     * Gets the product's download informations of the current product in HTML format.
     * 
     * @param productId id of the product on which to get informations.
     * @param out writer in which catalog's information will be list.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    private void getProductDownloadInfoHTML(String productId, Writer out) throws MotuException, MotuNotImplementedException, NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductDownloadInfoHTML() - entering");
        }

        if (this.currentService == null) {
            throw new MotuException("Error in getProductDownloadInfoHTML - No service has been initialized - currentService is null");
        }

        currentService.getProductDownloadInfoHTML(productId, out);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductDownloadInfoHTML() - exiting");
        }
    }

    /**
     * Initializes the Organizer service.
     * 
     * @throws MotuException the motu exception
     */
    private void init() throws MotuException {

        servicesMap = new HashMap<String, ServiceData>();

        Organizer.initJAXB();

        initVelocityEngine();

        fillServices();

    }

    /**
     * initializes the Velocity runtime engine, using default properties plus the properties in the Motu
     * velocity properties file.
     * 
     * @throws MotuException the motu exception
     */
    private void initVelocityEngine() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initVelocityEngine() - entering");
        }

        try {
            velocityEngine = new VelocityEngine();

            Properties conf = new Properties();

            conf.put(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            conf.put("runtime.log.logsystem.log4j.category", LOG.getName());

            conf.put("resource.loader", "class");
            conf.put("class.resource.loader.description", "Velocity Classpath Resource Loader");
            conf.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

            velocityEngine.init(conf);

        } catch (Exception e) {
            LOG.error("initVelocityEngine()", e);

            throw new MotuException("Error in initVelocityEngine - Unable to intialize Velocity engine", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("initVelocityEngine() - exiting");
        }
    }

}

// CSON: MultipleStringLiterals
