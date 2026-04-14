package com.sinch.smsrouter.exception;

public class InvalidPhoneNumberException extends RuntimeException {

    public InvalidPhoneNumberException(String phoneNumber) {
        super("Invalid phone number: " + phoneNumber);
    }
}
