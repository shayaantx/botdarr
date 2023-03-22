package com.botdarr.scheduling;

import com.botdarr.api.Api;
import com.botdarr.clients.ChatClient;
import com.botdarr.clients.ChatClientResponse;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.commands.responses.CommandResponse;

import src.main.java.com.botdarr.api.ApiRequests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class Scheduler {
  public static Scheduler getScheduler() {
    if (instance == null) {
      synchronized (Scheduler.class) {
        if (instance == null) {
          instance = new Scheduler();
        }
      }
    }
    return instance;
  }


  public <T extends ChatClientResponse> void initApiNotifications(List<Api> apis, ChatClient<T> chatClient, ChatClientResponseBuilder<T> responseBuilder) {
    if (notificationFuture == null) {
      notificationFuture = Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
        try {
          for (Api api : apis) {
            List<CommandResponse> downloads = api.downloads();
            for (CommandResponse commandResponse : downloads) {
              T response = commandResponse.convertToChatClientResponse(responseBuilder);
              chatClient.sendToConfiguredChannels(Collections.singletonList(response));
            }
          }
        } catch (Throwable e) {
          LOGGER.error("Error during api notification", e);
        }
      }, 0, NOTIFICATION_INTERVAL, TimeUnit.MINUTES);
    }
  }

  public void initApiCaching(List<Api> apis) {
    //cache initially
    for (Api api : apis) {
      api.cacheData();
    }

    //then cache on a schedule
    if (cacheFuture == null) {
      cacheFuture = Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
        try {
          for (Api api : apis) {
            api.cacheData();
          }
        } catch (Throwable e) {
          LOGGER.error("Error during api cache", e);
        }
      }, 0, 2, TimeUnit.MINUTES);
    }
  }

  public void executeCommand(Callable callable) {
    if (commandThreadPool == null) {
      commandThreadPool = Executors.newFixedThreadPool(10);
    }
    commandThreadPool.submit(callable);
  }

  private ScheduledFuture notificationFuture;
  private ScheduledFuture cacheFuture;
  private ExecutorService commandThreadPool;
  private static volatile Scheduler instance;
  private static final Logger LOGGER = LogManager.getLogger();
  private static final int NOTIFICATION_INTERVAL = new ApiRequests().getNotificationInterval();
}
