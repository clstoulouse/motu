package fr.cls.atoll.motu.library.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.cls.atoll.motu.library.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.intfce.Organizer.Format;
import fr.cls.atoll.motu.library.utils.Zip;
import fr.cls.atoll.motu.library.vfs.VFSManager;
import fr.cls.commons.util.DatePeriod;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.6 $ - $Date: 2009-07-08 13:38:37 $
 */
public class DatasetFtp extends DatasetBase {

    /** The Constant TXT_FILE_EXTENSION_FINAL. */
    public final static String TXT_FILE_EXTENSION_FINAL = ".txt";

    /** The Constant DOWNLOAD_TEMPDIR_PREFIX. */
    public final static String DOWNLOAD_TEMPDIR_PREFIX = "download";

    /**
     * Instantiates a new dataset ftp.
     */
    public DatasetFtp() {
    }

    /**
     * Instantiates a new dataset ftp.
     * 
     * @param product the product
     */
    public DatasetFtp(Product product) {
        super(product);
    }

    /** {@inheritDoc} */
    @Override
    public void computeAmountDataSize() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException,
            MotuNoVarException, NetCdfVariableNotFoundException {
        
        List<DataFile> dataFiles = selectDataFile();

        amountDataSize = DatasetFtp.getAmountDataSize(dataFiles);

    }

    /** {@inheritDoc} */
    @Override
    public void extractData(Format dataOutputFormat) throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException,
            MotuNoVarException, NetCdfVariableNotFoundException, IOException {

        switch (dataOutputFormat) {
        case URL:
            extractDataAsUrlList();
            break;

        case NETCDF:
            extractDataAsZip();
            break;

        default:
            throw new MotuException(String.format("Unknown data output format '%s' (%d) ", dataOutputFormat.name(), dataOutputFormat));

        }

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
        product.setExtractFilename(Organizer.getUniqueFileName(product.getProductId(), DatasetFtp.TXT_FILE_EXTENSION_FINAL));

        List<String> uriFiles = extractPrepare(true, false);

        try {
            FileWriter outputFile = new FileWriter(product.getExtractLocationDataTemp());

            for (String uriFile : uriFiles) {
                outputFile.write(uriFile);
                outputFile.write("\n");
            }
            outputFile.flush();
            outputFile.close();

        } catch (IOException e) {
            throw new MotuException(String.format("Data extraction - I/O error on file '%s'", product.getExtractLocationDataTemp()), e);
        }

        product.moveTempExtractFileToFinalExtractFile();

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
        product.setExtractFilename(Organizer.getUniqueFileName(product.getProductId(), Organizer.ZIP_EXTENSION));

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Product.getExtractionPath());
        stringBuffer.append(Organizer.getUniqueFileName(DatasetFtp.DOWNLOAD_TEMPDIR_PREFIX, null));

        String tempDownloadDir = stringBuffer.toString();

        List<String> uriFiles = extractPrepare(false, true);
        List<String> localFiles = new ArrayList<String>();

        for (String uriFile : uriFiles) {
            URI uri = null;
            try {
                uri = new URI(uriFile);
            } catch (URISyntaxException e) {
                throw new MotuException(String.format("Data extraction - Invalid URI '%s'", uriFile), e);
            }
            File srcFilePath = new File(uri.getPath());

            stringBuffer = new StringBuffer();
            stringBuffer.append(tempDownloadDir.toString());
            stringBuffer.append("/");
            stringBuffer.append(srcFilePath.getName());

            String fileDest = stringBuffer.toString();
            Organizer.getVFSSystemManager().copyFileToLocalFile(uriFile, fileDest);

            localFiles.add(fileDest);
        }

        Zip.zip(product.getExtractLocationDataTemp(), localFiles, false);

        Organizer.deleteDirectory(tempDownloadDir);

        product.moveTempExtractFileToFinalExtractFile();

    }

    /**
     * Select data file.
     * 
     * @return the list< data file>
     */
    protected List<DataFile> selectDataFile() {

        ExtractCriteriaDatetime extractCriteriaDatetime = findCriteriaDatetime();
        if (extractCriteriaDatetime == null) {
            return product.getDataFiles();
        }

        List<DataFile> selected = new ArrayList<DataFile>();

        Date start = extractCriteriaDatetime.getFrom();
        Date end = extractCriteriaDatetime.getTo();

        DatePeriod datePeriod = new DatePeriod(start, end);

        // files are sorted by date (ascending)
        List<DataFile> dataFiles = product.getDataFiles();

        for (DataFile dataFile : dataFiles) {
            Date fileStart = dataFile.getStartCoverageDate().toDate();
            // file start date is greater than criteria end date --> end
            if (fileStart.compareTo(end) > 0) {
                break;
            }
            if (datePeriod.contains(fileStart)) {
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
    protected List<String> extractPrepare(boolean removeUserInfo, boolean checkMaxSize) throws MotuException, MotuExceedingCapacityException {

        List<String> listUrls = new ArrayList<String>();

        List<DataFile> dataFiles = selectDataFile();

        if (checkMaxSize) {
            checkSize(dataFiles);
        }

        if (Organizer.isNullOrEmpty(dataFiles)) {
            throw new MotuException(String.format("No data files corresponding to the selection criteria have been found for product '%s'", product
                    .getProductId()));
        }

        String locationData = "";
        try {

            // removes user info from URI
            locationData = product.getLocationData();
            URI uri = new URI(locationData);

            URI uriExtraction = null;

            if (removeUserInfo) {
                uriExtraction = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
            } else {
                uriExtraction = uri;
            }

            for (DataFile dataFile : dataFiles) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(uriExtraction.toString());
                stringBuffer.append("/");
                if (!Organizer.isNullOrEmpty(dataFile.getPath())) {
                    stringBuffer.append("dataFile.getPath()");
                    stringBuffer.append("/");                    
                }
                stringBuffer.append(dataFile.getName());

                listUrls.add(stringBuffer.toString());
            }

        } catch (URISyntaxException e) {
            throw new MotuException(String.format("Data extraction - Invalid URI '%s'", locationData), e);
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
        amountDataSize = DatasetFtp.getAmountDataSize(dataFiles);
        if (amountDataSize > Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue()) {
            throw new MotuExceedingCapacityException(Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue());
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
        return (DatasetFtp.computeSize(dataFiles)) / (1024 * 1024);
        
    }

}
