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

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.exception.ServerException;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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

            String username = UserAuthenticatorUtils
                    .getData(authData, UserAuthenticationData.USERNAME, UserAuthenticatorUtils.toChar(rootName.getUserName())).toString();
            String password = UserAuthenticatorUtils
                    .getData(authData, UserAuthenticationData.PASSWORD, UserAuthenticatorUtils.toChar(rootName.getPassword())).toString();
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
