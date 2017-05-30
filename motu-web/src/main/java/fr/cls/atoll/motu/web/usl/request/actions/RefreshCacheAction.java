package fr.cls.atoll.motu.web.usl.request.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.PassPhraseHTTPParameterValidator;

public class UpdateCacheAction extends AbstractAction {

    public static final String ACTION_NAME = "updatecache";

    private PassPhraseHTTPParameterValidator passPhraseValidator;

    public UpdateCacheAction(String actionCode_, HttpServletRequest request_, HttpServletResponse response_) {
        super(ACTION_NAME, actionCode_, request_, response_);
        passPhraseValidator = new PassPhraseHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_PASS_PHRASE,
                CommonHTTPParameters.getRequestParameterIgnoreCase(getRequest(), MotuRequestParametersConstant.PARAM_PASS_PHRASE));
    }

    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        passPhraseValidator.validate();
    }

    @Override
    protected void process() throws MotuException {
        String passPhrase = passPhraseValidator.getParameterValueValidated();

        if (passPhrase.equals(BLLManager.getInstance().getConfigManager().getMotuConfig().getUpdateCachePassPhrase())) {

        } else {
            throw new MotuException(
                    ErrorType.WRONG_PASS_PHRASE,
                    StringUtils.getLogMessage(getActionCode(), ErrorType.WRONG_PASS_PHRASE, "The provided passphrase is not correct"));
        }
    }

}
