package org.datavaultplatform.common.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

@Slf4j
public final class MdcUtils {

    public static final String ANONYMOUS = "anonymous";
    public static final String MDC_USER = "user";

    private MdcUtils() {
    }

    public static String getMdcUserName(String username) {
        if (StringUtils.hasText(username)) {
            return username;
        } else {
            return ANONYMOUS;
        }
    }
    
    public static void addUserNameToMdc(String username) {
        MDC.put(MDC_USER, getMdcUserName(username));
        log.info("MDC[user] now [{}]", username);
    }
    
    public static String getMdcUserName() {
        return MDC.get(MDC_USER);
    }

    public static void removeMdcUserName() {
        MDC.remove(MDC_USER);
    }
}
