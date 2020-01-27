package com.botdarr;

import com.botdarr.commands.Command;

import java.util.List;

public interface Application {
  void init(List<Command> commands) throws Exception;
}
