package fr.cls.atoll.motu.web.usl.wcs.data;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * . <br>
 * <br>
 * Copyright : Copyright (c) 2017 <br>
 * <br>
 * Company : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1456 $ - $Date: 2011-04-08 18:37:34 +0200 $
 */
public class CapabilitiesData {

    private String version;
    private String serviceType;
    private String serviceTypeVersion;
    private String requestURL;
    private List<String> productList;
    private List<QName> subTypeList;
    private List<String> operationList;
    private String title;
    private String abstractId;
    private List<String> profiles;
    private List<String> supportedFormat;

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public List<String> getProductList() {
        return productList;
    }

    public void setProductList(List<String> productList) {
        this.productList = productList;
    }

    public List<QName> getSubTypeList() {
        return subTypeList;
    }

    public void setSubTypeList(List<QName> subTypeList) {
        this.subTypeList = subTypeList;
    }

    public List<String> getOperationList() {
        return operationList;
    }

    public void setOperationList(List<String> operationList) {
        this.operationList = operationList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractId() {
        return abstractId;
    }

    public void setAbstractId(String abstractId) {
        this.abstractId = abstractId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceTypeVersion() {
        return serviceTypeVersion;
    }

    public void setServiceTypeVersion(String serviceTypeVersion) {
        this.serviceTypeVersion = serviceTypeVersion;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    public List<String> getSupportedFormat() {
        return supportedFormat;
    }

    public void setSupportedFormat(List<String> supportedFormat) {
        this.supportedFormat = supportedFormat;
    }
}
