package fr.cls.atoll.motu.library.misc.data;

import java.util.Comparator;

/**
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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
