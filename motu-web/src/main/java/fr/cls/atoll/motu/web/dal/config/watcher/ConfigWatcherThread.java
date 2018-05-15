package fr.cls.atoll.motu.web.dal.config.watcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ConfigWatcherThread extends Thread {

    private static final Logger LOGGER = LogManager.getLogger();
    private File fMotuConfig;

    public ConfigWatcherThread(File fMotuConfig_) {
        super("ConfigService watcher:" + fMotuConfig_.getName());
        fMotuConfig = fMotuConfig_;
    }

    @Override
    public void run() {
        try {
            initAndStartConfigWatcher(fMotuConfig);
        } catch (IOException e) {
            LOGGER.error("Error while initAndStartConfigWatcher: " + fMotuConfig.getAbsolutePath(), e);
        }
    }

    private void initAndStartConfigWatcher(File fMotuConfig_) throws IOException {
        ConfigWatcher wc = new ConfigWatcher(fMotuConfig_, StandardWatchEventKinds.ENTRY_MODIFY) {

            @Override
            protected void onNewFileEvent(File filename) {
                LOGGER.info("[ConfigWatcher] Event on file: " + filename);
                onMotuConfigurationUpdated(filename);
            }

        };
        wc.startWatching();
    }

    public abstract void onMotuConfigurationUpdated(File configFile);
}
