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
package fr.cls.atoll.motu.library.misc.ftp;

import com.jcraft.jsch.Session;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.utils.ConfigLoader;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.commons.vfs.provider.ftp.FtpClientFactory;
import org.apache.commons.vfs.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.http.HttpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpClientFactory;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.log4j.Logger;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class TestFtp {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(TestFtp.class);
    private static final Log _LOG = LogFactory.getLog(TestFtp.class);

    public class Client extends Thread {
        /**
         * Logger for this class
         */
        private final Logger LOG = Logger.getLogger(Client.class);

        public Client(String name) {
            this.name = name;
        }

        String name;

        @Override
        public void run() {

            System.out.print("Start Client ");
            System.out.println(name);

            try {
                Organizer.getVFSSystemManager().copyFileToLocalFile("t",
                                                                    "t",
                                                                    "sftp",
                                                                    "CLS-EARITH.pc.cls.fr",
                                                                    "AsciiEnvisat.txt",
                                                                    "c:/tempVFS/AsciiEnvisat.txt");
                Organizer.removeVFSSystemManager();
            } catch (MotuExceptionBase e) {
                System.out.print("Exception in ");
                System.out.println(name);
                System.out.println(e.notifyException());
                e.printStackTrace();
            }

            System.out.print("End Client ");
            System.out.println(name);

        }
    }
    public class Client2 extends Thread {
        /**
         * Logger for this class
         */
        private final Logger LOG = Logger.getLogger(Client2.class);

        public Client2(String name) {
            this.name = name;
        }

        String name;

        @Override
        public void run() {

            System.out.print("Start Client2 ");
            System.out.println(name);

            try {
                Organizer.getVFSSystemManager()
                        .copyFileToLocalFile("atoll",
                                             "atoll",
                                             "sftp",
                                             "catsat-data1.cls.fr/home/atoll",
                                             "/atoll-distrib/HOA_Catsat/Interface_ATOLL/nrt_med_infrared_sst_timestamp_FTP_20090516.xml",
                                             "c:/tempVFS/nrt_med_infrared_sst_timestamp_FTP_20090516.xml");
                Organizer.removeVFSSystemManager();
            } catch (MotuExceptionBase e) {
                System.out.print("Exception in ");
                System.out.println(name);
                System.out.println(e.notifyException());
                e.printStackTrace();
            }

            System.out.print("End Client2 ");
            System.out.println(name);

        }
    }
    public class Client3 extends Thread {
        /**
         * Logger for this class
         */
        private final Logger LOG = Logger.getLogger(Client3.class);

        public Client3(String name) {
            this.name = name;
        }

        String name;

        @Override
        public void run() {

            System.out.print("Start Client3 ");
            System.out.println(name);

            try {
                Organizer.getVFSSystemManager().copyFileToLocalFile("anonymous",
                                                                    "email",
                                                                    "ftp",
                                                                    "ftp.cls.fr/pub/oceano/AVISO/",
                                                                    "NRT-SLA/maps/rt/j2/h/msla_rt_j2_err_21564.nc.gz",
                                                                    "c:/tempVFS/msla_rt_j2_err_21564.nc.gz");
                Organizer.removeVFSSystemManager();
            } catch (MotuExceptionBase e) {
                System.out.print("Exception in ");
                System.out.println(name);
                System.out.println(e.notifyException());
                e.printStackTrace();
            }

            System.out.print("End Client3 ");
            System.out.println(name);

        }
    }
    public class Client4 extends Thread {
        /**
         * Logger for this class
         */
        private final Logger LOG = Logger.getLogger(Client4.class);

        public Client4(String name) {
            this.name = name;
        }

        String name;

        @Override
        public void run() {

            System.out.print("Start Client4 ");
            System.out.println(name);

            try {
                Organizer.getVFSSystemManager().copyFileToLocalFile("anonymous@ftp.unidata.ucar.edu",
                                                                    "",
                                                                    "ftp",
                                                                    "proxy.cls.fr",
                                                                    "/pub/README",
                                                                    "c:/tempVFS/README");
                Organizer.removeVFSSystemManager();
            } catch (MotuExceptionBase e) {
                System.out.print("Exception in ");
                System.out.println(name);
                System.out.println(e.notifyException());
                e.printStackTrace();
            }

            System.out.print("End Client4 ");
            System.out.println(name);

        }
    }

    public static void testVFSThread() {

        TestFtp testFtp = new TestFtp();
        // TestFtp.Client[] c = { testFtp.new Client("Client1"), testFtp.new Client("Client2"), };
        TestFtp.Client[] c = { testFtp.new Client("Client1A"), };
        for (int i = 0; i < c.length; i++) {
            c[i].start();
        }
        TestFtp.Client2[] c2 = { testFtp.new Client2("Client2A"), };
        for (int i = 0; i < c2.length; i++) {
            c2[i].start();
        }
        TestFtp.Client3[] c3 = { testFtp.new Client3("Client3A"), };
        for (int i = 0; i < c3.length; i++) {
            c3[i].start();
        }
        TestFtp.Client4[] c4 = { testFtp.new Client4("Client4A"), };
        for (int i = 0; i < c4.length; i++) {
            c4[i].start();
        }

    }

    /**
     * .
     * 
     * @param args
     */
    public static void main(String[] args) {

        // try {
        // URI uri = new URI("sftp://catsat-data1.cls.fr/home/atoll");
        // URI newURI = new URI(uri.getScheme(), "atoll:atoll", uri.getHost(), uri.getPort(), uri.getPath(),
        // uri.getQuery(), uri.getFragment());
        // System.out.println(newURI.toString());
        //            
        // } catch (URISyntaxException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        // try {
        // Organizer.getVFSSystemManager();
        // //Organizer.closeVFSSystemManager();
        // Organizer.removeVFSSystemManager();
        // } catch (MotuException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        //

        // testFtp();
        // testSftp();
        // testVFS("t", "t", "sftp", "CLS-EARITH.pc.cls.fr", "AsciiEnvisat.txt");
        // testVFS("atoll", "atoll", "sftp", "catsat-data1.cls.fr/home/atoll",
        // "/atoll-distrib/HOA_Catsat/Interface_ATOLL/nrt_med_infrared_sst_timestamp_FTP_20090516.xml");

        // testVFS("anonymous", "email", "email.fr/pub/oceano/AVISO/",
        // "NRT-SLA/maps/rt/j2/h/msla_rt_j2_err_21564.nc.gz");
        // testVFS("anonymous@ftp.unidata.ucar.edu", "", "ftp", "proxy.cls.fr", "/pub/README");

        // testVFS("", "", "http", "catsat-data1.cls.fr:43080", "/thredds/catalog.xml");

        // testVFS("anonymous@gridftp.bigred.iu.teragrid.org:2811", "email", "gsiftp",        // "proxy.cls.fr", "/pub/README");
        // testVFS("anonymous@dcgftp.usatlas.bnl.gov:2811/", "email", "gsiftp",
        // "proxy.clemail  // "pnfs/usatlas.bnl.gov/arelvalid/loadtest/data1188508850256");
        // gsiftp://gridftp.bigred.iu.teragrid.org:2811/
        // gsiftp://155.69.144.160:8080/home/shahand/globus-4.1.2.1/var/DWSSDF/repository/org_globus_examples_services_core_first.0.gar
        // gsiftp://dcgftp.usatlas.bnl.gov/pnfs/usatlas.bnl.gov/arelvalid/loadtest/data1188508850256
        // gsiftp://dcgftp.usatlas.bnl.gov:2811/pnfs/usatlas.bnl.gov/data/prod/pandadev/

        // fromUri = "http://proxy.cls.fr/19139/20060504/serviceMetadata.xsd";
        // toUri = "ftp://t:t@CLS-EARITH.pc.cls.fr/MonDossier2/test.txt";
        // userFrom = "anonymous@schemas.opengis.net/iso";
        // pwdFrom = "email";
        // testVFS("login", "pwd", "http", "schemas.opengis.net",
        // "iso/19139/20060504/srv/serviceMetadata.xsd");
        // testVFSThread();

        String fromUri = "http://atoll-dev.cls.fr:30080/motu-extract/atoll-ressource-dataset-datafile-nrt-med-infrared-sst-timestamp_1244456058793.txt";
        // String toUri =
        // "sftp://t:t@CLS-EARITH.pc.cls.fr/atoll-ressource-dataset-datafile-nrt-med-infrared-sst-timestamp_1244456058793.txt";
        // String toUri =
        // "sftp://atoll:atoll@CLS-EARITH.pc.cls.fr/atoll-ressource-dataset-datafile-nrt-med-infrared-sst-timestamp_1244456058793.txt";
        // String toUri =
        // "file://c:/tempVFS/atoll-ressource-dataset-datafile-nrt-med-infrared-sst-timestamp_1244456058793.txt";
        // String toUri =
        // "c:/tempVFS/atoll-ressource-dataset-datafile-nrt-med-infrared-sst-timestamp_1244456058793.txt";
        // String toUri =
        // "c:\\tempVFS\\atoll-ressource-dataset-datafile-nrt-med-infrared-sst-timestamp_1244456058793.txt";
        String toUri = "sftp://atoll:atoll@catsat-data1.cls.fr/home/atoll/atoll-distrib/HOA_Catsat/Interface_ATOLL/test.txt";
        toUri = "ftp://t:t@CLS-EARITH.pc.cls.fr/test.txt";
        // testPush(fromUri, toUri);

        String userFrom = "";
        String pwdFrom = "";
        String userTo = "aviso";
        String pwdTo = "aviso;00";

        // toUri = "ftp://ftp.cls.fr/data/ftp/depot/oceano/AVISO/test.txt";
        toUri = "ftp://ftp.cls.fr/depot/oceano/AVISO/test.txt";
        // testPush(fromUri, toUri, userFrom, pwdFrom, userTo, pwdTo);

        userTo = "anonymous";
        pwdTo = "email";
        toUri = "ftp://ftpsedr.cls.femailistach/test.txt";
        // testPush(fromUri, toUri, userFrom, pwdFrom, userTo, pwdTo);

        userTo = "anonymous";
        pwdTo = "email";
        // testPush(fromUri, toUri, userFrom, emailo, pwdTo);

        userTo = "anonymous";
        pwdTo = "email";
        toUri = "ftp://CLS-EARITH.pc.cls.fr/test.txt";
        // testPush(fromUri, toUri, userFrom, pwdFrom, userTo, pwdTo);

        userTo = "t";
        pwdTo = "t";
        // toUri = "ftp://CLS-EARITH.pc.cls.fr/test.txt";
        toUri = "ftp://CLS-EARITH.pc.cls.fr/MonDossier/test.txt";
        // testPush(fromUri, toUri, userFrom, pwdFrom, userTo, pwdTo);

        fromUri = "sftp://atoll:atoll@catsat-data1.cls.fr/home/atoll/atoll-distrib/HOA_Catsat/Interface_ATOLL/test.txt";
        // toUri = "http://atoll-dev.cls.fr:30080/motu-extract/test.txt";
        // toUri = "http://atoll-dev.cls.fr:30080/motu-extract/test.txt";
        toUri = "ftp://t:t@CLS-EARITH.pc.cls.fr/MonDossier2/test.txt";
        // testPush(fromUri, toUri);

        fromUri = "ftp://proxy.cls.fr/pub/README";
        toUri = "ftp://t:t@CLS-EARITH.pc.cls.fr/MonDossier2/test.txt";
        userFrom = "anonymous@ftp.unidata.ucar.edu";
        pwdFrom = "email";
        //testPush(fromUri, toUri, userFrom, pwdFrom, userTo, pwdTo);
        fromUri = "http://proxy.cls.fr/19139/20060504/serviceMetadata.xsd";
        toUri = "ftp://t:t@CLS-EARITH.pc.cls.fr/MonDossier2/test.txt";
        userFrom = "anonymous@schemas.opengis.net/iso";
        pwdFrom = "email";
        //testPush(fromUri, toUri, userFrom, pwdFrom, userTo, pwdTo);

        //fromUri= "ftp://idpopendap:ghf57sf6@ftpsedr.cls.fr/donnees/ftpsedr/DUACS/global/dt/upd/sla/e1//dt_upd_global_e1_sla_vfec_19950510_19950515_20100503.nc.gz";
        //fromUri= "ftp://idpopendap:ghf57sf6@ftpsedr.cls.fr/global/dt/upd/sla/e1//dt_upd_global_e1_sla_vfec_19950510_19950515_20100503.nc.gz";
        fromUri= "ftp://idpopendap:ghf57sf6@ftpsedr.cls.fr/global/dt/upd/sla/e1/dt_upd_global_e1_sla_vfec_19950510_19950515_20100503.nc.gz";
                      toUri = "ftp://t:t@CLS-EARITH.pc.cls.fr/MonDossier2/test.nc";
        testPush(fromUri, toUri);
        
        // URI uriTest = null;
        // try {
        // uriTest =
        // Organizer.newURI("http:/\\atoll-dev.cls.fr:30080/motu-extract/atoll-ressource-dataset-datafile-nrt-med-infrared-sst-timestamp_1244456058793.txt");
        // } catch (URISyntaxException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // System.out.println(uriTest.toString());
    }

    public static void testFtp() {

        // System.setProperty("http.proxyHost", "proxy.cls.fr"); // adresse IP
        // System.setProperty("http.proxyPort", "8080");
        // System.setProperty("socksProxyHost", "proxy.cls.fr");
        // System.setProperty("socksProxyPort", "1080");

        try {

            // String user = "anonymous";
            // String pass = "email";
            // String server = "ftp.cls.fr";
            //
            // FTPClient client = new FTPClient();
            // client.connect(server);
            // System.out.print(client.getReplyString());
            // int reply = client.getReplyCode();
            // if (!FTPReply.isPositiveCompletion(reply))
            // {
            // throw new IllegalArgumentException("cant connect: " + reply);
            // }
            // if (!client.login(user, pass))
            // {
            // throw new IllegalArgumentException("login failed");
            // }
            // client.enterLocalPassiveMode();
            // client.disconnect();

            FileSystemOptions opts = new FileSystemOptions();
            // FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);

            String server = "proxy.cls.fr";
            String user = "anonymous@ftp.unidata.ucar.edu";
            String pass = "";
            server = "ftp.cls.fr";
            user = "anonymous";
            pass = "email";

            StaticUserAuthenticator auth = new StaticUserAuthenticator(null, user, pass);
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
            FileObject fo = VFS.getManager().resolveFile("ftp://ftp.cls.fr/pub/oceano/AVISO/NRT-SLA/maps/rt/j2/h/msla_rt_j2_err_21564.nc.gz", opts);

            FTPClient ftpClient = FtpClientFactory.createConnection(server, 21, user.toCharArray(), pass.toCharArray(), ".", opts);
        } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // } catch (SocketException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void testSftp() {

        try {

            // String user = "anonymous";
            // String pass = "email";
            // String server = "ftp.cls.fr";
            //
            // FTPClient client = new FTPClient();
            // client.connect(server);
            // System.out.print(client.getReplyString());
            // int reply = client.getReplyCode();
            // if (!FTPReply.isPositiveCompletion(reply))
            // {
            // throw new IllegalArgumentException("cant connect: " + reply);
            // }
            // if (!client.login(user, pass))
            // {
            // throw new IllegalArgumentException("login failed");
            // }
            // client.enterLocalPassiveMode();
            // client.disconnect();

            FileSystemOptions opts = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            // SftpFileSystemConfigBuilder.getInstance().setProxyHost(opts, "proxy.cls.fr");
            // SftpFileSystemConfigBuilder.getInstance().setProxyPort(opts, 8080);
            // SftpFileSystemConfigBuilder.getInstance().setProxyType(opts,
            // SftpFileSystemConfigBuilder.PROXY_SOCKS5 );
            // //SftpFileSystemConfigBuilder.getInstance().setProxyType(opts,
            // SftpFileSystemConfigBuilder.PROXY_HTTP );
            FileObject fo = VFS.getManager().resolveFile("sftp://t:t@CLS-EARITH.pc.cls.fr/AsciiEnvisat.txt", opts);
            String server = "CLS-EARITH.pc.cls.fr";
            String user = "t";
            String pass = "t";

            server = "aviso-motu.cls.fr";
            user = "mapserv";
            pass = "mapserv";
            Session sftpClient = SftpClientFactory.createConnection(server, 22, user.toCharArray(), pass.toCharArray(), opts);
        } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // } catch (SocketException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void testVFS(String user, String pwd, String scheme, String host, String file) {
        StandardFileSystemManager fsManager = null;

        try {
            fsManager = new StandardFileSystemManager();
            fsManager.setLogger(_LOG);

            StaticUserAuthenticator auth = new StaticUserAuthenticator(null, user, pwd);

            fsManager.setConfiguration(ConfigLoader.getInstance().get(Organizer.getVFSProviderConfig()));
            // fsManager.setCacheStrategy(CacheStrategy.ON_CALL);
            // fsManager.addProvider("moi", new DefaultLocalFileProvider());
            fsManager.init();

            FileSystemOptions opts = new FileSystemOptions();
            FileSystemConfigBuilder fscb = fsManager.getFileSystemConfigBuilder(scheme);
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);

            System.out.println(fsManager.getProviderCapabilities(scheme));

            if (fscb instanceof FtpFileSystemConfigBuilder) {
                FtpFileSystemConfigBuilder ftpFscb = (FtpFileSystemConfigBuilder) fscb;
                ftpFscb.setUserDirIsRoot(opts, true);

            }
            if (fscb instanceof HttpFileSystemConfigBuilder) {
                HttpFileSystemConfigBuilder httpFscb = (HttpFileSystemConfigBuilder) fscb;
                httpFscb.setProxyHost(opts, "proxy.cls.fr");
                httpFscb.setProxyPort(opts, 8080);

            }
            if (fscb instanceof SftpFileSystemConfigBuilder) {
                SftpFileSystemConfigBuilder sftpFscb = (SftpFileSystemConfigBuilder) fscb;
                // sftpFscb.setUserDirIsRoot(opts, true);

                // TrustEveryoneUserInfo trustEveryoneUserInfo = new TrustEveryoneUserInfo();
                // trustEveryoneUserInfo.promptYesNo("eddfsdfs");
                // sftpFscb.setUserInfo(opts, new TrustEveryoneUserInfo());
                sftpFscb.setTimeout(opts, 5000);
                // SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
                // SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");

            }
            // FileObject fo =
            // fsManager.resolveFile("ftp://ftp.cls.fr/pub/oceano/AVISO/NRT-SLA/maps/rt/j2/h/msla_rt_j2_err_21564.nc.gz",
            // opts);

            // String uri = String.format("%s://%s/%s", scheme, host, file);
            // String uri = String.format("%s://%s/", scheme, host);
            // FileObject originBase = fsManager.resolveFile(uri, opts);
            // fsManager.setBaseFile(originBase);

            File tempDir = new File("c:/tempVFS");
            // File tempFile = File.createTempFile("AsciiEnvisat", ".txt", tempDir);
            File hostFile = new File(file);
            String fileName = hostFile.getName();
            File newFile = new File(tempDir, fileName);
            newFile.createNewFile();

            DefaultFileReplicator dfr = new DefaultFileReplicator(tempDir);
            fsManager.setTemporaryFileStore(dfr);
            // System.out.println(fsManager.getBaseFile());
            // System.out.println(dfr);
            // System.out.println(fsManager.getTemporaryFileStore());

            // FileObject ff = fsManager.resolveFile("sftp://t:t@CLS-EARITH.pc.cls.fr/AsciiEnvisat.txt",
            // opts);
            String uri = String.format("%s://%s/%s", scheme, host, file);
            // FileObject ff2 =
            // fsManager.resolveFile("sftp://atoll:atoll@catsat-data1.cls.fr/home/atoll/atoll-distrib/HOA_Catsat/Interface_ATOLL/nrt_med_infrared_sst_timestamp_FTP_20090516.xml");
            FileObject ff = fsManager.resolveFile(uri, opts);
            FileObject dest = fsManager.toFileObject(newFile);
            // dest.copyFrom(ff2, Selectors.SELECT_ALL);
            dest.copyFrom(ff, Selectors.SELECT_ALL);
            //            
            // URL url = ff.getURL();
            //            
            // url.openConnection();
            // URLConnection conn = url.openConnection();
            // InputStream in = conn.getInputStream();
            // in.close();

            // InputStream in = ff.getContent().getInputStream();

        } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            // fsManager.close();
            // fsManager.freeUnusedResources();
        }

    }

    public static void testPush(String fromUri, String toUri) {

        try {
            Organizer.copyFile(fromUri, toUri);
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.notifyException());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public static void testPush(String fromUri, String toUri, String userFrom, String pwdFrom, String userTo, String pwdTo) {

        try {

            Organizer.copyFile(fromUri, toUri, userFrom, pwdFrom, userTo, pwdTo);
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.notifyException());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
