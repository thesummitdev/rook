package dev.thesummit.rook.database;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An interface that describes the a corresponding database column for the relevant model.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseField {

  /**
   * The SQL operator to use when this field is compared in a WHERE clause.
   */
  public String whereOperator() default " = ";

  /**
   * A string that wraps the value when the field is compared in a WHERE clause.
   * i.e this could be set to "%" to wrap a value such as: <code>LIKE "%value%"</code>
   */
  public String valueWrapper() default "";

  /**
   * If the field should be considered the unique ID of the database row.
   */
  public boolean isId() default false;

  /**
   * If the field is a unique identifier for the row.
   * (For tables without a primary key ID, for instance.)
   */
  public boolean isIdentifier() default false;

  /**
   * If this field is automatically set by the database and should not be manually set.
   */
  public boolean isSetByDatabase() default false;

  /**
   * If this field should be used as the default sortable column for the table.
   */
  public boolean orderBy() default false;
}
