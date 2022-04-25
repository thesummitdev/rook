package dev.thesummit.rook.utils;

public class Flag {

  private String name = "";
  private boolean enabled = false;

  public Flag(String name, Boolean enabled) {
    this.name = name;
    this.enabled = enabled;
  }

  public String getName() {
    return name;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
