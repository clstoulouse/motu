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