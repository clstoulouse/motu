package fr.cls.atoll.motu.processor.wps;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.Code;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescription;
import org.deegree.ogcwebservices.wps.execute.IOValue;
import org.deegree.ogcwebservices.wps.execute.OutputDefinition;
import org.deegree.ogcwebservices.wps.execute.OutputDefinitions;
import org.deegree.ogcwebservices.wps.execute.Process;
import org.deegree.ogcwebservices.wps.execute.ExecuteResponse.ProcessOutputs;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public abstract class TransformProcessAbstract extends Process implements FunctionProvider {
    private String outputAbstract;

    private URI outputEncoding;

    private String outputFormat;

    private Code outputIdentifier;

    private URL outputSchema;

    private String outputTitle;

    private URI outputUom;

    public TransformProcessAbstract(ProcessDescription processDescription) {
        super(processDescription);
    }

    /**
     * Execute this function from a workflow.
     * <p>
     * Map the arguments to match the WPS ones and call the underlying function.
     * 
     * @param transientVars Variables that will not be persisted.
     * @param args The properties for this function invocation.
     * @param ps The persistent variables that are associated with the current instance of the workflow.
     */
    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.ogcwebservices.wps.execute.Process#execute(java.util.Map,
     * org.deegree.ogcwebservices.wps.execute.OutputDefinitions)
     */
    @Override
    public ProcessOutputs execute(Map<String, IOValue> inputs, OutputDefinitions outputDefinitions) throws OGCWebServiceException {
        injectOutputDefinitions(outputDefinitions);
        final Object objet = execute();

        return null;
    }

    /**
     * @return the outputAbstract
     */
    public String getOutputAbstract() {
        return outputAbstract;
    }

    /**
     * @return the outputEncoding
     */
    public URI getOutputEncoding() {
        return outputEncoding;
    }

    /**
     * @return the outputFormat
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * @return the outputIdentifier
     */
    public Code getOutputIdentifier() {
        return outputIdentifier;
    }

    /**
     * @return the outputSchema
     */
    public URL getOutputSchema() {
        return outputSchema;
    }

    /**
     * @return the outputTitle
     */
    public String getOutputTitle() {
        return outputTitle;
    }

    /**
     * @return the outputUom
     */
    public URI getOutputUom() {
        return outputUom;
    }

    protected abstract Object execute();

    /**
     * Inject input values to local variables.
     * 
     * @param inputs the inputs to inject.
     * @throws OGCWebServiceException
     */
    protected void injectInputs(Map<String, IOValue> inputs) throws OGCWebServiceException {
        final BeanWrapper bw = new BeanWrapperImpl(this);
        for (Field field : inspectAnnotations()) {
            final InputAnnotation annotation = field.getAnnotation(InputAnnotation.class);
            final IOValue value = inputs.get(annotation.value());
            if (value != null) {
                bw.setPropertyValue(field.getName(), extractValueFromInput(value));
            }
        }
    }

    /**
     * Inject the output definitions as member property of this instance.
     * <p>
     * Assumes (simplified for the actual process) that only one output is defined.
     * 
     * @param outputDefinitions the definitions to inject.
     */
    protected void injectOutputDefinitions(OutputDefinitions outputDefinitions) {
        List<OutputDefinition> outputDefinitionList = outputDefinitions.getOutputDefinitions();
        Iterator<OutputDefinition> outputDefinitionListIterator = outputDefinitionList.iterator();
        while (outputDefinitionListIterator.hasNext()) {
            OutputDefinition outputDefinition = outputDefinitionListIterator.next();
            this.outputAbstract = outputDefinition.getAbstract();
            this.outputTitle = outputDefinition.getTitle();
            this.outputIdentifier = outputDefinition.getIdentifier();
            this.outputSchema = outputDefinition.getSchema();
            this.outputFormat = outputDefinition.getFormat();
            this.outputEncoding = outputDefinition.getEncoding();
            this.outputUom = outputDefinition.getUom();
        }
    }

    /**
     * Method for validating provided input parameters against configured input parameters.
     * <p>
     * This method may be overriden by subclass to provide another check.
     * 
     * @return <code>true</code>
     */
    protected boolean validate() {
        return true;
    }

    private Object extractValueFromInput(IOValue value) throws OGCWebServiceException {

        if (value.isLiteralValueType()) {
            return value.getLiteralValue().getValue();
        } else if (value.isComplexValueType()) {
            return value.getComplexValue().getContent();
        } else if (value.isBoundingBoxValueType()) {
            return value;
        }
        throw new OGCWebServiceException(getOutputTitle(), "IO Value not understadable");

    }

    /**
     * @return all the declared field that are annotated with {@link InputAnnotation}.
     */
    private Collection<Field> inspectAnnotations() {
        return Collections2.filter(Lists.newArrayList(getClass().getFields()), new Predicate<Field>() {
            @Override
            public boolean apply(Field field) {
                return field.isAnnotationPresent(InputAnnotation.class);
            }
        });
    }
}
