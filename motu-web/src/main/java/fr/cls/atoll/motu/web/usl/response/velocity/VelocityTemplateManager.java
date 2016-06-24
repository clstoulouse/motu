package fr.cls.atoll.motu.web.usl.response.velocity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.response.velocity.model.ICommonService;

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
public class VelocityTemplateManager {

    public final static String VELOCITY_TEMPLATE_DIR = "velocityTemplates/";
    public final static String DEFAULT_LANG = "uk";
    public final static String DEFAULT_CHARSET_ENCODING = "UTF-8";

    private final static Logger LOGGER = LogManager.getLogger();

    private VelocityEngine velocityEngine;

    private static VelocityTemplateManager s_instance;

    private VelocityTemplateManager() {
    }

    public static VelocityTemplateManager getInstance() {
        if (s_instance == null) {
            s_instance = new VelocityTemplateManager();
        }
        return s_instance;
    }

    /**
     * .
     * 
     * @param actionName
     * @param defaultLang
     * @return
     */
    public static String getTemplatePath(String actionName, String lang_) {
        return getTemplatePath(actionName, lang_, false);
    }

    /**
     * .
     * 
     * @param actionName
     * @param defaultLang
     * @return
     */
    public static String getTemplatePath(String actionName, String lang_, boolean isXMLTemplate_) {
        String langSuffix = "";
        String lang = lang_;
        if (StringUtils.isNullOrEmpty(lang) || !isLocaleManaged(lang)) {
            lang = VelocityTemplateManager.DEFAULT_LANG;
        } else {
            langSuffix = "_" + lang;
        }

        String xmlDir = "";
        if (isXMLTemplate_) {
            xmlDir = "/xml";
        }
        return VELOCITY_TEMPLATE_DIR + xmlDir + getTemplateFileNameFromActionName(actionName) + langSuffix + ".vm";
    }

    /**
     * .
     * 
     * @param lang
     * @return
     */
    private static boolean isLocaleManaged(String lang) {
        // For instance only English is managed by this web site
        return false;
    }

    private static String getTemplateFileNameFromActionName(String actionName_) {
        String templateFileName = "";
        switch (actionName_.toLowerCase()) {
        case "describecoverage":
            templateFileName = "describecoverage";
            break;
        case "listcatalog":
            templateFileName = "listCatalog";
            break;
        case "productdownloadhome":
            templateFileName = "productDownloadHome";
            break;
        case "listproductmetadata":
            templateFileName = "listProductMetadata";
            break;
        case "listservices":
        default:
            templateFileName = "listServices";
        }
        return templateFileName;
    }

    /**
     * @return a new context with some tools initialized.
     * 
     * @see NumberTool
     * @see DateTool
     * @see MathTool
     */
    public static VelocityContext getPrepopulatedVelocityContext() {
        final NumberTool numberTool = new NumberTool();
        final DateTool dateTool = new DateTool();
        final MathTool mathTool = new MathTool();

        VelocityContext context = new VelocityContext();
        context.put("numberTool", numberTool);
        context.put("dateTool", dateTool);
        context.put("mathTool", mathTool);
        context.put("enLocale", Locale.ENGLISH);

        context.put("service", new ICommonService() {

            @Override
            public String getHttpBaseRef() {
                return BLLManager.getInstance().getConfigManager().getMotuConfig().getHttpBaseRef();
            }
        });

        return context;
    }

    public static String encodeString(String value_) {
        try {
            return URLEncoder.encode(value_, VelocityTemplateManager.DEFAULT_CHARSET_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return value_;
        }
    }

    /**
     * initializes the Velocity runtime engine, using default properties plus the properties in the Motu
     * velocity properties file.
     * 
     * @throws Exception
     * 
     * @throws MotuException the motu exception
     */
    private void initVelocityEngine() throws Exception {
        velocityEngine = new VelocityEngine();

        Properties conf = new Properties();
        // TO disable logs: VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
        // "org.apache.velocity.runtime.log.NullLogChute"
        conf.put(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        conf.put(VelocityEngine.RUNTIME_LOG, System.getProperty("motu-log-dir") + "/velocity.log");
        // conf.put("runtime.log.logsystem.log4j.category", LOG.getName());

        // 1st try to load from the configuration folder,
        // then if not found, load project src/main/resources folder
        conf.put("file.resource.loader.path", BLLManager.getInstance().getConfigManager().getMotuConfigurationFolderPath());
        conf.put("resource.loader", "file, class");
        conf.put("class.resource.loader.description", "Velocity Classpath Resource Loader");
        conf.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        velocityEngine.init(conf);
    }

    /**
     * Valeur de velocityEngine.
     * 
     * @return la valeur.
     */
    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    /**
     * .
     * 
     * @throws Exception
     */
    public void init() throws Exception {
        initVelocityEngine();
    }

    /**
     * .
     * 
     * @param defaultLang
     * @throws Exception
     * @throws ParseErrorException
     * @throws ResourceNotFoundException
     */
    public Template initVelocityEngineWithGenericTemplate(String lang, String velocityTemplateName_)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        String veloTplName = velocityTemplateName_;
        if (veloTplName == null) {
            veloTplName = getCommonGlobalVeloTemplateName(lang);
        }
        if (veloTplName != null && !veloTplName.endsWith(".vm")) {
            veloTplName += ".vm";
        }

        return getVelocityEngine().getTemplate(VELOCITY_TEMPLATE_DIR + veloTplName);
    }

    protected String getCommonGlobalVeloTemplateName(String lang) {
        StringBuffer buffer = new StringBuffer();
        String veloTemplatePrefix = BLLManager.getInstance().getConfigManager().getMotuConfig().getCommonVeloTemplatePrefix();
        if (StringUtils.isNullOrEmpty(veloTemplatePrefix)) {
            buffer.append("generic");
        } else {
            buffer.append(veloTemplatePrefix.toLowerCase());
        }

        if (StringUtils.isNullOrEmpty(lang)) {
            if (!"UK".equalsIgnoreCase(BLLManager.getInstance().getConfigManager().getMotuConfig().getCommonDefaultLanguage())) {
                buffer.append("_");
                buffer.append(BLLManager.getInstance().getConfigManager().getMotuConfig().getCommonDefaultLanguage());
            }
        } else {
            buffer.append("_");
            buffer.append(lang);
        }
        buffer.append(".vm");
        return buffer.toString();
    }
}
