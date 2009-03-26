package fr.cls.atoll.motu.processor.wps;

import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.LiteralOutput;

import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.msg.xml.ErrorType;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-03-26 15:42:42 $
 */
public abstract class MotuWPSProcess implements Processlet {

    /**
     * Constructeur.
     */
    public MotuWPSProcess() {
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    @Override
    public void init() {

    }

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {
        // TODO Auto-generated method stub

    }

    /**
     * Test if a string is null or empty.
     * 
     * @param value string to be tested.
     * 
     * @return true if string is null or empty, otherwise false.
     */
    protected static boolean isNullOrEmpty(String value) {
        if (value == null) {
            return true;
        }
        if (value.equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Checks if is null or empty.
     * 
     * @param value the value
     * 
     * @return true, if is null or empty
     */
    protected static boolean isNullOrEmpty(LiteralInput value) {
        if (value == null) {
            return true;
        }

        return isNullOrEmpty(value.getValue());
    }

    protected static void setReturnCode(ErrorType code, String msg, ProcessletOutputs out) {

        LiteralOutput codeParam = (LiteralOutput) out.getParameter(MotuWPSProcess.PARAM_CODE);
        LiteralOutput msgParam = (LiteralOutput) out.getParameter(MotuWPSProcess.PARAM_MESSAGE);

        if ((codeParam != null) && (code != null)) {
            codeParam.setValue(code.toString());
        }
        if ((msgParam != null) && (msg != null)) {
            msgParam.setValue(msg);
        }

    }

    /**
     * Gets Organizer object form the HttpSession.
     * 
     * @param response the response
     * @param session that contains Organizer.
     * 
     * @return Organizer object.
     */
    protected Organizer getOrganizer(ProcessletOutputs response) {

        Organizer organizer = null;
        try {
            organizer = new Organizer();
        } catch (MotuExceptionBase e) {
            String msg = String.format("ERROR: - MotuWPSProcess.getOrganizer - Unable to create a new organiser. Native Error: %s", e
                    .notifyException());
            MotuWPSProcess.setReturnCode(ErrorType.SYSTEM, msg, response);
        }

        return organizer;
    }

    /** Url parameter name. */
    public static final String PARAM_URL = "Url";

    /** Service servlet parameter name. */
    public static final String PARAM_SERVICE = "Service";

    /** Product servlet parameter name. */
    public static final String PARAM_PRODUCT = "Product";

    public static final String PARAM_START = "Start";
    
    public static final String PARAM_END = "End";

    /** Process output return code parameter name. */
    public static final String PARAM_CODE = "Code";

    /** Process output message parameter name. */
    public static final String PARAM_MESSAGE = "Message";

}
