package org.datavaultplatform.worker.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class SocketUtils {

    public static boolean isServerListening(String host, int port) {
        Socket s = null;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 30_000);
            log.info("Socket Connect to socket [{}/{}] SUCCESS", host, port);
            return true;
        } catch (Exception ex) {
            log.error("Socket Connect to socket [{}/{}] FAILED", host, port);
            return false;
        } finally {
            IOUtils.closeQuietly(s);
        }
    }
}
