package com.botdar;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ContextTests {
  @Test
  public void setUsername_usernameSavedInCurrentThread() {
    Context.getConfig().setUsername("fakeUser1");
    Assert.assertEquals("fakeUser1", Context.getConfig().getUsername());
  }

  @Test
  public void setUsername_usernameSavedInDifferentThread() throws Exception {
    Context.getConfig().setUsername("fakeUser1");
    //confirm fake user 1 exists in thread local
    Assert.assertEquals("fakeUser1", Context.getConfig().getUsername());

    CountDownLatch countDownLatch = new CountDownLatch(1);
    new Thread(() -> {
      //confirm the username in this thread is null since it was set in a previous thread
      Assert.assertNull(Context.getConfig().getUsername());

      //set fake user 2 in this thread and confirm its the fake user 2
      Context.getConfig().setUsername("fakeUser2");
      Assert.assertEquals("fakeUser2", Context.getConfig().getUsername());
      countDownLatch.countDown();
    }).start();

    //wait for test thread to complete (timeout after a minute if the other thread fails for some insane reason)
    countDownLatch.await(1, TimeUnit.MINUTES);

    //validate fake user 1 is still the set user in this thread
    Assert.assertEquals("fakeUser1", Context.getConfig().getUsername());
  }

  @Test
  public void reset_usernameResetAfterContextCleared() {
    Context.getConfig().setUsername("fakeUser1");
    //confirm fake user 1 exists in thread local
    Assert.assertEquals("fakeUser1", Context.getConfig().getUsername());

    //reset the context
    Context.reset();

    //verify the username doesn't exist
    Assert.assertNull(Context.getConfig().getUsername());
  }
}
