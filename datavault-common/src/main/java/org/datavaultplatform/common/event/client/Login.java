package org.datavaultplatform.common.event.client;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavaultplatform.common.event.Event;

@Entity
@Table(name="Events")
public class Login extends Event {
       
    Login() {};
    public Login(String remoteAddress, String userAgent) {
        super("Login");
        this.eventClass = Login.class.getCanonicalName();
        this.remoteAddress = remoteAddress;
        this.userAgent = userAgent;
    }
}
