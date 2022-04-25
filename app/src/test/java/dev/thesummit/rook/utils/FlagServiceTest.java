package dev.thesummit.rook.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class FlagServiceTest {

  private FlagService service;

  @Test
  public void parseEmptyArgs() {

    String[] testArgs = {};

    service = new FlagService(testArgs);

    Flag dbResetFlag = service.defineFlag("dbreset", false);
    assertFalse(service.flagExists("dbreset"));
    assertFalse(dbResetFlag.isEnabled());
    assertFalse(service.getOrDefault("dbreset", false));
  }

  @Test
  public void parseSingleArg() {

    String[] testArgs = {"-dbreset=true"};

    service = new FlagService(testArgs);

    Flag dbResetFlag = service.defineFlag("dbreset", false);
    assertTrue(service.flagExists("dbreset"));
    assertTrue(dbResetFlag.isEnabled());
    assertTrue(service.getOrDefault("dbreset", false));
  }

  @Test
  public void parseMultipleArgs() {

    String[] testArgs = {"-dbreset=true", "-d", "-foo", "-bar=baz"};

    service = new FlagService(testArgs);

    Flag dbResetFlag = service.defineFlag("dbreset", false);
    assertTrue(service.flagExists("dbreset"));
    assertTrue(dbResetFlag.isEnabled());
    assertTrue(service.getOrDefault("dbreset", false));

    Flag dFlag = service.defineFlag("d", false);
    assertTrue(service.flagExists("d"));
    assertTrue(dFlag.isEnabled());
    assertTrue(service.getOrDefault("d", false));

    Flag fooFlag = service.defineFlag("foo", false);
    assertTrue(service.flagExists("foo"));
    assertTrue(fooFlag.isEnabled());
    assertTrue(service.getOrDefault("foo", false));

    Flag barFlag = service.defineFlag("bar", false);
    assertTrue(service.flagExists("bar"));
    assertFalse(barFlag.isEnabled());
    assertFalse(service.getOrDefault("bar", false));

    Flag unknownFlag = service.defineFlag("unknown", true);
    assertFalse(service.flagExists("unknown"));
    assertTrue(unknownFlag.isEnabled());
    assertFalse(service.getOrDefault("unknown", false));
  }

  @Test
  public void unknownFlags() {

    String[] testArgs = {"-dbreset=true"};
    service = new FlagService(testArgs);

    Flag unknownFlag = service.defineFlag("unknown", false);
    assertFalse(service.flagExists("unknown"));
    assertFalse(unknownFlag.isEnabled());
    assertFalse(service.getOrDefault("unknown", false));
  }
}
