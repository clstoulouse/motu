package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.CacheTypeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.TokenHTTPParameterValidator;

public class RefreshCacheAction extends AbstractAction {

    public static final String ACTION_NAME = "refreshcache";

    private static final String ALL_CACHE_TO_REFRESH = "ALL";
    private static final String ONLY_AUTO_CACHE_TO_REFRESH = "ONLYAUTO";

    private TokenHTTPParameterValidator passPhraseValidator;
    private CacheTypeHTTPParameterValidator cacheTypeValidator;

    public RefreshCacheAction(String actionCode_, HttpServletRequest request_, HttpServletResponse response_) {
        super(ACTION_NAME, actionCode_, request_, response_);
        passPhraseValidator = new TokenHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_PASS_PHRASE,
                CommonHTTPParameters.getRequestParameterIgnoreCase(getRequest(), MotuRequestParametersConstant.PARAM_PASS_PHRASE));
        cacheTypeValidator = new CacheTypeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_CACHE_TYPE,
                CommonHTTPParameters.getRequestParameterIgnoreCase(getRequest(), MotuRequestParametersConstant.PARAM_CACHE_TYPE));
    }

    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        passPhraseValidator.validate();
        cacheTypeValidator.validate();
    }

    @Override
    protected void process() throws MotuException {
        String passPhrase = passPhraseValidator.getParameterValueValidated();

        if (passPhrase.equals(BLLManager.getInstance().getConfigManager().getMotuConfig().getRefreshCacheToken())) {
            String cacheType = cacheTypeValidator.getParameterValueValidated().toUpperCase();
            if (cacheType.equals(ALL_CACHE_TO_REFRESH)) {
                BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().updateAllTheCache();
            } else if (cacheType.equals(ONLY_AUTO_CACHE_TO_REFRESH)) {
                BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().updateCache();
            } else {
                throw new MotuException(
                        ErrorType.BAD_CACHE_TYPE,
                        StringUtils.getLogMessage(getActionCode(),
                                                  ErrorType.BAD_CACHE_TYPE,
                                                  "The possible values for \"caches\" parameter are [ALL, ONLYAUTO]"));
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

    @Override
    protected void generateParameterString() {
        super.generateParameterString();
        String passPhraseParam = getRequest().getParameter(MotuRequestParametersConstant.PARAM_PASS_PHRASE);
        if (passPhraseParam != null) {
            parameters = parameters.replace(passPhraseParam, "XXX");
        }
    }

}
