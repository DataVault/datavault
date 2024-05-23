package org.datavaultplatform.worker.rabbit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RabbitMessageSelectorTest {

    @Mock
    RabbitQueuePoller mHiPriorityPoller;

    @Mock
    RabbitQueuePoller mLoPriorityPoller;
    
    @Mock
    RabbitMessageProcessor mProcessor;
    
    
    @Mock
    RabbitMessageInfo mLoPriorityMessageInfo;

    @Mock
    RabbitMessageInfo mHiPriorityMessageInfo;
    
    @Captor
    ArgumentCaptor<RabbitMessageInfo> argRabbitMessageInfo;

    RabbitMessageSelector selector;

    @BeforeEach
    void setup() {
        selector = new RabbitMessageSelector(mHiPriorityPoller, mLoPriorityPoller, mProcessor);
        lenient().doNothing().when(mProcessor).onMessage(argRabbitMessageInfo.capture());
    }
    
    @Test
    void testNoMessagesWhenPolled() {
        
        when(mHiPriorityPoller.poll()).thenReturn(Optional.empty());
        when(mLoPriorityPoller.poll()).thenReturn(Optional.empty());
        
        selector.selectAndProcessNextMessage();

        Mockito.verify(mHiPriorityPoller).poll();
        Mockito.verify(mLoPriorityPoller).poll();
        
        verifyNoMoreInteractions(mProcessor, mHiPriorityPoller, mLoPriorityPoller, mHiPriorityMessageInfo, mLoPriorityMessageInfo);
    }
    
    @Test
    void testHiPriorityMessagesWhenPolled() {

        when(mHiPriorityPoller.poll()).thenReturn(Optional.of(mHiPriorityMessageInfo));
 
        selector.selectAndProcessNextMessage();

        Mockito.verify(mHiPriorityPoller).poll();
        Mockito.verify(mProcessor).onMessage(mHiPriorityMessageInfo);
        
        verifyNoMoreInteractions(mProcessor, mHiPriorityPoller, mLoPriorityPoller, mHiPriorityMessageInfo, mLoPriorityMessageInfo);
    }

    @Test
    void testLoPriorityMessagesWhenPolled() {

        when(mHiPriorityPoller.poll()).thenReturn(Optional.empty());
        when(mLoPriorityPoller.poll()).thenReturn(Optional.of(mLoPriorityMessageInfo));

        selector.selectAndProcessNextMessage();

        Mockito.verify(mHiPriorityPoller).poll();
        Mockito.verify(mLoPriorityPoller).poll();
        Mockito.verify(mProcessor).onMessage(mLoPriorityMessageInfo);

        verifyNoMoreInteractions(mProcessor, mHiPriorityPoller, mLoPriorityPoller, mHiPriorityMessageInfo, mLoPriorityMessageInfo);
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
