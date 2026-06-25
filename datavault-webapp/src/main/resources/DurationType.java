package org.datavaultplatform.webapp.config.ratelimited;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.datavaultplatform.webapp.config.ratelimited.DurationTypePatterns.*;


public enum DurationType {

    SECONDS(REGEX_SECONDS, REGEX_ISO8601_SECONDS, Duration::ofSeconds),
    MINUTES(REGEX_MINUTES, REGEX_ISO8601_MINUTES, Duration::ofMinutes),
    HOURS(REGEX_HOURS, REGEX_ISO8601_HOURS, Duration::ofHours),
    DAYS(REGEX_DAYS, REGEX_ISO8601_DAYS, Duration::ofDays);

    private final Pattern iso8601pattern;
    private final Function<Long, Duration> durationFunction;
    private final Pattern nonIsoPattern;

    DurationType(Pattern nonIsoPattern, Pattern iso8601pattern, Function<Long, Duration> durationFunction) {
        this.nonIsoPattern = nonIsoPattern;
        this.durationFunction = durationFunction;
        this.iso8601pattern = iso8601pattern;
    }

    Optional<Long> matchISO(String value) {
        return match(this.iso8601pattern, value);
    }

    Optional<Long> matchNonISO(String value) {
        return match(this.nonIsoPattern, value);
    }

    Optional<Long> match(Pattern pattern, String value) {
        Matcher m = pattern.matcher(value);
        if (m.matches()) {
            return Optional.of(Long.valueOf(m.group(1)));
        } else {
            return Optional.empty();
        }
    }

    Optional<Long> matches(String value) {
        return matchISO(value).or(() -> matchNonISO(value));
    }

    public Optional<DurationInfo> extractDurationInfo(String value) {
        return this.matches(value)
                .map(durationFunction)
                .map(duration -> new DurationInfo(duration, this));
    }
}