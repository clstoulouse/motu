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
package fr.cls.atoll.motu.processor.wps;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.ExtractionParameters;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.queueserver.RunnableExtraction;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletOutputs;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.9 $ - $Date: 2009-10-07 14:00:39 $
 */
public class RunnableWPSExtraction extends RunnableExtraction {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(RunnableWPSExtraction.class);

    // final ReentrantLock lock = new ReentrantLock();
    // final Condition requestEndedCondition = lock.newCondition();
    /** The lock. */
    private ReentrantLock lock = null;

    /** The mode. */
    private String mode = null;

    /** The request ended. */
    private Condition requestEndedCondition = null;

    /** The response. */
    private ProcessletOutputs response = null;

    // public boolean notEnded = true;

    // public void waitFor() throws InterruptedException {
    // try {
    //
    // lock.lock();
    // while (notEnded) {
    // requestEndedCondition.await();
    // }
    // } finally {
    // lock.unlock();
    // }
    // }

    /**
     * The Constructor.
     * 
     * @param response the response
     * @param range the range
     * @param requestEndedCondition the request ended
     * @param priority the priority
     * @param organizer the organizer
     * @param lock the lock
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     */
    public RunnableWPSExtraction(
        int priority,
        int range,
        Organizer organizer,
        ExtractionParameters extractionParameters,
        ProcessletOutputs response,
        String mode,
        Condition requestEndedCondition,
        ReentrantLock lock) {

        super(priority, range, organizer, extractionParameters);

        init(response, mode, requestEndedCondition, lock);
    }

    /**
     * The Constructor.
     * 
     * @param response the response
     * @param requestEndedCondition the request ended
     * @param priority the priority
     * @param organizer the organizer
     * @param lock the lock
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     */
    public RunnableWPSExtraction(
        int priority,
        Organizer organizer,
        ExtractionParameters extractionParameters,
        ProcessletOutputs response,
        String mode,
        Condition requestEndedCondition,
        ReentrantLock lock) {

        super(priority, organizer, extractionParameters);

        init(response, mode, requestEndedCondition, lock);
    }

