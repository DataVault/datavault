package org.datavaultplatform.common.event.client;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class Login extends Event {

    public Login() {
    }
    public Login(String remoteAddress, String userAgent) {
        super("Login");
        this.eventClass = Login.class.getCanonicalName();
        this.remoteAddress = remoteAddress;
        this.userAgent = userAgent;
    }
}
