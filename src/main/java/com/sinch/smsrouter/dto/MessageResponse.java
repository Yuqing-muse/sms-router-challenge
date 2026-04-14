package com.sinch.smsrouter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sinch.smsrouter.model.Carrier;
import com.sinch.smsrouter.model.Message;
import com.sinch.smsrouter.model.MessageStatus;

import java.util.UUID;

public class MessageResponse {

    private final UUID id;
    @JsonProperty("destination_number")
    private final String destinationNumber;
    private final String content;
    private final String format;
    private final MessageStatus status;
    private final Carrier carrier;

    public MessageResponse(Message message) {
        this.id = message.getId();
        this.destinationNumber = message.getDestinationNumber();
        this.content = message.getContent();
        this.format = message.getFormat();
        this.status = message.getStatus();
        this.carrier = message.getCarrier();
    }

    public UUID getId() { return id; }
    public String getDestinationNumber() { return destinationNumber; }
    public String getContent() { return content; }
    public String getFormat() { return format; }
    public MessageStatus getStatus() { return status; }
    public Carrier getCarrier() { return carrier; }
}
