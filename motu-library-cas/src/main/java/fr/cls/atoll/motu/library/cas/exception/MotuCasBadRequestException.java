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
package fr.cls.atoll.motu.library.cas.exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import fr.cls.atoll.motu.library.cas.util.RestUtil;

/**
 * The Class MotuCasBadRequestException.
 */
public class MotuCasBadRequestException extends MotuCasException {

    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuCasBadRequestException.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1L;

    /** The code. */
    protected Integer code = -1;
    
    /** The url. */
    protected String url;

    /** The Constant STATUS_LINE_FIELD. */
    public static final String STATUS_LINE_FIELD = "Status-line";
    
    /** The header fields. */
    protected MultivaluedMap<String, String> headerFields = new MultivaluedMapImpl();

    // public MotuCasBadRequestException(String message, Throwable cause) {
    // super(message, cause);
    // }
    //
    // public MotuCasBadRequestException(String message) {
    // super(message);
    // }
    //
    // public MotuCasBadRequestException(Throwable cause) {
    // super(cause);
    // }

    /**
     * Instantiates a new motu cas bad request exception.
     *
     * @param code the code
     * @param url the url
     * @param message the message
     */
    public MotuCasBadRequestException(int code, String url, String message) {
        super(message);
        this.code = code;
        this.url = url;
    }

    /**
     * Instantiates a new motu cas bad request exception.
     *
     * @param code the code
     * @param url the url
     * @param message the message
     * @param cause the cause
     */
    public MotuCasBadRequestException(int code, String url, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.url = url;
    }

    /**
     * Instantiates a new motu cas bad request exception.
     *
     * @param code the code
     * @param url the url
     */
    public MotuCasBadRequestException(int code, String url) {
        this(code, url, "Bad request.");
    }

    /**
     * Instantiates a new motu cas bad request exception.
     *
     * @param code the code
     * @param url the url
     * @param cause the cause
     */
    public MotuCasBadRequestException(int code, String url, Throwable cause) {
        this(code, url, "Bad request.", cause);
    }
       
    /**
     * Instantiates a new motu cas bad request exception.
     *
     * @param conn the conn
     * @param code the code
     * @param url the url
     * @param message the message
     */
    public MotuCasBadRequestException(HttpURLConnection conn, int code, String url, String message) {
        this(code, url, message);
        setHeaderFields(conn);

    }

    /**
     * Instantiates a new motu cas bad request exception.
     *
     * @param conn the conn
     * @param code the code
     * @param url the url
     * @param message the message
     * @param cause the cause
     */
    public MotuCasBadRequestException(HttpURLConnection conn, int code, String url, String message, Throwable cause) {
        this(code, url, message, cause);
        setHeaderFields(conn);
    }

    /**
     * Instantiates a new motu cas bad request exception.
     *
     * @param conn the conn
     * @param code the code
     * @param url the url
     */
    public MotuCasBadRequestException(HttpURLConnection conn, int code, String url) {
        this(code, url, "Bad request.");
        setHeaderFields(conn);
    }

    /**
     * Instantiates a new motu cas bad request exception.
     *
     * @param conn the conn
     * @param code the code
     * @param url the url
     * @param cause the cause
     */
    public MotuCasBadRequestException(HttpURLConnection conn, int code, String url, Throwable cause) {
        this(code, url, "Bad request.", cause);
        setHeaderFields(conn);
    }
    
