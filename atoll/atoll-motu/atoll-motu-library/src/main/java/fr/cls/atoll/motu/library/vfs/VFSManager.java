package fr.cls.atoll.motu.library.vfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.cache.SoftRefFilesCache;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.commons.vfs.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.log4j.Logger;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.commons.util.io.ConfigLoader;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.6 $ - $Date: 2009-06-08 14:44:01 $
 */
public class VFSManager {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(VFSManager.class);
    
    /** The Constant DEFAULT_SCHEME. */
    public static final String DEFAULT_SCHEME = "local";

    /**
     * Instantiates a new vFS manager.
     */
    public VFSManager() {
    }

    /** The standard file system manager. */
    protected StandardFileSystemManager standardFileSystemManager = null;

    /**
     * Gets the opts.
     * 
     * @return the opts
     */
    public FileSystemOptions getOpts() {
        return opts;
    }

    /**
     * Gets the standard file system manager.
     * 
     * @return the standard file system manager
     */
    public StandardFileSystemManager getStandardFileSystemManager() {
        return standardFileSystemManager;
    }

    /** The opts. */
    protected FileSystemOptions opts = null;

    /**
     * Sets the opts.
     * 
     * @param opts the new opts
     */
    public void setOpts(FileSystemOptions opts) {
        this.opts = opts;
    }

    /** The open. */
    protected boolean open = false;

    /**
     * Checks if is opened.
     * 
     * @return true, if is opened
     */
    public boolean isOpened() {
        return open;
    }

    /**
     * Open.
     * 
     * @throws MotuException the motu exception
     */
    public void open() throws MotuException {
        open("", "", "");
    }

    /**
     * Open.
     * 
     * @param user the user
     * @param pwd the pwd
     * @param scheme the scheme
     * 
     * @throws MotuException the motu exception
     */
    public void open(String user, String pwd, String scheme) throws MotuException {
        if (isOpened()) {
            return;
        }

        standardFileSystemManager = new StandardFileSystemManager();
        standardFileSystemManager.setLogger(LogFactory.getLog(VFS.class));
        try {
            standardFileSystemManager.setConfiguration(ConfigLoader.getInstance().get(Organizer.getVFSProviderConfig()));
            standardFileSystemManager.setCacheStrategy(CacheStrategy.ON_CALL);
            standardFileSystemManager.setFilesCache(new SoftRefFilesCache());
            // standardFileSystemManager.addProvider("moi", new DefaultLocalFileProvider());
            standardFileSystemManager.init();
            open = true;
        } catch (FileSystemException e) {
            LOG.fatal("Error in VFS initialisation - Unable to intiialize VFS", e);
            throw new MotuException("Error in VFS initialisation - Unable to intiialize VFS", e);
        } catch (IOException e) {
            LOG.fatal("Error in VFS initialisation - Unable to intiialize VFS", e);
            throw new MotuException("Error in VFS initialisation - Unable to intiialize VFS", e);
        }

        opts = new FileSystemOptions();

        setUserInfo(user, pwd);
        setSchemeOpts(scheme);

    }

    /**
     * Sets the user info.
     * 
     * @param user the user
     * @param pwd the pwd
     * 
     * @return the file system options
     * 
     * @throws MotuException the motu exception
     */
    public FileSystemOptions setUserInfo(String user, String pwd) throws MotuException {
        if (opts == null) {
            opts = new FileSystemOptions();
        }
        StaticUserAuthenticator auth = new StaticUserAuthenticator(null, user, pwd);
        try {
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
        } catch (FileSystemException e) {
            throw new MotuException("Error in VFSManager#setUserInfo", e);
        }

        return opts;

    }

    /**
     * Sets the scheme.
     * 
     * @param scheme the scheme
     * 
     * @return the file system options
     * 
     * @throws MotuException the motu exception
     */
    public FileSystemOptions setSchemeOpts(String scheme) throws MotuException {

        if (Organizer.isNullOrEmpty(scheme)) {
            return opts;
        }

        if (opts == null) {
            opts = new FileSystemOptions();
        }

        FileSystemConfigBuilder fscb;
        try {
            try {
                fscb = standardFileSystemManager.getFileSystemConfigBuilder(scheme);
            } catch (FileSystemException e) {
                fscb = standardFileSystemManager.getFileSystemConfigBuilder(VFSManager.DEFAULT_SCHEME);
            }
            
            if (fscb instanceof FtpFileSystemConfigBuilder) {
                FtpFileSystemConfigBuilder ftpFscb = (FtpFileSystemConfigBuilder) fscb;
                // ftpFscb.setUserDirIsRoot(opts, true);

            }

            if (fscb instanceof SftpFileSystemConfigBuilder) {
                SftpFileSystemConfigBuilder sftpFscb = (SftpFileSystemConfigBuilder) fscb;
                // sftpFscb.setUserDirIsRoot(opts, true);
                sftpFscb.setStrictHostKeyChecking(opts, Organizer.getMotuConfigInstance().getStrictHostKeyChecking());

                long sftpTimeOut = Organizer.getMotuConfigInstance().getSftpSessionTimeOut().toStandardDuration().getMillis();
                if (sftpTimeOut > Integer.MAX_VALUE) {
                    throw new MotuException(String.format("Motu Configuration : sftp timeout value is too large '%ld' milliseconds. Max is '%d'",
                                                          sftpTimeOut,
                                                          Integer.MAX_VALUE));
                }

                if (sftpTimeOut > 0) {                    
                    sftpFscb.setTimeout(opts, (int) sftpTimeOut);
                }

            }

        } catch (FileSystemException e) {
            throw new MotuException("Error in VFSManager#setScheme", e);
        }

        return opts;
    }

