package com.belka.jmxhttp.exception;

public class MBeanNotFoundException extends RuntimeException {
    public MBeanNotFoundException(String message) {
        super(message);
    }
}
