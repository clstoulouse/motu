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
package fr.cls.atoll.motu.api.rest;

import javax.xml.bind.JAXBException;

/**
 * Classe d'execption pour l'API motu.
 * 
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuRequestException extends Exception {

    /** Numéro de version. */
    private static final long serialVersionUID = -9112137116434392334L;

    /**
     * Constructeur de l'exception pour l'API motu à partir d'un message et d'une exception mère.
     * 
     * @param message message to post.
     * @param cause native exception.
     */
    public MotuRequestException(String message, Throwable cause) {
        super(message, cause);

    }

    /**
     * Constructeur de l'exception pour l'API motu à partir d'un message et d'une exception mère.
     * 
     * @param message message to post.
     * @param cause native exception.
     */
    public MotuRequestException(String message, JAXBException cause) {
        super(message, ((cause.getLinkedException() != null) ? cause.getLinkedException() : cause));

    }

    /**
     * Constructeur de l'exception pour l'API motu à partir d'un message.
     * 
     * @param message message to post.
     */
    public MotuRequestException(String message) {
        super(message);
    }

    /**
     * Constructeur de l'exception pour l'API motu à partir d'une exception mère.
     * 
     * @param cause native exception.
     */
    public MotuRequestException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructeur de l'exception pour l'API motu à partir d'une exception mère.
     * 
     * @param cause native exception.
     */
    public MotuRequestException(JAXBException cause) {
        super(((cause.getLinkedException() != null) ? cause.getLinkedException() : cause));
    }

    /**
     * Notify exception.
     * 
     * @return the string
     */
    public String notifyException() {
        String msg;

        if (this.getCause() != null) {
            msg = String.format("%s\nNative Exception Type: %s\nNative Exception Message: %s\n", this.getMessage(), getCause().getClass(), getCause()
                    .getMessage());
        } else {
            msg = this.getMessage();
        }
        return msg;
    }

}
