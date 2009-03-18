package fr.cls.atoll.motu.library.metadata;

/**
 * Paremeter's catagory. Parameters are groupped by category and discipline.
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 */
public class ParameterCategory {

    /**
     * Default constructor.
     */
    public ParameterCategory() {

    }

    /**
     * @uml.property name="code"
     */
    private String code = "";

    /**
     * Getter of the property <tt>code</tt>.
     * 
     * @return Returns the code.
     * @uml.property name="code"
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter of the property <tt>code</tt>.
     * 
     * @param code The code to set.
     * @uml.property name="code"
     */
    public void setCode(String code) {
        this.code = code;
    }

    // CSOFF: StrictDuplicateCode : normal duplication code.

    /**
     * @uml.property name="label"
     */
    private String label = "";

    /**
     * Getter of the property <tt>label</tt>.
     * 
     * @return Returns the label.
     * @uml.property name="label"
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setter of the property <tt>label</tt>.
     * 
     * @param label The label to set.
     * @uml.property name="label"
     */
    public void setLabel(String label) {
        this.label = label;
    }

    // CSON: StrictDuplicateCode

    /**
     * @uml.property name="discipline"
     */
    private String discipline = "";

    /**
     * Getter of the property <tt>discipline</tt>.
     * 
     * @return Returns the discipline.
     * @uml.property name="discipline"
     */
    public String getDiscipline() {
        return this.discipline;
    }

    /**
     * Setter of the property <tt>discipline</tt>.
     * 
     * @param discipline The discipline to set.
     * @uml.property name="discipline"
     */
    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

}
