package fr.cls.atoll.motu.queueserver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import fr.cls.atoll.motu.configuration.QueueType;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:26 $
 */
public class SchedulePriorityJob implements StatefulJob {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(SchedulePriorityJob.class);

    /** The Constant ERRORS_KEY_MAP. */
    public static final String ERRORS_KEY_MAP = "Errors";

    /**
     * Constructor.
     */
    public SchedulePriorityJob() {
    }

    /**
     * {@inheritDoc}.
     * 
     * @param context the context
     * 
     * @throws JobExecutionException the job execution exception
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("SchedulePriorityJob.execute(JobExecutionContext) - entering");
        }

        boolean loop = true;
        
        while (loop) {
            loop = checkPriorityTimeOut(context);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("SchedulePriorityJob.execute(JobExecutionContext) - exiting");
        }
    }

    /**
     * Gets the thread pool executor.
     * 
     * @param context the context
     * 
     * @return the thread pool executor
     */
    private ExtractionThreadPoolExecutor getThreadPoolExecutor(JobExecutionContext context) {

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        return ExtractionThreadPoolExecutor.class.cast(jobDataMap.get(ExtractionThreadPoolExecutor.class.getSimpleName()));

    }

    /**
     * Gets the queue config.
     * 
     * @param context the context
     * 
     * @return the queue config
     */
    private QueueType getQueueConfig(JobExecutionContext context) {

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        return QueueType.class.cast(jobDataMap.get(QueueType.class.getSimpleName()));

    }
    
    /**
     * Check priority time out.
     * 
     * @param context the context
     * 
     * @return true, if check priority time out
     * 
     * @throws JobExecutionException the job execution exception
     */
    public boolean checkPriorityTimeOut(JobExecutionContext context) throws JobExecutionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkPriorityTimeOut(JobExecutionContext) - entering");
        }

        ExtractionThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor(context);

        if (threadPoolExecutor == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("checkPriorityTimeOut(JobExecutionContext) - threadPoolExecutor is null - exiting");
            }
            return false;
        }

        QueueType queueConfig = getQueueConfig(context);
        if (queueConfig == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("checkPriorityTimeOut(JobExecutionContext) - queueConfig is null - exiting");
            }
            return false;
        }
        PriorityBlockingQueue<Runnable> priorityBlockingQueue = (PriorityBlockingQueue<Runnable>) threadPoolExecutor.getQueue();


        Map<RunnableExtraction, Exception> listRunnableError = new HashMap<RunnableExtraction, Exception>();
        boolean removed = false;
        
        for (Runnable runnable : priorityBlockingQueue) {
            if (!(runnable instanceof RunnableExtraction)) {
                //-------------
                continue;
                //-------------
            }

            
            RunnableExtraction runnableExtraction = (RunnableExtraction) runnable;

            // Not out of time or runnable has higher priority
            if (!(runnableExtraction.isOutOfTime(queueConfig.getLowPriorityWaiting()))) {
                //-------------
                continue;
                //-------------
            }

            removed = threadPoolExecutor.remove(runnableExtraction);
            if (!removed) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("checkPriorityTimeOut(JobExecutionContext) - unable to remove runnable : priority: %d,  range %d'", runnableExtraction
                            .getPriority(), runnableExtraction.getRange()));
                }
                //-------------
                continue;
                //-------------
            }
 
            try {
                // MotuInvalidQueuePriorityException ee = new MotuInvalidQueuePriorityException(1, 2, 3);
                // listRunnableError.put(runnableExtraction, ee);
                runnableExtraction.increasePriority();
                
                threadPoolExecutor.incrementPriorityMap(runnableExtraction);
                
                threadPoolExecutor.execute(runnableExtraction);
                
            } catch (Exception e) {
                LOG.error("checkPriorityTimeOut(JobExecutionContext)", e);

                listRunnableError.put(runnableExtraction, e);
            }
            
            // one is removed --> break to keep consitent queue list 'for loop' 
            //-------------
            break;
            //-------------

        }

        if (listRunnableError.size() > 0) {
            context.put(SchedulePriorityJob.ERRORS_KEY_MAP, listRunnableError);
            throw new JobExecutionException(
                    String
                            .format("ERROR in SchedulePriorityJob.checkPriorityTimeOut : %d error(s) - Gets and inspects 'Map<RunnableExtraction, Exception>' object (context data map key is '%s'",
                                    listRunnableError.size(),
                                    SchedulePriorityJob.ERRORS_KEY_MAP));
        }
        
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkPriorityTimeOut(JobExecutionContext) - exiting");
        }

        return removed;
    }

//    /**
//     * Adjust max range by priority.
//     * 
//     * @param context the context
//     */
//    public void adjustMaxRangeByPriority(JobExecutionContext context) {
//        
//        ExtractionThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor(context);
//
//        if (threadPoolExecutor == null) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("checkPriorityTimeOut(JobExecutionContext) - threadPoolExecutor is null - exiting");
//            }
//            return;
//        }
//
//        @SuppressWarnings("unused")
//        PriorityBlockingQueue<Runnable> priorityBlockingQueue = (PriorityBlockingQueue<Runnable>) threadPoolExecutor.getQueue();
//
//        Map<Integer, Integer> priorityMapWork = new HashMap<Integer, Integer>();
//        
//        for (Runnable runnable : priorityBlockingQueue) {
//            if (!(runnable instanceof RunnableExtraction)) {
//                //-------------
//                continue;
//                //-------------
//            }
//            
//            RunnableExtraction runnableExtraction = (RunnableExtraction) runnable;
//            Integer priority = runnableExtraction.getPriority();
//            Integer range = runnableExtraction.getRange();
//            Integer maxRange = null;
//            
//            if (priorityMapWork.containsKey(priority)) {
//                maxRange = ((priorityMapWork.get(priority) > range) ? priorityMapWork.get(priority) : range);
//            } else {
//                maxRange = range;
//            }
//            
//            priorityMapWork.put(priority, maxRange);                
//            
//        }
//        
//        threadPoolExecutor.adjustPriorityMap(priorityMapWork);
//                
//    }
}
