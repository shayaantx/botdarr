package com.botdarr.clients.matrix;

import com.botdarr.Config;
import com.botdarr.clients.ChatClient;
import com.botdarr.clients.matrix.transactions.*;
import com.botdarr.clients.matrix.transactions.filter.*;
import com.botdarr.clients.matrix.transactions.sync.MatrixSyncEvent;
import com.botdarr.clients.matrix.transactions.sync.MatrixSyncJoinRoom;
import com.botdarr.clients.matrix.transactions.sync.MatrixSyncResponse;
import com.botdarr.clients.matrix.transactions.sync.MatrixSyncRooms;
import com.botdarr.connections.ConnectionHelper;
import com.google.gson.Gson;
import org.apache.http.client.methods.*;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URLEncoder;
import java.util.*;

public class MatrixChatClient implements ChatClient<MatrixResponse> {
  public MatrixChatClient() {
    if (!isPasswordLoginSupported()) {
      throw new RuntimeException("Password login not supported on homeserver");
    }
    MatrixLoginResponse loginResponse = getAccessToken();
    if (loginResponse == null) {
      throw new RuntimeException("Could not get login response");
    }
    this.accessToken = loginResponse.getAccessToken();
    this.userId = loginResponse.getUserId();
    if (this.accessToken == null || this.accessToken.isEmpty()) {
      throw new RuntimeException("Could not get access token");
    }
    if (this.userId == null || this.userId.isEmpty()) {
      throw new RuntimeException("Could not get user id");
    }
    for (String room : getRooms()) {
      if (!this.joinRoom(room)) {
        throw new RuntimeException("Could not join room " + room);
      }
    }
    this.messageFilterId = this.addMessageFilter();
    if (this.messageFilterId == null || this.messageFilterId.isEmpty()) {
      throw new RuntimeException("Could not add/get matrix filter");
    }
  }

  @Override
  public void sendToConfiguredChannels(List<MatrixResponse> chatClientResponses) {
    for (MatrixResponse response : chatClientResponses) {
      for (String room : getRooms()) {
        sendMatrixResponse(response, room);
      }
    }
  }

  public void sendMessage(MatrixResponse response, String roomId) {
    sendMatrixResponse(response, roomId);
  }

  public void addListener(MatrixMessageListener listener) {
    this.listeners.add(listener);
  }

  public void listen() {
    String nextBatch = null;
    while (true) {
      try {
        //TODO: handle invalid access token
        MatrixSyncResponse syncResponse = sync(nextBatch);
        nextBatch = syncResponse.getNextBatch();
        MatrixSyncRooms syncRooms = syncResponse.getRooms();
        for (Map.Entry<String, MatrixSyncJoinRoom> entry : syncRooms.getJoin().entrySet()) {
          String roomId = entry.getKey();
          MatrixSyncJoinRoom joinRoom = entry.getValue();
          for (MatrixSyncEvent event : joinRoom.getTimeline().getEvents()) {
            if (event.getContent().getMsgtype() != null && event.getContent().getMsgtype().equals("m.text")) {
              for (MatrixMessageListener matrixMessageListener : this.listeners) {
                matrixMessageListener.process(roomId, event.getSender(), event.getContent().getBody());
              }
            }
          }
        }
      } catch (Throwable t) {
        LOGGER.error("Error during event listener", t);
      }
    }
  }

  private List<String> getRooms() {
    String rawRoomsStr = Config.getProperty(Config.Constants.MATRIX_ROOM);
    if (rawRoomsStr.contains(",")) {
      return Arrays.asList(rawRoomsStr.split(","));
    }
    return new ArrayList<String>() {{add(rawRoomsStr);}};
  }

  private MatrixPreviewUrlResponse getPreviewUrl(String url) {
    return ConnectionHelper.makeRequest(() -> new HttpGet(
      Config.getProperty(Config.Constants.MATRIX_HOME_SERVER) +
      Constants.API_PREVIEW_URL +
      "?access_token=" + MatrixChatClient.this.accessToken +
      "&url=" + URLEncoder.encode(url, "UTF-8")), new MatrixResponseHandler<MatrixPreviewUrlResponse>() {
      @Override
      public MatrixPreviewUrlResponse onSuccess(String response) throws Exception {
        return new Gson().fromJson(response, MatrixPreviewUrlResponse.class);
      }
    });
  }

