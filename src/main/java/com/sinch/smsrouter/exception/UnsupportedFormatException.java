package com.sinch.smsrouter.exception;

public class UnsupportedFormatException extends RuntimeException {

    public UnsupportedFormatException(String format) {
        super("Unsupported message format: " + format + ". Only 'SMS' is supported.");
    }
}
