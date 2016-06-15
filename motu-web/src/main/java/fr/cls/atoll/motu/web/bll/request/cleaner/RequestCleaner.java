package fr.cls.atoll.motu.web.bll.request.cleaner;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.IBLLRequestManager;
import fr.cls.atoll.motu.web.common.utils.UnitUtils;

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
    private String extractionFilePattern;
    private File folderExtractionPath;
    private long cleanExtractionFileIntervalInMs;
    private long cleanRequestIntervalInMs;

    public RequestCleaner() {
        bllRequestManager = BLLManager.getInstance().getRequestManager();
        extractionFilePattern = BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionFilePatterns();
        folderExtractionPath = new File(BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath());
        cleanExtractionFileIntervalInMs = BLLManager.getInstance().getConfigManager().getMotuConfig().getCleanExtractionFileInterval() * 60 * 1000;
        cleanRequestIntervalInMs = BLLManager.getInstance().getConfigManager().getMotuConfig().getCleanRequestInterval() * 60 * 1000;
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
        for (File file : files) {
            if (file.isFile()) {
                bytes = bytes + file.length();
            }
            if (file.isDirectory()) {
                bytes = bytes + getRecurseFileLength(file);
            }
        }
        return bytes;
    }

    /**
     * The Class FileLastModifiedComparator.
     */
    private static class FileLastModifiedComparator implements Comparator<File> {

        /**
         * Constructeur.
         */
        public FileLastModifiedComparator() {
        }

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
        long length = Math.round(UnitUtils.toMegaBytes(getRecurseFileLength(folderExtractionPath)));
        if (extractionFileCacheSize > 0 && length > extractionFileCacheSize) {
            File[] files = folderToScan.listFiles(new FileFilter() {

                @Override
                public boolean accept(File file) {
                    return file.getName().matches(extractionFilePattern);
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
                    length = Math.round(UnitUtils.toMegaBytes(getRecurseFileLength(folderToScan)));
                    if (length <= extractionFileCacheSize) {
                        break;
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
        File[] files = folderExtractionPath.listFiles(new ExtractedFileToDeleteFilter(extractionFilePattern, cleanExtractionFileIntervalInMs));
        if (files != null) {
            for (File fileToDelete : files) {
                boolean isDeleted = fileToDelete.delete();
                LOGGER.info(String.format("cleanExtractedFile - Deleting file '%s : %b' ", fileToDelete.getPath(), isDeleted));
            }
        }
        deleteOlderFilesBeyondCacheSize(folderExtractionPath);
    }

    @Override
    public void cleanJavaTempFile() {
        // This filter only returns directories
        FileFilter fileFilter = new ExtractedFileToDeleteFilter(extractionFilePattern, cleanExtractionFileIntervalInMs);
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
        List<Long> requestIds = bllRequestManager.getRequestIds();
        for (Long requestId : requestIds) {
            if ((requestId + cleanRequestIntervalInMs) > System.currentTimeMillis()) {
                bllRequestManager.deleteRequest(requestId);
                LOGGER.info("cleanRequestStatus - requestId=" + requestId);
            }
        }
    }

    /**
     * The Class ExtractedFileFilter.
     */
    public static class ExtractedFileToDeleteFilter implements FileFilter {

        /** The file patterns. */
        private String filePattern = null;

        /** The time ref. */
        private long timeInMsfAfterWhichFileIsAccepted;

        /**
         * The Constructor.
         * 
         * @param timeRef the time ref
         * @param filePattern the file patterns
         */
        public ExtractedFileToDeleteFilter(String filePattern, long timeInMsfAfterWhichFileIsAccepted_) {
            this.filePattern = filePattern;
            this.timeInMsfAfterWhichFileIsAccepted = timeInMsfAfterWhichFileIsAccepted_;
        }

        /** {@inheritDoc} */
        @Override
        public boolean accept(File file) {
            boolean acceptFile = false;
            if (file.isFile() && filePattern != null && timeInMsfAfterWhichFileIsAccepted > 0 && file.getName().matches(filePattern)) {
                acceptFile = ((file.lastModified() + timeInMsfAfterWhichFileIsAccepted) > System.currentTimeMillis());
            }

            return acceptFile;

        }

    }

}
