package dev.thesummit.flink.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thesummit.flink.FlinkApplication;
import dev.thesummit.flink.models.Link;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LinkHandler implements CrudHandler {

  private static Logger log = Logger.getLogger(LinkHandler.class.getName());

  /** Register a database connection with the request context. */
  public static void before(Context ctx) {
    ctx.register(Connection.class, FlinkApplication.getContext().pool.getConnection());
  }

  /** Release the handler's database connection back to the pool. */
  public static void after(Context ctx) {
    Connection conn = ctx.use(Connection.class);
    if (conn instanceof Connection) {
      FlinkApplication.getContext().pool.releaseConnection(conn);
    }
  }

  /** Request handler for GET ${host}/links/ */
  @Override
  public void getAll(Context ctx) {

    JSONArray arr = new JSONArray();

    Connection conn = ctx.use(Connection.class);
    try {
      HashMap<String, Object> params = new ObjectMapper().readValue(ctx.body(), HashMap.class);

      List<Link> lns = Link.getAll(params, conn);
      for (Link l : lns) {
        arr.put(l.toJSONObject());
      }

    } catch (JsonProcessingException e) {
      ctx.status(401);
      return;
    } catch (SQLException e) {
      ctx.status(501);
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
    Connection conn = ctx.use(Connection.class);
    Link l = Link.get(resourceId, conn);

    if (l == null) {
      throw new NotFoundResponse(String.format("Link with id %s was not found.", resourceId));
    }

    ctx.status(200);
    ctx.contentType("application/json");
    ctx.result(l.toJSONObject().toString());
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

    Connection conn = ctx.use(Connection.class);

    if (!body.has("url")) {
      ctx.status(400);
      ctx.result("Missing Required fields: url");
      return;
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
    } catch (JSONException e) {
      ctx.status(400);
      ctx.result(e.toString());
      return;
    }

    try {
      l = new Link(url, tags, unread);
    } catch (JSONException e) {
      ctx.status(400);
      ctx.result("Could not parse Link from request body.");
      return;
    }

    try {

      // Validate the link before committing to database.
      if (!l.isValid()) {
        ctx.status(400);
        ctx.result("Invalid link parameters.");
        return;
      }

      l.put(conn); // Everything looks good, add to database.
    } catch (SQLException e) {
      ctx.status(500);
      ctx.result(String.format("An error occured: %s", e.toString()));
      return;
    }

    ctx.status(200);
    ctx.contentType("application/json");
    ctx.result(l.toJSONObject().toString());
  }

  /** Request handler for PATCH ${host}/links/{id} */
  @Override
  public void update(Context ctx, String resourceId) {

    Connection conn = ctx.use(Connection.class);
    Link l = Link.get(resourceId, conn);

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
        l.setUrl(newUrl);
      }
      if (body.has("tags")) {
        newTags = body.getString("tags");
        l.setTags(newTags);
      }
      if (body.has("unread")) {
        newUnread = body.getBoolean("unread");
        l.setUnread(newUnread);
      }
    } catch (JSONException e) {
      ctx.status(400);
      ctx.result(e.toString());
      return;
    }

    try {
      //
      // Validate the link before committing to database.
      if (!l.isValid()) {
        ctx.status(400);
        ctx.result("Invalid link parameters.");
        return;
      }

      l.patch(conn); // Everything looks good, update the database.
    } catch (SQLException e) {
      ctx.status(500);
      ctx.result(e.toString());
      return;
    }

    ctx.status(200);
    ctx.result(l.toJSONObject().toString());
  }

  /** Request handler for DELETE ${host}/links/{id} */
  @Override
  public void delete(Context ctx, String resourceId) {

    Connection conn = ctx.use(Connection.class);
    Link l = Link.get(resourceId, conn);

    if (l != null) {
      try {
        Link.delete(resourceId, conn); // Link exists, delete it.
      } catch (SQLException e) {
        ctx.status(500);
        ctx.result(e.toString());
        return;
      }
    } else {
      ctx.status(404);
      ctx.result(String.format("Link with id %s was not found.", resourceId));
    }

    ctx.status(200);
  }
}
