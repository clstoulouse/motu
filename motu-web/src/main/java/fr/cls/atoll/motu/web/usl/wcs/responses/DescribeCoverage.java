package fr.cls.atoll.motu.web.usl.wcs.responses;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import fr.cls.atoll.motu.web.usl.wcs.data.DescribeCoveragesData;
import net.opengis.gml.v_3_2_1.BoundingShapeType;
import net.opengis.gml.v_3_2_1.DirectPositionType;
import net.opengis.gml.v_3_2_1.DomainSetType;
import net.opengis.gml.v_3_2_1.EnvelopeType;
import net.opengis.gml.v_3_2_1.GridEnvelopeType;
import net.opengis.gml.v_3_2_1.GridLimitsType;
import net.opengis.gml.v_3_2_1.GridType;
import net.opengis.swecommon.v_2_0.DataRecordPropertyType;
import net.opengis.swecommon.v_2_0.DataRecordType;
import net.opengis.swecommon.v_2_0.DataRecordType.Field;
import net.opengis.swecommon.v_2_0.QuantityType;
import net.opengis.swecommon.v_2_0.UnitReference;
import net.opengis.wcs.v_2_0.CoverageDescriptionType;
import net.opengis.wcs.v_2_0.CoverageDescriptionsType;
import net.opengis.wcs.v_2_0.ObjectFactory;

public class DescribeCoverage {

    private static DescribeCoverage instance = null;

    private ObjectFactory wcsFactory = new ObjectFactory();

    private net.opengis.gml.v_3_2_1.ObjectFactory gmlFactory = new net.opengis.gml.v_3_2_1.ObjectFactory();

    private net.opengis.swecommon.v_2_0.ObjectFactory sweFactory = new net.opengis.swecommon.v_2_0.ObjectFactory();

    private DescribeCoverage() {
    }

    public static DescribeCoverage getInstance() {
        if (instance == null) {
            instance = new DescribeCoverage();
        }

        return instance;
    }

    public String buildResponse(DescribeCoveragesData data) throws JAXBException {
        CoverageDescriptionsType coverageDescriptions = new CoverageDescriptionsType();

        List<CoverageDescriptionType> listOfCoverageDescription = new ArrayList<>();
        for (fr.cls.atoll.motu.web.usl.wcs.data.DescribeCoverageData currentCoverage : data.getCoverageDescriptions()) {
            listOfCoverageDescription.add(buildCoverageDescription(currentCoverage));
        }
        coverageDescriptions.setCoverageDescription(listOfCoverageDescription);

        JAXBElement<CoverageDescriptionsType> root = wcsFactory.createCoverageDescriptions(coverageDescriptions);
        StringWriter sw = new StringWriter();
        Marshaller marshaller = JAXBContext.newInstance(CoverageDescriptionsType.class).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(root, sw);
        return sw.toString();
    }

    private CoverageDescriptionType buildCoverageDescription(fr.cls.atoll.motu.web.usl.wcs.data.DescribeCoverageData coverageDescription) {
        CoverageDescriptionType result = new CoverageDescriptionType();

        result.setId(coverageDescription.getCoverageId());
        result.setCoverageId(coverageDescription.getCoverageId());
        result.setBoundedBy(buildBoundedBy(coverageDescription.getAxisLabels(),
                                           coverageDescription.getUomLabels(),
                                           coverageDescription.getLowerCorner(),
                                           coverageDescription.getUpperCorner()));
        result.setDomainSet(buildDomainSet(coverageDescription.getAxisLabels(),
                                           coverageDescription.getUomLabels(),
                                           coverageDescription.getLowerValues(),
                                           coverageDescription.getUpperValues(),
                                           coverageDescription.getDimension(),
                                           coverageDescription.getGridId()));
        result.setRangeType(buildRange(coverageDescription.getFieldNames(), coverageDescription.getFieldUoms()));

        return result;
    }

    private BoundingShapeType buildBoundedBy(List<String> axisLabels, List<String> uomLabels, List<Double> lowerCorners, List<Double> upperCorners) {
        DirectPositionType lower = new DirectPositionType();
        lower.setValue(lowerCorners);

        DirectPositionType upper = new DirectPositionType();
        upper.setValue(upperCorners);

        EnvelopeType envelope = new EnvelopeType();
        envelope.setAxisLabels(axisLabels);
        envelope.setUomLabels(uomLabels);
        envelope.setLowerCorner(lower);
        envelope.setUpperCorner(upper);

        JAXBElement<EnvelopeType> jaxbEnvelope = gmlFactory.createEnvelope(envelope);
        BoundingShapeType boundedBy = new BoundingShapeType();
        boundedBy.setEnvelope(jaxbEnvelope);

        return boundedBy;
    }

    private JAXBElement<DomainSetType> buildDomainSet(List<String> axisLabels,
                                                      List<String> uomLabels,
                                                      List<BigInteger> lowerValues,
                                                      List<BigInteger> upperValues,
                                                      BigInteger dimension,
                                                      String gridId) {
        GridEnvelopeType envelop = new GridEnvelopeType();
        envelop.setLow(lowerValues);
        envelop.setHigh(upperValues);

        GridLimitsType gridlimit = new GridLimitsType();
        gridlimit.setGridEnvelope(envelop);

        GridType grid = new GridType();
        grid.setAxisLabels(axisLabels);
        grid.setUomLabels(uomLabels);
        grid.setLimits(gridlimit);
        grid.setDimension(dimension);
        grid.setId(gridId);

        DomainSetType domainSet = new DomainSetType();
        domainSet.setAbstractGeometry(gmlFactory.createGrid(grid));

        return gmlFactory.createDomainSet(domainSet);
    }

    private DataRecordPropertyType buildRange(List<String> fieldNames, List<String> uomCode) {
        List<Field> listOfField = new ArrayList<>();

        for (int i = 0; i < fieldNames.size(); i++) {
            UnitReference unitReference = new UnitReference();
            unitReference.setCode(uomCode.get(i));

            QuantityType quantity = new QuantityType();
            quantity.setUom(unitReference);

            Field currentField = new Field();
            currentField.setName(fieldNames.get(i));
            currentField.setAbstractDataComponent(sweFactory.createQuantity(quantity));
            listOfField.add(currentField);
        }

        DataRecordType dataRecord = new DataRecordType();
        dataRecord.setField(listOfField);

        DataRecordPropertyType range = new DataRecordPropertyType();
        range.setDataRecord(dataRecord);

        return range;
    }

}
