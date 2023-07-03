package com.axonivy.connector.zendesk.enums;


public enum Type {
    PROBLEM,
    INCIDENT,
    QUESTION,
    TASK;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

}
