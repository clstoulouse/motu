package fr.cls.atoll.motu.processor.threadpools;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-04-23 14:16:09 $
 */
public class WPSProcessManagement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(WPSProcessManagement.class);
    
    /** The priority blocking queue. */
    private PriorityBlockingQueue<Runnable> priorityBlockingQueue = null;

    /** The thread pool executor. */
    private WPSProcessThreadPoolExecutor threadPoolExecutor = null;


    /**
     * Constructor.
     */
    public WPSProcessManagement() {
        
        this.priorityBlockingQueue = new PriorityBlockingQueue<Runnable>();
        this.threadPoolExecutor = new WPSProcessThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, priorityBlockingQueue);


    }

}
