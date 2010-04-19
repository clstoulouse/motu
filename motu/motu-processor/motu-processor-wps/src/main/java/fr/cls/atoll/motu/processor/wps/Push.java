package fr.cls.atoll.motu.processor.wps;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.processor.wps.framework.WPSUtils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ReferencedComplexInput;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.7 $, $Date: 2009-12-16 10:15:24 $
 */
public class Push extends MotuWPSProcess {

    /**
     * Constructor.
     */
    public Push() {
    }

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(Push.class);

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN CompressExtraction.process(), context: " + OGCFrontController.getContext());
        }

        super.process(in, out, info);

        try {
            push(in);
        } catch (MotuException e) {
            setReturnCode(out, e, true);
        } finally {
            super.afterProcess(in, out, info);
        }

    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("CompressExtraction#destroy() called");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("CompressExtraction#init() called");
        }
        super.init();

        // runnableWPS = new
    }

    /**
     * Push.
     * 
     * @param in the in
     * 
     * @throws ProcessletException the processlet exception
     * @throws MotuException the motu exception
     */
    private void push(ProcessletInputs in) throws ProcessletException, MotuException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        String from = MotuWPSProcess.getComplexInputValueFromBinaryStream(motuWPSProcessData.getFromParamIn());

        // Either the the source url (from) or the request id
        // have to be set as input parameter
        // If 'from' is null or empty : use resquest id and get the its remote url as 'from' parameter
        if (WPSUtils.isNullOrEmpty(from)) {
            long requestId = processRequestIdAsLong(in);

            if (requestId < 0) {
                return;
            }

            StatusModeResponse statusModeResponse = waitForResponse(motuWPSProcessData.getRequestIdParamIn(), requestId);

            if (statusModeResponse == null) {
                // MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), new
                // MotuInvalidRequestIdException(requestId), motuWPSProcessData
                // .getRequestIdParamIn() instanceof ReferencedComplexInput);
                MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), new MotuInvalidRequestIdException(requestId), true);
                return;
            }

            if (MotuWPSProcess.isStatusDone(statusModeResponse)) {
                from = statusModeResponse.getRemoteUri();
            } else {
                MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(),
                                             statusModeResponse,
                                             motuWPSProcessData.getRequestIdParamIn() instanceof ReferencedComplexInput);
                return;
            }

        }

        // if 'from' parameter contains an error message (it has been set by a child wps)
        // then throw exception if from i not a referenced input (from a parent wps)
        if (WPSUtils.isProcessletExceptionErrorMessageEncode(from)) {

            MotuWPSProcess.setComplexOutputParameters(motuWPSProcessData.getProcessletOutputs(), from);
            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(),
                                         from,
                                         motuWPSProcessData.getFromParamIn() instanceof ReferencedComplexInput);

            return;
        }

        URI outputUriToShow = null;
        boolean remove = false;

        String to = MotuWPSProcess.getComplexInputValueFromBinaryStream(motuWPSProcessData.getToParamIn());

        // Only sftp and ftp protocol are allowed
        URI uriToControl = null;
        try {
            uriToControl = new URI(to);
        } catch (URISyntaxException e) {
            MotuWPSProcess.setUrl(motuWPSProcessData.getProcessletOutputs(), "");
            MotuWPSProcess.setLocalUrl(motuWPSProcessData.getProcessletOutputs(), "");
            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), e, true);
            return;
        }

        String schemeToControl = "";

        if (uriToControl.getScheme() != null) {
            schemeToControl = uriToControl.getScheme();
        }

        if ((schemeToControl.compareToIgnoreCase("sftp") != 0) && (schemeToControl.compareToIgnoreCase("ftp") != 0)) {
            MotuWPSProcess.setUrl(motuWPSProcessData.getProcessletOutputs(), "");
            MotuWPSProcess.setLocalUrl(motuWPSProcessData.getProcessletOutputs(), "");
            String msg = "Push process allows only 'ftp' and 'sftp' protocols";
            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), ErrorType.INCONSISTENCY, msg, true);
            return;
        }

        String userFrom = MotuWPSProcess.getLiteralInputValue(motuWPSProcessData.getUserFromParamIn());
        String pwdFrom = MotuWPSProcess.getLiteralInputValue(motuWPSProcessData.getPwdFromParamIn());

        String userTo = MotuWPSProcess.getLiteralInputValue(motuWPSProcessData.getUserToParamIn());
        String pwdTo = MotuWPSProcess.getLiteralInputValue(motuWPSProcessData.getPwdToParamIn());

        remove = MotuWPSProcess.getLiteralInputValueAsBoolean(motuWPSProcessData.getRemoveParamIn(), false);

        boolean rename = MotuWPSProcess.getLiteralInputValueAsBoolean(motuWPSProcessData.getRenameParamIn(), false);

        if (!rename) {
            File fileTmp = new File(from);
            to = String.format("%s/%s", to, fileTmp.getName());
        }

        URI uriFrom = null;
        try {
            // Is user/pwd in the source url ?
            if (!WPSUtils.isNullOrEmpty(from)) {
                uriFrom = new URI(from);

                if (WPSUtils.isNullOrEmpty(userFrom)) {
                    String theUserInfo = uriFrom.getUserInfo();
                    if (!WPSUtils.isNullOrEmpty(theUserInfo)) {
                        String userInfo[] = theUserInfo.split(":");
                        if (userInfo.length >= 1) {
                            userFrom = userInfo[0];
                        }
                        if (userInfo.length >= 2) {
                            pwdFrom = userInfo[1];
                        }
                    }
                }
            }
            // Is user/pwd in the destination url ?
            if (WPSUtils.isNullOrEmpty(userTo)) {
                String theUserInfo = uriToControl.getUserInfo();
                if (!WPSUtils.isNullOrEmpty(theUserInfo)) {
                    String userInfo[] = theUserInfo.split(":");
                    if (userInfo.length >= 1) {
                        userTo = userInfo[0];
                    }
                    if (userInfo.length >= 2) {
                        pwdTo = userInfo[1];
                    }
                }
            }

            if ((WPSUtils.isNullOrEmpty(userFrom)) && (WPSUtils.isNullOrEmpty(userTo))) {
                Organizer.copyFile(from, to);
            } else {
                Organizer.copyFile(from, to, userFrom, pwdFrom, userTo, pwdTo);
            }
            String userToSetInDestUrl = (WPSUtils.isNullOrEmpty(userTo) ? null : userTo);

            URI accessUriTemp = new URI(to);
            outputUriToShow = new URI(accessUriTemp.getScheme(), userToSetInDestUrl, accessUriTemp.getHost(), accessUriTemp.getPort(), accessUriTemp
                    .getPath(), accessUriTemp.getQuery(), accessUriTemp.getFragment());

            MotuWPSProcess.setUrl(motuWPSProcessData.getProcessletOutputs(), outputUriToShow);
            MotuWPSProcess.setLocalUrl(motuWPSProcessData.getProcessletOutputs(), "");

        } catch (Exception e) {
            MotuWPSProcess.setUrl(motuWPSProcessData.getProcessletOutputs(), "");
            MotuWPSProcess.setLocalUrl(motuWPSProcessData.getProcessletOutputs(), "");
            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), e, true);
            return;
        }

        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(String.format("Uri '%s' have been transfered to '%s'", from, outputUriToShow.toString()));

        ErrorType errorType = ErrorType.OK;

        if (remove) {
            try {
                Organizer.deleteFile(from);
                stringBuffer.append(String.format("Uri '%s' have been deleted", from));
            } catch (MotuException e) {
                errorType = ErrorType.SYSTEM;
                stringBuffer.append(e.notifyException());
            }
        }

        MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), errorType, stringBuffer.toString(), true);

    }

}
