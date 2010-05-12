package fr.cls.atoll.motu.library.misc.netcdf;

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
