package fr.cls.atoll.motu.processor.wps.framework;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.utils.ReflectionUtils;
import fr.cls.atoll.motu.processor.opengis.ows110.CodeType;
import fr.cls.atoll.motu.processor.opengis.wps100.InputDescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.ObjectFactory;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptions;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptionType.DataInputs;
import fr.cls.atoll.motu.processor.wps.MotuWPSProcess;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-08-06 14:28:57 $
 */
public class WPSInfo {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(WPSInfo.class);


    public WPSInfo(String url) throws MotuException {
        this.url = url;
        loadDescribeProcess();
    }

    protected String url = null;
    protected ProcessDescriptions processDescriptions = null;

    protected ConcurrentMap<String, String> xmlNamespaceAliases = new ConcurrentHashMap<String, String>();
    public void clearXmlNamespaceAliases() {
        xmlNamespaceAliases.clear();
    }
    public String getXmlNamespaceAliases(String key) {
        return xmlNamespaceAliases.get(key);
    }
    
    public String putIfAbsentXmlNamespaceAliases(String key, String value) {
        return xmlNamespaceAliases.putIfAbsent(key, value);
    }
     public String putXmlNamespaceAliases(String key, String value) {
        return xmlNamespaceAliases.put(key, value);
    }

     public String removeXmlNamespaceAliases(String key) {
        return xmlNamespaceAliases.remove(key);
    }

     public String replaceXmlNamespaceAliases(String key, String value) {
        return xmlNamespaceAliases.replace(key, value);
    }

     public boolean xmlNamespaceAliasesMapContainsKey(String key) {
        return xmlNamespaceAliases.containsKey(key);
    }

     public Set<String> motuWPSProcessDataKeySet() {
        return xmlNamespaceAliases.keySet();
    }

     public int xmlNamespaceAliasesSize() {
        return xmlNamespaceAliases.size();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ProcessDescriptions getProcessDescriptions() {
        return processDescriptions;
    }

    public void loadDescribeProcess() throws MotuException {

        if (url == null) {
            throw new MotuException("WPSInfo - Unable to load WPS Process Descriptions (WPS uri is null)");
        }

        InputStream in = WPSUtils.post(url, MotuWPSProcess.WPS_DESCRIBE_ALL_XML);
        try {
            JAXBContext jc = JAXBContext.newInstance(MotuWPSProcess.WPS100_SHEMA_PACK_NAME);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            processDescriptions = (ProcessDescriptions) unmarshaller.unmarshal(in);
        } catch (Exception e) {
            throw new MotuException("WPSInfo - Unable to unmarshall WPS Process Descriptions", e);
        }

        if (processDescriptions == null) {
            throw new MotuException("Unable to load WPS Process Descriptions (processDescriptions is null)");
        }
    }

    public List<String> getProcessIdentifierValues() throws MotuException {

        List<String> processes = new ArrayList<String>();

        if (processDescriptions == null) {
            loadDescribeProcess();
        }

        List<ProcessDescriptionType> processDescriptionList = processDescriptions.getProcessDescription();

        for (ProcessDescriptionType processDescriptionType : processDescriptionList) {
            processes.add(processDescriptionType.getIdentifier().getValue());

        }

        return processes;

    }

    public List<CodeType> getProcessIdentifiers() throws MotuException {

        List<CodeType> processes = new ArrayList<CodeType>();

        if (processDescriptions == null) {
            loadDescribeProcess();
        }

        List<ProcessDescriptionType> processDescriptionList = processDescriptions.getProcessDescription();

        for (ProcessDescriptionType processDescriptionType : processDescriptionList) {
            processes.add(processDescriptionType.getIdentifier());

        }

        return processes;

    }

    public ProcessDescriptionType getProcessDescrition(String processName) throws MotuException {

        if (processDescriptions == null) {
            loadDescribeProcess();
        }

        List<ProcessDescriptionType> processDescriptionList = processDescriptions.getProcessDescription();

        for (ProcessDescriptionType processDescriptionType : processDescriptionList) {
            if (processName.equalsIgnoreCase(processDescriptionType.getIdentifier().getValue())) {
                return processDescriptionType;
            }

        }
        return null;
    }

    public DataInputs getDataInputs(String processName) throws MotuException {

        ProcessDescriptionType processDescriptionType = getProcessDescrition(processName);
        DataInputs dataInputs = null;

        if (processDescriptionType != null) {
            dataInputs = processDescriptionType.getDataInputs();            
        }
        
        return dataInputs;
    }


    public List<InputDescriptionType> getInputDescriptions(String processName) throws MotuException {

        DataInputs dataInputs = getDataInputs(processName);
        List<InputDescriptionType> inputDescriptionList = null;
        
        if (dataInputs != null) {
            inputDescriptionList = dataInputs.getInput();
        }
        
        return inputDescriptionList;
    }

    public String getXmlSchemaNamespace(Class<?> clazz) {
        
        String namespace = ReflectionUtils.getXmlSchemaNamespace(clazz);
        
        if (MotuWPSProcess.isNullOrEmpty(namespace)) {
            return namespace;
        }
        
        if (!xmlNamespaceAliasesMapContainsKey(namespace)) {
            putXmlNamespaceAliases(namespace, String.format("ns%d", xmlNamespaceAliasesSize()));
        }
        return namespace;        
    }
}
