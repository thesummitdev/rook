package dev.thesummit.rook;

import static io.javalin.apibuilder.ApiBuilder.*;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.thesummit.rook.auth.AuthModule;
import dev.thesummit.rook.database.DatabaseModule;
import dev.thesummit.rook.database.PostgresSchemaManager;
import dev.thesummit.rook.handlers.AuthHandler;
import dev.thesummit.rook.handlers.LinkHandler;
import dev.thesummit.rook.handlers.PreferenceHandler;
import dev.thesummit.rook.handlers.TagHandler;
import dev.thesummit.rook.handlers.UserHandler;
import dev.thesummit.rook.utils.FlagModule;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import java.io.IOException;
import java.util.Map;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RookApplication {

  private static Logger log = LoggerFactory.getLogger(RookApplication.class);
  private static Map<String, String> ENV = System.getenv();
  private static Integer PORT = Integer.parseInt(ENV.getOrDefault("ROOK_PORT", "8000"));

  public static void main(String[] args) throws IOException {

    // Set application time zone to UTC to match the database.
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    Injector injector =
        Guice.createInjector(new FlagModule(args), new DatabaseModule(), new AuthModule());

    // Verify & Check for database updates before starting the server.
    injector.getInstance(PostgresSchemaManager.class).verifySchema();

    Javalin app =
        Javalin.create(
            config -> {
              // config.enableDevLogging();
              config.addStaticFiles("web", Location.CLASSPATH);
              config.addStaticFiles("assets", Location.CLASSPATH);
              config.addSinglePageRoot("/", "web/index.html");
            });

    // Protected routes that require a User to be logged in and pass a bearer token.
    app.before("/links", injector.getInstance(AuthHandler.class)::requireUserContext);
    app.before("/links/*", injector.getInstance(AuthHandler.class)::requireUserContext);
    app.before("/tags", injector.getInstance(AuthHandler.class)::requireUserContext);

    // Routes that have optional user context handling, but don't require authorization.
    app.before("/prefs", injector.getInstance(AuthHandler.class)::optionalUserContext);

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
                put(injector.getInstance(PreferenceHandler.class)::create);
              });
        });

    app.start(RookApplication.PORT); // Start listening for http requests.
    log.info("Rook server successfully started.");
  }
}
