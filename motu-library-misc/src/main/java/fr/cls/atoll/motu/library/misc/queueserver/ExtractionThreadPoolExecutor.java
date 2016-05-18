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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidQueuePriorityException;
import fr.cls.atoll.motu.web.bll.request.queueserver.RunnableExtraction;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 *
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */
public class ExtractionThreadPoolExecutor extends ThreadPoolExecutor implements ExtractionThreadPoolExecutorMBean {

    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(ExtractionThreadPoolExecutor.class);

    /**
     * The Constant MIN_PRIORITY_VALUE.
     */
    public final static int MIN_PRIORITY_VALUE = 1;

    /**
     * The Constant MAX_PRIORITY_VALUE.
     */
    public final static int MAX_PRIORITY_VALUE = 2;

    /**
     * The identifier of this extraction pool executor.
     */
    private final String id;

    
    /**
     * The Constructor.
     *
     * @param id              the unique identifier of this thread executor
     * @param unit            the unit
     * @param corePoolSize    the core pool size
     * @param workQueue       the work queue
     * @param maximumPoolSize the maximum pool size
     * @param keepAliveTime   the keep alive time
     * @see {@link java.util.concurrent.ThreadPoolExecutor}
     */
    public ExtractionThreadPoolExecutor(String id, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new QueueThreadFactory(id));
        this.id = id;
    }

    /**
     * Check priority.
     *
     * @param priority the priority
     * @throws MotuInvalidQueuePriorityException
     *          the motu invalid queue priority exception
     */
    public static void checkPriority(int priority) throws MotuInvalidQueuePriorityException {
        if ((priority < MIN_PRIORITY_VALUE) || (priority > MAX_PRIORITY_VALUE)) {
            throw new MotuInvalidQueuePriorityException(priority, MIN_PRIORITY_VALUE, MAX_PRIORITY_VALUE);
        }

    }

    /**
     * Checks if is higher priority.
     *
     * @param priority the priority
     * @return true, if is higher priority
     */
    public static boolean isHigherPriority(int priority) {
        return priority == ExtractionThreadPoolExecutor.MIN_PRIORITY_VALUE;
    }

    /**
     * Checks if is lower priority.
     *
     * @param priority the priority
     * @return true, if is lower priority
     */
    public static boolean isLowerPriority(int priority) {
        return priority == ExtractionThreadPoolExecutor.MAX_PRIORITY_VALUE;
    }

    /**
     * The users.
     */
    private final ConcurrentMap<String, Integer> users = new ConcurrentHashMap<String, Integer>();

    /**
     * Gets the users.
     *
     * @return the users
     */
    @Override
    public ConcurrentMap<String, Integer> getUsers() {
        return users;
    }

    /**
     * Users key set.
     *
     * @return the set< string>
     */
    public Set<String> usersKeySet() {
        return users.keySet();
    }

    /**
     * Users contains key.
     *
     * @param key the key
     * @return true, if users contains key
     */
    public boolean usersContainsKey(String key) {
        return users.containsKey(key);
    }

    /**
     * Gets the users.
     *
     * @param key the key
     * @return the users
     */
    public Integer getUsers(String key) {
        return users.get(key);
    }

    /**
     * Checks if is users map empty.
     *
     * @return true, if is users map empty
     */
    public boolean isUsersMapEmpty() {
        return users.isEmpty();
    }

    /**
     * Users size.
     *
     * @return the int
     */
    @Override
    public int usersSize() {
        return users.size();
    }

    /**
     * Put users.
     *
     * @param value the value
     * @param key   the key
     * @return the integer
     */
    public Integer putUsers(String key, Integer value) {
        return users.put(key, value);
    }

    /**
     * Replace users.
     *
     * @param value the value
     * @param key   the key
     * @return the integer
     */
    public Integer replaceUsers(String key, Integer value) {
        return users.replace(key, value);
    }

    /**
     * Removes the users.
     *
     * @param key the key
     * @return the integer
     */
    public Integer removeUsers(String key) {
        return users.remove(key);
    }

    /**
     * Clear users.
     */
    public void clearUsers() {
        users.clear();
    }

    /**
     * Increment user.
     *
     * @param key the key
     * @return the integer
     */
    public Integer incrementUser(String key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("incrementUser(String) - entering");
        }

        if (key == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("incrementUser(String) - exiting");
            }
            return null;
        }
        Integer value = getUsers(key);
        Integer returnedValue = null;
        if (value == null) {
            returnedValue = putUsers(key, 1);
        } else {
            value++;
            returnedValue = replaceUsers(key, value);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("incrementUser(String) - exiting");
        }
        return returnedValue;
    }

    /**
     * Decrement user.
     *
     * @param key the key
     * @return the integer
     */
    public Integer decrementUser(String key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("decrementUser(String) - entering");
        }

        if (key == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("decrementUser(String) - kes is null -  exiting");
            }
            return null;
        }
        Integer value = getUsers(key);
        Integer returnedValue = null;
        if (value != null) {
            value--;
            if (value > 0) {
                returnedValue = replaceUsers(key, value);
            } else {
                returnedValue = removeUsers(key);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("decrementUser(String) - exiting");
        }
        return returnedValue;
    }

    /**
     * Decrement user.
     *
     * @param runnableExtraction the runnable extraction
     */
    public void decrementUser(RunnableExtraction runnableExtraction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("decrementUser(RunnableExtraction) - entering");
        }

        String userId = null;
        try {
            userId = runnableExtraction.getUserId();
        } catch (MotuException e) {
            LOG.error("decrementUser(RunnableExtraction)", e);

            // Do nothing, error is set.
        }

        decrementUser(userId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("decrementUser(RunnableExtraction) - exiting");
        }
    }

    /**
     * Increment user.
     *
     * @param runnableExtraction the runnable extraction
     */
    public void incrementUser(RunnableExtraction runnableExtraction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("incrementUser(RunnableExtraction) - entering");
        }

        String userId = null;
        try {
            userId = runnableExtraction.getUserId();
        } catch (MotuException e) {
            LOG.error("incrementUser(RunnableExtraction)", e);

            // Do nothing, error is set.
        }

        incrementUser(userId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("incrementUser(RunnableExtraction) - exiting");
        }

    }

    /**
     * The count priority map.
     */
    private final ConcurrentMap<Integer, Integer> priorityMap = new ConcurrentHashMap<Integer, Integer>();

    /**
     * Gets the priority map.
     *
     * @return the priority map
     */
    @Override
    public ConcurrentMap<Integer, Integer> getPriorityMap() {
        return priorityMap;
    }

    /**
     * Priority map key set.
     *
     * @return the set< integer>
     */
    public Set<Integer> priorityMapKeySet() {
        return priorityMap.keySet();
    }

    /**
     * Psriority map contains key.
     *
     * @param key the key
     * @return true, if psriority map contains key
     */
    public boolean priorityMapContainsKey(Integer key) {
        return priorityMap.containsKey(key);
    }

    /**
     * Gets the priority map.
     *
     * @param key the key
     * @return the priority map
     */
    public Integer getPriorityMap(Integer key) {
        return priorityMap.get(key);
    }

    /**
     * Checks if is priority map empty.
     *
     * @return true, if is priority map empty
     */
    public boolean isPriorityMapEmpty() {
        return priorityMap.isEmpty();
    }

    /**
     * Priority map size.
     *
     * @return the int
     */
    public int priorityMapSize() {
        return priorityMap.size();
    }

    /**
     * Put priority map.
     *
     * @param value the value
     * @param key   the key
     * @return the integer
     */
    public Integer putPriorityMap(Integer key, Integer value) {
        return priorityMap.put(key, value);
    }

    /**
     * Replace priority map.
     *
     * @param value the value
     * @param key   the key
     * @return the integer
     */
    public Integer replacePriorityMap(Integer key, Integer value) {
        return priorityMap.replace(key, value);
    }

    /**
     * Removes the priority map.
     *
     * @param key the key
     * @return the integer
     */
    public Integer removePriorityMap(Integer key) {
        return priorityMap.remove(key);
    }

    /**
     * Clear priority map.
     */
    public void clearPriorityMap() {
        priorityMap.clear();
    }

    /**
     * Increment priority map.
     *
     * @param priority the priority
     * @return the integer
     * @throws MotuInvalidQueuePriorityException
     *          the motu invalid queue priority exception
     */
    public synchronized Integer incrementPriorityMap(Integer priority) throws MotuInvalidQueuePriorityException {
        // check first it is not null
        if (priority == null) {
            return null;
        }
        // then check bounds
        ExtractionThreadPoolExecutor.checkPriority(priority);

        // ok, now we go on
        Integer value = getPriorityMap(priority);
        Integer returnedValue = null;
        if (value == null) {
            returnedValue = putPriorityMap(priority, 1);
        } else {
            value++;
            returnedValue = replacePriorityMap(priority, value);
        }

        return returnedValue;
    }

    // /**
    // * Decrement priority map.
    // *
    // * @param priority the priority
    // *
    // * @return the integer
    // *
    // * @throws MotuInvalidQueuePriorityException the motu invalid queue priority exception
    // */
    // public synchronized Integer decrementPriorityMap(Integer priority) throws
    // MotuInvalidQueuePriorityException {
    // ExtractionThreadPoolExecutor.checkPriority(priority);
    //
    // if (priority == null) {
    // return null;
    // }
    // Integer value = getPriorityMap(priority);
    // Integer returnedValue = null;
    // if (value != null) {
    // value--;
    // if (value > 0) {
    // returnedValue = replacePriorityMap(priority, value);
    // } else {
    // returnedValue = removePriorityMap(priority);
    // }
    // }
    //
    // return returnedValue;
    // }
    //

    /**
     * Adjust priority map.
     *
     * @param runnableExtraction the runnable extraction
     */
    public synchronized void adjustPriorityMap(RunnableExtraction runnableExtraction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("adjustPriorityMap(RunnableExtraction) - entering");
        }

        Integer priority = runnableExtraction.getPriority();
        int runningRange = runnableExtraction.getRange();

        Integer lastRange = getPriorityMap(priority);

        if (lastRange != null) {
            if (lastRange == runningRange) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("adjustPriorityMap priority %d range %d max rage %d - remove priority.",
                            priority.intValue(),
                            runningRange,
                            lastRange));
                }
                removePriorityMap(priority);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("adjustPriorityMap(RunnableExtraction) - exiting");
        }
    }

    /**
     * Increment priority map.
     *
     * @param runnableExtraction the runnable extraction
     * @throws MotuInvalidQueuePriorityException
     *          the motu invalid queue priority exception
     */
    public synchronized void incrementPriorityMap(RunnableExtraction runnableExtraction) throws MotuInvalidQueuePriorityException {

        Integer priority = runnableExtraction.getPriority();

        // if (ExtractionThreadPoolExecutor.isHigherPriority(priority)) {
        // return;
        // }

        incrementPriorityMap(priority);

        runnableExtraction.setPriority(priority, getPriorityMap(priority));

    }

    /**
     * After execute.
     *
     * @param t the t
     * @param r the r
     * @see {@link java.util.concurrent.ThreadPoolExecutor}
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if (LOG.isDebugEnabled()) {
            LOG.debug("afterExecute(Runnable, Throwable) - entering");
        }

        if (!(r instanceof RunnableExtraction)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("afterExecute(Runnable, Throwable) - exiting");
            }
            return;
        }

        RunnableExtraction runnableExtraction = (RunnableExtraction) r;

        if (LOG.isDebugEnabled()) {
            try {
                Product product = runnableExtraction.getProduct();

                String downloadUrlPath = "";
                String extractLocationData = "";
                if (product != null) {
                    downloadUrlPath = product.getDownloadUrlPath();
                    extractLocationData = product.getExtractLocationData();
                }

                LOG.debug(String
                        .format("afterExecute : user id: '%s' \n\t request parameters '%s'\n\t downloadUrlPath: '%s'\n\t extractLocationData: '%s' ",
                                runnableExtraction.getUserId(),
                                runnableExtraction.getExtractionParameters().toString(),
                                downloadUrlPath,
                                extractLocationData));
                LOG.debug(String.format("afterExecute : stattus response '%s', code: %s (%d), msg: '%s'", runnableExtraction.getStatusModeResponse()
                        .getStatus(), runnableExtraction.getStatusModeResponse().getCode().toString(), runnableExtraction.getStatusModeResponse()
                        .getCode().value(), runnableExtraction.getStatusModeResponse().getMsg()));
            } catch (Exception e) {
                // Do nothing
            }
        }

        if (t != null) {
            try {
                MotuException e = new MotuException(String
                        .format("An error occurs during extraction (detected from afterExecute): user id: '%s' - request parameters '%s'",
                                runnableExtraction.getUserId(),
                                runnableExtraction.getExtractionParameters().toString()), t);
                runnableExtraction.setError(e);
            } catch (MotuException e) {
                // Do nothing
            }
        }

        decrementUser(runnableExtraction);
        adjustPriorityMap(runnableExtraction);

        runnableExtraction.setEnded();

        if (LOG.isDebugEnabled()) {
            LOG.debug("afterExecute(Runnable, Throwable) - exiting");
        }
    }

    /**
     * Before execute.
     *
     * @param t the t
     * @param r the r
     * @see {@link java.util.concurrent.ThreadPoolExecutor}
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("beforeExecute(Thread, Runnable) - entering");
        }
        
        RunnableExtraction runnableExtraction = null;
        if (!(r instanceof RunnableExtraction)) {
            super.beforeExecute(t, r);

            if (LOG.isDebugEnabled()) {
                LOG.debug("beforeExecute(Thread, Runnable) - exiting");
            }
            return;
        }

        runnableExtraction = (RunnableExtraction) r;
        if (LOG.isDebugEnabled()) {
            try {
                LOG.debug(String.format("beforeExecute: user id: '%s' - request paramters '%s'", runnableExtraction.getUserId(), runnableExtraction
                        .getExtractionParameters().toString()));
            } catch (Exception e) {
                // Do nothing
            }
        }
        // incrementUser(runnableExtraction);

        runnableExtraction.setStarted();

        super.beforeExecute(t, r);

        if (LOG.isDebugEnabled()) {
            LOG.debug("beforeExecute(Thread, Runnable) - exiting");
        }

    }
    


    /** @return the unique identifier of this pool executor */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Count number requests.
     *
     * @return the total number of requests
     */
    @Override
    public int countNumberRequests() {
        int total = 0;
        Collection<Integer> numRequestperUser = users.values();
        for (Integer num : numRequestperUser) {
            total += num;
        }
        return total;
    }
}
