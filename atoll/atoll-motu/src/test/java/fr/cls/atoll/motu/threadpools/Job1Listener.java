package fr.cls.atoll.motu.threadpools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

public class Job1Listener implements JobListener {

    private static Log _log = LogFactory.getLog(Job1Listener.class);

    public String getName() {
        return "job1_to_job2";
    }

    public void jobToBeExecuted(JobExecutionContext inContext) {
        _log.info("Job1Listener says: Job Is about to be executed.");
    }

    public void jobExecutionVetoed(JobExecutionContext inContext) {
        _log.info("Job1Listener says: Job Execution was vetoed.");
    }

    public void jobWasExecuted(JobExecutionContext inContext, JobExecutionException inException) {
        _log.info("Job1Listener says: Job was executed.");

        if (inException != null) {
            _log.info("Job1Listener exception.");

        }

        // // Simple job #2
        // JobDetail job2 =
        // new JobDetail("job2",
        // Scheduler.DEFAULT_GROUP,
        // SimpleJob2.class);
        //        
        // // Simple trigger to fire immediately
        // SimpleTrigger trigger =
        // new SimpleTrigger("job2Trigger",
        // Scheduler.DEFAULT_GROUP,
        // new Date(),
        // null,
        // 0,
        // 0L);
        //        
        // try {
        // // schedule the job to run!
        // inContext.getScheduler().scheduleJob(job2, trigger);
        // } catch (SchedulerException e) {
        // _log.warn("Unable to schedule job2!");
        // e.printStackTrace();
        // }
        //        
    }
}
