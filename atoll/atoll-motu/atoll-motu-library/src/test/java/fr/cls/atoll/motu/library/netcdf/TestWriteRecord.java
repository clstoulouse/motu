/**
 * 
 */
package fr.cls.atoll.motu.library.netcdf;

import java.io.IOException;

import junit.framework.TestCase;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * Simple example to create a new netCDF file corresponding to the following CDL:
 * 
 * <pre>
 *   netcdf example {
 *   dimensions:
 *       lat = 3 ;
 *       lon = 4 ;
 *       time = UNLIMITED ;
 *   variables:
 *       int rh(time, lat, lon) ;
 *               rh:long_name=&quot;relative humidity&quot; ;
 *           rh:units = &quot;percent&quot; ;
 *       double T(time, lat, lon) ;
 *               T:long_name=&quot;surface temperature&quot; ;
 *           T:units = &quot;degC&quot; ;
 *       float lat(lat) ;
 *           lat:units = &quot;degrees_north&quot; ;
 *       float lon(lon) ;
 *           lon:units = &quot;degrees_east&quot; ;
 *       int time(time) ;
 *           time:units = &quot;hours&quot; ;
 *   // global attributes:
 *           :title = &quot;Example Data&quot; ;
 *   data:
 *    rh =
 *      1, 2, 3, 4,
 *      5, 6, 7, 8,
 *      9, 10, 11, 12,
 *      21, 22, 23, 24,
 *      25, 26, 27, 28,
 *      29, 30, 31, 32 ;
 *    T =
 *      1, 2, 3, 4,
 *      2, 4, 6, 8,
 *      3, 6, 9, 12,
 *      2.5, 5, 7.5, 10,
 *      5, 10, 15, 20,
 *      7.5, 15, 22.5, 30 ;
 *    lat = 41, 40, 39 ;
 *    lon = -109, -107, -105, -103 ;
 *    time = 6, 18 ;
 *   }
 * </pre>
 * 
 * @author : Russ Rew
 * @author : John Caron
 */
public class TestWriteRecord extends TestCase {

    static String fileName = "testWriteRecord.nc"; // default name of file created
    static boolean dumpAfterCreate = false;

    public TestWriteRecord(String name) {
        super(name);
    }

    public void testNC3WriteWithRecordVariables() throws IOException {
        NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(fileName, false);

        // define dimensions, including unlimited
        Dimension latDim = ncfile.addDimension("lat", 3);
        Dimension lonDim = ncfile.addDimension("lon", 4);
        Dimension timeDim = ncfile.addDimension("time", -1, true, true, false);

        // define Variables
        Dimension[] dim3 = new Dimension[3];
        dim3[0] = timeDim;
        dim3[1] = latDim;
        dim3[2] = lonDim;

        // int rh(time, lat, lon) ;
        // rh:long_name="relative humidity" ;
        // rh:units = "percent" ;
        ncfile.addVariable("rh", DataType.INT, dim3);
        ncfile.addVariableAttribute("rh", "long_name", "relative humidity");
        ncfile.addVariableAttribute("rh", "units", "percent");

        // test attribute array
        ArrayInt.D1 valid_range = new ArrayInt.D1(2);
        valid_range.set(0, 0);
        valid_range.set(1, 100);
        ncfile.addVariableAttribute("rh", "range", valid_range);

        ncfile.addVariableAttribute("rh", "valid_range", Array.factory(new double[] { 0d, 100d }));

        // double T(time, lat, lon) ;
        // T:long_name="surface temperature" ;
        // T:units = "degC" ;
        ncfile.addVariable("T", DataType.DOUBLE, dim3);
        ncfile.addVariableAttribute("T", "long_name", "surface temperature");
        ncfile.addVariableAttribute("T", "units", "degC");

        // float lat(lat) ;
        // lat:units = "degrees_north" ;
        ncfile.addVariable("lat", DataType.FLOAT, new Dimension[] { latDim });
        ncfile.addVariableAttribute("lat", "units", "degrees_north");

        // float lon(lon) ;
        // lon:units = "degrees_east" ;
        ncfile.addVariable("lon", DataType.FLOAT, new Dimension[] { lonDim });
        ncfile.addVariableAttribute("lon", "units", "degrees_east");

        // int time(time) ;
        // time:units = "hours" ;
        ncfile.addVariable("time", DataType.INT, new Dimension[] { timeDim });
        ncfile.addVariableAttribute("time", "units", "hours");

        // :title = "Example Data" ;
        ncfile.addGlobalAttribute("title", "Example Data");

        // create the file
        try {
            ncfile.create();
        } catch (IOException e) {
            System.err.println("ERROR creating file");
            assert (false);
        }
        if (dumpAfterCreate)
            System.out.println("ncfile = " + ncfile);

        Variable v = ncfile.findTopVariable("rh");
        assert v != null;
        assert v.isUnlimited();

        // write the RH data one value at a time to an Array
        int[][][] rhData = { { { 1, 2, 3, 4 }, { 5, 6, 7, 8 }, { 9, 10, 11, 12 } }, { { 21, 22, 23, 24 }, { 25, 26, 27, 28 }, { 29, 30, 31, 32 } } };

        ArrayInt rhA = new ArrayInt.D3(2, latDim.getLength(), lonDim.getLength());
        Index ima = rhA.getIndex();
        // write
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < latDim.getLength(); j++)
                for (int k = 0; k < lonDim.getLength(); k++)
                    rhA.setInt(ima.set(i, j, k), rhData[i][j][k]);

