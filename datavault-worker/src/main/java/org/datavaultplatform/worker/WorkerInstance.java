package org.datavaultplatform.worker;

import org.datavaultplatform.worker.queue.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class WorkerInstance {

    public static void main(String [] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"datavault-worker.xml"});

        EventSender eventSender = context.getBean(EventSender.class);
        Receiver receiver = context.getBean(Receiver.class);

        // Listen to the message queue ...
        try {
            receiver.receive(eventSender);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
