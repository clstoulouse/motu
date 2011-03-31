package fr.cls.atoll.motu.library.misc.queueserver;

/**
 * Interface that exposes the properties and methods that are visible from JMX.
 */
public interface QueueManagementMXBean {
    String getScheduleJobName();

    String getScheduleTriggerName();

    Boolean isMaxQueueSizeReached();

    Short getMaxPoolSize();

    Integer getMaxThreads();

    String getId();

    float getDataThreshold();

    boolean getBatch();

    short getLowPriorityWaiting();
}
