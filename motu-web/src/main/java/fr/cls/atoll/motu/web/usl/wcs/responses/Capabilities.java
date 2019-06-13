package fr.cls.atoll.motu.web.usl.wcs.responses;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import fr.cls.atoll.motu.web.usl.wcs.data.CapabilitiesData;
import net.opengis.ows.v_2_0.CodeType;
import net.opengis.ows.v_2_0.DCP;
import net.opengis.ows.v_2_0.HTTP;
import net.opengis.ows.v_2_0.LanguageStringType;
import net.opengis.ows.v_2_0.Operation;
import net.opengis.ows.v_2_0.OperationsMetadata;
import net.opengis.ows.v_2_0.RequestMethodType;
import net.opengis.ows.v_2_0.ServiceIdentification;
import net.opengis.wcs.v_2_0.CapabilitiesType;
import net.opengis.wcs.v_2_0.ContentsType;
import net.opengis.wcs.v_2_0.CoverageSummaryType;
import net.opengis.wcs.v_2_0.ObjectFactory;
import net.opengis.wcs.v_2_0.ServiceMetadataType;

/**
 * This class defines the method to build the "Capabilities" response on WCS request "GetCapabilities". This
 * class is a singleton class. Use the getInstance method to retrieve an instance of the class. <br>
 * <br>
 * Copyright : Copyright (c) 2017 <br>
 * <br>
 * Company : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1456 $ - $Date: 2011-04-08 18:37:34 +0200 $
 */
public class Capabilities {

    private static Capabilities instance = null;

    private net.opengis.ows.v_2_0.ObjectFactory owsFactory = new net.opengis.ows.v_2_0.ObjectFactory();

    private ObjectFactory wcsFactory = new ObjectFactory();

    private Capabilities() {
    }

    public static Capabilities getInstance() {
        if (instance == null) {
            instance = new Capabilities();
        }
        return instance;
    }

    public String buildResponse(CapabilitiesData data) throws JAXBException {
        CapabilitiesType responseWCS = wcsFactory.createCapabilitiesType();
        responseWCS.setOperationsMetadata(buildOperationMetaData(data.getOperationList(), data.getRequestURL()));
        responseWCS.setContents(buildContent(data.getProductList(), data.getSubTypeList()));
        responseWCS.setVersion(data.getVersion());
        responseWCS.setServiceIdentification(buildServiceIdentification(data
                .getTitle(), data.getAbstractId(), data.getServiceType(), data.getServiceTypeVersion(), data.getProfiles()));
        responseWCS.setServiceMetadata(buildServiceMetadataType(data.getSupportedFormat()));

        JAXBElement<CapabilitiesType> root = wcsFactory.createCapabilities(responseWCS);
        StringWriter sw = new StringWriter();
        Marshaller marshaller = JAXBContext.newInstance(CapabilitiesType.class).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(root, sw);
        return sw.toString();
    }

    public ServiceMetadataType buildServiceMetadataType(List<String> supportedFormat) {
        ServiceMetadataType serviceMetadataType = new ServiceMetadataType();

        serviceMetadataType.setFormatSupported(supportedFormat);

        return serviceMetadataType;
    }

    public ServiceIdentification buildServiceIdentification(String title,
                                                            String abstractId,
                                                            String serviceTypeValue,
                                                            String serviceTypeVersionValue,
                                                            List<String> profiles) {
        ServiceIdentification serviceIdentification = new ServiceIdentification();
        List<LanguageStringType> titleLanguageList = new ArrayList<>();
        titleLanguageList.add(buildLangagueString(title));

        List<LanguageStringType> abstractLanguageList = new ArrayList<>();
        abstractLanguageList.add(buildLangagueString(abstractId));

        CodeType serviceType = new CodeType();
        serviceType.setValue(serviceTypeValue);

        List<String> serviceTypeVersionList = new ArrayList<>();
        serviceTypeVersionList.add(serviceTypeVersionValue);

        serviceIdentification.setTitle(titleLanguageList);
        serviceIdentification.setAbstract(abstractLanguageList);
        serviceIdentification.setServiceType(serviceType);
        serviceIdentification.setServiceTypeVersion(serviceTypeVersionList);
        serviceIdentification.setProfile(profiles);
        return serviceIdentification;
    }

