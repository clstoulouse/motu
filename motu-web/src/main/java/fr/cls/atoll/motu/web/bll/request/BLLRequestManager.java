package fr.cls.atoll.motu.web.bll.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.ObjectFactory;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.web.dal.DALManager;

/**
 * <br>
 * Manage incoming requests:<br>
 * - check that not too much request are sent for a same authenticated user<br>
 * - check that if the request is processed, its result will not fall down TDS or Motu due to a lack of memory
 * for example<br>
 * - ...<br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class BLLRequestManager implements IBLLRequestManager {

    private Map<Long, StatusModeResponse> requestIdList;
    private long lastRequestId;

    public BLLRequestManager() {
        requestIdList = new HashMap<Long, StatusModeResponse>();
        // TODO SMA This class should take code from RequestManagement.getInstance();
    }

    public void init() {

    }

    /** {@inheritDoc} */
    @Override
    public List<Long> getRequestIds() {
        return new ArrayList<Long>(requestIdList.keySet());
    }

    /** {@inheritDoc} */
    @Override
    public StatusModeResponse getResquestStatus(Long requestId_) {
        return requestIdList.get(requestId_);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized long getNewRequestId() {
        long newRqtId = System.currentTimeMillis();
        if (newRqtId == lastRequestId) {
            lastRequestId++;
        } else {
            lastRequestId = newRqtId;
        }
        return lastRequestId;
    }

    /** {@inheritDoc} */
    @Override
    public StatusModeResponse processRequest(ExtractionParameters extractionParameters) {
        long requestId = getNewRequestId();
        StatusModeResponse statusModeResponse = createStatusModeResponse(requestId);
        requestIdList.put(requestId, statusModeResponse);

        DALManager.getInstance().getRequestManager().processRequest(statusModeResponse, extractionParameters);
        return statusModeResponse;
    }

    private StatusModeResponse createStatusModeResponse(long requestId) {
        ObjectFactory objectFactory = new ObjectFactory();
        StatusModeResponse statusModeResponse = objectFactory.createStatusModeResponse();
        statusModeResponse.setCode(ErrorType.OK);
        statusModeResponse.setStatus(StatusModeType.INPROGRESS);
        statusModeResponse.setMsg("request in progress");
        statusModeResponse.setRequestId(requestId);
        return statusModeResponse;
    }

    /** {@inheritDoc} */
    @Override
    public double processProductDataSize(ExtractionParameters extractionParameters) {
        return DALManager.getInstance().getRequestManager().processProductDataSizeRequest(extractionParameters);
    }
}
