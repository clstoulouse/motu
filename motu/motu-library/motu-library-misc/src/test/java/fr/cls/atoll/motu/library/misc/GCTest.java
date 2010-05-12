package fr.cls.atoll.motu.library.misc;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class GCTest {

    final int NELEMS = 50000;

    void eatMemory() {

        int[] intArray = new int[NELEMS];

        for (int i = 0; i < NELEMS; i++) {
            intArray[i] = i;
        }

    }

    public static void main(String[] args) {

        GCTest gct = new GCTest();

        // Step 1: get a Runtime object
        Runtime r = Runtime.getRuntime();

        // Step 2: determine the current amount of free memory
        long freeMem = r.freeMemory();
        System.out.println("free memory before creating array: " + freeMem);

        // Step 3: consume some memory
        gct.eatMemory();

        // Step 4: determine amount of memory left after consumption
        freeMem = r.freeMemory();
        System.out.println("free memory after creating array:  " + freeMem);

        // Step 5: run the garbage collector, then check freeMemory
        r.gc();
        freeMem = r.freeMemory();
        System.out.println("free memory after running gc():    " + freeMem);

    }

}