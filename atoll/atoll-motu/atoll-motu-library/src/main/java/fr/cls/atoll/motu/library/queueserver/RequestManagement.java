package fr.cls.atoll.motu.library.queueserver;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
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

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */
public class RequestManagement implements JobListener {

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
    private ConcurrentMap<Long, StatusModeResponse> requestStatusMap = new ConcurrentHashMap<Long, StatusModeResponse>();

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
            jobDataMap.put(getStatusModeResponseKeymap(), requestStatusMap);

            jobDataMap.put(RequestManagement.FILE_PATTERN_KEYMAP, Organizer.getMotuConfigInstance().getExtractionFilePatterns());
            jobDataMap.put(RequestManagement.DIR_TO_SCAN_KEYMAP, Organizer.getMotuConfigInstance().getExtractionPath());

            jobDetail.setJobDataMap(jobDataMap);

            // Trigger trigger = TriggerUtils.makeMinutelyTrigger(queueConfig.getLowPriorityWaiting());
            // trigger.setName(SCHEDULE_PRIORITY_TRIGGER_NAME);
            Trigger trigger = TriggerUtils.makeMinutelyTrigger(RequestManagement.SCHEDULE_CLEAN_TRIGGER_NAME, Organizer.getMotuConfigInstance()
                    .getRunCleanInterval(), SimpleTrigger.REPEAT_INDEFINITELY);
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
     * Generate request id.
     * 
     * @return the long
     */
    public long generateRequestId() {

        // Calcul d'un numéro de requête à partir du temps
        synchronized (this) {
            long num = Calendar.getInstance().getTimeInMillis();
            if (num == lastRequestId) {
                // Si c'est le même temps que le précédent on incrément pour en avoir un différent
                lastRequestId++;
            } else {
                lastRequestId = num;
            }
        }
        return lastRequestId;
    }

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
     * @param key the key
     * 
     * @return the resquest status map
     */
    public StatusModeResponse getResquestStatusMap(Long key) {
        return requestStatusMap.get(key);
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
        return requestStatusMap.putIfAbsent(key, value);
    }

    /**
     * Put resquest status map.
     * 
     * @param value the value
     * @param key the key
     * 
     * @return the status mode response
     */
    public StatusModeResponse putRequestStatusMap(Long key, StatusModeResponse value) {
        return requestStatusMap.put(key, value);
    }

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

    /**
     * Replace resquest status map.
     * 
     * @param value the value
     * @param key the key
     * 
     * @return the status mode response
     */
    public StatusModeResponse replaceRequestStatusMap(Long key, StatusModeResponse value) {
        return requestStatusMap.replace(key, value);
    }

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
     * Shutdown.
     * 
     * @throws MotuException the motu exception
     */
    public void shutdown() throws MotuException {

        try {
            Thread.sleep(500);
            if (LOG.isDebugEnabled()) {
                LOG.debug("RequestManagement shutdown() - Shutdown scheduler in progress....");
            }
            if (scheduler != null) {
                if (!scheduler.isShutdown()) {
                    scheduler.shutdown(true);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("RequestManagement shutdown() - scheduler shutdown: %b", scheduler.isShutdown()));
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
                    queueServerManagement.shutdown();
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

}
