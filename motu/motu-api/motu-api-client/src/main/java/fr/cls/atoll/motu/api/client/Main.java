package fr.cls.atoll.motu.api.client;

import fr.cls.atoll.motu.library.misc.intfce.Organizer;

/**
 * <br><br>Copyright : Copyright (c) 2010.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: $
 * @version $Revision: $ - $Date: $
 */
public class Main {

    /**
     * .
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        System.out.println("fr.cls.atoll.motu.api.client OK");
        System.out.println(System.getProperties());
        System.out.println("fr.cls.atoll.motu.api.client OK");
        
        String strClassPath = System.getProperty("java.class.path");       
        System.out.println("Classpath is " + strClassPath);
        System.out.println(Organizer.class.getName());
    }

}
