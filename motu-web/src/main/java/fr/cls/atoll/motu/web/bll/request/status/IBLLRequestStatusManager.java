package fr.cls.atoll.motu.web.bll.request.status;

import java.util.Map;
import java.util.Set;

import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;

public interface IBLLRequestStatusManager {

    Map<String, RequestStatus> getAllRequestStatus();

    Set<String> getAllRequestId();
}
