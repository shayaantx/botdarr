package com.botdarr.api;

import com.botdarr.Config;
import com.botdarr.connections.RequestBuilder;
import org.apache.logging.log4j.util.Strings;
import java.util.HashMap;
import java.util.Map;

public class ArrRequestBuilder {
    private final String urlConfigName;
    private final String urlBaseConfigName;
    private final String tokenName;
    private final RequestBuilder requestBuilder = new RequestBuilder();
    public ArrRequestBuilder(String urlConfigName, String urlBaseConfigName, String tokenName) {
        this.urlConfigName = urlConfigName;
        this.urlBaseConfigName = urlBaseConfigName;
        this.tokenName = tokenName;
    }

    private RequestBuilder tokens(RequestBuilder requestBuilder) {
        //TODO: which *arr's actually need the api key in path and headers? (this is likely a side effect from one
        //of the apis requiring one or the other and me being lazy)
        return requestBuilder.param("apiKey", Config.getProperty(this.tokenName))
                .headers("X-Api-Key", Config.getProperty(this.tokenName));
    }

    private String getHost() {
        String host = Config.getProperty(this.urlConfigName);
        String rawUrlBase = Config.getProperty(this.urlBaseConfigName);
        String urlBase = Strings.isBlank(rawUrlBase) ? "" : "/" + rawUrlBase;
        return host + urlBase + getApiSuffix();
    }

    public String getApiSuffix() {
        return "/api/";
    }

    public RequestBuilder buildGet(String path) {
        return buildGet(path, new HashMap<>());
    }

    public RequestBuilder buildGet(String path, Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            this.requestBuilder.param(entry.getKey(), entry.getValue());
        }
        RequestBuilder requestBuilder = this.requestBuilder.host(getHost() + path);
        return this.tokens(requestBuilder);
    }

    public RequestBuilder buildPost(String path, String postBody) {
        RequestBuilder requestBuilder = this.requestBuilder.host(getHost() + path).post(postBody);
        return this.tokens(requestBuilder);
    }
}