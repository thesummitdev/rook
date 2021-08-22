package dev.thesummit.flink.handlers;

import com.google.inject.Inject;
import dev.thesummit.flink.auth.JWTProvider;
import dev.thesummit.flink.auth.JWTResponse;
import dev.thesummit.flink.auth.PasswordManager;
import dev.thesummit.flink.database.DatabaseService;
import dev.thesummit.flink.models.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHandler {

  private static Logger log = LoggerFactory.getLogger(UserHandler.class);
  private PasswordManager pwm;
  private DatabaseService dbService;
  private JWTProvider jwtProvider;

  @Inject()
  public UserHandler(PasswordManager pwm, DatabaseService dbService, JWTProvider jwtProvider) {
    this.pwm = pwm;
    this.dbService = dbService;
    this.jwtProvider = jwtProvider;
  }

  public void login(Context ctx) throws Exception {

    JSONObject body = null;

    try {
      body = new JSONObject(ctx.body());
    } catch (JSONException e) {
      throw new BadRequestResponse("Unable to parse JSON payload");
    }

    Optional<String> username = Optional.ofNullable(body.getString("username"));
    Optional<String> password = Optional.ofNullable(body.getString("password"));

    if (username.isPresent() && password.isPresent()) {
      User user = this.pwm.authenticateUser(username.get(), password.get());
      if (user != null) {
        // User is now authenticated, return a JWT token for future requests.
        String token = this.jwtProvider.generateToken(user);
        ctx.json(new JWTResponse(token));
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

  public void create(Context ctx) throws Exception {

    JSONObject body = null;

    try {
      body = new JSONObject(ctx.body());

    } catch (JSONException e) {
      throw new BadRequestResponse("Unable to parse JSON payload");
    }

    Optional<String> username = Optional.ofNullable(body.getString("username"));
    Optional<String> password = Optional.ofNullable(body.getString("password"));

    if (username.isPresent() && password.isPresent()) {
      User exists = this.dbService.get(User.class, username.get());
      System.out.println(exists);

      if (exists != null) {
        throw new BadRequestResponse(String.format("User %s already exists.", username.get()));
      }

      String salt = this.pwm.getNewSalt();
      String userEncryptedPassword = this.pwm.getEncryptedPassword(password.get(), salt);

      User newUser = new User(username.get(), userEncryptedPassword, salt);

      this.dbService.put(newUser);

      ctx.result(newUser.toJSONObject().toString());
      ctx.status(200);
      ctx.contentType("application/json");
      return;
    }

    throw new BadRequestResponse("Missing required fields");
  }
}
