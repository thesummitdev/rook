package dev.thesummit.rook.database;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseListField {

  // The string that separates items in the list.
  public String seperator() default " ";
}
