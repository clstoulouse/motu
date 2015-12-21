package fr.cls.atoll.motu.library.misc.mxbean;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

public class Memory implements MemoryMXBean {
    public MemoryUsage getUsage() {
        return memoryUsageSnapshot();
    }

    private static MemoryUsage memoryUsageSnapshot() {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : pools) {
            return pool.getPeakUsage();
          }
        return null;

    }

}