package org.datavault.queue;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class EventListener implements MessageListener {

    @Override
    public void onMessage(Message msg) {
        System.out.println("[x] Received '" + new String(msg.getBody()) + "'");
    }
}
