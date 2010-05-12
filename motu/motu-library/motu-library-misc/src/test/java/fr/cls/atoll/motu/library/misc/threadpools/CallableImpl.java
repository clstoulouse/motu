package fr.cls.atoll.motu.library.misc.threadpools;

import java.util.concurrent.Callable;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class CallableImpl implements Callable<Integer> {

    private int myName;

    CallableImpl(int i) {
        myName = i;
    }

    public Integer call() {
        for (int i = 0; i < 10; i++) {
            System.out.println("Thread : " + getMyName() + " I is : " + i);
        }
        return new Integer(getMyName());

    }

    public int getMyName() {
        return myName;
    }

    public void setMyName(int myName) {
        this.myName = myName;
    }

}