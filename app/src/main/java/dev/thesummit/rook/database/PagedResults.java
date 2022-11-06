package dev.thesummit.rook.database;

import java.util.List;

public class PagedResults<T> {
  public enum CursorDirection { BEFORE, AFTER }
  private PagedCursor cursor;
  private List<T> items;

  public PagedResults(List<T> items, PagedCursor cursor) {
    this.items = items;
    this.cursor = cursor;
  }

  public List<T> getItems() {
    return items;
  }

  public PagedCursor getCursor() {
    return cursor;
  }
}
