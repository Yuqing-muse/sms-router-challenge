package com.sinch.smsrouter.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Carrier {
    TELSTRA("Telstra"),
    OPTUS("Optus"),
    SPARK("Spark"),
    GLOBAL("Global");

    private final String displayName;

    Carrier(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
