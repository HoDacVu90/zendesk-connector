package com.axonivy.connector.zendesk.enums;


public enum Status {
    NEW,
    OPEN,
    PENDING,
    HOLD,
    SOLVED,
    CLOSED,
    DELETED;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

}
