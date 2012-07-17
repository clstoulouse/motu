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
package fr.cls.atoll.motu.processor.wps.framework;

import fr.cls.atoll.motu.library.cas.UserBase;
import fr.cls.atoll.motu.library.cas.exception.MotuCasBadRequestException;
import fr.cls.atoll.motu.library.cas.util.AuthenticationHolder;
import fr.cls.atoll.motu.library.cas.util.RestUtil;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.processor.opengis.ows110.CodeType;
import fr.cls.atoll.motu.processor.opengis.wps100.DataType;
import fr.cls.atoll.motu.processor.opengis.wps100.InputDescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.OutputDataType;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptions;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptionType.DataInputs;
import fr.cls.atoll.motu.processor.wps.MotuWPSProcess;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.7 $ - $Date: 2009-10-21 10:28:02 $
 */
public class WPSInfo {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(WPSInfo.class);

    public WPSInfo(String url) throws MotuException {
        this.serverUrl = url;
    }
    
    protected UserBase user = null;

    public UserBase getUser() {
		return user;
	}

	public void setUser(UserBase user) {
		this.user = user;
	}

	protected String ticketGantingTicket = null;
	
	public String getTicketGantingTicket() {
		return ticketGantingTicket;
	}

	public void setTicketGantingTicket(String ticketGantingTicket) {
		this.ticketGantingTicket = ticketGantingTicket;
	}


	protected String casRestUrl = null;

	public String getCasRestUrl() {
		return casRestUrl;
	}

	public void setCasRestUrl(String casRestUrl) {
		this.casRestUrl = casRestUrl;
	}
	
	protected String serverUrl = null;
    protected ProcessDescriptions processDescriptions = null;

