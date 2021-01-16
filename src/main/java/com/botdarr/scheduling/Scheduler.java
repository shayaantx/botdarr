package com.botdarr.scheduling;

import com.botdarr.api.Api;
import com.botdarr.clients.ChatClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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


  public void initApiNotifications(List<Api> apis, ChatClient chatClient) {
    if (notificationFuture == null) {
      notificationFuture = Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
        try {
          for (Api api : apis) {
            api.sendPeriodicNotifications(chatClient);
          }
        } catch (Throwable e) {
          LOGGER.error("Error during api notification", e);
        }
      }, 0, 5, TimeUnit.MINUTES);
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
}
