package fr.cls.atoll.motu.library.misc.threadpools;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.queueserver.ScheduleCleanJob;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class TestSchedule {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(TestSchedule.class);

    /**
     * .
     * 
     * @param args
     */
    public static void main(String[] args) {

        TestSchedule testSchedule = new TestSchedule();

        try {
            // testSchedule.testQuartz();
            testFileToDelete();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testQuartz() throws SchedulerException, InterruptedException {
        SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
        Scheduler sched = schedFact.getScheduler();

        // JobDetail jobDetail = new JobDetail("myJob",
        // Scheduler.DEFAULT_GROUP,
        // TestSchedule.CheckPriorityJob.class);
        JobDetail jobDetail = new JobDetail("myJob", Scheduler.DEFAULT_GROUP, HelloJob.class);

        JobDataMap map = new JobDataMap();
        map.put("userId", "155-123587");

        jobDetail.setJobDataMap(map);

        jobDetail.getJobDataMap().put("jobSays", "Hello World!");
        jobDetail.getJobDataMap().put("myFloatValue", 3.141f);
        jobDetail.getJobDataMap().put("myStateData", new ArrayList<Date>());

        long endTime = System.currentTimeMillis() + 40000L;

        SimpleTrigger trigger = new SimpleTrigger(
                "myTrigger",
                Scheduler.DEFAULT_GROUP,
                new Date(),
                new Date(endTime),
                SimpleTrigger.REPEAT_INDEFINITELY,
                10L * 1000L);

        // Trigger trigger = TriggerUtils.makeMinutelyTrigger(2);
        // Trigger trigger = TriggerUtils.makeSecondlyTrigger(2);
        // trigger.setName("testtrig");

        JobListener listener = new Job1Listener();
        sched.addJobListener(listener);

        // make sure the listener is associated with the job
        jobDetail.addJobListener(listener.getName());

        sched.scheduleJob(jobDetail, trigger);
        sched.start();

        Thread.sleep(5000);

        sched.shutdown();
    }

    // public class CheckPriorityJob implements Job {
    // public class CheckPriorityJob implements StatefulJob {
    //            
    // /**
    // * Constructeur.
    // */
    // public CheckPriorityJob() {
    // }
    //
    // public void execute(JobExecutionContext context) throws JobExecutionException {
    // try {
    // Thread.sleep(1000);
    // System.err.println("CheckPriorityJob is executing.");
    //                
    //                
    // String instName = context.getJobDetail().getName();
    // String instGroup = context.getJobDetail().getGroup();
    //
    // JobDataMap dataMap = context.getJobDetail().getJobDataMap();
    //
    // String jobSays = dataMap.getString("jobSays");
    // float myFloatValue = dataMap.getFloat("myFloatValue");
    // List<Date> state = (ArrayList<Date>)dataMap.get("myStateData");
    // state.add(new Date());
    //
    // System.err.println("Instance " + instName + " of DumbJob says: " + jobSays);
    // } catch (Exception e) {
    // throw new JobExecutionException(e);
    // }
    // }
    // }

    public static void testFileToDelete() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("testFileToDelete() - entering");
        }

        int interval = 240;
        if (interval > 0) {
            interval = -interval;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, interval);

        Long timeRef = cal.getTimeInMillis();

        ScheduleCleanJob scheduleCleanJob = new ScheduleCleanJob();
        FileFilter fileFilter = scheduleCleanJob.new ExtractedFileToDeleteFilter(".*\\.nc$|.*\\.zip$|.*\\.tar$|.*\\.gz$", timeRef);
        File directoryToScan = new File(Organizer.getMotuConfigInstance().getExtractionPath());
        File[] files = null;
        files = directoryToScan.listFiles(fileFilter);

        for (File fileToDelete : files) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("cleanExtractedFile(JobExecutionContext) - Deleting file '%s' ", fileToDelete.getPath()));
            }

            boolean isDeleted = true;
            // boolean isDeleted = fileToDelete.delete();
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("cleanExtractedFile(JobExecutionContext) - file '%s' deleted: '%b'", fileToDelete.getPath(), isDeleted));
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanExtractedFile(JobExecutionContext) - exiting");
        }
    }

}
