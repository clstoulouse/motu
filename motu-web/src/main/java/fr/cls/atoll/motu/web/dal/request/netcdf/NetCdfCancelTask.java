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
package fr.cls.atoll.motu.web.dal.request.netcdf;

import ucar.nc2.util.CancelTask;

/**
 * A class used to set/get errors.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class NetCdfCancelTask implements CancelTask {

    /**
     * Default constructor.
     */
    public NetCdfCancelTask() {
    }

    /**
     * Called routine should check often during the task and cancel the task if it returns true.
     * 
     * @return true if cancelled
     * @see ucar.nc2.util.CancelTask#isCancel()
     */
    public boolean isCancel() {
        return false;
    }

    /**
     * Called routine got an error, so it sets a message for calling program to show to user.
     * 
     * @param msg error message to set
     * @see ucar.nc2.util.CancelTask#setError(java.lang.String)
     */
    public void setError(String msg) {
        this.error = msg;
    }

    /**
     * @return gets error message.
     */
    public String getError() {
        return this.error;
    }

    /**
     * @return true if error message is set.
     */
    public boolean hasError() {
        if (error == null) {
            return false;
        }
        if (error.equals("")) {
            return false;
        }
        return true;
    }

    private String error = null;

}
