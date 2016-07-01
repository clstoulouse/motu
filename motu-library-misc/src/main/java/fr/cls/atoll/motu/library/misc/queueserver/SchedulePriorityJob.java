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
import fr.cls.atoll.motu.web.bll.request.queueserver.RunnableExtraction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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
                // -------------
                continue;
                // -------------
            }

            RunnableExtraction runnableExtraction = (RunnableExtraction) runnable;

            // Not out of time or runnable has higher priority
            if (!(runnableExtraction.isOutOfTime(queueConfig.getLowPriorityWaiting()))) {
                // -------------
                continue;
                // -------------
            }

            removed = threadPoolExecutor.remove(runnableExtraction);
            if (!removed) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("checkPriorityTimeOut(JobExecutionContext) - unable to remove runnable : priority: %d,  range %d'",
                                            runnableExtraction.getPriority(),
                                            runnableExtraction.getRange()));
                }
                // -------------
                continue;
                // -------------
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
            // -------------
            break;
            // -------------

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

    // /**
    // * Adjust max range by priority.
    // *
    // * @param context the context
    // */
    // public void adjustMaxRangeByPriority(JobExecutionContext context) {
    //        
    // ExtractionThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor(context);
    //
    // if (threadPoolExecutor == null) {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("checkPriorityTimeOut(JobExecutionContext) - threadPoolExecutor is null - exiting");
    // }
    // return;
    // }
    //
    // @SuppressWarnings("unused")
    // PriorityBlockingQueue<Runnable> priorityBlockingQueue = (PriorityBlockingQueue<Runnable>)
    // threadPoolExecutor.getQueue();
    //
    // Map<Integer, Integer> priorityMapWork = new HashMap<Integer, Integer>();
    //        
    // for (Runnable runnable : priorityBlockingQueue) {
    // if (!(runnable instanceof RunnableExtraction)) {
    // //-------------
    // continue;
    // //-------------
    // }
    //            
    // RunnableExtraction runnableExtraction = (RunnableExtraction) runnable;
    // Integer priority = runnableExtraction.getPriority();
    // Integer range = runnableExtraction.getRange();
    // Integer maxRange = null;
    //            
    // if (priorityMapWork.containsKey(priority)) {
    // maxRange = ((priorityMapWork.get(priority) > range) ? priorityMapWork.get(priority) : range);
    // } else {
    // maxRange = range;
    // }
    //            
    // priorityMapWork.put(priority, maxRange);
    //            
    // }
    //        
    // threadPoolExecutor.adjustPriorityMap(priorityMapWork);
    //                
    // }
}
