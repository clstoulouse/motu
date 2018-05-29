package fr.cls.atoll.motu.web.dal.tds.ncss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDatetime;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDepth;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.DateUtils;
import fr.cls.atoll.motu.web.common.utils.ProcessOutputLogguer;
import fr.cls.atoll.motu.web.common.utils.ProcessOutputLogguer.Type;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ParameterMetaData;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import ucar.ma2.Array;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;

/**
 * Class to handle NCSS requests
 * 
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Joan SALA
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */

public class NetCdfSubsetService {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** Size block read in bytes from REST response */
    public final static int BSIZE_READ = 256;

    /** Name of variables, separated by ',' */
    public final static String NCSS_ATTR_VARIABLES = "var";

    /** Point location. units of degrees_east, degrees_north */
    public final static String NCSS_ATTR_LATITUDE = "latitude";
    public final static String NCSS_ATTR_LONGITUDE = "longitude";

    /** Lat/lon bounding box, units of degrees_east, degrees_north */
    public final static String NCSS_ATTR_NORTH = "north";
    public final static String NCSS_ATTR_EAST = "east";
    public final static String NCSS_ATTR_WEST = "west";
    public final static String NCSS_ATTR_SOUTH = "south";

    /** Projection bounding box, in projection coordinate units */
    public final static String NCSS_ATTR_MINX = "minx";
    public final static String NCSS_ATTR_MINY = "miny";
    public final static String NCSS_ATTR_MAXX = "maxx";
    public final static String NCSS_ATTR_MAXY = "maxy";

    /** Take only every nth point (both x and y) */
    public final static String NCSS_ATTR_HORIZSTRIDE = "horizStride";

    /** if present, make output strictly CF compliant by adding lat/lon coordinates if needed */
    public final static String NCSS_ATTR_ADDLATLON = "addLatLon";

    /** Time as a W3C date or "present". The time slice closest to the requested time is returned */
    public final static String NCSS_ATTR_TIME = "time";

    /** Used to specify a time range. Time as a W3C date or "present". Duration as a W3C time duration */
    public final static String NCSS_ATTR_TIME_START = "time_start";
    public final static String NCSS_ATTR_TIME_END = "time_end";
    public final static String NCSS_ATTR_TIME_DURATION = "time_duration";

    /** Must be equal to "all" to have effect */
    public final static String NCSS_ATTR_TEMPORAL = "temporal";

    /** Take only every nth time in the available series */
    public final static String NCSS_ATTR_TIMESTRIDE = "timeStride";

    /** Bounding box requests on grid datasets must have the same vertical levels */
    public final static String NCSS_ATTR_VERTCOORD = "vertCoord";

    /** Used to specify the returned format. */
    public final static String NCSS_ATTR_ACCEPT = "accept";

    /** Used to specify the type on a station feature. subset=stns means we will provide a station list */
    public final static String NCSS_ATTR_SUBSET = "subset";

    /** Used when subset=stns to specify the list of stations in the subset */
    public final static String NCSS_ATTR_STNS = "stns";

    /** Accepted output formats */
    public final static String NCSS_ATTR_FORMAT_NC3 = "netCDF";
    public final static String NCSS_ATTR_FORMAT_NC4 = "netCDF4";
    public final static String NCSS_ATTR_FORMAT_NC4EXT = "netCDF4ext";

    /** Attributes of the subset */
    protected ExtractCriteriaDatetime timeSubset;
    protected ExtractCriteriaLatLon geoSubset;
    protected ExtractCriteriaDepth depthSubset;
    protected Range depthRange;
    protected Array depthAxis;
    protected Set<String> varSubset;
    protected String outputDir;
    protected String outputFile;
    protected OutputFormat outputFormat;
    protected String ncssURL;
    protected ProductMetaData productMetadata;

    /** Control boolean for concatenation */
    private boolean multipleRequest;

    /** Control variables for depth concat-extraction */
    private Path depthTempDir;
    private String depthTempFname;
    private double depthSelected;

    private long readingTimeInNanoSec = 0L;
    private long writingTimeInNanoSec = 0L;

