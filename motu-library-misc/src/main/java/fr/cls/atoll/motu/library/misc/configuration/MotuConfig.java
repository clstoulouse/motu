//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.12.05 at 02:32:28 PM CET 
//

package fr.cls.atoll.motu.library.misc.configuration;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.time.Period;

import fr.cls.atoll.motu.library.converter.jaxb.JodaPeriodAdapter;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="configService" type="{}configService" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="queueServerConfig" type="{}queueServerType"/>
 *         &lt;element name="configFileSystem" type="{}configFileSystemType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{}sftpOptions"/>
 *       &lt;attGroup ref="{}ftpOptions"/>
 *       &lt;attGroup ref="{}proxyOptions"/>
 *       &lt;attribute name="defaultService" type="{http://www.w3.org/2001/XMLSchema}string" default="aviso_nrt" />
 *       &lt;attribute name="useAuthentication" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="authFilePath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="dataBlockSize" type="{http://www.w3.org/2001/XMLSchema}integer" default="2048" />
 *       &lt;attribute name="maxSizePerFile" type="{http://www.w3.org/2001/XMLSchema}integer" default="1024" />
 *       &lt;attribute name="extractionPath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="downloadHttpUrl" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="httpDocumentRoot" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="cleanRequestInterval" default="60">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;minInclusive value="1"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="cleanExtractionFileInterval" default="60">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;minInclusive value="1"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="extractionFilePatterns" type="{http://www.w3.org/2001/XMLSchema}string" default=".*\.nc$|.*\.zip$|.*\.tar$|.*\.gz$|wps_output_.*$|wps_response_.*$" />
 *       &lt;attribute name="extractionFileCacheSize" default="0">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;minInclusive value="0"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="runCleanInterval" default="1">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;minInclusive value="1"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="runGCInterval" default="0">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;minInclusive value="0"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="casRestUrlSuffix" type="{http://www.w3.org/2001/XMLSchema}string" default="/v1/tickets" />
 *       &lt;attribute name="commonVeloTemplatePrefix" type="{http://www.w3.org/2001/XMLSchema}string" default="myocean" />
 *       &lt;attribute name="commonDefaultLanguage" type="{http://www.w3.org/2001/XMLSchema}string" default="UK" />
 *       &lt;attribute name="httpBaseRef" type="{http://www.w3.org/2001/XMLSchema}string" default="http://resources.myocean.eu" />
 *       &lt;attribute name="defaultActionIsListServices" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "configService", "queueServerConfig", "configFileSystem" })
@XmlRootElement(name = "motuConfig")
public class MotuConfig {

