package org.datavaultplatform.common.storage.impl.ssh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClient.Attributes;
import org.apache.sshd.sftp.client.SftpClient.DirEntry;
import org.apache.sshd.sftp.common.SftpConstants;
import org.apache.sshd.sftp.common.SftpException;
import org.datavaultplatform.common.io.InputStreamAdapter;
import org.datavaultplatform.common.io.OutputStreamAdapter;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.impl.SFTPConnection;
import org.springframework.util.Assert;

@Slf4j
public abstract class UtilitySSHD {

    public static final int BUFFER_SIZE = 1024  * 64;
    @SuppressWarnings("OctalInteger")
    private static final int DEFAULT_DIR_MODE = 0755;
    @SuppressWarnings("OctalInteger")
    public static final int FILE_PERMISSION_MASK = 0777;

    public static long calculateSize(final SFTPConnection con,
                                     String remoteFile) throws IOException {
        long bytes = 0;
        String pwd = remoteFile;
        
        if (remoteFile.lastIndexOf('/') != -1) {
            if (remoteFile.length() > 1) {
                pwd = remoteFile.substring(0, remoteFile.lastIndexOf('/'));
            }
        }

        String fullPath = con.getFullPath(pwd);

        for (SftpClient.DirEntry entry : con.sftpClient.readDir(fullPath)) {

            if (entry.getFilename().equals(".") ||
                entry.getFilename().equals("..")) {
                continue;
            }

            if (entry.getAttributes().isDirectory()) {
                bytes += calculateSize(con, fullPath + "/" + entry.getFilename());
            } else {
                bytes += entry.getAttributes().getSize();
            }
        }
        return bytes;
    }
    
    public static class SFTPMonitorSSHD {
        
        private final Progress progressTracker;
        private final Clock clock;

        private final boolean monitoring;

        public SFTPMonitorSSHD(Progress progressTracker, Clock clock, boolean monitoring) {
            Assert.isTrue(clock != null, "The clock cannot be null");
            Assert.isTrue(progressTracker != null, "The proressTracker cannot be null");
            this.progressTracker = progressTracker;
            this.clock = clock;
            this.monitoring = monitoring;
        }
        public OutputStream monitorOutputStream(OutputStream os) {
            if (monitoring) {
                return wrapOutputStream(os);
            } else {
                return os;
            }
        }

        private OutputStream wrapOutputStream(OutputStream os){

                return new OutputStreamAdapter(os) {

                  private void incBytesWritten(int bytesWritten) {
                    progressTracker.incByteCount(bytesWritten);
                    progressTracker.setTimestamp(clock.millis());
                  }

                  @Override
                  public void write(byte[] data, int start, int len) throws IOException {
                      super.write(data, start, len);
                      incBytesWritten(len);
                  }
                  @Override
                  public void write(byte[] data) throws IOException {
                    super.write(data);
                    incBytesWritten(data.length);
                  }

                  /*
                  * Note : this is for writing a SINGLE byte
                  */
                  @Override
                  public void write(int data) throws IOException {
                    super.write(data);
                    incBytesWritten(1);
                  }
                  @Override
                  public void close() throws IOException {
                      super.close();
                      progressTracker.setTimestamp(clock.millis());
                  }
                };
        }
        public InputStream monitorInputStream(InputStream is) {
            if (monitoring) {
                return wrapInputStream(is);
            } else {
                return is;
            }
        }
        private InputStream wrapInputStream(InputStream is){
            return new InputStreamAdapter(is) {

                private int incBytesRead(int bytesRead) {
                    if(bytesRead > -1) {
                        progressTracker.incByteCount(bytesRead);
                        progressTracker.setTimestamp(clock.millis());
                    }
                    return bytesRead;
                }

                @Override
                public int read(byte[] data, int start, int len) throws IOException {
                    return incBytesRead(super.read(data, start, len));
                }
                @Override
                public int read(byte[] target) throws IOException {
                    return incBytesRead(super.read(target));
                }

                /*
                The other 'read' methods return the number of bytes read, this method
                return the single byte of data that's been read.
                 */
                @Override
                public int read() throws IOException {
                    int data = super.read();
                    if( data > -1 ) {
                        incBytesRead(1);
                    }
                    return data;
                }
                @Override
                public void close() throws IOException {
                    super.close();
                    progressTracker.setTimestamp(clock.millis());
                }
            };
        }

