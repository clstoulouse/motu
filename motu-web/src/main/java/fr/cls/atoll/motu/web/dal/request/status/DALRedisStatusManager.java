package fr.cls.atoll.motu.web.dal.request.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.status.data.DownloadStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.NormalStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.RequestStatusRedisConfig;
import fr.cls.atoll.motu.web.dal.request.status.jedis.MotuJedisClient;

public class DALRedisStatusManager implements IDALRequestStatusManager {

    private static final String ACTION_NAME = "actionName";
    private static final String ACTION_CODE = "actionCode";
    private static final String USER_ID = "userId";
    private static final String TIME = "time";
    private static final String STATUS = "status";
    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String SIZE = "size";
    private static final String DATE_PROC = "dateProc";
    private static final String SCRIPT_VERSION = "scriptVersion";
    private static final String OUTPUT_FILE_NAME = "outputFileName";
    private static final String PARAMETERS = "parameters";
    private static final String TYPE = "type";

    private static final String DOWNLOAD_STATUS_TYPE = "DownloadStatus";
    private static final String NORMAL_STATUS_TYPE = "NormalStatus";

    private static final Logger LOGGER = LogManager.getLogger();
    private MotuJedisClient jedisClient;
    private String idPrefix;
    private String identManager;

    public DALRedisStatusManager() {
    }

    @Override
    public void init() {
        RequestStatusRedisConfig redisConfig = DALManager.getInstance().getConfigManager().getMotuConfig().getRedisConfig();
        jedisClient = new MotuJedisClient(redisConfig.getIsRedisCluster(), redisConfig.getHost(), redisConfig.getPort());
        idPrefix = redisConfig.getPrefix() + ":";
        identManager = redisConfig.getPrefix() + "-identManager";
        if (!jedisClient.exists(identManager)) {
            jedisClient.set(identManager, "0");
        }
    }

    @Override
    public RequestStatus getRequestStatus(String requestId) {
        boolean requestIdExists = jedisClient.exists(requestId);
        if (requestIdExists) {
            Map<String, String> requestStatus = jedisClient.hgetAll(requestId);
            return requestStatusRedisUnserialization(requestStatus);
        } else {
            return null;
        }
    }

    @Override
    public String addNewRequestStatus(RequestStatus requestStatus) throws MotuException {
        String requestId = null;
        requestId = idPrefix + Long.toString(System.currentTimeMillis()) + "-" + jedisClient.getRedisIdent(identManager);
        jedisClient.hmset(requestId, requestStatusRedisSerialization(requestStatus));
        return requestId;
    }

    @Override
    public boolean updateRequestStatus(String requestId, RequestStatus requestStatus) {
        boolean result = true;

        if (jedisClient.exists(requestId)) {
            jedisClient.hmset(requestId, requestStatusRedisSerialization(requestStatus));
        } else {
            result = false;
        }

        return result;

    }

    @Override
    public Map<String, RequestStatus> getAllRequestStatus() {
        Map<String, RequestStatus> result = new HashMap<>();
        Set<String> keys = jedisClient.keys(idPrefix + "*");

        for (String currentKey : keys) {
            result.put(currentKey, requestStatusRedisUnserialization(jedisClient.hgetAll(currentKey)));
        }
        return result;
    }

    @Override
    public Set<String> getAllRequestId() {
        return jedisClient.keys(idPrefix + "*");
    }

    @Override
    public boolean removeRequestStatus(String requestId) {
        jedisClient.del(requestId);
        return false;
    }

    @Override
    public void setOutputFileName(String requestId, String fileName) {
        RequestStatus requestStatus = getRequestStatus(requestId);
        if (requestStatus instanceof DownloadStatus) {
            ((DownloadStatus) requestStatus).setOutputFileName(fileName);
        }
        updateRequestStatus(requestId, requestStatus);
    }

    private Map<String, String> requestStatusRedisSerialization(RequestStatus requestStatus) {
        Map<String, String> result = new HashMap<>();
        result.put(ACTION_NAME, unNullableValue(requestStatus.getActionName()));
        result.put(ACTION_CODE, unNullableValue(requestStatus.getActionCode()));
        result.put(USER_ID, unNullableValue(requestStatus.getUserId()));
        result.put(TIME, unNullableValue(requestStatus.getTime()));
        result.put(STATUS, unNullableValue(requestStatus.getStatus()));
        if (requestStatus instanceof DownloadStatus) {
            DownloadStatus downloadRequest = (DownloadStatus) requestStatus;
            result.put(TYPE, DOWNLOAD_STATUS_TYPE);
            result.put(CODE, unNullableValue(downloadRequest.getStatusCode()));
            result.put(MESSAGE, unNullableValue(downloadRequest.getMessage()));
            result.put(SIZE, unNullableValue(downloadRequest.getSize()));
            result.put(DATE_PROC, unNullableValue(downloadRequest.getDateProc()));
            result.put(SCRIPT_VERSION, unNullableValue(downloadRequest.getScriptVersion()));
            result.put(OUTPUT_FILE_NAME, unNullableValue(downloadRequest.getOutputFileName()));
        } else {
            NormalStatus normalRequest = (NormalStatus) requestStatus;
            result.put(TYPE, NORMAL_STATUS_TYPE);
            result.put(PARAMETERS, unNullableValue(normalRequest.getParameters()));

        }

        return result;
    }

    private String unNullableValue(String value) {
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    private RequestStatus requestStatusRedisUnserialization(Map<String, String> status) {
        RequestStatus statusResult;
        if (DOWNLOAD_STATUS_TYPE.equals(status.get(TYPE))) {
            DownloadStatus downloadStatus = new DownloadStatus();

            downloadStatus.setStatusCode(status.get(CODE));
            downloadStatus.setMessage(status.get(MESSAGE));
            downloadStatus.setSize(status.get(SIZE));
            downloadStatus.setDateProc(status.get(DATE_PROC));
            downloadStatus.setScriptVersion(status.get(SCRIPT_VERSION));
            downloadStatus.setOutputFileName(status.get(OUTPUT_FILE_NAME));

            statusResult = downloadStatus;
        } else {
            NormalStatus normalStatus = new NormalStatus();

            normalStatus.setParameters(status.get(PARAMETERS));

            statusResult = normalStatus;
        }

        statusResult.setActionName(status.get(ACTION_NAME));
        statusResult.setActionCode(status.get(ACTION_CODE));
        statusResult.setStatus(status.get(STATUS));
        statusResult.setTime(status.get(TIME));
        statusResult.setUserId(status.get(USER_ID));

        return statusResult;
    }
}
