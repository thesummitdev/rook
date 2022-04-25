package dev.thesummit.rook.utils;

import java.util.HashMap;

public class FlagService {

  private HashMap<String, Boolean> flagValuesMap = new HashMap<String, Boolean>();

  public FlagService(String[] args) {
    parseArgs(args);
  }

  public Flag defineFlag(String name, Boolean defaultValue) {
    return new Flag(name, getOrDefault(name, defaultValue));
  }

  private void parseArgs(String[] args) {

    for (String argument : args) {

      // Remove dashes in argument.
      argument = argument.replace("-", "");

      if (argument.contains("=")) {

        String[] splitArgument = argument.split("=");
        String name = splitArgument[0];
        boolean enabled = Boolean.parseBoolean(splitArgument[1]);

        this.flagValuesMap.put(name, enabled);
      } else {

        // If no explicit boolean provided, assume true since it was passed.
        this.flagValuesMap.put(argument, true);
      }
    }
  }

  public boolean flagExists(String name) {
    return this.flagValuesMap.containsKey(name);
  }

  public boolean getOrDefault(String name, Boolean defaultValue) {
    return this.flagValuesMap.getOrDefault(name, defaultValue);
  }
}
