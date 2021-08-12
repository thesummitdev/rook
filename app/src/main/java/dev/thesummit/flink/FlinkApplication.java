package dev.thesummit.flink;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.thesummit.flink.database.DatabaseModule;
import dev.thesummit.flink.handlers.LinkHandler;
import io.javalin.Javalin;
import io.javalin.apibuilder.*;
import java.io.IOException;

public class FlinkApplication {

  public static void main(String[] args) throws IOException {

    Injector injector = Guice.createInjector(new DatabaseModule());

    Javalin app =
        Javalin.create(
            config -> {
              config.enableDevLogging();
              config.addStaticFiles("web");
            });
    app.routes(
        () -> {
          ApiBuilder.crud("links/:id", injector.getInstance(LinkHandler.class));
        });

    app.start(8000); // Start listening for http requests.
  }
}
