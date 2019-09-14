package com.botdar.scheduling;

import com.botdar.Api;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Scheduler {
  public static Scheduler getScheduler() {
    if (instance == null) {
      synchronized (Scheduler .class) {
        if (instance == null) {
          instance = new Scheduler();
        }
      }
    }
    return instance;
  }


  public void initApiNotifications(List<Api> apis, JDA jda) {
    if (notificationFuture == null) {
      notificationFuture = Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {
        @Override
        public void run() {
          try {
            for (Api api : apis) {
              api.sendNotifications(jda);
            }
          } catch (InsufficientPermissionException e) {
            //TODO: debug log this
          } catch (Throwable e) {
            //TODO: log elsewhere
            e.printStackTrace();
          }
        }
      }, 1, 10, TimeUnit.MINUTES);
    }
  }

  public void initApiCaching(List<Api> apis, JDA jda) {
    //cache initially
    for (Api api : apis) {
      api.cacheData(jda);
    }

    //then cache on a schedule
    if (cacheFuture == null) {
      cacheFuture = Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {
        @Override
        public void run() {
          try {
            for (Api api : apis) {
              api.cacheData(jda);
            }
          } catch (Throwable e) {
            //TODO: log elsewhere
            e.printStackTrace();
          }
        }
      }, 0, 10, TimeUnit.MINUTES);
    }
  }

  private ScheduledFuture notificationFuture;
  private ScheduledFuture cacheFuture;
  private static volatile Scheduler instance;
}
