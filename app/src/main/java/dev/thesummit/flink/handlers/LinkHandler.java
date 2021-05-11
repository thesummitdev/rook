package dev.thesummit.flink.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.thesummit.flink.FlinkApplication;
import dev.thesummit.flink.models.Link;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.json.JSONArray;

public class LinkHandler implements HttpHandler {

  public void handle(HttpExchange t) throws IOException {

    JSONArray arr = new JSONArray();

    try {
      Connection conn = FlinkApplication.getContext().pool.getConnection();
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

    t.sendResponseHeaders(200, response.length());
    OutputStream os = t.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }
}
