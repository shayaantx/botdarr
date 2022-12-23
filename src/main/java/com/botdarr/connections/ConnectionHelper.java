package com.botdarr.connections;

import com.botdarr.Config;
import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.ErrorResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ConnectionHelper {
  public static <T> T makeGetRequest(RequestBuilder builder, ResponseHandler<T> responseHandler) {
    return makeRequest(new RequestHandler() {
      @Override
      public HttpRequestBase buildRequest() throws Exception {
        return builder.build();
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
          LOGGER.error("Non 200 status code returned from request, code=" + statusCode + ", reason=" + response.getStatusLine().getReasonPhrase());
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

  public static abstract class SimpleCommandResponseHandler implements ResponseHandler<List<CommandResponse>> {
    @Override
    public List<CommandResponse> onConnectException(HttpHostConnectException e) {
      return onException(e);
    }

    public SimpleCommandResponseHandler() {
    }

    @Override
    public List<CommandResponse> onFailure(int statusCode, String reason) {
      return Collections.singletonList(new ErrorResponse("Request failed with status code=" + statusCode + ", reason=" + reason));
    }

    @Override
    public List<CommandResponse> onException(Exception e) {
      return Collections.singletonList(new ErrorResponse("Requested failed with exception, e=" + e.getMessage() + ",class=" + e.getClass()));
    }
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