    /**
     * Setter of the time subset setup .
     * 
     * @return
     */
    public void setTimeSubset(ExtractCriteriaDatetime in) {
        timeSubset = in;
    }

    /**
     * Setter of the geographical subset setup .
     * 
     * @return
     */
    public void setGeoSubset(ExtractCriteriaLatLon in) {
        geoSubset = in;
    }

    /**
     * Setter of the depth subset setup .
     * 
     * @return
     */
    public void setDepthSubset(ExtractCriteriaDepth in) {
        depthSubset = in;
    }

    /**
     * Setter of the range depth subset .
     * 
     * @return
     */
    public void setDepthRange(Range in) {
        depthRange = in;
    }

    /**
     * Setter of the z-axis depth subset .
     * 
     * @return
     */
    public void setDepthAxis(Array in) {
        depthAxis = in;
    }

    /**
     * Setter of the variables subset setup .
     * 
     * @return
     */
    public void setVariablesSubset(Set<String> in) {
        varSubset = in;
    }

    /**
     * Setter of the extraction directory path .
     * 
     * @return
     */
    public void setOutputDir(String in) {
        outputDir = in;
    }

    /**
     * Setter of the unique name of the file to be created .
     * 
     * @return
     */
    public void setOutputFile(String in) {
        outputFile = in;
    }

    /**
     * Setter of the output format (netcdf/netcdf4)
     * 
     * @return
     */
    public void setOutputFormat(OutputFormat in) {
        outputFormat = in;
    }

    /**
     * Setter of the url to access NCSS REST service
     * 
     * @return
     */
    public void setncssURL(String in) {
        ncssURL = in;
    }

    /**
     * Returns the time subset setup .
     * 
     * @return
     */
    public ExtractCriteriaDatetime getTimeSubset() {
        return timeSubset;
    }

    /**
     * Returns the geographical subset setup .
     * 
     * @return
     */
    public ExtractCriteriaLatLon getGeoSubset() {
        return geoSubset;
    }

    /**
     * Returns the depth subset setup .
     * 
     * @return
     */
    public ExtractCriteriaDepth getDepthSubset() {
        return depthSubset;
    }

    /**
     * Returns the depth axis subset setup .
     * 
     * @return
     */
    public Array getDepthAxis() {
        return depthAxis;
    }

    /**
     * Returns the depth subset setup .
     * 
     * @return
     */
    public Range getDepthRange() {
        return depthRange;
    }

    /**
     * Returns the variables subset setup .
     * 
     * @return
     */
    public Set<String> getVariablesSubset() {
        return varSubset;
    }

    /**
     * Returns the extraction directory path .
     * 
     * @return
     */
    public String getOutputDir() {
        return outputDir;
    }

    /**
     * Returns the unique name of the file to be created .
     * 
     * @return
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * Getter of the output format (netcdf/netcdf4)
     * 
     * @return
     */
    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    /**
     * Returns the url of the NCSS subset service for 1 dataset
     * 
     * @return
     */
    public String getncssURL() {
        return ncssURL;
    }

