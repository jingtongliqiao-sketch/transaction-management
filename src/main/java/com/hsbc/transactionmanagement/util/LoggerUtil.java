package com.hsbc.transactionmanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {

    private LoggerUtil() {
    }

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static void logDebug(Logger logger, String format, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(format, args);
        }
    }

    public static void logInfo(Logger logger, String format, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(format, args);
        }
    }

    public static void logWarn(Logger logger, String format, Object... args) {
        logger.warn(format, args);
    }

    public static void logError(Logger logger, String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public static void logError(Logger logger, String format, Object... args) {
        logger.error(format, args);
    }

    public static void logMethodEntry(Logger logger, String methodName, Object... params) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entering {} with parameters: {}", methodName, params);
        }
    }

    public static void logMethodExit(Logger logger, String methodName, Object result) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting {} with result: {}", methodName, result);
        }
    }

    public static void logPerformance(Logger logger, String methodName, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 1000) { //log slow request only
            logger.warn("Performance warning: {} took {} ms", methodName, duration);
        } else if (logger.isDebugEnabled()) {
            logger.debug("{} executed in {} ms", methodName, duration);
        }
    }
}