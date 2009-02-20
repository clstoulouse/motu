/**
 * 
 */
package fr.cls.atoll.motu.data;

/**
 * Data parameter (variable) class.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:25 $
 */
public class VarData {

    /**
     * Default constructor.
     */
    public VarData() {

    }

    /**
     * Constructor.
     * 
     * @param name name of the variable.
     */
    public VarData(String name) {
        setVarName(name);
        setOutputName(name);
    }

    // CSOFF: StrictDuplicateCode : normal duplication code.

    /**
     * Name of the variable.
     * 
     * @uml.property name="name"
     */
    private String name = "";

    /**
     * Getter of the property <tt>name</tt>.
     * 
     * @return Returns the name.
     * @uml.property name="name"
     */
    public String getVarName() {
        return this.name;
    }

    /**
     * Setter of the property <tt>name</tt>.
     * 
     * @param value The name to set.
     * @uml.property name="name"
     */
    public void setVarName(String value) {
        this.name = value.trim();
    }

    /**
     * Name of the variable.
     * 
     * @uml.property name="standardName"
     */
    private String standardName = "";

    /**
     * Getter of the property <tt>standardName</tt>.
     * 
     * @return Returns the name.
     * @uml.property name="standardName"
     */
    public String getStandardName() {
        return this.standardName;
    }

    /**
     * Setter of the property <tt>standardName</tt>.
     * 
     * @param standardName The standardName to set.
     * @uml.property name="standardName"
     */
    public void setStandardName(String standardName) {
        this.standardName = standardName.trim();
    }

    // CSON: StrictDuplicateCode

    /**
     * Output name of the variable.
     * 
     * @uml.property name="outputName"
     */
    private String outputName = "";

    /**
     * Getter of the property <tt>outputName</tt>.
     * 
     * @return Returns the outputName.
     * @uml.property name="outputName"
     */
    public String getOutputName() {
        return this.outputName;
    }

    /**
     * Setter of the property <tt>outputName</tt>.
     * 
     * @param outputName The outputName to set.
     * @uml.property name="outputName"
     */
    public void setOutputName(String outputName) {
        this.outputName = outputName.trim();
    }

    /**
     * Output dimension of the variable.
     * 
     * @uml.property name="outputDimension"
     */
    private String outputDimension = "";

    /**
     * Getter of the property <tt>outputDimension</tt>.
     * 
     * @return Returns the outputDimension.
     * @uml.property name="outputDimension"
     */
    public String getOutputDimension() {
        return this.outputDimension;
    }

    /**
     * Setter of the property <tt>outputDimension</tt>.
     * 
     * @param outputDimension The outputDimension to set.
     * @uml.property name="outputDimension"
     */
    public void setOutputDimension(String outputDimension) {
        this.outputDimension = outputDimension;
    }

    /**
     * It can be one of the following: - None: no statistical calculation (default). - Average - Variance -
     * Sum - Min. - Max.
     * 
     * @uml.property name="statisticalCalculation"
     */
    private String statisticalCalculation = "";

    /**
     * Getter of the property <tt>computeMode</tt>.
     * 
     * @return Returns the computeMode.
     * @uml.property name="statisticalCalculation"
     */
    public String getStatisticalCalculation() {
        return this.statisticalCalculation;
    }

    /**
     * Setter of the property <tt>computeMode</tt>.
     * 
     * @param statisticalCalculation The statisticalCalculation to set.
     * @uml.property name="statisticalCalculation"
     */
    public void setStatisticalCalculation(String statisticalCalculation) {
        this.statisticalCalculation = statisticalCalculation;
    }

    /**
     * Expression of the variable. It can the variable itself or a formula to compute the variable (with
     * functions, arithmetical operators, other variables of the product). The result of the formula
     * (mathematical expression) is always of type 'double'.
     * 
     * @uml.property name="mathExpression"
     */
    private String mathExpression;

    /**
     * Getter of the property <tt>Expression</tt>.
     * 
     * @return Returns the expression.
     * @uml.property name="mathExpression"
     */
    public String getMathExpression() {
        return this.mathExpression;
    }

    /**
     * Setter of the property <tt>Expression</tt>.
     * 
     * @param mathExpression The expression to set.
     * @uml.property name="mathExpression"
     */
    public void setMathExpression(String mathExpression) {
        this.mathExpression = mathExpression;
    }

}
