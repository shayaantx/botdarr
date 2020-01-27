package com.botdarr.commands;

public class CommandContext {
  public static CommandContextConfig getConfig() {
    if (contextConfigThreadLocal == null) {
      contextConfigThreadLocal = new ThreadLocal<>();
      contextConfigThreadLocal.set(new CommandContextConfig());
    }
    if (contextConfigThreadLocal.get() == null) {
      contextConfigThreadLocal.set(new CommandContextConfig());
    }
    return contextConfigThreadLocal.get();
  }

  public static void end() {
    contextConfigThreadLocal = null;
  }

  public static CommandContextConfig start() {
    return getConfig();
  }

  public static class CommandContextConfig {
    public String getUsername() {
      return this.username;
    }

    public CommandContextConfig setUsername(String username) {
      this.username = username;
      return this;
    }
    private String username;
  }
  private static ThreadLocal<CommandContextConfig> contextConfigThreadLocal;
}
