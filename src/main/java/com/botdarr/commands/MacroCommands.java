package com.botdarr.commands;

import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MacroCommands {
  public static List<Command> getCommands() {
    return new ArrayList<Command>() {{
      add(new BaseCommand("macros add", "Adds a macro",
        Arrays.asList("new-command", "old-command", "profile")) {
        @Override
        //TODO: add generic input parsing based on BaseCommand definition
        public List<CommandResponse> execute(String command) {
          DatabaseHelper helper = new DatabaseHelper();
          //TODO: parse out input from command
          if (helper.addMacro("", "", "")) {
            //TODO: add success response
            return null;
          }
          //TODO: add failure response
          return null;
        }
      });
      //TODO: add remove command
      //TODO: add list command
    }};
  }
}
