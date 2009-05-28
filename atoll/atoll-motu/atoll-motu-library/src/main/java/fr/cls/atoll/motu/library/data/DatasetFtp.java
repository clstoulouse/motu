package fr.cls.atoll.motu.library.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
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

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.2 $ - $Date: 2009-05-28 09:53:39 $
 */
public class DatasetFtp extends DatasetBase {

    /** The Constant TXT_FILE_EXTENSION_FINAL. */
    public final static String TXT_FILE_EXTENSION_FINAL = ".txt";

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
     */
    protected void extractDataAsUrlList() throws MotuException, FileNotFoundException {

        // Create output file
        product.setExtractFilename(Organizer.getUniqueFileName(product.getProductId(), TXT_FILE_EXTENSION_FINAL));

        List<DataFile> dataFiles = product.getDataFiles();
        if (dataFiles == null) {
            throw new MotuException(String.format("No data files have been found for product '%s'", product.getProductId()));
        }

        String locationData = "";
        try {
            FileWriter outputFile = new FileWriter(product.getExtractLocationDataTemp());

            // removes user info from URI
            locationData = product.getLocationData();
            URI uri = new URI(locationData);
            URI uriExtraction = new URI(uri.getScheme(), "", uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());

            for (DataFile dataFile : dataFiles) {
                outputFile.write(uriExtraction.toString());
                outputFile.write("/");
                outputFile.write(dataFile.getName());
                outputFile.write("\n");
            }
            outputFile.flush();
            outputFile.close();


        } catch (URISyntaxException e) {
            throw new MotuException(String.format("Data extraction - Invalid URI '%s'", locationData), e);
        } catch (IOException e) {
            throw new MotuException(String.format("Data extraction - I/O error on file '%s'", product.getExtractLocationDataTemp()), e);
        }

        product.moveTempExtractFileToFinalExtractFile();

    }

}
