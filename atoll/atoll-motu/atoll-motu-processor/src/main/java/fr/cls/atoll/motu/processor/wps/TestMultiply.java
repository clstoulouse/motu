package fr.cls.atoll.motu.processor.wps;

import org.apache.log4j.Logger;

import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.LiteralOutput;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-04-02 15:03:44 $
 */
public class TestMultiply implements Processlet {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(TestMultiply.class);

    /**
     * Constructeur.
     */
    public TestMultiply() {
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy() - entering");
        }
        // TODO Auto-generated method stub

        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy() - exiting");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("process(ProcessletInputs in=" + in + ", ProcessletOutputs out=" + out + ", ProcessletExecutionInfo info=" + info
                    + ") - entering");
        }
        LiteralInput a = (LiteralInput)in.getParameter("A");
        LiteralInput b = (LiteralInput)in.getParameter("B");

        int value = Integer.parseInt(a.getValue()) * Integer.parseInt(b.getValue());
        // TODO Auto-generated method stub
        LiteralOutput c = (LiteralOutput)out.getParameter("C");
        c.setValue(Integer.toString(value));

        if (LOG.isDebugEnabled()) {
            LOG.debug("process(ProcessletInputs, ProcessletOutputs, ProcessletExecutionInfo) - exiting");
        }
    }

    /**
     * .
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
