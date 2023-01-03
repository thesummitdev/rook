package dev.thesummit.rook.database;

import java.util.List;

/**
 * A wrapper object that contains the paged results of a {@link PageableBaseModel} entity and the 
 * cursor page data for the current page.
 *
 * @see PageableBaseModel
 * @see PagedCursor
 * @see DatabaseService#getAllPaged(Class, java.util.Map)
 */
public class PagedResults<T> {
  private PagedCursor cursor;
  private List<T> items;

  /**
   * Assembles a wrapper object containing the resulting items and cursor page data.
   *
   * @param items  the {@link PageableBaseModel} results.
   * @param cursor the page data for the current result set.
   */
  public PagedResults(List<T> items, PagedCursor cursor) {
    this.items = items;
    this.cursor = cursor;
  }

  /**
   * Gets the list of items in this result set.
   *
   * @return the list of items in this result set.
   */
  public List<T> getItems() {
    return items;
  }

  /**
   * Gets the corresponding PagedCursor for this result set.
   *
   * @return the corresponding PagedCursor for this result set.
   */
  public PagedCursor getCursor() {
    return cursor;
  }
}
