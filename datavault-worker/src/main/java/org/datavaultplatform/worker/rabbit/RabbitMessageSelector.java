package org.datavaultplatform.worker.rabbit;

import com.rabbitmq.client.*;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.TraceUtils;
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
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;


@Slf4j
public class RabbitMessageSelector implements DisposableBean, ApplicationContextAware {

    public static final Set<String> EXPECTED_KEYS = Set.of(TraceUtils.TRACE_PARENT, TraceUtils.TRACE_STATE);
    
    public static final Propagator.Getter<Message> GETTER = (Message carrier, String key) -> {
        Assert.isTrue(EXPECTED_KEYS.contains(key), "unexpected key [%s]".formatted(key));
        Object header = carrier.getMessageProperties().getHeader(key);
        String result = header != null ? header.toString() : null;
        log.info("trace header[{}]: {}", key, result);
        return result;
    };

    private final DefaultMessagePropertiesConverter converter = new DefaultMessagePropertiesConverter();

    private final ConnectionFactory connectionFactory;
    private final RabbitMessageProcessor processor;
    private final String hiPriorityQueueName;
    private final String loPriorityQueueName;
    private final AtomicBoolean ready = new AtomicBoolean(false);
    private final Tracer tracer;
    private final Propagator propagator;

    private ApplicationContext ctx;
    private Connection connection;

    public RabbitMessageSelector(String hiPriorityQueueName, String loPriorityQueueName, ConnectionFactory connectionFactory, RabbitMessageProcessor processor, Tracer tracer, Propagator propagator) {
        this.hiPriorityQueueName = hiPriorityQueueName;
        this.loPriorityQueueName = loPriorityQueueName;
        this.connectionFactory = connectionFactory;
        this.processor = processor;
        this.tracer = tracer;
        this.propagator = propagator;
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
        selected.ifPresent(this::processMessageInfo);
    }
    
    void processMessageInfo(RabbitMessageInfo messageInfo){
        try {
            Span nextSpan = getSpanWithTraceIdFromMessage(propagator, messageInfo.message(), "process-rabbit-message");
            try (Tracer.SpanInScope ws = tracer.withSpan(nextSpan)) {
                
                // Now you can grab the Trace ID!
                String traceId1 = nextSpan.context().traceId();
                log.info("Trace ID1: {}", traceId1);

                String traceId2 = tracer.currentSpan().context().traceId();
                log.info("Trace ID2: {}", traceId2);
                // process the selected message
                processor.onMessage(messageInfo);
                // ack the selected message
                messageInfo.acknowledge();
            } finally {
                nextSpan.end();
            }
        } finally {
            messageInfo.closeChannel();
        }
    }

    private Optional<RabbitMessageInfo> pollRabbit(boolean isHiPriority, Supplier<Channel> channelSupplier, String queueName) {
        try {
            Channel channel = channelSupplier.get();
            GetResponse pollResult = channel.basicGet(queueName, false);
            if (pollResult == null) {
                channel.close();
                return Optional.empty();
            } else {
                AMQP.BasicProperties basicProperties = pollResult.getProps();
                MessageProperties messageProperties = converter.toMessageProperties(basicProperties, pollResult.getEnvelope(), StandardCharsets.UTF_8.name());
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
        String appName = ctx.getEnvironment().getProperty("spring.application.name", "spring.application.name not set!");
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

    public static Span getSpanWithTraceIdFromMessage(Propagator propagator, Message message, String spanName) {
        Span.Builder spanBuilder = propagator.extract(message, RabbitMessageSelector.GETTER);
        Span nextSpan = spanBuilder.name(spanName).start();
        return nextSpan;
    }
}
