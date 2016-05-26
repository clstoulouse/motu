package fr.cls.atoll.motu.web.bll.request;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.servlet.RunnableHttpExtraction;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class DownloadManager {

    private static final Logger LOGGER = LogManager.getLogger();

    public DownloadManager() {

    }

    /**
     * Product download.
     *
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     * @param priority the priority
     * @param session the session
     * @param response the response
     * @throws IOException the IO exception
     */
    private void productDownload(ExtractionParameters extractionParameters, String mode, int priority) throws IOException {
        boolean modeStatus = RunnableHttpExtraction.isModeStatus(mode);

        RunnableHttpExtraction runnableHttpExtraction = null;
        StatusModeResponse statusModeResponse = null;

        final ReentrantLock lock = new ReentrantLock();
        final Condition requestEndedCondition = lock.newCondition();

        String serviceName = extractionParameters.getServiceName();
        Organizer organizer = getOrganizer(getSession(), getResponse());
        try {

            if (organizer.isGenericService() && !StringUtils.isNullOrEmpty(serviceName)) {
                organizer.setCurrentService(serviceName);
            }
        } catch (MotuException e) {
            LOGGER.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            getResponse().sendError(400, String.format("ERROR: %s", e.notifyException()));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse) - exiting");
            }
            return;
        }

        long requestId = BLLManager.getInstance().getRequestManager().getNewRequestId();

        try {
            // ------------------------------------------------------
            lock.lock();
            // ------------------------------------------------------

            getQueueServerManagement().execute(runnableHttpExtraction);

            if (modeStatus) {
                getResponse().setContentType(null);
                Organizer.marshallStatusModeResponse(statusModeResponse, response.getWriter());
            } else {
                // --------- wait for the end of the request -----------
                requestEndedCondition.await();
                // ------------------------------------------------------
            }
        } catch (MotuMarshallException e) {
            LOGGER.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            getResponse().sendError(500, String.format("ERROR: %s", e.getMessage()));
        } catch (MotuExceptionBase e) {
            LOGGER.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            runnableHttpExtraction.aborted();
            // Do nothing error is in response error code
            // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        } catch (Exception e) {
            LOGGER.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            runnableHttpExtraction.aborted();
            // response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        } finally {
            // ------------------------------------------------------
            if (lock.isLocked()) {
                lock.unlock();
            }
            // ------------------------------------------------------
        }
    }

}
