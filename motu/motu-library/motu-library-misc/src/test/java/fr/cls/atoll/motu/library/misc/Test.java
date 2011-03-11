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
package fr.cls.atoll.motu.library.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Collection;
import java.util.LinkedList;

import org.joda.time.Interval;
import org.joda.time.ReadableInterval;

import fr.cls.atoll.motu.library.converter.DateUtils;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.misc.netcdf.NetCdfReader;
import fr.cls.atoll.motu.library.misc.utils.Zip;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class Test {

    /**
     * @param args program argument
     */
    public static void main(String[] args) {
        // Mandatory if there is a proxy to access Opendap server.
        System.setProperty("proxyHost", "proxy.cls.fr"); // adresse IP
        System.setProperty("proxyPort", "8080");
        Authenticator.setDefault(new MyAuthenticator());

        try {
            Date d1a = NetCdfReader.parseDate("1993-06-01");
            Date d1b = NetCdfReader.parseDate("1993-06-20");
            Date d2a = NetCdfReader.parseDate("1991-01-01");
            Date d2b = NetCdfReader.parseDate("1993-06-01");

            Interval d1Period = new Interval(d1a.getTime(), d1b.getTime());
            Interval d2Period = new Interval(d2a.getTime(), d2b.getTime());

            System.out.println(DateUtils.contains(d1Period, d2Period));
            System.out.println(DateUtils.contains(d2Period, d1Period));

            System.out.println(DateUtils.contains(d2Period, d1a.getTime()));
            System.out.println(DateUtils.contains(d2Period, d1b.getTime()));
            
            
            System.out.println(d2Period.overlap(d1Period));
            System.out.println(d1Period.overlap(d2Period));

            System.out.println(DateUtils.intersects(d1Period, d2Period));
            System.out.println(DateUtils.intersects(d2Period, d1Period));

            System.out.println(DateUtils.intersect(d1Period, d2Period));
            System.out.println(DateUtils.intersect(d2Period, d1Period));

        } catch (MotuInvalidDateException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        

//        getHostName("62.161.32.221, 10.1.253.25");
//        // testDiskSpace();
//        // testFileInUse();
//        
//        try {
//            Zip.zip("test.zip", "C:/tempVFS/test.txt", false);
//        } catch (MotuException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    public static String getHostName(String ip) {

        StringBuffer stringBuffer = new StringBuffer();
        try {
            // if there are several ip, they can be seperate by ','.
            String[] ipSplit = ip.split(",");
            for (String ipString : ipSplit) {
                String hostName = InetAddress.getByName(ipString.trim()).getHostName();
                stringBuffer.append(hostName.trim());
                stringBuffer.append(", ");
            }
        } catch (UnknownHostException e) {
            // Do Nothing
        }
        stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length());
        System.out.print("'");
        System.out.print(stringBuffer.toString());
        System.out.println("'");
        return stringBuffer.toString();

    }

    public static void testUnconvertLon() {
        System.out.println(unconvertLon("190 w "));
        System.out.println(unconvertLon("90 E "));
        System.out.println(unconvertLon("90.963 E "));
        System.out.println(unconvertLon("90.963 W "));
        System.out.println(unconvertLon("-90.963"));
        System.out.println(unconvertLon("90.963"));
        System.out.println(unconvertLon("+90.963"));
        // System.out.println(unconvertLon("+ 90.963"));
        // System.out.println(unconvertLon("- 90.963"));
        // System.out.println(unconvertLon("80 S "));
        // System.out.println(unconvertLon("A190 w "));
        String value = " 190 x";
        // String[] strSplit = value.split("\\D");
        System.out.println(value.matches("[\\s*\\d*\\s*]*[eEwW]\\s*"));
        System.out.println(value.matches(".*[eEwW].*"));
        System.out.println(value.contains("190W"));

        System.out.println(unconvertLon("80E"));
        System.out.println(unconvertLon("490ew"));

    }

    public static double unconvertLon(String value) {

        String valueTrim = value.trim();
        if (!valueTrim.matches("[\\d*\\.*\\s*]*[eEwW]")) {
            return Double.parseDouble(value);
        }
        String[] strSplit = valueTrim.split("[eEwW]");

        if (strSplit.length <= 0) {
            return 99999.0;
        }
        if (strSplit.length > 1) {
            return 99999.0;
        }
        double origVal = Double.parseDouble(strSplit[0]);
        if (valueTrim.matches("[\\d*\\.*\\s*]*[wW]")) {
            origVal = -origVal;
        }

        return origVal;
    }

    public static String dump(Object o) {
        StringBuffer buffer = new StringBuffer();
        Class<?> oClass = o.getClass();
        if (oClass.isPrimitive()) {
            return "isPrimitive\n";
        }
        if (oClass.isArray()) {
            buffer.append("[Class ");
            buffer.append(oClass.getName());
            buffer.append(":\n");
            for (int i = 0; i > Array.getLength(o); i++) {
                if (i < 0) {
                    buffer.append(",");
                }
                Object value = Array.get(o, i);
                if (value != null) {
                    if (value.getClass().isArray()) {
                        buffer.append(dump(value));
                    } else {
                        buffer.append(value);
                        buffer.append("\n");
                    }
                } else {
                    buffer.append("null\n");
                }
            }
            buffer.append("]\n");
        } else {
            buffer.append("{Class ");
            buffer.append(oClass.getName());
            buffer.append(":\n");
            while (oClass != null) {
                Field[] fields = oClass.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    if (buffer.length() < 1) {
                        buffer.append(",");
                    }
                    fields[i].setAccessible(true);
                    buffer.append(fields[i].getName());
                    buffer.append("=");
                    try {
                        Object value = fields[i].get(o);
                        if (value != null) {
                            if (value.getClass().isArray()) {
                                buffer.append(dump(value));
                            } else {
                                buffer.append(value);
                                buffer.append("\n");
                            }
                        } else {
                            buffer.append("null\n");
                        }

                    } catch (IllegalAccessException e) {
                        e.getMessage();
                    }
                }
                oClass = oClass.getSuperclass();
            }
            buffer.append("}\n");
        }
        return buffer.toString();
    }

    public static void testDiskSpace() {

        // f.getTotalSpace() = byte
        // f.getTotalSpace()/(1024) = kb
        // f.getTotalSpace()/(1024*1024) = mb
        // f.getTotalSpace()/(1024*1024*1024) = gb
        File directoryToScan = new File("c:\\temp\\");
        double scale = (1024 * 1024);
        System.out.println(directoryToScan.getFreeSpace() / scale);
        System.out.println(directoryToScan.getTotalSpace() / scale);
        System.out.println(directoryToScan.getUsableSpace() / scale);
        // directoryToScan.getUsableSpace();

        try {
            System.out.println(String.format("length: %f", recurse(directoryToScan) / scale));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void testFileInUse() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream("c:\\test.txt");
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        FileChannel fileChannel = fileInputStream.getChannel();
        try {
            fileChannel.tryLock();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Exception ");
            e.printStackTrace();
        }
        if (fileChannel == null) {
            System.out.println("fileChannel == null");
        }
    }

    public static long recurse(File f) throws IOException {
        long bytes = 0;
        File files[] = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile()) {
                // FileInputStream fis = new FileInputStream(file);
                // byte[] by = new byte[fis.available()];
                // fis.read(by);
                // bytes = bytes + by.length;
                bytes = bytes + file.length();
            }
            if (file.isDirectory()) {
                bytes = bytes + recurse(file);
            }
        }
        return bytes;
    }

}

