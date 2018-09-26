package fr.cls.atoll.motu.web.usl.wcs.request.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
import fr.cls.atoll.motu.web.usl.request.actions.AbstractAction;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.wcs.data.CapabilitiesData;
import fr.cls.atoll.motu.web.usl.wcs.data.SubTypeCoverage;
import fr.cls.atoll.motu.web.usl.wcs.exceptions.ActionException;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.WCSHTTPParameters;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.AcceptVersionsHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.RequestHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.responses.Capabilities;

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
public class WCSGetCapabilitiesAction extends AbstractAction {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "GetCapabilities";

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;
    private AcceptVersionsHTTPParameterValidator acceptVersionsHTTPParameterValidator;
    private RequestHTTPParameterValidator requestHTTPParameterValidator;

    /**
     * Constructeur.
     * 
     * @param actionName_
     * @param actionCode_
     * @param request_
     * @param response_
     */
    public WCSGetCapabilitiesAction(String actionCode_, HttpServletRequest request_, HttpServletResponse response_) {
        super(ACTION_NAME, actionCode_, request_, response_);
        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                WCSHTTPParameters.SERVICE,
                WCSHTTPParameters.getServiceFromRequest(getRequest()));
        acceptVersionsHTTPParameterValidator = new AcceptVersionsHTTPParameterValidator(
                WCSHTTPParameters.ACCEPT_VERSIONS,
                WCSHTTPParameters.getAcceptVersionsFromRequest(getRequest()));
        requestHTTPParameterValidator = new RequestHTTPParameterValidator(
                WCSHTTPParameters.REQUEST,
                WCSHTTPParameters.getRequestFromRequest(getRequest()));
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        serviceHTTPParameterValidator.validate();
        acceptVersionsHTTPParameterValidator.validate();
        requestHTTPParameterValidator.validate();
    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws MotuException {
        // String service = serviceHTTPParameterValidator.getParameterValueValidated();
        // String acceptVersions = acceptVersionsHTTPParameterValidator.getParameterValueValidated();
        // String request = requestHTTPParameterValidator.getParameterValueValidated();
        try {
            try {
                String xmlResponses = Capabilities.getInstance().buildResponse(buildCapabilitiesData());
                writeResponse(xmlResponses, HTTPUtils.CONTENT_TYPE_XML_UTF8);
            } catch (JAXBException | IOException e) {
                throw new ActionException(e);
            }
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while writing response", e);
        }
    }

    private CapabilitiesData buildCapabilitiesData() {
        List<String> productList = new ArrayList<>();
        List<QName> subTypeList = new ArrayList<>();
        MotuConfig mc = BLLManager.getInstance().getConfigManager().getMotuConfig();
        List<ConfigService> configService = mc.getConfigService();
        for (ConfigService cs : configService) {
            CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().getCatalogCache()
                    .getCatalog(cs.getName());
            if (cd == null) {
                LOGGER.error("[WCS GetCapabilities] Catalog is null for configService=" + cs.getName());
            } else {
                Map<String, Product> mapOfProduct = cd.getProducts();
                for (Map.Entry<String, Product> currentProduct : mapOfProduct.entrySet()) {
                    productList.add(cs.getName() + "@" + currentProduct.getValue().getProductId());
                    subTypeList.add(SubTypeCoverage.GRID_COVERAGE);
                }
            }
        }
        List<String> profiles = new ArrayList<>();
        profiles.add("http://www.opengis.net/spec/WCS/2.0/conf/core");
        profiles.add("http://www.opengis.net/spec/WCS_protocol-binding_get-kvp/1.0/conf/get-kvp");

        List<String> supportedFormats = new ArrayList<>();
        supportedFormats.add("application/netcdf");

        List<String> operationList = new ArrayList<>();
        operationList.add("GetCapabilities");
        operationList.add("DescribeCoverage");
        operationList.add("GetCoverage");
        CapabilitiesData currentCapabilitiesData = new CapabilitiesData();
        currentCapabilitiesData.setVersion("2.0.1");
        currentCapabilitiesData.setTitle("Motu");
        currentCapabilitiesData.setAbstractId("Motu WCS service");
        currentCapabilitiesData.setServiceType("OGC WCS");
        currentCapabilitiesData.setServiceTypeVersion("2.0.1");
        currentCapabilitiesData.setProfiles(profiles);
        currentCapabilitiesData.setRequestURL(getRequest().getRequestURL().toString());
        currentCapabilitiesData.setProductList(productList);
        currentCapabilitiesData.setSubTypeList(subTypeList);
        currentCapabilitiesData.setOperationList(operationList);
        currentCapabilitiesData.setSupportedFormat(supportedFormats);

        return currentCapabilitiesData;
    }

}
