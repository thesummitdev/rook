package dev.thesummit.flink.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import dev.thesummit.flink.database.DatabaseService;
import dev.thesummit.flink.models.Link;
import dev.thesummit.flink.models.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkHandler {

  private static Logger log = LoggerFactory.getLogger(UserHandler.class);
  private DatabaseService dbService;

  @Inject
  public LinkHandler(DatabaseService dbService) {
    this.dbService = dbService;
  }

  /** Request handler for POST ${host}/links/ */
  public void getAll(Context ctx) {

    User user = ctx.sessionAttribute("current_user");
    JSONArray arr = new JSONArray();

    try {
      @SuppressWarnings("unchecked") // TODO validate the incoming json body
      HashMap<String, Object> params = new ObjectMapper().readValue(ctx.body(), HashMap.class);
      params.put("userId", user.id); // Scope the search to Links the user owns.

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
  public void getOne(Context ctx) {

    String resourceId = ctx.pathParam("id");

    try {
      User user = ctx.sessionAttribute("current_user");
      Link link = this.dbService.get(Link.class, UUID.fromString(resourceId));

      if (link == null || !user.id.equals(link.userId)) {
        throw new NotFoundResponse(String.format("Link with id %s was not found.", resourceId));
      }

      ctx.status(200);
      ctx.contentType("application/json");
      ctx.result(link.toJSONObject().toString());

    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse(
          String.format("Bad Request: %s is not a valid UUID.", resourceId));
    }
  }

  /** Request handler for PUT ${host}/links/ */
  public void create(Context ctx) {

    User user = ctx.sessionAttribute("current_user");

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

    try {
      // Required Fields
      String title = body.getString("title");
      String url = body.getString("url");
      // Optional Fields
      String tags = body.optString("tags", "");
      l = new Link(title, url, tags, user.getId());
    } catch (JSONException e) {
      throw new BadRequestResponse("Bad Request: Could not parse Link object from request body.");
    }

    // Validate the link before committing to database.
    if (!l.isValid()) {
      throw new BadRequestResponse("Invalid link parameters");
    }

    this.dbService.put(l);

    if (l.getId() != null) {
      ctx.status(200);
      ctx.contentType("application/json");
      ctx.result(l.toJSONObject().toString());
    } else {
      log.debug("New link failed to recieve an ID from the database. Creation failed.");
      throw new InternalServerErrorResponse("Failed to create link, unknown error.");
    }
  }

  /** Request handler for PATCH ${host}/links/{id} */
  public void update(Context ctx) {

    User user = ctx.sessionAttribute("current_user");
    String resourceId = ctx.pathParam("id");

    Link link = this.dbService.get(Link.class, UUID.fromString(resourceId));

    if (link == null || user.id.equals(link.userId)) {
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

    String newTitle = null;
    String newUrl = null;
    String newTags = null;

    try {
      if (body.has("title")) {
        newTitle = body.getString("title");
        link.title = newTitle;
      }
      if (body.has("url")) {
        newUrl = body.getString("url");
        link.url = newUrl;
      }
      if (body.has("tags")) {
        newTags = body.getString("tags");
        link.tags = newTags;
      }
    } catch (JSONException e) {
      ctx.status(400);
      ctx.result(e.toString());
      return;
    }

    // Validate the link before committing to database.
    if (!link.isValid()) {
      throw new BadRequestResponse("Invalid link parameters");
    }

    this.dbService.patch(link);

    ctx.status(200);
    ctx.result(link.toJSONObject().toString());
  }

  /** Request handler for DELETE ${host}/links/{id} */
  public void delete(Context ctx) {

    String resourceId = ctx.pathParam("id");
    Link l = this.dbService.get(Link.class, UUID.fromString(resourceId));

    if (l != null) {
      this.dbService.delete(l);
    }
  }
}
