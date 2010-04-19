package fr.cls.atoll.motu.library.misc.data;

import java.util.Comparator;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-05-27 16:02:50 $
 */
public class DataFileComparator implements Comparator<DataFile> {

    /**
     * Instantiates a new data file comparator.
     */
    public DataFileComparator() {
    }

    /** {@inheritDoc} */
    @Override
    public int compare(DataFile o1, DataFile o2) {
        return o1.getStartCoverageDate().compareTo(o2.getStartCoverageDate());
    }

}
