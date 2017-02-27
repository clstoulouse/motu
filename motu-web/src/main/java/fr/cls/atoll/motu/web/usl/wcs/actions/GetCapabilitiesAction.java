package fr.cls.atoll.motu.web.usl.wcs.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.wcs.data.CapabilitiesData;
import fr.cls.atoll.motu.web.usl.wcs.data.SubTypeCoverage;
import fr.cls.atoll.motu.web.usl.wcs.exceptions.ActionException;
import fr.cls.atoll.motu.web.usl.wcs.responses.Capabilities;

public class GetCapabilitiesAction extends Action {

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws ActionException {
        List<String> productList = new ArrayList<>();
        List<QName> subTypeList = new ArrayList<>();
        MotuConfig mc = BLLManager.getInstance().getConfigManager().getMotuConfig();
        List<ConfigService> configService = mc.getConfigService();
        for (ConfigService cs : configService) {
            CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().getCatalogCache()
                    .getCatalog(cs.getName());
            Map<String, Product> mapOfProduct = cd.getProducts();
            for (Map.Entry<String, Product> currentProduct : mapOfProduct.entrySet()) {
                productList.add(cs.getName() + "@" + currentProduct.getValue().getProductId());
                subTypeList.add(SubTypeCoverage.GRID_COVERAGE);
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
        currentCapabilitiesData.setRequestURL(request.getRequestURL().toString());
        currentCapabilitiesData.setProductList(productList);
        currentCapabilitiesData.setSubTypeList(subTypeList);
        currentCapabilitiesData.setOperationList(operationList);
        currentCapabilitiesData.setSupportedFormat(supportedFormats);

        String xmlResponses;
        try {
            xmlResponses = Capabilities.getInstance().buildResponse(currentCapabilitiesData);
            response.getWriter().write(xmlResponses);
        } catch (JAXBException | IOException e) {
            throw new ActionException(e);
        }
    }

}
