package fr.cls.atoll.motu.library.misc.queueserver;

/**
 * Interface that exposes the properties and methods that are visibile from JMX.
 */
public interface QueueManagementMBean {
    String getScheduleJobName();

    String getScheduleTriggerName();

    Boolean isMaxQueueSizeReached();

    Short getMaxPoolSize();

    Integer getMaxThreads();

    String getId();
}
