package com.botdarr.commands;

import com.botdarr.Config;
import org.apache.logging.log4j.util.Strings;

public class CommandContext {
  public static CommandContextConfig getConfig() {
    if (contextConfigThreadLocal.get() == null) {
      contextConfigThreadLocal.set(new CommandContextConfig());
    }
    return contextConfigThreadLocal.get();
  }

  public static void end() {
    contextConfigThreadLocal.remove();
  }

  public static CommandContextConfig start() {
    return getConfig();
  }

  public static class CommandContextConfig {
    public String getUsername() {
      return this.username;
    }

    /**
     * @return The command prefix. The prefix can change under various scenarios:
     * 1. If the entry point has a hardcoded prefix (i.e., slash commands)
     * 2. If there is no hardcoded prefix, we just use whatever is configured
     */
    public String getPrefix() {
      if (!Strings.isEmpty(this.prefix)) {
        return this.prefix;
      }
      String configuredPrefix = Config.getProperty(Config.Constants.COMMAND_PREFIX);
      if (!Strings.isEmpty(configuredPrefix)) {
        return configuredPrefix;
      }
      return "!";
    }

    public CommandContextConfig setUsername(String username) {
      this.username = username;
      return this;
    }
    public CommandContextConfig setPrefix(String prefix) {
      this.prefix = prefix;
      return this;
    }
    private String username;
    private String prefix;
  }
  private static ThreadLocal<CommandContextConfig> contextConfigThreadLocal = new ThreadLocal<>();
}
