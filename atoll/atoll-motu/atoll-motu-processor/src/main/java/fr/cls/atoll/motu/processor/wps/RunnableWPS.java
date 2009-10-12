package fr.cls.atoll.motu.processor.wps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.httpclient.HttpException;
import org.deegree.commons.utils.HttpUtils;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.msg.xml.ErrorType;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;
import fr.cls.atoll.motu.msg.xml.StatusModeType;
import fr.cls.atoll.motu.processor.opengis.wps100.Execute;
import fr.cls.atoll.motu.processor.wps.framework.WPSFactory;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.3 $ - $Date: 2009-10-12 14:13:29 $
 */
public class RunnableWPS implements Runnable, Comparable<RunnableWPS> {

    /** The range. */
    protected int range = -1;
    protected Execute execute = null;
    protected String schemaLocationKey = "";
    protected String name = "";
    protected String serverURL = null;
    
    /** The lock. */
    protected ReentrantLock lock = null;

    /** The mode. */
    protected String mode = null;

    /** The request ended. */
    protected Condition requestEndedCondition = null;

    /** The status mode response. */
    protected StatusModeResponse statusModeResponse = null;

    public RunnableWPS(String name, int range, String serverURL, Execute execute, String schemaLocationKey, Condition requestEndedCondition, ReentrantLock lock) {

        init();

        this.name = name;
        this.range = range;
        this.serverURL = serverURL;
        this.execute = execute;
        this.schemaLocationKey = schemaLocationKey;
        
        this.requestEndedCondition = requestEndedCondition;
        this.lock = lock;

        setStatusPending();


    }

    /**
     * Init.
     */
    private void init() {

        statusModeResponse = Organizer.createStatusModeResponse();
        setStatusPending();

    }

    /**
     * Sets the error according to an exception.
     * 
     * @param e the exception
     */
    public void setError(Exception e) {
        Organizer.setError(statusModeResponse, e);
    }

    /**
     * Sets the status done.
     * 
     * @throws MotuException
     */
    protected void setStatusDone() throws MotuException {

        // Organizer.setStatusDone(statusModeResponse, product);

    }

    /**
     * Sets the status in progress.
     * 
     * @throws MotuException
     */
    protected void setStatusInProgress() {
        statusModeResponse.setStatus(StatusModeType.INPROGRESS);
        statusModeResponse.setMsg(StatusModeType.INPROGRESS.toString());
        statusModeResponse.setCode(ErrorType.OK);

    }

    /**
     * Sets the status pending.
     * 
     * @throws MotuException
     */
    protected void setStatusPending() {
        statusModeResponse.setStatus(StatusModeType.PENDING);
        statusModeResponse.setMsg(StatusModeType.PENDING.toString());
        statusModeResponse.setCode(ErrorType.OK);

    }

    /**
     * Sets the status error.
     * 
     * @param msg the status error
     * 
     */
    protected void setStatusError(String msg) {
        statusModeResponse.setStatus(StatusModeType.ERROR);
        statusModeResponse.setMsg(msg);
        statusModeResponse.setCode(ErrorType.OK);

    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        InputStream inputStream = null;

        try {
            inputStream = marshallExecute();
        } catch (MotuMarshallException e) {
            MotuException motuException = new MotuException(String.format("An error occurs during marshalling WPS '%s' execution (RunnableWPS.run).",
                                                                          name), e);
            setError(motuException);
        }
        
        Map<String, String> headers = new HashMap<String, String>();
        
        InputStream response = null;
        
        try {
            setStatusInProgress();

            response = HttpUtils.post(HttpUtils.STREAM, serverURL, inputStream, headers);

            setStatusDone();
        
        } catch (Exception e) {
            MotuException motuException = new MotuException(String.format("An error occurs during during  WPS request execution '%s' execution (RunnableWPS.run).",
                                                                          name), e);
            setError(motuException);
        }

        
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(RunnableWPS obj) {

        // max range at the top
        int objRange = obj.getRange();
        int retval = 0;
        if (range > objRange) {
            return -1;
        }
        if (range < objRange) {
            return 1;
        }
        if (retval == 0) {
            retval = Integer.valueOf(range).compareTo(Integer.valueOf(obj.getRange()));
        }
        return retval;
    }

    public int getRange() {
        return range;
    }

    public InputStream marshallExecute() throws MotuMarshallException {

        if (execute == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out);

        WPSFactory.marshallExecute(execute, writer, schemaLocationKey);

        return new ByteArrayInputStream(out.toByteArray());

    }
}
