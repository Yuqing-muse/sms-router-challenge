package com.sinch.smsrouter.service;

import com.sinch.smsrouter.repository.OptOutRepository;
import org.springframework.stereotype.Service;

@Service
public class OptOutService {

    private final OptOutRepository optOutRepository;
    private final PhoneNumberValidator phoneNumberValidator;

    public OptOutService(OptOutRepository optOutRepository, PhoneNumberValidator phoneNumberValidator) {
        this.optOutRepository = optOutRepository;
        this.phoneNumberValidator = phoneNumberValidator;
    }

    public void optOut(String phoneNumber) {
        // Reject invalid numbers early so the opt-out list stays clean
        phoneNumberValidator.validate(phoneNumber);
        optOutRepository.add(phoneNumber);
    }

    public boolean isOptedOut(String phoneNumber) {
        return optOutRepository.contains(phoneNumber);
    }
}
