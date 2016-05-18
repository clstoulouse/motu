package fr.cls.atoll.motu.web.dal.users;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;

import fr.cls.atoll.motu.library.misc.configuration.MotuConfig;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.utils.PropertiesUtilities;

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
            MotuConfig motuConfig = Organizer.getMotuConfigInstance();
            if (motuConfig.getUseAuthentication()) {
                authenticationProps = PropertiesUtilities.loadFromClasspath("motuUser.properties");
            }
        } catch (IOException e) {
            throw new MotuException("Authentication initialisation failure ", e);
        } catch (MotuException e) {
            throw new MotuException(String.format("Authentication initialisation failure - %s", e.notifyException()), e);
        }
    }

    @Override
    public Properties getAuthenticationProps() {
        return authenticationProps;
    }
}
