package fr.cls.atoll.motu.library.misc.threadpools;

/**
 * This class counts down from 7 to 0, printing the task ID and the count value with each iteration. After
 * printing, it then yields to another thread. The thread pool puts it back in the queue and it gets called
 * for the next iteration.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class CountDown implements Runnable {
    protected int count = 8;

    /*
     * the following counter is incremented once each time the class is instantiated, giving each instance a
     * unique number, which is printed in run()
     */
    private static int taskCount = 0;
    private final int id = taskCount++;

    public CountDown() {
    }

    /*
     * print the id and the iteration count to the console, then yield to another thread.
     */
    public void run() {
        while (count-- > 0) {
            System.out.printf("Task %d, count = %d\n", id, count);
            Thread.yield();
        }
    }
}