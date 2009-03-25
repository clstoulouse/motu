//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package fr.cls.atoll.motu.processor.wps;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.BoundingBoxInput;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.BoundingBoxOutput;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.output.LiteralOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The purpose of this {@link Processlet} is to provide a means for testing the correct processing of input and output
 * parameter types by the deegree 3 WPS implementation.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: dearith $
 * 
 * @version $Revision: 1.1 $, $Date: 2009-03-25 15:34:32 $
 */
public class TestProcesslet implements Processlet {

    private static final Logger LOG = LoggerFactory.getLogger( TestProcesslet.class );

    @Override
    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                            throws ProcessletException {

        LOG.trace( "BEGIN TestProcesslet#execute(), context: " + OGCFrontController.getContext() );

        // determine the sleep time in seconds (from input parameter 'LiteralInput')
        int sleepSeconds = determineSleepTime( (LiteralInput) in.getParameter( "LiteralInput" ) );

        // sleep a total of sleepSeconds (but update the percent completed information for every percent)
        try {
            float sleepMillis = sleepSeconds * 1000;
            int sleepStep = (int) ( sleepMillis / 99.0f );
            LOG.debug( "Sleep step (millis): " + sleepStep );
            for ( int percentCompleted = 0; percentCompleted <= 99; percentCompleted++ ) {
                LOG.debug( "Setting percent completed: " + percentCompleted );
                info.setPercentCompleted( percentCompleted );
                Thread.sleep( sleepStep );
            }
        } catch ( InterruptedException e ) {
            throw new ProcessletException( e.getMessage() );
        }

        BoundingBoxInput bboxInput = (BoundingBoxInput) in.getParameter( "BBOXInput" );
        LOG.debug( "- BBOXInput: " + bboxInput );

        ComplexInput xmlInput = (ComplexInput) in.getParameter( "XMLInput" );
        LOG.debug( "- XMLInput: " + xmlInput );

        ComplexInput binaryInput = (ComplexInput) in.getParameter( "BinaryInput" );
        LOG.debug( "- BinaryInput: " + binaryInput );

        LiteralOutput literalOutput = (LiteralOutput) out.getParameter( "LiteralOutput" );
        LOG.debug( "Setting literal output (requested=" + literalOutput.isRequested() + ")" );
        literalOutput.setValue( "" + sleepSeconds );

        BoundingBoxOutput bboxOutput = (BoundingBoxOutput) out.getParameter( "BBOXOutput" );
        LOG.debug( "Setting bbox output (requested=" + bboxOutput.isRequested() + ")" );        
        bboxOutput.setValue( bboxInput.getValue() );

        ComplexOutput xmlOutput = (ComplexOutput) out.getParameter( "XMLOutput" );
        LOG.debug( "Setting XML output (requested=" + xmlOutput.isRequested() + ")" );
        try {
            XMLStreamWriter writer = xmlOutput.getXMLStreamWriter();
            XMLAdapter.writeElement( writer, xmlInput.getValueAsXMLStream() );
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ComplexOutput binaryOutput = (ComplexOutput) out.getParameter( "BinaryOutput" );
        LOG.debug( "Setting binary output (requested=" + binaryOutput.isRequested() + ")" );
        try {
            InputStream is = binaryInput.getValueAsBinaryStream();
            BufferedOutputStream os = new BufferedOutputStream( binaryOutput.getBinaryOutputStream() );
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ( ( bytesRead = is.read( buffer ) ) != -1 ) {
                os.write( buffer, 0, bytesRead );
            }
            os.flush();
        } catch ( IOException e1 ) {
            e1.printStackTrace();
        }

        LOG.trace( "END TestProcesslet#execute()" );
    }

    private int determineSleepTime( LiteralInput input ) {
       
        int seconds = -1;
        String uom = input.getUOM();

        LOG.debug( "dataType: " + input.getDataType() + ", uom: " + input.getUOM());
        
        // NOTE: it is guaranteed (by the deegree WPS) that the UOM is always
        // one of the UOMs specified in the process definition
        if ( "seconds".equals( uom ) ) {
            LOG.debug( "Sleep time given in seconds" );
            seconds = (int) Double.parseDouble( input.getValue() );
        } else if ( "minutes".equals( uom ) ) {
            LOG.debug( "Sleep time given in minutes" );
            seconds = (int) ( Double.parseDouble( input.getValue() ) * 60 );
        }
        return seconds;
    }

    @Override
    public void destroy() {
        LOG.debug( "TestProcesslet#destroy() called" );
    }

    @Override
    public void init() {
        LOG.debug( "TestProcesslet#init() called" );
    }
}
