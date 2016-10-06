package fr.cls.atoll.motu.web.usl.response.velocity;

import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

import fr.cls.atoll.motu.web.bll.exception.MotuException;

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
public interface IVelocityTemplateManager {

    /**
     * .
     * 
     * @throws Exception
     */
    void init() throws Exception;

    /**
     * .
     * 
     * @return
     */
    VelocityEngine getVelocityEngine();

    /**
     * .
     * 
     * @param velocityContext
     * @param template
     * @return
     * @throws MotuException
     */
    String getResponseWithVelocity(Map<String, Object> velocityContext, Template template) throws MotuException;

    /**
     * .
     * 
     * @param velocityContext_
     * @param lang
     * @param velocityTemplateName_
     * @return
     * @throws MotuException
     */
    String getResponseWithVelocity(Map<String, Object> velocityContext_, String lang, String velocityTemplateName_) throws MotuException;

}
