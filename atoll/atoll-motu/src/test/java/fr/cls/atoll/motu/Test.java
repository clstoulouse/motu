/**
 * 
 */
package fr.cls.atoll.motu;

// import java.io.FileInputStream;
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
import java.util.Collection;
import java.util.LinkedList;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionTree;
import com.graphbuilder.math.FuncMap;
import com.graphbuilder.math.VarMap;

/**
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:01:43 $
 * 
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
        
        getHostName("62.161.32.221, 10.1.253.25");
        //testDiskSpace();
        //testFileInUse();
    }
    public static String getHostName(String ip) {

        StringBuffer stringBuffer = new StringBuffer();
        try {
            //if there are several ip, they can be seperate by ','.
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

    public static void testExpression() {
        String mathFormula = "(Var1 + 123.6) > 125";
        Expression x = ExpressionTree.parse(mathFormula);

        FuncMap fm = new FuncMap();
        fm.loadDefaultFunctions();

        // Récupération des variables de la formule mathématiques
        String[] varNames = x.getVariableNames();

        // Instanciation d'une Map des variables de la formule, avec une case
        // insensitive
        VarMap vm = new VarMap(false);
        vm.getClass();

        // Parcours de la liste des variables de la formule
        for (int i = 0; i < varNames.length; i++) {
            System.out.println(varNames[i]);

            // Sinon, on met à jour la map des variables avec la valeur du
            // facteur
            // vm.setValue(varNames[i], value);
        }

        // return new Float(x.eval(vm, fm));

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
        
        
        
//        f.getTotalSpace() = byte
//        f.getTotalSpace()/(1024) = kb
//        f.getTotalSpace()/(1024*1024) = mb
//        f.getTotalSpace()/(1024*1024*1024) = gb        
        File directoryToScan = new File("c:\\temp\\");
        double scale = (1024*1024);
        System.out.println(directoryToScan.getFreeSpace()/scale);
        System.out.println(directoryToScan.getTotalSpace()/scale);
        System.out.println(directoryToScan.getUsableSpace()/scale);
        // directoryToScan.getUsableSpace();
        
        try {
            System.out.println(String.format("length: %f", recurse(directoryToScan)/scale));
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
    public static long recurse(File f) throws IOException
    {
        long bytes = 0;
        File files[] = f.listFiles();
        for(int i =0 ;i<files.length;i++)
        {
            File file = files [i];
            if(file.isFile())
            {
//                FileInputStream fis = new FileInputStream(file);
//                byte[] by = new byte[fis.available()];
//                fis.read(by);                
//                bytes = bytes + by.length;
                bytes = bytes + file.length();
            }
            if(file.isDirectory())
            {
                bytes = bytes + recurse(file);
            }
        }
        return bytes;
    }

}

/**
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:01:43 $
 * 
 */
class Traverse {
    /**
     * 
     * @author $Author: dearith $
     * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:01:43 $
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
