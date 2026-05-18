package org.datavaultplatform.broker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.common.util.TraceInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class TraceControllerTest {

    TraceController controller;
    
    @Mock
    Sender mSender;
    
    @Mock
    Tracer mTracer;
    
    final ObjectMapper mapper = new ObjectMapper();
    
    @BeforeEach
    void setup() {
        controller = spy(new TraceController(mTracer, mSender, mapper));
    }
    
    
    @Captor
    ArgumentCaptor<String> argMessage;
    
    @Test
    void testBroker() {
        TraceInfo traceInfo = new TraceInfo("1234");
        doReturn(traceInfo).when(controller).getTraceInfo();
        TraceInfo result = controller.sendTraceTaskToWorker();
        verify(mSender).send(argMessage.capture(), eq(false));
        assertThat(result).isEqualTo(traceInfo);
        
        log.info(argMessage.getValue());
    }
}