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
package fr.cls.atoll.motu.library.misc.data;

import fr.cls.atoll.motu.library.misc.metadata.ParameterMetaData;

/**
 * Data parameter (variable) class.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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

    /**
     * Creates the from.
     *
     * @param parameterMetaData the parameter meta data
     * @return the var data
     */
    public static VarData createFrom(ParameterMetaData parameterMetaData) {
            if (parameterMetaData == null) {
                return null;
            }
            VarData varData = new VarData(parameterMetaData.getName());
            varData.setStandardName(parameterMetaData.getStandardName().trim());
            return varData;        
    }

    @Override
    public String toString() {
        return "VarData [" + (name != null ? "name=" + name + ", " : "") + (outputName != null ? "outputName=" + outputName + ", " : "")
                + (standardName != null ? "standardName=" + standardName : "") + "]";
    }
    
}
