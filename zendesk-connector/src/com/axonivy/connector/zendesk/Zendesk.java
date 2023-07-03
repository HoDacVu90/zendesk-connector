package com.axonivy.connector.zendesk;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Realm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.log.Logger;


public class Zendesk implements Closeable {
	private static final String JSON = "application/json; charset=UTF-8";
    private static final DefaultAsyncHttpClientConfig DEFAULT_ASYNC_HTTP_CLIENT_CONFIG =
            new DefaultAsyncHttpClientConfig.Builder().setFollowRedirect(true).build();
    private final boolean closeClient;
    private final AsyncHttpClient client;
    private final Realm realm;
    private final String url;
    private final String oauthToken;
    private final Map<String, String> headers;
    private final ObjectMapper mapper;
    private final Logger logger;
    private boolean closed = false;
	
    private Zendesk(AsyncHttpClient client, String url, String username, String password, Map<String, String> headers) {
        this.logger = Ivy.log();
        this.closeClient = client == null;
        this.oauthToken = null;
        this.client = client == null ? new DefaultAsyncHttpClient(DEFAULT_ASYNC_HTTP_CLIENT_CONFIG) : client;
        this.url = url.endsWith("/") ? url + "api/v2" : url + "/api/v2";
        if (username != null) {
            this.realm = new Realm.Builder(username, password)
                    .setScheme(Realm.AuthScheme.BASIC)
                    .setUsePreemptiveAuth(true)
                    .build();
        } else {
            if (password != null) {
                throw new IllegalStateException("Cannot specify token or password without specifying username");
            }
            this.realm = null;
        }
        this.headers = Collections.unmodifiableMap(headers);
        this.mapper = createMapper();
    }

    private Zendesk(AsyncHttpClient client, String url, String oauthToken, Map<String, String> headers) {
    	this.logger = Ivy.log();
        this.closeClient = client == null;
        this.realm = null;
        this.client = client == null ? new DefaultAsyncHttpClient(DEFAULT_ASYNC_HTTP_CLIENT_CONFIG) : client;
        this.url = url.endsWith("/") ? url + "api/v2" : url + "/api/v2";
        if (oauthToken != null) {
            this.oauthToken = oauthToken;
        } else {
            throw new IllegalStateException("Cannot specify token or password without specifying username");
        }
        this.headers = Collections.unmodifiableMap(headers);

        this.mapper = createMapper();
    }
	
    public boolean isClosed() {
        return closed || client.isClosed();
    }

    public void close() {
        if (closeClient && !client.isClosed()) {
            try {
                client.close();
            } catch (IOException e) {
                logger.warn("Unexpected error on client close", e);
            }
        }
        closed = true;
    }
    
    public static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new StdDateFormat());
        mapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);
        return mapper;
    }

    public static class Builder {
        private AsyncHttpClient client = null;
        private final String url;
        private String username = null;
        private String password = null;
        private String token = null;
        private String oauthToken = null;
        private final Map<String, String> headers;

        public Builder(String url) {
            this.url = url;
            this.headers = new HashMap<>();
        }

        public Builder setClient(AsyncHttpClient client) {
            this.client = client;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            if (password != null) {
                this.token = null;
                this.oauthToken = null;
            }
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            if (token != null) {
                this.password = null;
                this.oauthToken = null;
            }
            return this;
        }


        public Builder setOauthToken(String oauthToken) {
            this.oauthToken = oauthToken;
            if (oauthToken != null) {
                this.password = null;
                this.token = null;
            }
            return this;
        }


        public Builder setRetry(boolean retry) {
            return this;
        }

        public Builder addHeader(String name, String value) {
            Objects.requireNonNull(name, "Header name cannot be null");
            Objects.requireNonNull(value, "Header value cannot be null");
            headers.put(name, value);
            return this;
        }

        public Zendesk build() {
            if (token != null) {
                return new Zendesk(client, url, username + "/token", token, headers);
            } else if (oauthToken != null) {
                return new Zendesk(client, url, oauthToken, headers);
            }
            return new Zendesk(client, url, username, password, headers);
        }
    }

}
