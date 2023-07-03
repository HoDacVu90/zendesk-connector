package com.axonivy.connector.zendesk.enums;


public enum Priority {
    URGENT,
    HIGH,
    NORMAL,
    LOW;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

}
