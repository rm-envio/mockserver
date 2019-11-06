package org.mockserver.matchers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mockserver.log.model.LogEntry;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.model.HttpRequest;

import java.util.Arrays;

import static org.slf4j.event.Level.TRACE;

/**
 * @author jamesdbloom
 */
public class BinaryMatcher extends BodyMatcher<byte[]> {
    private static final String[] excludedFields = {"mockServerLogger"};
    private final MockServerLogger mockServerLogger;
    private final byte[] matcher;

    public BinaryMatcher(MockServerLogger mockServerLogger, byte[] matcher) {
        this.mockServerLogger = mockServerLogger;
        this.matcher = matcher;
    }

    public boolean matches(final HttpRequest context, byte[] matched) {
        boolean result = false;

        if (matcher == null || matcher.length == 0 || Arrays.equals(matcher, matched)) {
            result = true;
        }

        if (!result) {
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(LogEntry.LogMessageType.TRACE)
                    .setLogLevel(TRACE)
                    .setHttpRequest(context)
                    .setMessageFormat("Failed to perform binary match [{}] with [{}] because {}")
                    .setArguments((Object) matched)
            );
        }

        return not != result;
    }

    @Override
    @JsonIgnore
    public String[] fieldsExcludedFromEqualsAndHashCode() {
        return excludedFields;
    }
}
