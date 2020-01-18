package com.botdar;

public class Context {
  public static ContextConfig getConfig() {
    if (contextConfigThreadLocal == null) {
      contextConfigThreadLocal = new ThreadLocal<>();
      contextConfigThreadLocal.set(new ContextConfig());
    }
    if (contextConfigThreadLocal.get() == null) {
      contextConfigThreadLocal.set(new ContextConfig());
    }
    return contextConfigThreadLocal.get();
  }

  public static void reset() {
    contextConfigThreadLocal = null;
  }

  public static class ContextConfig {
    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    private String username;
  }
  private static ThreadLocal<ContextConfig> contextConfigThreadLocal;
}
