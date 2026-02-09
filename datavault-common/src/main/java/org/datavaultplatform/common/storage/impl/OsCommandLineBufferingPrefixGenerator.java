package org.datavaultplatform.common.storage.impl;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("SameReturnValue")
public class OsCommandLineBufferingPrefixGenerator {
    
    public static final Logger LOG = LoggerFactory.getLogger(OsCommandLineBufferingPrefixGenerator.class);
    public static final String LINUX_STDBUF = "stdBuf";
    public static final String MACOS_SCRIPT = "script";

    public boolean isOnPath(String command) {
        String path = System.getenv("PATH");
        if (path == null || path.isEmpty()) {
            return false;
        }

        String[] dirs = path.split(File.pathSeparator);
        for (String dir : dirs) {
            File f = new File(dir, command);
            if (f.canExecute()) {
                return true;
            }
        }
        return false;
    }

    /*
    Forces line-buffered output instead of block-buffered - helps us get output from dsmc on a line-by-line basis - not wait until 4K buffer is full
     */
    public List<String> generate() {
        List<String> result = new ArrayList<>();
        if (isLinux()) {
            if (isOnPath(LINUX_STDBUF)) {
                result.addAll(List.of(LINUX_STDBUF, "-oL"));
            } else {
                LOG.warn("CANNOT FIND {} on Linux Path", LINUX_STDBUF);
            }
        } else if (isMacOs()) {
            if (isOnPath(MACOS_SCRIPT)) {
                result.addAll(List.of(MACOS_SCRIPT, "-q", "/dev/null"));
            } else {
                LOG.warn("CANNOT FIND {} on MacOs Path", MACOS_SCRIPT);
            }
        } else {
            LOG.warn("OS is neither Linux nor MacOs");
        }
        return result;
    }

    public boolean isMacOs() {
        return SystemUtils.IS_OS_MAC;
    }

    public boolean isLinux() {
        return SystemUtils.IS_OS_LINUX;
    }
}