    protected List<ConfigService> configService;
    @XmlElement(required = true)
    protected QueueServerType queueServerConfig;
    protected List<ConfigFileSystemType> configFileSystem;
    @XmlAttribute(name = "defaultService")
    protected String defaultService;
    @XmlAttribute(name = "logFormat")
    protected String logFormat;
    @XmlAttribute(name = "useAuthentication")
    protected Boolean useAuthentication;
    @XmlAttribute(name = "authFilePath")
    protected String authFilePath;
    @XmlAttribute(name = "dataBlockSize")
    protected BigInteger dataBlockSize;
    @XmlAttribute(name = "maxSizePerFile")
    protected BigInteger maxSizePerFile;
    @XmlAttribute(name = "maxSizePerFileTDS")
    protected BigInteger maxSizePerFileTDS;
    @XmlAttribute(name = "extractionPath", required = true)
    protected String extractionPath;
    @XmlAttribute(name = "downloadHttpUrl", required = true)
    protected String downloadHttpUrl;
    @XmlAttribute(name = "httpDocumentRoot", required = true)
    protected String httpDocumentRoot;
    @XmlAttribute(name = "cleanRequestInterval")
    protected Integer cleanRequestInterval;
    @XmlAttribute(name = "cleanExtractionFileInterval")
    protected Integer cleanExtractionFileInterval;
    @XmlAttribute(name = "extractionFilePatterns")
    protected String extractionFilePatterns;
    @XmlAttribute(name = "extractionFileCacheSize")
    protected Integer extractionFileCacheSize;
    @XmlAttribute(name = "runCleanInterval")
    protected Integer runCleanInterval;
    @XmlAttribute(name = "runGCInterval")
    protected Integer runGCInterval;
    @XmlAttribute(name = "casRestUrlSuffix")
    protected String casRestUrlSuffix;
    @XmlAttribute(name = "commonVeloTemplatePrefix")
    protected String commonVeloTemplatePrefix;
    @XmlAttribute(name = "commonDefaultLanguage")
    protected String commonDefaultLanguage;
    @XmlAttribute(name = "httpBaseRef")
    protected String httpBaseRef;
    @XmlAttribute(name = "defaultActionIsListServices")
    protected Boolean defaultActionIsListServices;
    @XmlAttribute(name = "sftpUserDirIsRoot")
    protected Boolean sftpUserDirIsRoot;
    @XmlAttribute(name = "sftpSessionTimeOut")
    @XmlJavaTypeAdapter(JodaPeriodAdapter.class)
    @XmlSchemaType(name = "duration")
    protected Period sftpSessionTimeOut;
    @XmlAttribute(name = "strictHostKeyChecking")
    protected String strictHostKeyChecking;
    @XmlAttribute(name = "ftpUserDirIsRoot")
    protected Boolean ftpUserDirIsRoot;
    @XmlAttribute(name = "ftpPassiveMode")
    protected Boolean ftpPassiveMode;
    @XmlAttribute(name = "ftpDataTimeOut")
    @XmlJavaTypeAdapter(JodaPeriodAdapter.class)
    @XmlSchemaType(name = "duration")
    protected Period ftpDataTimeOut;
    @XmlAttribute(name = "useProxy")
    protected Boolean useProxy;
    @XmlAttribute(name = "useFtpProxy")
    protected Boolean useFtpProxy;
    @XmlAttribute(name = "useSftpProxy")
    protected Boolean useSftpProxy;
    @XmlAttribute(name = "useSocksProxy")
    protected Boolean useSocksProxy;
    @XmlAttribute(name = "sftpProxyLogin")
    protected String sftpProxyLogin;
    @XmlAttribute(name = "sftpProxyPwd")
    protected String sftpProxyPwd;
    @XmlAttribute(name = "sftpProxyHost")
    protected String sftpProxyHost;
    @XmlAttribute(name = "sftpProxyPort")
    protected String sftpProxyPort;
    @XmlAttribute(name = "socksProxyLogin")
    protected String socksProxyLogin;
    @XmlAttribute(name = "socksProxyPwd")
    protected String socksProxyPwd;
    @XmlAttribute(name = "socksProxyHost")
    protected String socksProxyHost;
    @XmlAttribute(name = "socksProxyPort")
    protected String socksProxyPort;
    @XmlAttribute(name = "proxyLogin")
    protected String proxyLogin;
    @XmlAttribute(name = "proxyPwd")
    protected String proxyPwd;
    @XmlAttribute(name = "proxyHost")
    protected String proxyHost;
    @XmlAttribute(name = "proxyPort")
    protected String proxyPort;
    @XmlAttribute(name = "ftpProxyLogin")
    protected String ftpProxyLogin;
    @XmlAttribute(name = "ftpProxyPwd")
    protected String ftpProxyPwd;
    @XmlAttribute(name = "ftpProxyHost")
    protected String ftpProxyHost;
    @XmlAttribute(name = "ftpProxyPort")
    protected String ftpProxyPort;

    /**
     * Gets the value of the configService property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification
     * you make to the returned list will be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the configService property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getConfigService().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ConfigService }
     * 
     * 
     */
    public List<ConfigService> getConfigService() {
        if (configService == null) {
            configService = new ArrayList<ConfigService>();
        }
        return this.configService;
    }

    /**
     * Gets the value of the queueServerConfig property.
     * 
     * @return possible object is {@link QueueServerType }
     * 
     */
    public QueueServerType getQueueServerConfig() {
        return queueServerConfig;
    }

    /**
     * Sets the value of the queueServerConfig property.
     * 
     * @param value allowed object is {@link QueueServerType }
     * 
     */
    public void setQueueServerConfig(QueueServerType value) {
        this.queueServerConfig = value;
    }

    /**
     * Gets the value of the configFileSystem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification
     * you make to the returned list will be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the configFileSystem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getConfigFileSystem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ConfigFileSystemType }
     * 
     * 
     */
    public List<ConfigFileSystemType> getConfigFileSystem() {
        if (configFileSystem == null) {
            configFileSystem = new ArrayList<ConfigFileSystemType>();
        }
        return this.configFileSystem;
    }

