package com.sinch.smsrouter.dto;

import com.sinch.smsrouter.model.MessageStatus;

import java.util.UUID;

public class SendMessageResponse {

    private final UUID id;
    private final MessageStatus status;

    public SendMessageResponse(UUID id, MessageStatus status) {
        this.id = id;
        this.status = status;
    }

    public UUID getId() { return id; }
    public MessageStatus getStatus() { return status; }
}
