package com.axonivy.connector.zendesk.model;

import java.io.Serializable;
import java.util.Map;

public class Via implements Serializable {

    private static final long serialVersionUID = 1L;

    private String channel;
    private Map<String, Object> source;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Map<String, Object> getSource() {
        return source;
    }

    public void setSource(Map<String, Object> source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "Via" +
                "{channel='" + channel + '\'' +
                ", source=" + source +
                '}';
    }
}
