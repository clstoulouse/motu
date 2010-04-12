package fr.cls.atoll.motu.library.vfs.provider.gsiftp;

import java.io.IOException;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.UserAuthenticationData;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.util.UserAuthenticatorUtils;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.exception.ServerException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-05-18 12:29:54 $
 */
public class GridFTPClientWrapper extends GridFTPClient {

    /** The root. */
    private final GenericFileName root;

    /** The file system options. */
    private final FileSystemOptions fileSystemOptions;

    /** The grid ftp client. */
    private GridFTPClient gridFtpClient = null;

    /**
     * Instantiates a new grid ftp client wrapper.
     * 
     * @param root the root
     * @param fileSystemOptions the file system options
     * 
     * @throws ServerException the server exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public GridFTPClientWrapper(final GenericFileName root, final FileSystemOptions fileSystemOptions) throws ServerException, IOException {
        super(root.getHostName(), root.getPort());
        this.root = root;
        this.fileSystemOptions = fileSystemOptions;
        getGridFtpClient(); // fail-fast
    }

    /**
     * Gets the root.
     * 
     * @return the root
     */
    public GenericFileName getRoot() {
        return root;
    }

    /**
     * Gets the file system options.
     * 
     * @return the file system options
     */
    public FileSystemOptions getFileSystemOptions() {
        return fileSystemOptions;
    }

    /**
     * Creates the client.
     * 
     * @return the grid ftp client
     * 
     * @throws FileSystemException the file system exception
     */
    protected GridFTPClient createClient() throws FileSystemException {
        final GenericFileName rootName = getRoot();

        UserAuthenticationData authData = null;
        try {
            authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, GsiFtpFileProvider.AUTHENTICATOR_TYPES);

            String username = UserAuthenticatorUtils.getData(authData,
                                                             UserAuthenticationData.USERNAME,
                                                             UserAuthenticatorUtils.toChar(rootName.getUserName())).toString();
            String password = UserAuthenticatorUtils.getData(authData,
                                                             UserAuthenticationData.PASSWORD,
                                                             UserAuthenticatorUtils.toChar(rootName.getPassword())).toString();
            return GsiFtpClientFactory.createConnection(rootName.getHostName(), rootName.getPort(), username, password, getFileSystemOptions());
        } finally {
            UserAuthenticatorUtils.cleanup(authData);
        }
    }

    /**
     * Gets the grid ftp client.
     * 
     * @return the grid ftp client
     * 
     * @throws FileSystemException the file system exception
     */
    protected GridFTPClient getGridFtpClient() throws FileSystemException {
        if (gridFtpClient == null) {
            gridFtpClient = createClient();
        }

        return gridFtpClient;
    }

}
