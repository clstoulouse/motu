package fr.cls.atoll.motu.web.usl;

import fr.cls.atoll.motu.web.usl.utils.log4j.Log4JInitializer;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class USLManager implements IUSLManager {

    private static IUSLManager s_instance;

    public static IUSLManager getInstance() {
        if (s_instance == null) {
            s_instance = new USLManager();
        }
        return s_instance;
    }

    public USLManager() {

    }

    @Override
    public void init() {
        // Init log4j
        Log4JInitializer.init(null);

    }

}
