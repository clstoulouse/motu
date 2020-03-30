package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.time.Duration;
import java.time.Instant;

public class ConfigServiceState {

    public static final String ADDED = "ADDED";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";

    private final String name;
    private String status = ADDED;
    private Instant lastUpdate = null;
    private Duration lastUpdateDuration = null;

    public ConfigServiceState(String name) {
        this.name = name;
    }

    /**
     * Gets the value of name.
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of status.
     *
     * @return the value of status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of status.
     *
     * @param status the value to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the value of lastUpdate.
     *
     * @return the value of lastUpdate
     */
    public Instant getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Sets the value of lastUpdate.
     *
     * @param instant the value to set
     */
    public void setLastUpdate(Instant instant) {
        this.lastUpdate = instant;
    }

    /**
     * Gets the value of lastUpdateDuration.
     *
     * @return the value of lastUpdateDuration
     */
    public Duration getLastUpdateDuration() {
        return lastUpdateDuration;
    }

    /**
     * Sets the value of lastUpdateDuration.
     *
     * @param lastUpdateDuration the value to set
     */
    public void setLastUpdateDuration(Duration lastUpdateDuration) {
        this.lastUpdateDuration = lastUpdateDuration;
    }

}