    public LanguageStringType buildLangagueString(String strValue) {
        LanguageStringType languageStringtype = new LanguageStringType();
        languageStringtype.setValue(strValue);

        return languageStringtype;
    }

    private OperationsMetadata buildOperationMetaData(List<String> operationList, String requestURL) {
        OperationsMetadata operationsMetadata = new OperationsMetadata();
        List<Operation> listOfOperation = new ArrayList<>();
        for (String operation : operationList) {
            listOfOperation.add(buildOperation(operation, requestURL));
        }
        operationsMetadata.setOperation(listOfOperation);

        return operationsMetadata;
    }

    private Operation buildOperation(String operationName, String requestURL) {
        Operation operation = new Operation();
        operation.setName(operationName);

        List<DCP> dcpList = new ArrayList<>();
        dcpList.add(buildDCP(requestURL));
        operation.setDCP(dcpList);

        return operation;
    }

    private DCP buildDCP(String requestURL) {
        RequestMethodType newGetRequestMethodType = new RequestMethodType();
        newGetRequestMethodType.setHref(requestURL);

        RequestMethodType newPostRequestMethodType = new RequestMethodType();
        newPostRequestMethodType.setHref(requestURL);

        List<JAXBElement<RequestMethodType>> requestMethodTypeList = new ArrayList<>();
        requestMethodTypeList.add(owsFactory.createHTTPGet(newGetRequestMethodType));
        // requestMethodTypeList.add(owsFactory.createHTTPPost(newPostRequestMethodType));

        HTTP newHTTP = new HTTP();
        newHTTP.setGetOrPost(requestMethodTypeList);

        DCP newDCP = new DCP();
        newDCP.setHTTP(newHTTP);

        return newDCP;
    }

    private ContentsType buildContent(List<String> productList, List<QName> subTypeList) {
        ContentsType contents = new ContentsType();
        wcsFactory.createContents(contents);
        // contentsType.
        List<CoverageSummaryType> coverageSummaryType = new ArrayList<>();
        for (int i = 0; i < productList.size(); i++) {
            coverageSummaryType.add(buildCoverageSummary(productList.get(i), subTypeList.get(i)));
        }

        contents.setCoverageSummary(coverageSummaryType);
        return contents;
    }

    private CoverageSummaryType buildCoverageSummary(String productId, QName subTypeName) {
        CoverageSummaryType coverageSummary = new CoverageSummaryType();

        coverageSummary.setCoverageId(productId);
        coverageSummary.setCoverageSubtype(subTypeName);
        // coverageSummary.setCoverageSubtypeParent(buildCoverageSubParentType(subTypeName));

        return coverageSummary;
    }

    // private CoverageSubtypeParentType buildCoverageSubParentType(QName subTypeName) throws
    // IllegalArgumentException {
    // CoverageSubtypeParentType result = new CoverageSubtypeParentType();
    // if (subTypeName.equals(SubTypeCoverage.GRID_COVERAGE)) {
    // result.setCoverageSubtype(SubTypeCoverage.ABSTRACT_DISCRETE_COVERAGE);
    // result.setCoverageSubtypeParent(buildCoverageSubParentType(SubTypeCoverage.ABSTRACT_DISCRETE_COVERAGE));
    // } else if (subTypeName.equals(SubTypeCoverage.ABSTRACT_DISCRETE_COVERAGE)) {
    // result.setCoverageSubtype(SubTypeCoverage.ABSTRACT_COVERAGE);
    // } else {
    // throw new IllegalArgumentException("The provided subtypename is not managed by this method");
    // }
    //
    // return result;
    // }
}
