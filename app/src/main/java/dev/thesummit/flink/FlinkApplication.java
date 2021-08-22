package dev.thesummit.flink;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.thesummit.flink.auth.AuthModule;
import dev.thesummit.flink.auth.JWTProvider;
import dev.thesummit.flink.auth.PasswordManager;
import dev.thesummit.flink.database.DatabaseModule;
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

    JWTProvider provider = injector.getInstance(JWTProvider.class);
    PasswordManager pw = injector.getInstance(PasswordManager.class);

    System.out.println(pw);

    Javalin app =
        Javalin.create(
            config -> {
              config.enableDevLogging();
              config.addStaticFiles("web");
            });

    // app.get(
    // "/generate",
    // context -> {
    //// Check body for username / password
    // User mockUser = new User("tylersaunders");

    // String token = provider.generateToken(mockUser);
    // context.json(new JWTResponse(token));
    // });

    // app.get(
    // "/validate",
    // context -> {
    // Optional<String> token =
    // Optional.ofNullable(context.header("Authorization"))
    // .flatMap(
    // header -> {
    // String[] split = header.split(" ");
    // if (split.length != 2 || !split[0].equals("Bearer")) {
    // return Optional.empty();
    // }

    // return Optional.of(split[1]);
    // });

    // if (token.isPresent()) {
    // Optional<DecodedJWT> jwt = provider.validateToken(token.get());

    // if (jwt.isPresent()) {
    //// jwt is valid add User to context
    // } else {
    //// invalid jwt so return unauthorized.
    // context.status(401);
    // context.result("Invalid authorization token.");
    // context.contentType("text");
    // }
    // }
    // });

    app.routes(
        () -> {
          ApiBuilder.path(
              "users",
              () -> {
                ApiBuilder.post(injector.getInstance(UserHandler.class)::create);
                ApiBuilder.path(
                    "/login",
                    () -> {
                      ApiBuilder.post(injector.getInstance(UserHandler.class)::login);
                    });
              });
          ApiBuilder.crud("links/:id", injector.getInstance(LinkHandler.class));
        });

    app.start(8000); // Start listening for http requests.
    log.info("Flink server successfully started.");
  }
}