    /**
     * Close.
     */
    public void close() {
        if (isOpened()) {
            standardFileSystemManager.close();
            open = false;
        }
    }

    /**
     * Gets the uri as input stream.
     * 
     * @param uri the uri
     * 
     * @return the uri as input stream
     * 
     * @throws MotuException the motu exception
     */
    public InputStream getUriAsInputStream(String uri) throws MotuException {
        InputStream in = null;
        try {

            FileObject fileObject = resolveFile(uri);
            if (fileObject != null) {
                in = fileObject.getContent().getInputStream();
            }
        } catch (IOException e) {
            throw new MotuException(String.format("'%s' uri file has not be found", uri), e);
        }
        return in;
    }

    /**
     * Resolve file.
     * 
     * @param uri the uri
     * 
     * @return the file object
     * 
     * @throws MotuException the motu exception
     */
    public FileObject resolveFile(final String uri) throws MotuException {
        FileObject fileObject = null;
        open();
        if (opts == null) {
            opts = new FileSystemOptions();
        }

        try {
            //URI uriObject = new URI(uri);
            URI uriObject = Organizer.newURI(uri);

            setSchemeOpts(uriObject.getScheme());

            fileObject = standardFileSystemManager.resolveFile(uri, opts);
        } catch (FileSystemException e) {
            throw new MotuException(String.format("Unable to resolve uri '%s' ", uri), e);
        } catch (URISyntaxException e) {
            throw new MotuException(String.format("Unable to resolve uri '%s' ", uri), e);
        }
        return fileObject;

    }

    /**
     * Copy file.
     * 
     * @param user the user
     * @param pwd the pwd
     * @param scheme the scheme
     * @param host the host
     * @param fileSrc the file src
     * @param fileDest the file dest
     * 
     * @throws MotuExceptionBase the motu exception base
     */
    public void copyFileToLocalFile(String user, String pwd, String scheme, String host, String fileSrc, String fileDest) throws MotuExceptionBase {

        open(user, pwd, scheme);

        FileObject foSrc = null;
        FileObject foDest = null;
        String uri = "";
        
        try {
            File newFile = VFSManager.createLocalFile(fileDest);

            uri = String.format("%s://%s/%s", scheme, host, fileSrc);
            foSrc = resolveFile(uri);
            if (foSrc == null) {
                throw new MotuException(String.format("Unable to resolve source uri '%s' ", uri));
            }

            foDest = standardFileSystemManager.toFileObject(newFile);
            if (foDest == null) {
                throw new MotuException(String.format("Unable to resolve dest uri '%s' ", fileDest));
            }
            foDest.copyFrom(foSrc, Selectors.SELECT_ALL);

        } catch (MotuExceptionBase e) {
            throw e;
        } catch (Exception e) {
            //throw new MotuException(String.format("Unable to copy file '%s' to '%s'", foSrc.getURL().toString(), foDest.getURL().toString()), e);
            throw new MotuException(String.format("Unable to copy file '%s' to '%s'", uri.toString(), fileDest), e);
        }

    }

