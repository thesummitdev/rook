package dev.thesummit.rook.handlers;

import com.google.inject.Inject;
import dev.thesummit.rook.auth.PasswordManager;
import dev.thesummit.rook.database.DatabaseService;
import dev.thesummit.rook.models.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** API handler for user creation. */
public class UserHandler {

  private static Logger log = LoggerFactory.getLogger(UserHandler.class);
  private PasswordManager pwm;
  private DatabaseService dbService;

  @Inject()
  public UserHandler(PasswordManager pwm, DatabaseService dbService) {
    this.pwm = pwm;
    this.dbService = dbService;
  }

  /**
   * HTTP handler for creating a new user. Expects to receive a JSON object with a username/password
   * and returns a 200 status and a User object if the creation is successful.
   */
  public void create(Context ctx) throws Exception {

    JSONObject body = null;

    try {
      body = new JSONObject(ctx.body());

    } catch (JSONException e) {
      throw new BadRequestResponse("Unable to parse JSON payload");
    }

    Optional<String> username = Optional.ofNullable(body.optString("username", null));
    Optional<String> password = Optional.ofNullable(body.optString("password", null));

    if (username.isPresent() && password.isPresent()) {
      User exists = this.dbService.get(User.class, username.get());

      if (exists != null) {
        throw new BadRequestResponse(String.format("User %s already exists.", username.get()));
      }

      String salt = this.pwm.getNewSalt();
      String userEncryptedPassword = this.pwm.getEncryptedPassword(password.get(), salt);

      User newUser = new User(username.get(), userEncryptedPassword, salt);

      log.debug(String.format("Creating new user %s", newUser.username));
      this.dbService.put(newUser);

      ctx.result(newUser.toJsonObject().toString());
      ctx.status(200);
      ctx.contentType("application/json");
      return;
    }

    throw new BadRequestResponse("Missing required fields");
  }
}
