package fr.cls.atoll.motu.web.bll.config;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;

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
public class BLLQueueServerConfigManager implements IBLLQueueServerConfigManager {

    @Override
    public int getRequestDefaultPriority() {
        // TODO SMA => ask to DAL this parameter and do not treat exception here
        try {
            return Organizer.getMotuConfigInstance().getQueueServerConfig().getDefaultPriority();
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
    }
}
