package fr.cls.atoll.motu.web.bll.config.comparator;

import java.util.Comparator;

import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;

public class ConfigServiceComparator implements Comparator<ConfigService> {
    private CatalogueServiceComparator catalogueServiceComparator;

    public ConfigServiceComparator() {
        catalogueServiceComparator = new CatalogueServiceComparator();
    }

    @Override
    public int compare(ConfigService o1, ConfigService o2) {
        int compareRes = 0;
        if (o1 != null && o2 != null) {
            if (!o1.getName().equals(o2.getName())) {
                compareRes = 1;
            }
            if (o1.getProfiles() != null && !o1.getProfiles().equals(o2.getProfiles())) {
                compareRes += 10;
            }
            if (o1.getGroup() != null && !o1.getGroup().equals(o2.getGroup())) {
                compareRes += 100;
            }
            if (o1.getDescription() != null && !o1.getDescription().equals(o2.getDescription())) {
                compareRes += 1000;
            }
            if (!o1.getRefreshCacheAutomaticallyEnabled() == o2.getRefreshCacheAutomaticallyEnabled()) {
                compareRes += 10000;
            }
            compareRes += catalogueServiceComparator.compare(o1.getCatalog(), o2.getCatalog());
        } else {
            compareRes = Integer.MIN_VALUE;
        }
        return compareRes;
    }

}
