/**
 * 
 */
package fr.cls.atoll.motu.intfce;

/**
 * User class.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:26 $
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

}
