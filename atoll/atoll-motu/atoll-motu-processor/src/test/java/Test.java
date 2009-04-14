
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.deegree.commons.utils.HttpUtils;


/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-04-14 15:02:45 $
 */
public class Test {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(Test.class);

    /**
     * .
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
    
    // TODO does this work in all cases?
    String href = "http://localhost:8080/atoll-motuservlet/services";    
    String postBodyString = "<wps:Execute  xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" service=\"WPS\" version=\"1.0.0\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">                        <ows:Identifier>TestAdd</ows:Identifier>                        <wps:DataInputs>                            <wps:Input>                             <ows:Identifier>A</ows:Identifier>                              <wps:Data>                                  <wps:ComplexData>10</wps:ComplexData>                               </wps:Data>                         </wps:Input>                            <wps:Input>                             <ows:Identifier>B</ows:Identifier>                              <wps:Data>                                  <wps:ComplexData>8</wps:ComplexData>                                </wps:Data>                         </wps:Input>                        </wps:DataInputs>                       <wps:ResponseForm>                          <wps:RawDataOutput mimeType=\"text/xml\">                             <ows:Identifier>C</ows:Identifier>                          </wps:RawDataOutput>                        </wps:ResponseForm>                 </wps:Execute>              </wps:Body>";
    LOG.debug( "Using post body '" + postBodyString + "'" );
    // TODO what about the encoding here?
    InputStream is = new ByteArrayInputStream( postBodyString.getBytes() );
    Map<String, String> headers = new HashMap<String, String>();
    try {
        is = HttpUtils.post( HttpUtils.STREAM, href, is, headers );
        byte b[] = new byte[4096];
        
        is.read(b);
        System.out.println(b);
    } catch (HttpException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    }
}
