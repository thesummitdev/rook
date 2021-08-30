package dev.thesummit.flink;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.thesummit.flink.auth.AuthModule;
import dev.thesummit.flink.database.DatabaseModule;
import dev.thesummit.flink.handlers.AuthHandler;
import dev.thesummit.flink.handlers.LinkHandler;
import dev.thesummit.flink.handlers.UserHandler;
import io.javalin.Javalin;
import io.javalin.apibuilder.*;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkApplication {

  private static Logger log = LoggerFactory.getLogger(FlinkApplication.class);

  public static void main(String[] args) throws IOException {

    Injector injector = Guice.createInjector(new DatabaseModule(), new AuthModule());

    Javalin app =
        Javalin.create(
            config -> {
              config.enableDevLogging();
              config.addStaticFiles("web");
            });

    // Protected routes that require a User to be logged in and pass a bearer token.
    app.before("/links", injector.getInstance(AuthHandler.class)::fetchUserContext);
    app.before("/links/*", injector.getInstance(AuthHandler.class)::fetchUserContext);

    // Other Routes
    app.routes(
        () -> {
          ApiBuilder.path(
              "/login",
              () -> {
                ApiBuilder.post(injector.getInstance(AuthHandler.class)::login);
              });
          ApiBuilder.path(
              "users",
              () -> {
                ApiBuilder.post(injector.getInstance(UserHandler.class)::create);
              });

          // Link Entity
          ApiBuilder.crud("links/:id", injector.getInstance(LinkHandler.class));
        });

    app.start(8000); // Start listening for http requests.
    log.info("Flink server successfully started.");
  }
}
