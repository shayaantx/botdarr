package com.botdarr.clients.telegram;

import com.botdarr.database.DatabaseHelper;
import com.botdarr.utilities.DateWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TelegramCallbackManager {
    private final DateWrapper dateWrapper;

    public TelegramCallbackManager(DateWrapper dateWrapper) {
        this.dateWrapper = dateWrapper;
    }

    public TelegramCallbackManager() {
        this.dateWrapper = new DateWrapper();
    }

    private void deleteCallback(Connection conn, int id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("delete from telegram_callbacks where id = ?");
        statement.setInt(1, id);
        statement.executeUpdate();
    }

    public int saveCallback(String callback) {
        String url = databaseHelper.getJdbcUrl();
        try (Connection conn = DriverManager.getConnection(url)) {
            PreparedStatement statement = conn.prepareStatement("insert into telegram_callbacks (callback, createdDt) values (?, ?)");
            statement.setString(1, callback);
            statement.setDate(2, Date.valueOf(this.dateWrapper.getNow()));
            statement.executeUpdate();
            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error trying to save telegram callback", e);
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Could not save telegram callback");
    }

    public String getCallback(int id) {
        String url = databaseHelper.getJdbcUrl();
        try (Connection conn = DriverManager.getConnection(url)) {
            PreparedStatement statement = conn.prepareStatement("select callback from telegram_callbacks where id = ?");
            statement.setInt(1, id);
            String callback = null;
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    callback = rs.getString("callback");
                }
            }
            if (!Strings.isEmpty(callback)) {
                // delete the callback entry
                deleteCallback(conn, id);
                return callback;
            }
            throw new TelegramCallbackMissing();
        } catch (TelegramCallbackMissing e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error trying to get telegram callback", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteOldCallbacks() {
        LOGGER.info("Checking for telegram callbacks to delete");
        String url = databaseHelper.getJdbcUrl();
        try (Connection conn = DriverManager.getConnection(url)) {
            // if callbacks haven't been used in 10 days, delete them
            PreparedStatement statement = conn.prepareStatement("SELECT id FROM telegram_callbacks WHERE createdDt < ?");
            LocalDate tenDaysAgo = this.dateWrapper.getNow().minusDays(10);
            statement.setDate(1, Date.valueOf(tenDaysAgo));
            List<Integer> rowsToDelete = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    rowsToDelete.add(rs.getInt("id"));
                }
            }
            LOGGER.info("Found telegram callbacks to delete, count=" + rowsToDelete.size());
            for (int rowIdToDelete : rowsToDelete) {
                LOGGER.info("Deleting telegram callback, id=" + rowIdToDelete);
                deleteCallback(conn, rowIdToDelete);
            }
        } catch (Exception e) {
            LOGGER.error("Error trying to delete old telegram callback", e);
            throw new RuntimeException(e);
        }
    }

    public static class TelegramCallbackMissing extends RuntimeException {

    }

    private final DatabaseHelper databaseHelper = new DatabaseHelper();
    Logger LOGGER = LogManager.getLogger();
}
