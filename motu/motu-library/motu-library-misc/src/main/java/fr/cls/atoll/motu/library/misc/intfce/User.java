/**
 * 
 */
package fr.cls.atoll.motu.library.misc.intfce;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.AuthentificationMode;
import fr.cls.atoll.motu.library.misc.exception.MotuException;

/**
 * User class.
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */
public class User {

    /**
     * Defalt constructor.
     */
    public User() {
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
    private AuthentificationMode authentificationMode = AuthentificationMode.NONE;

    /**
     * Gets the authentification mode.
     * 
     * @return the authentification mode
     */
    public AuthentificationMode getAuthentificationMode() {
        return authentificationMode;
    }

    /**
     * Sets the authentification mode.
     * 
     * @param authentificationMode the new authentification mode
     */
    public void setAuthentificationMode(AuthentificationMode authentificationMode) {
        if (authentificationMode != null) {
            this.authentificationMode = authentificationMode;
        } else {
            this.authentificationMode = AuthentificationMode.NONE;
        }
    }

    /**
     * Sets the authentification mode.
     * 
     * @param authentificationMode the new authentification mode
     * 
     * @throws MotuException the motu exception
     */
    public void setAuthentificationMode(String authentificationMode) throws MotuException {

        if (Organizer.isNullOrEmpty(authentificationMode)) {
            this.authentificationMode = AuthentificationMode.NONE;
            return;
        }

        try {
            this.authentificationMode = AuthentificationMode.fromValue(authentificationMode);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new MotuException(String.format("Invalid authentification mode '%s'. Valid values are: %s",
                                                  authentificationMode,
                                                  AuthentificationMode.getAvailableValues()));
        }
    }

    /**
     * Checks if is cas authentification.
     * 
     * @return true, if is cas authentification
     */
    public boolean isCASAuthentification() {
        return this.authentificationMode.equals(AuthentificationMode.CAS);
    }

    /**
     * Checks if is none authentification.
     * 
     * @return true, if is none authentification
     */
    public boolean isNoneAuthentification() {
        return this.authentificationMode.equals(AuthentificationMode.NONE);
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
        this.authentificationMode = (casAuthentification) ? AuthentificationMode.CAS : AuthentificationMode.NONE;
    }

    /** The cas rest suff url. */
    private String casRestSuffURL = null;

    /**
     * Gets the cas rest suff url.
     * 
     * @return the cas rest suff url
     * @throws MotuException
     */
    public String getCasRestSuffURL() throws MotuException {

        if (Organizer.isNullOrEmpty(casRestSuffURL)) {
            return Organizer.getMotuConfigInstance().getCasRestUrlSuffix();
        }

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
