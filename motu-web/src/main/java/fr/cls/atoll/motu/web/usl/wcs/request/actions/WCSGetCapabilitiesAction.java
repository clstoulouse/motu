package fr.cls.atoll.motu.web.usl.wcs.request.actions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.usl.request.actions.AbstractAction;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.WCSHTTPParameters;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.AcceptVersionsHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.RequestHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.ServiceHTTPParameterValidator;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class WCSGetCapabilitiesAction extends AbstractAction {

    public static final String ACTION_NAME = "GetCapabilities";

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;
    private AcceptVersionsHTTPParameterValidator acceptVersionsHTTPParameterValidator;
    private RequestHTTPParameterValidator requestHTTPParameterValidator;

    /**
     * Constructeur.
     * 
     * @param actionName_
     * @param actionCode_
     * @param request_
     * @param response_
     */
    public WCSGetCapabilitiesAction(String actionCode_, HttpServletRequest request_, HttpServletResponse response_) {
        super(ACTION_NAME, actionCode_, request_, response_);
        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                WCSHTTPParameters.SERVICE,
                WCSHTTPParameters.getServiceFromRequest(getRequest()));
        acceptVersionsHTTPParameterValidator = new AcceptVersionsHTTPParameterValidator(
                WCSHTTPParameters.ACCEPT_VERSIONS,
                WCSHTTPParameters.getAcceptVersionsFromRequest(getRequest()));
        requestHTTPParameterValidator = new RequestHTTPParameterValidator(
                WCSHTTPParameters.REQUEST,
                WCSHTTPParameters.getRequestFromRequest(getRequest()));
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        serviceHTTPParameterValidator.validate();
        acceptVersionsHTTPParameterValidator.validate();
        requestHTTPParameterValidator.validate();
    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws MotuException {
        String service = serviceHTTPParameterValidator.getParameterValueValidated();
        String acceptVersions = acceptVersionsHTTPParameterValidator.getParameterValueValidated();
        String request = requestHTTPParameterValidator.getParameterValueValidated();

        try {
            onGetCapabilitiesAction(service, acceptVersions, request);
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while writing response", e);
        }
        // getResponse().setContentType(CONTENT_TYPE_HTML);
        // try {
        // // TODO response to a successful GetCapabilities request shall be a valid XML document of type
        // // wcs:CapabilitiesType.
        // getResponse().getWriter().write("<!DOCTYPE html><html><body> TODO");
        // getResponse().getWriter().write("<BR />service=" + service);
        // getResponse().getWriter().write("<BR />acceptVersions=" + acceptVersions);
        // getResponse().getWriter().write("<BR />request=" + request);
        // getResponse().getWriter().write("</body></html>");
        // } catch (IOException e) {
        // throw new MotuException(ErrorType.SYSTEM, "Error while writing response", e);
        // }
    }

    /**
     * .
     * 
     * @param service
     * @param acceptVersions
     * @param request
     * @throws IOException
     * @throws DOMException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private void onGetCapabilitiesAction(String service, String acceptVersions, String request)
            throws DOMException, IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        Document responseTpl = getCapabilitiesResponseXMLDoc(dbFactory);
        Node nlContents = responseTpl.getElementsByTagName("wcs:Contents").item(0);

        // TODO Loop over the catalog
        nlContents.appendChild(createContent(dbFactory, "id01", "GridCoverage"));
        nlContents.appendChild(createContent(dbFactory, "id02", "GridCoverage"));

        getResponse().setContentType(CONTENT_TYPE_XML);
        getResponse().getWriter().write(responseTpl.getTextContent());
    }

    private Document getCapabilitiesResponseXMLDoc(DocumentBuilderFactory dbFactory)
            throws ParserConfigurationException, FileNotFoundException, SAXException, IOException {
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(getClass().getClassLoader().getResourceAsStream("wcs/tpl/getCapabilitiesResponse.tpl.xml"));
    }// fr.cls.atoll.motu.web.usl.wcs.request.actions.tpl.

    private Element createContent(DocumentBuilderFactory dbFactory, String coverageId, String coverageSubtype)
            throws ParserConfigurationException, FileNotFoundException, SAXException, IOException {
        Element res = null;
        try (Scanner sc = new Scanner(
                getClass().getClassLoader().getResourceAsStream("wcs/tpl/getCapabilitiesResponse-CoverageSummary.tpl.xml"),
                "UTF-8")) {
            String content = sc.useDelimiter("\\Z").next();
            content = content.replaceAll("@@CoverageId@@", coverageId);
            content = content.replaceAll("@@CoverageSubtype@@", coverageSubtype);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(content)));
            res = doc.getDocumentElement();
        }
        return res;
    }

}
