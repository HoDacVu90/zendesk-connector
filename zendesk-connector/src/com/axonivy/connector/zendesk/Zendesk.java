package com.axonivy.connector.zendesk;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.HttpMethod;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Realm;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.LoggerFactory;

import com.axonivy.connector.zendesk.dto.RequestDTO;
import com.axonivy.connector.zendesk.dto.TicketDTO;
import com.axonivy.connector.zendesk.model.Attachment;
import com.axonivy.connector.zendesk.model.Comment;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

//import ch.ivyteam.ivy.environment.Ivy;
//import ch.ivyteam.log.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
//        this.logger = Ivy.log();
    	this.logger = LoggerFactory.getLogger(Zendesk.class);
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
//    	this.logger = Ivy.log();
    	this.logger = LoggerFactory.getLogger(Zendesk.class);
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
    
    public com.axonivy.connector.zendesk.model.Request createRequest(RequestDTO request) {
    	return complete(submit(req(HttpMethod.POST, cnst("/requests.json"),
    			JSON, json(Collections.singletonMap("request", request))),
			handle(com.axonivy.connector.zendesk.model.Request.class, "request")));
    }
    
    public Attachment.Upload createUpload(String fileName, byte[] content) {
        return createUpload(null, fileName, "application/binary", content);
    }

    public Attachment.Upload createUpload(String fileName, String contentType, byte[] content) {
        return createUpload(null, fileName, contentType, content);
    }

    public Attachment.Upload createUpload(String token, String fileName, String contentType, byte[] content) {
        TemplateUri uri = tmpl("/uploads.json{?filename,token}").set("filename", fileName);
        if (token != null) {
            uri.set("token", token);
        }
        return complete(
                submit(req("POST", uri, contentType,
                        content), handle(Attachment.Upload.class, "upload")));
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

    private void logResponse(Response response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Response HTTP/{} {}\n{}", response.getStatusCode(), response.getStatusText(),
                    response.getResponseBody());
        }
        if (logger.isTraceEnabled()) {
            logger.debug("Response headers {}", response.getHeaders());
        }
    }

    private TemplateUri tmpl(String template) {
        return new TemplateUri(url + template);
    }

    private Uri cnst(String template) {
        return new FixedUri(url + template);
    }

    private Request req(String method, Uri template) {
        return req(method, template.toString());
    }

    private byte[] json(Object object) {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new ZendeskException(e.getMessage(), e);
        }
    }

    private <T> ListenableFuture<T> submit(Request request, ZendeskAsyncCompletionHandler<T> handler) {
        if (logger.isDebugEnabled()) {
            if (request.getStringData() != null) {
                logger.debug("Request {} {}\n{}", request.getMethod(), request.getUrl(), request.getStringData());
            } else if (request.getByteData() != null) {
                logger.debug("Request {} {} {} {} bytes", request.getMethod(), request.getUrl(),
                        request.getHeaders().get("Content-type"), request.getByteData().length);
            } else {
                logger.debug("Request {} {}", request.getMethod(), request.getUrl());
            }
        }
        return client.executeRequest(request, handler);
    }
    
    private static <T> T complete(ListenableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new ZendeskException(e.getMessage(), e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ZendeskException) {
                if (e.getCause() instanceof ZendeskResponseRateLimitException) {
                    throw new ZendeskResponseRateLimitException((ZendeskResponseRateLimitException) e.getCause());
                }
                if (e.getCause() instanceof ZendeskResponseException) {
                    throw new ZendeskResponseException((ZendeskResponseException)e.getCause());
                }
                throw new ZendeskException(e.getCause());
            }
            throw new ZendeskException(e.getMessage(), e);
        }
    }

    private static abstract class ZendeskAsyncCompletionHandler<T> extends AsyncCompletionHandler<T> {
        @Override
        public void onThrowable(Throwable t) {
            if (t instanceof IOException) {
                throw new ZendeskException(t);
            } else {
                super.onThrowable(t);
            }
        }
    }
    
    private Request req(String method, String url) {
        return reqBuilder(method, url).build();
    }

    private Request req(String method, Uri template, String contentType, byte[] body) {
        RequestBuilder builder = reqBuilder(method, template.toString());
        builder.addHeader("Content-type", contentType);
        builder.setBody(body);
        return builder.build();
    }

    private RequestBuilder reqBuilder(String method, String url) {
        RequestBuilder builder = new RequestBuilder(method);
        if (realm != null) {
            builder.setRealm(realm);
        } else {
            builder.addHeader("Authorization", "Bearer " + oauthToken);
        }
        headers.forEach(builder::setHeader);
        return builder.setUrl(url);
    }
    
    protected <T> ZendeskAsyncCompletionHandler<T> handle(final Class<T> clazz, final String name, final Class... typeParams) {
    	return new BasicAsyncCompletionHandler<>(clazz, name, typeParams); 
    }

    private class BasicAsyncCompletionHandler<T> extends ZendeskAsyncCompletionHandler<T> {
        private final Class<T> clazz;
        private final String name;
        private final Class[] typeParams;

        public BasicAsyncCompletionHandler(Class clazz, String name, Class... typeParams) {
            this.clazz = clazz;
            this.name = name;
            this.typeParams = typeParams;
        }

		@Override
		public T onCompleted(Response response) throws Exception {
			logResponse(response);
			if(isStatus2xx(response)) {
				if (typeParams.length > 0) {
                    JavaType type = mapper.getTypeFactory().constructParametricType(clazz, typeParams);
                    return mapper.convertValue(mapper.readTree(response.getResponseBodyAsStream()).get(name), type);
                }
				return mapper.convertValue(mapper.readTree(response.getResponseBodyAsStream()).get(name), clazz);
			} else if (isRateLimitResponse(response)) {
                throw new ZendeskResponseRateLimitException(response);
            }
            if (response.getStatusCode() == 404) {
                return null;
            }
            throw new ZendeskResponseException(response);
		}
    }

    private boolean isStatus2xx(Response response) {
        return response.getStatusCode() / 100 == 2;
    }

    private boolean isRateLimitResponse(Response response) {
        return response.getStatusCode() == 429;
    }
}
