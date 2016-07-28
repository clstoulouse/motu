package fr.cls.atoll.motu.web.bll.request.cleaner;

import java.io.File;
import java.io.FileFilter;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class ExtractedFileToDeleteFilter implements FileFilter {

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
