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
package fr.cls.atoll.motu.library.misc.threadpools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
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
