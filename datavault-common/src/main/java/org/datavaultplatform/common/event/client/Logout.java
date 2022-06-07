package org.datavaultplatform.common.event.client;

import javax.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class Logout extends Event {
    
    public Logout() {
    }
    public Logout(String remoteAddress) {
        super("Logout");
        this.eventClass = Logout.class.getCanonicalName();
        this.remoteAddress = remoteAddress;
    }
}
