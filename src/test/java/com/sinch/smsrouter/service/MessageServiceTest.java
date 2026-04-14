package com.sinch.smsrouter.service;

import com.sinch.smsrouter.dto.MessageResponse;
import com.sinch.smsrouter.dto.SendMessageRequest;
import com.sinch.smsrouter.dto.SendMessageResponse;
import com.sinch.smsrouter.exception.InvalidPhoneNumberException;
import com.sinch.smsrouter.exception.MessageNotFoundException;
import com.sinch.smsrouter.exception.UnsupportedFormatException;
import com.sinch.smsrouter.model.Carrier;
import com.sinch.smsrouter.model.MessageStatus;
import com.sinch.smsrouter.repository.MessageRepository;
import com.sinch.smsrouter.repository.OptOutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageServiceTest {

    private MessageService messageService;
    private OptOutService optOutService;

    @BeforeEach
    void setUp() {
        MessageRepository messageRepository = new MessageRepository();
        PhoneNumberValidator phoneNumberValidator = new PhoneNumberValidator();
        optOutService = new OptOutService(new OptOutRepository(), phoneNumberValidator);
        CarrierRoutingService carrierRoutingService = new CarrierRoutingService();
        messageService = new MessageService(messageRepository, optOutService, phoneNumberValidator, carrierRoutingService);
    }

    @Test
    void shouldDeliverAuMessage() {
        SendMessageRequest request = request("+61491570156", "Hello", "SMS");
        SendMessageResponse response = messageService.sendMessage(request);

        assertThat(response.getStatus()).isEqualTo(MessageStatus.DELIVERED);
        assertThat(response.getId()).isNotNull();
    }

    @Test
    void shouldDeliverNzMessage() {
        SendMessageRequest request = request("+64211234567", "Hello", "SMS");
        SendMessageResponse response = messageService.sendMessage(request);

        assertThat(response.getStatus()).isEqualTo(MessageStatus.DELIVERED);
    }

    @Test
    void shouldBlockMessageToOptedOutNumber() {
        optOutService.optOut("+61491570156");
        SendMessageRequest request = request("+61491570156", "Hello", "SMS");
        SendMessageResponse response = messageService.sendMessage(request);

        assertThat(response.getStatus()).isEqualTo(MessageStatus.BLOCKED);
    }

    @Test
    void shouldThrowWhenPhoneNumberIsInvalid() {
        SendMessageRequest request = request("invalid-number", "Hello", "SMS");

        assertThatThrownBy(() -> messageService.sendMessage(request))
                .isInstanceOf(InvalidPhoneNumberException.class);
    }

    @Test
    void shouldThrowWhenFormatIsNotSms() {
        SendMessageRequest request = request("+61491570156", "Hello", "MMS");

        assertThatThrownBy(() -> messageService.sendMessage(request))
                .isInstanceOf(UnsupportedFormatException.class);
    }

    @Test
    void shouldReturnFullRecordForExistingMessage() {
        SendMessageRequest request = request("+61491570156", "Hello", "SMS");
        SendMessageResponse sent = messageService.sendMessage(request);

        MessageResponse response = messageService.getMessageStatus(sent.getId());

        assertThat(response.getId()).isEqualTo(sent.getId());
        assertThat(response.getStatus()).isEqualTo(MessageStatus.DELIVERED);
        assertThat(response.getCarrier()).isEqualTo(Carrier.TELSTRA);
        assertThat(response.getDestinationNumber()).isEqualTo("+61491570156");
    }

    @Test
    void shouldThrowWhenMessageIdNotFound() {
        assertThatThrownBy(() -> messageService.getMessageStatus(UUID.randomUUID()))
                .isInstanceOf(MessageNotFoundException.class);
    }

    @Test
    void shouldHaveNullCarrierWhenMessageIsBlocked() {
        optOutService.optOut("+61491570156");
        SendMessageResponse sent = messageService.sendMessage(request("+61491570156", "Hi", "SMS"));

        MessageResponse response = messageService.getMessageStatus(sent.getId());

        assertThat(response.getStatus()).isEqualTo(MessageStatus.BLOCKED);
        assertThat(response.getCarrier()).isNull();
    }

    @Test
    void shouldNotIncrementAuCounterForBlockedMessage() {
        // First normal AU send → Telstra
        messageService.sendMessage(request("+61491570156", "Hi", "SMS"));

        // Opt out and send → BLOCKED, counter should not increase
        optOutService.optOut("+61298765432");
        messageService.sendMessage(request("+61298765432", "Hi", "SMS"));

        // Next AU send should be Optus (counter = 1), not Telstra (counter = 2)
        SendMessageResponse response = messageService.sendMessage(request("+61491570156", "Hi", "SMS"));
        MessageResponse status = messageService.getMessageStatus(response.getId());
        assertThat(status.getCarrier()).isEqualTo(Carrier.OPTUS);
    }

    private SendMessageRequest request(String number, String content, String format) {
        SendMessageRequest req = new SendMessageRequest();
        req.setDestinationNumber(number);
        req.setContent(content);
        req.setFormat(format);
        return req;
    }
}
