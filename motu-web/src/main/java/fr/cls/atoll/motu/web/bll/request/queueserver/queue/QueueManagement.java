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
package fr.cls.atoll.motu.web.bll.request.queueserver.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.QueueType;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class QueueManagement {

    /** Logger for this class. */
    private static final Logger LOG = LogManager.getLogger();

    /** The priority blocking queue. */
    private PriorityBlockingQueue<Runnable> priorityBlockingQueue = null;

    /** The thread pool executor. */
    private ExtractionThreadPoolExecutor threadPoolExecutor = null;

    /** The queue config. */
    private QueueType queueConfig = null;

    /**
     * The Constructor.
     * 
     * @param queueConfig the queue config
     * 
     * @throws MotuException the motu exception
     */
    public QueueManagement(QueueType queueConfig) {
        this.queueConfig = queueConfig;
        this.priorityBlockingQueue = new PriorityBlockingQueue<Runnable>();

        int maxRunningThreads = this.getMaxThreads();
        this.threadPoolExecutor = new ExtractionThreadPoolExecutor(
                getQueueConfig().getId(),
                maxRunningThreads,
                maxRunningThreads,
                0L,
                TimeUnit.SECONDS,
                priorityBlockingQueue);
    }

    /**
     * Gets the queue config.
     * 
     * @return the queue config
     */
    public QueueType getQueueConfig() {
        return queueConfig;
    }

    private void checkMaxQueueSize() throws MotuException {
        if (isMaxQueueSizeReached()) {
            throw new MotuException(ErrorType.EXCEEDING_QUEUE_DATA_CAPACITY, "Max queue size limit reached: " + priorityBlockingQueue.size());
        }
    }

    /**
     * Execute.
     * 
     * @param runnableExtraction the runnable extraction
     * 
     * @throws MotuException the motu exception
     */
    public void execute(IQueueJob runnableExtraction) throws MotuException {
        // If queue is full, throws new MotuExceedingQueueCapacityException
        checkMaxQueueSize();

        try {
            threadPoolExecutor.onNewRequestForUser(runnableExtraction.getExtractionParameters().isAnonymousUser() ? null
                    : runnableExtraction.getExtractionParameters().getUserId());
            threadPoolExecutor.execute(runnableExtraction);
        } catch (RejectedExecutionException e) {
            throw new MotuException(ErrorType.SYSTEM, "ERROR Execute request", e);

        }
    }

    /**
     * Checks if is max queue size reached.
     * 
     * @return true, if is max queue size reached
     */
    private Boolean isMaxQueueSizeReached() {
        int maxQueueSize = getMaxPoolSize();
        return maxQueueSize > 0 && priorityBlockingQueue.size() >= maxQueueSize;
    }

    /**
     * Gets the max pool size.
     * 
     * @return the max pool size
     */
    public Short getMaxPoolSize() {
        return queueConfig.getMaxPoolSize() == null ? -1 : queueConfig.getMaxPoolSize();
    }

    /**
     * Gets the max threads.
     * 
     * @return the max threads
     */
    public Integer getMaxThreads() {
        return queueConfig.getMaxThreads() == null ? 1 : queueConfig.getMaxThreads();
    }

    /**
     * @return the data threshold of the queue.
     */
    public float getDataThreshold() {
        return queueConfig.getDataThreshold();
    }

    /**
     * @return the batch flag of the queue
     */
    public boolean getBatch() {
        return queueConfig.getBatch();
    }

    /**
     * @return the low priority waiting of the queue
     */
    public short getLowPriorityWaiting() {
        return queueConfig.getLowPriorityWaiting();
    }

    /**
     * Count request user.
     * 
     * @param userId the user id
     * 
     * @return the int
     */
    public int countRequestUser(String userId) {
        Integer nbRqtForuser = threadPoolExecutor.getRequestCount(userId);
        return nbRqtForuser != null ? nbRqtForuser : 0;
    }

    /**
     * Shutdown.
     * 
     * @throws MotuException the motu exception
     */
    public void shutdown() {
        try {
            // Remove pending Runnable from the pool
            Collection<Runnable> removedRunnableList = new ArrayList<Runnable>();
            LOG.info(String.format("Queue '%s %s' is going to remove %d pending task(s) ....",
                                   this.getName(),
                                   this.queueConfig.getDescription(),
                                   threadPoolExecutor.getQueue().size()));
            threadPoolExecutor.getQueue().drainTo(removedRunnableList);
            shutdown(removedRunnableList);

            threadPoolExecutor.shutdown();

            while (!(threadPoolExecutor.isTerminated())) {
                LOG.info(String.format("Queue '%s %s'  await current job termination - (current pending job number is %d)",
                                       this.getName(),
                                       this.queueConfig.getDescription(),
                                       priorityBlockingQueue.size()));

                threadPoolExecutor.awaitTermination(1, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            LOG.error("ERROR in QueueManagement.shutdown.", e);
        }
        LOG.info(String.format("Queue '%s %s' is shutdown.", this.getName(), this.queueConfig.getDescription()));
    }

    /**
     * Shutdown.
     * 
     * @param removedRunnableList the removed runnable list
     */
    public void shutdown(Collection<Runnable> removedRunnableList) {
        for (Runnable r : removedRunnableList) {
            if (r instanceof IQueueJob) {
                ((IQueueJob) r).stop();
            }
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
     * Valeur de threadPoolExecutor.
     * 
     * @return la valeur.
     */
    public ExtractionThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

}
