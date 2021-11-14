package dev.thesummit.flink.database;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseArrayField {
  public String arraySeperator() default ",";

  public String arrayFuncton() default "";

  public String arrayCompareOperator() default "&&";
}