    /**
     * Copy file to local file.
     * 
     * @param uriSrc the uri src
     * @param fileDest the file dest
     * 
     * @throws MotuException the motu exception
     */
    public void copyFileToLocalFile(String uriSrc, String fileDest) throws MotuException {
        // URI uri = new URI(uriSrc);
        //        
        // String[] userInfo = uri.getUserInfo().split(":");
        // String user = "";
        // String pwd = "";
        //        
        // if (userInfo.length >= 2) {
        // pwd = userInfo[1];
        // }
        //
        // if (userInfo.length >= 1) {
        // user = userInfo[0];
        // }
        //        
        // copyFile(user, pwd, uri.getScheme(), uri.

        FileObject foSrc = null;
        FileObject foDest = null;

        try {
            File newFile = VFSManager.createLocalFile(fileDest);

            foSrc = resolveFile(uriSrc);
            if (foSrc == null) {
                throw new MotuException(String.format("Unable to resolve source uri '%s' ", uriSrc));
            }

            foDest = standardFileSystemManager.toFileObject(newFile);
            if (foSrc == null) {
                throw new MotuException(String.format("Unable to resolve dest uri '%s' ", newFile.getAbsolutePath()));
            }
            foDest.copyFrom(foSrc, Selectors.SELECT_ALL);

        } catch (Exception e) {
            try {
                throw new MotuException(String.format("Unable to copy file '%s' to '%s'", foSrc.getURL().toString(), foDest.getURL().toString()), e);
            } catch (FileSystemException e1) {
                throw new MotuException(String.format("Unable to copy files", e1));
            }
        }

    }

    /**
     * Creates the local file.
     * 
     * @param localFile the local file
     * 
     * @return the file
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static File createLocalFile(String localFile) throws IOException {

        File newFile = new File(localFile);
        File path = new File(newFile.getParent());
        path.mkdirs();
        newFile.createNewFile();

        return newFile;

    }

    /**
     * Delete a file.
     * 
     * @param file the file to delete
     * 
     * @return true, if successful
     * @throws MotuException 
     */
    public boolean delete(String file) throws MotuException {
        FileObject fileToDelete = resolveFile(file);
        return delete(fileToDelete);
    }
    
//    public static boolean deleteDirectory(String path) {
//        return VFSManager.deleteDirectory(new File(path));
//        
//    }

    /**
 * Delete directory.
 * 
 * @param path the path
 * 
 * @return true, if successful
     * @throws MotuException 
 */
public boolean deleteDirectory(String path) throws MotuException {
    
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(path);
    
    if (!(path.endsWith("/") || path.endsWith("\\"))) {
        stringBuffer.append("/");
    }
        FileObject pathToDelete = resolveFile(stringBuffer.toString());
        return deleteDirectory(pathToDelete);
        
    }
    
    /**
     * Delete directory ând all descendents of the file.
     * 
     * @param file the file
     * 
     * @return true, if successful
     * @throws MotuException 
     */
    public boolean deleteDirectory(FileObject file) throws MotuException {
        return delete(file, Selectors.SELECT_ALL);
    }

    /**
     * Delete directory.
     * 
     * @param path the path
     * 
     * @return true, if successful
     */
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
    
    /**
     * Delete the file repsented by the file parameter.
     * 
     * @param file the file
     * 
     * @return true, if successful
     * @throws MotuException 
     */
    public boolean delete(FileObject file) throws MotuException {
        
        boolean deleted = false;
        try {
            if (file.exists()) {
                deleted = file.delete();
            }
        } catch (FileSystemException e) {
            //throw new MotuException(String.format("Unable to copy file '%s' to '%s'", foSrc.getURL().toString(), foDest.getURL().toString()), e);
            throw new MotuException(String.format("Unable to delete '%s'", file.getName().toString()), e);
        }
        return deleted;
    }
    
    /**
     * Delete all descendents of this file that match a selector.
     * 
     * @param file the file
     * @param selector the selector
     * 
     * @return true, if successful
     * @throws MotuException 
     */
    public boolean delete(FileObject file, FileSelector selector) throws MotuException {
        
        int deleted = 0;
        try {
            if (file.exists()) {
                deleted = file.delete(selector);
            }
        } catch (FileSystemException e) {
            //throw new MotuException(String.format("Unable to copy file '%s' to '%s'", foSrc.getURL().toString(), foDest.getURL().toString()), e);
            throw new MotuException(String.format("Unable to delete '%s'", file.getName().toString()), e);
        }
        return (deleted > 0);
    }
    
     
    /**
     * Copy file.
     * 
     * @param from the from
     * @param to the to
     * 
     * @throws MotuException the motu exception
     */
    public void copyFile(String from, String to) throws MotuException {
        FileObject src = resolveFile(from);
        FileObject dest = resolveFile(to);
        copyFile(src, dest);
    }
    
    /**
     * Copy file.
     * 
     * @param from the from
     * @param to the to
     * 
     * @throws MotuException the motu exception
     */
    public void copyFile(FileObject from, FileObject to) throws MotuException {
        try {
            to.copyFrom(from, Selectors.SELECT_ALL);
        } catch (Exception e) {
            //throw new MotuException(String.format("Unable to copy file '%s' to '%s'", foSrc.getURL().toString(), foDest.getURL().toString()), e);
            throw new MotuException(String.format("Unable to copy file '%s' to '%s'", from.getName().toString(), from.getName().toString()), e);
        }
    }
}
