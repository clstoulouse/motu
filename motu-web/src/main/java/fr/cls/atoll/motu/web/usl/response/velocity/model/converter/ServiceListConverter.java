package fr.cls.atoll.motu.web.usl.response.velocity.model.converter;

import java.util.ArrayList;
import java.util.List;

import fr.cls.atoll.motu.library.misc.configuration.ConfigService;
import fr.cls.atoll.motu.web.usl.response.velocity.VelocityTemplateManager;
import fr.cls.atoll.motu.web.usl.response.velocity.model.IService;

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
public class ServiceListConverter {

    public static List<IService> converServiceList(List<ConfigService> cfgList_) {
        List<IService> isList = new ArrayList<IService>(cfgList_.size());
        for (final ConfigService cs : cfgList_) {
            isList.add(new IService() {

                @Override
                public String getNameEncoded() {
                    return VelocityTemplateManager.encodeString(cs.getName());
                }

                @Override
                public String getGroup() {
                    return VelocityTemplateManager.encodeString(cs.getGroup());
                }

                @Override
                public String getDescription() {
                    return VelocityTemplateManager.encodeString(cs.getDescription());
                }

                @Override
                public String getCatalogType() {
                    return VelocityTemplateManager.encodeString(cs.getCatalog().getType());
                }
            });
        }
        return isList;
    }
}
