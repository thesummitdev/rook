package dev.thesummit.rook.utils;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class FlagModule extends AbstractModule {

  static FlagService service;

  public FlagModule(String[] args) {
    FlagModule.service = new FlagService(args);
  }

  @Provides()
  static FlagService provideFlagService() {
    return FlagModule.service;
  }
}
