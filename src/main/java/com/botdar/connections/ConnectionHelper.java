package com.botdar.connections;

import com.botdar.Api;
import com.botdar.Config;
import com.botdar.discord.EmbedHelper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

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
            //TODO: log error to some logger
            return responseHandler.onException(e);
          }
        } else {
          return responseHandler.onFailure(statusCode, response.getStatusLine().getReasonPhrase());
        }
      }
    } catch (IOException e) {
      //TODO: log error to some logger
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
            //TODO: log error to some logger
            return responseHandler.onException(e);
          }
        } else {
          return responseHandler.onFailure(statusCode, response.getStatusLine().getReasonPhrase());
        }
      }
    } catch (IOException e) {
      //TODO: log error to some logger
      return responseHandler.onException(e);
    }
  }

  public static abstract class SimpleMessageEmbedResponseHandler implements ResponseHandler<MessageEmbed> {
    @Override
    public List<MessageEmbed> onFailure(int statusCode, String reason) {
      return Arrays.asList(EmbedHelper.createErrorMessage("Request failed with status code=" + statusCode + ", reason=" + reason));
    }

    @Override
    public List<MessageEmbed> onException(Exception e) {
      return Arrays.asList(EmbedHelper.createErrorMessage("Requested failed with exception, e=" + e.getMessage()));
    }
  }

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
}