  private void sendMatrixResponse(MatrixResponse response, String roomId) {
    List<String> mxcUrls = new ArrayList<>();
    //image previews urls seem to have sporadic behavior across clients in matrix ecosystem
    //so I just use the preview url api to get images in a format that works fine
    if (!response.getImageUrls().isEmpty()) {
      for (String imageUrl : response.getImageUrls()) {
        MatrixPreviewUrlResponse previewUrlResponse = getPreviewUrl(imageUrl);
        if (previewUrlResponse == null) {
          LOGGER.warn("Could not find image " + imageUrl);
          continue;
        }
        mxcUrls.add(previewUrlResponse.getMxcUri());
      }
    }
    MatrixSendMessageRequest sendMessageRequest = new MatrixSendMessageRequest();
    sendMessageRequest.setBody(response.getContent());
    sendMessage(roomId, sendMessageRequest);
    for (String mxcUrl : mxcUrls) {
      MatrixSendImageRequest sendImageRequest = new MatrixSendImageRequest();
      sendImageRequest.setUrl(mxcUrl);
      sendMessage(roomId, sendImageRequest);
    }
  }

  private <T> void sendMessage(String roomId, T message) {
    MatrixSendMessageResponse response = ConnectionHelper.makeRequest(() -> {
        HttpPost post = new HttpPost(Config.getProperty(Config.Constants.MATRIX_HOME_SERVER) +
          String.format(Constants.API_SEND_MESSAGE, roomId) +
          "?access_token=" + MatrixChatClient.this.accessToken);
        post.setEntity(new StringEntity(new Gson().toJson(message), ContentType.APPLICATION_JSON));
        return post;
      },
      new MatrixResponseHandler<MatrixSendMessageResponse>() {
        @Override
        public MatrixSendMessageResponse onSuccess(String response) throws Exception {
          return new Gson().fromJson(response, MatrixSendMessageResponse.class);
        }
      });
    if (response == null || response.getEventId() == null || response.getEventId().isEmpty()) {
      LOGGER.error("Missing event id for room " + roomId + " and message " + message);
    }
  }

  private String addMessageFilter() {
    return ConnectionHelper.makeRequest(() -> {
      HttpPost post = new HttpPost(Config.getProperty(Config.Constants.MATRIX_HOME_SERVER) +
        String.format(Constants.API_ADD_FILTER, this.userId) + "?access_token=" + this.accessToken);
      MatrixFilterRequest matrixFilterRequest = new MatrixFilterRequest();

      String[] excludeAll = new String[]{"*"};
      String[] rooms = getRooms().toArray(new String[0]);
      MatrixEventFilter accountData = new MatrixEventFilter();
      //don't need account data
      accountData.setNotTypes(excludeAll);
      MatrixEventFilter presenceData = new MatrixEventFilter();
      // no presence data either
      presenceData.setNotTypes(excludeAll);
      MatrixRoomFilter roomFilter = new MatrixRoomFilter();
      MatrixRoomEventFilter accountDataRoom = new MatrixRoomEventFilter();
      //no account data for the room events
      accountDataRoom.setNotTypes(excludeAll);
      MatrixStateFilter stateFilter = new MatrixStateFilter();
      stateFilter.setRooms(rooms);
      //only message events for the room
      stateFilter.setTypes(new String[] {"m.room.message"});
      //ignore events from the bot user itself
      stateFilter.setNotSenders(new String[] {this.userId});
      roomFilter.setAccountData(accountDataRoom);
      roomFilter.setRooms(rooms);
      roomFilter.setState(stateFilter);
      roomFilter.setTimeline(new MatrixRoomEventFilter());

      matrixFilterRequest.setRoom(roomFilter);
      matrixFilterRequest.setPresence(presenceData);
      matrixFilterRequest.setAccountData(accountData);
      matrixFilterRequest.setEventFields(new String[]{"content", "sender", "room_id", "event_id"});

      post.setEntity(new StringEntity(new Gson().toJson(matrixFilterRequest), ContentType.APPLICATION_JSON));
      return post;
    }, new MatrixResponseHandler<String>() {
      @Override
      public String onSuccess(String response) throws Exception {
        MatrixFilterResponse filterResponse =
          new Gson().fromJson(response, MatrixFilterResponse.class);
        return filterResponse.getFilterId();
      }
    });
  }

