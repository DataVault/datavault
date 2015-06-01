package org.datavault.worker;

public class Main {
        
    public static void main(String [] args) {

        // Listen to the message queue ...

        try {
            org.datavault.worker.queue.Receiver.receive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
