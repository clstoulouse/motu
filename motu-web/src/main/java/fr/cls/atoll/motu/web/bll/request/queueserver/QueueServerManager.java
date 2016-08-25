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
package fr.cls.atoll.motu.web.bll.request.queueserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.QueueJob;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.QueueJobListener;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.QueueManagement;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.QueueThresholdComparator;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.QueueServerType;
import fr.cls.atoll.motu.web.dal.config.xml.model.QueueType;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class QueueServerManager implements IQueueServerManager {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** The queue server config. */
    private QueueServerType queueServerConfig;

    /** The queue management map. */
    private Map<QueueType, QueueManagement> queueManagementMap;

    /**
     * The Constructor.
     * 
     * @throws MotuException the motu exception
     */
    public QueueServerManager() {
        queueManagementMap = new HashMap<QueueType, QueueManagement>();
    }

    /**
     * .
     */
    @Override
    public void init() {
        queueServerConfig = BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig();
        // Order queue by size (threshold) asc
        Collections.sort(getQueueServerConfig().getQueues(), new QueueThresholdComparator());

        for (QueueType queueConfig : getQueueServerConfig().getQueues()) {
            getQueueManagement().put(queueConfig, new QueueManagement(queueConfig));
        }
    }

    /**
     * Valeur de queueManagementMap.
     * 
     * @return la valeur.
     */
    @Override
    public Map<QueueType, QueueManagement> getQueueManagementMap() {
        return queueManagementMap;
    }

    /**
     * Execute.
     * 
     * @param runnableExtraction the runnable extraction
     * 
     * @throws MotuExceptionBase the motu exception base
     * @throws MotuException
     */
    @Override
    public void execute(RequestDownloadStatus rds_,
                        ConfigService cs_,
                        Product product_,
                        ExtractionParameters extractionParameters_,
                        double requestSizeInMB_,
                        Long requestId) throws MotuException {
        QueueManagement queueManagement = findQueue(requestSizeInMB_);
        if (queueManagement == null) {
            throw new MotuException(ErrorType.EXCEEDING_QUEUE_DATA_CAPACITY, "Oops, the size of the data to download (" + (int) requestSizeInMB_
                    + " Megabyte) is not managed by the Motu queue management system.");
        }
        rds_.setQueueId(queueManagement.getQueueConfig().getId());
        rds_.setQueueDescription(queueManagement.getQueueConfig().getDescription());

        // Here we synchronize the execution of the request
        QueueJobListener qjl = createQueueJobListener(rds_);
        queueManagement.execute(new QueueJob(cs_, product_, extractionParameters_, qjl, requestId));

        synchronized (this) {
            if (!qjl.isJobEnded()) {
                try {
                    wait();
                } catch (InterruptedException e1) {
                    LOGGER.error(e1);
                }
            }
        }
    }

    /**
     * .
     * 
     * @return
     */
    private QueueJobListener createQueueJobListener(final RequestDownloadStatus rds_) {
        return new QueueJobListener() {

            boolean isJobEnded = false;

            @Override
            public void onJobStarted() {
                rds_.setStartProcessingDateTime(System.currentTimeMillis());
            }

            @Override
            public void onJobStopped() {
                rds_.setEndProcessingDateTime(System.currentTimeMillis());

                synchronized (QueueServerManager.this) {
                    isJobEnded = true;
                    QueueServerManager.this.notify();
                }
            }

            @Override
            public void onJobException(MotuException e) {
                rds_.setRunningException(e);
                rds_.setEndProcessingDateTime(System.currentTimeMillis());

                synchronized (QueueServerManager.this) {
                    isJobEnded = true;
                    QueueServerManager.this.notify();
                }
            }

            @Override
            public boolean isJobEnded() {
                return isJobEnded;
            }

        };
    }

    /**
     * Valeur de queueServerConfig.
     * 
     * @return la valeur.
     */
    private QueueServerType getQueueServerConfig() {
        return queueServerConfig;
    }

    /**
     * Getter of the property <tt>queueManagement</tt>.
     * 
     * @return Returns the queueManagementMap.
     * 
     * @uml.property name="queueManagement"
     */
    private Map<QueueType, QueueManagement> getQueueManagement() {
        return queueManagementMap;
    }

    /**
     * Control max user.
     * 
     * @param queueManagement the queue management
     * @param runnableExtraction the runnable extraction
     * 
     */
    /**
     * .
     * 
     * @param userId
     * @param queueManagement
     * @return true if too much
     */
    @Override
    public boolean isNumberOfRequestTooHighForUser(String userId) {
        int countRequest = countRequestUser(userId);
        boolean isAnonymousUser = (userId == null);

        return (isAnonymousUser && getQueueServerConfig().getMaxPoolAnonymous() > 0 && countRequest > getQueueServerConfig().getMaxPoolAnonymous())
                || (!isAnonymousUser && getQueueServerConfig().getMaxPoolAuth() > 0 && countRequest >= getQueueServerConfig().getMaxPoolAuth());
    }

    /**
     * Count request user.
     * 
     * @param userId the user id
     * 
     * @return the int
     */
    private int countRequestUser(String userId) {
        int count = 0;
        for (QueueManagement queueManagement : getQueueManagement().values()) {
            count += queueManagement.countRequestUser(userId);
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
     */
    private QueueManagement findQueue(double sizeInMB_) {
        QueueManagement queueManagement = null;

        // queues are sorted by data threshold (ascending)
        for (QueueType queueType : getQueueServerConfig().getQueues()) {
            if (sizeInMB_ <= queueType.getDataThreshold()) {
                queueManagement = getQueueManagement().get(queueType);
                break;
            }
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
    @Override
    public double getMaxDataThreshold() {
        List<QueueType> queuesConfig = getQueueServerConfig().getQueues();
        double size = -1.0;
        for (QueueType queueType : queuesConfig) {
            double dataThreshold = queueType.getDataThreshold();
            if (size < dataThreshold) {
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
    @Override
    public void shutdown() throws MotuException {
        for (QueueManagement queueManagement : getQueueManagement().values()) {
            queueManagement.shutdown();
        }
    }

}
