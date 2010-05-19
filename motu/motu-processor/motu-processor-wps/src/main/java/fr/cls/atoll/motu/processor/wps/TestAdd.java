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
package fr.cls.atoll.motu.processor.wps;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.ReferencedComplexInput;
import org.deegree.services.wps.output.ComplexOutput;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.6 $ - $Date: 2009-04-22 14:40:14 $
 */
public class TestAdd implements Processlet {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(TestAdd.class);

    /**
     * Constructeur.
     */
    public TestAdd() {
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
        //        
        // LiteralInput a = (LiteralInput) in.getParameter("A");
        // LiteralInput b = (LiteralInput) in.getParameter("B");
        //
        // int value = Integer.parseInt(a.getValue()) + Integer.parseInt(b.getValue());

        ComplexInput a = (ComplexInput) in.getParameter("A");
        ComplexInput b = (ComplexInput) in.getParameter("B");
        
        if ( a instanceof ReferencedComplexInput ) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("process(ProcessletInputs, ProcessletOutputs, ProcessletExecutionInfo) : ComplexInput a is reference");
            }
        }
        if ( b instanceof ReferencedComplexInput ) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("process(ProcessletInputs, ProcessletOutputs, ProcessletExecutionInfo) : ComplexInput b is reference");
            }
        }
        String aa = null;
        String bb = null;
        try {
//            aa = a.getValueAsElement().getText();
//            bb = b.getValueAsElement().getText();
            
            byte[] buffer = new byte[1024];

            InputStream is = a.getValueAsBinaryStream();
            StringBuffer stringBuffer = new StringBuffer();
            int bytesRead = 0;
            while ( ( bytesRead = is.read( buffer ) ) != -1 ) {
                stringBuffer.append(new String(buffer, 0, bytesRead));
            }
            aa = stringBuffer.toString();

            is = b.getValueAsBinaryStream();
            stringBuffer = new StringBuffer();
            while ( ( bytesRead = is.read( buffer ) ) != -1 ) {
                stringBuffer.append(new String(buffer, 0, bytesRead));
            }
            bb = stringBuffer.toString();

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        int value = Integer.parseInt(aa) + Integer.parseInt(bb);
        // TODO Auto-generated method stub
        // LiteralOutput c = (LiteralOutput)out.getParameter("C");
        // c.setValue(Integer.toString(value));
        ComplexOutput c = (ComplexOutput) out.getParameter("C");
//        try {
//
//            XMLStreamWriter writer = c.getXMLStreamWriter();
//            XMLAdapter.writeElement(writer, c.getIdentifier().getCode(), Integer.toString(value));
//        } catch (XMLStreamException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
         try {
        
         c.getBinaryOutputStream().write(Integer.toString(value).getBytes());
         } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
        
         }
        // ComplexOutput c = (ComplexOutput) out.getParameter("C");
        // try {
        // c.getXMLStreamWriter().writeEmptyElement("C");
        // c.getXMLStreamWriter().writeCData(Integer.toString(value));
        // } catch (XMLStreamException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        if (LOG.isDebugEnabled()) {
            LOG.debug("process(ProcessletInputs, ProcessletOutputs, ProcessletExecutionInfo) - exiting");
        }
    }

    /**
     * .
     * 
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
