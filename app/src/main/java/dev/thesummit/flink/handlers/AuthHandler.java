package dev.thesummit.flink.handlers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import dev.thesummit.flink.auth.JWTProvider;
import dev.thesummit.flink.auth.JWTResponse;
import dev.thesummit.flink.auth.PasswordManager;
import dev.thesummit.flink.database.DatabaseService;
import dev.thesummit.flink.models.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthHandler {

  private static Logger log = LoggerFactory.getLogger(UserHandler.class);
  private PasswordManager pwm;
  private DatabaseService dbService;
  private JWTProvider jwtProvider;

  @Inject()
  public AuthHandler(PasswordManager pwm, DatabaseService dbService, JWTProvider jwtProvider) {
    this.pwm = pwm;
    this.dbService = dbService;
    this.jwtProvider = jwtProvider;
  }

  /**
   * HTTP handler for the users/login route. Expects to recieve a valid username/password JSON
   * object and returns a JWT to be used for future requests.
   */
  public void login(Context ctx) throws Exception {

    JSONObject body = null;

    try {
      body = new JSONObject(ctx.body());
    } catch (JSONException e) {
      log.debug("Error parsing JSON", e);
      throw new BadRequestResponse("Unable to parse JSON payload");
    }

    Optional<String> username = Optional.ofNullable(body.getString("username"));
    Optional<String> password = Optional.ofNullable(body.getString("password"));

    if (username.isPresent() && password.isPresent()) {
      User user = this.pwm.authenticateUser(username.get(), password.get());
      if (user != null) {
        // User is now authenticated, return a JWT token for future requests.
        String token = this.jwtProvider.generateToken(user);
        ctx.json(new JWTResponse(token, user.username));
        ctx.status(200);
        ctx.contentType("application/json");
        log.debug("User is successfully logged in.");
        return;
      } else {
        // User is not authenticated.
        ctx.status(401);
        ctx.result("Invalid username and password");
        log.debug("Unable to log user in, invalid credentials.");
        return;
      }
    }

    throw new BadRequestResponse("Missing required username and password.");
  }

  public void fetchUserContext(Context ctx) {

    Optional<String> token =
        Optional.ofNullable(ctx.header("Authorization"))
            .flatMap(
                header -> {
                  String[] split = header.split(" ");
                  if (split.length != 2 || !split[0].equals("Bearer")) {
                    return Optional.empty();
                  }

                  return Optional.of(split[1]);
                });

    if (token.isPresent()) {

      Optional<DecodedJWT> jwt = this.jwtProvider.validateToken(token.get());

      if (jwt.isPresent()) {
        User user = this.dbService.get(User.class, jwt.get().getClaim("username").asString());
        ctx.sessionAttribute("current_user", user);
      } else {
        throw new ForbiddenResponse("Invalid authorization token. The token might be expired.");
      }

    } else {
      throw new ForbiddenResponse(
          "Missing authorization token, please login via /users/login to obtain a token");
    }
  }
}
