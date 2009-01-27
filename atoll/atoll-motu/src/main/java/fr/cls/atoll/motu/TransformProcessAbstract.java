package fr.cls.atoll.motu;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

import java.util.Map;

import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescription;
import org.deegree.ogcwebservices.wps.execute.IOValue;
import org.deegree.ogcwebservices.wps.execute.OutputDefinitions;
import org.deegree.ogcwebservices.wps.execute.Process;
import org.deegree.ogcwebservices.wps.execute.ExecuteResponse.ProcessOutputs;

public abstract class TransformProcessAbstract extends Process implements FunctionProvider {
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
    public ProcessOutputs execute(Map<String, IOValue> arg0, OutputDefinitions arg1) throws OGCWebServiceException {
        // TODO Auto-generated method stub
        return null;
    }

}