  private MatrixSyncResponse sync(String nextBatch) {
    return ConnectionHelper.makeRequest(() -> new HttpGet(
      Config.getProperty(Config.Constants.MATRIX_HOME_SERVER) +
        Constants.API_SYNC +
        "?access_token=" + MatrixChatClient.this.accessToken +
        "&timeout=" + POLL_TIMEOUT +
        (nextBatch != null && !nextBatch.isEmpty() ? "&since=" + nextBatch : "") +
        "&filter=" + MatrixChatClient.this.messageFilterId +
        "&set_presence=online"), new MatrixResponseHandler<MatrixSyncResponse>() {
      @Override
      public MatrixSyncResponse onSuccess(String response) throws Exception {
        return new Gson().fromJson(response, MatrixSyncResponse.class);
      }
    });
  }

  private boolean joinRoom(String room) {
    Boolean joinedRoom =  ConnectionHelper.makeRequest(() -> new HttpPost(
      Config.getProperty(Config.Constants.MATRIX_HOME_SERVER) +
        Constants.API_JOIN_ROOM +
        URLEncoder.encode(room, "UTF-8") +
        "/join?access_token=" + MatrixChatClient.this.accessToken), new MatrixResponseHandler<Boolean>() {
      @Override
      public Boolean onSuccess(String response) throws Exception {
        MatrixJoinRoomResponse joinRoomResponse =
          new Gson().fromJson(response, MatrixJoinRoomResponse.class);
        return !joinRoomResponse.getRoomId().isEmpty();
      }
    });
    return joinedRoom != null && joinedRoom;
  }

  private MatrixLoginResponse getAccessToken() {
    return ConnectionHelper.makeRequest(() -> {
      HttpPost post = new HttpPost(Config.getProperty(Config.Constants.MATRIX_HOME_SERVER) + Constants.API_LOGIN);
      MatrixLoginRequest matrixLoginRequest = new MatrixLoginRequest();
      matrixLoginRequest.setUser(Config.getProperty(Config.Constants.MATRIX_USERNAME));
      matrixLoginRequest.setPassword(Config.getProperty(Config.Constants.MATRIX_PASSWORD));
      post.setEntity(new StringEntity(new Gson().toJson(matrixLoginRequest), ContentType.APPLICATION_JSON));
      return post;
    }, new MatrixResponseHandler<MatrixLoginResponse>() {
      @Override
      public MatrixLoginResponse onSuccess(String response) throws Exception {
        return new Gson().fromJson(response, MatrixLoginResponse.class);
      }
    });
  }

  private boolean isPasswordLoginSupported() {
    Boolean isPasswordLoginSupported = ConnectionHelper.makeRequest(() ->
      new HttpGet(Config.getProperty(Config.Constants.MATRIX_HOME_SERVER) + Constants.API_LOGIN), new MatrixResponseHandler<Boolean>() {
      @Override
      public Boolean onSuccess(String response) throws Exception {
        MatrixLoginInformation matrixLoginType =
          new Gson().fromJson(response, MatrixLoginInformation.class);
        for (MatrixLoginFlow flow : matrixLoginType.getFlows()) {
          if (flow.getType().equals("m.login.password")) {
            return true;
          }
        }
        return false;
      }
    });
    return isPasswordLoginSupported != null && isPasswordLoginSupported;
  }

  private static abstract class MatrixResponseHandler<T> implements ConnectionHelper.ResponseHandler<T> {

    @Override
    public T onFailure(int statusCode, String reason) {
      LOGGER.error("Error from matrix response, code=" + statusCode + ", reason=" + reason);
      if (statusCode == 429) {
        LOGGER.error("Rate limited from matrix");
      }
      return null;
    }

    @Override
    public T onException(Exception e) {
      LOGGER.error("Error trying to make matrix request", e);
      return null;
    }

    @Override
    public T onConnectException(HttpHostConnectException e) {
      return onException(e);
    }
  }

  protected static class Constants {
    private static final String API_BASE = "/_matrix/client/r0";
    private static final String API_LOGIN = API_BASE + "/login";
    private static final String API_JOIN_ROOM = API_BASE + "/rooms/";
    private static final String API_SYNC = API_BASE + "/sync";
    /** 1 == user_id */
    private static final String API_ADD_FILTER = API_BASE + "/user/%s/filter";
    /** 1 == room id*/
    private static final String API_SEND_MESSAGE = API_BASE + "/rooms/%s/send/m.room.message";
    private static final String API_PREVIEW_URL = "/_matrix/media/r0/preview_url";
  }

  public interface MatrixMessageListener {
    void process(String roomId, String sender, String content);
  }

  private final List<MatrixMessageListener> listeners = new ArrayList<>();
  private final String messageFilterId;
  private final String accessToken;
  private final String userId;
  private static final int POLL_TIMEOUT = 60000;
  private static final Logger LOGGER = LogManager.getLogger();
}
