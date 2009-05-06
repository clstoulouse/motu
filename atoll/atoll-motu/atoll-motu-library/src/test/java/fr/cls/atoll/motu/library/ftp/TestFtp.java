package fr.cls.atoll.motu.library.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.ftp.FtpClientFactory;
import org.apache.commons.vfs.provider.ftp.FtpFileSystemConfigBuilder;

public class TestFtp {

    /**
     * .
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        testFtp();
    }
    
    public static void testFtp() {
        String userName = "";
        String pwd = "";
        try {
            FileSystemOptions opts = new FileSystemOptions();
            FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
            //VFS.getManager().resolveFile("my/ftp/file", opts);

            
            FTPClient ftpClient = FtpClientFactory.createConnection("ftp.cls.fr", 21, 
                                                                    null, null, "/", opts);
        } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