    /**
     * Aborted.
     */
    @Override
    public void aborted() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("RunnableHttpExtraction.aborted() - entering");
        }

        try {
            if (noMode()) {
                setAbortedNoMode();
            } else if (isModeStatus()) {
                setAbortedModeStatus();
            } else if (isModeConsole()) {
                setAbortedModeConsole();
            } else if (isModeUrl()) {
                setAbortedModeUrl();
            }
        } finally {
            lock.lock();
            requestEndedCondition.signal();
            lock.unlock();
        }
        // ------------------
        super.aborted();
        // ------------------

        if (LOG.isDebugEnabled()) {
            LOG.debug("RunnableHttpExtraction.aborted() - exiting");
        }
    }

    /**
     * Valeur de mode.
     * 
     * @return la valeur.
     */
    public String getMode() {
        return mode;
    }

    /**
     * Checks for mode.
     * 
     * @return true, if has mode
     */
    public boolean hasMode() {
        return !MotuWPSProcess.noMode(mode);
    }

    /**
     * No mode.
     * 
     * @return true, if no mode
     */
    public boolean noMode() {
        return MotuWPSProcess.noMode(mode);
    }

    /**
     * Sets the ended.
     */
    @Override
    public void setEnded() {

        try {
            super.setEnded();

            if (noMode()) {
                return;
            } else if (isModeStatus()) {
                setResponseModeStatus();
            } else if (isModeConsole()) {
                setResponseModeConsole();
            } else if (isModeUrl()) {
                setResponseModeUrl();
            }
        } finally {
            lock.lock();
            requestEndedCondition.signal();
            lock.unlock();
        }
    }

    /**
     * Sets the in queue.
     */
    @Override
    public void setInQueue() {
        super.setInQueue();
    }

    /**
     * Sets the started.
     */
    @Override
    public void setStarted() {
        super.setStarted();
    }

    /**
     * Init.
     * 
     * @param responseP the response
     * @param modeP the mode
     * @param lockP the lock
     * @param requestEndedConditionP the request ended condition
     */
    private void init(ProcessletOutputs responseP, String modeP, Condition requestEndedConditionP, ReentrantLock lockP) {

        this.response = responseP;
        this.mode = modeP;
        this.requestEndedCondition = requestEndedConditionP;
        this.lock = lockP;

        setStatusPending();
        // this.notEnded = true;

    }

    protected void setUrl() throws MotuException {
        MotuWPSProcess.setUrl(response, statusModeResponse.getRemoteUri());

    }

    protected void setLocalUri() throws MotuException {
        MotuWPSProcess.setUrl(response, statusModeResponse.getLocalUri());

    }

    protected void setStatus() {
        MotuWPSProcess.setStatus(response, statusModeResponse.getStatus());

    }

    protected void setReturnCode() {
        try {
            MotuWPSProcess.setReturnCode(response, statusModeResponse, false);
        } catch (ProcessletException e) {
            // Do Nothing
        }

    }

    //
    // /** {@inheritDoc} */
    // @Override
    // protected void setStatusDone() throws MotuException {
    // super.setStatusDone();
    // setStatus();
    // setUrl();
    // setReturnCode();
    //        
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // protected void setStatusInProgress() {
    // super.setStatusInProgress();
    // setStatus();
    // setReturnCode();
    // }
    //
    // /** {@inheritDoc}
    // * @throws MotuException */
    // @Override
    // protected void setStatusPending() {
    // super.setStatusPending();
    // setStatus();
    // setReturnCode();
    //
    // }

    /**
     * Checks if is mode console.
     * 
     * @return true, if is mode console
     */
    private boolean isModeConsole() {
        return MotuWPSProcess.isModeConsole(mode);
    }

    /**
     * Checks if is mode status.
     * 
     * @return true, if is mode status
     */
    private boolean isModeStatus() {
        return MotuWPSProcess.isModeStatus(mode);
    }

    /**
     * Checks if is mode url.
     * 
     * @return true, if is mode url
     */
    private boolean isModeUrl() {
        return MotuWPSProcess.isModeUrl(mode);
    }

    /**
     * Send response error http 500.
     * 
     * @param msg the msg
     */
    private void sendResponseError500(String msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendResponseError500(String) - entering");
        }
        if (response == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("sendResponseError500(String) - exiting");
            }
            return;
        }
        setStatus();
        setReturnCode();

        if (LOG.isDebugEnabled()) {
            LOG.debug("sendResponseError500(String) - exiting");
        }
    }

    /**
     * Sets the aborted mode console.
     */
    private void setAbortedModeConsole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAbortedModeConsole() - entering");
        }

        // if (response == null) {
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("setAbortedModeConsole() - exiting");
        // }
        // return;
        // }
        //
        // try {
        // setStatus();
        // setReturnCode();
        // } catch (Exception e) {
        // LOG.error("setAbortedModeConsole()", e);
        //
        // sendResponseError500(e.getMessage());
        // }
        //
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAbortedModeConsole() - exiting");
        }
    }

    /**
     * Sets the aborted mode status.
     */
    private void setAbortedModeStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAbortedModeStatus() - entering");
        }
        // if (response == null) {
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("setAbortedModeStatus() - response == null - exiting");
        // }
        // return;
        // }
        //
        // setStatus();
        // setReturnCode();

        if (LOG.isDebugEnabled()) {
            LOG.debug("setAbortedModeStatus() - exiting");
        }
    }

    /**
     * Sets the aborted mode url.
     */
    private void setAbortedModeUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAbortedModeUrl() - entering");
        }

        // setAbortedModeConsole();

        if (LOG.isDebugEnabled()) {
            LOG.debug("setAbortedModeUrl() - exiting");
        }
    }

    /**
     * Sets the aborted no mode.
     */
    private void setAbortedNoMode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAbortedNoMode() - entering");
        }

        // if (queueLogInfo.getQueueLogError() != null) {
        // MotuWPSProcess.setReturnCode(response, queueLogInfo.getQueueLogError().getErrorCode(),
        // queueLogInfo.getQueueLogError().getMessage());
        // }

        if (LOG.isDebugEnabled()) {
            LOG.debug("setAbortedNoMode() - exiting");
        }
    }

    /**
     * Sets the response mode console.
     */
    private void setResponseModeConsole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setResponseModeConsole() - entering");
        }

        if (response == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setResponseModeConsole() - exiting");
            }
            return;
        }
        try {
            // $$$$$ response.sendRedirect(product.getDownloadUrlPath());
        } catch (Exception e) {
            LOG.error("setResponseModeConsole()", e);

            setError(e);
            sendResponseError500(statusModeResponse.getMsg());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("setResponseModeConsole() - exiting");
        }
    }

    /**
     * Sets the response mode status.
     */
    private void setResponseModeStatus() {
        if (response == null) {
            return;
        }

        // Nothing to do, it's a deferred request
        // and the response is set in statusModeResponse and
        // asked by the client using a request provided fot that.
    }

    /**
     * Sets the response mode url.
     */
    private void setResponseModeUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setResponseModeUrl() - entering");
        }

        if (response == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setResponseModeUrl() - exiting");
            }
            return;
        }
        try {
            // $$$$$ response.setContentType(MotuServlet.CONTENT_TYPE_PLAIN);
            // $$$$$ Writer out = response.getWriter();
            // $$$$$ out.write(product.getDownloadUrlPath());
        } catch (Exception e) {
            LOG.error("setResponseModeUrl()", e);

            setError(e);
            sendResponseError500(statusModeResponse.getMsg());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("setResponseModeUrl() - exiting");
        }
    }

}
