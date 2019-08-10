package com.mason.project.monitor;

public class MetricAlreadyExistsException extends RuntimeException {

    private static final long serialVersionID = -5644463087289986838L;

    public MetricAlreadyExistsException() {
        super();
    }

    public MetricAlreadyExistsException(String s) {
        super(s);
    }

    public MetricAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetricAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
