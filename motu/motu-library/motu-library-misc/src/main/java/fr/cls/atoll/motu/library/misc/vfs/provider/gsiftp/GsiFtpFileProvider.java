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
package fr.cls.atoll.motu.library.misc.vfs.provider.gsiftp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.UserAuthenticationData;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.GenericFileName;

/**
 * A provider for accessing files over GsiFTP. This file was modified by CLS.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class GsiFtpFileProvider extends AbstractOriginatingFileProvider {

    /** The log. */
    private final Log log = LogFactory.getLog(GsiFtpFileProvider.class);

    /** The Constant CAPABILITIES. */
    @SuppressWarnings("unchecked")
    protected final static Collection CAPABILITIES = Collections.unmodifiableCollection(Arrays.asList(new Capability[] {
            Capability.CREATE, Capability.DELETE, Capability.RENAME, Capability.GET_TYPE, Capability.LIST_CHILDREN, Capability.READ_CONTENT,
            Capability.URI, Capability.WRITE_CONTENT, Capability.GET_LAST_MODIFIED, Capability.SET_LAST_MODIFIED_FILE
    // Capability.RANDOM_ACCESS_READ
            }));

    /** The Constant ATTR_HOME_DIR. */
    public final static String ATTR_HOME_DIR = "HOME_DIRECTORY";

    /** The Constant AUTHENTICATOR_TYPES. */
    public final static UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[] {
            UserAuthenticationData.USERNAME, UserAuthenticationData.PASSWORD };

    /**
     * Instantiates a new gsi ftp file provider.
     */
    public GsiFtpFileProvider() {
        super();
        setFileNameParser(GsiFtpFileNameParser.getInstance());
    }

    /**
     * Creates a {@link FileSystem}.
     * 
     * @param name the name
     * @param fileSystemOptions the file system options
     * 
     * @return the file system
     * 
     * @throws FileSystemException the file system exception
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions) throws FileSystemException {
        // Create the file system
        final GenericFileName rootName = (GenericFileName) name;

        String attrHome;
        GridFTPClientWrapper gridFtpClient = null;
        try {
            gridFtpClient = new GridFTPClientWrapper(rootName, fileSystemOptions);
            log.debug("Creating connection to GsiFTP Host:" + gridFtpClient.getRoot().getHostName() + " Port:" + gridFtpClient.getRoot().getPort()
                    + " User:" + gridFtpClient.getRoot().getUserName() + " Path:" + gridFtpClient.getRoot().getPath());
            attrHome = gridFtpClient.getCurrentDir();
            log.debug("Current directory: " + attrHome);
        } catch (Exception e) {
            throw new FileSystemException("vfs.provider.gsiftp/connect.error", name, e);
        }

        // // Session session;
        // GridFTPClient client;
        // String attrHome;
        // try {
        // log.debug("Creating connection to GsiFTP Host:" + rootName.getHostName() + " Port:" +
        // rootName.getPort() + " User:"
        // + rootName.getUserName() + " Path:" + rootName.getPath());
        //
        // client = GsiFtpClientFactory.createConnection(rootName.getHostName(),
        // rootName.getPort(),
        // rootName.getUserName(),
        // rootName.getPassword(),
        // fileSystemOptions);
        //
        // attrHome = client.getCurrentDir();
        // log.debug("Current directory: " + attrHome);
        // } catch (final Exception e) {
        // throw new FileSystemException("vfs.provider.gsiftp/connect.error", name, e);
        // }

        // set HOME dir attribute
        final GsiFtpFileSystem fs = new GsiFtpFileSystem(rootName, gridFtpClient, fileSystemOptions);
        fs.setAttribute(ATTR_HOME_DIR, attrHome);

        return fs;
    }

    /**
     * Initialises the component.
     * 
     * @throws FileSystemException the file system exception
     */
    @Override
    public void init() throws FileSystemException {
    }

    /** {@inheritDoc} */
    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return GsiFtpFileSystemConfigBuilder.getInstance();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Collection getCapabilities() {
        return CAPABILITIES;
    }
}