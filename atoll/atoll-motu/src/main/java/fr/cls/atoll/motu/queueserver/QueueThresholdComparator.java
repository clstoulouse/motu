package fr.cls.atoll.motu.queueserver;

import java.util.Comparator;

import fr.cls.atoll.motu.configuration.QueueType;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:26 $
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
