package fr.cls.atoll.motu.web.bll.config;

import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;

public interface IConfigUpdatedListener {

    void onMotuConfigUpdated(MotuConfig newMotuConfig);
}
