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

import com.thoughtworks.xstream.XStream;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.library.misc.configuration.QueueServerType;
import fr.cls.atoll.motu.library.misc.configuration.QueueType;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingQueueCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.intfce.ExtractionParameters;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.queueserver.ExtractionThreadPoolExecutor;
import fr.cls.atoll.motu.library.misc.queueserver.QueueLogError;
import fr.cls.atoll.motu.library.misc.queueserver.QueueLogInfo;
import fr.cls.atoll.motu.library.misc.queueserver.QueueServerManagement;
import fr.cls.atoll.motu.library.misc.queueserver.RequestManagement;
import fr.cls.atoll.motu.library.misc.queueserver.RunnableExtraction;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class TestThreadPools {

    public class Client extends Thread {
        String name;

        QueueServerManagement queueServerManagement = null;
        RunnableExtraction runnableExtraction = null;

        public Client(String name, QueueServerManagement queueServerManagement, RunnableExtraction runnableExtraction) {
            this.name = name;
            this.runnableExtraction = runnableExtraction;
            this.queueServerManagement = queueServerManagement;

        }

        @Override
        public void run() {

            System.out.print("Start Client ");
            System.out.println(name);
            try {
                queueServerManagement.execute(runnableExtraction);
            } catch (MotuExceptionBase e) {
                // runnableExtraction.setError(e);
                runnableExtraction.aborted();
                System.out.println(e.notifyException());
            } catch (Exception e) {
                // runnableExtraction.setError(e);
                runnableExtraction.aborted();
                System.out.println(e.getMessage());
            }

            System.out.print("End Client ");
            System.out.println(name);

        }
    }

    public class ExecutePoolThread extends Thread {

        ThreadPoolExecutor executor;

        private boolean run = false;

        public ExecutePoolThread(ThreadPoolExecutor executor) {
            this.executor = executor;
        }

        public void notifyShutdownExecutor() {
            run = false;
        }

        @Override
        public void run() {
            System.out.print("Start ExcutePool ");
            System.out.println(executor.toString());
            BlockingQueue<Runnable> q = executor.getQueue();
            while (q.peek() != null) {
                Runnable r = q.poll();
                if (r != null) {
                    executor.execute(r);
                }
            }

            System.out.println("End ExcutePool ");
        }
    }

    public class RunnableSomething implements Runnable, Comparable<RunnableSomething> {
        private final int thePriority;
        private final int theRange;
        private final String theText;

        public RunnableSomething(String text, int priority, int range) {
            theText = text;
            thePriority = priority;
            theRange = range;
        }

        public int compareTo(RunnableSomething obj) {
            // int retval = Integer.valueOf(thePriority).compareTo(Integer.valueOf(obj.getPriority()));
            int objPriority = obj.getPriority();
            int retval = 0;
            if (thePriority > objPriority) {
                return 1;
            }
            if (thePriority < objPriority) {
                return -1;
            }
            if (retval == 0) {
                retval = Integer.valueOf(theRange).compareTo(Integer.valueOf(obj.getRange()));
            }
            // System.out.println(thePriority + " compareTo " + obj.getPriority() + " retval: " + retval);
            return retval;
        }

        public int getPriority() {
            return thePriority;
        }

        public int getRange() {
            return theRange;
        }

        public String getText() {
            return theText;
        }

        public void run() {
            String retval = null;
            // System.out.println("from thread with text: " + theText);

            // imagine we do some real work here, setting the string on success
            try {
                Thread.sleep(500);
                retval = theText;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

    }
    class DateT {
        int day;
        int month;
        int year;

        public int getDay() {
            return day;
        }

        public int getMonth() {
            return month;
        }

        public int getYear() {
            return year;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public void setYear(int year) {
            this.year = year;
        }
    }
    /** Sample Comparator */
    static class MyComparator implements Comparator<RunnableSomething> {
        public int compare(RunnableSomething x, RunnableSomething y) {
            int i = x.getPriority();
            int j = y.getPriority();
            if (i > j) {
                return 1;
            }
            if (i < j) {
                return -1;
            }
            return 0;
        }
    }

    /** Sample Comparator */
    static class MyReverseComparator implements Comparator<Object> {
        public int compare(Object x, Object y) {
            int i = ((Integer) x).intValue();
            int j = ((Integer) y).intValue();
            if (i < j) {
                return 1;
            }
            if (i > j) {
                return -1;
            }
            return 0;
        }
    }

    private class CallableSomething implements Callable<String>, Comparable<CallableSomething> {
        private final int thePriority;
        private final String theText;

        public CallableSomething(String text, int priority) {
            theText = text;
            thePriority = priority;
        }

        public String call() {
            String retval = null;
            System.out.println("from thread with text: " + theText);

            // imagine we do some real work here, setting the string on success
            try {
                Thread.sleep(2000);
                retval = theText;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            return retval;
        }

        public int compareTo(CallableSomething obj) {
            int retval = Integer.valueOf(thePriority).compareTo(Integer.valueOf((obj).getPriority()));

            System.out.println(thePriority + " compareTo " + (obj).getPriority() + " retval: " + retval);
            return retval;
        }

        public int getPriority() {
            return thePriority;
        }

    }

    /** The Constant NUM_THREADPOOLS. */
    public static final int NUM_THREADPOOLS = 2;

    private static String[] arr3 = {
            "1 this array has many more elements", "2 and we want to test it", "3 with many strings", "4 and some more", "5 how many are there",
            "6 twenty is fine", "7 or thirty", "8 maybe fourty", "9 today is Monday", "10 the month is March", "11 Loquendo rules!", "12 something",
            "13 something", "14 something", "15 something", "16 something", "17 something", "18 something", "19 something", "20 something",
            "21 something", "22 something", "23 done" };

    private static int countHighPriority = 0;

    private static int countLowPriority = 0;

    private static final Logger LOG = Logger.getLogger(ExtractionThreadPoolExecutor.class);

    // ExecutorService executor = null;
    List<TestExtractionThreadPoolExecutor> threadPoolExecutors = new ArrayList<TestExtractionThreadPoolExecutor>(NUM_THREADPOOLS);

    public static List<ExtractionParameters> getAvisoRequests() {
        List<ExtractionParameters> list = new ArrayList<ExtractionParameters>();
        String productId = "dt_upd_med_merged_madt_uv";
        // String productId = "blablabla";
        String serviceName = "aviso";

        String locationData = "http://opendap.aviso.oceanobs.com/thredds/dodsC/" + productId;
        // String locationData = null;
        productId = null;

        List<String> listVar = new ArrayList<String>();
        // listVar.add("Grid_0001");
        listVar.add("surface_northward_geostrophic_sea_water_velocity");

        List<String> listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("2007-05-23");
        listTemporalCoverage.add("2007-05-23");

        List<String> listLatLonCoverage = new ArrayList<String>();
        // listLatLonCoverage.add("46");
        // listLatLonCoverage.add("-20");
        // listLatLonCoverage.add("30");
        // listLatLonCoverage.add("-10");

        List<String> listDepthCoverage = null;

        // ExtractionParameters extractionParameters = new ExtractionParameters(
        // serviceName,
        // listVar,
        // listTemporalCoverage,
        // listLatLonCoverage,
        // listDepthCoverage,
        // productId,
        // Organizer.Format.NETCDF,
        // null,
        // null,
        // "login",
        // false);
        ExtractionParameters extractionParameters = new ExtractionParameters(
                serviceName,
                locationData,
                listVar,
                listTemporalCoverage,
                listLatLonCoverage,
                listDepthCoverage,
                productId,
                Organizer.Format.NETCDF,
                null,
                null,
                "login",
                true);

        list.add(extractionParameters);
        // ------------------------------------------
        listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("2001-05-20");
        listTemporalCoverage.add("2007-10-23");
        extractionParameters = new ExtractionParameters(
                serviceName,
                locationData,
                listVar,
                listTemporalCoverage,
                listLatLonCoverage,
                listDepthCoverage,
                productId,
                Organizer.Format.NETCDF,
                null,
                null,
                "login",
                true);
        list.add(extractionParameters);
        // ------------------------------------------
        listTemporalCoverage = new ArrayList<String>();
        listTemporalCoverage.add("1992-05-20");
        listTemporalCoverage.add("1998-10-23");
        extractionParameters = new ExtractionParameters(
                serviceName,
                locationData,
                listVar,
                listTemporalCoverage,
                listLatLonCoverage,
                listDepthCoverage,
                productId,
                Organizer.Format.NETCDF,
                null,
                null,
                "login",
                true);
        list.add(extractionParameters);

        return list;

    }

    /**
     * .
     * 
     * @param args
     */
    public static void main(String[] args) {
        // testCallable()
        // testRunnable();

        // TestTheadPools t = new TestTheadPools();
        // t.testPriorityBlockingQueueProblem();
        // t.testPriorityBlockingQueueOK();
        // t.testPriorityBlockingQueue();

        testXStream();

        // TestLog4J testLog4J = new TestLog4J(1, 10, null, null);
        // testLog4J.setEnded();

    }

    public static void testCallable() {

        Callable<Integer> callable = new CallableImpl(18);
        Callable<Integer> callableB = new CallableImpl(3);
        Callable<Integer> callableC = new CallableImpl(23);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> futureC = executor.submit(callableC);
        Future<Integer> future = executor.submit(callable);
        Future<Integer> futureB = executor.submit(callableB);

        try {
            System.out.println("Future value: " + future.get());
            System.out.println("Future value: " + futureB.get());
            System.out.println("Future value: " + futureC.get());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void testRunnable() {

        /*
         * create a thread pool with four threads
         */

        ExecutorService execSvc = Executors.newFixedThreadPool(4);

        /*
         * place six tasks in the work queue for the thread pool
         */

        for (int i = 0; i < 6; i++) {
            execSvc.execute(new CountDown());
        }

        /*
         * prevent other tasks from being added to the queue
         */
        execSvc.shutdown();
    }

    public static void testXStream() {
        List<ExtractionParameters> listRequest = getAvisoRequests();

        XStream xstream = new XStream();
        TestThreadPools testTheadPools = new TestThreadPools();
        DateT date = testTheadPools.new DateT();
        date.year = 2004;
        date.month = 8;
        date.day = 15;

        xstream.alias("date", DateT.class);

        String decl = "\n";

        String xml = xstream.toXML(date);
        System.out.println(decl + xml);
        if (LOG.isInfoEnabled()) {
            LOG.info(xml);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(outputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        xstream.alias("myString", String.class);

        // String str = "\nÚte_max%dfsdfÛ&dsf%";

        String str = "éàù";

        xml = xstream.toXML(str);
        System.out.println(decl + xml);
        if (LOG.isInfoEnabled()) {
            LOG.info(xml);
        }
        xstream.toXML(str, writer);
//        try {
            xml = outputStream.toString();
//        } catch (UnsupportedEncodingException e) {
//            // Do nothing
//        }

        System.out.println(decl + xml);
        if (LOG.isInfoEnabled()) {
            LOG.info(xml);
        }

        Calendar cal = Calendar.getInstance();
        QueueLogError queueLogError = new QueueLogError(ErrorType.EXCEEDING_CAPACITY, new MotuExceedingQueueCapacityException(512).notifyException());

        QueueLogInfo queueLogInfo = new QueueLogInfo();
        queueLogInfo.setInQueueTime(cal.getTime());
        queueLogInfo.setEndTime(new Date(cal.getTime().getTime() + 500));
        queueLogInfo.setAmountDataSize(123.36);
        queueLogInfo.setExtractionParameters(listRequest.get(0));

        queueLogInfo.setQueueLogError(queueLogError);

        // cal.set (1970,0,1);
        // long ageInDays = (System.currentTimeMillis() - cal.getTime().getTime())/(1000*3600*24);
        // System.out.println(ageInDays);
        // System.out.println(cal.getTime());
        //
        // xstream.alias("motuQueueServerLog", QueueLogInfo.class);
        // xstream.useAttributeFor(QueueLogInfo.class, "elapseRunTime");
        // //xstream.registerConverter(new XStreamDateConverter());
        // xml = xstream.toXML(queueLogInfo);
        // System.out.println(queueLogInfo.toXML());
        if (LOG.isInfoEnabled()) {
            LOG.info(queueLogInfo);
        }

    }

    public void testPriorityBlockingQueue() {

        Organizer organizer = null;
        try {
            organizer = new Organizer();
        } catch (MotuException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }

        RequestManagement requestManagement = null;
        // QueueServerManagement queueServerManagement = null;
        try {
            requestManagement = RequestManagement.getInstance();
        } catch (MotuException e) {
            System.out.println(e.notifyException());
            e.printStackTrace();
            if (requestManagement != null) {
                try {
                    requestManagement.shutdown();
                } catch (MotuException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            return;
        }

        // non trie
        // Set<QueueType> queuesConfig = queueServerManagement.queueManagementKeySet();

        // trié
        List<QueueType> queuesConfig = requestManagement.getQueueServerManagement().getQueuesConfig();

        for (QueueType queue : queuesConfig) {

            System.out.println(queue.getDescription());
            System.out.print("queue.getDataThreshold()");
            System.out.println(queue.getDataThreshold());
            System.out.print("queue.getMaxThreads()");
            System.out.println(queue.getMaxThreads());
            System.out.print("queue.getLowPriorityWaiting()");
            System.out.println(queue.getLowPriorityWaiting());
            System.out.print("queue.getMaxPoolSize()");
            System.out.println(queue.getMaxPoolSize());
        }
        List<ExtractionParameters> listRequest = getAvisoRequests();
        List<TestThreadPools.Client> listClient = new ArrayList<TestThreadPools.Client>();
        int i = 0;
        for (ExtractionParameters extractionParameters : listRequest) {
            i++;

            RunnableExtraction runnableExtraction = new RunnableExtraction(
                    requestManagement.getQueueServerManagement().getDefaultPriority(),
                    organizer,
                    extractionParameters);
            runnableExtraction.setBatchQueue(true);
            listClient.add(this.new Client(String.format("Client%d", i), requestManagement.getQueueServerManagement(), runnableExtraction));
            // try {
            // queueServerManagement.execute(runnableExtraction);
            // } catch (MotuExceptionBase e) {
            // runnableExtraction.setError(e);
            // System.out.println(e.notifyException());
            // }
        }

        for (TestThreadPools.Client client : listClient) {
            client.start();
        }
        try {
            Thread.sleep(180000);
            requestManagement.shutdown();
        } catch (MotuException e) {
            System.out.println(e.notifyException());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testPriorityBlockingQueueOK() {
        // Comparator<? super Runnable> cmp = (Comparator<? super Runnable>) new MyComparator();
        // MyComparator cmp = new MyComparator();

        QueueServerType queueServer = null;
        try {
            queueServer = Organizer.getMotuConfigInstance().getQueueServerConfig();
        } catch (MotuException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        List<QueueType> queues = queueServer.getQueues();
        QueueType queueConfig = queues.get(0);
        System.out.print("USING ");
        System.out.println(queueConfig.getDescription());

        int maxRunningThreads = queueConfig.getMaxThreads();

        // PriorityBlockingQueue<Runnable> q = new PriorityBlockingQueue<Runnable>(11, cmp);
        PriorityBlockingQueue<Runnable> q = new PriorityBlockingQueue<Runnable>(10);
        // ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(maxRunningThreads,
        // maxRunningThreads, 0l, TimeUnit.SECONDS, q);
        TestExtractionThreadPoolExecutor threadPoolExecutor = new TestExtractionThreadPoolExecutor(
                maxRunningThreads,
                maxRunningThreads,
                0l,
                TimeUnit.SECONDS,
                q);
        threadPoolExecutors.add(0, threadPoolExecutor);

        // ExecutePoolThread[] exThread = { this.new ExecutePoolThread(threadPoolExecutor),};
        //        
        // for (int i = 0; i < exThread.length; i++) {
        // exThread[i].start();
        // }

        String[] input = arr3;
        for (int i = 0; i < input.length; i++) {
            threadPoolExecutor.pause();
            try {
                String text = input[i];
                // int priority = i % 2;
                int priority = 2;
                if ((i + 1) % 2 == 0) {
                    priority = 1;
                }

                int range = 0;
                synchronized (this) {
                    if (priority == 1) {
                        countHighPriority++;
                        range = countHighPriority;
                    }
                    if (priority == 2) {
                        countLowPriority++;
                        range = countLowPriority;
                    }

                }
                RunnableSomething runSome = new RunnableSomething(text, priority, range);

                int poolSize = threadPoolExecutor.getPoolSize();
                int corePoolSize = threadPoolExecutor.getCorePoolSize();
                int queueSize = q.size();
                int queueSize2 = ((PriorityBlockingQueue<Runnable>) threadPoolExecutor.getQueue()).size();
                System.out.print("corePoolSize : ");
                System.out.print(corePoolSize);
                System.out.print(" poolSize : ");
                System.out.print(poolSize);
                System.out.print(" queueSize : ");
                System.out.print(queueSize);
                System.out.print(" queueSize2 : ");
                System.out.println(queueSize2);

                int maxPoolSize = queueConfig.getMaxPoolSize();
                if (maxPoolSize > 0) {
                    if (queueSize < maxPoolSize) {
                        // q.offer(runSome);

                        threadPoolExecutor.execute(runSome);
                    }
                } else {
                    threadPoolExecutor.execute(runSome);
                }
                threadPoolExecutor.resume();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Object[] o = q.toArray();
        // o.toString();
        // for (int i = 0; i < o.length; i++) {
        // RunnableSomething x = (RunnableSomething) o[i];
        // System.out.print(i);
        // System.out.print(" ");
        // System.out.println(x.thePriority);
        //
        // }
        // RunnableSomething[] o = q.toArray(new RunnableSomething[20]);
        // o.toString();
        // for (int i = 0; i < o.length; i++) {
        // RunnableSomething x = o[i];
        // System.out.print(i);
        // System.out.print(" ");
        // System.out.print(x.thePriority);
        // System.out.print(" ");
        // System.out.println(x.theText);
        // }

        // int i = 0;
        // while (q.peek() != null) {
        //
        // RunnableSomething x = (RunnableSomething) q.poll();
        // System.out.print(i);
        // System.out.print(" ");
        // System.out.print(x.thePriority);
        // System.out.print(" ");
        // System.out.print(x.theRange);
        // System.out.print(" ");
        // System.out.println(x.theText);
        // i++;
        // }

        // while (q.peek() != null){
        // Runnable r = q.poll();
        // if (r != null)
        // executor.execute(r);
        // }
        try {
            Thread.sleep(500);
            int queueSize = q.size();
            System.out.print(" queueSize : ");
            System.out.println(queueSize);
            threadPoolExecutor.shutdown();
            System.out.println("OKOKOKOKOKOK shutdown");
            while (!threadPoolExecutor.isTerminated()) {
                queueSize = q.size();
                System.out.print(" queueSize : ");
                System.out.println(queueSize);
                threadPoolExecutor.awaitTermination(1, TimeUnit.SECONDS);
                System.out.println("OKOKOKOKOKOK awaitTermination");
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        System.out.print("OKOKOKOKOKOK isTerminated : ");
        System.out.println(threadPoolExecutor.isTerminated());
    }

    public void testPriorityBlockingQueueProblem() {
        PriorityBlockingQueue<Runnable> q = new PriorityBlockingQueue<Runnable>(11);
        ExecutorService executor = new ThreadPoolExecutor(5, 5, 120l, TimeUnit.SECONDS, q);

        String[] input = arr3;
        for (int i = 0; i < input.length; i++) {
            try {
                String text = input[i];
                // int priority = i%2;
                int priority = 1;
                if (i > 4) {
                    priority = 0;
                }

                CallableSomething callSome = new CallableSomething(text, priority);
                executor.submit(callSome);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("OKOKOKOKOKOK");
            executor.shutdown();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
