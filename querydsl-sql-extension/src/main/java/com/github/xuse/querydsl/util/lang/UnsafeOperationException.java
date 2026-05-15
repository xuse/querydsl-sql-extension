package com.github.xuse.querydsl.util.lang;

/**
 * Unsafe 操作异常
 */
public class UnsafeOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnsafeOperationException(String message) {
        super(message);
    }

    public UnsafeOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