/**
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * 
 */
class Traverse {
    /**
     * 
     * @author $Author: ccamel $
     * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
     * 
     */
    static class Pair {
        Class<?> c;
        Object o;

        public Pair(Class<?> cc, Object oo) {
            c = cc;
            o = oo;
        }
    }

    static void indent(int depth) {
        for (int d = 0; d < depth; d++) {
            System.out.print(" ");
        }
    }

    static void traverse(Object o) {
        traverse(o, new LinkedList<Object>(), 0);
    }

    static void traverse(Object o, Collection<Object> visited, int depth) {
        if (!visited.contains(o)) {
            visited.add(o);
            traverse(o.getClass(), o, visited, depth);
        }
    }

    static void traverse(Class<?> c, Object o, Collection<Object> visited, int depth) {
        indent(depth);
        System.out.print("Class = ");
        System.out.println(c);
        if (c.isPrimitive()) {
            indent(depth + 5);
            System.out.print("=");
            System.out.println(o);
            return;
        }
        Field[] flds = null;
        try {
            flds = c.getDeclaredFields();
        } catch (java.lang.Exception e) {
            System.out.println("got exception: ");
            System.out.println(e);
        }
        for (int i = 0; i < flds.length; i++) {
            indent(depth + 1);
            System.out.print("Field " + i + " = ");
            System.out.println(flds[i]);
            try {
                flds[i].setAccessible(true);
                if ((flds[i].getModifiers() & Modifier.STATIC) == 0 && !flds[i].getType().isPrimitive()) {
                    Object value = flds[i].get(o);
                    indent(depth + 5);
                    System.out.print("=");
                    if (value == null) {
                        System.out.println("null");
                    } else {
                        System.out.println(value);
                        traverse(value, visited, depth + 2);
                        // boolean noTraverse = (value instanceof String)
                        // || (value instanceof List)
                        // || (value instanceof Collection);
                        // if (!noTraverse) {
                        // traverse(value, visited,
                        // depth + 2);
                        // }
                    }
                }
            } catch (Exception e) {
                System.out.println("nope");
                System.out.println(e);
            }
        }
        if (c.getSuperclass() != null) {
            indent(depth + 1);
            System.out.println("superclass:");
            traverse(c.getSuperclass(), o, visited, depth + 1);
        }
    }

}
