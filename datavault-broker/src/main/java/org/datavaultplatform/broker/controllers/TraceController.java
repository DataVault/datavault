package org.datavaultplatform.broker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.util.TraceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Profile("database")
@RequestMapping("/trace")
@Slf4j
public class TraceController {

    private final Tracer tracer;
    private final Sender sender;
    private final ObjectMapper mapper;
    @Autowired
    public TraceController(Tracer tracer, Sender sender, ObjectMapper mapper) {
        this.tracer = tracer;
        this.sender = sender;
        this.mapper = mapper;
    }

    @GetMapping("/info")
    public TraceInfo getTraceInfo() {
        String traceId = tracer.currentSpan().context().traceId();
        log.info("TraceId: {}", traceId);
        return new TraceInfo(traceId);
    }
    
    @SneakyThrows
    @GetMapping("/worker")
    public TraceInfo sendTraceTaskToWorker() {
        TraceInfo traceInfo = getTraceInfo();
        Map<String, String> properties = new HashMap<>();
        properties.put("brokerTraceId", traceInfo.traceId());
        List<ArchiveStore> archiveStores = List.of();
        Map<String, Map<String, String>> userFileStoreProperties = Map.of();
        Map<String, String> userFileStoreClasses = Map.of();
        Map<Integer, String> chunkFilesDigest = Map.of();
        byte[] tarIVs = new byte[0];
        Map<Integer, byte[]> chunksIVs = Map.of();
        String encTarDigest = "";
        Map<Integer, String> encChunksDigests = Map.of();
        Event lastEvent = null;
        Job job = new Job(){
            @Override
            public String getID() {
                return "1234567890";
            }
        };
        job.setState(0);
        job.setTaskClass("org.datavaultplatform.worker.tasks.Trace");
        Task retrieveTask = new Task(
                job, properties, archiveStores,
                userFileStoreProperties, userFileStoreClasses,
                null, null,
                chunkFilesDigest,
                tarIVs, chunksIVs,
                encTarDigest, encChunksDigests, lastEvent);
        String jsonRetrieve = mapper.writeValueAsString(retrieveTask);

        boolean isRestart = false;
        sender.send(jsonRetrieve, isRestart);
        return traceInfo;
    }
}
