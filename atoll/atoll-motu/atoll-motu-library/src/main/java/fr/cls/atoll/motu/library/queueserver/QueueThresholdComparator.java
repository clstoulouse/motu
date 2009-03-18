package fr.cls.atoll.motu.library.queueserver;

import java.util.Comparator;

import fr.cls.atoll.motu.library.configuration.QueueType;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */
public class QueueThresholdComparator implements Comparator<QueueType> {

    /**
     * Constructeur.
     */
    public QueueThresholdComparator() {
    }

    /** {@inheritDoc} */
    public int compare(QueueType o1, QueueType o2) {
        if (o1.getDataThreshold() > o2.getDataThreshold()) {
            return 1;
        }
        if (o1.getDataThreshold() < o2.getDataThreshold()) {
            return -1;
        }
        return 0;
    }

}
