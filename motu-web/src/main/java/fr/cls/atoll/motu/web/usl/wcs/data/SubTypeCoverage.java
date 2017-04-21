package fr.cls.atoll.motu.web.usl.wcs.data;

import javax.xml.namespace.QName;

public class SubTypeCoverage {
    public static final String GML_NAMES_SPACE = "http://www.opengis.net/wcs/2.0";

    public static final QName GRID_COVERAGE = new QName(GML_NAMES_SPACE, "GridCoverage", "");
    public static final QName ABSTRACT_DISCRETE_COVERAGE = new QName(GML_NAMES_SPACE, "AbstractDiscreteCoverage", "");
    public static final QName ABSTRACT_COVERAGE = new QName(GML_NAMES_SPACE, "AbstractCoverage", "");
}
