/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.library.cas;

import fr.cls.atoll.motu.api.message.AuthenticationMode;
import fr.cls.atoll.motu.library.cas.exception.MotuCasException;
import fr.cls.atoll.motu.library.cas.util.RestUtil;

/**
 * User class.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class UserBase {

    /**
     * Default constructor.
     */
    public UserBase() {
    }

    /**
     * FirstName of the user.
     * 
     * @uml.property name="firstName"
     */
    private String firstName = "";

    /**
     * Getter of the property <tt>firstName</tt>.
     * 
     * @return Returns the firstName.
     * @uml.property name="firstName"
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Setter of the property <tt>firstName</tt>.
     * 
     * @param firstName The firstName to set.
     * @uml.property name="firstName"
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * LastName of the user.
     * 
     * @uml.property name="lastName"
     */
    private String lastName = "";

    /**
     * Getter of the property <tt>lastName</tt>.
     * 
     * @return Returns the lastName.
     * @uml.property name="lastName"
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Setter of the property <tt>lastName</tt>.
     * 
     * @param lastName The lastName to set.
     * @uml.property name="lastName"
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // CSOFF: StrictDuplicateCode : normal duplication code.

    /**
     * Email adress of the user.
     * 
     * @uml.property name="email"
     */
    private String email = "";

    /**
     * Getter of the property <tt>email</tt>.
     * 
     * @return Returns the email.
     * @uml.property name="email"
     */
    public String getEmail() {
        return this.email;
    }

    // CSON: StrictDuplicateCode

    /**
     * Setter of the property <tt>email</tt>.
     * 
     * @param email The email to set.
     * @uml.property name="email"
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /** The login. */
    private String login = "";

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        if (login != null) {
            this.login = login;
        } else {
            this.login = "";
        }
    }

    /** The pwd. */
    private String pwd = "";

    /**
     * Gets the pwd.
     * 
     * @return the pwd
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * Sets the pwd.
     * 
     * @param pwd the new pwd
     */
    public void setPwd(String pwd) {
        if (pwd != null) {
            this.pwd = pwd;
        } else {
            this.pwd = "";
        }
    }

    /** The cas authentification. */
    private AuthenticationMode authentificationMode = AuthenticationMode.NONE;

    /**
     * Gets the authentification mode.
     * 
     * @return the authentification mode
     */
    public AuthenticationMode getAuthentificationMode() {
        return authentificationMode;
    }

    /**
     * Sets the authentification mode.
     * 
     * @param authentificationMode the new authentification mode
     */
    public void setAuthentificationMode(AuthenticationMode authentificationMode) {
        if (authentificationMode != null) {
            this.authentificationMode = authentificationMode;
        } else {
            this.authentificationMode = AuthenticationMode.NONE;
        }
    }

    /**
     * Sets the authentification mode.
     * 
     * @param authentificationMode the new authentification mode
     * 
     * @throws MotuCasException the motu exception
     */
    public void setAuthentificationMode(String authentificationMode) throws MotuCasException {

        if (RestUtil.isNullOrEmpty(authentificationMode)) {
            this.authentificationMode = AuthenticationMode.NONE;
            return;
        }

        try {
            this.authentificationMode = AuthenticationMode.fromValue(authentificationMode);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new MotuCasException(String.format("Invalid authentification mode '%s'. Valid values are: %s",
                                                  authentificationMode,
                                                  AuthenticationMode.getAvailableValues()));
        }
    }
    
    /**
     * Checks if is basic authentification.
     *
     * @return true, if is basic authentification
     */
    public boolean isBasicAuthentification() {
        return this.authentificationMode.equals(AuthenticationMode.BASIC);
    }

    /**
     * Checks if is cas authentification.
     * 
     * @return true, if is cas authentification
     */
    public boolean isCASAuthentification() {
        return this.authentificationMode.equals(AuthenticationMode.CAS);
    }

    /**
     * Checks if is none authentification.
     * 
     * @return true, if is none authentification
     */
    public boolean isNoneAuthentification() {
        return this.authentificationMode.equals(AuthenticationMode.NONE);
    }

    /**
     * Checks if is authentification.
     * 
     * @return true, if is authentification
     */
    public boolean isAuthentification() {
        return !isNoneAuthentification();
    }

    public void setCASAuthentification(boolean casAuthentification) {
        this.authentificationMode = (casAuthentification) ? AuthenticationMode.CAS : AuthenticationMode.NONE;
    }

    /** The cas rest suff url. */
    protected String casRestSuffURL = null;

    /**
     * Gets the cas rest suff url.
     *
     * @return the cas rest suff url
     * @throws MotuCasException the motu cas exception
     */
    public String getCasRestSuffURL() throws MotuCasException {

        return casRestSuffURL;
    }

    /**
     * Sets the cas rest suff url.
     * 
     * @param casRestSuffURL the new cas rest suff url
     */
    public void setCasRestSuffURL(String casRestSuffURL) {
        this.casRestSuffURL = casRestSuffURL;
    }

}
