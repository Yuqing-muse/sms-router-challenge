package com.sinch.smsrouter.model;

import java.util.UUID;

public class Message {

    private final UUID id;
    private final String destinationNumber;
    private final String content;
    private final String format;
    private MessageStatus status;
    private Carrier carrier;

    public Message(UUID id, String destinationNumber, String content, String format) {
        this.id = id;
        this.destinationNumber = destinationNumber;
        this.content = content;
        this.format = format;
        this.status = MessageStatus.PENDING;
    }

    public UUID getId() { return id; }
    public String getDestinationNumber() { return destinationNumber; }
    public String getContent() { return content; }
    public String getFormat() { return format; }
    public MessageStatus getStatus() { return status; }
    public Carrier getCarrier() { return carrier; }

    public void setStatus(MessageStatus status) { this.status = status; }
    public void setCarrier(Carrier carrier) { this.carrier = carrier; }
}