    /**
     * Make several requests to NCSS and then use CDO to concat them
     * 
     * @throws MotuException
     * @throws IOException
     * @throws InterruptedException
     */
    public void concatDepths() throws MotuException, IOException, InterruptedException {
        // Prepare multiple request mode
        multipleRequest = true;

        long startConcat = System.currentTimeMillis();

        // Create folder to store intermediate files
        depthTempDir = Files.createTempDirectory("motu_depth_concat");
        try {
            depthTempDir.toFile().deleteOnExit();

            Set<String> depthVariable = new HashSet<>();
            Set<String> noDepthVariable = new HashSet<>();

            CoordinateAxis heigthCoordAxis = productMetadata.getCoordinateAxes().get(AxisType.Height);

            for (String currentVariableName : varSubset) {
                ParameterMetaData currentVariable = productMetadata.findVariable(currentVariableName);
                List<Dimension> currentVarDimensionList = currentVariable.getDimensions();
                for (Dimension currentDimension : currentVarDimensionList) {
                    if (currentDimension.getName().equals(heigthCoordAxis.getName())) {
                        depthVariable.add(currentVariableName);
                    }
                }

                if (!depthVariable.contains(currentVariableName)) {
                    noDepthVariable.add(currentVariableName);
                }
            }

            if (noDepthVariable.size() > 0) {
                depthTempFname = "no_depth_concat";
                unitRequestNCSS(noDepthVariable);
            }

            // For every depth
            if (depthVariable.size() > 0) {
                int numberOfDigit = computeNumberOfDigit(depthRange.last());
                for (int z = depthRange.first(); z <= depthRange.last(); z += depthRange.stride()) {
                    depthSelected = depthAxis.getDouble(z);
                    depthTempFname = "depth_concat_" + String.format("%0" + numberOfDigit + "d", z);
                    unitRequestNCSS(depthVariable);
                }
            }

            long startCdo = System.currentTimeMillis();

            // Concatenate with NCO
            String cmd = "cdo.sh merge " + depthTempDir.toString() + "/* " + Paths.get(outputDir, outputFile);
            Process p = Runtime.getRuntime().exec(cmd);
            new Thread(new ProcessOutputLogguer(new BufferedReader(new InputStreamReader(p.getInputStream())), LOGGER, Type.INFO)).start();
            new Thread(new ProcessOutputLogguer(new BufferedReader(new InputStreamReader(p.getErrorStream())), LOGGER, Type.ERROR)).start();
            int exitValue = p.waitFor();

            LOGGER.info("Generate " + outputFile + " Concat duration : " + DateUtils.getDurationMinSecMsec(System.currentTimeMillis() - startConcat));
            LOGGER.info("Generate " + outputFile + " Cdo script duration : "
                    + DateUtils.getDurationMinSecMsec(System.currentTimeMillis() - startCdo));

            if (exitValue != 0) {
                throw new MotuException(ErrorType.NETCDF_GENERATION, "The generation of the NC file failled. See the log for more information.");
            }
        } finally {
            // Cleanup directory and intermediate files (right away once concat)
            FileUtils.deleteDirectory(depthTempDir.toFile());
        }
    }

    public boolean hasVariablesWithDepthDim() {
        Set<String> depthVariable = new HashSet<>();
        Set<String> noDepthVariable = new HashSet<>();

        CoordinateAxis heigthCoordAxis = productMetadata.getCoordinateAxes().get(AxisType.Height);

        for (String currentVariableName : varSubset) {
            ParameterMetaData currentVariable = productMetadata.findVariable(currentVariableName);
            List<Dimension> currentVarDimensionList = currentVariable.getDimensions();
            for (Dimension currentDimension : currentVarDimensionList) {
                if (currentDimension.getName().equals(heigthCoordAxis.getName())) {
                    depthVariable.add(currentVariableName);
                }
            }
        }

        return depthVariable.size() != 0;
    }

    private int computeNumberOfDigit(int maxValue) {
        int numberOfDigit = 1;
        int currentMaxValue = maxValue;

        while (currentMaxValue / 10 >= 1) {
            numberOfDigit++;
            currentMaxValue = currentMaxValue / 10;
        }
        return numberOfDigit;
    }

