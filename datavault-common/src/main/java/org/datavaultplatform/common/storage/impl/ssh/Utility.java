/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

//  Adapted from org.apache.tools.ant.taskdefs.optional.ssh

package org.datavaultplatform.common.storage.impl.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.File;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import org.datavaultplatform.common.io.Progress;

public class Utility {

    private static final int DEFAULT_DIR_MODE = 0755;
    
    public static long calculateSize(final ChannelSftp channel,
                                     String remoteFile) throws IOException, SftpException {
        
        long bytes = 0;        
        String pwd = remoteFile;
        
        if (remoteFile.lastIndexOf('/') != -1) {
            if (remoteFile.length() > 1) {
                pwd = remoteFile.substring(0, remoteFile.lastIndexOf('/'));
            }
        }
        
        channel.cd(pwd);
        
        final java.util.Vector files = channel.ls(remoteFile);
        final int size = files.size();
        for (int i = 0; i < size; i++) {
            final ChannelSftp.LsEntry le = (ChannelSftp.LsEntry) files.elementAt(i);
            final String name = le.getFilename();
            if (le.getAttrs().isDir()) {
                if (name.equals(".") || name.equals("..")) {
                    continue;
                }
                bytes += calculateSize(channel, channel.pwd() + "/" + name + "/");
            } else {
                bytes += le.getAttrs().getSize();
            }
        }
        
        channel.cd("..");
        return bytes;
    }
    
    public static class SFTPMonitor implements SftpProgressMonitor {
        
        private final Progress progressTracker;
        
        public SFTPMonitor(Progress progressTracker) {
            this.progressTracker = progressTracker;
        }
        
        @Override
        public void init(final int op, final String src, final String dest, final long max) {
            
        }
        
        @Override
        public boolean count(final long len) {
            
            // Inform the generic progress tracker
            progressTracker.byteCount += len;
            progressTracker.timestamp = System.currentTimeMillis();
            
            return true;
        }
        
        @Override
        public void end() {
        
        }
    }
    
    public static void getDir(final ChannelSftp channel,
                               String remoteFile,
                               final File localFile,
                               final SftpATTRS attrs,
                               SFTPMonitor monitor) throws IOException, SftpException {

        String pwd = remoteFile;

        if (remoteFile.lastIndexOf('/') != -1) {
            if (remoteFile.length() > 1) {
                pwd = remoteFile.substring(0, remoteFile.lastIndexOf('/'));
            }
        }
        
        channel.cd(pwd);
        
        if (attrs.isDir()) {
            if (!localFile.exists()) {
                localFile.mkdirs();
            }
        }
        
        final java.util.Vector files = channel.ls(remoteFile);
        final int size = files.size();
        for (int i = 0; i < size; i++) {
            final ChannelSftp.LsEntry le = (ChannelSftp.LsEntry) files.elementAt(i);
            final String name = le.getFilename();
            if (le.getAttrs().isDir()) {
                if (name.equals(".") || name.equals("..")) {
                    continue;
                }
                getDir(channel,
                       channel.pwd() + "/" + name + "/",
                       new File(localFile, le.getFilename()),
                       le.getAttrs(),
                       monitor);
            } else {
                getFile(channel, le, localFile, monitor);
            }
        }
        channel.cd("..");
    }

    private static void getFile(final ChannelSftp channel,
                                final ChannelSftp.LsEntry le,
                                File localFile,
                                SFTPMonitor monitor) throws IOException, SftpException {
        final String remoteFile = le.getFilename();
        if (!localFile.exists()) {
            final String path = localFile.getAbsolutePath();
            final int i = path.lastIndexOf(File.pathSeparator);
            if (i != -1) {
                if (path.length() > File.pathSeparator.length()) {
                    new File(path.substring(0, i)).mkdirs();
                }
            }
        }
        
        if (localFile.isDirectory()) {
            localFile = new File(localFile, remoteFile);
        }
        
        // System.out.println("Receiving: " + remoteFile + " : " + le.getAttrs().getSize());
        channel.get(remoteFile, localFile.getAbsolutePath(), monitor);
        
        /*
        if (getPreserveLastModified()) {
            FileUtils.getFileUtils().setFileLastModified(localFile,
                                                         ((long) le.getAttrs()
                                                          .getMTime())
                                                         * 1000);
        }
        */
    }
    
    public static void sendDirectory(final ChannelSftp channel,
                                     final Path current,
                                     SFTPMonitor monitor)
        throws IOException, SftpException {
        
        DirectoryStream<Path> stream = Files.newDirectoryStream(current);
        
        for (Path entry : stream) {
            
            File entryFile = entry.toFile();
            
            if (entryFile.isDirectory()) {
                sendDirectoryToRemote(channel, entry, monitor);
            } else {
                sendFileToRemote(channel, entryFile, null, monitor);
            }
        }
    }
    
    public static void send(final ChannelSftp channel,
            final File current,
            SFTPMonitor monitor)
        throws IOException, SftpException {
        if (current.isDirectory()) {
            sendDirectory(channel, current.toPath(), monitor);
        }
        else{
            sendFileToRemote(channel, current, null, monitor);
        }
    }
    
    private static void sendDirectoryToRemote(final ChannelSftp channel,
                                              final Path directory,
                                              SFTPMonitor monitor)
        throws IOException, SftpException {
        final String dir = directory.toFile().getName();
        try {
            channel.stat(dir);
        } catch (final SftpException e) {
            // dir does not exist.
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                channel.mkdir(dir);
                channel.chmod(DEFAULT_DIR_MODE, dir);
            }
        }
        channel.cd(dir);
        sendDirectory(channel, directory, monitor);
        channel.cd("..");
    }

    private static void sendFileToRemote(final ChannelSftp channel,
                                         final File localFile,
                                         String remotePath,
                                         SFTPMonitor monitor)
        throws IOException, SftpException {
        
        if (remotePath == null) {
            remotePath = localFile.getName();
        }

        channel.put(localFile.getAbsolutePath(), remotePath, monitor);
        channel.chmod(DEFAULT_DIR_MODE, remotePath);
    }
}
