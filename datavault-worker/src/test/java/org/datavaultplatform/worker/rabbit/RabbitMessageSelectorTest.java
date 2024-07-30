package org.datavaultplatform.worker.rabbit;

import com.rabbitmq.client.*;
import lombok.SneakyThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RabbitMessageSelectorTest {

    
    @Mock
    RabbitMessageProcessor mProcessor;
    
    @Mock
    ConnectionFactory mConnectionFactory;
    
    @Captor
    ArgumentCaptor<RabbitMessageInfo> argRabbitMessageInfo;

    RabbitMessageSelector selector;

    String loPriorityQueueName = "loPriorityQueue";
    String hiPriorityQueueName = "hiPriorityQueue";
    
    GetResponse hiGetResponse;
    GetResponse loGetResponse;
    
    @Mock
    Channel mHiChannel;
    @Mock
    Channel mLoChannel;
    
    public static String HI_MESSAGE = "HI_MESSAGE";
    public static String LO_MESSAGE = "LO_MESSAGE";
    
    byte[] bytesLo;
    byte[] bytesHi;
    Envelope envelopeLo;
    Envelope envelopeHi;

    AMQP.BasicProperties propsLO;
    AMQP.BasicProperties propsHI;
    
    @Mock
    ApplicationContext mApplicationContext;
    
    @Mock
    ApplicationReadyEvent mReadyEvent;
    
    @BeforeEach
    void setup() {
        lenient().when(mReadyEvent.getTimeTaken()).thenReturn(Duration.ofSeconds(1));
        propsHI = new AMQP.BasicProperties();
        propsLO = new AMQP.BasicProperties();
        
        bytesHi = HI_MESSAGE.getBytes(StandardCharsets.UTF_8);
        envelopeHi = new Envelope(1234, true, "exHI", "rkHI");

        bytesLo = LO_MESSAGE.getBytes(StandardCharsets.UTF_8);
        envelopeLo = new Envelope(101, true, "exLO", "rkLO");
        
        selector = spy(new RabbitMessageSelector(hiPriorityQueueName, loPriorityQueueName, mConnectionFactory, mProcessor));
        selector.onReady(mReadyEvent);

        lenient().doNothing().when(mProcessor).onMessage(argRabbitMessageInfo.capture());
        
        lenient().doReturn(mHiChannel, mLoChannel).when(selector).createChannel();

        loGetResponse = new GetResponse(envelopeLo, propsLO, bytesLo, 1);
        hiGetResponse = new GetResponse(envelopeHi, propsHI, bytesHi, 1);
    }
    
    @Test
    @SneakyThrows
    void testNoMessagesWhenPolled() {
        
        when(mHiChannel.basicGet(hiPriorityQueueName, false)).thenReturn(null);
        when(mLoChannel.basicGet(loPriorityQueueName, false)).thenReturn(null);
        
        selector.selectAndProcessNextMessage();

        Mockito.verify(mHiChannel).basicGet(hiPriorityQueueName, false);
        Mockito.verify(mLoChannel).basicGet(loPriorityQueueName, false);
        
        Mockito.verify(mLoChannel).close();
        Mockito.verify(mHiChannel).close();

        verifyNoMoreInteractions(mProcessor);
    }
    
    @Test
    @SneakyThrows
    void testHiPriorityMessagesWhenPolled() {

        when(mHiChannel.basicGet(hiPriorityQueueName, false)).thenReturn(hiGetResponse);

        doNothing().when(mProcessor).onMessage(argRabbitMessageInfo.capture());
        selector.selectAndProcessNextMessage();

        Mockito.verify(mHiChannel).basicGet(hiPriorityQueueName, false);
        Mockito.verify(mLoChannel, never()).basicGet(any(String.class), any(Boolean.class));

        RabbitMessageInfo actualInfo = argRabbitMessageInfo.getValue();
        checkHi(actualInfo);
        
        Mockito.verify(mProcessor).onMessage(actualInfo);
        
        verifyNoMoreInteractions(mProcessor);
    }

    private void checkHi(RabbitMessageInfo info){
        assertThat(info.message().getBody()).isEqualTo(bytesHi);
        assertThat(info.channel()).isEqualTo(mHiChannel);
        assertThat(info.queueName()).isEqualTo(hiPriorityQueueName);
        assertThat(info.getMessageBody()).isEqualTo(HI_MESSAGE);
        assertThat(info.deliveryTag()).isEqualTo(1234);
    }

    private void checkLo(RabbitMessageInfo info){
        assertThat(info.message().getBody()).isEqualTo(bytesLo);
        assertThat(info.channel()).isEqualTo(mLoChannel);
        assertThat(info.queueName()).isEqualTo(loPriorityQueueName);
        assertThat(info.getMessageBody()).isEqualTo(LO_MESSAGE);
        assertThat(info.deliveryTag()).isEqualTo(101);
    }
    
    @Test
    @SneakyThrows
    void testLoPriorityMessagesWhenPolled() {

        when(mHiChannel.basicGet(hiPriorityQueueName, false)).thenReturn(null);
        when(mLoChannel.basicGet(loPriorityQueueName, false)).thenReturn(loGetResponse);

        doNothing().when(mProcessor).onMessage(argRabbitMessageInfo.capture());
        selector.selectAndProcessNextMessage();

        Mockito.verify(mLoChannel).basicGet(loPriorityQueueName, false);
        Mockito.verify(mHiChannel).basicGet(hiPriorityQueueName, false);
        
        RabbitMessageInfo actualMessageInfo = argRabbitMessageInfo.getValue();
        checkLo(actualMessageInfo);
        Mockito.verify(mProcessor).onMessage(actualMessageInfo);

        verifyNoMoreInteractions(mProcessor);
    }
    
    @Nested
    class GetFirstMessageTests {
        @Test
        void testGetFirst() {
            Optional<String> first = RabbitMessageSelector.getFirst(() -> returnSome("one"), () -> returnSome("two"));
            assertThat(first).contains("one");
        }

        @Test
        void testGetSecond() {
            Optional<String> first = RabbitMessageSelector.getFirst(this::returnEmpty, () -> returnSome("two"));
            assertThat(first).contains("two");
        }

        @Test
        void testBothFail() {
            Optional<String> first = RabbitMessageSelector.getFirst(this::returnEmpty, this::returnEmpty);
            assertThat(first).isEmpty();
        }

        Optional<String> returnSome(String message) {
            System.out.printf("generating optional for [%s]%n", message);
            return Optional.of(message);
        }

        Optional<String> returnEmpty() {
            System.out.printf("generating EMPTY %n");
            return Optional.empty();
        }
    }

}
