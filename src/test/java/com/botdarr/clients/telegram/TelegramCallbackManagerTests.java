package com.botdarr.clients.telegram;

import com.botdarr.database.DatabaseBootstrap;
import com.botdarr.database.MockedDatabase;
import com.botdarr.utilities.DateWrapper;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.time.LocalDate;

public class TelegramCallbackManagerTests {
    @Before
    public void beforeTests() throws IOException {
        //create temporary database
        new MockedDatabase(temporaryFolder.newFile());
        DatabaseBootstrap.init();
    }

    @Test
    public void saveCallback_savesAndGetsCallbackDataSuccessfully() {
        TelegramCallbackManager telegramCallbackManager = new TelegramCallbackManager();
        String callback = "!movie find new test";
        int result = telegramCallbackManager.saveCallback(callback);
        Assert.assertTrue(result > 0);
        String actualCallback = telegramCallbackManager.getCallback(result);
        Assert.assertEquals(callback, actualCallback);
    }

    @Test(expected = TelegramCallbackManager.TelegramCallbackMissing.class)
    public void deleteCallbacks_removes10DayOldCallbacks() {
        LocalDate fakeNow = LocalDate.now();
        fakeNow = fakeNow.minusDays(11);

        // insert callbacks that are 11 days old
        TelegramCallbackManager telegramCallbackManager = new TelegramCallbackManager(new FakeDateWrapper(fakeNow));
        String callback = "!movie find new test";
        int result = telegramCallbackManager.saveCallback(callback);
        Assert.assertTrue(result > 0);

        // trigger the callback delete logic
        telegramCallbackManager = new TelegramCallbackManager();
        telegramCallbackManager.deleteOldCallbacks();

        // this should throw an exception now
        telegramCallbackManager.getCallback(result);
    }

    private static class FakeDateWrapper extends DateWrapper {
        private LocalDate now;

        public FakeDateWrapper(LocalDate now) {
            this.now = now;
        }

        @Override
        public LocalDate getNow() {
            return this.now;
        }
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
}
