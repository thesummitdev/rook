package dev.thesummit.flink.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.thesummit.flink.FlinkApplication;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LinkHandler implements HttpHandler {

  FlinkApplication app = null;

  public LinkHandler(FlinkApplication app) {
    this.app = app;
  }

  public void handle(HttpExchange t) throws IOException {

    String response = "";
    try {
      Connection conn = this.app.pool.getConnection();
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT * from links;");
      while (rs.next()) {
        System.out.println(rs.getString("url"));
        response = response + rs.getString("url");
      }

    } catch (SQLException e) {
      System.out.println(e);
    }

    t.sendResponseHeaders(200, response.length());
    OutputStream os = t.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }
}
