package dev.thesummit.flink.database;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseField {
  public boolean ignore() default false;

  public boolean isId() default false;

  public String cast() default "";

  public String whereOperator() default " = ";

  public boolean isIdentifier() default false;
}
