package org.datavault.worker;

import org.datavault.worker.queue.Receiver;
import org.datavault.worker.queue.EventSender;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class WorkerInstance {

    private Receiver receiver;
    private EventSender eventSender;

    public static void main(String [] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"config.xml"});

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
