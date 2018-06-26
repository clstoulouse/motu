package fr.cls.atoll.motu.api.message.mxbean;

import javax.xml.datatype.XMLGregorianCalendar;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;

public interface StatusModeResponseMXBean {
    /**
     * Gets the value of the status property.
     * 
     * @return possible object is {@link StatusModeType }
     * 
     */
    public StatusModeType getStatus();

    /**
     * Gets the value of the requestId property.
     * 
     * @return possible object is {@link Long }
     * 
     */
    public String getRequestId();

    /**
     * Gets the value of the dateProc property.
     * 
     * @return possible object is {@link XMLGregorianCalendar }
     * 
     */
    public XMLGregorianCalendar getDateProc();

    /**
     * Gets the value of the size property.
     * 
     * @return possible object is {@link Double }
     * 
     */
    public Double getSize();

    /**
     * Gets the value of the remoteUri property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getRemoteUri();

    /**
     * Gets the value of the localUri property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getLocalUri();

    /**
     * Gets the value of the msg property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getMsg();

    /**
     * Gets the value of the code property.
     * 
     * @return possible object is {@link ErrorType }
     * 
     */
    public String getCode();

    /**
     * Gets the value of the user property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getUserId();

    /**
     * Gets the value of the userHost property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getUserHost();

    /**
     * Gets the value of the dateSubmit property.
     * 
     * @return possible object is {@link XMLGregorianCalendar }
     * 
     */
    public XMLGregorianCalendar getDateSubmit();

}
