package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.ObjectFactory;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.api.utils.JAXBWriter;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuMarshallException;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class DeleteAction extends AbstractAction {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "delete";

    /**
     * Constructor of the DeleteAction class.
     * 
     * @param request The delete request to manage
     * @param response The response object used to return the response of the request
     * @param session The session object of the request
     */
    public DeleteAction(HttpServletRequest request_, HttpServletResponse response_, HttpSession session_) {
        super(ACTION_NAME, request_, response_, session_);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        // Nothing to do
    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws MotuException {
        String[] urls = CommonHTTPParameters.getListOfDataFromParameter(getRequest());

        if (urls != null) {
            deleteFile(urls);
        }
    }

    /**
     * Delete file.
     *
     * @param urls the urls to delete
     * @param response the response
     */
    protected void deleteFile(String[] urls) {
        Writer writer = null;
        boolean hasErrors = false;
        StringBuffer messages = new StringBuffer();

        try {

            writer = getResponse().getWriter();
            getResponse().setContentType(null);

            boolean[] fileDeletionStatus = BLLManager.getInstance().getRequestManager().deleteFiles(urls);

            String extractionPath = BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath();
            String downloadHttpUrl = BLLManager.getInstance().getConfigManager().getMotuConfig().getDownloadHttpUrl();

            int cpteFile = 0;
            for (boolean currentStatus : fileDeletionStatus) {

                if (currentStatus) {
                    messages.append(String.format("==>File '%s' is deleted\n", urls[cpteFile]));
                } else {
                    hasErrors = true;
                    messages.append(String.format("==>Unable to delete file '%s' (internal name '%s')\n",
                                                  urls[cpteFile],
                                                  urls[cpteFile].replace(downloadHttpUrl, extractionPath)));
                }
                cpteFile++;
            }

            StatusModeResponse statusModeResponse = createStatusModeResponse();

            if (hasErrors) {
                statusModeResponse.setCode(ErrorType.SYSTEM);
                statusModeResponse.setStatus(StatusModeType.ERROR);
            } else {
                statusModeResponse.setCode(ErrorType.OK);
                statusModeResponse.setStatus(StatusModeType.DONE);
            }

            statusModeResponse.setMsg(messages.toString());

            marshallStatusModeResponse(statusModeResponse, writer);

        } catch (Exception e) {
            try {
                marshallStatusModeResponse(e, writer);
            } catch (MotuMarshallException e2) {
            }
        }
    }

    /**
     * Marshall status mode response.
     * 
     * @param writer the writer
     * @param statusModeResponse the status mode response
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public static void marshallStatusModeResponse(StatusModeResponse statusModeResponse, Writer writer) throws MotuMarshallException {
        if (writer == null) {
            return;
        }

        if (statusModeResponse == null) {
            return;
        }
        try {
            JAXBWriter.getInstance().write(statusModeResponse, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new MotuMarshallException("Error in Organizer - marshallStatusModeResponse", e);
        }
    }

    /**
     * Marshall status mode response.
     * 
     * @param ex the ex
     * @param writer the writer
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public static void marshallStatusModeResponse(Exception ex, Writer writer) throws MotuMarshallException {

        if (writer == null) {
            return;
        }

        StatusModeResponse statusModeResponse = createStatusModeResponse();
        setError(statusModeResponse, ex);
        try {
            JAXBWriter.getInstance().write(statusModeResponse, writer);
            writer.flush();
            writer.close();
        } catch (JAXBException e) {
            throw new MotuMarshallException("Error in Organizer - marshallRequestSize", e);
        } catch (IOException e) {
            throw new MotuMarshallException("Error in Organizer - marshallRequestSize", e);
        }
    }

    /**
     * Creates the status mode response.
     * 
     * @return the status mode response
     */
    public static StatusModeResponse createStatusModeResponse() {
        ObjectFactory objectFactory = new ObjectFactory();
        StatusModeResponse statusModeResponse = objectFactory.createStatusModeResponse();
        setError(statusModeResponse, new MotuException("If you see that message, the request has failed and the error has not been filled"));
        return statusModeResponse;
    }

    /**
     * Sets the error.
     * 
     * @param e the e
     * @param statusModeResponse the status mode response
     */
    public static void setError(StatusModeResponse statusModeResponse, Exception e) {
        ErrorType errorType = ErrorType.SYSTEM;
        statusModeResponse.setStatus(StatusModeType.ERROR);
        statusModeResponse.setMsg(e.getMessage());
        statusModeResponse.setCode(errorType);
    }
}
