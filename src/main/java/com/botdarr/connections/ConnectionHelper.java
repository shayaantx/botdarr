package com.botdarr.connections;

import com.botdarr.api.Api;
import com.botdarr.Config;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConnectionHelper {
  public static <T> T makeGetRequest(Api api, String path, ResponseHandler<T> responseHandler) {
    return makeGetRequest(api, path, "", responseHandler);
  }

  public static <T> T makeGetRequest(Api api, String path, String params, ResponseHandler<T> responseHandler) {
    return makeRequest(new RequestHandler() {
      @Override
      public HttpRequestBase buildRequest() throws Exception {
        HttpGet get = new HttpGet(api.getApiUrl(path) + params);
        get.setHeader("X-Api-Key", Config.getProperty(api.getApiToken()));
        return get;
      }

      @Override
      public boolean turnOnTimeouts() {
        // all api requests should use timeouts
        return true;
      }
    }, responseHandler);
  }

  public static <T> T makeRequest(RequestHandler requestHandler, ResponseHandler<T> responseHandler) {
    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
    if (requestHandler.turnOnTimeouts()) {
      int timeout = Config.getTimeout();
      requestConfigBuilder.setConnectTimeout(timeout);
      requestConfigBuilder.setSocketTimeout(timeout);
      requestConfigBuilder.setConnectionRequestTimeout(timeout);
    }
    try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfigBuilder.build()).build()) {
      try (CloseableHttpResponse response = client.execute(requestHandler.buildRequest())) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
          try {
            return responseHandler.onSuccess(EntityUtils.toString(response.getEntity()));
          } catch (Exception e) {
            LOGGER.error("Error trying to process response", e);
            return responseHandler.onException(e);
          }
        } else {
          return responseHandler.onFailure(statusCode, response.getStatusLine().getReasonPhrase());
        }
      } catch (HttpHostConnectException e) {
        LOGGER.error("Error trying to connect during request", e);
        return responseHandler.onConnectException(e);
      } catch (Exception e) {
        LOGGER.error("Error trying to execute connection during request", e);
        return responseHandler.onException(e);
      }
    } catch (IOException e) {
      LOGGER.error("Error trying to make connection during request", e);
      return responseHandler.onException(e);
    }
  }

  public static abstract class SimpleMessageEmbedResponseHandler implements ResponseHandler<List<ChatClientResponse>> {
    @Override
    public List<ChatClientResponse> onConnectException(HttpHostConnectException e) {
      return onException(e);
    }

    public SimpleMessageEmbedResponseHandler(ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder) {
      this.chatClientResponseBuilder = chatClientResponseBuilder;
    }

    @Override
    public List<ChatClientResponse> onFailure(int statusCode, String reason) {
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Request failed with status code=" + statusCode + ", reason=" + reason));
    }

    @Override
    public List<ChatClientResponse> onException(Exception e) {
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Requested failed with exception, e=" + e.getMessage() + ",class=" + e.getClass()));
    }
    private ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  }

  public static abstract class SimpleEntityResponseHandler<T> implements ResponseHandler<T> {

    @Override
    public T onFailure(int statusCode, String reason) {
      return null;
    }

    @Override
    public T onException(Exception e) {
      return null;
    }

    @Override
    public T onConnectException(HttpHostConnectException e) {
      return null;
    }
  }

  public interface RequestHandler {
    HttpRequestBase buildRequest() throws Exception;
    default boolean turnOnTimeouts() {
      return false;
    }
  }

  public interface ResponseHandler<T> {
    T onSuccess(String response) throws Exception;

    T onFailure(int statusCode, String reason);

    T onException(Exception e);

    T onConnectException(HttpHostConnectException e);
  }

  private static final Logger LOGGER = LogManager.getLogger();
}
