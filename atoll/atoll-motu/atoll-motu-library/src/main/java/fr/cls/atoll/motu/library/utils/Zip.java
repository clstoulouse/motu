package fr.cls.atoll.motu.library.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import fr.cls.atoll.motu.library.exception.MotuException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-05-04 14:48:23 $
 */
public class Zip {

    /**
     * The Constructor.
     */
    protected Zip() {

    }

    /** The Constant BUFFER. */
    static final int BUFFER = 2048;

    /**
     * Unaccent.
     * 
     * @param s the s
     * 
     * @return the string
     */
    public static String unAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFKD);
        return temp.replaceAll("[^\\p{ASCII}]", "");
    }

    /**
     * The main method.
     * 
     * @param argv the arguments
     */
    public static void main(String[] argv) {
        String zipFileName = "c:/temp/test.zip";
        List<String> files = new ArrayList<String>();
        files.add("C:/BratWks/thelasttest/Operations/CreateOperations_1.cmd");
        files.add("C:/BratWks/TestPreselection/Operations/CreateEnvisat.nc");
        try {
            // File xx = new File(zipFileName);
            // System.out.println(xx.getName());
            // System.out.println(xx.toURI());
            // System.out.println(xx.getCanonicalPath());
            // System.out.println(xx.getCanonicalFile());
            // Zip.zip(zipFileName, files.toArray(new String[files.size()]));
            List<String> listFiles = Zip.getEntries(zipFileName);
            System.out.println(listFiles.toString());
            Zip.unzip(zipFileName, "c:\\temp\\testtt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Zip.
     * 
     * @param files the files
     * @param zipFileName the zip file name
     * 
     * @throws MotuException the motu exception
     */
    public static void zip(String zipFileName, String[] files) throws MotuException {
        try {
            // création d'un flux d'écriture sur fichier
            FileOutputStream dest = new FileOutputStream(zipFileName);

            // calcul du checksum : Adler32 (plus rapide) ou CRC32
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());

            // création d'un buffer d'écriture
            BufferedOutputStream buff = new BufferedOutputStream(checksum);

            // création d'un flux d'écriture Zip
            ZipOutputStream out = new ZipOutputStream(buff);

            // spécification de la méthode de compression
            out.setMethod(ZipOutputStream.DEFLATED);

            // spécifier la qualité de la compression 0..9
            out.setLevel(Deflater.BEST_COMPRESSION);

            // buffer temporaire des données à écriture dans le flux de sortie
            byte[] data = new byte[BUFFER];

            // pour chacun des fichiers de la liste
            for (int i = 0; i < files.length; i++) {

                File ftemp = new File(files[i]);
                if (ftemp.isDirectory()) {
                    continue;
                }
                // en afficher le nom
                // System.out.println("Adding: " + files[i]);

                // création d'un flux de lecture
                FileInputStream fi = new FileInputStream(files[i]);

                // création d'un tampon de lecture sur ce flux
                BufferedInputStream buffi = new BufferedInputStream(fi, BUFFER);

                // création d'une entrée Zip pour ce fichier
                ZipEntry entry = new ZipEntry(Zip.unAccent(files[i]));

                // ajout de cette entrée dans le flux d'écriture de l'archive Zip
                out.putNextEntry(entry);

                // écriture du fichier par paquet de BUFFER octets
                // dans le flux d'écriture
                int count;
                while ((count = buffi.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }

                // Close the current entry
                out.closeEntry();

                // fermeture du flux de lecture
                buffi.close();
            }

            // fermeture du flux d'écriture
            out.close();
            buff.close();
            checksum.close();
            dest.close();

            // System.out.println("checksum: " + checksum.getChecksum().getValue());

            // traitement de toute exception
        } catch (Exception e) {
            throw new MotuException("Error in Zip#zip", e);
        }

    }

    /**
     * Unzip.
     * 
     * @param zipFileName the zip file name
     * 
     * @throws MotuException the motu exception
     */
    public static void unzip(String zipFileName) throws MotuException {
        try {

            // fichier destination
            BufferedOutputStream dest = null;

            // ouverture fichier entrée
            FileInputStream fis = new FileInputStream(zipFileName);

            // ouverture fichier de buffer
            BufferedInputStream buffi = new BufferedInputStream(fis);

            // ouverture archive Zip d'entrée
            ZipInputStream zis = new ZipInputStream(buffi);

            // entrée Zip
            ZipEntry entry;

            // parcours des entrées de l'archive
            while ((entry = zis.getNextEntry()) != null) {

                if (entry.isDirectory()) {
                    // Assume directories are stored parents first then children.
                    // System.err.println("Extracting directory: " + entry.getName());
                    (new File(entry.getName())).mkdir();
                    continue;
                }

                // affichage du nom de l'entrée
                // System.out.println("Extracting: " +entry);

                int count;
                byte[] data = new byte[BUFFER];

                // création fichier
                FileOutputStream fos = new FileOutputStream(entry.getName());

                // affectation buffer de sortie
                dest = new BufferedOutputStream(fos, BUFFER);

                // écriture sur disque
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }

                // vidage du tampon
                dest.flush();

                // fermeture fichier
                dest.close();
            }

            // fermeture archive
            zis.close();

        } catch (Exception e) {
            throw new MotuException("Error in Zip#unzip", e);
        }

    }

    /**
     * Unzip.
     * 
     * @param zipFileName the zip file name
     * @param targetDirName the target dir name
     * 
     * @throws MotuException the motu exception
     */
    public static void unzip(String zipFileName, String targetDirName) throws MotuException {
        Zip.unzip(zipFileName, targetDirName, true);
    }

    /**
     * Unzip.
     * 
     * @param zipFileName the zip file name
     * @param targetDirName the target dir name
     * @param restoreOrignalFolders the restore orignal folders
     * 
     * @throws MotuException the motu exception
     */
    public static void unzip(String zipFileName, String targetDirName, boolean restoreOrignalFolders) throws MotuException {
        try {

            // fichier destination
            BufferedOutputStream dest = null;

            // ouverture fichier entrée
            FileInputStream fis = new FileInputStream(zipFileName);

            // ouverture fichier de buffer
            BufferedInputStream buffi = new BufferedInputStream(fis);

            // ouverture archive Zip d'entrée
            ZipInputStream zis = new ZipInputStream(buffi);

            // entrée Zip
            ZipEntry entry;

            // parcours des entrées de l'archive
            while ((entry = zis.getNextEntry()) != null) {
                String targetFilename = "";

                String entryName = entry.getName();
                if (!restoreOrignalFolders) {
                    File f = new File(entryName);
                    entryName = f.getName();
                }

                targetFilename = targetDirName + "/" + entryName;

                if (entryName.length() >= 2) {
                    if (entryName.charAt(1) == ':') {
                        if (entryName.length() > 2) {
                            targetFilename = targetDirName + "/" + entryName.substring(2);
                        } else {
                            targetFilename = targetDirName;
                        }
                    }
                }
                targetFilename = targetFilename.replace('\\', '/');
                if (entry.isDirectory()) {
                    new File(targetFilename).mkdirs();
                    continue;
                } else {
                    // create dir if needed
                    int lastSlash = targetFilename.lastIndexOf("/");
                    if (lastSlash > 0) {
                        String dirName = targetFilename.substring(0, lastSlash);
                        new File(dirName).mkdirs();
                    }
                }

                // affichage du nom de l'entrée
                // System.out.println("Extracting: " +entry);

                int count;
                byte[] data = new byte[BUFFER];

                // création fichier
                FileOutputStream fos = new FileOutputStream(targetFilename);

                // affectation buffer de sortie
                dest = new BufferedOutputStream(fos, BUFFER);

                // écriture sur disque
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }

                // vidage du tampon
                dest.flush();

                // fermeture fichier
                dest.close();

            }

            // fermeture archive
            zis.close();

        } catch (Exception e) {
            throw new MotuException("Error in Zip#unzip", e);
        }

    }

    /**
     * Gets the entries.
     * 
     * @param zipFileName the zip file name
     * 
     * @return the entries
     * 
     * @throws MotuException the motu exception
     */
    public static List<String> getEntries(String zipFileName) throws MotuException {

        List<String> files = new ArrayList<String>();
        try {

            // ouverture de l'archive
            ZipFile zipfile = new ZipFile(zipFileName);

            // extraction des entrées
            Enumeration<? extends ZipEntry> entries = zipfile.entries();

            // parcours des entrées
            while (entries.hasMoreElements()) {

                // extraction entrée courante
                ZipEntry e = ((ZipEntry) entries.nextElement());

                // extractions du nom
                files.add(e.getName());
                // files.add(e.toString());

            }

            // fermeture archive
            zipfile.close();

        } catch (Exception e) {
            throw new MotuException("Error in Zip#getEntries", e);
        }

        return files;

    }

}
