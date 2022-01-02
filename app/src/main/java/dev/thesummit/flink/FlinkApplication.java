package dev.thesummit.flink;

import static io.javalin.apibuilder.ApiBuilder.*;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.thesummit.flink.auth.AuthModule;
import dev.thesummit.flink.database.DatabaseModule;
import dev.thesummit.flink.handlers.AuthHandler;
import dev.thesummit.flink.handlers.LinkHandler;
import dev.thesummit.flink.handlers.PreferenceHandler;
import dev.thesummit.flink.handlers.TagHandler;
import dev.thesummit.flink.handlers.UserHandler;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import java.io.IOException;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkApplication {

  private static Logger log = LoggerFactory.getLogger(FlinkApplication.class);

  public static void main(String[] args) throws IOException {

    // Set application time zone to UTC to match the database.
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    Injector injector = Guice.createInjector(new DatabaseModule(), new AuthModule());

    Javalin app =
        Javalin.create(
            config -> {
              config.enableDevLogging();
              config.addStaticFiles("web", Location.CLASSPATH);
              config.addStaticFiles("assets", Location.CLASSPATH);
            });

    // Protected routes that require a User to be logged in and pass a bearer token.
    app.before("/links", injector.getInstance(AuthHandler.class)::fetchUserContext);
    app.before("/links/*", injector.getInstance(AuthHandler.class)::fetchUserContext);
    app.before("/tags", injector.getInstance(AuthHandler.class)::fetchUserContext);
    app.before("/prefs", injector.getInstance(AuthHandler.class)::fetchUserContext);

    // Other Routes
    app.routes(
        () -> {
          path(
              "login",
              () -> {
                post(injector.getInstance(AuthHandler.class)::login);
              });
          path(
              "users",
              () -> {
                put(injector.getInstance(UserHandler.class)::create);
              });

          // Link Entity
          path(
              "links",
              () -> {
                post(injector.getInstance(LinkHandler.class)::getAll);
                put(injector.getInstance(LinkHandler.class)::create);
                path(
                    "<id>",
                    () -> {
                      get(injector.getInstance(LinkHandler.class)::getOne);
                      patch(injector.getInstance(LinkHandler.class)::update);
                      delete(injector.getInstance(LinkHandler.class)::delete);
                    });
              });

          path(
              "tags",
              () -> {
                get(injector.getInstance(TagHandler.class)::getAll);
              });

          path(
              "prefs",
              () -> {
                get(injector.getInstance(PreferenceHandler.class)::getAll);
              });
        });

    app.start(8000); // Start listening for http requests.
    log.info("Flink server successfully started.");
  }
}
