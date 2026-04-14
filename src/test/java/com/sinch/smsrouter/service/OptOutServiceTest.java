package com.sinch.smsrouter.service;

import com.sinch.smsrouter.exception.InvalidPhoneNumberException;
import com.sinch.smsrouter.repository.OptOutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptOutServiceTest {

    private OptOutService optOutService;

    @BeforeEach
    void setUp() {
        optOutService = new OptOutService(new OptOutRepository(), new PhoneNumberValidator());
    }

    @Test
    void shouldOptOutValidPhoneNumber() {
        assertThatNoException().isThrownBy(() -> optOutService.optOut("+61491570156"));
    }

    @Test
    void shouldThrowWhenPhoneNumberIsInvalid() {
        assertThatThrownBy(() -> optOutService.optOut("invalid-number"))
                .isInstanceOf(InvalidPhoneNumberException.class);
    }

    @Test
    void shouldReturnTrueForOptedOutNumber() {
        optOutService.optOut("+61491570156");
        assertThat(optOutService.isOptedOut("+61491570156")).isTrue();
    }

    @Test
    void shouldReturnFalseForNonOptedOutNumber() {
        assertThat(optOutService.isOptedOut("+61491570156")).isFalse();
    }
}
