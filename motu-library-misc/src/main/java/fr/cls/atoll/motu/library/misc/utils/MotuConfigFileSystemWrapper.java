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

package fr.cls.atoll.motu.library.misc.utils;

import org.apache.log4j.Logger;

import fr.cls.atoll.motu.library.misc.configuration.ConfigFileSystemType;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
/**
 * This class allows to wrap motu configuration field.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */

public class MotuConfigFileSystemWrapper<K> extends ObjectUtils<K> {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(MotuConfigFileSystemWrapper.class);
    
//    public static final String FTP_PROTOCOL = "ftp";
//    public static final String SFTP_PROTOCOL = "ftp";
//    public static final String HTTP_PROTOCOL = "http";
    
    public static final String PROP_HOST = "host";
    public static final String PROP_FTPUSERDIRISROOT = "ftpUserDirIsRoot";
    public static final String PROP_FTPPASSIVEMODE = "ftpPassiveMode";
    public static final String PROP_FTPDATATIMEOUT = "ftpDataTimeOut";
    public static final String PROP_SFTPUSERDIRISROOT = "sftpUserDirIsRoot";
    public static final String PROP_SFTPSESSIONTIMEOUT = "sftpSessionTimeOut";
    public static final String PROP_STRICTHOSTKEYCHECKING = "strictHostKeyChecking";
    public static final String PROP_USEHTTPPROXY = "useProxy";
    public static final String PROP_USEFTPPROXY = "useFtpProxy";
    public static final String PROP_USESFTPPROXY = "useSftpProxy";
    public static final String PROP_HTTPPROXYLOGIN = "proxyLogin";
    public static final String PROP_HTTPPROXYPWD = "proxyPwd";
    public static final String PROP_HTTPPROXYHOST = "proxyHost";
    public static final String PROP_HTTPPROXYPORT = "proxyPort";
    public static final String PROP_SFTPPROXYLOGIN = "sftpProxyLogin";
    public static final String PROP_SFTPPROXYPWD = "sftpProxyPwd";
    public static final String PROP_SFTPPROXYHOST = "sftpProxyHost";
    public static final String PROP_SFTPPROXYPORT = "sftpProxyPort";
    public static final String PROP_FTPPROXYLOGIN = "ftpProxyLogin";
    public static final String PROP_FTPPROXYPWD = "ftpProxyPwd";
    public static final String PROP_FTPPROXYHOST = "ftpProxyHost";
    public static final String PROP_FTPPROXYPORT = "ftpProxyPort";
    
    /**
     * Gets the field value.
     *
     * @param key the key
     * @param fieldName the field name
     * @return the field value
     * @throws MotuException the motu exception
     */
    public K getFieldValue(String key, String fieldName) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFieldValue(String, String) - start");
        }

        ObjectUtils<K> objectUtils = new ObjectUtils<K>();        
        K value = null;
        K globalValue = null;
        
        try {
            
        globalValue = objectUtils.getValue(Organizer.getMotuConfigInstance(), fieldName);

        if (Organizer.isNullOrEmpty(key)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getFieldValue(String, String) - end");
                }
            return globalValue;
        }

        ConfigFileSystemType configFileSystem = Organizer.getConfigFileSytem(key);

        if (configFileSystem == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getFieldValue(String, String) - end");
                }
            return globalValue;
        }

            value = objectUtils.getValue(configFileSystem, fieldName);
        } catch (Exception e) {
            LOG.error("getFieldValue(String, String)", e);

            throw new MotuException(
                    "ERROR in Organizer#isBooleanOptions: unable to convert the list of file system configurations to a map object.",
                    e);
        }

        if (value == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getFieldValue(String, String) - end");
            }
            return globalValue;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getFieldValue(String, String) - end");
        }
        return value;

    }
    
}