        public boolean isMonitoring() {
            return monitoring;
        }

        public Progress getProgress() {
            return progressTracker;
        }
    }
    
    public static void getDir(final SftpClient sftpClient,
                               final Path remoteDirOrFile,
                               final File localStorageDir,
                               SFTPMonitorSSHD monitor) throws IOException {

        Attributes remoteAttrs = sftpClient.stat(remoteDirOrFile.toString());

        //if we are getting directory, gotta make sure the local directory exists
        if (remoteAttrs.isDirectory()) {
            if (!localStorageDir.exists()) {
                localStorageDir.mkdirs();
            } else {
                // you cannot pull remote directory back to a non-directory
                Assert.isTrue(localStorageDir.isDirectory(), String.format("You cannot retrieve remote directory [%s] back to a non-directory[%s]", remoteDirOrFile, localStorageDir));
            }
        }

        Path remoteDir = remoteAttrs.isDirectory() ? remoteDirOrFile : remoteDirOrFile.getParent();
        for (DirEntry dirEntry :  sftpClient.readDir(remoteDir.toString())) {
            final String name = dirEntry.getFilename();
            if (dirEntry.getAttributes().isDirectory()) {
                if (name.equals(".") || name.equals("..")) {
                    continue;
                }
                Path remoteDirPath = remoteDir.resolve(name);
                File nestedLocalDir = new File(localStorageDir, name);
                getDir(sftpClient,
                       remoteDirPath,
                       nestedLocalDir,
                       monitor);
            } else {
                Path remoteFilePath = remoteDir.resolve(name);
                getFile(sftpClient,
                        remoteFilePath,
                        localStorageDir,
                        monitor);
            }
        }
     }

    private static void getFile(final SftpClient sftpClient,
                                final Path remoteFilePath,
                                File localFileOrDirectory,
                                SFTPMonitorSSHD monitor) throws IOException {

        if (!localFileOrDirectory.exists()) {
            if (localFileOrDirectory.isDirectory()) {
                localFileOrDirectory.mkdirs();
            } else {
                localFileOrDirectory.getParentFile().mkdirs();
            }
        }

        final File localFile;
        if (localFileOrDirectory.isDirectory()) {
            localFile = new File(localFileOrDirectory, remoteFilePath.getFileName().toString());
        } else {
            localFile = localFileOrDirectory;
        }

        Attributes attrs = sftpClient.stat(remoteFilePath.toString());
        Assert.isTrue(attrs.isRegularFile(), String.format("Remote file [%s] is not a regular file", remoteFilePath));
        final long remoteFileSize = attrs.getSize();

        try (OutputStream os = Files.newOutputStream(localFile.toPath())) {
            try (InputStream inputStream = monitor.monitorInputStream(sftpClient.read(remoteFilePath.toString(), BUFFER_SIZE))) {
                long copied = transfer(inputStream, os);
                if (remoteFileSize != copied) {
                    log.error("expected to recv [{}] bytes but recvd [{}]bytes", remoteFileSize, copied);
                }
            }
        }
    }

    @SneakyThrows
    private static long transfer(InputStream inputStream, OutputStream outputStream) {
        byte[] buffer = new byte[BUFFER_SIZE];
        return IOUtils.copyLarge(inputStream, outputStream, buffer);
    }

    public static void send(final SftpClient sftpClient,
        final File localFileOrDirectoryToSend,
        final Path sftpDestDirPath,
        SFTPMonitorSSHD monitor)
        throws IOException {

        Assert.isTrue(existsOnSftpServer(sftpClient, sftpDestDirPath), () -> "sftpDestDirPath does not exist: " + sftpDestDirPath);

        if (localFileOrDirectoryToSend.isDirectory()) {
            //noinspection UnnecessaryLocalVariable
            File localDirectory = localFileOrDirectoryToSend;
            sendDirectory(sftpClient, localDirectory, sftpDestDirPath, monitor);
        } else {
            //noinspection UnnecessaryLocalVariable
            File localFile = localFileOrDirectoryToSend;
            Path fullPathToFileOnSftpServer = sftpDestDirPath.resolve(localFile.getName());
            transferToSftpServer(sftpClient, localFile, fullPathToFileOnSftpServer, monitor);
        }
    }

