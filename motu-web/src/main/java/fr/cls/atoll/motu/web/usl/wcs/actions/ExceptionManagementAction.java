package fr.cls.atoll.motu.web.usl.wcs.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import fr.cls.atoll.motu.web.usl.wcs.data.ExceptionData;
import fr.cls.atoll.motu.web.usl.wcs.exceptions.ActionException;
import fr.cls.atoll.motu.web.usl.wcs.responses.ExceptionBuilder;

public class ExceptionManagementAction extends Action {

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws ActionException {
        ExceptionData exceptionData = new ExceptionData();

        String errorCode = "";
        List<String> errorMessage = new ArrayList<>();
        exceptionData.setErrorCode(errorCode);
        exceptionData.setErrorMessage(errorMessage);

        String xmlResponses;
        try {
            xmlResponses = ExceptionBuilder.getInstance().buildResponse(exceptionData);
            response.getWriter().write(xmlResponses);
        } catch (JAXBException | IOException e) {
            throw new ActionException(e);
        }
    }

}
