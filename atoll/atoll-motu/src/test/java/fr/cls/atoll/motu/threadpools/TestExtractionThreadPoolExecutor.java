package fr.cls.atoll.motu.threadpools;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:01:43 $
 */
public class TestExtractionThreadPoolExecutor extends ThreadPoolExecutor {
    private boolean isPaused;
    private ReentrantLock pauseLock = new ReentrantLock();
    private Condition unpaused = pauseLock.newCondition();

    /**
     * Constructeur.
     * 
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     */
    public TestExtractionThreadPoolExecutor(
        int corePoolSize,
        int maximumPoolSize,
        long keepAliveTime,
        TimeUnit unit,
        BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /**
     * Constructeur.
     * 
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param threadFactory
     */
    public TestExtractionThreadPoolExecutor(
        int corePoolSize,
        int maximumPoolSize,
        long keepAliveTime,
        TimeUnit unit,
        BlockingQueue<Runnable> workQueue,
        ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * Constructeur.
     * 
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param handler
     */
    public TestExtractionThreadPoolExecutor(
        int corePoolSize,
        int maximumPoolSize,
        long keepAliveTime,
        TimeUnit unit,
        BlockingQueue<Runnable> workQueue,
        RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    /**
     * Constructeur.
     * 
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param threadFactory
     * @param handler
     */
    public TestExtractionThreadPoolExecutor(
        int corePoolSize,
        int maximumPoolSize,
        long keepAliveTime,
        TimeUnit unit,
        BlockingQueue<Runnable> workQueue,
        ThreadFactory threadFactory,
        RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (r instanceof TestTheadPools.RunnableSomething) {
            TestTheadPools.RunnableSomething r2 = (TestTheadPools.RunnableSomething) r;
            System.out.print("afterExecute ");
            System.out.print(r2.getText());
            System.out.print("r2.getPriority()");
            System.out.print(r2.getPriority());
            System.out.print("r2.getRange()");
            System.out.print(r2.getRange());
            System.out.println("");
        }

        super.afterExecute(r, t);
    }

    // @Override
    // protected void beforeExecute(Thread t, Runnable r) {
    // if (r instanceof TestTheadPools.RunnableSomething) {
    // TestTheadPools.RunnableSomething r2 = (TestTheadPools.RunnableSomething) r;
    // System.out.print("beforeExecute ");
    // System.out.print(r2.getText());
    // System.out.print("r2.getPriority()");
    // System.out.print(r2.getPriority());
    // System.out.print("r2.getRange()");
    // System.out.print(r2.getRange());
    // System.out.println("");
    // }
    // super.beforeExecute(t, r);
    // }

    protected void beforeExecute(Thread t, Runnable r) {
        if (r instanceof TestTheadPools.RunnableSomething) {
            TestTheadPools.RunnableSomething r2 = (TestTheadPools.RunnableSomething) r;
            System.out.print("beforeExecute ");
            System.out.print(r2.getText());
            System.out.print("r2.getPriority()");
            System.out.print(r2.getPriority());
            System.out.print("r2.getRange()");
            System.out.print(r2.getRange());
            System.out.println("");
        }
        super.beforeExecute(t, r);
        pauseLock.lock();
        try {
            while (isPaused) {
                unpaused.await();
            }
        } catch (InterruptedException ie) {
            t.interrupt();
        } finally {
            pauseLock.unlock();
        }
    }

    public void pause() {
        System.out.println("PAUSE ");
        pauseLock.lock();
        try {
            isPaused = true;
        } finally {
            pauseLock.unlock();
        }
    }

    public void resume() {
        System.out.println("RESUME ");
        pauseLock.lock();
        try {
            isPaused = false;
            unpaused.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }

}
