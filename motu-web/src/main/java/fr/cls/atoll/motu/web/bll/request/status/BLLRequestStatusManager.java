package fr.cls.atoll.motu.web.bll.request.status;

import java.util.Map;
import java.util.Set;

import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;
import fr.cls.atoll.motu.web.dal.DALManager;

public class BLLRequestStatusManager implements IBLLRequestStatusManager {

    public BLLRequestStatusManager() {
    }

    @Override
    public Map<String, RequestStatus> getAllRequestStatus() {
        return DALManager.getInstance().getRequestManager().getDalRequestStatusManager().getAllRequestStatus();
    }

    @Override
    public Set<String> getAllRequestId() {
        return DALManager.getInstance().getRequestManager().getDalRequestStatusManager().getAllRequestId();
    }

}
