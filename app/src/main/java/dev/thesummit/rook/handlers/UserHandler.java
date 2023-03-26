package dev.thesummit.rook.handlers;

import com.google.inject.Inject;
import dev.thesummit.rook.auth.PasswordManager;
import dev.thesummit.rook.database.DatabaseService;
import dev.thesummit.rook.models.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONArray;
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

  /**
   * Request handler for GET ${host}/users.
   *
   * @param ctx                 the request {@link Context}
   * @throws ForbiddenResponse  non-admin users are not authorized to list all users.
   */
  public void getAll(Context ctx) throws ForbiddenResponse {
    User user = ctx.sessionAttribute("current_user");

    if (!user.isAdmin) {
      throw new ForbiddenResponse("Current user cannot make this request.");
    }

    List<User> results = this.dbService.getAll(User.class, Map.of());
    JSONArray arr = new JSONArray();

    for (User u : results) {
      arr.put(u.toJsonObject());
    }

    ctx.status(HttpCode.OK);
    ctx.result(new JSONObject().put("items", arr).toString());
  }
}
