package com.botdar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;

public class RadarrApi implements Api {
  @Override
  public MessageEmbed lookup(String search) {
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpGet get = new HttpGet(getApiUrl("movie/lookup") + "&term=" + URLEncoder.encode(search, "UTF-8"));
      try (CloseableHttpResponse response = client.execute(get)) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
          JsonParser parser = new JsonParser();
          JsonArray json = parser.parse(EntityUtils.toString(response.getEntity())).getAsJsonArray();
          for (int i = 0; i < json.size(); i++) {
            JsonObject jsonObject = json.get(i).getAsJsonObject();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(jsonObject.get("title").getAsString());
            embedBuilder.setImage(jsonObject.get("remotePoster").getAsString());
            return embedBuilder.build();
          }
        } else {
          //TODO: impl
          throw new RuntimeException("Could not lookup movie, status-code=" + statusCode + ", reason=" + response.getStatusLine().getReasonPhrase());
        }
      }
    } catch (IOException e) {
      //TODO: impl
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public String getApiUrl(String path) {
    return Config.getProperty(Config.Constants.RADARR_URL) + "/api/" + path + "?apikey=" + Config.getProperty(Config.Constants.RADARR_TOKEN);
  }
}
