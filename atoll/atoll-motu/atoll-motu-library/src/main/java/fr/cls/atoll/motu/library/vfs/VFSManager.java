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
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.commons.vfs.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.http.HttpFileSystemConfigBuilder;
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
 * @version $Revision: 1.11 $ - $Date: 2009-08-20 16:10:02 $
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOpts() - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOpts() - exiting");
        }
        return opts;
    }

    /**
     * Gets the standard file system manager.
     * 
     * @return the standard file system manager
     */
    public StandardFileSystemManager getStandardFileSystemManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStandardFileSystemManager() - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getStandardFileSystemManager() - exiting");
        }
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("setOpts(FileSystemOptions) - entering");
        }

        this.opts = opts;

        if (LOG.isDebugEnabled()) {
            LOG.debug("setOpts(FileSystemOptions) - exiting");
        }
    }

    /** The open. */
    protected boolean open = false;

    /**
     * Checks if is opened.
     * 
     * @return true, if is opened
     */
    public boolean isOpened() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isOpened() - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("isOpened() - exiting");
        }
        return open;
    }

    /**
     * Open.
     * 
     * @throws MotuException the motu exception
     */
    public void open() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("open() - entering");
        }

        open("", "", "");

        if (LOG.isDebugEnabled()) {
            LOG.debug("open() - exiting");
        }
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("open(String, String, String) - entering");
        }

        if (isOpened()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("open(String, String, String) - exiting");
            }
            return;
        }

        standardFileSystemManager = new StandardFileSystemManager();
        standardFileSystemManager.setLogger(LogFactory.getLog(VFS.class));
        try {
            standardFileSystemManager.setConfiguration(ConfigLoader.getInstance().get(Organizer.getVFSProviderConfig()));
            standardFileSystemManager.setCacheStrategy(CacheStrategy.ON_CALL);
            //standardFileSystemManager.setFilesCache(new SoftRefFilesCache());
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("open(String, String, String) - exiting");
        }
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("setUserInfo(String, String) - entering");
        }

        if (opts == null) {
            opts = new FileSystemOptions();
        }
        StaticUserAuthenticator auth = new StaticUserAuthenticator(null, user, pwd);
        try {
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
        } catch (FileSystemException e) {
            LOG.error("setUserInfo(String, String)", e);

            throw new MotuException("Error in VFSManager#setUserInfo", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("setUserInfo(String, String) - exiting");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSchemeOpts(String) - entering");
        }

        if (Organizer.isNullOrEmpty(scheme)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setSchemeOpts(String) - exiting");
            }
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
                LOG.error("setSchemeOpts(String)", e);

                fscb = standardFileSystemManager.getFileSystemConfigBuilder(VFSManager.DEFAULT_SCHEME);
            }
            
            if (fscb instanceof FtpFileSystemConfigBuilder) {
                FtpFileSystemConfigBuilder ftpFscb = (FtpFileSystemConfigBuilder) fscb;
                //ftpFscb.setUserDirIsRoot(opts, true);

            }
            
            if (fscb instanceof HttpFileSystemConfigBuilder) {
                HttpFileSystemConfigBuilder httpFscb = (HttpFileSystemConfigBuilder) fscb;
                String proxyHost = Organizer.getMotuConfigInstance().getProxyHost();
                String proxyPort = Organizer.getMotuConfigInstance().getProxyPort();
                httpFscb.setProxyHost(opts, proxyHost);
                httpFscb.setProxyPort(opts, Integer.parseInt(proxyPort));

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
            LOG.error("setSchemeOpts(String)", e);

            throw new MotuException("Error in VFSManager#setScheme", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("setSchemeOpts(String) - exiting");
        }
        return opts;
    }

    /**
     * Close.
     */
    public void close() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("close() - entering");
        }

        if (isOpened()) {
            standardFileSystemManager.close();
            open = false;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("close() - exiting");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUriAsInputStream(String) - entering");
        }

        InputStream in = null;
        try {

            FileObject fileObject = resolveFile(uri);
            if (fileObject != null) {
                in = fileObject.getContent().getInputStream();
            }
        } catch (IOException e) {
            LOG.error("getUriAsInputStream(String)", e);

            throw new MotuException(String.format("'%s' uri file has not be found", uri), e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getUriAsInputStream(String) - exiting");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("resolveFile(String) - entering");
        }

        FileObject returnFileObject = resolveFile(uri, this.opts);
        if (LOG.isDebugEnabled()) {
            LOG.debug("resolveFile(String) - exiting");
        }
        return returnFileObject;
    }
    
    /**
     * Resolve file.
     * 
     * @param uri the uri
     * @param fileSystemOptions the file system options
     * 
     * @return the file object
     * 
     * @throws MotuException the motu exception
     */
    public FileObject resolveFile(final String uri, FileSystemOptions fileSystemOptions) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resolveFile(String, FileSystemOptions) - entering");
        }

        FileObject fileObject = null;
        
        open();

