package fr.cls.atoll.motu.library.misc.queueserver;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class ScheduleCleanJob implements StatefulJob {

    /**
     * The Class ExtractedFileFilter.
     */
    public class ExtractedFileToDeleteFilter implements FileFilter {

        /** The file patterns. */
        private String filePatterns = null;

        /** The time ref. */
        private Long timeRef = null;

        /**
         * The Constructor.
         * 
         * @param timeRef the time ref
         * @param filePatterns the file patterns
         */
        public ExtractedFileToDeleteFilter(String filePatterns, Long timeRef) {
            this.filePatterns = filePatterns;
            this.timeRef = timeRef;
        }

        /** {@inheritDoc} */
        public boolean accept(File file) {
            if (!file.isFile() || filePatterns == null) {
                return false;
            }
            boolean match = file.getName().matches(filePatterns);

            if (timeRef == null) {
                return match;
            }
            if (!match) {
                return false;
            }
            return file.lastModified() <= timeRef.longValue();
        }

    }

    /**
     * The Class FileLastModifiedComparator.
     */
    public class FileLastModifiedComparator implements Comparator<File> {

        /**
         * Constructeur.
         */
        public FileLastModifiedComparator() {
        }

        /** {@inheritDoc} */
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

    /** The Constant ERRORS_KEY_MAP. */
    public static final String ERRORS_KEY_MAP = "Errors";

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(ScheduleCleanJob.class);

    /**
     * Constructor.
     */
    public ScheduleCleanJob() {
    }

    /**
     * Gets the recurse file length.
     * 
     * @param dirOrFileToScan the dir or file to scan
     * 
     * @return the recurse file length
     */
    public static long getRecurseFileLength(File dirOrFileToScan) {
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
     * Gets the recurse file length as kilo bytes.
     * 
     * @param dirOrFileToScan the dir or file to scan
     * 
     * @return the recurse file length as kilo bytes
     */
    public static int getRecurseFileLengthAsKiloBytes(File dirOrFileToScan) {
        return (int) (ScheduleCleanJob.getRecurseFileLength(dirOrFileToScan) / 1024);
    }

    /**
     * Gets the recurse file length as mega bytes.
     * 
     * @param dirOrFileToScan the dir or file to scan
     * 
     * @return the recurse file length as mega bytes
     */
    public static int getRecurseFileLengthAsMegaBytes(File dirOrFileToScan) {
        return ScheduleCleanJob.getRecurseFileLengthAsKiloBytes(dirOrFileToScan) / 1024;
    }

    /**
     * Clean extracted file.
     * 
     * @param context the context
     * 
     * @throws JobExecutionException the job execution exception
     */
    public void cleanExtractedFile(JobExecutionContext context) throws JobExecutionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanExtractedFile(JobExecutionContext) - entering");
        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getFilePatterns(context));

        // // add temporary extract file extension
        // stringBuffer.append("|.*\\");
        // stringBuffer.append(NetCdfWriter.NETCDF_FILE_EXTENSION_EXTRACT);

        String filePatterns = stringBuffer.toString();

        String dirToScan = getDirToScan(context);

        List<Exception> listError = new ArrayList<Exception>();

        Calendar cal = Calendar.getInstance();
        try {
            int interval = Organizer.getMotuConfigInstance().getCleanExtractionFileInterval();
            if (interval > 0) {
                interval = -interval;
            }
            cal.add(Calendar.MINUTE, interval);
        } catch (MotuException e) {
            LOG.error("cleanExtractedFile(JobExecutionContext)", e);

            listError.add(e);
            context.put(ScheduleCleanJob.ERRORS_KEY_MAP, listError);
            throw new JobExecutionException(
                    String
                            .format("ERROR in SchedulecleanJob.cleanExtractedFile : %d error(s) - Gets and inspects 'List<Exception>' object (context data map key is '%s'",
                                    listError.size(),
                                    ScheduleCleanJob.ERRORS_KEY_MAP));
        }

        Long timeRef = cal.getTimeInMillis();

        // This filter only returns directories
        FileFilter fileFilter = new ExtractedFileToDeleteFilter(filePatterns, timeRef);
        File directoryToScan = new File(dirToScan);
        File[] files = null;
        files = directoryToScan.listFiles(fileFilter);

        for (File fileToDelete : files) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("cleanExtractedFile(JobExecutionContext) - Deleting file '%s' ", fileToDelete.getPath()));
            }

            boolean isDeleted = fileToDelete.delete();
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("cleanExtractedFile(JobExecutionContext) - file '%s' deleted: '%b'", fileToDelete.getPath(), isDeleted));
            }
        }

        try {

            deleteOlderFilesBeyondCacheSize(context);

        } catch (MotuException e) {
            LOG.error("cleanExtractedFile(JobExecutionContext)", e);

            listError.add(e);
            context.put(ScheduleCleanJob.ERRORS_KEY_MAP, listError);
            throw new JobExecutionException(
                    String
                            .format("ERROR in SchedulecleanJob.cleanExtractedFile : %d error(s) - Gets and inspects 'List<Exception>' object (context data map key is '%s'",
                                    listError.size(),
                                    ScheduleCleanJob.ERRORS_KEY_MAP));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanExtractedFile(JobExecutionContext) - exiting");
        }
    }

    public void cleanTempFile(JobExecutionContext context) throws JobExecutionException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanTempFile(JobExecutionContext) - entering");
        }

        // // add temporary extract file extension
        // stringBuffer.append("|.*\\");
        // stringBuffer.append(NetCdfWriter.NETCDF_FILE_EXTENSION_EXTRACT);

        String filePatterns = getFilePatterns(context);

        List<Exception> listError = new ArrayList<Exception>();

        Calendar cal = Calendar.getInstance();
        try {
            int interval = Organizer.getMotuConfigInstance().getCleanExtractionFileInterval();
            if (interval > 0) {
                interval = -interval;
            }
            cal.add(Calendar.MINUTE, interval);
        } catch (MotuException e) {
            LOG.error("cleanTempFile(JobExecutionContext)", e);

            listError.add(e);
            context.put(ScheduleCleanJob.ERRORS_KEY_MAP, listError);
            throw new JobExecutionException(
                    String
                            .format("ERROR in SchedulecleanJob.cleanTempFile : %d error(s) - Gets and inspects 'List<Exception>' object (context data map key is '%s'",
                                    listError.size(),
                                    ScheduleCleanJob.ERRORS_KEY_MAP));
        }

        Long timeRef = cal.getTimeInMillis();

        // This filter only returns directories
        FileFilter fileFilter = new ExtractedFileToDeleteFilter(filePatterns, timeRef);
        File directoryToScan = new File(System.getProperty("java.io.tmpdir"));
        File[] files = null;
        files = directoryToScan.listFiles(fileFilter);

        for (File fileToDelete : files) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("cleanTempFile(JobExecutionContext) - Deleting file '%s' ", fileToDelete.getPath()));
            }

            boolean isDeleted = fileToDelete.delete();
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("cleanTempFile(JobExecutionContext) - file '%s' deleted: '%b'", fileToDelete.getPath(), isDeleted));
            }
        }

        try {

            deleteOlderFilesBeyondCacheSize(context);

        } catch (MotuException e) {
            LOG.error("cleanTempFile(JobExecutionContext)", e);

            listError.add(e);
            context.put(ScheduleCleanJob.ERRORS_KEY_MAP, listError);
            throw new JobExecutionException(
                    String
                            .format("ERROR in SchedulecleanJob.cleanTempFile : %d error(s) - Gets and inspects 'List<Exception>' object (context data map key is '%s'",
                                    listError.size(),
                                    ScheduleCleanJob.ERRORS_KEY_MAP));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanTempFile(JobExecutionContext) - exiting");
        }
    }

    /**
     * Clean status.
     * 
     * @param context the context
     * 
     * @throws JobExecutionException the job execution exception
     */
    public void cleanStatus(JobExecutionContext context) throws JobExecutionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanStatus(JobExecutionContext) - entering");
        }

        ConcurrentHashMap<Long, StatusModeResponse> requestStatusMap = getStatusMap(context);

        Set<Long> requestIds = requestStatusMap.keySet();
        List<Exception> listError = new ArrayList<Exception>();

        Calendar cal = Calendar.getInstance();
        try {
            int interval = Organizer.getMotuConfigInstance().getCleanRequestInterval();
            if (interval > 0) {
                interval = -interval;
            }
            cal.add(Calendar.MINUTE, interval);
        } catch (MotuException e) {
            LOG.error("cleanStatus(JobExecutionContext)", e);

            listError.add(e);
            context.put(ScheduleCleanJob.ERRORS_KEY_MAP, listError);
            throw new JobExecutionException(
                    String
                            .format("ERROR in SchedulecleanJob.cleanStatus : %d error(s) - Gets and inspects 'List<Exception>' object (context data map key is '%s'",
                                    listError.size(),
                                    ScheduleCleanJob.ERRORS_KEY_MAP));
        }

        Long timeRef = cal.getTimeInMillis();

        List<Long> requestIdToDelete = new ArrayList<Long>();

        for (Long requestId : requestIds) {
            if (requestId <= timeRef) {
                LOG.debug(String.format("cleanStatus(JobExecutionContext) - clean request id: %d (time ref. is %d)", requestId.longValue(), timeRef
                        .longValue()));
                requestIdToDelete.add(requestId);
            }
        }

        requestIds.removeAll(requestIdToDelete);

        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanStatus(JobExecutionContext) - exiting");
        }
    }

    /**
     * {@inheritDoc}.
     * 
     * @param context the context
     * 
     * @throws JobExecutionException the job execution exception
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ScheduleCleanJob.execute(JobExecutionContext) - entering");
        }

        cleanStatus(context);
        cleanExtractedFile(context);
        cleanTempFile(context);

        if (LOG.isDebugEnabled()) {
            LOG.debug("ScheduleCleanJob.execute(JobExecutionContext) - exiting");
        }
    }

    /**
     * Delete older files beyond cache size.
     * 
     * @param context the context
     * @throws MotuException
     */
    private void deleteOlderFilesBeyondCacheSize(JobExecutionContext context) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteOlderFilesBeyondCacheSize() - entering");
        }

        String dirToScan = getDirToScan(context);

        File directoryToScan = new File(dirToScan);

        // gets disk space used by files (in Megabytes).
        int length = ScheduleCleanJob.getRecurseFileLengthAsMegaBytes(directoryToScan);
        int maxAllowedLength = Organizer.getMotuConfigInstance().getExtractionFileCacheSize();

        if (maxAllowedLength == 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("deleteOlderFilesBeyondCacheSize() -  max allowed is zero - no delete - exiting");
            }
            return;
        }

        if (length <= maxAllowedLength) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("deleteOlderFilesBeyondCacheSize() - length (%d Mo) is less than max allowed (%d Mo) - no delete - exiting",
                                        length,
                                        maxAllowedLength));
            }
            return;
        }

        String filePatterns = getFilePatterns(context);
        FileFilter fileFilter = new ExtractedFileToDeleteFilter(filePatterns, null);

        File[] files = null;
        files = directoryToScan.listFiles(fileFilter);

        List<File> listFiles = Arrays.asList(files);

        // Sort file by last modified date/time (older first);
        FileLastModifiedComparator fileLastModifiedComparator = new FileLastModifiedComparator();
        Collections.sort(listFiles, fileLastModifiedComparator);

        for (File fileToDelete : listFiles) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("deleteOlderFilesBeyondCacheSize(JobExecutionContext) - Deleting file '%s' ", fileToDelete.getPath()));
            }

            boolean isDeleted = fileToDelete.delete();
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("deleteOlderFilesBeyondCacheSize(JobExecutionContext) - file '%s' deleted: '%b'",
                                        fileToDelete.getPath(),
                                        isDeleted));
            }

            length = ScheduleCleanJob.getRecurseFileLengthAsMegaBytes(directoryToScan);

            if (length <= maxAllowedLength) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("deleteOlderFilesBeyondCacheSize() - length (%d Mo) <= max allowed (%d Mo) - stop deleting files",
                                            length,
                                            maxAllowedLength));
                }
                break;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("deleteOlderFilesBeyondCacheSize() - length (%d Mo) > max allowed (%d Mo) - delete another file",
                                            length,
                                            maxAllowedLength));
                }

            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteOlderFilesBeyondCacheSize() - exiting");
        }
    }

    /**
     * Gets the dir to scan.
     * 
     * @param context the context
     * 
     * @return the dir to scan
     */
    private String getDirToScan(JobExecutionContext context) {

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        return (String) jobDataMap.get(RequestManagement.DIR_TO_SCAN_KEYMAP);

    }

    /**
     * Gets the file patterns.
     * 
     * @param context the context
     * 
     * @return the file patterns
     */
    private String getFilePatterns(JobExecutionContext context) {

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        return (String) jobDataMap.get(RequestManagement.FILE_PATTERN_KEYMAP);

    }

    /**
     * Gets the status map.
     * 
     * @param context the context
     * 
     * @return the status map
     */
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Long, StatusModeResponse> getStatusMap(JobExecutionContext context) {

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        return (ConcurrentHashMap<Long, StatusModeResponse>) jobDataMap.get(StatusModeResponse.class.getSimpleName());

    }

}
