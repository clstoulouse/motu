package fr.cls.atoll.motu.web.usl.response.velocity.model.transaction;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class LogTransaction {

    private String fileName;
    private double sizeInMBytes;
    private String lastModifiedDate;
    private long lastModifiedTimeStamp;

    /**
     * Constructeur.
     * 
     * @param fileName
     * @param sizeInMBytes
     * @param lastModifiedDate
     */
    public LogTransaction(String fileName, double sizeInMBytes, long lastModifiedTimeStamp, String lastModifiedDate) {
        setFileName(fileName);
        setSizeInMBytes(sizeInMBytes);
        setLastModifiedDate(lastModifiedDate);
        setLastModifiedTimeStamp(lastModifiedTimeStamp);
    }

    /**
     * Valeur de lastModifiedTimeStamp.
     * 
     * @return la valeur.
     */
    public long getLastModifiedTimeStamp() {
        return lastModifiedTimeStamp;
    }

    /**
     * Valeur de lastModifiedTimeStamp.
     * 
     * @param lastModifiedTimeStamp nouvelle valeur.
     */
    public void setLastModifiedTimeStamp(long lastModifiedTimeStamp) {
        this.lastModifiedTimeStamp = lastModifiedTimeStamp;
    }

    /**
     * Valeur de fileName.
     * 
     * @return la valeur.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Valeur de fileName.
     * 
     * @param fileName nouvelle valeur.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Valeur de sizeInMBytes.
     * 
     * @return la valeur.
     */
    public double getSizeInMBytes() {
        return sizeInMBytes;
    }

    /**
     * Valeur de sizeInMBytes.
     * 
     * @param sizeInMBytes nouvelle valeur.
     */
    public void setSizeInMBytes(double sizeInMBytes) {
        this.sizeInMBytes = sizeInMBytes;
    }

    /**
     * Valeur de lastModifiedDate.
     * 
     * @return la valeur.
     */
    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * Valeur de lastModifiedDate.
     * 
     * @param lastModifiedDate nouvelle valeur.
     */
    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

}
