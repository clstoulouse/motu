package fr.cls.atoll.motu.web.usl.wcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.request.USLRequestManager;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.wcs.data.ExceptionData;
import fr.cls.atoll.motu.web.usl.wcs.responses.ExceptionBuilder;
import ucar.nc2.constants.AxisType;

public class Utils {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    public static void onException(HttpServletResponse response,
                                   String actionCode,
                                   String wcsErrorCode,
                                   ErrorType errorType,
                                   Exception e,
                                   Object... parameters)
            throws MotuException {
        Throwable t = e.getCause();
        Exception me = null;
        while (t != null && !(t instanceof MotuExceptionBase)) {
            if (t instanceof MotuException) {
                me = (Exception) t;
            }
            t = t.getCause();
        }

        String errMessage = "";
        if (me instanceof MotuException) {
            errorType = ((MotuException) me).getErrorType();
            errMessage = BLLManager.getInstance().getMessagesErrorManager().getMessageError(errorType, me);
        } else if (me instanceof InvalidHTTPParameterException) {
            errorType = ErrorType.BAD_PARAMETERS;
            errMessage = e.getMessage();
        } else {
            errMessage = BLLManager.getInstance().getMessagesErrorManager().getMessageError(errorType, parameters);
        }

        if (USLRequestManager.isErrorTypeToLog(errorType)) {
            LOGGER.error(StringUtils.getLogMessage(actionCode, errorType, errMessage), e);
        }

        ExceptionData data = new ExceptionData();
        data.setErrorCode(wcsErrorCode);
        // if (!"".equals(locator)) {
        // data.setLocator(locator);
        // }

        List<String> messageLine = new ArrayList<>();
        messageLine.add(errMessage);
        data.setErrorMessage(messageLine);

        try {
            response.getWriter().write(ExceptionBuilder.getInstance().buildResponse(data));
        } catch (IOException | JAXBException e2) {
            LOGGER.error("Error while processing HTTP request", e2);
            throw new MotuException(ErrorType.SYSTEM, "Error while processing HTTP request", e2);
        }

    }

    public static String onError(String actionCode, String wcsErrorCode, ErrorType errorType, Object... parameters) throws MotuException {
        return onError(actionCode, "", wcsErrorCode, errorType, parameters);
    }

    public static String onError(String actionCode, String locator, String wcsErrorCode, ErrorType errorType, Object... parameters)
            throws MotuException {
        ExceptionData data = new ExceptionData();
        data.setErrorCode(wcsErrorCode);
        if (!"".equals(locator)) {
            data.setLocator(locator);
        }

        List<String> messageLine = new ArrayList<>();
        messageLine.add((StringUtils
                .getLogMessage(actionCode, errorType, BLLManager.getInstance().getMessagesErrorManager().getMessageError(errorType, parameters))));
        data.setErrorMessage(messageLine);

        try {
            return ExceptionBuilder.getInstance().buildResponse(data);
        } catch (JAXBException e) {
            LOGGER.error("Error while processing HTTP request", e);
            throw new MotuException(ErrorType.SYSTEM, "Error while processing HTTP request", e);
        }
    }

    public static boolean contains(AxisType[] array, String data) {
        boolean contains = false;

        for (AxisType currentData : array) {
            if (currentData.toString().equals(data)) {
                contains = true;
            }
        }

        return contains;
    }
}
