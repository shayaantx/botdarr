package com.botdarr.clients.matrix;

import com.botdarr.clients.ChatClientResponse;

import java.util.ArrayList;
import java.util.List;

public class MatrixResponse implements ChatClientResponse {
  public void addContent(String content) {
    this.content.append(content + " </br>");
  }

  public void addRawContent(String content) {
    this.content.append(content);
  }

  public void addImage(String imageUrl) {
    if (imageUrl == null || imageUrl.isEmpty()) {
      return;
    }
    this.imageUrls.add(imageUrl);
  }

  public String getContent() {
    return this.content.toString();
  }

  public List<String> getImageUrls() {
    return imageUrls;
  }

  private List<String> imageUrls = new ArrayList<>();
  private StringBuilder content = new StringBuilder();
}
