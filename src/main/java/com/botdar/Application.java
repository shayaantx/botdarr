package com.botdar;

import com.botdar.commands.Command;

import java.util.List;

public interface Application {
  void init(List<Command> commands) throws Exception;
}