    /**
     * Gets the value of the defaultService property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDefaultService() {
        if (defaultService == null) {
            return "aviso_nrt";
        } else {
            return defaultService;
        }
    }

    /**
     * Gets the value of the logFormat property. (default is csv)
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getLogFormat() {
        if (logFormat == null) {
            return "csv";
        } else {
            return logFormat;
        }
    }

    /**
     * Sets the value of the defaultService property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setDefaultService(String value) {
        this.defaultService = value;
    }

    /**
     * Sets the value of the logFormat property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setLogFormat(String value) {
        this.logFormat = value;
    }

    /**
     * Gets the value of the useAuthentication property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public boolean getUseAuthentication() {
        if (useAuthentication == null) {
            return false;
        } else {
            return useAuthentication;
        }
    }

    /**
     * Sets the value of the useAuthentication property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setUseAuthentication(Boolean value) {
        this.useAuthentication = value;
    }

    /**
     * Gets the value of the authFilePath property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getAuthFilePath() {
        return authFilePath;
    }

    /**
     * Sets the value of the authFilePath property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setAuthFilePath(String value) {
        this.authFilePath = value;
    }

    /**
     * Gets the value of the dataBlockSize property.
     * 
     * @return possible object is {@link BigInteger }
     * 
     */
    public BigInteger getDataBlockSize() {
        if (dataBlockSize == null) {
            return new BigInteger("2048");
        } else {
            return dataBlockSize;
        }
    }

    /**
     * Sets the value of the dataBlockSize property.
     * 
     * @param value allowed object is {@link BigInteger }
     * 
     */
    public void setDataBlockSize(BigInteger value) {
        this.dataBlockSize = value;
    }

    /**
     * Gets the value of the maxSizePerFile property.
     * 
     * @return possible object is {@link BigInteger }
     * 
     */
    public BigInteger getMaxSizePerFile() {
        if (maxSizePerFile == null) {
            return new BigInteger("1024");
        } else {
            return maxSizePerFile;
        }
    }

    /**
     * Gets the value of the maxSizePerFileTDS property.
     * 
     * @return possible object is {@link BigInteger }
     * 
     */
    public BigInteger getMaxSizePerFileTDS() {
        if (maxSizePerFileTDS == null) {
            return new BigInteger("1024");
        } else {
            return maxSizePerFileTDS;
        }
    }

    /**
     * Sets the value of the maxSizePerFile property.
     * 
     * @param value allowed object is {@link BigInteger }
     * 
     */
    public void setMaxSizePerFile(BigInteger value) {
        this.maxSizePerFile = value;
    }

    /**
     * Sets the value of the maxSizePerFileTDS property.
     * 
     * @param value allowed object is {@link BigInteger }
     * 
     */
    public void setMaxSizePerFileTDS(BigInteger value) {
        this.maxSizePerFileTDS = value;
    }

    /**
     * Gets the value of the extractionPath property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getExtractionPath() {
        return extractionPath;
    }

    /**
     * Sets the value of the extractionPath property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setExtractionPath(String value) {
        this.extractionPath = value;
    }

    /**
     * Gets the value of the downloadHttpUrl property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDownloadHttpUrl() {
        return downloadHttpUrl;
    }

    /**
     * Sets the value of the downloadHttpUrl property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setDownloadHttpUrl(String value) {
        this.downloadHttpUrl = value;
    }

    /**
     * Gets the value of the httpDocumentRoot property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getHttpDocumentRoot() {
        return httpDocumentRoot;
    }

    /**
     * Sets the value of the httpDocumentRoot property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setHttpDocumentRoot(String value) {
        this.httpDocumentRoot = value;
    }

    /**
     * Gets the value of the cleanRequestInterval property.
     * 
     * @return possible object is {@link Integer }
     * 
     */
    public int getCleanRequestInterval() {
        if (cleanRequestInterval == null) {
            return 60;
        } else {
            return cleanRequestInterval;
        }
    }

    /**
     * Sets the value of the cleanRequestInterval property.
     * 
     * @param value allowed object is {@link Integer }
     * 
     */
    public void setCleanRequestInterval(Integer value) {
        this.cleanRequestInterval = value;
    }