        // write rhData out to disk
        try {
            ncfile.write("rh", rhA);
        } catch (IOException e) {
            System.err.println("ERROR writing file");
            assert (false);
        } catch (InvalidRangeException e) {
            e.printStackTrace();
            assert (false);
        }

        // Here's an Array approach to set the values of T all at once.
        double[][][] tData = {
                { { 1., 2, 3, 4 }, { 2., 4, 6, 8 }, { 3., 6, 9, 12 } }, { { 2.5, 5, 7.5, 10 }, { 5., 10, 15, 20 }, { 7.5, 15, 22.5, 30 } } };
        try {
            ncfile.write("T", Array.factory(tData));
        } catch (IOException e) {
            System.err.println("ERROR writing file");
            assert (false);
        } catch (InvalidRangeException e) {
            e.printStackTrace();
            assert (false);
        }

        // Store the rest of variable values
        try {
            ncfile.write("lat", Array.factory(new float[] { 41, 40, 39 }));
            ncfile.write("lon", Array.factory(new float[] { -109, -107, -105, -103 }));
            ncfile.write("time", Array.factory(new int[] { 6, 18 }));
        } catch (IOException e) {
            System.err.println("ERROR writing file");
            assert (false);
        } catch (InvalidRangeException e) {
            e.printStackTrace();
            assert (false);
        }

