package fr.cls.atoll.motu.web.bll.config.updater;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;

public interface IConfigUpdatedListener {

    void onMotuConfigUpdated(MotuConfig newMotuConfig) throws MotuException;
}
