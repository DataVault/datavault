package org.datavaultplatform.worker.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.utils.SocketUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
public class RabbitMessageSelector implements DisposableBean, ApplicationContextAware {

    private final DefaultMessagePropertiesConverter converter = new DefaultMessagePropertiesConverter();

    private final ConnectionFactory connectionFactory;
    private final RabbitMessageProcessor processor;
    private final String hiPriorityQueueName;
    private final String loPriorityQueueName;
    private final AtomicBoolean ready = new AtomicBoolean(false);

    private ApplicationContext ctx;
    private Connection connection;

    public RabbitMessageSelector(String hiPriorityQueueName, String loPriorityQueueName, ConnectionFactory connectionFactory, RabbitMessageProcessor processor) {
        this.hiPriorityQueueName = hiPriorityQueueName;
        this.loPriorityQueueName = loPriorityQueueName;
        this.connectionFactory = connectionFactory;
        this.processor = processor;
    }
    
    public static <T> Optional<T> getFirst(Supplier<Optional<T>> hi, Supplier<Optional<T>> lo) {
        return Stream.of(hi, lo).map(Supplier::get).flatMap(Optional::stream).findFirst();
    }
    
    public synchronized void selectAndProcessNextMessage() throws Exception {
        if (!ready.get()) {
            return;
        }
        SocketUtils.isServerListening(connectionFactory.getHost(), connectionFactory.getPort());
        log.info("Waiting for RabbitMQ connection [{}/{}]", connectionFactory.getHost(), connectionFactory.getPort());
        this.connection = connectionFactory.newConnection();
        try {
            selectAndProcessNextMessageWithConnection();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void selectAndProcessNextMessageWithConnection() {
        Supplier<Optional<RabbitMessageInfo>> pollHiPriority = () -> pollRabbit(true, this::createChannel, hiPriorityQueueName);
        Supplier<Optional<RabbitMessageInfo>> pollLoPriority = () -> pollRabbit(false, this::createChannel, loPriorityQueueName);

        // it only looks on loPriorityQueue is no messages on hiPriorityQueue
        Optional<RabbitMessageInfo> selected = getFirst(pollHiPriority, pollLoPriority);

        // max 1 selected message
        selected.ifPresent(messageinfo -> {
            try {
                // process the selected message
                processor.onMessage(messageinfo);
                // ack the selected message
                messageinfo.acknowledge();
            } finally {
                messageinfo.closeChannel();
            }
        });
    }

    private Optional<RabbitMessageInfo> pollRabbit(boolean isHiPriority, Supplier<Channel> channelSupplier, String queueName) {
        try {
            Channel channel = channelSupplier.get();
            GetResponse pollResult = channel.basicGet(queueName, false);
            if (pollResult == null) {
                channel.close();
                return Optional.empty();
            } else {
                MessageProperties messageProperties = converter.toMessageProperties(pollResult.getProps(), pollResult.getEnvelope(), StandardCharsets.UTF_8.name());
                Message message = new Message(pollResult.getBody(), messageProperties);
                long deliveryTag = pollResult.getEnvelope().getDeliveryTag();
                RabbitMessageInfo rabbitMessageInfo = new RabbitMessageInfo(isHiPriority, message, queueName, channel, deliveryTag);
                return Optional.of(rabbitMessageInfo);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @PostConstruct
    public void init() {
        if (ctx == null) {
            log.warn("No Application Context Set!");
            return;
        }
        String appName = ctx.getEnvironment().getProperty("spring.application.name","spring.application.name not set!");
        log.info("Worker [{}] Restart Queue [{}]", appName, this.hiPriorityQueueName);
        log.info("Worker [{}] Worker  Queue [{}]", appName, this.loPriorityQueueName);
    }
    
    @SneakyThrows
    protected Channel createChannel() {
        Channel channel = connection.createChannel();
        channel.basicQos(1);
        long num = channel.getChannelNumber();
        channel.addShutdownListener(cause -> log.trace("The channel [{}] has been shutdown [{}]", num, cause.getMessage()));
        return channel;
    }

    @Override
    public synchronized void destroy() throws Exception {
        Connection temp = this.connection;
        this.connection = null;
        if (temp == null) {
            return;
        }
        log.info("CLOSING RABBITMQ CONNECTION [{}]", temp);
        temp.close();
    }

    @EventListener(ApplicationReadyEvent.class)
    public synchronized void onReady(ApplicationReadyEvent event) {
        log.info("ready took[{}]", event.getTimeTaken());
        this.ready.set(true);
    }

    public boolean isReady() {
        return this.ready.get();
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }
}
