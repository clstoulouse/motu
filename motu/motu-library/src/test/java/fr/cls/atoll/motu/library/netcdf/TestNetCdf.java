/**
 * 
 */
package fr.cls.atoll.motu.library.netcdf;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;

/**
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * 
 */
public class TestNetCdf {

    /**
     * @param args
     */
    public static void main(String[] args) {
        createArray();
    }

    public static void createArray() {
        // write the RH data one value at a time to an Array
        double[][][] rhData = {
                { { 1., 2., 3., 4. }, { 5., 6., 7., 8. }, { 9., 10., 11., 12. } },
                { { 21., 22., 23., 24. }, { 25., 26., 27., 28. }, { 29., 30., 31., 32. } } };

        // ArrayInt rhA = new ArrayInt.D3(2, 3, 4);
        // Index ima = rhA.getIndex();
        // // write
        // for (int i=0; i<2; i++)
        // for (int j=0; j<3; j++)
        // for (int k=0; k<4; k++)
        // rhA.setInt(ima.set(i,j,k), rhData[i][j][k]);

        Array rhA = Array.factory(rhData);
        // Array rhB = Array.factory(rhA.getElementType(), rhA.getShape());
        Array rhB = rhA.copy();

        Index ima = rhB.getIndex();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 4; k++) {
                    System.out.println(rhB.getDouble(ima.set(i, j, k)));
                }
            }
        }

        double[][][] rhData2 = { { { 210., 220., 230., 240. }, { 250., 260., 270., 280. }, { 290., 300., 310., 320. } } };
        double[][][] rhData3 = { { { 210., 220., 230., 240. }, { 250., 260., 270., 280. }, { 290., 300., 310., 320. } } };
        // Array rhC = MAMath.add(rhA, rhB);
        Array rhC = Array.factory(rhData2);
        int[] newShape = new int[rhB.getRank()];
        for (int i = 0; i < rhB.getRank(); i++) {
            newShape[i] = rhB.getShape()[i] + rhC.getShape()[i];
        }

        Array rhD = Array.factory(rhB.getElementType(), newShape);
        IndexIterator indexItB = rhB.getIndexIterator();
        IndexIterator indexItD = rhD.getIndexIterator();

        while (indexItB.hasNext()) {
            indexItD.setDoubleNext(indexItB.getDoubleNext());
        }

        IndexIterator indexItC = rhC.getIndexIterator();
        while (indexItC.hasNext()) {
            indexItD.setDoubleNext(indexItC.getDoubleNext());
        }

        System.out.println("---------------- rhB");

        indexItD = rhD.getIndexIterator();
        while (indexItD.hasNext()) {
            System.out.println(indexItD.getDoubleNext());
        }

    }

}
