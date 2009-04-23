package fr.cls.atoll.motu.processor.wps;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-04-23 14:16:09 $
 */
public class RunnableWPS implements Runnable, Comparable<RunnableWPS> {

    /** The range. */
    protected int range = -1;

    public RunnableWPS(int range) {
        this.range = range;

    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(RunnableWPS obj) {
        
        // max range at the top
        int objRange = obj.getRange();
        int retval = 0;
        if (range > objRange) {
            return -1;
        }
        if (range < objRange) {
            return 1;
        }
        if (retval == 0) {
            retval = Integer.valueOf(range).compareTo(Integer.valueOf(obj.getRange()));
        }
        // System.out.println(priority + " compareTo " + obj.priority() + " retval: " + retval);
        return retval;
    }

     public int getRange() {
        return range;
    }

}
