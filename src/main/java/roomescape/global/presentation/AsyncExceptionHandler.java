package roomescape.global.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());

    @Override
    public void handleUncaughtException(Throwable e, Method method, Object... params) {
        log.error("[Line number] " + e.getStackTrace()[0].getClassName() + " " + e.getStackTrace()[0].getLineNumber() +
                " [Exception] " + e.getClass() +
                " [Message] " + e.getMessage(), e);
    }
}