    /**
     * Gets the value of the cleanExtractionFileInterval property.
     * 
     * @return possible object is {@link Integer }
     * 
     */
    public int getCleanExtractionFileInterval() {
        if (cleanExtractionFileInterval == null) {
            return 60;
        } else {
            return cleanExtractionFileInterval;
        }
    }

    /**
     * Sets the value of the cleanExtractionFileInterval property.
     * 
     * @param value allowed object is {@link Integer }
     * 
     */
    public void setCleanExtractionFileInterval(Integer value) {
        this.cleanExtractionFileInterval = value;
    }

    /**
     * Gets the value of the extractionFilePatterns property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getExtractionFilePatterns() {
        if (extractionFilePatterns == null) {
            return ".*\\.nc$|.*\\.zip$|.*\\.tar$|.*\\.gz$|wps_output_.*$|wps_response_.*$";
        } else {
            return extractionFilePatterns;
        }
    }

    /**
     * Sets the value of the extractionFilePatterns property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setExtractionFilePatterns(String value) {
        this.extractionFilePatterns = value;
    }

    /**
     * Gets the value of the extractionFileCacheSize property.
     * 
     * @return possible object is {@link Integer }
     * 
     */
    public int getExtractionFileCacheSize() {
        if (extractionFileCacheSize == null) {
            return 0;
        } else {
            return extractionFileCacheSize;
        }
    }

    /**
     * Sets the value of the extractionFileCacheSize property.
     * 
     * @param value allowed object is {@link Integer }
     * 
     */
    public void setExtractionFileCacheSize(Integer value) {
        this.extractionFileCacheSize = value;
    }

    /**
     * Gets the value of the runCleanInterval property.
     * 
     * @return possible object is {@link Integer }
     * 
     */
    public int getRunCleanInterval() {
        if (runCleanInterval == null) {
            return 1;
        } else {
            return runCleanInterval;
        }
    }

    /**
     * Sets the value of the runCleanInterval property.
     * 
     * @param value allowed object is {@link Integer }
     * 
     */
    public void setRunCleanInterval(Integer value) {
        this.runCleanInterval = value;
    }

    /**
     * Gets the value of the runGCInterval property.
     * 
     * @return possible object is {@link Integer }
     * 
     */
    public int getRunGCInterval() {
        if (runGCInterval == null) {
            return 0;
        } else {
            return runGCInterval;
        }
    }

    /**
     * Sets the value of the runGCInterval property.
     * 
     * @param value allowed object is {@link Integer }
     * 
     */
    public void setRunGCInterval(Integer value) {
        this.runGCInterval = value;
    }

