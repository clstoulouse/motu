package fr.cls.atoll.motu.web.bll.exception;

import fr.cls.atoll.motu.api.message.xml.ErrorType;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class NotEnoughSpaceException extends MotuException {

    private static final long serialVersionUID = -1;

    /**
     * Constructeur.
     * 
     * @param message
     */
    public NotEnoughSpaceException(String message) {
        super(ErrorType.SYSTEM, message);
    }

}
