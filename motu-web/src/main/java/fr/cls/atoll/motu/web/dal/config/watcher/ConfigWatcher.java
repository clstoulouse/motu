package fr.cls.atoll.motu.web.dal.config.watcher;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ConfigWatcher {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    private File fileToWatch;
    private Kind<?>[] watchEvents;

    public ConfigWatcher(File fileToWatch_) {
        this(fileToWatch_, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
    }

    public ConfigWatcher(File fileToWatch_, Kind<?>... watchEvents_) {
        fileToWatch = fileToWatch_;
        watchEvents = watchEvents_;
    }

    /**
     * Blocking method
     * 
     * @throws IOException
     */
    public void startWatching() throws IOException {
        Path dir = Paths.get(fileToWatch.getParentFile().getAbsolutePath());
        URI uri = dir.toFile().toURI();
        LOGGER.info("ConfigWatcher: Start watching: " + dir.toString() + ", uri=" + uri);
        FileSystem fs = null;
        try {
            fs = FileSystems.getFileSystem(uri);
        } catch (java.lang.IllegalArgumentException e) {
            fs = FileSystems.getFileSystem(new File("/").toURI());
        }
        WatchService watcher = fs.newWatchService();
        try {
            dir.register(watcher, watchEvents, com.sun.nio.file.SensitivityWatchEventModifier.LOW);
        } catch (IOException x) {
            LOGGER.error("Error during watcher registration for file: " + fileToWatch.getAbsolutePath(), x);
        }

        for (;;) {
            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // This key is registered only
                // for ENTRY_CREATE events,
                // but an OVERFLOW event can
                // occur regardless if events
                // are lost or discarded.
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // The filename is the
                // context of the event.
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();

                if (filename.toFile().getName().equals(fileToWatch.getName())) {
                    onNewFileEvent(fileToWatch);
                }
            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events. If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    protected abstract void onNewFileEvent(File fileUpdated);
}
