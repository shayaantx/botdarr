package com.botdar.connections;

import com.botdar.api.Api;
import com.botdar.Config;
import com.botdar.clients.ChatClientResponse;
import com.botdar.clients.ChatClientResponseBuilder;
import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConnectionHelper {
  public static <T> List<T> makeGetRequest(Api api, String path, ResponseHandler<T> responseHandler) {
    return makeGetRequest(api, path, "", responseHandler);
  }

  public static <T> List<T> makeDeleteRequest(Api api, String path, ResponseHandler<T> responseHandler) {
    return makeDeleteRequest(api, path, "", responseHandler);
  }

  public static <T, K> List<T> makePostRequest(Api api, String path, K params, ResponseHandler<T> responseHandler) {
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(api.getApiUrl(path) + params);
      post.setHeader("X-Api-Key", Config.getProperty(api.getApiToken()));
      post.setEntity(new StringEntity(new Gson().toJson(params), ContentType.APPLICATION_JSON));
      try (CloseableHttpResponse response = client.execute(post)) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
          try {
            return responseHandler.onSuccess(EntityUtils.toString(response.getEntity()));
          } catch (Exception e) {
            LOGGER.error("Error trying to make post request", e);
            return responseHandler.onException(e);
          }
        } else {
          return responseHandler.onFailure(statusCode, response.getStatusLine().getReasonPhrase());
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error trying to make connection during post request", e);
      return responseHandler.onException(e);
    }
  }

  public static <T> List<T> makeGetRequest(Api api, String path, String params, ResponseHandler<T> responseHandler) {
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpGet get = new HttpGet(api.getApiUrl(path) + params);
      get.setHeader("X-Api-Key", Config.getProperty(api.getApiToken()));
      try (CloseableHttpResponse response = client.execute(get)) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
          try {
            return responseHandler.onSuccess(EntityUtils.toString(response.getEntity()));
          } catch (Exception e) {
            LOGGER.error("Error trying to make get request", e);
            return responseHandler.onException(e);
          }
        } else {
          return responseHandler.onFailure(statusCode, response.getStatusLine().getReasonPhrase());
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error trying to make connection during get request", e);
      return responseHandler.onException(e);
    }
  }

  public static <T> List<T> makeDeleteRequest(Api api, String path, String params, ResponseHandler<T> responseHandler) {
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpDelete delete = new HttpDelete(api.getApiUrl(path) + params);
      delete.setHeader("X-Api-Key", Config.getProperty(api.getApiToken()));
      try (CloseableHttpResponse response = client.execute(delete)) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
          try {
            return responseHandler.onSuccess(EntityUtils.toString(response.getEntity()));
          } catch (Exception e) {
            LOGGER.error("Error trying to make delete request", e);
            return responseHandler.onException(e);
          }
        } else {
          return responseHandler.onFailure(statusCode, response.getStatusLine().getReasonPhrase());
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error trying to make connection during delete request", e);
      return responseHandler.onException(e);
    }
  }

  public static abstract class SimpleMessageEmbedResponseHandler implements ResponseHandler<ChatClientResponse> {
    public SimpleMessageEmbedResponseHandler(ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder) {
      this.chatClientResponseBuilder = chatClientResponseBuilder;
    }

    @Override
    public List<ChatClientResponse> onFailure(int statusCode, String reason) {
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Request failed with status code=" + statusCode + ", reason=" + reason));
    }

    @Override
    public List<ChatClientResponse> onException(Exception e) {
      return Arrays.asList(chatClientResponseBuilder.createErrorMessage("Requested failed with exception, e=" + e.getMessage()));
    }
    private ChatClientResponseBuilder<? extends ChatClientResponse> chatClientResponseBuilder;
  }

  //TODO: add ability to return any object (instead of just a list)
  public static abstract class SimpleEntityResponseHandler<T> implements ResponseHandler<T> {

    @Override
    public List<T> onFailure(int statusCode, String reason) {
      return Collections.emptyList();
    }

    @Override
    public List<T> onException(Exception e) {
      return Collections.emptyList();
    }
  }

  public static interface ResponseHandler<T> {
    List<T> onSuccess(String response) throws Exception;

    List<T> onFailure(int statusCode, String reason);

    List<T> onException(Exception e);
  }

  private static final Logger LOGGER = LogManager.getLogger();
}
