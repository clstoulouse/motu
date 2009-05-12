package fr.cls.atoll.motu.library.ftp;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.commons.vfs.operations.FileOperationProvider;
import org.apache.commons.vfs.provider.ftp.FtpClientFactory;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs.provider.sftp.SftpClientFactory;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.log4j.Logger;

import com.jcraft.jsch.Session;

import fr.cls.commons.util.io.ConfigLoader;

public class TestFtp {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(TestFtp.class);
    private static final Log _LOG = LogFactory.getLog(TestFtp.class);

    /**
     * .
     * @param args
     */
    public static void main(String[] args) {

        //testFtp();
        //testSftp();
        testVFS();
    }
    
    public static void testFtp() {
        
        //System.setProperty("http.proxyHost", "proxy.cls.fr"); // adresse IP
        //System.setProperty("http.proxyPort", "8080");
        //System.setProperty("socksProxyHost", "proxy.cls.fr");
        //System.setProperty("socksProxyPort", "1080");
        

        try {
            
//            String user = "anonymous";
//            String pass = "dearith@cls.fr";
//            String server = "ftp.cls.fr";
//
//            FTPClient client = new FTPClient();
//            client.connect(server);
//            System.out.print(client.getReplyString());
//            int reply = client.getReplyCode();
//            if (!FTPReply.isPositiveCompletion(reply))
//            {
//                throw new IllegalArgumentException("cant connect: " + reply);
//            }
//            if (!client.login(user, pass))
//            {
//                throw new IllegalArgumentException("login failed");
//            }
//            client.enterLocalPassiveMode();
//            client.disconnect();
            
            FileSystemOptions opts = new FileSystemOptions();
            //FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
            FileObject fo = VFS.getManager().resolveFile("my/ftp/file", opts);
            
            String server = "proxy.cls.fr";
            String user = "anonymous@ftp.unidata.ucar.edu";
            String pass = "";
            FTPClient ftpClient = FtpClientFactory.createConnection(server, 21, 
                                                                    user.toCharArray(), pass.toCharArray(), "", opts);
        } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
//        } catch (SocketException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
   public static void testSftp() {
        
        

        try {
            
//            String user = "anonymous";
//            String pass = "dearith@cls.fr";
//            String server = "ftp.cls.fr";
//
//            FTPClient client = new FTPClient();
//            client.connect(server);
//            System.out.print(client.getReplyString());
//            int reply = client.getReplyCode();
//            if (!FTPReply.isPositiveCompletion(reply))
//            {
//                throw new IllegalArgumentException("cant connect: " + reply);
//            }
//            if (!client.login(user, pass))
//            {
//                throw new IllegalArgumentException("login failed");
//            }
//            client.enterLocalPassiveMode();
//            client.disconnect();
            
            FileSystemOptions opts = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
//            SftpFileSystemConfigBuilder.getInstance().setProxyHost(opts, "proxy.cls.fr");           
//            SftpFileSystemConfigBuilder.getInstance().setProxyPort(opts, 8080);
//            SftpFileSystemConfigBuilder.getInstance().setProxyType(opts, SftpFileSystemConfigBuilder.PROXY_SOCKS5 );
//            //SftpFileSystemConfigBuilder.getInstance().setProxyType(opts, SftpFileSystemConfigBuilder.PROXY_HTTP );
            FileObject fo = VFS.getManager().resolveFile("sftp://t:t@CLS-EARITH.pc.cls.fr/AsciiEnvisat.txt", opts);
            String server = "CLS-EARITH.pc.cls.fr";
            String user = "t";
            String pass = "t";
            
            server = "aviso-motu.cls.fr";
            user = "mapserv";
            pass = "mapserv";
            Session sftpClient = SftpClientFactory.createConnection(server, 22, 
                                                                    user.toCharArray(), pass.toCharArray(), opts);
        } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
//        } catch (SocketException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
   
   public static void testVFS() {
       StandardFileSystemManager fsManager = null;
       
       try {
           fsManager = new StandardFileSystemManager();
           fsManager.setLogger(_LOG);

           fsManager.setConfiguration(ConfigLoader.getInstance().get("testVFS.xml"));
           fsManager.setCacheStrategy(CacheStrategy.ON_CALL);
           //fsManager.addProvider("moi", new DefaultLocalFileProvider());
           fsManager.init();
           
           
           FileOperationProvider[] fops = fsManager.getOperationProviders("sftp");
           
           FileSystemOptions opts = new FileSystemOptions();
           SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
           SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");

           FileObject originBase = fsManager.resolveFile("sftp://t:t@CLS-EARITH.pc.cls.fr/", opts);
           fsManager.setBaseFile(originBase);
           
           File tempDir = new File("c:/tempVFS");
           File tempFile = File.createTempFile("AsciiEnvisat", ".txt", tempDir);
           
           DefaultFileReplicator dfr = new DefaultFileReplicator(tempDir);
           fsManager.setTemporaryFileStore(dfr);
           System.out.println(fsManager.getBaseFile());
           System.out.println(dfr);
           System.out.println(fsManager.getTemporaryFileStore());
           
           FileObject ff = fsManager.resolveFile("sftp://t:t@CLS-EARITH.pc.cls.fr/AsciiEnvisat.txt", opts);
           FileObject dest = fsManager.toFileObject(tempFile);
           dest.copyFrom(ff, Selectors.SELECT_ALL);
//           open = true;
//
//           if (tmpFilesManager != null) {
//               tmpFilesManager.open();
//           }
//
           //manageConnection(true);
           
       } catch (FileSystemException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } finally {
        fsManager.close();
    }


   }
}
