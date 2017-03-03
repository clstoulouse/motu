package fr.cls.atoll.motu.web.usl.wcs.data;

import java.util.ArrayList;
import java.util.List;

public class DescribeCoveragesData {
    private List<DescribeCoverageData> coverageDescriptions;

    public DescribeCoveragesData() {
        coverageDescriptions = new ArrayList<>();
    }

    public List<DescribeCoverageData> getCoverageDescriptions() {
        return coverageDescriptions;
    }

    public void setCoverageDescriptions(List<DescribeCoverageData> coverageDescriptions) {
        this.coverageDescriptions = coverageDescriptions;
    }
}