        // test reading without closing and reopening
        try {
            /* Get the value of the global attribute named "title" */
            String title = ncfile.findAttValueIgnoreCase(null, "title", "N/A");

            /*
             * Read the latitudes into an array of double. This works regardless of the external type of the
             * "lat" variable.
             */
            Variable lat = ncfile.findVariable("lat");
            assert (lat.getRank() == 1); // make sure it's 1-dimensional
            int nlats = lat.getShape()[0]; // number of latitudes
            double[] lats = new double[nlats]; // where to put them

            Array values = lat.read(); // read all into memory
            ima = values.getIndex(); // index array to specify which value
            for (int ilat = 0; ilat < nlats; ilat++) {
                lats[ilat] = values.getDouble(ima.set0(ilat));
            }
            /* Read units attribute of lat variable */
            String latUnits = ncfile.findAttValueIgnoreCase(lat, "units", "N/A");
            assert (latUnits.equals("degrees_north"));

            /* Read the longitudes. */
            Variable lon = ncfile.findVariable("lon");
            values = lon.read();
            assert (values instanceof ArrayFloat.D1);
            ArrayFloat.D1 fa = (ArrayFloat.D1) values;

            /*
             * Now we can just use the MultiArray to access values, or we can copy the MultiArray elements to
             * another array with toArray(), or we can get access to the MultiArray storage without copying.
             * Each of these approaches to accessing the data are illustrated below.
             */

            /* Whats the time dimensin length ? */
            Dimension td = ncfile.findDimension("time");
            assert td.getLength() == 2;

            /* Read the times: unlimited dimension */
            Variable time = ncfile.findVariable("time");
            Array timeValues = time.read();
            assert (timeValues instanceof ArrayInt.D1);
            ArrayInt.D1 ta = (ArrayInt.D1) timeValues;
            assert (ta.get(0) == 6) : ta.get(0);
            assert (ta.get(1) == 18) : ta.get(1);

            /* Read the relative humidity data */
            Variable rh = ncfile.findVariable("rh");
            Array rhValues = rh.read();
            assert (rhValues instanceof ArrayInt.D3);
            ArrayInt.D3 rha = (ArrayInt.D3) rhValues;
            int[] shape = rha.getShape();
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    for (int k = 0; k < shape[2]; k++) {
                        int want = 20 * i + 4 * j + k + 1;
                        int val = rha.get(i, j, k);
                        // System.out.println(" "+i+" "+j+" "+k+" "+want+" "+val);
                        assert (want == val) : val;
                    }
                }
            }

            /* Read the temperature data */
            Variable t = ncfile.findVariable("T");
            Array tValues = t.read();
            assert (tValues instanceof ArrayDouble.D3);
            ArrayDouble.D3 Ta = (ArrayDouble.D3) tValues;

            /* Read subset of the temperature data */
            tValues = t.read(new int[3], new int[] { 2, 2, 2 });
            assert (tValues instanceof ArrayDouble.D3);
            Ta = (ArrayDouble.D3) tValues;

        } catch (InvalidRangeException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        // all done
        try {
            ncfile.flush();
            ncfile.close();
        } catch (IOException e) {
            System.err.println("ERROR writing file");
            assert (false);
        }

        System.out.println("**** TestWriteRecord done");

    }

    // make an example writing records
    public void testNC3WriteWithRecord() throws IOException {
        NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew("C:/temp/writeRecordExample.nc", false);

        // define dimensions, including unlimited
        Dimension latDim = ncfile.addDimension("lat", 64);
        Dimension lonDim = ncfile.addDimension("lon", 128);
        Dimension timeDim = ncfile.addDimension("time", -1, true, true, false);

        // define Variables
        Dimension[] dim3 = new Dimension[3];
        dim3[0] = timeDim;
        dim3[1] = latDim;
        dim3[2] = lonDim;

        // double T(time, lat, lon) ;
        // T:long_name="surface temperature" ;
        // T:units = "degC" ;
        ncfile.addVariable("T", DataType.DOUBLE, dim3);
        ncfile.addVariableAttribute("T", "long_name", "surface temperature");
        ncfile.addVariableAttribute("T", "units", "degC");

        // float lat(lat) ;
        // lat:units = "degrees_north" ;
        ncfile.addVariable("lat", DataType.FLOAT, new Dimension[] { latDim });
        ncfile.addVariableAttribute("lat", "units", "degrees_north");

        // float lon(lon) ;
        // lon:units = "degrees_east" ;
        ncfile.addVariable("lon", DataType.FLOAT, new Dimension[] { lonDim });
        ncfile.addVariableAttribute("lon", "units", "degrees_east");

        // int time(time) ;
        // time:units = "hours" ;
        ncfile.addVariable("time", DataType.INT, new Dimension[] { timeDim });
        ncfile.addVariableAttribute("time", "units", "hours");

        // :title = "Example Data" ;
        ncfile.addGlobalAttribute("title", "Example Data");

        // create the file
        try {
            ncfile.create();
        } catch (IOException e) {
            System.err.println("ERROR creating file");
            e.printStackTrace();
            return;
        }
        System.out.println("ncfile = " + ncfile);

        // now write one record at a time
        Variable v = ncfile.findTopVariable("T");
        ArrayDouble data = new ArrayDouble.D3(1, latDim.getLength(), lonDim.getLength());
        ArrayInt timeData = new ArrayInt.D1(1);
        int[] origin = new int[v.getRank()];
        int[] timeOrigin = new int[1];

        for (int time = 0; time < 100; time++) {
            // fill the data array
            Index ima = data.getIndex();
            for (int j = 0; j < latDim.getLength(); j++)
                for (int k = 0; k < lonDim.getLength(); k++)
                    data.setDouble(ima.set(0, j, k), (double) time * j * k);
            timeData.setInt(timeData.getIndex(), time);

            // write to file
            origin[0] = time;
            timeOrigin[0] = time;
            try {
                ncfile.write("T", origin, data);
                ncfile.write("time", timeOrigin, timeData);
            } catch (IOException e) {
                System.err.println("ERROR writing file");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }
        }

        // all done
        try {
            ncfile.close();
        } catch (IOException e) {
            System.err.println("ERROR writing file");
        }

        System.out.println("**** TestWriteRecord done");
    }

}