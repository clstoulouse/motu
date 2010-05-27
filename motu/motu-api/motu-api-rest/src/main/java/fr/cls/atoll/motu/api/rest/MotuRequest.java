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
package fr.cls.atoll.motu.api.rest;

import fr.cls.atoll.motu.api.message.MotuMsgConstant;
import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Helper class that allows to send a Motu request and retrieve the results.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuRequest {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(MotuRequest.class);

    /** The jcontext. */
    private static JAXBContext jaxbContextMotuMsg = null;

    private static Unmarshaller unmarshallerMotuMsg = null;

    private int connectTimeout = 60000;

    // private static Map<String, String> requestExtraInfo = null;

    /**
     * The Constructor.
     */
    public MotuRequest() {
        this(null, null);

    }

    /**
     * Constructeur.
     * 
     * @param servletUrl the servlet url
     */
    public MotuRequest(String servletUrl) {
        this(servletUrl, null);
    }

    /**
     * Constructeur.
     * 
     * @param motuRequestParameters the motu request parameters
     * @param servletUrl the servlet url
     */
    public MotuRequest(String servletUrl, MotuRequestParameters motuRequestParameters) {
        setServletUrl(servletUrl);
        this.motuRequestParameters = motuRequestParameters;
    }

    /**
     * Inits the JAXB.
     * 
     * @throws MotuRequestException the motu request exception
     */
    private static synchronized void initJAXB() throws MotuRequestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXB() - entering");
        }

        if (MotuRequest.jaxbContextMotuMsg != null) {
            return;
        }

        try {
            MotuRequest.jaxbContextMotuMsg = JAXBContext.newInstance(MotuMsgConstant.MOTU_MSG_SCHEMA_PACK_NAME);
            MotuRequest.unmarshallerMotuMsg = MotuRequest.jaxbContextMotuMsg.createUnmarshaller();
        } catch (JAXBException e) {
            LOG.error("initJAXB()", e);
            throw new MotuRequestException("Error in initJAXB ", e);

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXB() - exiting");
        }
    }

    /** The servlet url. */
    private String servletUrl = null;

    /** The motu request parameters. */
    private MotuRequestParameters motuRequestParameters = null;

    /**
     * Gets the request url.
     * 
     * @return the request url
     * 
     * @throws MotuRequestException the motu request exception
     */
    public String getRequestUrl() throws MotuRequestException {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            stringBuffer.append(servletUrl);
            stringBuffer.append("?");
            stringBuffer.append(getRequestParams());
        } catch (UnsupportedEncodingException ex) {
            LOG.error("execute()", ex);

            throw new MotuRequestException("Request parameters encoding error", ex);
        }

        return stringBuffer.toString();
    }

    /**
     * Exécute de la requête et retourne du résultat dans un flux. Le flux contient le fichier netcdf en
     * mode console, l'url du fichier extrait en mode url ou l'url du fichier de status en mode status (ce
     * fichier contiendra l'état de la requête en cours : INPRGRESS ou ERROR msg_erreur ou DONE.
     * 
     * @return le flux résultat de la requête
     * 
     * @throws MotuRequestException the motu request exception
     */
    public InputStream execute() throws MotuRequestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("execute() - entering");
        }

        String requestParams = null;

        try {
            requestParams = getRequestParams();
        } catch (UnsupportedEncodingException ex) {
            LOG.error("execute()", ex);

            throw new MotuRequestException("Request parameters encoding error", ex);
        }

        URL url = null;

        Map<String, String> requestExtraInfo = MotuRequest.searchUrlUserPwd(servletUrl);
        String targetUrl = servletUrl;

        if (requestExtraInfo != null) {
            targetUrl = requestExtraInfo.get(MotuRequestParametersConstant.PARAM_MODE_URL);
        }

        try {
            url = new URL(targetUrl);
        } catch (MalformedURLException ex) {
            LOG.error("execute()", ex);

            throw new MotuRequestException("Invalid url", ex);
        }

        LOG.info("URL=" + getRequestUrl());

        HttpURLConnection urlConnection;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(connectTimeout);
        } catch (IOException ex) {
            LOG.error("execute()", ex);

            throw new MotuRequestException("Request connection failed", ex);
        }
        try {

            // Effectue un POST Http plutôt qu'un GET car le nombre de
            // paramètres peu être important et on ne veut pas voir passer
            // le login et password dans l'url

            if (requestExtraInfo != null) {
                String user = requestExtraInfo.get(MotuRequestParametersConstant.PARAM_LOGIN);
                String pwd = requestExtraInfo.get(MotuRequestParametersConstant.PARAM_PWD);
                if ((user != null) && (pwd != null)) {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(user);
                    stringBuffer.append(":");
                    stringBuffer.append(pwd);
                    byte[] encoding = new org.apache.commons.codec.binary.Base64().encode(stringBuffer.toString().getBytes());
                    urlConnection.setRequestProperty("Authorization", "Basic " + new String(encoding));
                }

            }

            urlConnection.setDoOutput(true);
            Writer writer = new OutputStreamWriter(urlConnection.getOutputStream());
            try {
                writer.write(requestParams);
                writer.flush();
            } finally {
                IOUtils.closeQuietly(writer);
            }

            InputStream returnInputStream = urlConnection.getInputStream();
            if (LOG.isDebugEnabled()) {
                LOG.debug("execute() - exiting");
            }
            return returnInputStream;

        } catch (IOException ex) {
            LOG.error("execute()", ex);

            MotuRequestException motuRequestException;
            try {
                motuRequestException = new MotuRequestException("Request failed - errorCode: " + urlConnection.getResponseCode() + ", errorMsg: "
                        + urlConnection.getResponseMessage(), ex);
            } catch (IOException e) {
                LOG.error("execute()", e);

                motuRequestException = new MotuRequestException("Request connection failed", ex);
            }
            throw motuRequestException;
        }

    }

    /**
     * Search url user pwd.
     * 
     * @param url the url
     * 
     * @return the map< string, string>
     */
    public static Map<String, String> searchUrlUserPwd(String url) {
        if (url == null) {
            return null;
        }

        String patternExpression = "(http://)(.*)\\:(.*)\\@(.*)";

        Pattern pattern = Pattern.compile(patternExpression);
        Matcher matcher = pattern.matcher(url);
        // System.out.println(matcher.groupCount());
        if (matcher.groupCount() != 4) {
            return null;
        }
        Map<String, String> map = new HashMap<String, String>();
        if (!(matcher.find())) {
            return null;
        }
        map.put(MotuRequestParametersConstant.PARAM_LOGIN, matcher.group(2));
        map.put(MotuRequestParametersConstant.PARAM_PWD, matcher.group(3));

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(matcher.group(1));
        stringBuffer.append(matcher.group(4));

        map.put(MotuRequestParametersConstant.PARAM_MODE_URL, stringBuffer.toString());

        return map;
        // while (matcher.find()) {
        // for (int i = 1; i <= matcher.groupCount(); i++) {
        // CharSequence line = matcher.group(i);
        // // System.out.println(line);
        // }
        // }
    }

    /**
     * Méthode utilitaire qui fait une requête via {@code execute()} et retourne ne résultat sous forme
     * d'une string. Trés utilisé en mode url et mode status.
     * 
     * @return La chaine qui contient le résultat de la requête: en mode url, l'url du fichier extrait, en
     *         mode status, l'url du fichier de status.
     * 
     * @throws MotuRequestException si une exception motu se produit
     */
    public String executeAsString() throws MotuRequestException {
        InputStream in = execute();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int c;
        try {
            while ((c = in.read()) >= 0) {
                out.write(c);
            }
        } catch (IOException ex) {
            throw new MotuRequestException("Request reading error", ex);
        }

        return out.toString();
    }

    /**
     * Méthode utilitaire qui fait une requête via {@code execute()} et retourne le resultat sous forme d'un
     * objet (xml jaxb) Utilisé en mode status.
     * 
     * @return un object xml jaxb du package {@value MotuMsgConstant.MOTU_MSG_SCHEMA_PACK_NAME}(diffère selon
     *         le type de requête demandée)
     * 
     * @throws MotuRequestException si une exception motu se produit
     */
    public Object executeAsXML() throws MotuRequestException {
        // Calendar cal = Calendar.getInstance();
        // long start1 = cal.getTimeInMillis();

        InputStream in = execute();

        // cal = Calendar.getInstance();
        // long stop1 = cal.getTimeInMillis();
        // System.out.print("execute executed in : ");
        // System.out.print((stop1 - start1));
        // System.out.println(" milliseconds : ");

        return unmarshallMsg(in);

    }

    /**
     * Unmarshall msg.
     * 
     * @param in the input stream to unmarshall.
     * 
     * @return the object
     * 
     * @throws MotuRequestException the motu request exception
     */
    public Object unmarshallMsg(InputStream in) throws MotuRequestException {

        Object object = null;
        try {
            MotuRequest.initJAXB();
            synchronized (MotuRequest.unmarshallerMotuMsg) {
                object = MotuRequest.unmarshallerMotuMsg.unmarshal(in);
            }
        } catch (JAXBException e) {
            throw new MotuRequestException("request reading error in executeAsXML", e);
        }

        if (object == null) {
            throw new MotuRequestException("Unable to load XML in executeAsXML (returned object is null)");
        }
        try {
            in.close();
        } catch (IOException io) {
            // Do nothing
        }

        return object;
    }

    /**
     * Checks if is status done.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status done
     */
    public static boolean isStatusDone(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return (statusModeResponse.getStatus().compareTo(StatusModeType.DONE) == 0);
    }

    /**
     * Checks if is status in progress.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status in progress
     */
    public static boolean isStatusInProgress(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return (statusModeResponse.getStatus().compareTo(StatusModeType.INPROGRESS) == 0);
    }

    /**
     * Checks if is status in pending.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status in pending
     */
    public static boolean isStatusPending(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return (statusModeResponse.getStatus().compareTo(StatusModeType.PENDING) == 0);
    }

    /**
     * Checks if is status error.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status error
     */
    public static boolean isStatusError(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return (statusModeResponse.getStatus().compareTo(StatusModeType.ERROR) == 0);
    }

    /**
     * Checks if is status done or error.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status done or error
     */
    public static boolean isStatusDoneOrError(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return ((statusModeResponse.getStatus().compareTo(StatusModeType.DONE) == 0) || (statusModeResponse.getStatus()
                .compareTo(StatusModeType.ERROR) == 0));
    }

    /**
     * Checks if is status pending or in progress.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status pending or in progress
     */
    public static boolean isStatusPendingOrInProgress(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return ((statusModeResponse.getStatus().compareTo(StatusModeType.PENDING) == 0) || (statusModeResponse.getStatus()
                .compareTo(StatusModeType.INPROGRESS) == 0));
    }

    /**
     * Gets the status done.
     * 
     * @param fullFilePath the full file path
     * 
     * @return the status mode response if done, otherwise null
     * 
     * @throws MotuRequestException the motu request exception
     */
    public static StatusModeResponse getStatusDone(String fullFilePath) throws MotuRequestException {
        StatusModeResponse statusModeResponse = (StatusModeResponse) getMessageAsXML(fullFilePath);
        if (statusModeResponse.getStatus().compareTo(StatusModeType.DONE) != 0) {
            statusModeResponse = null;
        }
        return statusModeResponse;
    }

    /**
     * Gets the status in progress.
     * 
     * @param fullFilePath the full file path
     * 
     * @return the status mode response if in progress, otherwise null
     * 
     * @throws MotuRequestException the motu request exception
     */
    public static StatusModeResponse getStatusInProgress(String fullFilePath) throws MotuRequestException {
        StatusModeResponse statusModeResponse = (StatusModeResponse) getMessageAsXML(fullFilePath);
        if (statusModeResponse.getStatus().compareTo(StatusModeType.INPROGRESS) != 0) {
            statusModeResponse = null;
        }
        return statusModeResponse;
    }

    /**
     * Gets the status pending.
     * 
     * @param fullFilePath the full file path
     * 
     * @return the status pending
     * 
     * @throws MotuRequestException the motu request exception
     */
    public static StatusModeResponse getStatusPending(String fullFilePath) throws MotuRequestException {
        StatusModeResponse statusModeResponse = (StatusModeResponse) getMessageAsXML(fullFilePath);
        if (statusModeResponse.getStatus().compareTo(StatusModeType.PENDING) != 0) {
            statusModeResponse = null;
        }
        return statusModeResponse;
    }

    /**
     * Gets the status error.
     * 
     * @param fullFilePath the full file path
     * 
     * @return the status mode response if error, otherwise null
     * 
     * @throws MotuRequestException the motu request exception
     */
    public static StatusModeResponse getStatusError(String fullFilePath) throws MotuRequestException {
        StatusModeResponse statusModeResponse = (StatusModeResponse) getMessageAsXML(fullFilePath);
        if (statusModeResponse.getStatus().compareTo(StatusModeType.ERROR) != 0) {
            statusModeResponse = null;
        }
        return statusModeResponse;
    }

    // /**
    // * Gets the status done or error.
    // *
    // * @param requestId the request id
    // *
    // * @return the status done or error
    // *
    // * @throws MotuRequestException the motu request exception
    // */
    // public StatusModeResponse getStatusDoneOrError(long requestId) throws MotuRequestException {
    // return getStatusDoneOrError(motuRequestParameters, requestId);
    // }
    //
    // /**
    // * Gets the status done or error.
    // *
    // * @param requestId the request id
    // * @param params the motu request parameters
    // *
    // * @return the status done or error
    // *
    // * @throws MotuRequestException the motu request exception
    // */
    // public StatusModeResponse getStatusDoneOrError(MotuRequestParameters params, long requestId) throws
    // MotuRequestException {
    //        
    // StatusModeResponse statusModeResponse = executeActionGetStatusParams(params, requestId);
    // if (!(MotuRequest.isStatusDoneOrError(statusModeResponse))) {
    // statusModeResponse = null;
    // }
    // return statusModeResponse;
    // }
    //    
    // /**
    // * Gets the status pending or in progress.
    // *
    // * @param requestId the request id
    // *
    // * @return the status pending or in progress
    // *
    // * @throws MotuRequestException the motu request exception
    // */
    // public StatusModeResponse getStatusPendingOrInProgress(long requestId) throws MotuRequestException {
    // return getStatusPendingOrInProgress(motuRequestParameters, requestId);
    // }
    //
    // /**
    // * Gets the status pending or in progress.
    // *
    // * @param requestId the request id
    // * @param params the params
    // *
    // * @return the status pending or in progress
    // *
    // * @throws MotuRequestException the motu request exception
    // */
    // public StatusModeResponse getStatusPendingOrInProgress(MotuRequestParameters params, long requestId)
    // throws MotuRequestException {
    //
    // StatusModeResponse statusModeResponse = executeActionGetStatusParams(params, requestId);
    // if (!(MotuRequest.isStatusPendingOrInProgress(statusModeResponse))) {
    // statusModeResponse = null;
    // }
    // return statusModeResponse;
    // }
    //    
    // /**
    // * Gets the status pending.
    // *
    // * @param requestId the request id
    // * @param params the params
    // *
    // * @return the status pending
    // *
    // * @throws MotuRequestException the motu request exception
    // */
    // public StatusModeResponse getStatusPending(MotuRequestParameters params, long requestId) throws
    // MotuRequestException {
    //
    // StatusModeResponse statusModeResponse = executeActionGetStatusParams(params, requestId);
    // if (!(MotuRequest.isStatusPending(statusModeResponse))) {
    // statusModeResponse = null;
    // }
    // return statusModeResponse;
    // }
    //    
    // /**
    // * Gets the status in progess.
    // *
    // * @param requestId the request id
    // * @param params the params
    // *
    // * @return the status in progess
    // *
    // * @throws MotuRequestException the motu request exception
    // */
    // public StatusModeResponse getStatusInProgess(MotuRequestParameters params, long requestId) throws
    // MotuRequestException {
    //
    // StatusModeResponse statusModeResponse = executeActionGetStatusParams(params, requestId);
    // if (!(MotuRequest.isStatusInProgress(statusModeResponse))) {
    // statusModeResponse = null;
    // }
    // return statusModeResponse;
    // }
    //    
    // /**
    // * Gets the status done.
    // *
    // * @param requestId the request id
    // * @param params the params
    // *
    // * @return the status done
    // *
    // * @throws MotuRequestException the motu request exception
    // */
    // public StatusModeResponse getStatusDone(MotuRequestParameters params, long requestId) throws
    // MotuRequestException {
    //
    // StatusModeResponse statusModeResponse = executeActionGetStatusParams(params, requestId);
    // if (!(MotuRequest.isStatusDone(statusModeResponse))) {
    // statusModeResponse = null;
    // }
    // return statusModeResponse;
    // }
    //    
    // /**
    // * Gets the status error.
    // *
    // * @param requestId the request id
    // * @param params the params
    // *
    // * @return the status error
    // *
    // * @throws MotuRequestException the motu request exception
    // */
    // public StatusModeResponse getStatusError(MotuRequestParameters params, long requestId) throws
    // MotuRequestException {
    //
    // StatusModeResponse statusModeResponse = executeActionGetStatusParams(params, requestId);
    // if (!(MotuRequest.isStatusError(statusModeResponse))) {
    // statusModeResponse = null;
    // }
    // return statusModeResponse;
    // }

    /**
     * Sets the action get status params.
     * 
     * @param requestId the request id
     */
    public void setActionGetStatusParams(long requestId) {
        if (motuRequestParameters != null) {
            motuRequestParameters.clearParameters();
        } else {
            motuRequestParameters = new MotuRequestParameters();
        }

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_GET_REQUEST_STATUS);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_REQUEST_ID, requestId);

    }

    /**
     * Execute action get status params.
     * 
     * @param requestId the request id
     * 
     * @return the status mode response
     * 
     * @throws MotuRequestException the motu request exception
     */
    public StatusModeResponse executeActionGetStatusParams(long requestId) throws MotuRequestException {

        setActionGetStatusParams(requestId);
        StatusModeResponse statusModeResponse = (StatusModeResponse) executeAsXML();
        if (statusModeResponse == null) {
            throw new MotuRequestException("ERROR in executeActionGetStatusParams - no status response (statusModeResponse is null");
        }

        return statusModeResponse;
    }

    /**
     * Sets the action delete file.
     * 
     * @param files the files
     */
    public void setActionDeleteFile(List<String> files) {
        if (motuRequestParameters != null) {
            motuRequestParameters.clearParameters();
        } else {
            motuRequestParameters = new MotuRequestParameters();
        }

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_DELETE);
        motuRequestParameters.setMultiValuedParameter(MotuRequestParameters.PARAM_DATA, files);

    }

    /**
     * Sets the action delete file.
     * 
     * @param file the file
     */
    public void setActionDeleteFile(String file) {
        if (motuRequestParameters != null) {
            motuRequestParameters.clearParameters();
        } else {
            motuRequestParameters = new MotuRequestParameters();
        }

        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_ACTION, MotuRequestParameters.ACTION_DELETE);
        motuRequestParameters.setParameter(MotuRequestParameters.PARAM_DATA, file);

    }

    /**
     * Execute action delete file.
     * 
     * @param file the file
     * 
     * @return the status mode response
     * 
     * @throws MotuRequestException the motu request exception
     */
    public StatusModeResponse executeActionDeleteFile(String file) throws MotuRequestException {
        setActionDeleteFile(file);
        StatusModeResponse statusModeResponse = (StatusModeResponse) executeAsXML();
        if (statusModeResponse == null) {
            throw new MotuRequestException("ERROR in executeActionDeleteFile - no status response (statusModeResponse is null");
        }

        return statusModeResponse;
    }

    /**
     * Execute action delete file.
     * 
     * @param files the files
     * 
     * @return the status mode response
     * 
     * @throws MotuRequestException the motu request exception
     */
    public StatusModeResponse executeActionDeleteFile(List<String> files) throws MotuRequestException {

        setActionDeleteFile(files);
        StatusModeResponse statusModeResponse = (StatusModeResponse) executeAsXML();
        if (statusModeResponse == null) {
            throw new MotuRequestException("ERROR in executeActionDeleteFile - no status response (statusModeResponse is null");
        }

        return statusModeResponse;
    }

    /**
     * Gets the status done or error.
     * 
     * @param fullFilePath the full file path
     * 
     * @return the status mode response if done or error, otherwise null
     * 
     * @throws MotuRequestException the motu request exception
     */
    public StatusModeResponse getStatusDoneOrError(String fullFilePath) throws MotuRequestException {
        StatusModeResponse statusModeResponse = (StatusModeResponse) getMessageAsXML(fullFilePath);
        if ((statusModeResponse.getStatus().compareTo(StatusModeType.DONE) != 0)
                && (statusModeResponse.getStatus().compareTo(StatusModeType.ERROR) != 0)) {
            statusModeResponse = null;
        }
        return statusModeResponse;
    }

    /**
     * Gets the status pending or in progress.
     * 
     * @param fullFilePath the full file path
     * 
     * @return the status pending or in progress
     * 
     * @throws MotuRequestException the motu request exception
     */
    public StatusModeResponse getStatusPendingOrInProgress(String fullFilePath) throws MotuRequestException {
        StatusModeResponse statusModeResponse = (StatusModeResponse) getMessageAsXML(fullFilePath);
        if ((statusModeResponse.getStatus().compareTo(StatusModeType.PENDING) != 0)
                && (statusModeResponse.getStatus().compareTo(StatusModeType.INPROGRESS) != 0)) {
            statusModeResponse = null;
        }
        return statusModeResponse;
    }

    /**
     * Gets the message as XML.
     * 
     * @param fullFilePath the full file path
     * 
     * @return the message as XML
     * 
     * @throws MotuRequestException the motu request exception
     */
    public static Object getMessageAsXML(String fullFilePath) throws MotuRequestException {

        URL url = null;
        try {
            url = new URL(fullFilePath);
        } catch (MalformedURLException e) {
            throw new MotuRequestException("url creation error in getMessageAsXML", e);
        }
        return getMessageAsXML(url);

    }

    /**
     * Gets the message as XML.
     * 
     * @param url the url
     * 
     * @return the message as XML
     * 
     * @throws MotuRequestException the motu request exception
     */
    public static Object getMessageAsXML(URL url) throws MotuRequestException {

        Object object = null;

        try {
            InputStream in = url.openStream();
            object = getMessageAsXML(in);
            in.close();
        } catch (IOException e) {
            throw new MotuRequestException("url open stream error in getMessageAsXML", e);
        }

        return object;

    }

    /**
     * Gets the message as XML.
     * 
     * @param in the in
     * 
     * @return the message as XML
     * 
     * @throws MotuRequestException the motu request exception
     */
    public static Object getMessageAsXML(InputStream in) throws MotuRequestException {

        Object object = null;

        try {
            JAXBContext jc = JAXBContext.newInstance(MotuMsgConstant.MOTU_MSG_SCHEMA_PACK_NAME);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            object = unmarshaller.unmarshal(in);
        } catch (Exception e) {
            throw new MotuRequestException("request reading error in getMessageAsXML", e);
        }

        if (object == null) {
            throw new MotuRequestException("Unable to load XML in getMessageAsXML (returned object is null)");
        }
        try {
            in.close();
        } catch (IOException io) {
            // Do nothing
        }

        return object;
    }

    /**
     * Récupère les paramètres de la requête.
     * 
     * @return les paramètres de la requêtes.
     * 
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public String getRequestParams() throws UnsupportedEncodingException {

        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Map.Entry<String, Object> pv : motuRequestParameters.monoValuedParamMap.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append('&');
            }
            sb.append(pv.getKey());
            sb.append('=');
            sb.append(URLEncoder.encode(pv.getValue().toString(), "UTF-8"));
        }

        for (Map.Entry<String, List<?>> pmv : motuRequestParameters.multiValuedParamMap.entrySet()) {
            for (Object v : pmv.getValue()) {
                if (first) {
                    first = false;
                } else {
                    sb.append('&');
                }
                sb.append(pmv.getKey());
                sb.append('=');
                sb.append(v.toString());
            }
        }

        return sb.toString();
    }

    /**
     * Retourne l'url de la servlet motu.
     * 
     * @return l'url de la servlet motu
     */
    public String getServletUrl() {
        return servletUrl;
    }

    /**
     * Positionne l'url de la servlet motu.
     * 
     * @param servletUrl l'url de la servlet motu
     */
    public void setServletUrl(String servletUrl) {

        this.servletUrl = servletUrl;

        // requestExtraInfo = MotuRequest.searchUrlUserPwd(servletUrl);
        //
        // if (requestExtraInfo == null) {
        // return;
        // }
        //
        // this.servletUrl = requestExtraInfo.get(MotuRequestParametersConstant.PARAM_MODE_URL);
        // // System.out.println(requestExtraInfo.get(MotuRequestParametersConstant.PARAM_LOGIN));
        // // System.out.println(requestExtraInfo.get(MotuRequestParametersConstant.PARAM_PWD));
        // // System.out.println(requestExtraInfo.get(MotuRequestParametersConstant.PARAM_MODE_URL));

    }

    /**
     * Retourne les paramètres de la requête motu.
     * 
     * @return les paramètres de la requête motu
     */
    public MotuRequestParameters getMotuRequestParameters() {
        return motuRequestParameters;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Positionne les paramètres de la requête motu.
     * 
     * @param motuRequestParameters les paramètres de la requête motu
     */
    public void setMotuRequestParameters(MotuRequestParameters motuRequestParameters) {
        this.motuRequestParameters = motuRequestParameters;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName()).append("{");
        sb.append("\n   - url = \"").append(getServletUrl()).append("\"");
        sb.append("\n   - parameters = ").append(getMotuRequestParameters());
        sb.append("\n}");

        return sb.toString();
    }

    private static class RequestAuthenticator extends Authenticator {

        String username = "";
        String password = "";

        // This method is called when a password-protected URL is accessed
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            // Get information about the request
            // String promptString = getRequestingPrompt();
            // String hostname = getRequestingHost();
            // InetAddress ipaddr = getRequestingSite();
            // int port = getRequestingPort();

            // Return the information
            return new PasswordAuthentication(username, password.toCharArray());
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
