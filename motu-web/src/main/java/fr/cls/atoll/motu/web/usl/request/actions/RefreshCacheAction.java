package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ConfigServiceNamesHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.TokenHTTPParameterValidator;

public class RefreshCacheAction extends AbstractAction {

    public static final String ACTION_NAME = "refreshcache";

    private static final String ALL_CACHE_TO_REFRESH = "ALL";
    private static final String ONLY_AUTO_CACHE_TO_REFRESH = "ONLYAUTO";

    private TokenHTTPParameterValidator passPhraseValidator;
    private ConfigServiceNamesHTTPParameterValidator ConfigServiceNamesValidator;

    public RefreshCacheAction(String actionCode_, HttpServletRequest request_, HttpServletResponse response_) {
        super(ACTION_NAME, actionCode_, request_, response_);
        passPhraseValidator = new TokenHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_PASS_PHRASE,
                CommonHTTPParameters.getRequestParameterIgnoreCase(getRequest(), MotuRequestParametersConstant.PARAM_PASS_PHRASE));
        ConfigServiceNamesValidator = new ConfigServiceNamesHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_CONFIG_SERVICE_LIST,
                CommonHTTPParameters.getRequestParameterIgnoreCase(getRequest(), MotuRequestParametersConstant.PARAM_CONFIG_SERVICE_LIST));
    }

    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        passPhraseValidator.validate();
        ConfigServiceNamesValidator.validate();
    }

    @Override
    protected void process() throws MotuException {
        String passPhrase = passPhraseValidator.getParameterValueValidated();

        if (passPhrase.equals(BLLManager.getInstance().getConfigManager().getMotuConfig().getRefreshCacheToken())) {
            String configServiceList = ConfigServiceNamesValidator.getParameterValueValidated();
            if (configServiceList.equalsIgnoreCase(ALL_CACHE_TO_REFRESH)) {
                BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().updateAllTheCache();
            } else if (configServiceList.equalsIgnoreCase(ONLY_AUTO_CACHE_TO_REFRESH)) {
                BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().updateCache();
            } else if (!configServiceList.isEmpty()) {
                String[] listOfConfigService = configServiceList.split(",");
                List<String> badConfigServiceList = new ArrayList<>();
                List<ConfigService> configServiceToRefresh = new ArrayList<>();
                checkConfigServiceList(listOfConfigService, badConfigServiceList, configServiceToRefresh);
                if (badConfigServiceList.isEmpty()) {
                    BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().updateCache(configServiceToRefresh);
                } else {
                    managerBadConfigServiceError(badConfigServiceList);
                }
            } else {
                throw new MotuException(
                        ErrorType.BAD_CACHE_TYPE,
                        StringUtils
                                .getLogMessage(getActionCode(),
                                               ErrorType.BAD_CACHE_TYPE,
                                               "The parameter configServiceNames cannot be empty. The possible values for \"caches\" parameter are [ALL, ONLYAUTO, List of configService separated by comma]"));
            }
            try {
                getResponse().getWriter().append("OK cache refresh in progress");
            } catch (IOException e) {
                throw new MotuException(ErrorType.SYSTEM, e);
            }
        } else {
            throw new MotuException(
                    ErrorType.WRONG_PASS_PHRASE,
                    StringUtils.getLogMessage(getActionCode(), ErrorType.WRONG_PASS_PHRASE, "The provided token is not correct"));
        }
    }

    private void managerBadConfigServiceError(List<String> badConfigServiceList) throws MotuException {
        StringBuilder badConfigServiceMessage = new StringBuilder();
        for (String currentConfigService : badConfigServiceList) {
            badConfigServiceMessage.append(currentConfigService);
            badConfigServiceMessage.append(",");
        }
        if (!badConfigServiceList.isEmpty()) {
            badConfigServiceMessage = new StringBuilder(badConfigServiceMessage.substring(0, badConfigServiceMessage.lastIndexOf(",")));
        }
        throw new MotuException(
                ErrorType.BAD_CACHE_TYPE,
                StringUtils.getLogMessage(getActionCode(), ErrorType.BAD_CACHE_TYPE, badConfigServiceMessage.toString()),
                badConfigServiceMessage.toString());
    }

    private void checkConfigServiceList(String[] listOfConfigService, List<String> badConfigServiceList, List<ConfigService> configServiceToRefresh) {
        if (badConfigServiceList != null && configServiceToRefresh != null) {
            for (String currentConfigServiceName : listOfConfigService) {
                ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(currentConfigServiceName);
                if (cs != null) {
                    configServiceToRefresh.add(cs);
                } else {
                    badConfigServiceList.add(currentConfigServiceName);
                }
            }
        }
    }

    @Override
    protected void generateParameterString() {
        super.generateParameterString();
        String passPhraseParam = getRequest().getParameter(MotuRequestParametersConstant.PARAM_PASS_PHRASE);
        if (passPhraseParam != null) {
            parameters = parameters.replace(passPhraseParam, "XXX");
        }
    }

}
