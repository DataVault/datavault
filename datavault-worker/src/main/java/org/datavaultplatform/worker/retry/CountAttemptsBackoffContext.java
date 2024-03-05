package org.datavaultplatform.worker.retry;

import lombok.Getter;
import lombok.Setter;
import org.springframework.retry.backoff.BackOffContext;

@Setter
@Getter
public class CountAttemptsBackoffContext implements BackOffContext {
    int attempts = 0;
    long totalDelay = 0;
}