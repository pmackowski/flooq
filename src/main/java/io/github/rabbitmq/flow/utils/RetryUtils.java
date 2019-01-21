package io.github.rabbitmq.flow.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.retry.Retry;

import java.time.Duration;

public class RetryUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepeatUtils.class);

    public static final String RETRYING_DUE_TO_THE_FOLLOWING_EXCEPTION = "Retrying due to the following exception";
    public static final Duration DEFAULT_FIRST_BACKOFF = Duration.ofSeconds(2);
    public static final Duration DEFAULT_MAX_BACKOFF = Duration.ofSeconds(60);

    public static <T> Retry<T> defaultExponentialBackoff() {
        return defaultExponentialBackoff(RETRYING_DUE_TO_THE_FOLLOWING_EXCEPTION, true);
    }

    public static <T> Retry<T> defaultExponentialBackoff(String message) {
        return defaultExponentialBackoff(message, true);
    }

    public static <T> Retry<T> defaultExponentialBackoff(String message, boolean condition) {
        return Retry.<T>onlyIf(tRetryContext -> condition)
                .doOnRetry(retryContext -> LOGGER.error(message, retryContext.exception()))
                .exponentialBackoff(DEFAULT_FIRST_BACKOFF, DEFAULT_MAX_BACKOFF)
                .retryMax(Integer.MAX_VALUE);
    }


}
