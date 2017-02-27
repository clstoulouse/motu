package fr.cls.atoll.motu.web.usl.wcs.responses;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import fr.cls.atoll.motu.web.usl.wcs.data.ExceptionData;
import net.opengis.ows.v_2_0.ExceptionType;

public class ExceptionBuilder {

    private static ExceptionBuilder instance = null;

    private net.opengis.ows.v_2_0.ObjectFactory owsFactory = new net.opengis.ows.v_2_0.ObjectFactory();

    private Marshaller marshaller = JAXBContext.newInstance(ExceptionType.class).createMarshaller();

    private ExceptionBuilder() throws JAXBException {

    }

    public static ExceptionBuilder getInstance() throws JAXBException {
        if (instance == null) {
            instance = new ExceptionBuilder();
        }

        return instance;
    }

    public String buildResponse(ExceptionData exceptionData) throws JAXBException {
        String result = "";

        ExceptionType exception = new ExceptionType();
        exception.setExceptionCode(exceptionData.getErrorCode());
        exception.setExceptionText(exceptionData.getErrorMessage());

        JAXBElement<ExceptionType> root = owsFactory.createException(exception);
        StringWriter sw = new StringWriter();
        marshaller.marshal(root, sw);

        return result;
    }

}
