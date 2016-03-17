package org.datavaultplatform.common.event.client;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavaultplatform.common.event.Event;

@Entity
@Table(name="Events")
public class Logout extends Event {
    
    String remoteAddress;
    
    Logout() {};
    public Logout(String remoteAddress) {
        super("Logout");
        this.eventClass = Logout.class.getCanonicalName();
        this.remoteAddress = remoteAddress;
    }
}
