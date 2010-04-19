package fr.cls.atoll.motu.msg;

import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import fr.cls.atoll.motu.msg.xml.StatusModeResponse;
import fr.cls.atoll.motu.msg.xml.StatusModeType;

public class MotuMsgTest {
    
    private static final String MOTU_MSG_SCHEMA_PACK_NAME = "fr.cls.atoll.motu.msg.xml";

    private static JAXBContext jcontext = null;
    /**
     * .
     * @param args
     */
    public static void main(String[] args) {

        initJAXB();
        testWriteStatusModeResponse();

    }
    static private void initJAXB() {
        try {
            jcontext  = JAXBContext.newInstance(MOTU_MSG_SCHEMA_PACK_NAME);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    static private void testWriteStatusModeResponse()  {
        String xmlFile = "./target/testStatusModeResponse.xml";
        StatusModeType status = StatusModeType.DONE;
        String msg = "OK";                
        fr.cls.atoll.motu.msg.xml.ObjectFactory objectFactory = new fr.cls.atoll.motu.msg.xml.ObjectFactory();

        FileOutputStream out = null;                
        
        StatusModeResponse statusModeResponse = objectFactory.createStatusModeResponse();
        statusModeResponse.setStatus(status);
        statusModeResponse.setMsg(msg);
        
        Marshaller marshaller = null;
        try {
            out = new FileOutputStream(xmlFile);        
            marshaller = jcontext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(statusModeResponse, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

}
