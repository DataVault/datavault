package org.datavaultplatform.common.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
class TaskTest {

    // assumes each chunk is 1GB and the entire file is 10TB
    public static final int NUMBER_OF_CHUNKS = 10_240;

    File tempFile;

    ObjectMapper mapper;

    @SneakyThrows
    @BeforeEach
    void setup() {
        tempFile = Files.createTempFile("test", ".json").toFile();
        mapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    @SneakyThrows
    void testTaskSerializationForChunkNumbers() {
        Task task = new Task();
        assertThat(task.getRestartArchiveIds()).isEmpty();
        
        HashMap<String, String> properties = new HashMap<>();

        List<Integer> origChunkNumbers = IntStream.rangeClosed(1, NUMBER_OF_CHUNKS).boxed().toList();

        properties.put(PropNames.DEPOSIT_CHUNKS_STORED, Utils.toCommaSeparatedString(origChunkNumbers));
        task.setProperties(properties);

        mapper.writeValue(this.tempFile, task);

        assertThat(tempFile.exists()).isTrue();
        assertThat(tempFile.isFile()).isTrue();
        assertThat(tempFile.canRead()).isTrue();

        long size = tempFile.length();
        
        log.info("file size [{}] bytes", size);
        assertThat(size).isLessThan(100_000);
        Task task2 = mapper.readValue(tempFile, Task.class);
        List<Integer> chunkNumbers = Utils.fromCommaSeparatedString(task2.getProperties().get(PropNames.DEPOSIT_CHUNKS_STORED), Integer::parseInt);
        assertThat(chunkNumbers).isEqualTo(origChunkNumbers);

        assertThat(task2.getRestartArchiveIds()).isEmpty();
    }
}