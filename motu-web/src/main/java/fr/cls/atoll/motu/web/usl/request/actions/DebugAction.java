package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.queueserver.IQueueServerManager;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.QueueManagement;
import fr.cls.atoll.motu.web.bll.request.status.data.DownloadStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.NormalStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.config.xml.model.QueueType;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.DebugOrderHTTParameterValidator;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites) <br>
 * <br>
 * <br>
 * This interface is used to display debug information in HTML.<br>
 * Operation invocation consists in performing an HTTP GET request.<br>
 * Input parameters are the following: [x,y] is the cardinality<br>
 * <ul>
 * <li><b>action</b>: [1]: {@link #ACTION_NAME}</li>
 * </ul>
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class DebugAction extends AbstractAction {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "debug";
    public static final String ACTION_NAME_ALIAS_QUEUE_SERVER = "queue-server";
    public static final String ACTION_CODE = "003";

    private DebugOrderHTTParameterValidator debugOrderHTTParameterValidator;

    /**
     * Constructeur.
     * 
     */
    public DebugAction(HttpServletRequest request, HttpServletResponse response) {
        super(ACTION_NAME, ACTION_CODE, request, response);

        debugOrderHTTParameterValidator = new DebugOrderHTTParameterValidator(
                MotuRequestParametersConstant.PARAM_DEBUG_ORDER,
                CommonHTTPParameters.getDebugOrderFromRequest(getRequest()));
        MotuConfig config = BLLManager.getInstance().getConfigManager().getMotuConfig();
    }

    @Override
    public void process() throws MotuException {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<html>\n");
        stringBuilder.append("<head>\n");
        stringBuilder.append("<title>Motu | Debug</title>\n");
        stringBuilder.append("</head>\n");
        stringBuilder.append("<body>\n");

        debugRequestAllStatus(stringBuilder);
        debugPendingRequest(stringBuilder);

        stringBuilder.append("</body>\n");
        stringBuilder.append("<html>\n");

        try {
            writeResponse(stringBuilder.toString(), HTTPUtils.CONTENT_TYPE_HTML_UTF8);
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while wirting the response", e);
        }
    }

    /**
     * Debug request all status.
     * 
     * @param stringBuilder the string buffer
     */
    private void debugRequestAllStatus(StringBuilder stringBuilder) {
        if (stringBuilder == null) {
            return;
        }
        stringBuilder.append("<h1 align=\"center\">\n");
        stringBuilder.append("Request status");
        stringBuilder.append("</h1>\n");

        List<String> orders = debugOrderHTTParameterValidator.getParameterValueValidated();
        for (String currentItem : orders) {
            debugRequestStatus(stringBuilder, statusMapping(currentItem));
        }
    }

    private StatusModeType statusMapping(String status) {
        return StatusModeType.valueOf(status);
    }

    /**
     * Debug request status.
     * 
     * @param stringBuilder the string buffer
     * @param statusModeType the status mode type
     */
    private void debugRequestStatus(StringBuilder stringBuilder, StatusModeType statusModeType) {

        if (stringBuilder == null) {
            return;
        }

        String requestCountToken = "@@requestCountForStatus@@";
        stringBuilder.append("<h2>\n");
        stringBuilder.append(statusModeType.toString() + " (x" + requestCountToken + ")");
        stringBuilder.append("</h2>\n");
        stringBuilder.append("<table border=\"1\">\n");
        stringBuilder.append("<tr>\n");
        stringBuilder.append("<th>\n");
        stringBuilder.append("Request Id");
        stringBuilder.append("</th>\n");
        stringBuilder.append("<th>\n");
        stringBuilder.append("Request Type");
        stringBuilder.append("</th>\n");
        stringBuilder.append("<th>\n");
        stringBuilder.append("User Id");
        stringBuilder.append("</th>\n");
        stringBuilder.append("<th>\n");
        stringBuilder.append("Time");
        stringBuilder.append("</th>\n");
        stringBuilder.append("<th>\n");
        stringBuilder.append("Status");
        stringBuilder.append("</th>\n");
        stringBuilder.append("<th>\n");
        stringBuilder.append("Code");
        stringBuilder.append("</th>\n");
        stringBuilder.append("<th>\n");
        stringBuilder.append("Message");
        stringBuilder.append("</th>\n");
        stringBuilder.append("<th>\n");
        stringBuilder.append("Remote data");
        stringBuilder.append("</th>\n");
        stringBuilder.append("<th>\n");
        stringBuilder.append("Local data");
        stringBuilder.append("</th>\n");
        stringBuilder.append("</tr>\n");

        int requestCount = 0;
        Map<String, RequestStatus> status = BLLManager.getInstance().getRequestManager().getBllRequestStatusManager().getAllRequestStatus();
        for (Map.Entry<String, RequestStatus> currentRequest : status.entrySet()) {
            RequestStatus requestStatus = currentRequest.getValue();
            String requestId = currentRequest.getKey();
            if (requestStatus instanceof DownloadStatus) {
                DownloadStatus downloadStatus = (DownloadStatus) requestStatus;
                if (manageTheDownloadProductActionLog(stringBuilder, requestId, statusModeType, downloadStatus)) {
                    requestCount++;
                }
            } else if (requestStatus.getStatus().equals(statusModeType.name())) {
                requestCount++;
                manageTheActionLog(stringBuilder, requestId, requestStatus);
            }
        }
        stringBuilder.append("</table>\n");
        int startIndex = stringBuilder.indexOf(requestCountToken);
        stringBuilder.replace(startIndex, startIndex + requestCountToken.length(), Integer.toString(requestCount));
    }

    private void manageTheActionLog(StringBuilder stringBuilder, String requestId, RequestStatus requestStatus) {
        if (requestStatus instanceof NormalStatus) {
            stringBuilder.append("<tr>\n");
            stringBuilder.append("<td>\n");
            stringBuilder.append(requestId);
            stringBuilder.append("</td>\n");
            stringBuilder.append("<td>\n");
            stringBuilder.append(requestStatus.getActionName());
            stringBuilder.append("</td>\n");
            stringBuilder.append("<td>\n");
            stringBuilder.append(requestStatus.getUserId());
            stringBuilder.append("</td>\n");
            stringBuilder.append("<td>\n");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.valueOf(requestStatus.getTime()));
            stringBuilder.append(cal.getTime().toString());
            stringBuilder.append("</td>\n");
            stringBuilder.append("<td>\n");
            stringBuilder.append(requestStatus.getStatus());
            stringBuilder.append("</td>\n");
            stringBuilder.append("<td>\n");
            stringBuilder.append("1");
            stringBuilder.append("</td>\n");
            stringBuilder.append("<td>\n");
            stringBuilder.append(((NormalStatus) requestStatus).getParameters());
            stringBuilder.append("</td>\n");
            stringBuilder.append("<td>\n");
            stringBuilder.append("</td>\n");
            stringBuilder.append("<td>\n");
            stringBuilder.append("</td>\n");
            stringBuilder.append("</tr>\n");
            stringBuilder.append("\n");
        }
    }

    private boolean manageTheDownloadProductActionLog(StringBuilder stringBuilder,
                                                      String requestId,
                                                      StatusModeType statusModeType,
                                                      DownloadStatus downloadRequestStatus) {
        if (!downloadRequestStatus.getStatus().equals(statusModeType.name())) {
            return false;
        }

        stringBuilder.append("<tr>\n");
        stringBuilder.append("<td>\n");
        stringBuilder.append(requestId);
        stringBuilder.append("</td>\n");
        stringBuilder.append("<td>\n");
        stringBuilder.append(downloadRequestStatus.getActionName());
        stringBuilder.append("</td>\n");
        stringBuilder.append("<td>\n");
        stringBuilder.append(downloadRequestStatus.getUserId());
        stringBuilder.append("</td>\n");
        stringBuilder.append("<td>\n");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.valueOf(downloadRequestStatus.getTime()));
        stringBuilder.append(cal.getTime().toString());
        stringBuilder.append("</td>\n");
        stringBuilder.append("<td>\n");
        stringBuilder.append(downloadRequestStatus.getStatus());
        stringBuilder.append("</td>\n");
        stringBuilder.append("<td>\n");
        stringBuilder.append(downloadRequestStatus.getStatusCode());
        stringBuilder.append("</td>\n");
        stringBuilder.append("<td>\n");
        if (!StringUtils.isNullOrEmpty(downloadRequestStatus.getMessage())) {
            stringBuilder.append(downloadRequestStatus.getMessage());
            stringBuilder.append("<BR>");
        }
        stringBuilder.append("Length : ");
        if (downloadRequestStatus.getSize() != null) {
            stringBuilder.append(downloadRequestStatus.getSize());
            stringBuilder.append("Mbits");
        }
        stringBuilder.append("<BR>Last modified: ");

        if (downloadRequestStatus.getDateProc() != null) {
            Calendar calDateProc = Calendar.getInstance();
            calDateProc.setTimeInMillis(Long.valueOf(downloadRequestStatus.getTime()));
            if (calDateProc.get(Calendar.YEAR) == 1970) {
                stringBuilder.append("Unknown");
            } else {
                stringBuilder.append(calDateProc.getTime().toString());
            }
        }
        if (!StringUtils.isNullOrEmpty(downloadRequestStatus.getScriptVersion())) {
            stringBuilder.append("<BR> ScriptVersion:" + downloadRequestStatus.getScriptVersion());
        }
        stringBuilder.append("</td>\n");
        stringBuilder.append("<td>\n");
        String remoteURL = BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath() + "/"
                + downloadRequestStatus.getOutputFileName();
        if (remoteURL.endsWith("/")) {
            stringBuilder.append("No file.");
        } else if (!remoteURL.endsWith("null")) {
            stringBuilder.append(remoteURL);
        } else {
            stringBuilder.append("In progress...");
        }
        stringBuilder.append("</td>\n");
        stringBuilder.append("<td>\n");
        String localPath = BLLManager.getInstance().getConfigManager().getMotuConfig().getDownloadHttpUrl() + "/"
                + downloadRequestStatus.getOutputFileName();
        if (localPath.endsWith("/")) {
            stringBuilder.append("No file.");
        } else if (!localPath.endsWith("null")) {
            stringBuilder.append(localPath);
        } else {
            stringBuilder.append("In progress...");
        }
        stringBuilder.append("</td>\n");
        stringBuilder.append("</tr>\n");
        stringBuilder.append("\n");
        return true;
    }

    /**
     * Debug pending request.
     * 
     * @param stringBuilder the string buffer
     */
    private void debugPendingRequest(StringBuilder stringBuilder) {
        if (stringBuilder == null) {
            return;
        }

        stringBuilder.append("<hr/><h1 align=\"center\">\n");
        stringBuilder.append("Queue server general configuration\n");
        stringBuilder.append("</h1>\n");

        IQueueServerManager queueServerManagement = BLLManager.getInstance().getRequestManager().getQueueServerManager();
        if (queueServerManagement == null) {
            stringBuilder.append("<p> Queue server is not active</p>");
            return;
        }
        stringBuilder.append("<p>\n");
        stringBuilder.append(" Max. data threshold: ");
        stringBuilder.append(String.format("%8.2f Mo",
                                           BLLManager.getInstance().getRequestManager().getQueueServerManager().getMaxDataThresholdInMegabyte()));
        stringBuilder.append("</p>\n");

        debugPendingRequest(stringBuilder, queueServerManagement);
    }

    /**
     * Debug pending request.
     *
     * @param stringBuilder the string buffer
     * @param queueServerManagement the queue server management
     * @param batch the batch
     */
    private void debugPendingRequest(StringBuilder stringBuilder, IQueueServerManager queueServerManagement) {
        List<QueueType> queuesConfig = BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig().getQueues();
        QueueManagement queueManagement = null;
        boolean hasQueue = false;
        // queues are sorted by data threshold (ascending)
        for (QueueType queueConfig : queuesConfig) {
            queueManagement = queueServerManagement.getQueueManagementMap().get(queueConfig);
            if (queueManagement == null) {
                continue;
            }

            hasQueue = true;

            stringBuilder.append("<h3>\n");
            stringBuilder.append(queueConfig.getId());
            stringBuilder.append(": ");
            stringBuilder.append(queueConfig.getDescription());
            stringBuilder.append("</h3>\n");
            stringBuilder.append("<table border=\"1\">\n");
            stringBuilder.append("<p>\n");
            stringBuilder.append("Max. pool anonymous: ");
            short maxPoolAnonymous = BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig().getMaxPoolAnonymous();
            stringBuilder.append(maxPoolAnonymous < 0 ? "unlimited" : maxPoolAnonymous);

            stringBuilder.append(" Max. pool authenticate: ");
            short maxPoolAuth = BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig().getMaxPoolAuth();
            stringBuilder.append(maxPoolAuth < 0 ? "unlimited" : maxPoolAuth);
            stringBuilder.append("</p>\n");
            stringBuilder.append("<p>\n");
            stringBuilder.append("Max. threads: ");
            stringBuilder.append(queueConfig.getMaxThreads());
            stringBuilder.append(" Data threshold: ");
            stringBuilder.append(String.format("%8.2f Mo", queueConfig.getDataThreshold()));
            stringBuilder.append(" Max. pool size: ");
            stringBuilder.append(queueConfig.getMaxPoolSize());
            stringBuilder.append(" Low priority waiting: ");
            stringBuilder.append(queueConfig.getLowPriorityWaiting());
            stringBuilder.append("</p>\n");
            stringBuilder.append("<p>\n");
            stringBuilder.append(" Approximate number of threads that are actively executing tasks: ");
            stringBuilder.append(queueManagement.getThreadPoolExecutor().getActiveCount());
            stringBuilder.append("</p>\n");
            stringBuilder.append("<p>\n");
            stringBuilder.append(" Approximate total number of tasks that have completed execution: ");
            stringBuilder.append(queueManagement.getThreadPoolExecutor().getCompletedTaskCount());
            stringBuilder.append("</p>\n");

            stringBuilder.append("</table>\n");
        }
        if (!hasQueue) {
            stringBuilder.append("None");
        }

    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        debugOrderHTTParameterValidator.validate();
    }

}
