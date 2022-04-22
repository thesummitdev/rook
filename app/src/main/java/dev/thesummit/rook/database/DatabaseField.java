package dev.thesummit.rook.database;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseField {
  public String cast() default "";

  public String whereOperator() default " = ";

  public boolean isId() default false;

  public boolean isIdentifier() default false;

  public boolean isSetByDatabase() default false;
}
