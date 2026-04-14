package com.sinch.smsrouter.service;

import com.sinch.smsrouter.exception.InvalidPhoneNumberException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class PhoneNumberValidator {

    // AU: +61 followed by 9 digits (area code 2-9, then 8 more digits)
    private static final Pattern AU_PATTERN = Pattern.compile("^\\+61[2-9]\\d{8}$");

    // NZ: +64 followed by 8-10 digits (mobile and landline ranges)
    private static final Pattern NZ_PATTERN = Pattern.compile("^\\+64[2-9]\\d{7,9}$");

    // Fallback: any + followed by digits (routed to Global)
    private static final Pattern GLOBAL_PATTERN = Pattern.compile("^\\+\\d+$");

    /**
     * Validates the phone number format.
     * AU and NZ numbers are validated strictly.
     * All other numbers are accepted if they match +<digits>.
     *
     * @throws InvalidPhoneNumberException if the number is malformed
     */
    public void validate(String phoneNumber) {
        if (phoneNumber == null) {
            throw new InvalidPhoneNumberException("null");
        }
        if (phoneNumber.startsWith("+61")) {
            if (!AU_PATTERN.matcher(phoneNumber).matches()) {
                throw new InvalidPhoneNumberException(phoneNumber);
            }
        } else if (phoneNumber.startsWith("+64")) {
            if (!NZ_PATTERN.matcher(phoneNumber).matches()) {
                throw new InvalidPhoneNumberException(phoneNumber);
            }
        } else if (!GLOBAL_PATTERN.matcher(phoneNumber).matches()) {
            throw new InvalidPhoneNumberException(phoneNumber);
        }
    }
}
