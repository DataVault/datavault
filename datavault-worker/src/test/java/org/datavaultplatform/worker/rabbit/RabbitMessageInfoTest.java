package org.datavaultplatform.worker.rabbit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMessageInfoTest {

    @Test
    void testHiPriorityMessagesComeFirst() {
        RabbitMessageInfo lo = new RabbitMessageInfo(false, new Message("lo".getBytes(StandardCharsets.UTF_8)), "queueHi");
        RabbitMessageInfo hi = new RabbitMessageInfo(true, new Message("hi".getBytes(StandardCharsets.UTF_8)), "queueLo");
        PriorityQueue<RabbitMessageInfo> queue = new PriorityQueue<>();
        queue.add(lo);
        queue.add(hi);
        assertThat(queue.poll()).isEqualTo(hi);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testWhenBothSamePriorityEarliestMessageComesFirst(boolean hiPriority) {
        Instant now = Instant.now();
        Instant plus1hour = now.plusSeconds(TimeUnit.HOURS.toSeconds(1));

        MessageProperties mp1 = new MessageProperties();
        mp1.setTimestamp(Date.from(now));

        MessageProperties mp2 = new MessageProperties();
        mp2.setTimestamp(Date.from(plus1hour));

        Message m1 = MessageBuilder.withBody("m1".getBytes(StandardCharsets.UTF_8)).setTimestamp(Date.from(now)).build();
        Message m2 = MessageBuilder.withBody("m2".getBytes(StandardCharsets.UTF_8)).setTimestamp(Date.from(plus1hour)).build();

        RabbitMessageInfo wm1 = new RabbitMessageInfo(hiPriority, m1, "queueHi");
        RabbitMessageInfo wm2 = new RabbitMessageInfo(hiPriority, m2, "queueLo");
        PriorityQueue<RabbitMessageInfo> queue = new PriorityQueue<>();
        queue.add(wm2);
        queue.add(wm1);
        assertThat(queue.poll()).isEqualTo(wm1);
    }

    @Test
    void testWhenBothSamePriorityAndTimestampLowestMessageIdComesFirst() {
        Instant now = Instant.now();

        MessageProperties mp1 = new MessageProperties();
        mp1.setTimestamp(Date.from(now));

        MessageProperties mp2 = new MessageProperties();
        mp2.setTimestamp(Date.from(now));

        Message m1 = MessageBuilder.withBody("m1".getBytes(StandardCharsets.UTF_8)).setTimestamp(Date.from(now)).setMessageId("1111").build();
        Message m2 = MessageBuilder.withBody("m2".getBytes(StandardCharsets.UTF_8)).setTimestamp(Date.from(now)).setMessageId("2222").build();

        RabbitMessageInfo wm1 = new RabbitMessageInfo(true, m1, "queueHi");
        RabbitMessageInfo wm2 = new RabbitMessageInfo(true, m2, "queueLo");
        PriorityQueue<RabbitMessageInfo> queue = new PriorityQueue<>();
        queue.add(wm2);
        queue.add(wm1);
        assertThat(queue.poll()).isEqualTo(wm1);
    }
}