    public static MotuCasBadRequestException createMotuCasBadRequestException(ClientResponse response, String url) {     
            return MotuCasBadRequestException.createMotuCasBadRequestException(response, url, "");
    }       
    /**
     * Creates the motu cas bad request exception.
     *
     * @param response the response
     * @param url the url
     * @return the motu cas bad request exception
     */
    public static MotuCasBadRequestException createMotuCasBadRequestException(ClientResponse response, String url, String message) {     
        if (response == null) {
            return new MotuCasBadRequestException(-1, url);
        }
        
        InputStream entity = response.getEntityInputStream();

        String responseEntity = "";
        if (entity != null) {
            try {
                responseEntity = IOUtils.toString(entity);
            } catch (IOException e) {
                // Do nothing
            }
        }
        
        StringBuffer stringBuffer = new StringBuffer();
        
        if (!RestUtil.isNullOrEmpty(message)) {
            stringBuffer.append(message);
            stringBuffer.append("\n");
        }
        
        stringBuffer.append(responseEntity);
        
        MotuCasBadRequestException exception = new MotuCasBadRequestException(response.getStatus(), url, stringBuffer.toString());
        exception.setHeaderFields(response.getHeaders());
        exception.getHeaderFields().add(MotuCasBadRequestException.STATUS_LINE_FIELD, response.toString());
        
        return exception;
    }

    
    /**
     * Gets the code.
     *
     * @return the code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Gets the header fields.
     *
     * @return the header fields
     */
    public MultivaluedMap<String, String> getHeaderFields() {
        return headerFields;
    }
    
    /**
     * Gets the header field.
     *
     * @param key the key
     * @return the header field
     */
    public List<String> getHeaderFields(String key) {
        return headerFields.get(key);
    }
    
    /**
     * Gets the header fields.
     *
     * @param key the key
     * @return the header fields
     */
    public String getHeaderField(String key) {
        return headerFields.getFirst(key);
    }

    /**
     * Sets the header fields.
     *
     * @param headerFields the header fields
     */
    public void setHeaderFields(MultivaluedMap<String, String> headerFields) {
        this.headerFields = headerFields;
    }

    /**
     * Sets the code.
     *
     * @param code the new code
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * Sets the url.
     *
     * @param url the new url
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /* (non-Javadoc)
     * @see fr.cls.atoll.motu.library.cas.exception.MotuCasException#notifyException()
     */
    @Override
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(super.notifyException());
        stringBuffer.append("\nUrl is '");
        stringBuffer.append(url);
        stringBuffer.append("'");

        stringBuffer.append("\nCode is ");
        stringBuffer.append(code);

        stringBuffer.append("\nHttp Headers are ");
        stringBuffer.append(headerFields.toString());
        return stringBuffer.toString();
    }

    /* (non-Javadoc)
     * @see fr.cls.atoll.motu.library.cas.exception.MotuCasException#notifyLogException()
     */
    @Override
    public void notifyLogException() {
        super.notifyLogException();
        LOG.warn("Url is '" + url + "'");
        LOG.warn("Code is " + code.toString());
        LOG.warn("Http Headers are " + headerFields.toString());
    }

    /**
     * Sets the header fields.
     *
     * @param conn the new header fields
     */
    public void setHeaderFields(HttpURLConnection conn) {

        // java doc about getHeaderFieldKey:
        //
        // Returns the key for the nth header field.
        // Some implementations may treat the 0th header field as special,
        // i.e. as the status line returned by the HTTP server.
        // In this case, getHeaderField(0) returns the status line,
        // but getHeaderFieldKey(0) returns null.

        // WARNING HttpURLConnection#getHeaderFields() can return an empty map
        // We retrieve header field until key or value is null (except for index 0 which can be null - see
        // above)

        int index = 0;
        // getHeaderField(0) can return an non-null value, but getHeaderFieldKey(0) can return a null value
        String fieldKey = conn.getHeaderFieldKey(index);
        
        if (RestUtil.isNullOrEmpty(fieldKey))
        {
            fieldKey = MotuCasBadRequestException.STATUS_LINE_FIELD; // a non-standard key
        }
        
        String fieldValue = conn.getHeaderField(index);

        boolean hasField = ((fieldKey != null) && (fieldValue != null));

        if (hasField) {
            headerFields.add(fieldKey, fieldValue);
        }

        index++;

        fieldKey = conn.getHeaderFieldKey(index);
        fieldValue = conn.getHeaderField(index);

        hasField = ((fieldKey != null) && (fieldValue != null));

        while (hasField) {
            headerFields.add(fieldKey, fieldValue);

            index++;

            fieldKey = conn.getHeaderFieldKey(index);
            fieldValue = conn.getHeaderField(index);

            hasField = ((fieldKey != null) && (fieldValue != null));
        }

    }

 
}
