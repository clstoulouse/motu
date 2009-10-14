package fr.cls.atoll.motu.processor.wps.framework;

import opendap.servlet.GetAsciiHandler;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.processor.opengis.wps100.ExecuteResponse;
import fr.cls.atoll.motu.processor.opengis.wps100.StatusType;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-10-14 14:11:06 $
 */
public class MotuExecuteResponse {

    /**
     * Constructeur.
     * @throws MotuException 
     */
    public MotuExecuteResponse(ExecuteResponse executeResponse) throws MotuException {
        if (executeResponse == null) {
            throw new MotuException("MotuExecuteResponse constructor - enable to process - executeResponse parameter is null");
        }
        this.executeResponse = executeResponse;
    }
    
    ExecuteResponse executeResponse = null;

    public ExecuteResponse getExecuteResponse() {
        return executeResponse;
    }
    
    public StatusType getStatus() {
        if (executeResponse == null) {
            return null;
        }
        
        return executeResponse.getStatus();
        
    }
    public String getStatusAsString() {
        StatusType statusType = getStatus();
        if (statusType == null) {
            return null;
        }
        String status = "";
        if (statusType.getProcessAccepted() != null) {
        }
        return status;
        
    }


}
