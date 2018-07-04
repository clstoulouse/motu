package fr.cls.atoll.motu.web.dal.request.extractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.Interval;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.library.converter.DateUtils;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuNoVarException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDatetime;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.common.utils.ListUtils;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.common.utils.UnitUtils;
import fr.cls.atoll.motu.web.common.utils.Zip;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DataFile;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.vfs.VFSManager;

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
public class DatasetFileManager extends DALAbstractDatasetManager {

    /** The Constant TXT_FILE_EXTENSION_FINAL. */
    public final static String TXT_FILE_EXTENSION_FINAL = ".txt";

    /** The Constant DOWNLOAD_TEMPDIR_PREFIX. */
    public final static String DOWNLOAD_TEMPDIR_PREFIX = "download";

    /**
     * Constructeur.
     * 
     * @param requestProduct
     */
    public DatasetFileManager(RequestDownloadStatus rds_) {
        super(rds_);
    }

    /**
     * Extract data.
     * 
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @throws MotuException
     * @throws MotuExceedingCapacityException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidDateRangeException
     * @throws MotuInvalidDepthRangeException
     * @throws NetCdfVariableException
     * @throws MotuInvalidLatLonRangeException
     * @throws MotuNoVarException
     * @throws NetCdfVariableNotFoundException
     * @throws IOException
     */
    @Override
    public void extractData() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
            MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, IOException {
        switch (getRequestDownloadStatus().getRequestProduct().getExtractionParameters().getDataOutputFormat()) {
        case URL:
            extractDataAsUrlList();
            break;

        case NETCDF:
            extractDataAsZip();
            break;

        default:
            throw new MotuException(ErrorType.BAD_PARAMETERS, String.format("Unknown data output format '%s' (%d) ",
                                                                            getRequestDownloadStatus().getRequestProduct().getExtractionParameters()
                                                                                    .getDataOutputFormat().name(),
                                                                            getRequestDownloadStatus().getRequestProduct().getExtractionParameters()
                                                                                    .getDataOutputFormat().value()));

        }
    }

    /**
     * Compute amount data size.
     * 
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    @Override
    public double computeAmountDataSize() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException {
        List<DataFile> dataFiles = selectDataFile();
        amountDataSize = getAmountDataSize(dataFiles);
        return amountDataSize;
    }

    /**
     * Extract data as url list.
     * 
     * @throws MotuException the motu exception
     * @throws FileNotFoundException the file not found exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     */
    protected void extractDataAsUrlList() throws MotuException, FileNotFoundException, MotuExceedingCapacityException {
        // Create output file
        String fileName = StringUtils.getUniqueFileName(getRequestDownloadStatus().getRequestProduct().getProduct().getProductId(),
                                                        TXT_FILE_EXTENSION_FINAL);
        getRequestDownloadStatus().getRequestProduct().getRequestProductParameters().setExtractFilename(fileName);
        DALManager.getInstance().getRequestManager().getDalRequestStatusManager().setOutputFileName(getRequestDownloadStatus().getRequestId(),
                                                                                                    fileName);

        List<String> uriFiles = extractPrepare(false, true, true);

        try {
            FileWriter outputFile = new FileWriter(
                    getRequestDownloadStatus().getRequestProduct().getRequestProductParameters().getExtractLocationDataTemp());

            for (String uriFile : uriFiles) {
                outputFile.write(uriFile);
                outputFile.write("\n");
            }
            outputFile.flush();
            outputFile.close();

        } catch (IOException e) {
            throw new MotuException(
                    ErrorType.BAD_PARAMETERS,
                    String.format("Data extraction - I/O error on file '%s'",
                                  getRequestDownloadStatus().getRequestProduct().getRequestProductParameters().getExtractLocationDataTemp()),
                    e);
        }

        moveTempExtractFileToFinalExtractFile();
    }

