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
import org.jasig.cas.client.util.AssertionHolder;

import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingQueueDataCapacityException;
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
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.QueueJob;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.QueueJobListener;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.QueueManagement;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.QueueThresholdComparator;
import fr.cls.atoll.motu.web.dal.config.xml.model.QueueServerType;
import fr.cls.atoll.motu.web.dal.config.xml.model.QueueType;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class QueueServerManagement {

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
    public QueueServerManagement() {
        queueManagementMap = new HashMap<QueueType, QueueManagement>();

        queueServerConfig = BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig();
        // Order queue by size (threshold) asc
        Collections.sort(getQueueServerConfig().getQueues(), new QueueThresholdComparator());

        for (QueueType queueConfig : getQueueServerConfig().getQueues()) {
            getQueueManagement().put(queueConfig, new QueueManagement(queueConfig));
        }
    }

    /**
     * Execute.
     * 
     * @param runnableExtraction the runnable extraction
     * 
     * @throws MotuExceptionBase the motu exception base
     */
    public void execute(final RequestDownloadStatus rds_, final ExtractionParameters extractionParameters) throws MotuExceptionBase {
        // TODO SMA : Ask BLL service for the amount size of the request
        // runnableExtraction.getAmountDataSizeAsMBytes()
        double sizeInMB = BLLManager.getInstance().getRequestManager().getAmountDataSizeAsMBytes(extractionParameters);

        QueueManagement queueManagement = findQueue(sizeInMB);
        queueManagement.execute(new QueueJob(extractionParameters, new QueueJobListener() {

            @Override
            public void onJobStarted() {
                rds_.setStartProcessingDateTime(System.currentTimeMillis());

                // TODO SMA not sure that CAS AssertionHolder shall be managed here !
                if (extractionParameters.getAssertion() != null) {
                    AssertionHolder.setAssertion(extractionParameters.getAssertion());
                }
            }

            @Override
            public void onJobStopped() {
                rds_.setEndProcessingDateTime(System.currentTimeMillis());
            }

            @Override
            public void onJobException(Exception e) {
                rds_.setRunningException(e);
                rds_.setEndProcessingDateTime(System.currentTimeMillis());

                // TODO SMA not sure that CAS AssertionHolder shall be managed here !
                if (extractionParameters.getAssertion() != null) {
                    AssertionHolder.clear();
                }
            }

        }));
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
     * Gets the queue according to a data threshold.
     * 
     * @param dataThreshold the data threshold
     * 
     * @return the queue or null if no queue can manage the dataThreshold
     * 
     */
    private QueueType getQueue(float dataThreshold) {
        List<QueueType> queuesConfig = getQueueServerConfig().getQueues();

        QueueType queueConfigToReturn = null;
        // Assumes that list of queues have been sorted by data threshold.
        for (QueueType queue : queuesConfig) {
            if (dataThreshold <= queue.getDataThreshold()
                    && (queueConfigToReturn != null && queueConfigToReturn.getDataThreshold() > queue.getDataThreshold())) {
                queueConfigToReturn = queue;
            }
        }

        return queueConfigToReturn;
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
    private boolean isNumberOfRequestTooHighForUser(String userId) {
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
    public void shutdown() throws MotuException {
        for (QueueManagement queueManagement : getQueueManagement().values()) {
            queueManagement.shutdown();
        }
    }

}
