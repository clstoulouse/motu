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

import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.quartz.Scheduler;

import fr.cls.atoll.motu.library.misc.configuration.QueueServerType;
import fr.cls.atoll.motu.library.misc.configuration.QueueType;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingQueueDataCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingUserCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class QueueServerManagement {
    // Name pattern under which managed beans get registered
    private final static String OBJECT_NAME_PATTERN = "fr.cls:artefact=Motu,domain=QueueServer,id={0},element={1}";

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(QueueServerManagement.class);

    /** The instance. */
    private static QueueServerManagement instance;

    /** The queue server config. */
    private QueueServerType queueServerConfig;

    /** The max pool anonymous overrided. */
    private Short maxPoolAnonymousOverrided = null;

    /** The max pool auth overrided. */
    private Short maxPoolAuthOverrided = null;

    /** The scheduler. */
    private Scheduler scheduler = null;

    /**
     * Gets the scheduler.
     * 
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Sets the scheduler.
     * 
     * @param scheduler the scheduler
     * 
     * @throws MotuException the motu exception
     */
    public void setScheduler(Scheduler scheduler) throws MotuException {
        this.scheduler = scheduler;

        Collection<QueueManagement> queuesCollection = queueManagementValues();
        for (QueueManagement queueManagement : queuesCollection) {
            if (this.scheduler != null) {
                queueManagement.createAndScheduleJob(this.scheduler);
            }
        }

    }

    /** The Constant SCHEDULE_PRIORITY_JOB_NAME. */
    public static final String SCHEDULE_PRIORITY_JOB_NAME = "scheduleQueuePriorityJob";

    /** The Constant SCHEDULE_PRIORITY_TRIGGER_NAME. */
    public static final String SCHEDULE_PRIORITY_TRIGGER_NAME = "scheduleQueuePriorityTrigger";

    /**
     * The Constructor.
     * 
     * @throws MotuException the motu exception
     */
    protected QueueServerManagement() throws MotuException {

        init();
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
     * Initialization of the queues and registration of the MBeans.
     * 
     * @throws MotuException the motu exception
     */
    protected void init() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("init() - entering");
        }

        queueManagementMap = new HashMap<QueueType, QueueManagement>();

        queueServerConfig = Organizer.getMotuConfigInstance().getQueueServerConfig();
        // Sort queues by data threshold.
        sortQueuesByDataThreshold();

        // createScheduler();

        createQueuesManagement();

        registerJmxMbeans();

        if (LOG.isDebugEnabled()) {
            LOG.debug("init() - exiting");
        }
    }

    /**
     * Registers to the MBean platform the managed beans like the queue managements and extraction thread pool
     * executors.
     */
    private void registerJmxMbeans() {
        try {
            // works well in tomcat
            final MBeanServer platform = ManagementFactory.getPlatformMBeanServer();

            for (QueueManagement queueManagement : queueManagementMap.values()) {
                // registers the queue management
                ObjectName name = new ObjectName(MessageFormat.format(OBJECT_NAME_PATTERN, queueManagement.getId(), "QueueManagement"));
                platform.registerMBean(queueManagement, name);

                // registers its associated executor
                name = new ObjectName(MessageFormat.format(OBJECT_NAME_PATTERN, queueManagement.getId(), "ThreadPoolExecutor"));
                platform.registerMBean(queueManagement.getThreadPoolExecutor(), name);
            }
        } catch (Exception e) {
            // JMX supervision should never alters Motu behaviour, so we don't let exeption propagation
            LOG.error("Failed to register managed beans (Motu will still continue to start)", e);
        }

    }

    /**
     * Gets the instance.
     * 
     * @return the instance
     * 
     * @throws MotuException the motu exception
     */
    public static QueueServerManagement getInstance() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstance() - entering");
        }

        if (instance == null) {
            instance = new QueueServerManagement();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstance() - exiting");
        }
        return instance;
    }

    /**
     * Checks for instance.
     * 
     * @return true, if has instance
     */
    public static boolean hasInstance() {
        return (instance != null);
    }

    // /**
    // * Creates the scheduler.
    // *
    // * @throws MotuException the motu exception
    // */
    // private void createScheduler() throws MotuException {
    // SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
    // try {
    // scheduler = schedulerFactory.getScheduler();
    // scheduler.start();
    // } catch (SchedulerException e) {
    // throw new MotuException("ERROR in QueueManagement.createScheduler.", e);
    // }
    //
    // }

    /**
     * Gets the queue according to a data threshold.
     * 
     * @param dataThreshold the data threshold
     * 
     * @return the queue
     * 
     * @throws MotuException the motu exception
     */
    public QueueType getQueue(float dataThreshold) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getQueue(float) - entering");
        }

        List<QueueType> queuesConfig = getQueuesConfig();
        QueueType queueConfigToReturn = null;

        if (queuesConfig.size() <= 0) {
            throw new MotuException("ERROR in QueueServerManagement.getQueue: list of queues is empty");
        }

        // Assumes that list of queues have been sorted by data threshold.
        for (QueueType queue : queuesConfig) {
            if (dataThreshold <= queue.getDataThreshold()) {
                queueConfigToReturn = queue;
            }
        }
        if (queueConfigToReturn == null) {
            throw new MotuException(
                    String.format("ERROR in QueueServerManagement.getQueue: no queue equivalent to a '%f' data threshold found", dataThreshold));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getQueue(float) - exiting");
        }
        return queueConfigToReturn;
    }

    /**
     * Gets the queues config.
     * 
     * @return the queues config
     */
    public List<QueueType> getQueuesConfig() {

        return queueServerConfig.getQueues();
    }

    /**
     * Sort queues by data threshold.
     */
    private void sortQueuesByDataThreshold() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sortQueuesByDataThreshold() - entering");
        }

        // Sort queues by data threshold.
        QueueThresholdComparator queueThresholdComparator = new QueueThresholdComparator();
        Collections.sort(getQueuesConfig(), queueThresholdComparator);

        if (LOG.isDebugEnabled()) {
            LOG.debug("sortQueuesByDataThreshold() - exiting");
        }
    }

    /**
     * Creates the queues management.
     * 
     * @throws MotuException the motu exception
     */
    private void createQueuesManagement() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createQueuesManagement() - entering");
        }

        List<QueueType> queuesConfig = getQueuesConfig();
        for (QueueType queueConfig : queuesConfig) {

            QueueManagement queueManagement = new QueueManagement(queueConfig);

            // if (scheduler != null) {
            // queueManagement.createAndScheduleJob(scheduler);
            // }

            putQueueManagement(queueConfig, queueManagement);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("createQueuesManagement() - exiting");
        }
    }

    /** The queue management map. */
    private Map<QueueType, QueueManagement> queueManagementMap;

    /**
     * Getter of the property <tt>queueManagement</tt>.
     * 
     * @return Returns the queueManagementMap.
     * 
     * @uml.property name="queueManagement"
     */
    public Map<QueueType, QueueManagement> getQueueManagement() {
        return queueManagementMap;
    }

    /**
     * Returns a set view of the keys contained in this map.
     * 
     * @return a set view of the keys contained in this map.
     * 
     * @see java.util.Map#keySet()
     * @uml.property name="queueManagement"
     */
    public Set<QueueType> queueManagementKeySet() {
        return queueManagementMap.keySet();
    }

    /**
     * Returns a collection view of the values contained in this map.
     * 
     * @return a collection view of the values contained in this map.
     * 
     * @see java.util.Map#values()
     * @uml.property name="queueManagement"
     */
    public Collection<QueueManagement> queueManagementValues() {
        return queueManagementMap.values();
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     * 
     * @param key key whose presence in this map is to be tested.
     * 
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     * 
     * @see java.util.Map#containsKey(Object)
     * @uml.property name="queueManagement"
     */
    public boolean queueManagementContainsKey(QueueType key) {
        return queueManagementMap.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
     * 
     * @param value value whose presence in this map is to be tested.
     * 
     * @return <tt>true</tt> if this map maps one or more keys to the specified value.
     * 
     * @see java.util.Map#containsValue(Object)
     * @uml.property name="queueManagement"
     */
    public boolean queueManagementContainsValue(QueueManagement value) {
        return queueManagementMap.containsValue(value);
    }

    /**
     * Returns the value to which this map maps the specified key.
     * 
     * @param key key whose associated value is to be returned.
     * 
     * @return the value to which this map maps the specified key, or <tt>null</tt> if the map contains no
     *         mapping for this key.
     * 
     * @see java.util.Map#get(Object)
     * @uml.property name="queueManagement"
     */
    public QueueManagement getQueueManagement(QueueType key) {
        return queueManagementMap.get(key);
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @return <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @see java.util.Map#isEmpty()
     * @uml.property name="queueManagement"
     */
    public boolean isQueueManagementEmpty() {
        return queueManagementMap.isEmpty();
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return the number of key-value mappings in this map.
     * 
     * @see java.util.Map#size()
     * @uml.property name="queueManagement"
     */
    public int queueManagementSize() {
        return queueManagementMap.size();
    }

    /**
     * Setter of the property <tt>queueManagement</tt>.
     * 
     * @param value the queueManagementMap to set.
     * 
     * @uml.property name="queueManagement"
     */
    public void setQueueManagement(Map<QueueType, QueueManagement> value) {
        queueManagementMap = value;
    }

    /**
     * Associates the specified value with the specified key in this map (optional operation).
     * 
     * @param value value to be associated with the specified key.
     * @param key key with which the specified value is to be associated.
     * 
     * @return previous value associated with specified key, or <tt>null</tt>
     * 
     * @see java.util.Map#put(Object,Object)
     * @uml.property name="queueManagement"
     */
    public QueueManagement putQueueManagement(QueueType key, QueueManagement value) {
        return queueManagementMap.put(key, value);
    }

    /**
     * Removes the mapping for this key from this map if it is present (optional operation).
     * 
     * @param key key whose mapping is to be removed from the map.
     * 
     * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.
     * 
     * @see java.util.Map#remove(Object)
     * @uml.property name="queueManagement"
     */
    public QueueManagement removeQueueManagement(QueueType key) {
        return queueManagementMap.remove(key);
    }

    /**
     * Removes all mappings from this map (optional operation).
     * 
     * @see java.util.Map#clear()
     * @uml.property name="queueManagement"
     */
    public void clearQueueManagement() {
        queueManagementMap.clear();
    }

    /**
     * Execute.
     * 
     * @param runnableExtraction the runnable extraction
     * 
     * @throws MotuExceptionBase the motu exception base
     */
    public void execute(RunnableExtraction runnableExtraction) throws MotuExceptionBase {
        if (LOG.isDebugEnabled()) {
            LOG.debug("execute(RunnableExtraction, boolean) - entering");
        }

        try {
            QueueManagement queueManagement = findQueueManagement(runnableExtraction);

            if (!runnableExtraction.isBatchQueue()) {
                checkMaxUser(runnableExtraction, queueManagement);
            }

            queueManagement.execute(runnableExtraction);
        } catch (MotuExceptionBase e) {
            LOG.error("execute(RunnableExtraction, boolean)", e);

            runnableExtraction.setError(e);
            throw e;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("execute(RunnableExtraction, boolean) - exiting");
        }
    }

    /**
     * Control max user.
     * 
     * @param queueManagement the queue management
     * @param runnableExtraction the runnable extraction
     * 
     * @throws MotuException the motu exception
     * @throws MotuExceedingUserCapacityException the motu exceeding user capacity exception
     */
    public void checkMaxUser(RunnableExtraction runnableExtraction, QueueManagement queueManagement)
            throws MotuException, MotuExceedingUserCapacityException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("controlMaxUser(RunnableExtraction) - entering");
        }

        int maxPoolAnonymous = computeMaxPoolAnonymous(queueManagement);
        int maxPoolAuthenticate = computeMaxPoolAuthenticate(queueManagement);

        boolean reached = false;

        String userId = runnableExtraction.getUserId();
        int countRequest = countRequestUser(userId);

        boolean isAnonymousUser = runnableExtraction.isAnonymousUser();

        if (isAnonymousUser) {
            if (maxPoolAnonymous > 0) {
                reached = (countRequest >= maxPoolAnonymous);
            }
        } else {
            if (maxPoolAuthenticate > 0) {
                reached = (countRequest >= maxPoolAuthenticate);
            }
        }

        if (reached) {
            MotuExceedingUserCapacityException e = new MotuExceedingUserCapacityException(
                    userId,
                    isAnonymousUser,
                    (isAnonymousUser ? maxPoolAnonymous : maxPoolAuthenticate));
            // runnableExtraction.setError(e);
            throw e;

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("controlMaxUser(RunnableExtraction) - exiting");
        }
    }

    /**
     * Compute max pool anonymous.
     * 
     * @param queueManagement the queue management
     * 
     * @return the int
     */
    public int computeMaxPoolAnonymous(QueueManagement queueManagement) {
        int maxPoolAnonymous = (maxPoolAnonymousOverrided != null ? maxPoolAnonymousOverrided.shortValue() : queueServerConfig.getMaxPoolAnonymous());

        int maxPoolSize = queueManagement.getMaxPoolSize();

        if ((maxPoolSize > 0) && ((maxPoolAnonymous > maxPoolSize) || (maxPoolAnonymous <= 0))) {

            maxPoolAnonymous = maxPoolSize;
        }
        return maxPoolAnonymous;
    }

    /**
     * Compute max pool authenticate.
     * 
     * @param queueManagement the queue management
     * 
     * @return the int
     */
    public int computeMaxPoolAuthenticate(QueueManagement queueManagement) {
        int maxPoolAuthenticate = (maxPoolAuthOverrided != null ? maxPoolAuthOverrided.shortValue() : queueServerConfig.getMaxPoolAuth());
        int maxPoolSize = queueManagement.getMaxPoolSize();

        if ((maxPoolSize > 0) && ((maxPoolAuthenticate > maxPoolSize) || (maxPoolAuthenticate <= 0))) {

            maxPoolAuthenticate = maxPoolSize;
        }
        return maxPoolAuthenticate;
    }

    /**
     * Count request user.
     * 
     * @param userId the user id
     * 
     * @return the int
     */
    private int countRequestUser(String userId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("countRequestUser(String) - entering");
        }

        Collection<QueueManagement> queuesCollection = queueManagementValues();
        int count = 0;
        for (QueueManagement queueManagement : queuesCollection) {
            count += queueManagement.countRequestUser(userId);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("countRequestUser(String) - exiting");
        }
        return count;

    }

    /**
     * Find queue management.
     * 
     * @param runnableExtraction the runnable extraction
     * 
     * @return the queue management
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuInconsistencyException the motu inconsistency exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuExceedingQueueDataCapacityException the motu exceeding queue data capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public QueueManagement findQueueManagement(RunnableExtraction runnableExtraction)
            throws MotuInconsistencyException, MotuInvalidDateException, MotuInvalidDepthException, MotuInvalidLatitudeException,
            MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
            MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException, NetCdfVariableException, MotuNoVarException, NetCdfAttributeException,
            NetCdfVariableNotFoundException, MotuExceedingQueueDataCapacityException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findQueueManagement(RunnableExtraction) - entering");
        }

        double size = runnableExtraction.getAmountDataSizeAsMBytes();
        List<QueueType> queuesConfig = getQueuesConfig();
        QueueManagement queueManagement = null;

        // queues are sorted by data threshold (ascending)
        for (QueueType queueConfig : queuesConfig) {
            if (runnableExtraction.isBatchQueue() != queueConfig.getBatch()) {
                continue;
            }
            if (size <= queueConfig.getDataThreshold()) {
                queueManagement = getQueueManagement(queueConfig);
                if (queueManagement == null) {
                    throw new MotuException(
                            String.format("ERROR in QueueserverManagement.findQueueManagement : unable to find queue configuration '%s' ",
                                          queueConfig.getDescription()));
                }
                break;
            }
        }

        if (queueManagement == null) {
            throw new MotuExceedingQueueDataCapacityException(
                    size,
                    getMaxDataThreshold(runnableExtraction.isBatchQueue()),
                    runnableExtraction.isBatchQueue());

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("findQueueManagement(RunnableExtraction) - exiting");
        }
        return queueManagement;
    }

    /**
     * Gets the max data threshold.
     * 
     * @param batchQueue the batch queue
     * 
     * @return the max data threshold
     */
    public double getMaxDataThreshold(boolean batchQueue) {
        if (getQueuesConfig().isEmpty()) {
            return -1d;
        }

        // // queues are sorted by data threshold (ascending)
        // QueueType queueConfig = getQueuesConfig().get(getQueuesConfig().size() - 1);
        // return queueConfig.getDataThreshold();
        //
        List<QueueType> queuesConfig = getQueuesConfig();
        double size = Double.MIN_VALUE;

        // queues are sorted by data threshold (ascending)
        for (QueueType queueConfig : queuesConfig) {
            if (batchQueue != queueConfig.getBatch()) {
                continue;
            }
            double dataThreshold = queueConfig.getDataThreshold();
            if (size <= queueConfig.getDataThreshold()) {
                size = dataThreshold;
            }
        }
        return size;

    }

    /**
     * Shutdown.
     * 
     * @throws MotuException the motu exception
     */
    public void shutdown() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("QueueServerManagement shutdown() - entering");
        }

        Collection<QueueManagement> queuesCollection = queueManagementValues();
        for (QueueManagement queueManagement : queuesCollection) {
            queueManagement.shutdown();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("QueueServerManagement shutdown() - exiting");
        }
    }

    /**
     * Gets the max pool anonymous overrided.
     * 
     * @return the max pool anonymous overrided
     */
    public Short getMaxPoolAnonymousOverrided() {
        return maxPoolAnonymousOverrided;
    }

    /**
     * Sets the max pool anonymous overrided.
     * 
     * @param maxPoolAnonymousOverrided the max pool anonymous overrided
     */
    public void setMaxPoolAnonymousOverrided(Short maxPoolAnonymousOverrided) {
        this.maxPoolAnonymousOverrided = maxPoolAnonymousOverrided;
    }

    /**
     * Gets the max pool auth overrided.
     * 
     * @return the max pool auth overrided
     */
    public Short getMaxPoolAuthOverrided() {
        return maxPoolAuthOverrided;
    }

    /**
     * Sets the max pool auth overrided.
     * 
     * @param maxPoolAuthOverrided the max pool auth overrided
     */
    public void setMaxPoolAuthOverrided(Short maxPoolAuthOverrided) {
        this.maxPoolAuthOverrided = maxPoolAuthOverrided;
    }

    /**
     * Gets the default priority.
     * 
     * @return the default priority
     */
    public short getDefaultPriority() {
        if (queueServerConfig == null) {
            return 2;
        }
        return queueServerConfig.getDefaultPriority();
    }

}