    /**
     * REST unitary request to NCSS subset service and redirect output to file .
     * 
     * @throws MotuException
     */
    public void unitRequestNCSS(Set<String> variableToRequest) throws MotuException {
        long readingTimeInNanoSecStartEvent = System.nanoTime();

        // Geographical subset
        String north = String.valueOf(geoSubset.getUpperLeftLat());
        double westDbl = geoSubset.getUpperLeftLon();
        String west = String.valueOf(westDbl);
        double eastSuperiorToWestLong = geoSubset.getUpperRightLon();
        while (eastSuperiorToWestLong < westDbl) {
            eastSuperiorToWestLong += 360;
        }
        String east = String.valueOf(eastSuperiorToWestLong);
        String south = String.valueOf(geoSubset.getLowerLeftLat());

        // Temporal subset (W3C format supported by TDS)
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String time_start = df.format(timeSubset.getFrom());
        String time_end = df.format(timeSubset.getTo());

        try {
            // Setup query parameters
            MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

            // Variables subset
            for (String var : variableToRequest) {
                queryParams.add(NCSS_ATTR_VARIABLES, var);
            }

            // Geographical subset
            queryParams.add(NCSS_ATTR_NORTH, north);
            queryParams.add(NCSS_ATTR_WEST, west);
            queryParams.add(NCSS_ATTR_EAST, east);
            queryParams.add(NCSS_ATTR_SOUTH, south);

            // Temporal subset
            queryParams.add(NCSS_ATTR_TIME_START, time_start);
            queryParams.add(NCSS_ATTR_TIME_END, time_end);

            // Multiple request
            if (multipleRequest) {
                queryParams.add(NCSS_ATTR_VERTCOORD, Double.toString(depthSelected));
            } else {
                // Single request: Vertical subset (1-level) || Default case: ALL depth levels
                if (depthAxis != null) {
                    if (depthRange.length() == 1)
                        queryParams.add(NCSS_ATTR_VERTCOORD, Double.toString(NetCdfReader.unconvertDepth(depthSubset.getFromAsString())));
                }
            }

            // Output format
            queryParams.add(NCSS_ATTR_ACCEPT, outputFormat.name());

            // Prepare client
            Client client = Client.create();
            WebResource webResource = client.resource(ncssURL).queryParams(queryParams);

            // Read buffer response and detect response type
            ClientResponse response = webResource.get(ClientResponse.class);

            readingTimeInNanoSec += (System.nanoTime() - readingTimeInNanoSecStartEvent);
            if (response.getClientResponseStatus().getStatusCode() == 200) {
                if (response.getType().toString().contains("application/x-netcdf")) {
                    long writingTimeInNanoSecStartEvent = System.nanoTime();
                    // Output file and directory depending on concatenation
                    InputStream is = response.getEntity(InputStream.class);
                    String extractFolder = outputDir;
                    String extractFileName = outputFile;
                    if (multipleRequest) {
                        extractFolder = depthTempDir.toFile().getAbsolutePath();
                        extractFileName = depthTempFname;
                    }
                    File extractFolderFile = new File(extractFolder);
                    if (!extractFolderFile.exists()) {
                        boolean folderCreated = extractFolderFile.mkdirs();
                        if (folderCreated) {
                            LOGGER.info("Creation of the folder: " + extractFolder);
                        } else {
                            LOGGER.error("Error while creating folder: " + extractFolder);
                        }
                    }
                    FileOutputStream fos = new FileOutputStream(new File(extractFolderFile, extractFileName));

                    // Read/Write by chunks the REST response (avoid Heap over-usage)
                    int bytesRead = 0;
                    byte[] buff = new byte[BSIZE_READ];
                    while ((bytesRead = is.read(buff)) != -1) {
                        fos.write(buff, 0, bytesRead);
                    }

                    // Close inputs/outputs
                    fos.close();
                    is.close();
                    writingTimeInNanoSec += (System.nanoTime() - writingTimeInNanoSecStartEvent);
                } else if (response.getType().toString().equals("text/plain")) {
                    // TDS error message handle (plain/text)
                    String msg = response.getEntity(String.class);
                    throw new MotuException(ErrorType.NETCDF_GENERATION, msg);
                } else {
                    // Other error handling
                    String msg = "Unkown response type -> " + response.getType().toString();
                    throw new MotuException(ErrorType.NETCDF_GENERATION, msg);
                }
            } else {
                throw new MotuException(ErrorType.NETCDF_GENERATION, response.getEntity(String.class));
            }
        } catch (MotuException e) {
            throw e;
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, e);
        }
    }

    public long getReadingTimeInNanoSec() {
        return readingTimeInNanoSec;
    }

    public void setReadingTimeInNanoSec(long readingTimeInNanoSec) {
        this.readingTimeInNanoSec = readingTimeInNanoSec;
    }

    public long getWritingTimeInNanoSec() {
        return writingTimeInNanoSec;
    }

    public void setWritingTimeInNanoSec(long writingTimeInNanoSec) {
        this.writingTimeInNanoSec = writingTimeInNanoSec;
    }

    public ProductMetaData getProductMetadata() {
        return productMetadata;
    }

    public void setProductMetadata(ProductMetaData productMetadata) {
        this.productMetadata = productMetadata;
    }
}
