package org.datavaultplatform.common.storage.impl;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;


@SuppressWarnings("SameReturnValue")
public class OsCommandLineBufferingPrefixGenerator {

    public static final Logger LOG = LoggerFactory.getLogger(OsCommandLineBufferingPrefixGenerator.class);
    public static final String COMMAND_STDBUF = "stdBuf";
    public static final String COMMAND_SCRIPT = "script";

    public static boolean isCommandOnPath(String command) {
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

    public boolean isOnPath(String command) {
        return isCommandOnPath(command);
    }

    /*
    Forces line-buffered output instead of block-buffered - helps us get output from dsmc on a line-by-line basis - not wait until 4K buffer is full
     */
    public List<String> generate() {
        List<String> result;
        if (isLinux()) {
            if (isOnPath(COMMAND_STDBUF)) {
                result = getStdBufPrefix();
            } else if (isOnPath(COMMAND_SCRIPT)) {
                result = getScriptPrefix();
            } else {
                LOG.warn("CANNOT FIND [{}] or [{}]on Linux Path", COMMAND_SCRIPT, COMMAND_STDBUF);
                return Collections.emptyList();
            }
        } else if (isMacOs()) {
            if (isOnPath(COMMAND_SCRIPT)) {
                result = getScriptPrefix();
            } else {
                result = Collections.emptyList();
                LOG.warn("CANNOT FIND [{}] on MacOs Path", COMMAND_SCRIPT);
            }
        } else {
            LOG.warn("OS is neither Linux nor MacOs");
            result = Collections.emptyList();
        }
        return result;
    }

    List<String> getScriptPrefix() {
        return List.of(COMMAND_SCRIPT, "-q", "/dev/null");
    }

    List<String> getStdBufPrefix() {
        return List.of(COMMAND_STDBUF, "-oL");
    }

    public boolean isMacOs() {
        return SystemUtils.IS_OS_MAC;
    }

    public boolean isLinux() {
        return SystemUtils.IS_OS_LINUX;
    }
}
