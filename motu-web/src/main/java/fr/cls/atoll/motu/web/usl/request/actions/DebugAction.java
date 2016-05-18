package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.XMLGregorianCalendar;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.library.misc.configuration.QueueType;
import fr.cls.atoll.motu.library.misc.queueserver.QueueManagement;
import fr.cls.atoll.motu.library.misc.queueserver.QueueServerManagement;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.request.queueserver.RunnableExtraction;

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

    public static final String ACTION_NAME = "debug";
    public static final String ACTION_NAME_ALIAS_QUEUE_SERVER = "queue-server";

    /**
     * Constructeur.
     * 
     * @param actionName_
     */
    public DebugAction(HttpServletRequest request, HttpServletResponse response) {
        super(ACTION_NAME, request, response);
    }

    @Override
    public void process() throws IOException {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("<html>\n");
        stringBuffer.append("<head>\n");
        stringBuffer.append("</head>\n");
        stringBuffer.append("<body>\n");

        debugRequestAllStatus(stringBuffer);
        debugPendingRequest(stringBuffer);

        stringBuffer.append("</body>\n");
        stringBuffer.append("<html>\n");

        getResponse().setContentType(CONTENT_TYPE_HTML);
        getResponse().getWriter().write(stringBuffer.toString());
    }

    /**
     * Debug request all status.
     * 
     * @param stringBuffer the string buffer
     */
    private void debugRequestAllStatus(StringBuffer stringBuffer) {
        if (stringBuffer == null) {
            return;
        }
        stringBuffer.append("<h1 align=\"center\">\n");
        stringBuffer.append("Request status");
        stringBuffer.append("</h1>\n");

        for (StatusModeType statusModeType : StatusModeType.values()) {
            debugRequestStatus(stringBuffer, statusModeType);

        }
    }

    /**
     * Debug request status.
     * 
     * @param stringBuffer the string buffer
     * @param statusModeType the status mode type
     */
    private void debugRequestStatus(StringBuffer stringBuffer, StatusModeType statusModeType) {

        if (stringBuffer == null) {
            return;
        }

        stringBuffer.append("<h2>\n");
        stringBuffer.append("Status: \n");
        stringBuffer.append(statusModeType.toString());
        stringBuffer.append("</h2>\n");
        stringBuffer.append("<table border=\"1\">\n");
        stringBuffer.append("<tr>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Request Id");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Time");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Status");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Code");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Message");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Remote data");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Local data");
        stringBuffer.append("</th>\n");
        stringBuffer.append("</tr>\n");

        List<Long> requestIds = BLLManager.getInstance().getRequestManager().getRequestIds();

        for (Long requestId : requestIds) {
            StatusModeResponse statusModeResponse = BLLManager.getInstance().getRequestManager().getResquestStatus(requestId);
            if (statusModeResponse.getStatus() != statusModeType) {
                continue;
            }

            stringBuffer.append("<tr>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(requestId.toString());
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(requestId);
            stringBuffer.append(cal.getTime().toString());
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(statusModeResponse.getStatus().toString());
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(statusModeResponse.getCode().toString());
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(statusModeResponse.getMsg());
            stringBuffer.append(" - file length is : ");
            if (statusModeResponse.getSize() != null) {
                stringBuffer.append(statusModeResponse.getSize());
            } else {
                stringBuffer.append("null");
            }
            stringBuffer.append(" - file lastModified is : ");
            if (statusModeResponse.getDateProc() != null) {
                XMLGregorianCalendar lastModified = statusModeResponse.getDateProc().normalize();
                stringBuffer.append(lastModified.toString());
            } else {
                stringBuffer.append("null");
            }
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(statusModeResponse.getRemoteUri());
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(statusModeResponse.getLocalUri());
            stringBuffer.append("</td>\n");
            stringBuffer.append("</tr>\n");
            stringBuffer.append("\n");
        }
        stringBuffer.append("</table>\n");

    }

    /**
     * Debug pending request.
     * 
     * @param stringBuffer the string buffer
     */
    private void debugPendingRequest(StringBuffer stringBuffer) {
        if (stringBuffer == null) {
            return;
        }

        stringBuffer.append("<h1 align=\"center\">\n");
        stringBuffer.append("Pending requests\n");
        stringBuffer.append("</h1>\n");

        QueueServerManagement queueServerManagement = requestManagement.getQueueServerManagement();
        if (queueServerManagement == null) {
            stringBuffer.append("<p> Queue server is not active</p>");
            return;
        }
        stringBuffer.append("<h2>\n");
        stringBuffer.append("Queue server general configuration");
        stringBuffer.append("</h2>\n");
        stringBuffer.append("<p>\n");
        stringBuffer.append(" Default priority: ");
        stringBuffer.append(queueServerManagement.getDefaultPriority());
        stringBuffer.append(" Max. data threshold non-batch: ");
        stringBuffer.append(String.format("%8.2f Mo", queueServerManagement.getMaxDataThreshold(false)));
        stringBuffer.append(" Max. data threshold batch: ");
        stringBuffer.append(String.format("%8.2f Mo", queueServerManagement.getMaxDataThreshold(true)));
        stringBuffer.append("</p>\n");

        stringBuffer.append("<h2>\n");
        stringBuffer.append("Non-Batch Queues");
        stringBuffer.append("</h2>\n");
        debugPendingRequest(stringBuffer, queueServerManagement, false);
        stringBuffer.append("<h2>\n");
        stringBuffer.append("Batch Queues");
        stringBuffer.append("</h2>\n");
        debugPendingRequest(stringBuffer, queueServerManagement, true);
    }

    /**
     * Debug pending request.
     *
     * @param stringBuffer the string buffer
     * @param queueServerManagement the queue server management
     * @param batch the batch
     */
    private void debugPendingRequest(StringBuffer stringBuffer, QueueServerManagement queueServerManagement, boolean batch) {

        if (stringBuffer == null) {
            return;
        }

        if (queueServerManagement == null) {
            return;
        }

        List<QueueType> queuesConfig = queueServerManagement.getQueuesConfig();
        QueueManagement queueManagement = null;

        boolean hasQueue = false;

        // queues are sorted by data threshold (ascending)
        for (QueueType queueConfig : queuesConfig) {
            queueManagement = queueServerManagement.getQueueManagement(queueConfig);
            if (queueManagement == null) {
                continue;
            }
            if (queueConfig.getBatch() != batch) {
                continue;
            }

            hasQueue = true;

            stringBuffer.append("<h3>\n");
            stringBuffer.append(queueConfig.getId());
            stringBuffer.append(": ");
            stringBuffer.append(queueConfig.getDescription());
            stringBuffer.append("</h3>\n");
            stringBuffer.append("<table border=\"1\">\n");
            stringBuffer.append("<p>\n");
            stringBuffer.append("Max. pool anonymous: ");
            if (queueConfig.getBatch()) {
                stringBuffer.append("unlimited");
            } else {
                stringBuffer.append(queueServerManagement.computeMaxPoolAnonymous(queueManagement));
            }
            stringBuffer.append(" Max. pool authenticate: ");
            if (queueConfig.getBatch()) {
                stringBuffer.append("unlimited");
            } else {
                stringBuffer.append(queueServerManagement.computeMaxPoolAuthenticate(queueManagement));
            }
            stringBuffer.append("</p>\n");
            stringBuffer.append("<p>\n");
            stringBuffer.append("Max. threads: ");
            stringBuffer.append(queueConfig.getMaxThreads());
            stringBuffer.append(" Data threshold: ");
            stringBuffer.append(String.format("%8.2f Mo", queueConfig.getDataThreshold()));
            stringBuffer.append(" Max. pool size: ");
            stringBuffer.append(queueConfig.getMaxPoolSize());
            stringBuffer.append(" Low priority waiting: ");
            stringBuffer.append(queueConfig.getLowPriorityWaiting());
            stringBuffer.append("</p>\n");
            stringBuffer.append("<p>\n");
            stringBuffer.append(" Approximate number of threads that are actively executing tasks: ");
            stringBuffer.append(queueManagement.getThreadPoolExecutor().getActiveCount());
            stringBuffer.append("</p>\n");
            stringBuffer.append("<p>\n");
            stringBuffer.append(" Approximate total number of tasks that have completed execution: ");
            stringBuffer.append(queueManagement.getThreadPoolExecutor().getCompletedTaskCount());
            stringBuffer.append("</p>\n");

            PriorityBlockingQueue<Runnable> priorityBlockingQueue = queueManagement.getPriorityBlockingQueue();

            if (priorityBlockingQueue == null) {
                stringBuffer.append("</table>\n");
                continue;
            }

            stringBuffer.append("<tr>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Request Id");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Status");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Mode");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Priority");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Range");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Amount data size");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("User");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Anonymous ?");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("User Host");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("In queue since");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Extraction parameters");
            stringBuffer.append("</th>\n");
            stringBuffer.append("</tr>\n");

            for (Runnable runnable : priorityBlockingQueue) {
                if (!(runnable instanceof RunnableExtraction)) {
                    continue;
                }

                stringBuffer.append("<tr>\n");
                RunnableExtraction runnableExtraction = (RunnableExtraction) runnable;
                StatusModeResponse statusModeResponse = runnableExtraction.getStatusModeResponse();
                if (statusModeResponse == null) {
                    stringBuffer.append("</tr>\n");
                    continue;
                }
                stringBuffer.append("<td>\n");
                stringBuffer.append(statusModeResponse.getRequestId().toString());
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(statusModeResponse.getStatus().toString());
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                String mode = runnableExtraction.getMode();
                if (mode != null) {
                    stringBuffer.append(runnableExtraction.getMode());
                }
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(Integer.toString(runnableExtraction.getPriority()));
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(Integer.toString(runnableExtraction.getRange()));
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(String.format("%8.2f Mo", runnableExtraction.getQueueLogInfo().getAmountDataSize()));
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(runnableExtraction.getExtractionParameters().getUserId());
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(runnableExtraction.getExtractionParameters().isAnonymousUser());
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(runnableExtraction.getExtractionParameters().getUserHost());
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                if (runnableExtraction.getQueueLogInfo().getInQueueTime() != null) {
                    stringBuffer.append(runnableExtraction.getQueueLogInfo().getInQueueTime().toString());
                } else {
                    stringBuffer.append("Unknown");
                }
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(runnableExtraction.getExtractionParameters().toString());
                stringBuffer.append("</td>\n");
                stringBuffer.append("</tr>\n");
            }
            stringBuffer.append("</table>\n");
        }
        if (!hasQueue) {
            stringBuffer.append("None");
            return;
        }

    }

}
