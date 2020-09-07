package fr.cls.atoll.motu.web.bll.request.cleaner;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.BLLRequestManager;
import fr.cls.atoll.motu.web.bll.request.IBLLRequestManager;
import fr.cls.atoll.motu.web.bll.request.status.data.DownloadStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;
import fr.cls.atoll.motu.web.common.utils.UnitUtils;
import fr.cls.atoll.motu.web.dal.DALManager;

/**
 * Used to clean data in memory from queue server
 * 
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class RequestCleaner implements IRequestCleaner {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    private IBLLRequestManager bllRequestManager;

    public RequestCleaner() {
        bllRequestManager = BLLManager.getInstance().getRequestManager();
    }

    /**
     * Gets the recurse file length.
     * 
     * @param dirOrFileToScan the dir or file to scan
     * 
     * @return the recurse file length
     */
    private static long getRecurseFileLength(File dirOrFileToScan) {
        long bytes = 0;
        File[] files = dirOrFileToScan.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    bytes = bytes + file.length();
                }
                if (file.isDirectory()) {
                    bytes = bytes + getRecurseFileLength(file);
                }
            }
        }
        return bytes;
    }

    /**
     * The Class FileLastModifiedComparator.
     */
    private static class FileLastModifiedComparator implements Comparator<File> {

        /** {@inheritDoc} */
        @Override
        public int compare(File o1, File o2) {
            if (o1.lastModified() > o2.lastModified()) {
                return -1;
            }
            if (o1.lastModified() < o2.lastModified()) {
                return 1;
            }
            return 0;
        }

    }

    /**
     * Delete older files beyond cache size.
     * 
     * @param context the context
     * @throws MotuException
     */
    private void deleteOlderFilesBeyondCacheSize(File folderToScan) {
        // gets disk space used by files (in Megabytes).
        int extractionFileCacheSize = BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionFileCacheSize();
        if (extractionFileCacheSize > 0) {
            long length = Math.round(UnitUtils.byteToMegaByte(getRecurseFileLength(getFolderExtractionPath())));
            if (length > extractionFileCacheSize) {
                File[] files = folderToScan.listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return file.getName().matches(getExtractionFilePatterns());
                    }
                });
                if (files != null) {
                    List<File> listFiles = Arrays.asList(files);
                    // Sort file by last modified date/time (older first);
                    FileLastModifiedComparator fileLastModifiedComparator = new FileLastModifiedComparator();
                    Collections.sort(listFiles, fileLastModifiedComparator);
                    for (File fileToDelete : listFiles) {
                        boolean isDeleted = fileToDelete.delete();
                        LOGGER.info(String.format("deleteOlderFilesBeyondCacheSize - Deleting file '%s : %b' ", fileToDelete.getPath(), isDeleted));
                        length = Math.round(UnitUtils.byteToMegaByte(getRecurseFileLength(folderToScan)));
                        if (length <= extractionFileCacheSize) {
                            break;
                        }
                    }
                }

            }
        }
    }

    /**
     * Clean extracted file.
     * 
     */
    @Override
    public void cleanExtractedFile() {
        LOGGER.info("Start cleanExtractedFile");
        // This filter only returns directories
        File[] files = getFolderExtractionPath()
                .listFiles(new ExtractedFileToDeleteFilter(getExtractionFilePatterns(), getCleanExtractionFileIntervalInMs()));
        if (files != null) {
            for (File fileToDelete : files) {
                boolean isDeleted = fileToDelete.delete();
                LOGGER.info(String.format("cleanExtractedFile - Deleting file '%s : %b' ", fileToDelete.getPath(), isDeleted));
            }
        }
        deleteOlderFilesBeyondCacheSize(getFolderExtractionPath());
    }

    @Override
    public void cleanJavaTempFile() {
        // This filter only returns directories
        FileFilter fileFilter = new ExtractedFileToDeleteFilter(getExtractionFilePatterns(), getCleanExtractionFileIntervalInMs());
        File javaTmpDirFolder = new File(System.getProperty("java.io.tmpdir"));
        File[] files = javaTmpDirFolder.listFiles(fileFilter);

        if (files != null) {
            for (File fileToDelete : files) {
                boolean isDeleted = fileToDelete.delete();
                LOGGER.info(String.format("cleanTempFile - Deleting file '%s : %b' ", fileToDelete.getPath(), isDeleted));
            }
        }
        deleteOlderFilesBeyondCacheSize(javaTmpDirFolder);
    }

    /**
     * Clean request status from bllRequestManager
     * 
     * @param context the context
     * 
     * @throws JobExecutionException the job execution exception
     */
    @Override
    public void cleanRequestStatus() {
        for (String requestId : getAllNonRunningRequestIds()) {
            LOGGER.info("cleanRequestStatus - try to clean requestId=" + requestId);
            bllRequestManager.deleteRequest(requestId);
            LOGGER.info("cleanRequestStatus - clean done for requestId=" + requestId);
        }
    }

    /**
     * A request is not running if it has no RequestDownloadStatus or its end processing time plus the
     * cleanRequestIntervalInMs is greater than current time or its creation time plus 1 hour is greater than
     * current time
     * 
     * @return
     */
    private List<String> getAllNonRunningRequestIds() {
        List<String> allNonRunningRequestIdList = new ArrayList<>();
        for (Entry<String, RequestStatus> entry : DALManager.getInstance().getRequestManager().getDalRequestStatusManager().getAllRequestStatus()
                .entrySet()) {
            String id = entry.getKey();
            RequestStatus rs = entry.getValue();
            if (rs instanceof DownloadStatus) {
                DownloadStatus ds = (DownloadStatus) rs;
                if ((ds.getEndProcessingDateTime() > 0
                        && (ds.getEndProcessingDateTime() + getCleanRequestIntervalInMs()) < System.currentTimeMillis())
                        || ((ds.getCreationDateTime() + BLLRequestManager.REQUEST_TIMEOUT_MSEC) < System.currentTimeMillis())) {
                    allNonRunningRequestIdList.add(id);
                }
            }
        }
        return allNonRunningRequestIdList;
    }

    private String getExtractionFilePatterns() {
        return BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionFilePatterns();
    }

    private long getCleanExtractionFileIntervalInMs() {
        return BLLManager.getInstance().getConfigManager().getMotuConfig().getCleanExtractionFileInterval() * 60 * 1000;
    }

    private long getCleanRequestIntervalInMs() {
        return BLLManager.getInstance().getConfigManager().getMotuConfig().getCleanRequestInterval() * 60 * 1000;
    }

    private File getFolderExtractionPath() {
        return new File(BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath());
    }

}
