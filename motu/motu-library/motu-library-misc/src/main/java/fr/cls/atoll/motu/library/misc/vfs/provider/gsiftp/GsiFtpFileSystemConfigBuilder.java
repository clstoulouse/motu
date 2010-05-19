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

import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemOptions;

/**
 * The config builder for various sftp configuration options.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class GsiFtpFileSystemConfigBuilder extends FileSystemConfigBuilder {

    /** The Constant BUILDER. */
    private final static GsiFtpFileSystemConfigBuilder BUILDER = new GsiFtpFileSystemConfigBuilder();

    // private final static String FACTORY_KEY = FTPFileEntryParserFactory.class.getName() + ".KEY";
    // private final static String PASSIVE_MODE = FtpFileSystemConfigBuilder.class.getName() + ".PASSIVE";
    // private final static String USER_DIR_IS_ROOT = FtpFileSystemConfigBuilder.class.getName() +
    // ".USER_DIR_IS_ROOT";
    // private final static String DATA_TIMEOUT = FtpFileSystemConfigBuilder.class.getName() +
    // ".DATA_TIMEOUT";
    /** The Constant FACTORY_KEY. */
    private final static String FACTORY_KEY = FileSystemConfigBuilder.class.getName() + ".KEY";

    /** The Constant PASSIVE_MODE. */
    private final static String PASSIVE_MODE = FileSystemConfigBuilder.class.getName() + ".PASSIVE";

    /** The Constant USER_DIR_IS_ROOT. */
    private final static String USER_DIR_IS_ROOT = FileSystemConfigBuilder.class.getName() + ".USER_DIR_IS_ROOT";

    /** The Constant DATA_TIMEOUT. */
    private final static String DATA_TIMEOUT = FileSystemConfigBuilder.class.getName() + ".DATA_TIMEOUT";

    /**
     * Gets the single instance of GsiFtpFileSystemConfigBuilder.
     * 
     * @return single instance of GsiFtpFileSystemConfigBuilder
     */
    public static GsiFtpFileSystemConfigBuilder getInstance() {
        return BUILDER;
    }

    /**
     * Instantiates a new gsi ftp file system config builder.
     */
    private GsiFtpFileSystemConfigBuilder() {
    }

    /**
     * FTPFileEntryParserFactory which will be used for ftp-entry parsing.
     * 
     * @param opts the opts
     * @param key the key
     */
    // public void setEntryParserFactory(FileSystemOptions opts, FTPFileEntryParserFactory factory)
    // {
    // setParam(opts, FTPFileEntryParserFactory.class.getName(), factory);
    // }

    /**
     * @param opts
     * @see #setEntryParserFactory
     */
    // public FTPFileEntryParserFactory getEntryParserFactory(FileSystemOptions opts)
    // {
    // return (FTPFileEntryParserFactory) getParam(opts, FTPFileEntryParserFactory.class.getName());
    // }

    /**
     * set the FQCN of your FileEntryParser used to parse the directory listing from your server.<br />
     * <br />
     * <i>If you do not use the default commons-net FTPFileEntryParserFactory e.g. by using
     * {@link #setEntryParserFactory} this is the "key" parameter passed as argument into your custom
     * factory</i>
     * 
     * @param opts the opts
     * @param key the key
     */

    public void setEntryParser(FileSystemOptions opts, String key) {
        setParam(opts, FACTORY_KEY, key);
    }

    /**
     * Gets the entry parser.
     * 
     * @param opts the opts
     * 
     * @return the entry parser
     * 
     * @see #setEntryParser
     */
    public String getEntryParser(FileSystemOptions opts) {
        return (String) getParam(opts, FACTORY_KEY);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    protected Class getConfigClass() {
        return GsiFtpFileSystem.class;
    }

    /**
     * enter into passive mode.
     * 
     * @param opts the opts
     * @param passiveMode the passive mode
     */
    public void setPassiveMode(FileSystemOptions opts, boolean passiveMode) {
        setParam(opts, PASSIVE_MODE, passiveMode ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Gets the passive mode.
     * 
     * @param opts the opts
     * 
     * @return the passive mode
     * 
     * @see #setPassiveMode
     */
    public Boolean getPassiveMode(FileSystemOptions opts) {
        return (Boolean) getParam(opts, PASSIVE_MODE);
    }

    /**
     * use user directory as root (do not change to fs root).
     * 
     * @param opts the opts
     * @param userDirIsRoot the user dir is root
     */
    public void setUserDirIsRoot(FileSystemOptions opts, boolean userDirIsRoot) {
        setParam(opts, USER_DIR_IS_ROOT, userDirIsRoot ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Gets the user dir is root.
     * 
     * @param opts the opts
     * 
     * @return the user dir is root
     * 
     * @see #setUserDirIsRoot
     */
    public Boolean getUserDirIsRoot(FileSystemOptions opts) {
        return (Boolean) getParam(opts, USER_DIR_IS_ROOT);
    }

    /**
     * Gets the data timeout.
     * 
     * @param opts the opts
     * 
     * @return the data timeout
     * 
     * @see #setDataTimeout
     */
    public Integer getDataTimeout(FileSystemOptions opts) {
        return (Integer) getParam(opts, DATA_TIMEOUT);
    }

    /**
     * set the data timeout for the ftp client.<br />
     * If you set the dataTimeout to <code>null</code> no dataTimeout will be set on the ftp client.
     * 
     * @param opts the opts
     * @param dataTimeout the data timeout
     */
    public void setDataTimeout(FileSystemOptions opts, Integer dataTimeout) {
        setParam(opts, DATA_TIMEOUT, dataTimeout);
    }

}
