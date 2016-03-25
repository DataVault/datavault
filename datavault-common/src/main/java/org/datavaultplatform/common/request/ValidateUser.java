package org.datavaultplatform.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

/**
 * User: Robin Taylor
 * Date: 24/03/2016
 * Time: 16:00
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "ValidateUser")
public class ValidateUser {

    @ApiObjectField(description = "Userid")
    private String userid;

    @ApiObjectField(description = "Password")
    private String password;

    public ValidateUser() {}

    public ValidateUser(String userid, String password) {
        this.userid = userid;
        this.password = password;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

