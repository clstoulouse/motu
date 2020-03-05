package fr.cls.atoll.motu.web.dal.request.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.status.data.DownloadStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.NormalStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.config.xml.model.RequestStatusRedisConfig;
import fr.cls.atoll.motu.web.dal.request.status.jedis.MotuJedisClient;

public class DALRedisStatusManager implements IDALRequestStatusManager {

    private static final String ACTION_NAME = "actionName";
    private static final String ACTION_CODE = "actionCode";
    private static final String USER_ID = "userId";
    private static final String USER_HOST = "userHost";
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
    private static final String LOCAL_URI = "localUri";
    private static final String REMOTE_URI = "remoteUri";

    private static final String DOWNLOAD_STATUS_TYPE = "DownloadStatus";
    private static final String NORMAL_STATUS_TYPE = "NormalStatus";

    private static final Logger LOGGER = LogManager.getLogger();
    private MotuJedisClient jedisClient;
    private String idPrefix;
    private String identManager;
    private RequestStatusRedisConfig redisConfig;

    @Override
    public void onMotuConfigUpdate(MotuConfig newMotuConfig) throws MotuException {
        RequestStatusRedisConfig newRedisCfg = newMotuConfig.getRedisConfig();
        if (newRedisCfg != null && redisConfig != null && !isRedisCfgEquals(redisConfig, newRedisCfg)) {
            init();
            LOGGER.info("Redis configuration has been updated to: " + redisConfig.getHost() + ":" + redisConfig.getPort() + ", isRedisCluster:"
                    + redisConfig.getIsRedisCluster() + ", prefix=" + redisConfig.getPrefix());
        }
    }

    private boolean isRedisCfgEquals(RequestStatusRedisConfig mc1, RequestStatusRedisConfig mc2) {
        return mc1.getHost().equalsIgnoreCase(mc2.getHost()) || mc1.getPort() == mc2.getPort() || mc1.getPrefix().equalsIgnoreCase(mc2.getPrefix())
                || mc1.getIsRedisCluster() == mc2.getIsRedisCluster();
    }

    @Override
    public void init() throws MotuException {
        redisConfig = DALManager.getInstance().getConfigManager().getMotuConfig().getRedisConfig();
        String host = redisConfig.getHost();
        int port = redisConfig.getPort();
        boolean isRedisCluster = redisConfig.getIsRedisCluster();
        String prefix = redisConfig.getPrefix();
        jedisClient = new MotuJedisClient(isRedisCluster, host, port);
        idPrefix = prefix + ":";
        identManager = prefix + "-identManager";

        if (!jedisClient.isConnected()) {
            String msg = String
                    .format("Unable to establish connection with Redis DB. Check motuConfiguration.xml <redisConfig host=%s port=%d isRedisCluster=%b prefix=%s> parameters.",
                            host,
                            port,
                            isRedisCluster,
                            prefix);
            LOGGER.error(msg);
            throw new MotuException(ErrorType.BAD_PARAMETERS, msg);
        }

        if (!jedisClient.exists(identManager)) {
            jedisClient.set(identManager, "0");
        }
    }

    @Override
    public RequestStatus getRequestStatus(String requestId) {
        boolean requestIdExists = jedisClient.exists(requestId);
        if (requestIdExists) {
            Map<String, String> requestStatus = jedisClient.hgetAll(requestId);
            return requestStatusRedisUnserialization(requestId, requestStatus);
        } else {
            return null;
        }
    }

    @Override
    public String addNewRequestStatus(RequestStatus requestStatus) throws MotuException {
        String requestId = null;
        requestId = idPrefix + Long.toString(System.currentTimeMillis()) + "-" + jedisClient.getRedisIdent(identManager);
        jedisClient.hmset(requestId, requestStatusRedisSerialization(requestStatus));
        requestStatus.setRequestId(requestId);
        return requestId;
    }

    @Override
    public boolean updateRequestStatus(RequestStatus requestStatus) {
        boolean result = true;

        if (jedisClient.exists(requestStatus.getRequestId())) {
            jedisClient.hmset(requestStatus.getRequestId(), requestStatusRedisSerialization(requestStatus));
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
            result.put(currentKey, requestStatusRedisUnserialization(currentKey, jedisClient.hgetAll(currentKey)));
        }
        return result;
    }

