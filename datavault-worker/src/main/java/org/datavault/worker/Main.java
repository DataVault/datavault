package org.datavault.worker;

import org.datavault.worker.queue.Receiver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    private Receiver receiver;

    public static void main(String [] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"config/config.xml"});

        Receiver receiver = context.getBean(Receiver.class);

        // Listen to the message queue ...
        try {
            receiver.receive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
