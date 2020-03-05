package fr.cls.atoll.motu.web.dal.request.status;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.status.data.DownloadStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;

public class DALLocalStatusManager implements IDALRequestStatusManager {

    private Map<String, RequestStatus> requestStatusMap;

    public DALLocalStatusManager() {
        requestStatusMap = new HashMap<>();
    }

    @Override
    public void init() {
        // nothing to do
    }

    @Override
    public RequestStatus getRequestStatus(String requestId) {
        RequestStatus requestStatus = null;
        if (requestStatusMap.containsKey(requestId)) {
            requestStatus = requestStatusMap.get(requestId);
        }
        return requestStatus;
    }

    @Override
    public String addNewRequestStatus(RequestStatus request) throws MotuException {
        String requestId = computeRequestId();
        request.setRequestId(requestId);
        requestStatusMap.put(requestId, request);
        return requestId;
    }

    @Override
    public boolean updateRequestStatus(RequestStatus request) {
        boolean result = true;
        if (requestStatusMap.containsKey(request.getRequestId())) {
            requestStatusMap.put(request.getRequestId(), request);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public Map<String, RequestStatus> getAllRequestStatus() {
        return Collections.unmodifiableMap(requestStatusMap);
    }

    @Override
    public Map<String, DownloadStatus> getDownloadRequestStatus() {
        return requestStatusMap.entrySet().parallelStream().filter(entry -> DownloadStatus.class.isAssignableFrom(entry.getValue().getClass()))
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, entry -> DownloadStatus.class.cast(entry.getValue())));
    }

    @Override
    public boolean removeRequestStatus(String requestId) {
        requestStatusMap.remove(requestId);
        return true;
    }

    @Override
    public Set<String> getAllRequestId() {
        return requestStatusMap.keySet();
    }

    private String computeRequestId() {
        String requestId = null;
        do {
            requestId = Long.toString(System.currentTimeMillis());
        } while (requestStatusMap.containsKey(requestId));
        return requestId;
    }

    @Override
    public void onMotuConfigUpdate(MotuConfig newMotuConfig) {
        // nothing to do
    }

    @Override
    public long[] getPendingAndInProgressDownloadRequestNumber() {
        ConcurrentMap<Integer, Long> counts = requestStatusMap.entrySet().parallelStream()
                .filter(entry -> DownloadStatus.class.isAssignableFrom(entry.getValue().getClass()))
                .collect(Collectors.groupingByConcurrent(entry -> Integer.parseInt(entry.getValue().getStatusCode()), Collectors.counting()));
        return new long[] { counts.get(StatusModeType.PENDING.value()), counts.get(StatusModeType.INPROGRESS.value()) };

    }

}
