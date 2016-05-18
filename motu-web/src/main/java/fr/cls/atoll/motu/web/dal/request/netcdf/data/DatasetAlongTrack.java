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
package fr.cls.atoll.motu.web.dal.request.netcdf.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.common.format.OutputFormat;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * "Along track" dataset class.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class DatasetAlongTrack extends fr.cls.atoll.motu.web.dal.request.netcdf.data.DatasetBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Default constructor.
     */
    public DatasetAlongTrack() {

    }

    /**
     * Constructor.
     * 
     * @param product product linked to the dataset
     */

    public DatasetAlongTrack(Product product) {
        super(product);
    }

    /**
     * Compute amount data size.
     * 
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    @Override
    public void computeAmountDataSize()
            throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
            MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException {

        throw new MotuNotImplementedException("compute amount data from 'Along the track' dataset is not implemented");
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
     */
    @Override
    public void extractData(OutputFormat dataOutputFormat)
            throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
            MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractData() - entering");
        }

        throw new MotuNotImplementedException("data extraction from 'Along the track' dataset is not implemented");
        /*
         * if (product == null) { throw new MotuException("Error in DatasetAlongTrack - extractData product
         * have not nbeen set (= null)"); }
         * 
         * switch (dataOutputFormat) { case NETCDF: extractDataIntoNetCdf(); break;
         * 
         * default: throw new MotuNotImplementedException(String.format("extraction into %s is not
         * implemented", dataOutputFormat.toString())); // break; }
         * TLog.logger().exiting(this.getClass().getName(), "extractData");
         */
    }

    /**
     * Extract data into a NetCdf format.
     * 
     * @throws MotuException
     * @throws MotuExceedingCapacityException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidDateRangeException
     * @throws MotuInvalidDepthRangeException
     * @throws NetCdfVariableException
     * @throws MotuInvalidLatLonRangeException
     * @throws MotuNoVarException
     */
    /*
     * public void extractDataIntoNetCdf() throws MotuException, MotuInvalidDateRangeException,
     * MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidDepthRangeException,
     * MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException {
     * TLog.logger().entering(this.getClass().getName(), "extractDataIntoNetCdf");
     * 
     * if (product == null) { throw new MotuException("Error in DatasetAlongTrack - extractDataIntoNetCdf
     * product have not nbeen set (= null)"); } if (variablesSize() <= 0) { throw new MotuNoVarException(
     * "Variable list is empty"); } product.openNetCdfReader(); // gets ranges to be extracted double[]
     * tRangeValue = new double[2]; Range tRange = getTimeRange(tRangeValue);
     * 
     * double[] yRangeValue = new double[2]; double[] xRangeValue = new double[2]; Range[] yxRange =
     * getYXRange(yRangeValue, xRangeValue);
     * 
     * double[] zRangeValue = new double[2]; Range zRange = getZRange(zRangeValue);
     * 
     * GridDataset gds = new GridDataset(product.getNetCdfReaderDataset()); List<Attribute>
     * globalFixedAttributes = initializeNetCdfFixedGlobalAttributes(); List<Attribute> globalDynAttributes =
     * initializeNetCdfDynGlobalAttributes(tRangeValue, zRangeValue, yRangeValue, xRangeValue);
     * 
     * product.setExtractFilename(NetCdfWriter.getUniqueNetCdfFileName(product.getProductId()));
     * 
     * NetCdfWriter netCdfWriter = new NetCdfWriter(product.getExtractLocationData(), true);
     * 
     * netCdfWriter.writeGlobalAttributes(globalFixedAttributes);
     * netCdfWriter.writeGlobalAttributes(globalDynAttributes);
     * 
     * for (VarData varData : variablesValues()) {
     * 
     * GeoGrid geoGrid = gds.findGridByName(varData.getName()); if (geoGrid == null) { throw new
     * MotuNotImplementedException(String .format("Variable %s in not geo-referenced - Non-georeferenced data
     * is not implemented (method: DatasetGrid.extractData)", varData.getName())); } //GridDatatype
     * geoGridSubset = null; GeoGrid geoGridSubset = null; try { geoGridSubset = geoGrid.subset(tRange,
     * zRange, yxRange[0], yxRange[1]); //geoGridSubset = geoGrid.makeSubset(null, null, tRange, zRange,
     * yxRange[0], yxRange[1]); } catch (InvalidRangeException e) { throw new MotuException("Error in
     * subsetting geo grid", (Throwable) e); } netCdfWriter.writeVariables(geoGridSubset); }
     * 
     * netCdfWriter.finish(VAR_ATTR_TO_REMOVE);
     * 
     * TLog.logger().exiting(this.getClass().getName(), "extractDataIntoNetCdf"); }
     */
}
// CSON: MultipleStringLiterals
