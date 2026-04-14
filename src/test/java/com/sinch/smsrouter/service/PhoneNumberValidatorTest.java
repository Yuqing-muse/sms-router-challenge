package com.sinch.smsrouter.service;

import com.sinch.smsrouter.exception.InvalidPhoneNumberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberValidatorTest {

    private PhoneNumberValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PhoneNumberValidator();
    }

    @ParameterizedTest(name = "shouldAcceptValidNumber: {0}")
    @ValueSource(strings = {
            "+61491570156",   // AU
            "+64211234567",   // NZ
            "+12025550123"    // Global
    })
    void shouldAcceptValidPhoneNumber(String number) {
        assertThatNoException().isThrownBy(() -> validator.validate(number));
    }

    @ParameterizedTest(name = "shouldRejectInvalidNumber: {0}")
    @NullSource
    @ValueSource(strings = {
            "61491570156",    // missing + prefix
            "+6149157015",    // AU too short
            "+614915701567",  // AU too long
            "invalid-number"    // plain text
    })
    void shouldRejectInvalidPhoneNumber(String number) {
        assertThatThrownBy(() -> validator.validate(number))
                .isInstanceOf(InvalidPhoneNumberException.class);
    }
}
