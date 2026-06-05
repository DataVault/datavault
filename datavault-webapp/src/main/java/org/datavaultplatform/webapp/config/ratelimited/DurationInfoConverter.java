package org.datavaultplatform.webapp.config.ratelimited;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

import java.util.Optional;

public class DurationInfoConverter implements Converter<String, DurationInfo> {

    @Override
    public @Nullable DurationInfo convert(@NonNull String source) {
        for (DurationType type : DurationType.values()) {
            Optional<DurationInfo> result = type.extractDurationInfo(source);
            if (result.isPresent()) {
                return result.get();
            }
        }
        throw new IllegalArgumentException(
                "Cannot convert [%s] to DurationInfo - valid examples : [1s, 2m, 3h, 4d, PT1S, PT2M, PT3H, P4D]".formatted(source)
        );
    }
}
