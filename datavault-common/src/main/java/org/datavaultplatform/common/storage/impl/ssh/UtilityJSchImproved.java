package org.datavaultplatform.common.storage.impl.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import java.util.Comparator;
import java.util.Iterator;

public abstract class UtilityJSchImproved {

    public static long calculateSize(final ChannelSftp channel,
                                     String remoteFile) throws SftpException {
        
        long bytes = 0;        
        String pwd = remoteFile;
        
        if (remoteFile.lastIndexOf('/') != -1) {
            if (remoteFile.length() > 1) {
                pwd = remoteFile.substring(0, remoteFile.lastIndexOf('/'));
            }
        }
        
        channel.cd(pwd);
        
        @SuppressWarnings("unchecked") final java.util.Vector<ChannelSftp.LsEntry> files = channel.ls(remoteFile);
        // Sort the entries, we will process directories last.
        files.sort(COMPARATOR);
        Iterator<ChannelSftp.LsEntry> iter = files.iterator();
        //process each entry, removing it from Vector after processing, go to aid GC.
        while(iter.hasNext()) {
            final ChannelSftp.LsEntry le = iter.next();

            // remove items from the Vector as soon as possible
            iter.remove();

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

    public static final Comparator<ChannelSftp.LsEntry> COMPARATOR =
        Comparator.comparing(entry -> entry.getAttrs().isDir());

}