    /*
     * protected ConcurrentMap<String, String> xmlNamespaceAliases = new ConcurrentHashMap<String, String>();
     * public void clearXmlNamespaceAliases() { xmlNamespaceAliases.clear(); } public String
     * getXmlNamespaceAliases(String key) { return xmlNamespaceAliases.get(key); }
     * 
     * public String putIfAbsentXmlNamespaceAliases(String key, String value) { return
     * xmlNamespaceAliases.putIfAbsent(key, value); } public String putXmlNamespaceAliases(String key, String
     * value) { return xmlNamespaceAliases.put(key, value); }
     * 
     * public String removeXmlNamespaceAliases(String key) { return xmlNamespaceAliases.remove(key); }
     * 
     * public String replaceXmlNamespaceAliases(String key, String value) { return
     * xmlNamespaceAliases.replace(key, value); }
     * 
     * public boolean xmlNamespaceAliasesMapContainsKey(String key) { return
     * xmlNamespaceAliases.containsKey(key); }
     * 
     * public Set<String> motuWPSProcessDataKeySet() { return xmlNamespaceAliases.keySet(); }
     * 
     * public int xmlNamespaceAliasesSize() { return xmlNamespaceAliases.size(); }
     */
    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String url) {
        this.serverUrl = url;
    }

    public ProcessDescriptions getProcessDescriptions() throws MotuException {
        return loadDescribeProcess();
    }

    public synchronized void reloadDescribeProcess() throws MotuException {
        processDescriptions = null;
        loadDescribeProcess();
    }

    public synchronized ProcessDescriptions loadDescribeProcess() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadDescribeProcess() - entering");
        }

        if (processDescriptions != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("loadDescribeProcess() - exiting");
            }
            return processDescriptions;
        }

        if (serverUrl == null) {
            throw new MotuException("WPSInfo - Unable to load WPS Process Descriptions (WPS uri is null)");
        }

        URL urlFile = Organizer.findResource(MotuWPSProcess.WPS_DESCRIBE_ALL_XML);
        if (urlFile == null) {
            throw new MotuException(String.format("WPSInfo - Unable to find file '%s'", MotuWPSProcess.WPS_DESCRIBE_ALL_XML));
        }

        InputStream in = WPSUtils.post(serverUrl, urlFile);
        try {
            JAXBContext jc = JAXBContext.newInstance(MotuWPSProcess.WPS100_SHEMA_PACK_NAME);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            processDescriptions = (ProcessDescriptions) unmarshaller.unmarshal(in);
        } catch (Exception e) {
            LOG.error("loadDescribeProcess()", e);

            throw new MotuException("WPSInfo - Unable to unmarshall WPS Process Descriptions", e);
        }

        if (processDescriptions == null) {
            throw new MotuException("Unable to load WPS Process Descriptions (processDescriptions is null)");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadDescribeProcess() - exiting");
        }
        return processDescriptions;
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

    public ProcessDescriptionType getProcessDescription(String processName) throws MotuException {

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

        ProcessDescriptionType processDescriptionType = getProcessDescription(processName);
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

    public static boolean isComplexData(InputDescriptionType inputDescriptionType) {
        if (inputDescriptionType == null) {
            return false;
        }
        return inputDescriptionType.getComplexData() != null;
    }

    public static boolean isLiteralData(InputDescriptionType inputDescriptionType) {
        if (inputDescriptionType == null) {
            return false;
        }
        return inputDescriptionType.getLiteralData() != null;
    }

    public static boolean isBoundingBoxData(InputDescriptionType inputDescriptionType) {
        if (inputDescriptionType == null) {
            return false;
        }
        return inputDescriptionType.getBoundingBoxData() != null;
    }

    public static boolean isComplexData(OutputDataType outputDataType) {
        if (outputDataType == null) {
            return false;
        }
        return WPSInfo.isComplexData(outputDataType.getData());
    }

    public static boolean isLiteralData(OutputDataType outputDataType) {
        if (outputDataType == null) {
            return false;
        }
        return WPSInfo.isLiteralData(outputDataType.getData());
    }

    public static boolean isBoundingBoxData(OutputDataType outputDataType) {
        if (outputDataType == null) {
            return false;
        }
        return WPSInfo.isBoundingBoxData(outputDataType.getData());
    }

    public static boolean isComplexData(DataType dataType) {
        if (dataType == null) {
            return false;
        }
        return dataType.getComplexData() != null;
    }

    public static boolean isLiteralData(DataType dataType) {
        if (dataType == null) {
            return false;
        }
        return dataType.getLiteralData() != null;
    }

    public static boolean isBoundingBoxData(DataType dataType) {
        if (dataType == null) {
            return false;
        }
        return dataType.getBoundingBoxData() != null;
    }

    public String getService() throws MotuException {
        return getProcessDescriptions().getService();
    }

    public String getVersion() throws MotuException {
        return getProcessDescriptions().getVersion();
    }

    public String getSchemaLocationKey() throws MotuException {

        return String.format("%s%s", getService(), getVersion());
    }

    public String getSchemaLocation() throws MotuException {
        String schemaLocationKey = getSchemaLocationKey();
        return WPSFactory.getSchemaLocations().get(schemaLocationKey);
    }

    public void setCasRestInfo() throws MotuException {
    	if (user == null) {
    		user = AuthenticationHolder.getUser();
    	}
    	if (user == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setCasRestInfo() - exiting - user is null - can't ask TGT");
            }
    		return;
    	}

    	if (serverUrl == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setCasRestInfo() - exiting - server url is null - can't get CAS server url");
            }
    		return;
    	}
    	
	    try {
			casRestUrl = RestUtil.getCasRestletUrl(serverUrl, RestUtil.CAS_REST_URL_SUFFIX);
		} catch (IOException e) {
			throw new MotuException("setCasRestInfo : Enable to get CAS REST url", e);
		} catch (MotuCasBadRequestException e) {
			throw new MotuException("setCasRestInfo : Enable to get CAS REST url", e);
		}
	    try {
			ticketGantingTicket = RestUtil.getTicketGrantingTicket(casRestUrl, user.getLogin(), user.getPwd());
		} catch (IOException e) {
			throw new MotuException("setCasRestInfo : Enable to set TGT", e);
		}
	}

    /*
     * public String getXmlSchemaNamespace(Class<?> clazz) {
     * 
     * String namespace = ReflectionUtils.getXmlSchemaNamespace(clazz);
     * 
     * if (MotuWPSProcess.isNullOrEmpty(namespace)) { return namespace; }
     * 
     * if (!xmlNamespaceAliasesMapContainsKey(namespace)) { putXmlNamespaceAliases(namespace,
     * String.format("ns%d", xmlNamespaceAliasesSize())); } return namespace; }
     */
}