    /**
     * Extract data as zip.
     * 
     * @throws MotuException the motu exception
     * @throws FileNotFoundException the file not found exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     */
    protected void extractDataAsZip() throws MotuException, FileNotFoundException, MotuExceedingCapacityException {
        // Create output file
        String fileName = StringUtils.getUniqueFileName(getRequestDownloadStatus().getRequestProduct().getProduct().getProductId(), ".zip");
        getRequestDownloadStatus().getRequestProduct().getRequestProductParameters().setExtractFilename(fileName);
        DALManager.getInstance().getRequestManager().getDalRequestStatusManager().setOutputFileName(getRequestDownloadStatus().getRequestId(),
                                                                                                    fileName);

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Product.getExtractionPath());
        stringBuffer.append(StringUtils.getUniqueFileName(DOWNLOAD_TEMPDIR_PREFIX, null));

        String tempDownloadDir = stringBuffer.toString();

        List<String> uriFiles = extractPrepare(false, false, true);
        List<String> localFiles = new ArrayList<String>();

        long d1 = System.nanoTime();

        for (String uriFile : uriFiles) {
            URI uri = null;
            try {
                uri = new URI(uriFile);
            } catch (URISyntaxException e) {
                throw new MotuException(ErrorType.BAD_PARAMETERS, String.format("Data extraction - Invalid URI '%s'", uriFile), e);
            }
            File srcFilePath = new File(uri.getPath());

            stringBuffer = new StringBuffer();
            stringBuffer.append(tempDownloadDir.toString());
            stringBuffer.append("/");
            stringBuffer.append(srcFilePath.getName());

            String fileDest = stringBuffer.toString();

            getVFSSystemManager().copyFileToLocalFile(uriFile, fileDest);
            localFiles.add(fileDest);
        }

        long d2 = System.nanoTime();
        getRequestDownloadStatus().getDataBaseExtractionTimeCounter().addCopyingTime(d2 - d1);

        d1 = System.nanoTime();
        Zip.zip(getRequestDownloadStatus().getRequestProduct().getRequestProductParameters().getExtractLocationDataTemp(), localFiles, false);
        d2 = System.nanoTime();
        getRequestDownloadStatus().getDataBaseExtractionTimeCounter().addCompressingTime(d2 - d1);

