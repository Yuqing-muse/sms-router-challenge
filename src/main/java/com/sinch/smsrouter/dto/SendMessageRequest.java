package com.sinch.smsrouter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class SendMessageRequest {

    @NotBlank
    @JsonProperty("destination_number")
    private String destinationNumber;

    @NotBlank
    private String content;

    @NotBlank
    private String format;

    public String getDestinationNumber() { return destinationNumber; }
    public String getContent() { return content; }
    public String getFormat() { return format; }

    public void setDestinationNumber(String destinationNumber) { this.destinationNumber = destinationNumber; }
    public void setContent(String content) { this.content = content; }
    public void setFormat(String format) { this.format = format; }
}
