package com.revolution.tools.exception;

/**
 * Exception thrown when there's an error with Claude AI operations.
 */
public class ClaudeAIException extends RuntimeException {

    public ClaudeAIException(String message) {
        super(message);
    }

    public ClaudeAIException(String message, Throwable cause) {
        super(message, cause);
    }
}
