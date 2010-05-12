package fr.cls.atoll.motu.library.misc.threadpools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class HelloJob implements StatefulJob {
    /**
     * Logger for this class
     */
    // private static Log _log = LogFactory.getLog(HelloJob.class);

    /**
     * <p>
     * Empty constructor for job initilization
     * </p>
     * <p>
     * Quartz requires a public empty constructor so that the scheduler can instantiate the class whenever it
     * needs.
     * </p>
     */
    public HelloJob() {
    }

    /**
     * <p>
     * Called by the <code>{@link org.quartz.Scheduler}</code> when a <code>{@link org.quartz.Trigger}</code>
     * fires that is associated with the <code>Job</code>.
     * </p>
     * 
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    // public void execute(JobExecutionContext context)
    // throws JobExecutionException {
    //
    // // Say Hello to the World and display the date/time
    // _log.info("Hello World! - " + new Date());
    // }
    @SuppressWarnings("unchecked")
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Thread.sleep(1000);
            System.err.println("CheckPriorityJob is executing.");

            String instName = context.getJobDetail().getName();
            String instGroup = context.getJobDetail().getGroup();

            JobDataMap dataMap = context.getJobDetail().getJobDataMap();

            String jobSays = dataMap.getString("jobSays");
            float myFloatValue = dataMap.getFloat("myFloatValue");
            List<Date> state = (ArrayList<Date>) dataMap.get("myStateData");
            state.add(new Date());
            System.err.println("Instance " + instName + " of DumbJob says: " + jobSays);
            // throw new JobExecutionException(new Exception ("Excetion test"));

        } catch (Exception e) {
            throw new JobExecutionException(e);
        }

    }

}