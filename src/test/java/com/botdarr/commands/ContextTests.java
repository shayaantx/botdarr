package com.botdarr.commands;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ContextTests {
  @Test
  public void setUsername_usernameSavedInCurrentThread() {
    CommandContext.getConfig().setUsername("fakeUser1");
    Assert.assertEquals("fakeUser1", CommandContext.getConfig().getUsername());
  }

  @Test
  public void setUsername_usernameSavedInDifferentThread() throws Exception {
    CommandContext.getConfig().setUsername("fakeUser1");
    //confirm fake user 1 exists in thread local
    Assert.assertEquals("fakeUser1", CommandContext.getConfig().getUsername());

    CountDownLatch countDownLatch = new CountDownLatch(1);
    new Thread(() -> {
      //confirm the username in this thread is null since it was set in a previous thread
      Assert.assertNull(CommandContext.getConfig().getUsername());

      //set fake user 2 in this thread and confirm its the fake user 2
      CommandContext.getConfig().setUsername("fakeUser2");
      Assert.assertEquals("fakeUser2", CommandContext.getConfig().getUsername());
      countDownLatch.countDown();
    }).start();

    //wait for test thread to complete (timeout after a minute if the other thread fails for some insane reason)
    countDownLatch.await(1, TimeUnit.MINUTES);

    //validate fake user 1 is still the set user in this thread
    Assert.assertEquals("fakeUser1", CommandContext.getConfig().getUsername());
  }

  @Test
  public void reset_usernameResetAfterContextCleared() {
    CommandContext.getConfig().setUsername("fakeUser1");
    //confirm fake user 1 exists in thread local
    Assert.assertEquals("fakeUser1", CommandContext.getConfig().getUsername());

    //reset the context
    CommandContext.end();

    //verify the username doesn't exist
    Assert.assertNull(CommandContext.getConfig().getUsername());
  }
}