    /**
     * Gets the value of the casRestUrlSuffix property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCasRestUrlSuffix() {
        if (casRestUrlSuffix == null) {
            return "/v1/tickets";
        } else {
            return casRestUrlSuffix;
        }
    }

    /**
     * Sets the value of the casRestUrlSuffix property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setCasRestUrlSuffix(String value) {
        this.casRestUrlSuffix = value;
    }

    /**
     * Gets the value of the commonVeloTemplatePrefix property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCommonVeloTemplatePrefix() {
        if (commonVeloTemplatePrefix == null) {
            return "myocean";
        } else {
            return commonVeloTemplatePrefix;
        }
    }

    /**
     * Sets the value of the commonVeloTemplatePrefix property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setCommonVeloTemplatePrefix(String value) {
        this.commonVeloTemplatePrefix = value;
    }

    /**
     * Gets the value of the commonDefaultLanguage property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCommonDefaultLanguage() {
        if (commonDefaultLanguage == null) {
            return "UK";
        } else {
            return commonDefaultLanguage;
        }
    }

    /**
     * Sets the value of the commonDefaultLanguage property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setCommonDefaultLanguage(String value) {
        this.commonDefaultLanguage = value;
    }

    /**
     * Gets the value of the httpBaseRef property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getHttpBaseRef() {
        if (httpBaseRef == null) {
            return "http://resources.myocean.eu";
        } else {
            return httpBaseRef;
        }
    }

    /**
     * Sets the value of the httpBaseRef property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setHttpBaseRef(String value) {
        this.httpBaseRef = value;
    }

    /**
     * Gets the value of the defaultActionIsListServices property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public boolean getDefaultActionIsListServices() {
        if (defaultActionIsListServices == null) {
            return false;
        } else {
            return defaultActionIsListServices;
        }
    }

    /**
     * Sets the value of the defaultActionIsListServices property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setDefaultActionIsListServices(Boolean value) {
        this.defaultActionIsListServices = value;
    }

    /**
     * Gets the value of the sftpUserDirIsRoot property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public Boolean getSftpUserDirIsRoot() {
        return sftpUserDirIsRoot;
    }

    /**
     * Sets the value of the sftpUserDirIsRoot property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setSftpUserDirIsRoot(Boolean value) {
        this.sftpUserDirIsRoot = value;
    }

    /**
     * Gets the value of the sftpSessionTimeOut property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public Period getSftpSessionTimeOut() {
        if (sftpSessionTimeOut == null) {
            return new JodaPeriodAdapter().unmarshal("PT0M");
        } else {
            return sftpSessionTimeOut;
        }
    }

    /**
     * Sets the value of the sftpSessionTimeOut property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSftpSessionTimeOut(Period value) {
        this.sftpSessionTimeOut = value;
    }

    /**
     * Gets the value of the strictHostKeyChecking property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getStrictHostKeyChecking() {
        if (strictHostKeyChecking == null) {
            return "no";
        } else {
            return strictHostKeyChecking;
        }
    }

    /**
     * Sets the value of the strictHostKeyChecking property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setStrictHostKeyChecking(String value) {
        this.strictHostKeyChecking = value;
    }

    /**
     * Gets the value of the ftpUserDirIsRoot property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public Boolean getFtpUserDirIsRoot() {
        return ftpUserDirIsRoot;
    }

    /**
     * Sets the value of the ftpUserDirIsRoot property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setFtpUserDirIsRoot(Boolean value) {
        this.ftpUserDirIsRoot = value;
    }

    /**
     * Gets the value of the ftpPassiveMode property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public Boolean getFtpPassiveMode() {
        return ftpPassiveMode;
    }

    /**
     * Sets the value of the ftpPassiveMode property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setFtpPassiveMode(Boolean value) {
        this.ftpPassiveMode = value;
    }

    /**
     * Gets the value of the ftpDataTimeOut property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public Period getFtpDataTimeOut() {
        return ftpDataTimeOut;
    }

    /**
     * Sets the value of the ftpDataTimeOut property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setFtpDataTimeOut(Period value) {
        this.ftpDataTimeOut = value;
    }

    /**
     * Gets the value of the useProxy property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public boolean getUseProxy() {
        if (useProxy == null) {
            return false;
        } else {
            return useProxy;
        }
    }

    /**
     * Sets the value of the useProxy property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setUseProxy(Boolean value) {
        this.useProxy = value;
    }

    /**
     * Gets the value of the useFtpProxy property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public boolean getUseFtpProxy() {
        if (useFtpProxy == null) {
            return false;
        } else {
            return useFtpProxy;
        }
    }

    /**
     * Sets the value of the useFtpProxy property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setUseFtpProxy(Boolean value) {
        this.useFtpProxy = value;
    }

    /**
     * Gets the value of the useSftpProxy property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public boolean getUseSftpProxy() {
        if (useSftpProxy == null) {
            return false;
        } else {
            return useSftpProxy;
        }
    }

    /**
     * Sets the value of the useSftpProxy property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setUseSftpProxy(Boolean value) {
        this.useSftpProxy = value;
    }

    /**
     * Gets the value of the useSocksProxy property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public boolean getUseSocksProxy() {
        if (useSocksProxy == null) {
            return false;
        } else {
            return useSocksProxy;
        }
    }

    /**
     * Sets the value of the useSocksProxy property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setUseSocksProxy(Boolean value) {
        this.useSocksProxy = value;
    }

    /**
     * Gets the value of the sftpProxyLogin property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSftpProxyLogin() {
        return sftpProxyLogin;
    }

    /**
     * Sets the value of the sftpProxyLogin property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSftpProxyLogin(String value) {
        this.sftpProxyLogin = value;
    }

    /**
     * Gets the value of the sftpProxyPwd property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSftpProxyPwd() {
        return sftpProxyPwd;
    }

    /**
     * Sets the value of the sftpProxyPwd property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSftpProxyPwd(String value) {
        this.sftpProxyPwd = value;
    }

    /**
     * Gets the value of the sftpProxyHost property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSftpProxyHost() {
        if (sftpProxyHost == null) {
            return "proxy-prod.cls.fr";
        } else {
            return sftpProxyHost;
        }
    }

    /**
     * Sets the value of the sftpProxyHost property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSftpProxyHost(String value) {
        this.sftpProxyHost = value;
    }

    /**
     * Gets the value of the sftpProxyPort property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSftpProxyPort() {
        if (sftpProxyPort == null) {
            return "8080";
        } else {
            return sftpProxyPort;
        }
    }

    /**
     * Sets the value of the sftpProxyPort property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSftpProxyPort(String value) {
        this.sftpProxyPort = value;
    }

    /**
     * Gets the value of the socksProxyLogin property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSocksProxyLogin() {
        return socksProxyLogin;
    }

    /**
     * Sets the value of the socksProxyLogin property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSocksProxyLogin(String value) {
        this.socksProxyLogin = value;
    }

    /**
     * Gets the value of the socksProxyPwd property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSocksProxyPwd() {
        return socksProxyPwd;
    }

    /**
     * Sets the value of the socksProxyPwd property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSocksProxyPwd(String value) {
        this.socksProxyPwd = value;
    }

    /**
     * Gets the value of the socksProxyHost property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSocksProxyHost() {
        if (socksProxyHost == null) {
            return "proxy-prod.cls.fr";
        } else {
            return socksProxyHost;
        }
    }

    /**
     * Sets the value of the socksProxyHost property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSocksProxyHost(String value) {
        this.socksProxyHost = value;
    }

    /**
     * Gets the value of the socksProxyPort property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSocksProxyPort() {
        if (socksProxyPort == null) {
            return "1080";
        } else {
            return socksProxyPort;
        }
    }

    /**
     * Sets the value of the socksProxyPort property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSocksProxyPort(String value) {
        this.socksProxyPort = value;
    }

    /**
     * Gets the value of the proxyLogin property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getProxyLogin() {
        return proxyLogin;
    }

    /**
     * Sets the value of the proxyLogin property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setProxyLogin(String value) {
        this.proxyLogin = value;
    }

    /**
     * Gets the value of the proxyPwd property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getProxyPwd() {
        return proxyPwd;
    }

    /**
     * Sets the value of the proxyPwd property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setProxyPwd(String value) {
        this.proxyPwd = value;
    }

    /**
     * Gets the value of the proxyHost property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getProxyHost() {
        if (proxyHost == null) {
            return "proxy-prod.cls.fr";
        } else {
            return proxyHost;
        }
    }

    /**
     * Sets the value of the proxyHost property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setProxyHost(String value) {
        this.proxyHost = value;
    }

    /**
     * Gets the value of the proxyPort property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getProxyPort() {
        if (proxyPort == null) {
            return "8080";
        } else {
            return proxyPort;
        }
    }

    /**
     * Sets the value of the proxyPort property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setProxyPort(String value) {
        this.proxyPort = value;
    }

    /**
     * Gets the value of the ftpProxyLogin property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getFtpProxyLogin() {
        return ftpProxyLogin;
    }

    /**
     * Sets the value of the ftpProxyLogin property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setFtpProxyLogin(String value) {
        this.ftpProxyLogin = value;
    }

    /**
     * Gets the value of the ftpProxyPwd property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getFtpProxyPwd() {
        return ftpProxyPwd;
    }

    /**
     * Sets the value of the ftpProxyPwd property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setFtpProxyPwd(String value) {
        this.ftpProxyPwd = value;
    }

    /**
     * Gets the value of the ftpProxyHost property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getFtpProxyHost() {
        if (ftpProxyHost == null) {
            return "proxy-prod.cls.fr";
        } else {
            return ftpProxyHost;
        }
    }

    /**
     * Sets the value of the ftpProxyHost property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setFtpProxyHost(String value) {
        this.ftpProxyHost = value;
    }

    /**
     * Gets the value of the ftpProxyPort property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getFtpProxyPort() {
        if (ftpProxyPort == null) {
            return "8080";
        } else {
            return ftpProxyPort;
        }
    }

    /**
     * Sets the value of the ftpProxyPort property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setFtpProxyPort(String value) {
        this.ftpProxyPort = value;
    }

}
