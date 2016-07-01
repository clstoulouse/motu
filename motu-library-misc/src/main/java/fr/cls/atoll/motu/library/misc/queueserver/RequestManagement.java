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
package fr.cls.atoll.motu.library.misc.queueserver;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.ObjectName;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.utils.MBeanUtils;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class RequestManagement implements JobListener, RequestManagementMBean {

    // Name pattern under which managed beans get registered
    public final static String OBJECT_NAME_PATTERN = "fr.cls:artefact=Motu,domain=Resquests,id={0},element={1}";

    /** The Constant FILE_PATTERN_KEYMAP. */
    public static final String DIR_TO_SCAN_KEYMAP = "dirToScan";

    /** The Constant FILE_PATTERN_KEYMAP. */
    public static final String FILE_PATTERN_KEYMAP = "filePattern";

    /** The Constant SCHEDULE_CLEAN_JOB_NAME. */
    public static final String SCHEDULE_CLEAN_JOB_NAME = "scheduleCleanJob";

    /** The Constant SCHEDULE_CLEAN_TRIGGER_NAME. */
    public static final String SCHEDULE_CLEAN_TRIGGER_NAME = "scheduleCleanTrigger";

    /** The Constant SCHEDULE_RUNGC_JOB_NAME. */
    public static final String SCHEDULE_RUNGC_JOB_NAME = "scheduleRunGCJob";

    /** The Constant SCHEDULE_RUNGC_TRIGGER_NAME. */
    public static final String SCHEDULE_RUNGC_TRIGGER_NAME = "scheduleRunGCTrigger";

    /** The Request Management instance. */
    private static RequestManagement instance;

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(RequestManagement.class);

    /** The use queue server. */
    private static boolean useQueueServer = true;

    /** The last request id. */
    private long lastRequestId = 0;

    /** The queue server management instance. */
    private QueueServerManagement queueServerManagement = null;

    /** The resquest status map. */
    private final ConcurrentMap<Long, StatusModeResponse> requestStatusMap = new ConcurrentHashMap<Long, StatusModeResponse>();

    /** The scheduler. */
    private Scheduler scheduler = null;

    /**
     * Constructor.
     * 
     * @throws MotuException the motu exception
     */
    protected RequestManagement() throws MotuException {
        init();
    }

    /**
     * Gets the instance.
     * 
     * @return the instance
     * 
     * @throws MotuException the motu exception
     */
    public static RequestManagement getInstance() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstance() - entering");
        }

        if (instance == null) {
            instance = new RequestManagement();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstance() - exiting");
        }
        return instance;
    }

    /**
     * Gets the status mode response keymap.
     * 
     * @return the status mode response keymap
     */
    public static String getStatusModeResponseKeymap() {
        return StatusModeResponse.class.getSimpleName();
    }

    /**
     * Checks if is use queue server.
     * 
     * @return true, if is use queue server
     */
    public static boolean isUseQueueServer() {
        return useQueueServer;
    }

    /**
     * Sets the use queue server.
     * 
     * @param useQueueServer the use queue server
     */
    public static void setUseQueueServer(boolean useQueueServer) {
        RequestManagement.useQueueServer = useQueueServer;
    }

    /**
     * Clear resquest status map.
     */
    public void clearRequestStatusMap() {
        requestStatusMap.clear();
    }

    /**
     * Creates the and schedule cleaning job.
     * 
     * @throws MotuException the motu exception
     */
    public void createAndScheduleCleanJob() throws MotuException {
        try {
            JobDetail jobDetail = new JobDetail(RequestManagement.SCHEDULE_CLEAN_JOB_NAME, Scheduler.DEFAULT_GROUP, ScheduleCleanJob.class);

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(RequestManagement.getStatusModeResponseKeymap(), requestStatusMap);

            jobDataMap.put(RequestManagement.FILE_PATTERN_KEYMAP, Organizer.getMotuConfigInstance().getExtractionFilePatterns());
            jobDataMap.put(RequestManagement.DIR_TO_SCAN_KEYMAP, Organizer.getMotuConfigInstance().getExtractionPath());

            jobDetail.setJobDataMap(jobDataMap);

            // Trigger trigger = TriggerUtils.makeMinutelyTrigger(queueConfig.getLowPriorityWaiting());
            // trigger.setName(SCHEDULE_PRIORITY_TRIGGER_NAME);
            Trigger trigger = TriggerUtils.makeMinutelyTrigger(RequestManagement.SCHEDULE_CLEAN_TRIGGER_NAME,
                                                               Organizer.getMotuConfigInstance().getRunCleanInterval(),
                                                               SimpleTrigger.REPEAT_INDEFINITELY);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 1);
            trigger.setStartTime(cal.getTime());

            // LOG.info(trigger.getStartTime());

            scheduler.addJobListener(this);

            // make sure the listener is associated with the job
            jobDetail.addJobListener(getName());

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new MotuException("ERROR in RequestManagement.createAndScheduleCleanJob.", e);
        }

    }

    // /**
    // * Gets the schedule job name.
    // *
    // * @return the schedule job name
    // */
    // public String getScheduleJobName() {
    // return RequestManagement.SCHEDULE_CLEAN_JOB_NAME;
    // }
    //
    // /**
    // * Gets the schedule trigger name.
    // *
    // * @return the schedule trigger name
    // */
    // public String getScheduleTriggerName() {
    // return RequestManagement.SCHEDULE_CLEAN_TRIGGER_NAME;
    // }

    /**
     * Creates the and schedule run GC job.
     * 
     * @throws MotuException the motu exception
     */
    public void createAndScheduleRunGCJob() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createAndScheduleRunGCJob() - entering");
        }

        try {

            int runGCInterval = Organizer.getMotuConfigInstance().getRunGCInterval();

            if (runGCInterval <= 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("createAndScheduleRunGCJob() - runGCInterval <= 0 - exiting");
                }
                return;
            }

            JobDetail jobDetail = new JobDetail(RequestManagement.SCHEDULE_RUNGC_JOB_NAME, Scheduler.DEFAULT_GROUP, ScheduleRunGCJob.class);
            // Trigger trigger = TriggerUtils.makeMinutelyTrigger(queueConfig.getLowPriorityWaiting());
            // trigger.setName(SCHEDULE_PRIORITY_TRIGGER_NAME);
            Trigger trigger = TriggerUtils.makeMinutelyTrigger(RequestManagement.SCHEDULE_RUNGC_TRIGGER_NAME,
                                                               runGCInterval,
                                                               SimpleTrigger.REPEAT_INDEFINITELY);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, runGCInterval);
            trigger.setStartTime(cal.getTime());

            scheduler.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            LOG.error("createAndScheduleRunGCJob()", e);

            throw new MotuException("ERROR in RequestManagement.createAndScheduleRunGCJob.", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("createAndScheduleRunGCJob() - exiting");
        }
    }

    // public static String getFilePatternKeymap() {
    // return RequestManagement.FILE_PATTERN_KEYMAP;
    // }

    /**
     * Gets the last request id.
     * 
     * @return the last request id
     */
    public long getLastRequestId() {
        return lastRequestId;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Gets the queue server management.
     * 
     * @return the queue server management
     */
    public QueueServerManagement getQueueServerManagement() {
        return queueServerManagement;
    }

    /**
     * Gets the resquest status map.
     * 
     * @return the resquest status map
     */
    @Override
    public ConcurrentMap<Long, StatusModeResponse> getResquestStatusMap() {
        return requestStatusMap;
    }

    /**
     * Gets the resquest status map.
     * 
     * @param key the key
     * 
     * @return the resquest status map
     */
    @Override
    public StatusModeResponse getResquestStatusMap(Long key) {
        return requestStatusMap.get(key);
    }

    /**
     * Gets the resquest status map key.
     * 
     * @return the resquest status map key
     */
    @Override
    public Set<Long> getResquestStatusMapKey() {

        return requestStatusMap.keySet();
    }

    /**
     * Checks if is resquest status map map empty.
     * 
     * @return true, if is resquest status map map empty
     */
    public boolean isRequestStatusMapEmpty() {
        return requestStatusMap.isEmpty();
    }

    /**
     * Job execution vetoed.
     * 
     * @param context the context
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("RequestManagement.jobExecutionVetoed(JobExecutionContext) - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("RequestManagement.jobExecutionVetoed(JobExecutionContext) - exiting");
        }
    }

    /**
     * Job to be executed.
     * 
     * @param context the context
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("RequestManagement.jobToBeExecuted(JobExecutionContext) - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("RequestManagement.jobToBeExecuted(JobExecutionContext) - exiting");
        }
    }

    /**
     * Job was executed.
     * 
     * @param context the context
     * @param jobException the job exception
     */
    @Override
    @SuppressWarnings("unchecked")
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("RequestManagement.jobWasExecuted(JobExecutionContext, JobExecutionException) - entering");
        }

        if (jobException != null) {
            LOG.error(jobException.getMessage(), jobException);
            reportJobErrors((List<Exception>) context.get(ScheduleCleanJob.ERRORS_KEY_MAP));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("RequestManagement.jobWasExecuted(JobExecutionContext, JobExecutionException) - exiting");
        }
    }

    /**
     * Put if absent resquest status map.
     * 
     * @param value the value
     * @param key the key
     * 
     * @return the status mode response
     */
    public StatusModeResponse putIfAbsentRequestStatusMap(Long key, StatusModeResponse value) {

        this.registerJmxMbeans(value);

        return requestStatusMap.putIfAbsent(key, value);
    }

    // /**
    // * Put resquest status map.
    // *
    // * @param value the value
    // * @param key the key
    // *
    // * @return the status mode response
    // */
    // public StatusModeResponse putRequestStatusMap(Long key, StatusModeResponse value) {
    // return requestStatusMap.put(key, value);
    // }

    /**
     * Removes the resquest status map.
     * 
     * @param key the key
     * 
     * @return the status mode response
     */
    public StatusModeResponse removeRequestStatusMap(Long key) {
        return requestStatusMap.remove(key);
    }

    // /**
    // * Replace resquest status map.
    // *
    // * @param value the value
    // * @param key the key
    // *
    // * @return the status mode response
    // */
    // public StatusModeResponse replaceRequestStatusMap(Long key, StatusModeResponse value) {
    // return requestStatusMap.replace(key, value);
    // }

    /**
     * Resquest status map contains key.
     * 
     * @param key the key
     * 
     * @return true, if resquest status map contains key
     */
    public boolean requestStatusMapContainsKey(Long key) {
        return requestStatusMap.containsKey(key);
    }

    /**
     * Resquest status map key set.
     * 
     * @return the set< long>
     */
    public Set<Long> requestStatusMapKeySet() {
        return requestStatusMap.keySet();
    }

    /**
     * Resquest status map size.
     * 
     * @return the int
     */
    public int requestStatusMapSize() {
        return requestStatusMap.size();
    }

    /**
     * Checks if is shutdown.
     * 
     * @return true, if checks if is shutdown
     */
    public boolean isShutdown() {

        if (scheduler == null) {
            return true;
        }
        try {
            return scheduler.isShutdown();
        } catch (SchedulerException e) {
            return true;
        }

    }

    /**
     * Shutdown.
     * 
     * @throws MotuException the motu exception
     */
    public void shutdown() throws MotuException {

        try {
            Thread.sleep(500);
            if (LOG.isInfoEnabled()) {
                LOG.info("RequestManagement shutdown() - Shutdown scheduler in progress....");
            }
            if (scheduler != null) {
                if (!scheduler.isShutdown()) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("RequestManagement shutdown() - scheduler is shutting down...");
                    }
                    scheduler.shutdown(true);
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("RequestManagement shutdown() - scheduler shutdown: %b", scheduler.isShutdown()));
                }
            }

        } catch (InterruptedException e) {
            throw new MotuException("ERROR in RequestManagement.shutdown.", e);
        } catch (SchedulerException e) {
            throw new MotuException("ERROR in RequestManagement.shutdown.", e);
        }

        try {
            Thread.sleep(500);
            if (RequestManagement.useQueueServer) {
                if (queueServerManagement != null) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("RequestManagement shutdown() - queueServerManagement is shutting down...");
                    }
                    queueServerManagement.shutdown();
                    if (LOG.isInfoEnabled()) {
                        LOG.info("RequestManagement shutdown() - queueServerManagement is shutdown.");
                    }
                }
            }
        } catch (InterruptedException e) {
            // Do nothing
        }

    }

    /**
     * Finalize.
     */
    @Override
    protected void finalize() {

        try {
            // Invokes <tt>shutdown</tt> when no longer referenced
            shutdown();
        } catch (MotuException e) {
            // Do Nothing
        }
        try {
            super.finalize();
        } catch (Throwable e) {
            // Do nothing
        }
    }

    /**
     * Init.
     * 
     * @throws MotuException the motu exception
     */
    protected void init() throws MotuException {

        createScheduler();
        createAndScheduleCleanJob();
        initQueueServer();

        createAndScheduleRunGCJob();

    }

    /**
     * Creates the scheduler.
     * 
     * @throws MotuException the motu exception
     */
    private void createScheduler() throws MotuException {
        SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
        try {
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            throw new MotuException("ERROR in RequestManagement.createScheduler.", e);
        }

    }

    /**
     * Inits the queue server.
     * 
     * @throws MotuException the motu exception
     */
    private void initQueueServer() throws MotuException {

        if (!RequestManagement.useQueueServer) {
            return;
        }

        try {
            queueServerManagement = QueueServerManagement.getInstance();
            queueServerManagement.setScheduler(this.scheduler);
        } catch (MotuException e) {
            if (queueServerManagement != null) {
                try {
                    queueServerManagement.shutdown();
                } catch (MotuException e1) {
                    // Do nothing
                }
                throw new MotuException(String.format("ERROR while queue server initialization.\n%s", e.notifyException()), e);
            }
            return;
        }

    }

    /**
     * Report job errors.
     * 
     * @param errors the errors
     */
    private void reportJobErrors(List<Exception> errors) {

        if (errors == null) {
            return;
        }

        for (Exception e : errors) {
            if (e instanceof MotuExceptionBase) {
                MotuExceptionBase motuExceptionBase = (MotuExceptionBase) e;
                LOG.error(motuExceptionBase.notifyException(), motuExceptionBase);
            } else {
                LOG.error(e.getMessage(), e);

            }
        }

    }

    /**
     * Gets the status mode response object name.
     * 
     * @param statusModeResponse the status mode response
     * @return the status mode response object name
     */
    public static ObjectName getStatusModeResponseObjectName(StatusModeResponse statusModeResponse) {
        try {
            return new ObjectName(
                    MessageFormat.format(OBJECT_NAME_PATTERN, statusModeResponse.getUserId(), statusModeResponse.getRequestId().toString()));
        } catch (Exception e) {
            // JMX supervision should never alters Motu behaviour, so we don't let exception propagation
            LOG.error("Failed to create ObjectName for managed beans (Motu will still continue to start)", e);
        }
        return null;

    }

    /**
     * Registers to the MBean platform the managed beans like the status of the request.
     */
    public void registerJmxMbeans(StatusModeResponse statusModeResponse) {
        // try {
        // // works well in tomcat
        // final MBeanServer platform = ManagementFactory.getPlatformMBeanServer();
        // ObjectName name = new ObjectName(MessageFormat.format(OBJECT_NAME_PATTERN, "EndedRequests",
        // statusModeResponse.getRequestId()));
        // platform.registerMBean(statusModeResponse, name);
        //
        // } catch (Exception e) {
        // // JMX supervision should never alters Motu behaviour, so we don't let exception propagation
        // LOG.error("Failed to register managed beans (Motu will still continue to start)", e);
        // }

        // try {
        // registerModelMBean(createRawModelMBean(statusModeResponse),
        // MessageFormat.format(OBJECT_NAME_PATTERN, "EndedRequests",
        // statusModeResponse.getRequestId().toString()));
        // } catch (Exception e) {
        // // JMX supervision should never alters Motu behaviour, so we don't let exception propagation
        // LOG.error("Failed to register managed beans (Motu will still continue to start)", e);
        // }
        // try {
        // registerModelMBean(makeModelMBean(statusModeResponse), MessageFormat.format(OBJECT_NAME_PATTERN,
        // "EndedRequests", statusModeResponse.getRequestId().toString()));
        // } catch (Exception e) {
        // // JMX supervision should never alters Motu behaviour, so we don't let exception propagation
        // LOG.error("Failed to register managed beans (Motu will still continue to start)", e);
        // }

        // try {
        // //Memory memory = new Memory();
        // // List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        // // for (MemoryPoolMXBean pool : pools) {
        // // memoryUsage = pool.getPeakUsage();
        // // }
        //
        // // MXBeanMapper mxBeanMapper = new MXBeanMapper(statusModeResponse.getClass());
        // // MXBeanMapper mxBeanMapper = new MXBeanMapper(memoryUsage.getClass());
        // //
        // // registerMBean(mxBeanMapper.mxbean, MessageFormat.format(OBJECT_NAME_PATTERN, "EndedRequests",
        // // statusModeResponse.getRequestId().toString()));
        // MBeanUtils.registerMBean(statusModeResponse,
        // RequestManagement.getStatusModeResponseObjectName(statusModeResponse));
        // } catch (Exception e) {
        // // JMX supervision should never alters Motu behaviour, so we don't let exception propagation
        // LOG.error("Failed to register managed beans (Motu will still continue to start)", e);
        // }

        MBeanUtils.registerMBean(statusModeResponse, RequestManagement.getStatusModeResponseObjectName(statusModeResponse));

    }

    /**
     * Unregisters to the MBean platform the managed beans like the status of the request.
     */
    public void unregisterJmxMbeansStatusModeResponse(List<Long> requestIdToDelete) {

        for (Long requestId : requestIdToDelete) {
            StatusModeResponse statusModeResponse = requestStatusMap.get(requestId);
            MBeanUtils.unregisterMBean(RequestManagement.getStatusModeResponseObjectName(statusModeResponse));
        }

    }

}
