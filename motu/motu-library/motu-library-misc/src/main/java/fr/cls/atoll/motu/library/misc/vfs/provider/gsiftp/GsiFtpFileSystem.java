package fr.cls.atoll.motu.library.misc.vfs.provider.gsiftp;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.GenericFileName;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.exception.ServerException;

/**
 * Represents the files on an SFTP server.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class GsiFtpFileSystem extends AbstractFileSystem implements FileSystem {

    /** The Constant LOG. */
    private final static Log LOG = LogFactory.getLog(GsiFtpFileSystem.class);

    // An idle client
    /** The idle client. */
    private GridFTPClient idleClient;

    /** The idle client sync. */
    private final Object idleClientSync = new Object();

    // File system attribute
    /** The attribs. */
    @SuppressWarnings("unchecked")
    private final Map attribs = new HashMap();

    /**
     * The Constructor.
     * 
     * @param rootName the root name
     * @param ftpClient the ftp client
     * @param fileSystemOptions the file system options
     */
    protected GsiFtpFileSystem(final GenericFileName rootName, final GridFTPClient ftpClient, final FileSystemOptions fileSystemOptions) {
        super(rootName, null, fileSystemOptions);

        idleClient = ftpClient;
    }

    /** {@inheritDoc} */
    @Override
    protected void doCloseCommunicationLink() {
        // Clean up the connection
        if (idleClient != null) {
            closeConnection(idleClient);
            idleClient = null;
        }
    }

    /**
     * Adds the capabilities of this file system.
     * 
     * @param caps the caps
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void addCapabilities(final Collection caps) {
        caps.addAll(GsiFtpFileProvider.CAPABILITIES);
    }

    /**
     * Cleans up the connection to the server.
     * 
     * @param client the client
     */
    private void closeConnection(final GridFTPClient client) {
        try {
            // Clean up
            // if (client.isConnected())
            // {
            client.close(); // disconnect();
            // }
        } catch (final IOException ioe) {
            // getLogger().warn("vfs.provider.ftp/close-connection.error", e);
            // VfsLog.warn(getLogger(), log,
            // "vfs.provider.ftp/close-connection.error", ioe);
            LOG.warn("vfs.provider.ftp/close-connection.error", ioe);
        } catch (final ServerException e) {
            // getLogger().warn("vfs.provider.ftp/close-connection.error", e);
            // VfsLog.warn(getLogger(), log,
            // "vfs.provider.ftp/close-connection.error", e);
            LOG.warn("vfs.provider.ftp/close-connection.error", e);
        }
    }

    /**
     * Creates an FTP client to use.
     * 
     * @return the client
     * 
     * @throws FileSystemException the file system exception
     */
    public GridFTPClient getClient() throws FileSystemException {
        synchronized (idleClientSync) {
            if (idleClient == null) {
                final GenericFileName rootName = (GenericFileName) getRoot().getName();

                LOG.debug("Creating connection to GSIFTP Host: " + rootName.getHostName() + " Port:" + rootName.getPort() + " User:"
                        + rootName.getUserName() + " Path: " + rootName.getPath());

                return GsiFtpClientFactory.createConnection(rootName.getHostName(), rootName.getPort(), rootName.getUserName(), rootName
                        .getPassword(),
                // rootName.getPath(),
                                                            getFileSystemOptions());
            } else {
                final GridFTPClient client = idleClient;
                idleClient = null;
                return client;
            }
        }
    }

    /**
     * Returns an FTP client after use.
     * 
     * @param client the client
     */
    public void putClient(final GridFTPClient client) {
        synchronized (idleClientSync) {
            if (idleClient == null) {
                // Hang on to client for later
                idleClient = client;
            } else {
                // Close the client
                closeConnection(client);
            }
        }
    }

    /**
     * Creates a file object.
     * 
     * @param name the name
     * 
     * @return the file object
     * 
     * @throws FileSystemException the file system exception
     */
    @Override
    protected FileObject createFile(final FileName name) throws FileSystemException {
        return new GsiFtpFileObject(name, this, getRootName());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void setAttribute(final String attrName, final Object value) throws FileSystemException {
        attribs.put(attrName, value);
    }

    /** {@inheritDoc} */
    @Override
    public Object getAttribute(final String attrName) throws FileSystemException {
        return attribs.get(attrName);
    }

}
