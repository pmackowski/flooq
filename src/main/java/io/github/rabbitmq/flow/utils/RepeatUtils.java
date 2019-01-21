package io.github.rabbitmq.flow.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.retry.Repeat;

import java.time.Duration;

public class RepeatUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepeatUtils.class);

    public static final Duration ONE_SECOND = Duration.ofSeconds(1);

    public static <T> Repeat<T> afterOneSecond(String message) {
        return afterOneSecond(message, true);
    }

    public static <T> Repeat<T> afterOneSecond(String message, boolean condition) {
        return fixedBackoff(ONE_SECOND, message, condition);
    }

    public static <T> Repeat<T> fixedBackoff(Duration duration, String message, boolean condition) {
        return Repeat.<T>onlyIf(tRepeatContext -> condition)
                .doOnRepeat(objectRepeatContext -> LOGGER.info(message))
                .fixedBackoff(duration);
    }

}
