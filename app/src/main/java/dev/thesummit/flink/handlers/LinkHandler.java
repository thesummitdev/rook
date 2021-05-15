package dev.thesummit.flink.handlers;

import dev.thesummit.flink.FlinkApplication;
import dev.thesummit.flink.models.Link;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LinkHandler implements CrudHandler {

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

    try {
      Connection conn = ctx.use(Connection.class);
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * from links;");
      while (rs.next()) {
        Link l = Link.fromResultSet(rs);
        arr.put(l.toJSONObject());
      }

    } catch (SQLException e) {
      System.out.println(e);
    }

    String response = arr.toString();

    ctx.status(200);
    ctx.contentType("application/json");
    ctx.result(response);
  }

  /** Request handler for GET ${host}/links/{id} */
  @Override
  public void getOne(Context ctx, String resourceId) {
    Link l = null;
    try {
      Connection conn = ctx.use(Connection.class);
      // TODO
      // https://stackoverflow.com/questions/46433459/postgres-select-where-the-where-is-uuid-or-string
      PreparedStatement statement = conn.prepareStatement("SELECT * from links WHERE id::text = ?");
      statement.setString(1, resourceId);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        l = Link.fromResultSet(rs);
      }
      statement.close();
    } catch (SQLException e) {
      System.out.println(e);
    }

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

    try {
      Connection conn = ctx.use(Connection.class);
      PreparedStatement statement =
          conn.prepareStatement(
              "INSERT INTO links (url, tags, unread) VALUES (?,?,?) RETURNING *;");
      statement.setString(1, body.getString("url"));
      statement.setString(2, body.getString("tags"));
      statement.setBoolean(3, body.getBoolean("unread"));
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        l = Link.fromResultSet(rs);
      }
    } catch (SQLException e) {

    }

    ctx.status(200);
    ctx.contentType("application/json");
    ctx.result(l.toJSONObject().toString());
  }

  /** Request handler for PATCH ${host}/links/{id} */
  @Override
  public void update(Context ctx, String resourceId) {
    String UPDATE_QUERY = "UPDATE links SET url=?, tags=?, unread=? WHERE id::text=? RETURNING *;";

    Link l = null;
    JSONObject body = null;

    try {
      body = new JSONObject(ctx.body());

    } catch (JSONException e) {
      throw new BadRequestResponse("Unable to parse JSON payload");
    }

    try {
      Connection conn = ctx.use(Connection.class);
      PreparedStatement statement = conn.prepareStatement(UPDATE_QUERY);
      statement.setString(1, body.getString("url"));
      statement.setString(2, body.getString("tags"));
      statement.setBoolean(3, body.getBoolean("unread"));
      statement.setString(4, resourceId);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        l = Link.fromResultSet(rs);
      }
    } catch (SQLException e) {

    }

    ctx.status(200);
    ctx.result(l.toJSONObject().toString());
  }

  /** Request handler for DELETE ${host}/links/{id} */
  @Override
  public void delete(Context ctx, String resourceId) {

    try {
      Connection conn = ctx.use(Connection.class);

      PreparedStatement checkIfExists =
          conn.prepareStatement("SELECT COUNT(id) FROM LINKS where id::text = ?;");
      checkIfExists.setString(1, resourceId);
      ResultSet rs = checkIfExists.executeQuery();
      rs.next();
      int count = rs.getInt(1);
      boolean recordFound = count > 0;

      if (recordFound) {
        PreparedStatement statement =
            conn.prepareStatement("DELETE FROM LINKS where id::text = ?;");
        statement.setString(1, resourceId);
        statement.execute();
      } else {
        ctx.status(404);
        ctx.result(String.format("Link with id %s does not exist.", resourceId));
        return;
      }

    } catch (SQLException e) {
    }

    ctx.status(200);
  }
}