        d1 = System.nanoTime();
        getVFSSystemManager().deleteDirectory(tempDownloadDir);
        moveTempExtractFileToFinalExtractFile();
        d2 = System.nanoTime();
        getRequestDownloadStatus().getDataBaseExtractionTimeCounter().addCopyingTime(d2 - d1);

    }

    /**
     * Gets the file system manager.
     * 
     * @return the file system manager
     * 
     * @throws MotuException the motu exception
     */
    public static final VFSManager getVFSSystemManager() throws MotuException {
        VFSManager vfsManager = VFS_MANAGER.get();
        if (vfsManager == null) {
            vfsManager = new VFSManager();
            // throw new MotuException("Error File System manager has not been initialized");
        }
        return vfsManager;
    }

    /** The vfs standard manager. */
    private static final ThreadLocal<VFSManager> VFS_MANAGER = new ThreadLocal<VFSManager>() {
        @Override
        protected synchronized VFSManager initialValue() {
            VFSManager vfsManager = new VFSManager();
            return vfsManager;
        }

    };

    /**
     * Select data file.
     * 
     * @return the list< data file>
     */
    protected List<DataFile> selectDataFile() {
        ExtractCriteriaDatetime extractCriteriaDatetime = getRequestDownloadStatus().getRequestProduct().getRequestProductParameters()
                .findCriteriaDatetime();
        if (extractCriteriaDatetime == null) {
            return getRequestDownloadStatus().getRequestProduct().getProduct().getDataFiles();
        }

        List<DataFile> selected = new ArrayList<DataFile>();

        Date start = extractCriteriaDatetime.getFrom();
        Date end = extractCriteriaDatetime.getTo();

        Interval datePeriod = new Interval(start.getTime(), end.getTime());

        // files are sorted by date (ascending)
        List<DataFile> dataFiles = getRequestDownloadStatus().getRequestProduct().getProduct().getDataFiles();

        for (DataFile dataFile : dataFiles) {
            Date fileStart = dataFile.getStartCoverageDate().toDate();
            // file start date is greater than criteria end date --> end
            if (fileStart.compareTo(end) > 0) {
                break;
            }

            Date fileEnd = dataFile.getEndCoverageDate().toDate();

            Interval filePeriod = new Interval(fileStart.getTime(), fileEnd.getTime());

            if (DateUtils.intersects(filePeriod, datePeriod)) {
                selected.add(dataFile);
            }
        }

        return selected;
    }

    /**
     * Extract prepare.
     * 
     * @param removeUserInfo the remove user info
     * @param checkMaxSize the check max size
     * 
     * @return the list< string>
     * 
     * @throws MotuException the motu exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     */
    protected List<String> extractPrepare(boolean removeUserLogin, boolean removeUserPwd, boolean checkMaxSize)
            throws MotuException, MotuExceedingCapacityException {

        List<String> listUrls = new ArrayList<>();

        List<DataFile> dataFiles = selectDataFile();

        if (checkMaxSize) {
            checkSize(dataFiles);
        }

        if (ListUtils.isNullOrEmpty(dataFiles)) {
            throw new MotuException(
                    ErrorType.BAD_PARAMETERS,
                    String.format("No data file corresponding to the selection criteria have been found for product '%s'",
                                  getRequestDownloadStatus().getRequestProduct().getProduct().getProductId()));
        }

        String locationData = "";
        try {

            // removes user info from URI
            locationData = getRequestDownloadStatus().getRequestProduct().getProduct().getLocationData();
            URI uri = new URI(locationData);

            URI uriExtraction = null;
            if ((!removeUserLogin) && (!removeUserPwd)) {
                // Don't remove login and pwd
                uriExtraction = uri;
            } else if (removeUserLogin) {
                // remove login also remove pwd
                uriExtraction = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
            } else if (removeUserPwd) {
                // Remove only pwd
                String theUserInfo = uri.getUserInfo();
                if (!StringUtils.isNullOrEmpty(theUserInfo)) {
                    String userInfo[] = theUserInfo.split(":");
                    if (userInfo.length >= 1) {
                        uriExtraction = new URI(
                                uri.getScheme(),
                                userInfo[0],
                                uri.getHost(),
                                uri.getPort(),
                                uri.getPath(),
                                uri.getQuery(),
                                uri.getFragment());
                    } else {
                        uriExtraction = uri;
                    }
                }
            }

            for (DataFile dataFile : dataFiles) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(uriExtraction.toString());
                stringBuffer.append("/");
                if (!StringUtils.isNullOrEmpty(dataFile.getPath())) {
                    stringBuffer.append(dataFile.getPath());
                    stringBuffer.append("/");
                }
                stringBuffer.append(dataFile.getName());

                listUrls.add(stringBuffer.toString());
            }

        } catch (URISyntaxException e) {
            throw new MotuException(ErrorType.BAD_PARAMETERS, String.format("Data extraction - Invalid URI '%s'", locationData), e);
        }

        return listUrls;
    }

    /**
     * Compute size.
     * 
     * @param dataFiles the data files
     * 
     * @return the double
     */
    public static double computeSize(List<DataFile> dataFiles) {
        double count = 0.0;

        for (DataFile dataFile : dataFiles) {
            count += dataFile.getWeight();
        }

        return count;

    }

    /**
     * Check size.
     * 
     * @param dataFiles the data files
     * 
     * @throws MotuException the motu exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     */
    public void checkSize(List<DataFile> dataFiles) throws MotuException, MotuExceedingCapacityException {
        // Compute size in Mega-bytes
        amountDataSize = getAmountDataSize(dataFiles);
        if (amountDataSize > BLLManager.getInstance().getConfigManager().getMotuConfig().getMaxSizePerFile().doubleValue()) {
            throw new MotuExceedingCapacityException(BLLManager.getInstance().getConfigManager().getMotuConfig().getMaxSizePerFile().doubleValue());
        }

    }

    /**
     * Gets the amount data size in megabytes.
     * 
     * @param dataFiles the data files
     * 
     * @return the amount data size
     */
    public static double getAmountDataSize(List<DataFile> dataFiles) {
        return UnitUtils.byteToMegaByte(computeSize(dataFiles));// (computeSize(dataFiles)) / (1024 * 1024);
    }

}
