package dev.thesummit.rook.database;

import java.util.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cursor object for denoting a position & direction of {@link PagedResults} in the wider
 * result set.
 */
public class PagedCursor {
  private static Logger log = LoggerFactory.getLogger(PagedCursor.class);
  private static final String PREFIX_PREV = "prev__";
  private static final String PREFIX_NEXT = "next__";

  private Integer cursorPrev;
  private Integer cursorNext;
  private String tokenPrev;
  private String tokenNext;

  /**
   * Attempts to create a cursor from a Base64 encoded string.
   *
   * @param  source A {@link Base64} encoded cursor string.
   * @return cursor Either the parsed cursor, or null if a cursor could not be parsed.
   */
  public static PagedCursor parse(String source) {
    String tokenPrev = null;
    String tokenNext = null;

    String decodedSource;
    try {
      decodedSource = new String(Base64.getDecoder().decode(source));
    } catch (IllegalArgumentException e) {
      log.debug(String.format("Unable to parse cursor %s", source), e);
      return null;
    }

    // Look for the leading prefixes that signify what direction this token is for.
    if (decodedSource.contains(PREFIX_PREV)) {
      tokenPrev = decodedSource;
    } else if (decodedSource.contains(PREFIX_NEXT)) {
      tokenNext = decodedSource;
    }

    return new PagedCursor(tokenPrev, tokenNext);
  }

  /**
   * Assembles a cursor object from a previous and next decoded token.
   *
   * @param tokenPrev : Should be a decoded token such as prev__{token}.
   * @param tokenNext : Should be a decoded token such as next__{token}.
   */
  public PagedCursor(String tokenPrev, String tokenNext) {
    this.tokenPrev = tokenPrev;
    this.tokenNext = tokenNext;
    if (tokenPrev != null) {
      this.cursorPrev = parseCursorFromDecodedToken(tokenPrev);
    }
    if (tokenNext != null) {
      this.cursorNext = parseCursorFromDecodedToken(tokenNext);
    }
  }

  /**
   * Assembles a cursor object from previous and next cursors.
   *
   * @param cursorPrev : Should be a row cursor, generally an {@link Integer} id.
   * @param cursorNext : Should be a row cursor, generally an {@link Integer} id.
   */
  public PagedCursor(Integer cursorPrev, Integer cursorNext) {
    this.cursorPrev = cursorPrev;
    this.cursorNext = cursorNext;

    if (cursorPrev != null) {
      this.tokenPrev = Base64.getEncoder().encodeToString(
          String.format("%s%d", PREFIX_PREV, cursorPrev).getBytes());
    }

    if (cursorNext != null) {
      this.tokenNext = Base64.getEncoder().encodeToString(
          String.format("%s%d", PREFIX_NEXT, cursorNext).getBytes());
    }
  }

  /**
   * Gets the cursor's {@link Base64} encoded previous token.
   *
   * @return  the Base64 encoded previous token.
   */
  public String getPrevToken() {
    return tokenPrev;
  }

  /**
   * Gets the cursor's {@link Base64} encoded next token.
   *
   * @return  the Base64 encoded next token.
   */
  public String getNextToken() {
    return tokenNext;
  }

  /**
   * Gets this cursor's previous page cursor.
   *
   * @return  the cursor for the previous page.
   */
  public Integer getPrevCursor() {
    return cursorPrev;
  }

  /**
   * Gets this cursor's next page cursor.
   *
   * @return  the cursor for the next page.
   */
  public Integer getNextCursor() {
    return cursorNext;
  }

  /**
   * If the cursor has a previous page.
   *
   * @return  whether this cursor has a previous page.
   */
  public boolean hasPrev() {
    return cursorPrev != null;
  }

  /**
   * If the cursor has a next page.
   *
   * @return  whether this cursor has a next page.
   */
  public boolean hasNext() {
    return cursorNext != null;
  }

  /**
   * Parses the {@link Integer} cursor from a decoded Base64 token.
   *
   * <p>Assumes the token is in the expected format with the correct prefix to identify whether it 
   * is a previous or next token.
   *
   * @return  The parsed cursor.
   * @throws  IllegalArgumentException if a cursor cannot be parsed.
   * @see     PREFIX_PREV
   * @see     PREFIX_NEXT
   *
   */
  private Integer parseCursorFromDecodedToken(String token) throws IllegalArgumentException {
    if (token.contains(PREFIX_PREV)) {
      try {
        return Integer.parseInt(token.replace(PREFIX_PREV, ""));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "Found previous cursor, however, it could not be parsed.", e);
      }
    } else if (token.contains(PREFIX_NEXT)) {
      try {
        return Integer.parseInt(token.replace(PREFIX_NEXT, ""));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "Found next cursor, however, it could not be parsed.", e);
      }
    }

    return null;
  }

  /**
   * Converts the {@link PagedCursor} into a {@link JSONObject}.
   *
   * <p>This does not include all data, only the Base64 encoded tokens are included in the resulting
   * JSONObject.
   *
   * @return  The paged cursor as a JSONObject
   * @see     JSONObject
   */
  public JSONObject toJsonObject() {
    return new JSONObject().put("next", tokenNext).put("prev", tokenPrev);
  }

  @Override
  public String toString() {
    return String.format("prev_token: %s, prev_cursor: %d, next_token: %s, next_cursor: %d",
        tokenPrev, cursorPrev, tokenNext, cursorNext);
  }
}