    @Override
    public Map<String, DownloadStatus> getDownloadRequestStatus() {
        Set<String> keys = jedisClient.keys(idPrefix + "*");
        Map<String, Map<String, String>> result = new HashMap<>();
        for (String currentKey : keys) {
            result.put(currentKey, jedisClient.hgetAll(currentKey));
        }
        return result.entrySet().parallelStream().filter(entry -> DOWNLOAD_STATUS_TYPE.equals(entry.getValue().get(TYPE))).collect(Collectors
                .toConcurrentMap(Map.Entry::getKey, entry -> downloadStatusRedisUnserialization(entry.getKey(), entry.getValue())));
    }

    @Override
    public long[] getPendingAndInProgressDownloadRequestNumber() {
        Set<String> keys = jedisClient.keys(idPrefix + "*");
        Map<String, Map<String, String>> result = new HashMap<>();
        for (String currentKey : keys) {
            result.put(currentKey, jedisClient.hgetAll(currentKey));
        }
        ConcurrentMap<Integer, Long> counts = result.entrySet().parallelStream()
                .filter(entry -> DOWNLOAD_STATUS_TYPE.equals(entry.getValue().get(TYPE)))
                .collect(Collectors.groupingByConcurrent(entry -> Integer.parseInt(entry.getValue().get(CODE)), Collectors.counting()));
        return new long[] { counts.get(StatusModeType.PENDING.value()), counts.get(StatusModeType.INPROGRESS.value()) };
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

    private Map<String, String> requestStatusRedisSerialization(RequestStatus requestStatus) {
        Map<String, String> result = new HashMap<>();
        result.put(ACTION_NAME, unNullableValue(requestStatus.getActionName()));
        result.put(ACTION_CODE, unNullableValue(requestStatus.getActionCode()));
        result.put(USER_ID, unNullableValue(requestStatus.getUserId()));
        result.put(USER_HOST, unNullableValue(requestStatus.getUserHost()));
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
            result.put(LOCAL_URI, unNullableValue(downloadRequest.getLocalUri()));
            result.put(REMOTE_URI, unNullableValue(downloadRequest.getRemoteUri()));
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

    private RequestStatus requestStatusRedisUnserialization(String requestId, Map<String, String> status) {
        RequestStatus statusResult;
        if (DOWNLOAD_STATUS_TYPE.equals(status.get(TYPE))) {
            DownloadStatus downloadStatus = new DownloadStatus();

            downloadStatus.setStatusCode(status.get(CODE));
            downloadStatus.setMessage(status.get(MESSAGE));
            downloadStatus.setSize(status.get(SIZE));
            downloadStatus.setDateProc(status.get(DATE_PROC));
            downloadStatus.setScriptVersion(status.get(SCRIPT_VERSION));
            downloadStatus.setOutputFileName(status.get(OUTPUT_FILE_NAME));
            downloadStatus.setLocalUri(status.get(LOCAL_URI));
            downloadStatus.setRemoteUri(status.get(REMOTE_URI));

            statusResult = downloadStatus;
        } else {
            NormalStatus normalStatus = new NormalStatus();

            normalStatus.setParameters(status.get(PARAMETERS));

            statusResult = normalStatus;
        }
        statusResult.setRequestId(requestId);
        statusResult.setActionName(status.get(ACTION_NAME));
        statusResult.setActionCode(status.get(ACTION_CODE));
        statusResult.setStatus(status.get(STATUS));
        statusResult.setTime(status.get(TIME));
        statusResult.setUserId(status.get(USER_ID));
        statusResult.setUserHost(status.get(USER_HOST));

        return statusResult;
    }

    private DownloadStatus downloadStatusRedisUnserialization(String requestId, Map<String, String> status) {

        DownloadStatus downloadStatus = new DownloadStatus();

        downloadStatus.setRequestId(requestId);
        downloadStatus.setStatusCode(status.get(CODE));
        downloadStatus.setMessage(status.get(MESSAGE));
        downloadStatus.setSize(status.get(SIZE));
        downloadStatus.setDateProc(status.get(DATE_PROC));
        downloadStatus.setScriptVersion(status.get(SCRIPT_VERSION));
        downloadStatus.setOutputFileName(status.get(OUTPUT_FILE_NAME));
        downloadStatus.setLocalUri(status.get(LOCAL_URI));
        downloadStatus.setRemoteUri(status.get(REMOTE_URI));
        downloadStatus.setActionName(status.get(ACTION_NAME));
        downloadStatus.setActionCode(status.get(ACTION_CODE));
        downloadStatus.setStatus(status.get(STATUS));
        downloadStatus.setTime(status.get(TIME));
        downloadStatus.setUserId(status.get(USER_ID));
        downloadStatus.setUserHost(status.get(USER_HOST));

        return downloadStatus;
    }
}
