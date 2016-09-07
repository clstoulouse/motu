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
package fr.cls.atoll.motu.web.dal.request.netcdf.data.vfs.provider.gsiftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.Messages;
import org.globus.ftp.FileInfo;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
import org.globus.io.streams.GridFTPInputStream;
import org.globus.io.streams.GridFTPOutputStream;

/**
 * An FTP file.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
@SuppressWarnings("unchecked")
public class GsiFtpFileObject extends AbstractFileObject {

    /** The log. */
    private final Log log = LogFactory.getLog(GsiFtpFileObject.class);

    /** The Constant EMPTY_FTP_FILE_MAP. */
    private static final Map EMPTY_FTP_FILE_MAP = Collections.unmodifiableMap(new TreeMap());

    /** The ftp fs. */
    private final GsiFtpFileSystem ftpFs;

    /** The rel path. */
    private final String relPath;

    // Cached info
    /** The file info. */
    private FileInfo fileInfo;

    /** The children. */
    private Map children;

    // private FileObject linkDestination;

    /**
     * Instantiates a new gsi ftp file object.
     * 
     * @param name the name
     * @param fileSystem the file system
     * @param rootName the root name
     * 
     * @throws FileSystemException the file system exception
     */
    protected GsiFtpFileObject(final AbstractFileName name, final GsiFtpFileSystem fileSystem, final FileName rootName) throws FileSystemException {
        super(name, fileSystem);
        ftpFs = fileSystem;
        String relPathTmp = UriParser.decode(rootName.getRelativeName(name));

        // log.debug("FileName=" + name + " Root=" + rootName
        // + " Relative path=" + relPath );

        if (".".equals(relPathTmp)) {
            // do not use the "." as path against the ftp-server
            // e.g. the uu.net ftp-server do a recursive listing then
            // this.relPath = UriParser.decode(rootName.getPath());
            // this.relPath = ".";
            // boolean ok = true;
            // try {
            // cwd = ftpFs.getClient().getCurrentDir();
            // }
            // catch (ServerException se) { ok = false;}
            // catch (IOException se) { ok = false;}

            // if ( ! ok ) {
            // throw new FileSystemException("vfs.provider.gsiftp/get-type.error", getName());
            // }
            this.relPath = "/"; // cwd;
        } else {
            this.relPath = relPathTmp;
        }
    }

    /**
     * Called by child file objects, to locate their ftp file info.
     * 
     * @param name the filename in its native form ie. without uri stuff (%nn)
     * @param flush recreate children cache
     * 
     * @return the child file
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private FileInfo getChildFile(final String name, final boolean flush) throws IOException {
        if (flush) {
            children = null;
        }

        // List the children of this file
        doGetChildren();

        // Look for the requested child
        FileInfo ftpFile = (FileInfo) children.get(name);
        return ftpFile;
    }

    /**
     * Fetches the children of this file, if not already cached.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void doGetChildren() throws IOException {
        if (children != null) {
            return;
        }

        final GridFTPClient client = ftpFs.getClient();
        try {
            // String key =
            // GsiFtpFileSystemConfigBuilder.getInstance().getEntryParser(getFileSystem().getFileSystemOptions());

            /** required to perform multiple requests **/
            client.setLocalPassive();
            client.setActive();

            final Vector tmpChildren = client.list(getName().getPath());

            if (tmpChildren == null || tmpChildren.size() == 0) {
                children = EMPTY_FTP_FILE_MAP;
            } else {
                children = new TreeMap();

                // Remove '.' and '..' elements
                for (int i = 0; i < tmpChildren.size(); i++) {
                    // final FTPFile child = tmpChildren[i];
                    final FileInfo child = (FileInfo) tmpChildren.get(i);

                    if (child == null) {
                        if (log.isDebugEnabled()) {
                            log.debug(Messages.getString("vfs.provider.ftp/invalid-directory-entry.debug", new Object[] { new Integer(i), relPath }));
                        }
                        continue;
                    }
                    if (!".".equals(child.getName()) && !"..".equals(child.getName())) {
                        children.put(child.getName(), child);
                    }
                }
            }
        } catch (ServerException se) {
            throw new IOException(se.getMessage());
        } catch (ClientException ce) {
            throw new IOException(ce.getMessage());
        } finally {
            ftpFs.putClient(client);
        }
    }

    /**
     * Attaches this file object to its file resource.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    protected void doAttach() throws IOException {
        // Get the parent folder to find the info for this file
        getInfo(false);
    }

    /**
     * Fetches the info for this file.
     * 
     * @param flush the flush
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void getInfo(boolean flush) throws IOException {
        final GsiFtpFileObject parent = (GsiFtpFileObject) getParent();
        FileInfo newFileInfo = null;

        if (parent != null) {
            newFileInfo = parent.getChildFile(UriParser.decode(getName().getBaseName()), flush);
        } else {
            // Assume the root is a directory and exists
            newFileInfo = new FileInfo();

            // GridFTTP Only runs in UNIX so this would be OK
            newFileInfo.setName("/");
            newFileInfo.setFileType(FileInfo.DIRECTORY_TYPE);
        }

        this.fileInfo = newFileInfo;
    }

    /**
     * Detaches this file object from its file resource.
     */
    @Override
    protected void doDetach() {
        this.fileInfo = null;
        children = null;
    }

    /**
     * Called when the children of this file change.
     * 
     * @param child the child
     * @param newType the new type
     */
    @Override
    protected void onChildrenChanged(FileName child, FileType newType) {
        if (children != null && newType.equals(FileType.IMAGINARY)) {
            try {
                children.remove(UriParser.decode(child.getBaseName()));
            } catch (FileSystemException e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            // if child was added we have to rescan the children
            // TODO - get rid of this
            children = null;
        }
    }

    /**
     * Called when the type or content of this file changes.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    protected void onChange() throws IOException {
        children = null;

        if (getType().equals(FileType.IMAGINARY)) {
            // file is deleted, avoid server lookup
            this.fileInfo = null;
            return;
        }

        getInfo(true);
    }

    /**
     * Determines the type of the file, returns null if the file does not exist.
     * 
     * @return the file type
     * 
     * @throws Exception the exception
     */
    @Override
    protected FileType doGetType() throws Exception {
        // log.debug("relative path:" + relPath);

        if (this.fileInfo == null) {
            return FileType.IMAGINARY;
        } else if (this.fileInfo.isDirectory()) {
            return FileType.FOLDER;
        } else if (this.fileInfo.isFile()) {
            return FileType.FILE;
        } else if (this.fileInfo.isSoftLink()) {
            return FileType.FILE; // getLinkDestination().getType();
        }

        throw new FileSystemException("vfs.provider.gsiftp/get-type.error", getName());
    }

    // private FileObject getLinkDestination() throws FileSystemException
    // {
    // if (linkDestination == null)
    // {
    // final String path = this.fileInfo.getLink();
    // FileName relativeTo = getName().getParent();
    // if (relativeTo == null)
    // {
    // relativeTo = getName();
    // }
    // FileName linkDestinationName = getFileSystem().getFileSystemManager().resolveName(relativeTo, path);
    // linkDestination = getFileSystem().resolveFile(linkDestinationName);
    // }
    //
    // return linkDestination;
    // }

    // protected FileObject[] doListChildrenResolved() throws Exception
    // {
    // if (this.fileInfo.isSymbolicLink())
    // {
    // return getLinkDestination().getChildren();
    // }
    //
    // return null;
    // }

    /**
     * Lists the children of the file.
     * 
     * @return the string[]
     * 
     * @throws Exception the exception
     */
    @Override
    protected String[] doListChildren() throws Exception {
        // List the children of this file
        doGetChildren();

        // TODO - get rid of this children stuff
        final String[] childNames = new String[children.size()];
        int childNum = -1;
        Iterator iterChildren = children.values().iterator();

        while (iterChildren.hasNext()) {
            childNum++;
            final FileInfo child = (FileInfo) iterChildren.next();
            childNames[childNum] = child.getName();
        }

        return UriParser.encode(childNames);
    }

    /**
     * Deletes the file.
     * 
     * @throws Exception the exception
     */
    @Override
    protected void doDelete() throws Exception {
        boolean ok = true;
        final GridFTPClient ftpClient = ftpFs.getClient();
        try {
            // String Path = relPath;

            // if ( ! relPath.startsWith("/")) {
            // Path = "/" + Path;
            // log.warn("relative path " + relPath + " doesn't start with (/). Using:" + Path + " instead");
            // }

            if (this.fileInfo.isDirectory()) {
                ftpClient.deleteDir(getName().getPath());
            } else {
                ftpClient.deleteFile(getName().getPath());
            }
        } catch (IOException ioe) {
            ok = false;
        } catch (ServerException e) {
            ok = false;
        } finally {
            ftpFs.putClient(ftpClient);
        }

        if (!ok) {
            throw new FileSystemException("vfs.provider.gsiftp/delete-file.error", getName());
        }
        this.fileInfo = null;
        children = EMPTY_FTP_FILE_MAP;
    }

    /**
     * Renames the file.
     * 
     * @param newfile the newfile
     * 
     * @throws Exception the exception
     */
    @Override
    protected void doRename(FileObject newfile) throws Exception {
        boolean ok = true;
        final GridFTPClient ftpClient = ftpFs.getClient();
        try {
            String oldName = getName().getPath();
            String newName = newfile.getName().getPath();
            ftpClient.rename(oldName, newName);
        } catch (IOException ioe) {
            ok = false;
        } catch (ServerException e) {
            ok = false;
        } finally {
            ftpFs.putClient(ftpClient);
        }

        if (!ok) {
            throw new FileSystemException("vfs.provider.gsiftp/rename-file.error", new Object[] { getName().toString(), newfile });
        }
        this.fileInfo = null;
        children = EMPTY_FTP_FILE_MAP;
    }

    /**
     * Creates this file as a folder.
     * 
     * @throws Exception the exception
     */
    @Override
    protected void doCreateFolder() throws Exception {
        boolean ok = true;
        final GridFTPClient client = ftpFs.getClient();
        try {
            client.makeDir(getName().getPath());
        } catch (IOException ioe) {
            ok = false;
        } catch (ServerException se) {
            ok = false;
        } finally {
            ftpFs.putClient(client);
        }

        if (!ok) {
            throw new FileSystemException("vfs.provider.gsiftp/create-folder.error", getName());
        }
    }

    /**
     * Returns the size of the file content (in bytes).
     * 
     * @return the long
     * 
     * @throws Exception the exception
     */
    @Override
    protected long doGetContentSize() throws Exception {
        // if (this.fileInfo.isSymbolicLink())
        // {
        // return getLinkDestination().getContent().getSize();
        // }
        // else
        // {
        return this.fileInfo.getSize();
        // }
    }

    /**
     * get the last modified time on an ftp file.
     * 
     * @return the long
     * 
     * @throws Exception the exception
     * 
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doGetLastModifiedTime()
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        // if (this.fileInfo.isSymbolicLink())
        // {
        // return getLinkDestination().getContent().getLastModifiedTime();
        // }
        // else
        // {
        // Unix ls date/time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm");

        String timestamp = fileInfo.getDate() + " " + fileInfo.getTime();

        // Calendar timestamp = this.fileInfo.getTimestamp();
        if (timestamp == null) {
            return 0L;
        } else {
            // return (timestamp.getTime().getTime());
            return sdf.parse(timestamp).getTime();
        }
        // }
    }

    /**
     * Creates an input stream to read the file content from.
     * 
     * @return the input stream
     * 
     * @throws Exception the exception
     */
    @Override
    protected InputStream doGetInputStream() throws Exception {
        // String Path = relPath;

        log.debug("Creating GridFTP Input Stream to host:" + GsiFtpClientFactory.host + ":" + GsiFtpClientFactory.port);

        // if ( ! relPath.startsWith("/")) {
        // Path = "/" + Path;
        // log.warn("Relative path " + relPath + " doesn't start with /. Using:" + Path + " instead");
        // }

        GridFTPInputStream fis = new GridFTPInputStream(null, GsiFtpClientFactory.host, GsiFtpClientFactory.port, getName().getPath());

        // final InputStream instr = client.retrieveFileStream(relPath);
        return fis; // new FtpInputStream(client, instr);
    }

    // protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception
    // {
    // return new FtpRandomAccessContent(this, mode);
    // }

    /**
     * Creates an output stream to write the file content to.
     * 
     * @param bAppend the b append
     * 
     * @return the output stream
     * 
     * @throws Exception the exception
     */
    @Override
    protected OutputStream doGetOutputStream(boolean bAppend) throws Exception {
        // String Path = relPath;

        log.debug("Creating FTP Output Stream to host:" + GsiFtpClientFactory.port + ":" + GsiFtpClientFactory.port + " Abs path:"
                + getName().getPath() + " Append:" + bAppend);

        // if ( ! relPath.startsWith("/")) {
        // Path = "/" + Path;
        // log.warn("Relative path " + relPath + " doesn't start with /. Using:" + Path + " instead");
        // }

        return new GridFTPOutputStream(null, GsiFtpClientFactory.host, GsiFtpClientFactory.port, getName().getPath(), bAppend);

        // log.debug("Creating FTP Output stream: Unimplemented");
        // throw new Exception("Unimplemented");

        // final FtpClient client = ftpFs.getClient();
        // OutputStream out = null;
        // if (bAppend)
        // {
        // out = client.appendFileStream(relPath);
        // }
        // else
        // {
        // out = client.storeFileStream(relPath);
        // }
        //
        // if (out == null)
        // {
        // throw new FileSystemException("vfs.provider.ftp/output-error.debug", new Object[]
        // {
        // this.getName(),
        // client.getReplyString()
        // });
        // }
        //
        // return new FtpOutputStream(client, out);
    }

    /**
     * Gets the rel path.
     * 
     * @return the rel path
     */
    String getRelPath() {
        return relPath;
    }

    // FtpInputStream getInputStream(long filePointer) throws IOException
    // {
    // final FtpClient client = ftpFs.getClient();
    // final InputStream instr = client.retrieveFileStream(relPath, filePointer);
    // if (instr == null)
    // {
    // throw new FileSystemException("vfs.provider.ftp/input-error.debug", new Object[]
    // {
    // this.getName(),
    // client.getReplyString()
    // });
    // }
    // return new FtpInputStream(client, instr);
    // }

    /**
     * An InputStream that monitors for end-of-file.
     */
    // class FtpInputStream
    // extends MonitorInputStream
    // {
    // private final FtpClient client;
    //
    // public FtpInputStream(final FtpClient client, final InputStream in)
    // {
    // super(in);
    // this.client = client;
    // }
    //
    // void abort() throws IOException
    // {
    // client.abort();
    // close();
    // }
    //
    // /**
    // * Called after the stream has been closed.
    // */
    // protected void onClose() throws IOException
    // {
    // final boolean ok;
    // try
    // {
    // ok = client.completePendingCommand();
    // }
    // finally
    // {
    // ftpFs.putClient(client);
    // }
    //
    // if (!ok)
    // {
    // throw new FileSystemException("vfs.provider.ftp/finish-get.error", getName());
    // }
    // }
    // }
    /**
     * An OutputStream that monitors for end-of-file.
     */
    // private class FtpOutputStream
    // extends MonitorOutputStream
    // {
    // private final FtpClient client;
    //
    // public FtpOutputStream(final FtpClient client, final OutputStream outstr)
    // {
    // super(outstr);
    // this.client = client;
    // }
    //
    // /**
    // * Called after this stream is closed.
    // */
    // protected void onClose() throws IOException
    // {
    // final boolean ok;
    // try
    // {
    // ok = client.completePendingCommand();
    // }
    // finally
    // {
    // ftpFs.putClient(client);
    // }
    //
    // if (!ok)
    // {
    // throw new FileSystemException("vfs.provider.ftp/finish-put.error", getName());
    // }
    // }
    // }
}
