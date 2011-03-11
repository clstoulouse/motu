/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.library.misc.data;

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
import org.joda.time.Period;

import fr.cls.atoll.motu.library.converter.DateUtils;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.intfce.Organizer.Format;
import fr.cls.atoll.motu.library.misc.utils.Zip;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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
            throw new MotuException(String.format("Unknown data output format '%s' (%d) ", dataOutputFormat.name(), dataOutputFormat.value()));

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

        List<String> uriFiles = extractPrepare(false, true, false);

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

        List<String> uriFiles = extractPrepare(false, false, true);
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

        Interval datePeriod = new Interval(start.getTime(), end.getTime());

        // files are sorted by date (ascending)
        List<DataFile> dataFiles = product.getDataFiles();

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
    protected List<String> extractPrepare(boolean removeUserLogin, boolean removeUserPwd, boolean checkMaxSize) throws MotuException, MotuExceedingCapacityException {

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
            if ((! removeUserLogin ) && (! removeUserPwd )) {
                // Don't remove login and pwd
                uriExtraction = uri;                
            } else if (removeUserLogin) {
                // remove login also remove pwd
                uriExtraction = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
            } else if (removeUserPwd){
                // Remove only pwd
                String theUserInfo = uri.getUserInfo();
                if (!Organizer.isNullOrEmpty(theUserInfo)) {
                    String userInfo[] = theUserInfo.split(":");
                    if (userInfo.length >= 1) {
                        uriExtraction = new URI(uri.getScheme(), userInfo[0], uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
                    } else {
                        uriExtraction = uri;                
                    }
                }
            }

            for (DataFile dataFile : dataFiles) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(uriExtraction.toString());
                stringBuffer.append("/");
                if (!Organizer.isNullOrEmpty(dataFile.getPath())) {
                    stringBuffer.append(dataFile.getPath());
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

    /** {@inheritDoc} */
    @Override
    public List<String> addVariables(List<String> listVar) throws MotuException {
        // There is no variable criteria on FTP dataset
        return listVar;
    }

    /** {@inheritDoc} */
    @Override
    public void updateVariables(List<String> listVar) throws MotuException {
        // There is no variable criteria on FTP dataset
        // Do nothing
    }

}
