package dev.thesummit.flink.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import dev.thesummit.flink.database.DatabaseService;
import dev.thesummit.flink.models.Link;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LinkHandler implements CrudHandler {

  private static Logger log = Logger.getLogger(LinkHandler.class.getName());
  private DatabaseService dbService;

  @Inject
  public LinkHandler(DatabaseService dbService) {
    this.dbService = dbService;
  }

  /** Request handler for GET ${host}/links/ */
  @Override
  public void getAll(Context ctx) {

    JSONArray arr = new JSONArray();

    try {
      @SuppressWarnings("unchecked") // TODO validate the incoming json body
      HashMap<String, Object> params = new ObjectMapper().readValue(ctx.body(), HashMap.class);

      List<Link> lns = this.dbService.getAll(Link.class, params);
      for (Link l : lns) {
        arr.put(l.toJSONObject());
      }

    } catch (JsonProcessingException e) {
      ctx.status(401);
      return;
    }

    String response = arr.toString();
    ctx.status(200);
    ctx.contentType("application/json");
    ctx.result(response);
  }

  /** Request handler for GET ${host}/links/{id} */
  @Override
  public void getOne(Context ctx, String resourceId) {

    try {
      Link l = this.dbService.get(Link.class, UUID.fromString(resourceId));

      if (l == null) {
        throw new NotFoundResponse(String.format("Link with id %s was not found.", resourceId));
      }

      ctx.status(200);
      ctx.contentType("application/json");
      ctx.result(l.toJSONObject().toString());

    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse(
          String.format("Bad Request: %s is not a valid UUID.", resourceId));
    }
  }

  /** Request handler for POST ${host}/links/ */
  @Override
  public void create(Context ctx) {

    JSONObject body = null;
    Link l = null;

    try {
      body = new JSONObject(ctx.body());

    } catch (JSONException e) {
      throw new BadRequestResponse("Unable to parse JSON payload");
    }

    if (!body.has("url")) {
      throw new BadRequestResponse("Missing Required fields: url");
    }

    String url = "";
    String tags = "";
    Boolean unread = false;

    try {
      url = body.getString("url");

      if (body.has("tags")) {
        tags = body.getString("tags");
      }
      if (body.has("unread")) {
        unread = body.getBoolean("unread");
      }
      l = new Link(url, tags, unread);
    } catch (JSONException e) {
      throw new BadRequestResponse("Bad Request: Could not parse Link object from request body.");
    }

    // Validate the link before committing to database.
    if (!l.isValid()) {
      throw new BadRequestResponse("Invalid link parameters");
    }

    this.dbService.put(l);

    ctx.status(200);
    ctx.contentType("application/json");
    ctx.result(l.toJSONObject().toString());
  }

  /** Request handler for PATCH ${host}/links/{id} */
  @Override
  public void update(Context ctx, String resourceId) {

    Link l = this.dbService.get(Link.class, UUID.fromString(resourceId));

    if (l == null) {
      ctx.status(404);
      ctx.result("Link not found.");
      return;
    }

    JSONObject body = null;

    try {
      body = new JSONObject(ctx.body());
    } catch (JSONException e) {
      throw new BadRequestResponse("Unable to parse JSON payload");
    }

    String newUrl = null;
    String newTags = null;
    Boolean newUnread = null;

    try {
      if (body.has("url")) {
        newUrl = body.getString("url");
        l.url = newUrl;
      }
      if (body.has("tags")) {
        newTags = body.getString("tags");
        l.tags = newTags;
      }
      if (body.has("unread")) {
        newUnread = body.getBoolean("unread");
        l.unread = newUnread;
      }
    } catch (JSONException e) {
      ctx.status(400);
      ctx.result(e.toString());
      return;
    }

    // Validate the link before committing to database.
    if (!l.isValid()) {
      throw new BadRequestResponse("Invalid link parameters");
    }

    this.dbService.patch(l);

    ctx.status(200);
    ctx.result(l.toJSONObject().toString());
  }

  /** Request handler for DELETE ${host}/links/{id} */
  @Override
  public void delete(Context ctx, String resourceId) {

    Link l = this.dbService.get(Link.class, UUID.fromString(resourceId));

    if (l != null) {
      this.dbService.delete(l);
    }
  }
}
