package com.botdarr.utilities;

import java.util.List;

public class ListUtils {
  public static <T> List<T> subList(List<T> responses, int max) {
    return responses.subList(0, responses.size() > max ? max : responses.size());
  }
}
