package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.velocity.tools.generic.MathTool;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.UnitUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.AbstractHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.FileNameHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.response.velocity.VelocityTemplateManager;
import fr.cls.atoll.motu.web.usl.response.velocity.model.transaction.LogTransaction;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)<br>
 * <br>
 * <br>
 * This interface is used to display the version of the Motu entities.<br>
 * Operation invocation consists in performing an HTTP GET request.<br>
 * There is no input parameter<br>
 * The output response is a simple HTML web page.<br>
 * <br>
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class TransactionsAction extends AbstractAction {

    public static final String ACTION_NAME = "transactions";
    private FileNameHTTPParameterValidator fileNameHTTPParameterValidator;
    private static final String FILE_NAME_PATTERN = ".*motuQSlog.*";

    private File logFolder;

    /**
     * Constructeur.
     * 
     * @param actionName_
     */
    public TransactionsAction(String actionCode_, HttpServletRequest request, HttpServletResponse response) {
        super(ACTION_NAME, actionCode_, request, response);

        logFolder = getLogFolder();
        fileNameHTTPParameterValidator = new FileNameHTTPParameterValidator(
                FILE_NAME_PATTERN,
                MotuRequestParametersConstant.PARAM_FILE_NAME,
                CommonHTTPParameters.getFileNameFromRequest(getRequest()),
                AbstractHTTPParameterValidator.EMPTY_VALUE);

    }

    private File getLogFolder() {
        org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("fr.cls.atoll.motu.web.bll.request");

        org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) logger;

        org.apache.logging.log4j.core.LoggerContext context = coreLogger.getContext();

        RollingFileAppender rfa = context.getConfiguration().getAppender("log-file-infos.queue");
        String fileNamePattern = rfa.getFileName();
        String logFolderStr = StringUtils.substringBeforeLast(fileNamePattern, "/");
        File logFolder = null;
        if (logFolderStr.equalsIgnoreCase("motu-log-dir")) {
            logFolder = new File(System.getProperty("motu-log-dir"));
        } else {
            logFolder = new File(logFolderStr);
        }
        return logFolder;
    }

    @Override
    public void process() throws MotuException {
        // READ log4j file to know where MotuQSLog files are stored
        File logFolder = getLogFolder();

        String logFileName = fileNameHTTPParameterValidator.getParameterValueValidated();
        if (fr.cls.atoll.motu.web.common.utils.StringUtils.isNullOrEmpty(logFileName)) {
            listLogFiles(logFolder);
        } else {
            try {
                downloadLogFile(logFolder, logFileName);
            } catch (IOException e) {
                throw new MotuException(ErrorType.SYSTEM, "Error while ownloading log file", e);
            }
        }

    }

    /**
     * .
     * 
     * @param logFolder
     * @param logFileName
     * @throws IOException
     * @throws MotuException
     */
    private void downloadLogFile(File logFolder, String logFileName) throws IOException, MotuException {
        File file = new File(logFolder, logFileName);
        getResponse().setCharacterEncoding("UTF-8");
        getResponse().setContentType(logFileName.endsWith("xml") ? "text/xml" : "text/csv");
        getResponse().setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
        getResponse().setHeader("Content-Length", Long.toString(file.length()));
        Files.copy(file.toPath(), getResponse().getOutputStream());
    }

    /**
     * .
     * 
     * @throws MotuException
     */
    private void listLogFiles(File logFolder) throws MotuException {
        // Filter all MotuQSLog files xml or csv and display a link to download files
        String[] fileNames = logFolder.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.matches(".*motuQSlog.*");
            }
        });

        List<LogTransaction> logTransactionList = new ArrayList<LogTransaction>();
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss.SSS");
        for (String fileName : fileNames) {
            File f = new File(logFolder, fileName);
            double sizeInMBytes = UnitUtils.toMegaBytes(new Double(f.length()));
            long lastModifiedDate = f.lastModified();
            logTransactionList.add(new LogTransaction(fileName, sizeInMBytes, lastModifiedDate, df.format(lastModifiedDate)));
        }
        // Sort by date DESC
        Collections.sort(logTransactionList, new Comparator<LogTransaction>() {

            @Override
            public int compare(LogTransaction o1, LogTransaction o2) {
                return Long.valueOf(o1.getLastModifiedTimeStamp()).compareTo(o2.getLastModifiedTimeStamp());
            }
        });
        Collections.reverse(logTransactionList);
        Map<String, Object> velocityContext = new HashMap<String, Object>(2);
        velocityContext.put("body_template", VelocityTemplateManager.getTemplatePath(ACTION_NAME, VelocityTemplateManager.DEFAULT_LANG));
        velocityContext.put("logTransactionList", logTransactionList);
        velocityContext.put("logFolder", logFolder);
        velocityContext.put("math", new MathTool());

        String response = VelocityTemplateManager.getInstance().getResponseWithVelocity(velocityContext, null, null);
        try {
            getResponse().setContentType(CONTENT_TYPE_HTML);
            getResponse().getWriter().write(response);
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while using velocity template", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        fileNameHTTPParameterValidator.validate();

        String logFileName = fileNameHTTPParameterValidator.getParameterValueValidated();
        if (!fr.cls.atoll.motu.web.common.utils.StringUtils.isNullOrEmpty(logFileName)) {
            File file = new File(logFolder, logFileName);
            if (!file.exists()) {
                throw new InvalidHTTPParameterException(
                        "Log file " + logFileName + " does not exist",
                        MotuRequestParametersConstant.PARAM_FILE_NAME,
                        logFileName,
                        "");
            }
            long fileLength = file.length();
            if (fileLength <= 0) {
                throw new InvalidHTTPParameterException(
                        "Log file " + logFileName + " is empty.",
                        MotuRequestParametersConstant.PARAM_FILE_NAME,
                        logFileName,
                        "");
            }
        }

    }

}
