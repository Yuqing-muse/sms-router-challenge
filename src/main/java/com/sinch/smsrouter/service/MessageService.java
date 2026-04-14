package com.sinch.smsrouter.service;

import com.sinch.smsrouter.dto.MessageResponse;
import com.sinch.smsrouter.dto.SendMessageRequest;
import com.sinch.smsrouter.dto.SendMessageResponse;
import com.sinch.smsrouter.exception.MessageNotFoundException;
import com.sinch.smsrouter.exception.UnsupportedFormatException;
import com.sinch.smsrouter.model.Carrier;
import com.sinch.smsrouter.model.Message;
import com.sinch.smsrouter.model.MessageStatus;
import com.sinch.smsrouter.repository.MessageRepository;
import com.sinch.smsrouter.repository.OptOutRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final OptOutRepository optOutRepository;
    private final PhoneNumberValidator phoneNumberValidator;
    private final CarrierRoutingService carrierRoutingService;

    public MessageService(MessageRepository messageRepository,
                          OptOutRepository optOutRepository,
                          PhoneNumberValidator phoneNumberValidator,
                          CarrierRoutingService carrierRoutingService) {
        this.messageRepository = messageRepository;
        this.optOutRepository = optOutRepository;
        this.phoneNumberValidator = phoneNumberValidator;
        this.carrierRoutingService = carrierRoutingService;
    }

    public SendMessageResponse sendMessage(SendMessageRequest request) {
        if (!"SMS".equals(request.getFormat())) {
            throw new UnsupportedFormatException(request.getFormat());
        }

        phoneNumberValidator.validate(request.getDestinationNumber());

        Message message = new Message(
                UUID.randomUUID(),
                request.getDestinationNumber(),
                request.getContent(),
                request.getFormat()
        );

        if (optOutRepository.contains(request.getDestinationNumber())) {
            message.setStatus(MessageStatus.BLOCKED);
            messageRepository.save(message);
            return new SendMessageResponse(message.getId(), message.getStatus());
        }

        Carrier carrier = carrierRoutingService.selectCarrier(request.getDestinationNumber());
        message.setCarrier(carrier);
        message.setStatus(MessageStatus.SENT);
        message.setStatus(MessageStatus.DELIVERED);
        messageRepository.save(message);

        return new SendMessageResponse(message.getId(), message.getStatus());
    }

    public MessageResponse getMessageStatus(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));
        return new MessageResponse(message);
    }
}
