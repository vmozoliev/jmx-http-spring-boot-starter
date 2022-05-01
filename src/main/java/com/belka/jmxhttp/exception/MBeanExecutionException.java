package com.belka.jmxhttp.exception;

public class MBeanExecutionException extends RuntimeException {
    public MBeanExecutionException(Exception ex) {
        super(ex);
    }
}
