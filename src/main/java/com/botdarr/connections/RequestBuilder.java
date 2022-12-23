package com.botdarr.connections;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestBuilder {
    private URIBuilder builder = new URIBuilder();
    private Map<String, String> headers = new HashMap<>();
    private String postBody = null;

    public String getHost() {
        return builder.getHost();
    }

    public RequestBuilder host(String host) {
        try {
            URL url = new URL(host);
            builder.setScheme(url.getProtocol());
            builder.setHost(url.getHost());
            builder.setPath(url.getPath());
            builder.setPort(url.getPort());
        } catch (MalformedURLException e) {
            LOGGER.error("Error trying to parse hostname " + host, e);
            throw new RuntimeException(e);
        }
        return this;
    }
    public RequestBuilder param(String key, String value) {
        builder.addParameter(key, value);
        return this;
    }
    public RequestBuilder headers(String key, String value) {
        headers.put(key, value);
        return this;
    }
    public RequestBuilder post(String postBody) {
        this.postBody = postBody;
        return this;
    }
    public HttpRequestBase build() {
        try {
            if (postBody != null) {
                HttpPost post = new HttpPost(builder.build());
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    post.addHeader(entry.getKey(), entry.getValue());
                }
                post.addHeader("content-type", "application/json");
                post.setEntity(new StringEntity(postBody, StandardCharsets.UTF_8));
                return post;
            }
            HttpGet get = new HttpGet(builder.build());
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                get.addHeader(entry.getKey(), entry.getValue());
            }
            return get;
        } catch (URISyntaxException e) {
            LOGGER.error("Error trying to build get request", e);
        }
        throw new RuntimeException("Could not build get or post request");
    }
    private static final Logger LOGGER = LogManager.getLogger();
}
