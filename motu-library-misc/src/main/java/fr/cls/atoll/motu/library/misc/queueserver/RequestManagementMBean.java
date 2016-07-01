package fr.cls.atoll.motu.library.misc.queueserver;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;

/**
 * Interface that exposes the properties and methods that are visible from JMX.
 */
public interface RequestManagementMBean {

    ConcurrentMap<Long, StatusModeResponse> getResquestStatusMap();

    StatusModeResponse getResquestStatusMap(Long key);

    Set<Long> getResquestStatusMapKey();
}