//        if (fileSystemOptions == null) {
//            fileSystemOptions = new FileSystemOptions();
//        }

        try {
            //URI uriObject = new URI(uri);
            URI uriObject = Organizer.newURI(uri);

            fileSystemOptions = setSchemeOpts(uriObject.getScheme());

            fileObject = standardFileSystemManager.resolveFile(uri, fileSystemOptions);
        } catch (FileSystemException e) {
            LOG.error("resolveFile(String, FileSystemOptions)", e);

            throw new MotuException(String.format("Unable to resolve uri '%s' ", uri), e);
        } catch (URISyntaxException e) {
            LOG.error("resolveFile(String, FileSystemOptions)", e);

            throw new MotuException(String.format("Unable to resolve uri '%s' ", uri), e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("resolveFile(String, FileSystemOptions) - exiting");
        }
        return fileObject;

    }
    
    /**
     * Resolve file.
     * 
     * @param baseFile the base file
     * @param file the file
     * 
     * @return the file object
     * 
     * @throws MotuException the motu exception
     */
    public FileObject resolveFile(FileObject baseFile, final String file) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resolveFile(FileObject, String) - entering");
        }

        FileObject fileObject = null;
        open();
        if (opts == null) {
            opts = new FileSystemOptions();
        }

        try {
            
            setSchemeOpts(baseFile.getName().getScheme());

            fileObject = standardFileSystemManager.resolveFile(baseFile, file, opts);
        } catch (FileSystemException e) {
            LOG.error("resolveFile(FileObject, String)", e);

            throw new MotuException(String.format("Unable to resolve uri '%s/%s' ", baseFile.getName().toString(), file), e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("resolveFile(FileObject, String) - exiting");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFileToLocalFile(String, String, String, String, String, String) - entering");
        }

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
            LOG.error("copyFileToLocalFile(String, String, String, String, String, String)", e);

            throw e;
        } catch (Exception e) {
            LOG.error("copyFileToLocalFile(String, String, String, String, String, String)", e);

            //throw new MotuException(String.format("Unable to copy file '%s' to '%s'", foSrc.getURL().toString(), foDest.getURL().toString()), e);
            throw new MotuException(String.format("Unable to copy file '%s' to '%s'", uri.toString(), fileDest), e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFileToLocalFile(String, String, String, String, String, String) - exiting");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFileToLocalFile(String, String) - entering");
        }

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
            LOG.error("copyFileToLocalFile(String, String)", e);

            try {
                throw new MotuException(String.format("Unable to copy file '%s' to '%s'", foSrc.getURL().toString(), foDest.getURL().toString()), e);
            } catch (FileSystemException e1) {
                LOG.error("copyFileToLocalFile(String, String)", e1);

                throw new MotuException(String.format("Unable to copy files", e1));
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFileToLocalFile(String, String) - exiting");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("createLocalFile(String) - entering");
        }

        File newFile = new File(localFile);
        File path = new File(newFile.getParent());
        path.mkdirs();
        newFile.createNewFile();

        if (LOG.isDebugEnabled()) {
            LOG.debug("createLocalFile(String) - exiting");
        }
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
    public boolean deleteFile(String file) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteFile(String) - entering");
        }

        FileObject fileToDelete = resolveFile(file);
        boolean returnboolean = deleteFile(fileToDelete);
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteFile(String) - exiting");
        }
        return returnboolean;
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteDirectory(String) - entering");
        }
    
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(path);
    
    if (!(path.endsWith("/") || path.endsWith("\\"))) {
        stringBuffer.append("/");
    }
        FileObject pathToDelete = resolveFile(stringBuffer.toString());
        boolean returnboolean = deleteDirectory(pathToDelete);
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteDirectory(String) - exiting");
        }
        return returnboolean;
        
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteDirectory(FileObject) - entering");
        }

        boolean returnboolean = delete(file, Selectors.SELECT_ALL);
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteDirectory(FileObject) - exiting");
        }
        return returnboolean;
    }

    /**
     * Delete directory.
     * 
     * @param path the path
     * 
     * @return true, if successful
     */
    public static boolean deleteDirectory(File path) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteDirectory(File) - entering");
        }

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
        boolean returnboolean = (path.delete());
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteDirectory(File) - exiting");
        }
        return returnboolean;
    }
    
    /**
     * Delete the file repsented by the file parameter.
     * 
     * @param file the file
     * 
     * @return true, if successful
     * @throws MotuException 
     */
    public boolean deleteFile(FileObject file) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteFile(FileObject) - entering");
        }
        
        boolean deleted = false;
        try {

            if (file.exists()) {
                if (file.getType() != FileType.FILE) {
                    throw new MotuException(String.format("Delete file '%s' is rejected: it is a folder. ", file.getName().toString()));
                }
                
                deleted = file.delete();
            }
        } catch (FileSystemException e) {
            LOG.error("deleteFile(FileObject)", e);

            //throw new MotuException(String.format("Unable to copy file '%s' to '%s'", foSrc.getURL().toString(), foDest.getURL().toString()), e);
            throw new MotuException(String.format("Unable to delete '%s'", file.getName().toString()), e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteFile(FileObject) - exiting");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("delete(FileObject, FileSelector) - entering");
        }
        
        int deleted = 0;
        try {
            if (file.exists()) {
                deleted = file.delete(selector);
            }
        } catch (FileSystemException e) {
            LOG.error("delete(FileObject, FileSelector)", e);

            //throw new MotuException(String.format("Unable to copy file '%s' to '%s'", foSrc.getURL().toString(), foDest.getURL().toString()), e);
            throw new MotuException(String.format("Unable to delete '%s'", file.getName().toString()), e);
        }
        boolean returnboolean = (deleted > 0);
        if (LOG.isDebugEnabled()) {
            LOG.debug("delete(FileObject, FileSelector) - exiting");
        }
        return returnboolean;
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFile(String, String) - entering");
        }
        
        //FileObject originBase = fsManager.resolveFile(uri, opts);
        //fsManager.setBaseFile(originBase);

        FileObject src = resolveFile(from);
        FileObject dest = resolveFile(to);
        copyFile(src, dest);

        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFile(String, String) - exiting");
        }
    }
    
    /**
     * Copy file.
     * 
     * @param from the from
     * @param to the to
     * @param optsFrom the opts from
     * @param optsTo the opts to
     * 
     * @throws MotuException the motu exception
     */
    public void copyFile(String from, String to, FileSystemOptions optsFrom, FileSystemOptions optsTo) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFile(String, String, FileSystemOptions, FileSystemOptions) - entering");
        }

        FileObject src = resolveFile(from, optsFrom);
        FileObject dest = resolveFile(to, optsTo);
        copyFile(src, dest);

        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFile(String, String, FileSystemOptions, FileSystemOptions) - exiting");
        }
    }
    
    /**
     * Copy file.
     * 
     * @param from the from
     * @param to the to
     * @param userFrom the user from
     * @param pwdFrom the pwd from
     * @param userTo the user to
     * @param pwdTo the pwd to
     * 
     * @throws MotuException the motu exception
     */
    public void copyFile(String from, String to, String userFrom, String pwdFrom, String userTo, String pwdTo) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFile(String, String, String, String, String, String) - entering");
        }

        opts = null;
        if (!Organizer.isNullOrEmpty(userFrom)) {
            opts = setUserInfo(userFrom, pwdFrom);            
        }
        FileObject src = resolveFile(from, opts);

        opts = null;
        if (!Organizer.isNullOrEmpty(userTo)) {
            opts = setUserInfo(userTo, pwdTo);            
        }

        FileObject dest = resolveFile(to, opts);
        
        copyFile(src, dest);

        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFile(String, String, String, String, String, String) - exiting");
        }
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFile(FileObject, FileObject) - entering");
        }

        try {
            if ((to.exists())&& (to.getType() == FileType.FOLDER)) {
                throw new MotuException(String.format("File copy from '%s' to '%s' is rejected: the destination already exists and is a folder. You were about to loose all of the content of '%s' ", from.getName().toString(), to.getName().toString(), to.getName().toString()));
            }
            to.copyFrom(from, Selectors.SELECT_ALL);
        } catch (MotuException e) {
            LOG.error("copyFile(FileObject, FileObject)", e);

            throw e;
        } catch (Exception e) {
            LOG.error("copyFile(FileObject, FileObject)", e);

            //throw new MotuException(String.format("Unable to copy file '%s' to '%s'", foSrc.getURL().toString(), foDest.getURL().toString()), e);
            throw new MotuException(String.format("Unable to copy file '%s' to '%s'", from.getName().toString(), to.getName().toString()), e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFile(FileObject, FileObject) - exiting");
        }
    }
}
