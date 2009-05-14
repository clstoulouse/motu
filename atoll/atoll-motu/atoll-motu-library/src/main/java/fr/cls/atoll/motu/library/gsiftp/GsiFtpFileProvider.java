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
package fr.cls.atoll.motu.library.gsiftp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.GenericFileName;
import org.globus.ftp.GridFTPClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A provider for accessing files over GsiFTP.
 * 
 * @author <a href="mailto:vladimir_silva@yahoo.com">Vladimir Silva</a>
 * @version $Id: GsiFtpFileProvider.java,v 1.1 2009-05-14 14:16:46 dearith Exp $
 */
public class GsiFtpFileProvider extends AbstractOriginatingFileProvider {
    
    /** The log. */
    private Log log = LogFactory.getLog(GsiFtpFileProvider.class);

    /** The Constant CAPABILITIES. */
    @SuppressWarnings("unchecked")
    protected final static Collection CAPABILITIES = Collections.unmodifiableCollection(Arrays.asList(new Capability[] {
            Capability.CREATE, Capability.DELETE, Capability.RENAME, Capability.GET_TYPE, Capability.LIST_CHILDREN, Capability.READ_CONTENT,
            Capability.URI, Capability.WRITE_CONTENT, Capability.GET_LAST_MODIFIED, Capability.SET_LAST_MODIFIED_FILE
    // Capability.RANDOM_ACCESS_READ
            }));

    /** The Constant ATTR_HOME_DIR. */
    public final static String ATTR_HOME_DIR = "HOME_DIRECTORY";

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
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions) throws FileSystemException {
        // Create the file system
        final GenericFileName rootName = (GenericFileName) name;

        // Session session;
        GridFTPClient client;
        String attrHome;
        try {
            log.debug("Creating connection to GsiFTP Host:" + rootName.getHostName() + " Port:" + rootName.getPort() + " User:"
                    + rootName.getUserName() + " Path:" + rootName.getPath());

            client = GsiFtpClientFactory.createConnection(rootName.getHostName(),
                                                          rootName.getPort(),
                                                          rootName.getUserName(),
                                                          rootName.getPassword(),
                                                          fileSystemOptions);

            attrHome = client.getCurrentDir();
            log.debug("Current directory: " + attrHome);
        } catch (final Exception e) {
            throw new FileSystemException("vfs.provider.gsiftp/connect.error", name, e);
        }

        // set HOME dir attribute
        final GsiFtpFileSystem fs = new GsiFtpFileSystem(rootName, client, fileSystemOptions);
        fs.setAttribute(ATTR_HOME_DIR, attrHome);

        return fs;
    }

    /**
     * Initialises the component.
     * 
     * @throws FileSystemException the file system exception
     */
    public void init() throws FileSystemException {
    }

    /** {@inheritDoc} */
    public FileSystemConfigBuilder getConfigBuilder() {
        return GsiFtpFileSystemConfigBuilder.getInstance();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Collection getCapabilities() {
        return CAPABILITIES;
    }
}