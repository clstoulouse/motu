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
package fr.cls.atoll.motu.processor.wps;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.StandardFileSystemManager;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class TestVFS {

    public static void main(String[] args) {

        testBugDoReplicateFile();

    }

    public static void testBugDoReplicateFile() {

        StandardFileSystemManager standardFileSystemManager = new StandardFileSystemManager();
        standardFileSystemManager.setLogger(LogFactory.getLog(VFS.class));
        standardFileSystemManager.setClassLoader(TestVFS.class.getClassLoader());
        try {
            URL configUrl = new URL("file:/J:/dev/atoll-v2/atoll-motu/atoll-motu-library/src/main/resources/motuVFSProvider.xml");
            standardFileSystemManager.setConfiguration(configUrl);
            standardFileSystemManager.setCacheStrategy(CacheStrategy.ON_CALL);
            standardFileSystemManager.init();

            String uri = "jar:file:/C:/Documents%20and%20Settings/dearith/.m2/repository/org/jvnet/ogc/iso-19139-d_2006_05_04-schema/1.0.0-PATCH-CLS/iso-19139-d_2006_05_04-schema-1.0.0-PATCH-CLS.jar!/schema/iso19139";
            FileSystemOptions opts = new FileSystemOptions();

            FileObject fileObject = standardFileSystemManager.resolveFile(uri, opts);

        } catch (FileSystemException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
