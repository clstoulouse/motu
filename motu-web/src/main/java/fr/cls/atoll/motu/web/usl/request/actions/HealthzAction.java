package fr.cls.atoll.motu.web.usl.request.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.catalog.product.cache.CacheRefreshScheduler;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
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
public class HealthzAction extends AbstractAction {

    public static final String ACTION_NAME = "healthz";
    public static final String ACTION_CODE = "020";

    /**
     * Constructeur.
     * 
     */
    public HealthzAction(HttpServletRequest request, HttpServletResponse response) {
        super(ACTION_NAME, ACTION_CODE, request, response);
    }

    @Override
    public void process() throws MotuException {
        try {
            final int status;
            final String response;
            if (CacheRefreshScheduler.getInstance().isCacheRefreshed()) {
                status = HttpStatus.SC_OK;
                response = "Server is ready.";
            } else {
                status = HttpStatus.SC_ACCEPTED;
                response = "Server started and refresh in progress (remaining " + CacheRefreshScheduler.getInstance().getAddedConfigServiceNumber()
                        + " / " + BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService().size() + ").";
            }
            writeResponse(response, HTTPUtils.CONTENT_TYPE_HTML_UTF8);
            getResponse().setStatus(status);
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while preparing response message", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        // No parameter to check
    }

}
