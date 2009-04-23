package fr.cls.atoll.motu.processor.wps;

import org.apache.log4j.Logger;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.ProcessletInput;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.queueserver.RequestManagement;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-04-23 14:16:09 $
 */
public class WPSRequestManagement {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(WPSRequestManagement.class);

    private static WPSRequestManagement instance;

    private ConcurrentMap<ProcessletInputs, MotuWPSProcessData> motuWPSProcessDataMap = new ConcurrentHashMap<ProcessletInputs, MotuWPSProcessData>();

    public void clearMotuWPSProcessDataMap() {
        motuWPSProcessDataMap.clear();
    }
    public MotuWPSProcessData getMotuWPSProcessData(ProcessletInputs key) {
        return motuWPSProcessDataMap.get(key);
    }

    public boolean isRequestStatusMapEmpty() {
        return motuWPSProcessDataMap.isEmpty();
    }
    
    public MotuWPSProcessData putIfAbsentMotuWPSProcessData(ProcessletInputs key, MotuWPSProcessData value) {
        return motuWPSProcessDataMap.putIfAbsent(key, value);
    }
     public MotuWPSProcessData putMotuWPSProcessData(ProcessletInputs key, MotuWPSProcessData value) {
        return motuWPSProcessDataMap.put(key, value);
    }

     public MotuWPSProcessData removeMotuWPSProcessData(ProcessletInputs key) {
        return motuWPSProcessDataMap.remove(key);
    }

     public MotuWPSProcessData replaceMotuWPSProcessData(ProcessletInputs key, MotuWPSProcessData value) {
        return motuWPSProcessDataMap.replace(key, value);
    }

     public boolean requestStatusMapContainsKey(ProcessletInputs key) {
        return motuWPSProcessDataMap.containsKey(key);
    }

     public Set<ProcessletInputs> motuWPSProcessDataKeySet() {
        return motuWPSProcessDataMap.keySet();
    }

     public int motuWPSProcessDataMapSize() {
        return motuWPSProcessDataMap.size();
    }

    public WPSRequestManagement() {
    }

    public static WPSRequestManagement getInstance() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstance() - entering");
        }

        if (instance == null) {
            instance = new WPSRequestManagement();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstance() - exiting");
        }
        return instance;
    }

}
