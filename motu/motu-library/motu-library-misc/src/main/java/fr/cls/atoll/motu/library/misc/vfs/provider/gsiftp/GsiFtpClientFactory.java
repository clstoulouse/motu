/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.cls.atoll.motu.library.misc.vfs.provider.gsiftp;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.globus.ftp.GridFTPClient;

import java.io.IOException;

/**
 * Create a GridFtpClient instance.
 * 
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @author <a href="mailto:vladimir_silva@yaho.com">Vladimir Silva</a>
 * @version $Revision: 1.1 $ $Date: 2009-05-14 14:58:33 $
 */
public class GsiFtpClientFactory {
    
    /** The host. */
    static String host;
    
    /** The port. */
    static int port;

    /**
     * Instantiates a new gsi ftp client factory.
     */
    private GsiFtpClientFactory() {
    }

    /**
     * Creates a new connection to the server.
     * 
     * @param hostname the hostname
     * @param portN the port n
     * @param username the username
     * @param password the password
     * @param fileSystemOptions the file system options
     * 
     * @return the grid ftp client
     * 
     * @throws FileSystemException the file system exception
     */
    public static GridFTPClient createConnection(String hostname, int portN, String username, String password, FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        GsiFtpClientFactory.host = hostname;
        GsiFtpClientFactory.port = portN;

        try {
            if (password == null) {
                throw new Exception("Password cannot be null");
            }

            // Create a proxy cert (if missing)
            new ProxyTool().createProxy(password);

            final GridFTPClient client = new GridFTPClient(hostname, port);

            try {

                // Authenticate w/ user credentials defines in $HOME/.globus/cog.properties
                client.authenticate(null);

                // Set binary mode
                // if (!client.setFileType(FTP.BINARY_FILE_TYPE))
                // {
                // throw new FileSystemException("vfs.provider.ftp/set-binary.error", hostname);
                // }

                // Set dataTimeout value
                // Integer dataTimeout =
                // FtpFileSystemConfigBuilder.getInstance().getDataTimeout(fileSystemOptions);
                // if (dataTimeout != null)
                // {
                // client.setDataTimeout(dataTimeout.intValue());
                // }

                // Change to root by default
                // All file operations a relative to the filesystem-root
                // String root = getRoot().getName().getPath();

                // Boolean userDirIsRoot =
                // FtpFileSystemConfigBuilder.getInstance().getUserDirIsRoot(fileSystemOptions);
                // if (workingDirectory != null && (userDirIsRoot == null || !userDirIsRoot.booleanValue()))
                // {
                // if (!client.changeWorkingDirectory(workingDirectory))
                // {
                // throw new FileSystemException("vfs.provider.ftp/change-work-directory.error",
                // workingDirectory);
                // }
                // }
                //
                // Boolean passiveMode =
                // FtpFileSystemConfigBuilder.getInstance().getPassiveMode(fileSystemOptions);
                // if (passiveMode != null && passiveMode.booleanValue())
                // {
                // client.enterLocalPassiveMode();
                // }
            } catch (final IOException e) {
                if (client != null) { // .isConnected())
                    client.close();
                }
                throw e;
            }

            return client;
        } catch (final Exception exc) {
            throw new FileSystemException("vfs.provider.gsiftp/connect.error", new Object[] { hostname }, exc);
        }
    }
}