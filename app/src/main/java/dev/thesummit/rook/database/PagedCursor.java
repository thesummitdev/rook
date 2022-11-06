package dev.thesummit.rook.database;

import java.sql.Timestamp;
import java.util.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PagedCursor {
  private static Logger log = LoggerFactory.getLogger(PagedCursor.class);
  private static final String PREFIX_PREV = "prev__";
  private static final String PREFIX_NEXT = "next__";

  private Integer cursorPrev;
  private Integer cursorNext;
  private String tokenPrev;
  private String tokenNext;

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

  public String getPrevToken() {
    return tokenPrev;
  }

  public String getNextToken() {
    return tokenNext;
  }

  public Integer getPrevCursor() {
    return cursorPrev;
  }

  public Integer getNextCursor() {
    return cursorNext;
  }

  public boolean hasPrev() {
    return cursorPrev != null;
  }

  public boolean hasNext() {
    return cursorNext != null;
  }

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

  public JSONObject toJSONObject() {
    return new JSONObject().put("next", tokenNext).put("prev", tokenPrev);
  }

  @Override
  public String toString() {
    return String.format("prev_token: %s, prev_cursor: %d, next_token: %s, next_cursor: %d",
        tokenPrev, cursorPrev, tokenNext, cursorNext);
  }
}
