package org.datavaultplatform.webapp.config;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class StringToDateConverterTest {

    final StringToDateConverter sut = new StringToDateConverter();

    @Test
    void testNull(){
        assertThat(sut.convert(null)).isNull();
    }

    @Test
    void testBlank(){
        assertThat(sut.convert("   ")).isNull();
    }

    @Test
    void test6thFeb2024(){
        assertThat(sut.convert("2024-02-06")).isEqualTo(
                Date.from(LocalDate.of(2024,2,6).atStartOfDay().toInstant(ZoneOffset.UTC)));
    }
}