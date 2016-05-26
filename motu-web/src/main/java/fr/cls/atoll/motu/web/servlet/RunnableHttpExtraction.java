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
package fr.cls.atoll.motu.web.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.rest.MotuRequestParameters;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.bll.request.queueserver.RunnableExtraction;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class RunnableHttpExtraction extends RunnableExtraction {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LogManager.getLogger();

    /** The lock. */
    private ReentrantLock lock = null;

    /** The mode. */
    private String mode = null;

    /** The request ended. */
    private Condition requestEndedCondition = null;

    /** The response. */
    private HttpServletResponse response = null;

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
    public RunnableHttpExtraction(
        int priority,
        int range,
        Organizer organizer,
        ExtractionParameters extractionParameters,
        HttpServletResponse response,
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
    public RunnableHttpExtraction(
        int priority,
        Organizer organizer,
        ExtractionParameters extractionParameters,
        HttpServletResponse response,
        String mode,
        Condition requestEndedCondition,
        ReentrantLock lock) {

        super(priority, organizer, extractionParameters);

        init(response, mode, requestEndedCondition, lock);
    }

    /**
     * Checks for mode.
     * 
     * @param mode the mode
     * 
     * @return true, if has mode
     */
    public static boolean hasMode(String mode) {
        return !RunnableHttpExtraction.noMode(mode);
    }

    /**
     * Checks if is mode console.
     * 
     * @param mode the mode
     * 
     * @return true, if is mode console
     */
    public static boolean isModeConsole(String mode) {
        boolean isMode = false;
        if (RunnableHttpExtraction.hasMode(mode)) {
            isMode = mode.equalsIgnoreCase(MotuRequestParameters.PARAM_MODE_CONSOLE);
        }
        return isMode;
    }

    /**
     * Checks if is mode status.
     * 
     * @param mode the mode
     * 
     * @return true, if is mode status
     */
    public static boolean isModeStatus(String mode) {
        boolean isMode = false;
        if (RunnableHttpExtraction.hasMode(mode)) {
            isMode = mode.equalsIgnoreCase(MotuRequestParameters.PARAM_MODE_STATUS);
        }
        return isMode;
    }

    /**
     * Checks if is mode url.
     * 
     * @param mode the mode
     * 
     * @return true, if is mode url
     */
    public static boolean isModeUrl(String mode) {
        boolean isMode = false;
        if (RunnableHttpExtraction.hasMode(mode)) {
            isMode = mode.equalsIgnoreCase(MotuRequestParameters.PARAM_MODE_URL);
        }
        return isMode;
    }

    /**
     * No mode.
     * 
     * @param mode the mode
     * 
     * @return true, if no mode
     */
    public static boolean noMode(String mode) {
        return MotuServlet.isNullOrEmpty(mode);
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
    @Override
    public String getMode() {
        return mode;
    }

    /**
     * Checks for mode.
     * 
     * @return true, if has mode
     */
    public boolean hasMode() {
        return !RunnableHttpExtraction.noMode(mode);
    }

    /**
     * No mode.
     * 
     * @return true, if no mode
     */
    public boolean noMode() {
        return RunnableHttpExtraction.noMode(mode);
    }

    /**
     * Sets the ended.
     */
    @Override
    public void setEnded() {

        try {
            super.setEnded();

            if (!(statusModeResponse.getCode().equals(ErrorType.OK))) {
                aborted();
                return;
            }

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
    private void init(HttpServletResponse responseP, String modeP, Condition requestEndedConditionP, ReentrantLock lockP) {

        this.response = responseP;
        this.mode = modeP;
        this.requestEndedCondition = requestEndedConditionP;
        this.lock = lockP;
        // this.notEnded = true;

    }

    /**
     * Checks if is mode console.
     * 
     * @return true, if is mode console
     */
    private boolean isModeConsole() {
        return RunnableHttpExtraction.isModeConsole(mode);
    }

    /**
     * Checks if is mode status.
     * 
     * @return true, if is mode status
     */
    private boolean isModeStatus() {
        return RunnableHttpExtraction.isModeStatus(mode);
    }

    /**
     * Checks if is mode url.
     * 
     * @return true, if is mode url
     */
    private boolean isModeUrl() {
        return RunnableHttpExtraction.isModeUrl(mode);
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

        try {
            response.sendError(500, String.format("ERROR: %s", msg));
        } catch (IOException e) {
            LOG.error("sendResponseError500()", e);
        }

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

        if (response == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setAbortedModeConsole() - exiting");
            }
            return;
        }

        try {
            response.setContentType(MotuServlet.CONTENT_TYPE_PLAIN);
            Writer out = response.getWriter();
            out.write(String.format("ERROR: %s", statusModeResponse.getMsg()));
        } catch (Exception e) {
            LOG.error("setAbortedModeConsole()", e);

            sendResponseError500(e.getMessage());
        }

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
        if (response == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setAbortedModeStatus() - response == null - exiting");
            }
            return;
        }
        if (statusModeResponse == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setAbortedModeStatus() - statusModeResponse == null - exiting");
            }
            return;
        }

        response.setContentType(null);
        try {

            // If error is SHUTTING_DOWN : nothing to do, it's a deferred request
            // and the response is set in statusModeResponse and
            // asked by the client using a request provided for that.

            if (!statusModeResponse.getCode().equals(ErrorType.SHUTTING_DOWN)) {
                Organizer.marshallStatusModeResponse(statusModeResponse, response.getWriter());
            }
        } catch (MotuMarshallException e) {
            LOG.error("setAbortedModeStatus()", e);
            sendResponseError500(e.notifyException());
        } catch (IOException e) {
            LOG.error("setAbortedModeStatus()", e);
            sendResponseError500(e.getMessage());
        }

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

        setAbortedModeConsole();

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

        Writer out = null;
        try {
            boolean writeHTML = false;
            out = response.getWriter();
            if (organizer.getCurrentService() != null) {
                writeHTML = organizer.getCurrentService().writeProductDownloadHTML(product, out);
            }
            if (!writeHTML) {
                if (queueLogInfo.getQueueLogError() != null) {
                    response.setContentType(MotuServlet.CONTENT_TYPE_PLAIN);
                    out.write(queueLogInfo.getQueueLogError().toString());
                }
            }

        } catch (MotuException e) {
            LOG.error("setAbortedNoMode()", e);

            response.setContentType(MotuServlet.CONTENT_TYPE_PLAIN);
            try {
                out.write(Organizer.getFormattedError(e, null));
            } catch (IOException e1) {
                LOG.error("setAbortedNoMode()", e1);

                // Do Nothing
            }
            setError(e);
        } catch (IOException e) {
            LOG.error("setAbortedNoMode()", e);

            setError(e);

        }

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
            if (statusModeResponse.getCode().equals(ErrorType.OK)) {
                response.sendRedirect(product.getDownloadUrlPath());
            } else {
                sendResponseError500(statusModeResponse.getMsg());
            }
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
        // asked by the client using a request provided for that.
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
            response.setContentType(MotuServlet.CONTENT_TYPE_PLAIN);
            Writer out = response.getWriter();
            out.write(product.getDownloadUrlPath());
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
