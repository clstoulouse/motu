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

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fr.cls.atoll.motu.web.bll.exception.MotuException;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 *
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */
public class ExtractionThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * The identifier of this extraction pool executor.
     */
    private final String id;

    /**
     * The users.
     */
    private ConcurrentMap<String, Integer> users;

    /**
     * The Constructor.
     *
     * @param id the unique identifier of this thread executor
     * @param unit the unit
     * @param corePoolSize the core pool size
     * @param workQueue the work queue
     * @param maximumPoolSize the maximum pool size
     * @param keepAliveTime the keep alive time
     * @see {@link java.util.concurrent.ThreadPoolExecutor}
     */
    public ExtractionThreadPoolExecutor(
        String id,
        int corePoolSize,
        int maximumPoolSize,
        long keepAliveTime,
        TimeUnit unit,
        BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new QueueThreadFactory(id));
        this.id = id;
        users = new ConcurrentHashMap<String, Integer>();
    }

    /**
     * Gets the users.
     *
     * @return the users
     */
    public Map<String, Integer> getUsersRequestNumberMap() {
        return users;
    }

    /**
     * Increment user.
     *
     * @param userId_ the key
     * @return the integer
     */
    public Integer onNewRequestForUser(String userId_) {
        if (userId_ == null) {
            return null;
        }

        Integer nbRqtForUser = getUsersRequestNumberMap().get(userId_);
        if (nbRqtForUser == null) {
            nbRqtForUser = 0;

        }
        nbRqtForUser++;
        getUsersRequestNumberMap().put(userId_, nbRqtForUser);

        return nbRqtForUser;
    }

    /**
     * Decrement user.
     *
     * @param key the key
     * @return the integer
     */
    public Integer onRequestStoppedForUser(String userId_) {
        if (userId_ == null) {
            return null;
        }
        Integer nbRqtForUser = getUsersRequestNumberMap().get(userId_);
        if (nbRqtForUser != null) {
            nbRqtForUser--;
            if (nbRqtForUser > 0) {
                getUsersRequestNumberMap().put(userId_, nbRqtForUser);
            } else {
                getUsersRequestNumberMap().remove(userId_);
            }
        }

        return nbRqtForUser;
    }

    // /**
    // * Before execute.
    // *
    // * @param t the t
    // * @param r the r
    // * @see {@link java.util.concurrent.ThreadPoolExecutor}
    // */
    // @Override
    // protected void beforeExecute(Thread t, Runnable r) {
    // ((IQueueJob) r).setStarted();
    // super.beforeExecute(t, r);
    // }

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

        IQueueJob qj = (IQueueJob) r;
        if (t != null) {
            MotuException e = new MotuException(
                    String.format("An error occurs during extraction (detected from afterExecute): user id: '%s' - request parameters '%s'",
                                  qj.getExtractionParameters().getUserId(),
                                  qj.getExtractionParameters().toString()),
                    t);
            qj.onJobException(e);
        }

        onRequestStoppedForUser(qj.getExtractionParameters().getUserId());

        // runnableExtraction.setEnded();
    }

    /** @return the unique identifier of this pool executor */
    public String getId() {
        return id;
    }

    /**
     * Count number requests.
     *
     * @return the total number of requests
     */
    public int countNumberRequests() {
        int total = 0;
        for (Integer num : getUsersRequestNumberMap().values()) {
            total += num;
        }
        return total;
    }
}
