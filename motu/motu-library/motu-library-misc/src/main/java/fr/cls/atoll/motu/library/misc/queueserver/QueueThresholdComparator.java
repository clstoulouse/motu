package fr.cls.atoll.motu.library.misc.queueserver;

import fr.cls.atoll.motu.library.misc.configuration.QueueType;

import java.util.Comparator;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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
