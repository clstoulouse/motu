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
package fr.cls.atoll.motu.library.misc.utils;

import fr.cls.atoll.motu.library.misc.exception.MotuException;

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

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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
            // Zip.zip(zipFileName, files.toArray(new String[files.size()]), false);
            // List<String> listFiles = Zip.getEntries(zipFileName);
            // System.out.println(listFiles.toString());
            // Zip.unzip(zipFileName, "c:\\temp\\testtt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Zip.
     * 
     * @param zipFileName the zip file name
     * @param file the file to zip
     * @param storeOrignalFolders the store orignal folders
     * 
     * @throws MotuException the motu exception
     */
    public static void zip(String zipFileName, String file, boolean storeOrignalFolders) throws MotuException {
        String[] files = new String[1];
        files[0] = file;
        Zip.zip(zipFileName, files, storeOrignalFolders);
    }

    /**
     * Zip.
     * 
     * @param zipFileName the zip file name
     * @param files the files to zip
     * @param storeOrignalFolders the store orignal folders
     * 
     * @throws MotuException the motu exception
     */
    public static void zip(String zipFileName, List<String> files, boolean storeOrignalFolders) throws MotuException {
        Zip.zip(zipFileName, files.toArray(new String[files.size()]), storeOrignalFolders);
    }

    /**
     * Zip.
     * 
     * @param files the files to sip
     * @param zipFileName the zip file name
     * @param storeOrignalFolders the store orignal folders
     * 
     * @throws MotuException the motu exception
     */
    public static void zip(String zipFileName, String[] files, boolean storeOrignalFolders) throws MotuException {
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

                String entryName = "";
                ZipEntry entry = null;

                if (storeOrignalFolders) {
                    entryName = files[i];
                } else {
                    File f = new File(files[i]);
                    entryName = f.getName();

                }

                // création d'une entrée Zip pour ce fichier
                entry = new ZipEntry(Zip.unAccent(entryName));

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
                ZipEntry e = (entries.nextElement());

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
