package fr.cls.atoll.motu.library.misc.threadpools;

import fr.cls.atoll.motu.library.misc.intfce.ExtractionParameters;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.queueserver.RunnableExtraction;

public class TestLog4J extends RunnableExtraction {


    /**
     * Constructeur.
     * @param priority
     * @param range
     * @param organizer
     * @param extractionParameters
     */
    public TestLog4J(int priority, int range, Organizer organizer, ExtractionParameters extractionParameters) {
        super(priority, range, organizer, extractionParameters);
    }

    /**
     * Constructeur.
     * @param priority
     * @param organizer
     * @param extractionParameters
     */
    public TestLog4J(int priority, Organizer organizer, ExtractionParameters extractionParameters) {
        super(priority, organizer, extractionParameters);
    }
    @Override
    public void setEnded() {
        // TODO Auto-generated method stub
        super.setEnded();
    }

}
