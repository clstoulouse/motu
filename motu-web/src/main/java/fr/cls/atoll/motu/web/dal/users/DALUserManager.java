package fr.cls.atoll.motu.web.dal.users;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;

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

public class DALUserManager implements IDALUserManager {

    private Properties authenticationProps = null;

    public DALUserManager() {
    }

    /**
     * Inits the authentication.
     * 
     * @throws ServletException the servlet exception
     */
    @Override
    public void init() throws MotuException {
        try {
            MotuConfig motuConfig = BLLManager.getInstance().getConfigManager().getMotuConfig();
            if (motuConfig.getUseAuthentication()) {
                authenticationProps = new Properties();
                InputStream in = getClass().getResourceAsStream("motuUser.properties");
                authenticationProps.load(in);
                in.close();
            }
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, "Authentication initialisation failure ", e);
        }
    }

    @Override
    public Properties getAuthenticationProps() {
        return authenticationProps;
    }
}
