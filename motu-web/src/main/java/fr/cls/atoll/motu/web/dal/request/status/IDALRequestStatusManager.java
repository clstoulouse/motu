package fr.cls.atoll.motu.web.dal.request.status;

import java.util.Map;
import java.util.Set;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.status.data.DownloadStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;

public interface IDALRequestStatusManager {

    void init() throws MotuException;

    RequestStatus getRequestStatus(String requestId);

    String addNewRequestStatus(RequestStatus request) throws MotuException;

    boolean updateRequestStatus(String requestId, RequestStatus request);

    Map<String, RequestStatus> getAllRequestStatus();

    Map<String, DownloadStatus> getDownloadRequestStatus();

    Set<String> getAllRequestId();

    boolean removeRequestStatus(String requestId);

    void setOutputFileName(String requestId, String fileName);

    void onMotuConfigUpdate(MotuConfig newMotuConfig) throws MotuException;

    long[] getPendingAndInProgressDownloadRequestNumber();
}
