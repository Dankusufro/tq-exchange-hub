package com.tq.exchangehub.exception;

import org.springframework.http.HttpStatus;

public class AdminReportException extends RuntimeException {

    private final HttpStatus status;

    public AdminReportException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public AdminReportException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
