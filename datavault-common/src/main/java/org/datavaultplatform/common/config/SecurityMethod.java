package org.datavaultplatform.common.config;

import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record SecurityMethod(String method, String arg) {

    public static final  String METHOD_PERMIT_ALL = "permitAll";
    public static final  String METHOD_HAS_ROLE = "hasRole";
    public static final  String METHOD_HAS_AUTHORITY = "hasAuthority";

    public static final Pattern EXPRESSION_PATTERN = Pattern.compile("^(\\w+)\\s*\\('(.*)'\\)$|^(\\w+)\\s*\\((.*)\\)$");

    public SecurityMethod {
        switch (method){
            case METHOD_PERMIT_ALL:
                Assert.isTrue("".equals(arg), "permitAll() method does not accept any arguments");
                break;
            case METHOD_HAS_ROLE:
                Assert.isTrue(!arg.startsWith("ROLE_"), "Role names must NOT start with ROLE_");
                break;
            case METHOD_HAS_AUTHORITY:
                Assert.isTrue(arg.startsWith("ROLE_"), "Authority names must start with ROLE_");
                break;
            default:
                throw new RuntimeException("Unknown security method: " + method());
        }
    }
    
    public static SecurityMethod from(String expression) {
        if (expression == null || expression.isBlank()) {
            return null;
        }

        Matcher matcher = EXPRESSION_PATTERN.matcher(expression.trim());

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid security expression: " + expression);
        }
        // Group 1 & 2 handle the quoted version: hasRole('ADMIN')
        // Group 3 & 4 handle the unquoted version: hasRole(123)
        String method = matcher.group(1) != null ? matcher.group(1) : matcher.group(3);
        String arg = matcher.group(2) != null ? matcher.group(2) : matcher.group(4);
        return new SecurityMethod(method, arg);
    }
    public boolean isPermitAll() {
        return METHOD_PERMIT_ALL.equals(method);
    }
    public boolean isHasRole() {
        return METHOD_HAS_ROLE.equals(method);
    }
    public boolean isHasAuthority() {
        return METHOD_HAS_AUTHORITY.equals(method);
    }
}