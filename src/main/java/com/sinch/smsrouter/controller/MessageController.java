package com.sinch.smsrouter.controller;

import com.sinch.smsrouter.dto.MessageResponse;
import com.sinch.smsrouter.dto.SendMessageRequest;
import com.sinch.smsrouter.dto.SendMessageResponse;
import com.sinch.smsrouter.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SendMessageResponse send(@Valid @RequestBody SendMessageRequest request) {
        return messageService.sendMessage(request);
    }

    @GetMapping("/{id}")
    public MessageResponse getStatus(@PathVariable UUID id) {
        return messageService.getMessageStatus(id);
    }
}
