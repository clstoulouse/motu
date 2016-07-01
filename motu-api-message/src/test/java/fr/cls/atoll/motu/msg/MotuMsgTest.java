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
package fr.cls.atoll.motu.msg;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;

import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class MotuMsgTest {

    private static final String MOTU_MSG_SCHEMA_PACK_NAME = "fr.cls.atoll.motu.api.message.xml";

    private static JAXBContext jcontext = null;

    /**
     * .
     * 
     * @param args
     */
    public static void main(String[] args) {

        initJAXB();
        testWriteStatusModeResponse();

    }

    static private void initJAXB() {
        try {
            jcontext = JAXBContext.newInstance(MOTU_MSG_SCHEMA_PACK_NAME);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    static private void testWriteStatusModeResponse() {
        String xmlFile = "./target/testStatusModeResponse.xml";
        StatusModeType status = StatusModeType.DONE;
        String msg = "OK";
        fr.cls.atoll.motu.api.message.xml.ObjectFactory objectFactory = new fr.cls.atoll.motu.api.message.xml.ObjectFactory();

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