    public static void sendDirectory(final SftpClient sftpClient,
                                     final File localDir,
                                     final Path sftpDestDirPath,
        SFTPMonitorSSHD monitor)
        throws IOException {

        Assert.isTrue(UtilitySSHD.existsOnSftpServer(sftpClient, sftpDestDirPath), () -> "sftpDestDirPath does not exist: " + sftpDestDirPath);

        Path localDirPath = localDir.toPath();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(localDirPath)) {

            for (Path localPathEntry : stream) {

                File localPathFile = localPathEntry.toFile();

                if (localPathFile.isDirectory()) {
                    //noinspection UnnecessaryLocalVariable
                    File localPathDir = localPathFile;
                    Path nestedSftpDestDirPath = sftpDestDirPath.resolve(localPathDir.getName());
                    sendDirectoryToRemote(sftpClient, localPathDir, nestedSftpDestDirPath, monitor);
                } else {
                    Path fullPathToDestinationFileOnSftpServer = sftpDestDirPath.resolve(
                        localPathFile.getName());
                    transferToSftpServer(sftpClient, localPathFile,
                        fullPathToDestinationFileOnSftpServer, monitor);
                }
            }
        }
    }
    

    private static void sendDirectoryToRemote(final SftpClient sftpClient,
                                              final File localDirPath,
                                              final Path sftpDestDirPath,
                                              final SFTPMonitorSSHD monitor)
        throws IOException {

        if (!existsOnSftpServer(sftpClient, sftpDestDirPath)) {
            createDir(sftpClient, sftpDestDirPath);
        }
        sendDirectory(sftpClient, localDirPath, sftpDestDirPath, monitor);
    }

    private static void transferToSftpServer(final SftpClient sftpClient,
                                         final File fromFileOnLocal,
                                         final Path fullPathToDestinationFileOnSftpServer,
                                         SFTPMonitorSSHD monitor)
        throws IOException {

        final Path destinationDirOnSftpServer = fullPathToDestinationFileOnSftpServer.getParent();
        Assert.isTrue(existsOnSftpServer(sftpClient,destinationDirOnSftpServer), () -> "The directory " + destinationDirOnSftpServer + " does not exist");
        
        try (OutputStream os = monitor.monitorOutputStream(sftpClient.write(fullPathToDestinationFileOnSftpServer.toString(), BUFFER_SIZE))) {
            try (InputStream is = Files.newInputStream(fromFileOnLocal.toPath())) {
                long copied = transfer(is, os);
                if (fromFileOnLocal.length() != copied) {
                    log.error("expected to send [{}] bytes but sent [{}]bytes",
                        fromFileOnLocal.length(),
                        copied);
                }
            }
        }
        setPermissionAttributes(sftpClient, fullPathToDestinationFileOnSftpServer);
    }

    @SneakyThrows
    private static boolean existsOnSftpServer(SftpClient sftpClient, Path sftpPath) {
        try {
            sftpClient.stat(sftpPath.toString());
            return true;
        } catch (SftpException ex) {
            if (ex.getStatus() == SftpConstants.SSH_FX_NO_SUCH_FILE) {
                return false;
            } else {
                throw ex;
            }
        }
    }

    @SneakyThrows
    public static void createDir(SftpClient sftpClient, Path sftpPath) {
        if(!existsOnSftpServer(sftpClient, sftpPath)){
            String path = sftpPath.normalize().toString();
            sftpClient.mkdir(path);
        }
        setPermissionAttributes(sftpClient, sftpPath);
    }

    @SneakyThrows
    public static void setPermissionAttributes(SftpClient sftpClient, Path sftpServerPath) {
       Assert.isTrue(existsOnSftpServer(sftpClient, sftpServerPath), () -> "The path " + sftpServerPath + " does not exist");
       Attributes attrs = new Attributes();
       attrs.setPermissions(DEFAULT_DIR_MODE);
       sftpClient.setStat(sftpServerPath.toString(), attrs);
       Attributes actual = sftpClient.stat(sftpServerPath.toString());
       boolean isDir = actual.isDirectory();
       log.info("permissions[{}]isDir[{}]path[{}]", Integer.toOctalString(actual.getPermissions()), isDir, sftpServerPath);
       int filePerms = actual.getPermissions() & FILE_PERMISSION_MASK;
       Assert.isTrue(filePerms == DEFAULT_DIR_MODE, () -> "The permissions on the path " + sftpServerPath + "[" + filePerms + "] are not " + DEFAULT_DIR_MODE);
    }

}
