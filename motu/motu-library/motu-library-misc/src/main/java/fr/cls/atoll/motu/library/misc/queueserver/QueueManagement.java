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

import fr.cls.atoll.motu.library.misc.configuration.QueueType;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingQueueCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidQueuePriorityException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class QueueManagement implements JobListener, QueueManagementMBean {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(QueueManagement.class);

    // /** The count high priority. */
    // private static int countHighPriority = 0;
    //
    // /** The count low priority. */
    // private static int countLowPriority = 0;

    /** The priority blocking queue. */
    private PriorityBlockingQueue<Runnable> priorityBlockingQueue = null;

    /** The thread pool executor. */
    private ExtractionThreadPoolExecutor threadPoolExecutor = null;

    /** The queue config. */
    private QueueType queueConfig = null;

    /**
     * Gets the queue config.
     * 
     * @return the queue config
     */
    public QueueType getQueueConfig() {
        return queueConfig;
    }

    /**
     * The Constructor.
     * 
     * @param queueConfig the queue config
     * 
     * @throws MotuException the motu exception
     */
    public QueueManagement(QueueType queueConfig) throws MotuException {

        this.queueConfig = queueConfig;
        this.priorityBlockingQueue = new PriorityBlockingQueue<Runnable>();

        createThreadPoolExecutor();

        // createScheduler();

        // synchronized (this) {
        // if (priority == 1) {
        // countHighPriority++;
        // range = countHighPriority;
        // }
        // if (priority == 2) {
        // countLowPriority++;
        // range = countLowPriority;
        // }
        //
        // }

    }

    /**
     * Creates the thread pool executor.
     */
    private void createThreadPoolExecutor() {
        int maxRunningThreads = this.getMaxThreads();
        this.threadPoolExecutor = new ExtractionThreadPoolExecutor(this.getId(), maxRunningThreads, maxRunningThreads, 0L, TimeUnit.SECONDS, priorityBlockingQueue);

    }

    /**
     * Gets the schedule job name.
     * 
     * @return the schedule job name
     */
    @Override
    public String getScheduleJobName() {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(QueueServerManagement.SCHEDULE_PRIORITY_JOB_NAME);
        stringBuffer.append(getName());

        return stringBuffer.toString();
    }

    /**
     * Gets the schedule trigger name.
     * 
     * @return the schedule trigger name
     */
    @Override
    public String getScheduleTriggerName() {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(QueueServerManagement.SCHEDULE_PRIORITY_TRIGGER_NAME);
        stringBuffer.append(getName());

        return stringBuffer.toString();
    }

    /**
     * Creates the and schedule job.
     * 
     * @param scheduler the scheduler
     * 
     * @throws MotuException the motu exception
     */
    public void createAndScheduleJob(Scheduler scheduler) throws MotuException {
        try {
            JobDetail jobDetail = new JobDetail(getScheduleJobName(), Scheduler.DEFAULT_GROUP, SchedulePriorityJob.class);

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(ExtractionThreadPoolExecutor.class.getSimpleName(), threadPoolExecutor);
            jobDataMap.put(QueueType.class.getSimpleName(), queueConfig);

            jobDetail.setJobDataMap(jobDataMap);

            // Trigger trigger = TriggerUtils.makeMinutelyTrigger(queueConfig.getLowPriorityWaiting());
            // trigger.setName(SCHEDULE_PRIORITY_TRIGGER_NAME);
            Trigger trigger = TriggerUtils.makeMinutelyTrigger(getScheduleTriggerName(),
                                                               queueConfig.getLowPriorityWaiting(),
                                                               SimpleTrigger.REPEAT_INDEFINITELY);
            trigger.setStartTime(TriggerUtils.getNextGivenMinuteDate(null, queueConfig.getLowPriorityWaiting()));
            // Trigger trigger = TriggerUtils.makeSecondlyTrigger(getScheduleTriggerName(), 2,
            // SimpleTrigger.REPEAT_INDEFINITELY );
            // trigger.setStartTime(TriggerUtils.getNextGivenSecondDate(null, 5));

            // LOG.info(trigger.getStartTime());

            scheduler.addJobListener(this);

            // make sure the listener is associated with the job
            jobDetail.addJobListener(getName());

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new MotuException("ERROR in QueueManagement.createScheduler.", e);
        }

    }

    // /**
    // * Creates the scheduler.
    // *
    // * @throws MotuException the motu exception
    // */
    // private void createScheduler() throws MotuException {
    // SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
    // try {
    // schedule = schedulerFactory.getScheduler();
    //
    // // JobDetail jobDetail = new JobDetail("myJob",
    // // Scheduler.DEFAULT_GROUP,
    // // TestSchedule.CheckPriorityJob.class);
    // jobDetail = new JobDetail(SCHEDULE_PRIORITY_JOB_NAME, Scheduler.DEFAULT_GROUP,
    // SchedulePriorityJob.class);
    //            
    // JobDataMap jobDataMap = new JobDataMap();
    // jobDataMap.put(ExtractionThreadPoolExecutor.class.getSimpleName(), threadPoolExecutor);
    // jobDataMap.put(QueueType.class.getSimpleName(), queueConfig);
    //
    // jobDetail.setJobDataMap(jobDataMap);
    //
    // // Trigger trigger = TriggerUtils.makeMinutelyTrigger(queueConfig.getLowPriorityWaiting());
    // // trigger.setName(SCHEDULE_PRIORITY_TRIGGER_NAME);
    // Trigger trigger = TriggerUtils.makeMinutelyTrigger(SCHEDULE_PRIORITY_TRIGGER_NAME);
    //
    // scheduler.addJobListener(this);
    //
    // // make sure the listener is associated with the job
    // jobDetail.addJobListener(this.getName());
    //
    // scheduler.scheduleJob(jobDetail, trigger);
    //
    // } catch (SchedulerException e) {
    // throw new MotuException("ERROR in QueueManagement.createScheduler.", e);
    // }
    //
    // }

    /**
     * Execute.
     * 
     * @param runnableExtraction the runnable extraction
     * 
     * @throws MotuException the motu exception
     * @throws MotuExceedingQueueCapacityException the motu exceeding queue capacity exception
     * @throws MotuInvalidQueuePriorityException the motu invalid queue priority exception
     */
    public void execute(RunnableExtraction runnableExtraction) throws MotuExceedingQueueCapacityException, MotuException,
            MotuInvalidQueuePriorityException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("execute(RunnableExtraction) - entering");
        }

        if (isMaxQueueSizeReached()) {
            MotuExceedingQueueCapacityException e = new MotuExceedingQueueCapacityException(priorityBlockingQueue.size());
            throw e;
        }

        setRequestRange(runnableExtraction);

        setQueueGlobalInfo(runnableExtraction);

        try {
            threadPoolExecutor.execute(runnableExtraction);
            setInQueue(runnableExtraction);
        } catch (RejectedExecutionException e) {
            throw new MotuException("ERROR Execute request", e);

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("execute(RunnableExtraction) - exiting");
        }
    }

    /**
     * Sets the in queue time.
     * 
     * @param runnableExtraction the runnable extraction
     */
    private void setInQueue(RunnableExtraction runnableExtraction) {
        runnableExtraction.setInQueue();
        threadPoolExecutor.incrementUser(runnableExtraction);

    }

    /**
     * Sets the queue global info.
     * 
     * @param runnableExtraction the runnable extraction
     */
    private void setQueueGlobalInfo(RunnableExtraction runnableExtraction) {

        runnableExtraction.setQueueId(queueConfig.getId());
        runnableExtraction.setQueueDesc(queueConfig.getDescription());

    }

    /**
     * Sets the request range.
     * 
     * @param runnableExtraction the runnable extraction
     * 
     * @throws MotuInvalidQueuePriorityException the motu invalid queue priority exception
     */
    public void setRequestRange(RunnableExtraction runnableExtraction) throws MotuInvalidQueuePriorityException {
        threadPoolExecutor.incrementPriorityMap(runnableExtraction);
    }

    // /**
    // * Gets the last request range value.
    // *
    // * @param priority the priority
    // *
    // * @return the last request range value
    // *
    // * @throws MotuInvalidQueuePriorityException the motu invalid queue priority exception
    // */
    // public synchronized Integer getLastRequestRangeValue(int priority) throws
    // MotuInvalidQueuePriorityException {
    //
    // QueueManagement.checkPriority(priority);
    // return listCountPriority.get(priority);
    // }

    // /**
    // * Sets the last request range value.
    // *
    // * @param priority the priority
    // * @param value the value
    // *
    // * @throws MotuInvalidQueuePriorityException the motu invalid queue priority exception
    // */
    // public synchronized void setLastRequestRangeValue(int priority, Integer value) throws
    // MotuInvalidQueuePriorityException {
    //
    // QueueManagement.checkPriority(priority);
    // listCountPriority.set(priority, value);
    // }

    /**
     * Checks if is max queue size reached.
     * 
     * @return true, if is max queue size reached
     */
    @Override
    public Boolean isMaxQueueSizeReached() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMaxQueueSizeReached() - entering");
        }

        int maxQueueSize = getMaxPoolSize();
        int queueSize = priorityBlockingQueue.size();
        boolean reached = false;

        if ((maxQueueSize > 0)) {
            reached = (queueSize >= maxQueueSize);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("isMaxQueueSizeReached() - exiting");
        }

        return reached;
    }

    /**
     * Gets the max pool size.
     * 
     * @return the max pool size
     */
    @Override
    public Short getMaxPoolSize() {
        if (queueConfig.getMaxPoolSize() == null) {
            return -1;
        } else {
            return queueConfig.getMaxPoolSize();
        }
    }

    /**
     * Gets the max threads.
     * 
     * @return the max threads
     */
    @Override
    public Integer getMaxThreads() {
        if (queueConfig.getMaxThreads() == null) {
            return 1;
        } else {
            return queueConfig.getMaxThreads();
        }
    }

    /**
     * @return the identifier of the queue.
     */
    @Override
    public String getId() {
        return queueConfig.getId();
    }

    /**
     * Count request user.
     * 
     * @param userId the user id
     * 
     * @return the int
     */
    public int countRequestUser(String userId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("countRequestUser(String) - entering");
        }

        int count = 0;
        if (threadPoolExecutor.usersContainsKey(userId)) {
            count = threadPoolExecutor.getUsers(userId).intValue();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("countRequestUser(String) - exiting");
        }
        return count;

    }

    /**
     * Shutdown.
     * 
     * @throws MotuException the motu exception
     */
    public void shutdown() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("QueueManagement shutdown() - Queue description: '%s' - Is terminated %b - entering", this.queueConfig
                    .getDescription(), threadPoolExecutor.isTerminated()));
        }

        try {

            Thread.sleep(500);
            threadPoolExecutor.shutdown();

            while (!(threadPoolExecutor.isTerminated())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("QueueManagement awaitTermination - %s queue size is %d", getName(), priorityBlockingQueue.size()));
                }
                threadPoolExecutor.awaitTermination(1, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            LOG.error("shutdown()", e);
            throw new MotuException("ERROR in QueueManagement.shutdown.", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("QueueManagement shutdown() - Queue description: '%s' - Is terminated %b - exiting", this.queueConfig
                    .getDescription(), threadPoolExecutor.isTerminated()));
        }
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return queueConfig.getId();
    }

    /**
     * Job execution vetoed.
     * 
     * @param context the context
     */
    public void jobExecutionVetoed(JobExecutionContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("QueueManagement.jobExecutionVetoed(JobExecutionContext) - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("QueueManagement.jobExecutionVetoed(JobExecutionContext) - exiting");
        }
    }

    /**
     * Job to be executed.
     * 
     * @param context the context
     */
    public void jobToBeExecuted(JobExecutionContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("QueueManagement.jobToBeExecuted(JobExecutionContext) - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("QueueManagement.jobToBeExecuted(JobExecutionContext) - exiting");
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
            LOG.debug(String.format("'%s' - QueueManagement.jobWasExecuted(JobExecutionContext, JobExecutionException) - entering", this.getName()));
        }
        if (jobException != null) {
            LOG.error(jobException.getMessage(), jobException);
            reportJobErrors((Map<RunnableExtraction, Exception>) context.get(SchedulePriorityJob.ERRORS_KEY_MAP));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("'%s' - QueueManagement.jobWasExecuted(JobExecutionContext, JobExecutionException) - exiting", this.getName()));
        }
    }

    /**
     * Report job errors.
     * 
     * @param errors the errors
     */
    private void reportJobErrors(Map<RunnableExtraction, Exception> errors) {

        if (errors == null) {
            return;
        }

        Set<RunnableExtraction> set = errors.keySet();
        if (set == null) {
            return;
        }

        for (RunnableExtraction runnableExtraction : set) {
            Exception e = errors.get(runnableExtraction);
            if (e == null) {
                continue;
            }
            threadPoolExecutor.decrementUser(runnableExtraction);
            threadPoolExecutor.adjustPriorityMap(runnableExtraction);

            runnableExtraction.setError(e);
            runnableExtraction.setEnded();

        }

    }

    /**
     * Gets the priority blocking queue.
     * 
     * @return the priority blocking queue
     */
    public PriorityBlockingQueue<Runnable> getPriorityBlockingQueue() {
        return priorityBlockingQueue;
    }

    /**
     * Gets the thread pool executor.
     * 
     * @return the thread pool executor
     */
    public ExtractionThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }
}
