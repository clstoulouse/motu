package fr.cls.atoll.motu.library.queueserver;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */

public class ScheduleRunGCJob implements StatefulJob {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(ScheduleRunGCJob.class);

    /**
     * Constructor.
     */
    public ScheduleRunGCJob() {
    }

    /**
     * {@inheritDoc}.
     * 
     * @param context the context
     * 
     * @throws JobExecutionException the job execution exception
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ScheduleRunGCJob.execute(JobExecutionContext) - entering");
        }

        Runtime r = Runtime.getRuntime();
        r.gc();  
        r.runFinalization();

        if (LOG.isDebugEnabled()) {
            LOG.debug("ScheduleRunGCJob.execute(JobExecutionContext) - exiting");
        }
    }
}
