package fr.cls.atoll.motu.web.bll.config.comparator;

import java.util.Comparator;

import fr.cls.atoll.motu.web.dal.config.xml.model.CatalogService;

public class CatalogueServiceComparator implements Comparator<CatalogService> {

    @Override
    public int compare(CatalogService o1, CatalogService o2) {
        int compareRes = 0;
        if (o1 != null && o2 != null) {
            if (o1.getName() != null && !o1.getName().equals(o2.getName())) {
                compareRes = 2;
            }
            if (o1.getNcss() != null && !o1.getNcss().equals(o2.getNcss())) {
                compareRes += 20;
            }
            if (o1.getType() != null && !o1.getType().equals(o2.getType())) {
                compareRes += 200;
            }
            if (o1.getUrlSite() != null && !o1.getUrlSite().equals(o2.getUrlSite())) {
                compareRes += 2000;
            }
        } else {
            compareRes = Integer.MIN_VALUE;
        }
        return compareRes;
    }

}
