package org.datavaultplatform.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class PausedStateDTOTest {
    
    private static final Clock FIXED = Clock.fixed(Instant.parse("2007-12-03T10:15:30Z"), ZoneOffset.UTC);

    private static final String PAUSED_STATE_JSON = """
             {
             "isPaused" : true,
             "created" : "2007-12-03T10:15:30"
             }
            """;

    final static  ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    @Test
    void testSerialization() throws Exception {
        PausedRetrieveStateDTO dto = new PausedRetrieveStateDTO(true, LocalDateTime.now(FIXED));
        String json = toJson(dto);
        JSONAssert.assertEquals(PAUSED_STATE_JSON, json, false);
    }

    @Test
    void testDeSerialization() throws Exception {
        PausedRetrieveStateDTO dto = MAPPER.readValue(PAUSED_STATE_JSON, PausedRetrieveStateDTO.class);
        assertThat(dto.isPaused()).isTrue();
        assertThat(dto.created()).isEqualTo(LocalDateTime.now(FIXED));
    }

    private String toJson(Object dto) throws Exception {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
    }
    
}