package fr.cls.atoll.motu.web.usl.request.actions;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.catalog.product.cache.CacheRefreshScheduler;
import fr.cls.atoll.motu.web.bll.catalog.product.cache.ConfigServiceState;
import fr.cls.atoll.motu.web.bll.config.version.IBLLVersionManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

/**
 * 
 * . <br>
 * <br>
 * Copyright : Copyright (c) 2019 <br>
 * <br>
 * Company : CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1456 $ - $Date: 2011-04-08 18:37:34 +0200 $
 */
public class CacheStatusAction extends AbstractAction {

    public static final String ACTION_NAME = "cachestatus";

    /**
     * This action build and fill a JSon object with data about caching status:
     * 
     * {cacheStatus: {state: { nbTotal: Z, nbSuccess : X, nbFailure: Y, lastUpdate : ISO8601TimeStamp,
     * lastUpdateDuration:ISO8601Duration }, configServices : [ configServiceName1 : {state : { status :
     * ADDED|SUCESSS|FAILURE, lastUpdate : ISO8601TimeStamp, lastUpdateDuration:ISO8601Duration }, conf : {
     * refreshCacheAutomaticallyEnabled:true|false, type:"tds", ncss:"enabled" }, configServiceName2 : { ....
     * ... } ] }, version : { motu-products: 3.y.y, motu-distribution : 3.xx, motu-configuration : 3.z.z } }
     * 
     * @param actionCode
     * @param request
     * @param response
     */
    public CacheStatusAction(String actionCode, HttpServletRequest request, HttpServletResponse response) {
        super(ACTION_NAME, actionCode, request, response);
    }

    @Override
    public void process() throws MotuException {
        try {
            final String json = Json.createObjectBuilder().add(ACTION_NAME, buildCacheStatus()).add("version", buildVersion()).build().toString();
            writeResponse(json, MediaType.APPLICATION_JSON);
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while preparing response message", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        // No parameter to check
    }

    private JsonObjectBuilder buildCacheStatus() {
        return Json.createObjectBuilder().add("state", buildState()).add("configServices", buildConfigServices());
    }

    private JsonObjectBuilder buildState() {
        return Json.createObjectBuilder().add("nbTotal", BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService().size())
                .add("nbSuccess", CacheRefreshScheduler.getInstance().getConfigServiceRefeshedOK())
                .add("nbFailure", CacheRefreshScheduler.getInstance().getConfigServiceRefeshedKO())
                .add("lastUpdate", prepareString(CacheRefreshScheduler.getLastUpdate()))
                .add("lastUpdateDuration", prepareString(CacheRefreshScheduler.getLastUpdateDuration()));
    }

    private JsonArrayBuilder buildConfigServices() {
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (ConfigService service : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            result.add(Json.createObjectBuilder()
                    .add(service.getName(),
                         Json.createObjectBuilder().add("state", buildConfigServicesState(service)).add("conf", buildConfigServicesConf(service))));
        }
        return result;
    }

    private JsonObjectBuilder buildConfigServicesState(ConfigService service) {
        ConfigServiceState serviceState = CacheRefreshScheduler.getInstance().getConfigServiceState(service.getName());
        return Json.createObjectBuilder().add("status", serviceState.getStatus()).add("lastUpdate", prepareString(serviceState.getLastUpdate()))
                .add("lastUpdateDuration", prepareString(serviceState.getLastUpdateDuration()));
    }

    private JsonObjectBuilder buildConfigServicesConf(ConfigService service) {
        return Json.createObjectBuilder().add("refreshCacheAutomaticallyEnabled", service.getRefreshCacheAutomaticallyEnabled())
                .add("type", prepareString(service.getCatalog().getType())).add("ncss", prepareString(service.getCatalog().getNcss()));
    }

    private JsonObjectBuilder buildVersion() {
        IBLLVersionManager versionManager = BLLManager.getInstance().getConfigManager().getVersionManager();
        return Json.createObjectBuilder().add("motu-products", versionManager.getProductsVersion())
                .add("motu-distribution", versionManager.getDistributionVersion())
                .add("motu-configuration", versionManager.getConfigurationVersion());
    }

    private String prepareString(Object o) {
        return prepareString(o, "");
    }

    private String prepareString(Object o, String defaultStr) {
        if (o == null) {
            return defaultStr;
        } else {
            return o.toString();
        }
    }

}
