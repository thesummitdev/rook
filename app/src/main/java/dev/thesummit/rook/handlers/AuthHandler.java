package dev.thesummit.rook.handlers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import dev.thesummit.rook.auth.JWTProvider;
import dev.thesummit.rook.auth.JWTResponse;
import dev.thesummit.rook.auth.PasswordManager;
import dev.thesummit.rook.database.DatabaseService;
import dev.thesummit.rook.models.ApiKey;
import dev.thesummit.rook.models.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Authentication handler for /user/ related routes. */
public class AuthHandler {
  private static Logger log = LoggerFactory.getLogger(UserHandler.class);
  private PasswordManager pwm;
  private DatabaseService dbService;
  private JWTProvider jwtProvider;

  /** Builds an auth handler for API authentication related routes. */
  @Inject()
  public AuthHandler(PasswordManager pwm, DatabaseService dbService, JWTProvider jwtProvider) {
    this.pwm = pwm;
    this.dbService = dbService;
    this.jwtProvider = jwtProvider;
  }

  /**
   * HTTP handler for the users/login route. Expects to recieve a valid
   * username/password JSON
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
        ctx.json(new JWTResponse(token, user.username, user.isAdmin));
        ctx.status(200);
        ctx.contentType("application/json");
        log.info("[Login] {} has successfully logged in.", user.username);
        return;
      } else {
        // User is not authenticated.
        ctx.status(401);
        ctx.result("Invalid username and password");
        log.debug("[Login] Unable to log user in, invalid credentials.");
        return;
      }
    }

    throw new BadRequestResponse("Missing required username and password.");
  }

  /** Ensures that a properly authenticated user is present in the current request context. */
  public void requireUserContext(Context ctx) {
    Optional<User> user = getUserFromRequest(ctx);

    if (user.isPresent()) {
      ctx.sessionAttribute("current_user", user.get());
    } else {
      throw new ForbiddenResponse(
          "Missing authorized user, please login via /users/login to obtain a token");
    }
  }

  /** Checks to see if a properly authenticated user is present in the current request context. */
  public void optionalUserContext(Context ctx) {
    Optional<User> user = getUserFromRequest(ctx);

    if (user.isPresent()) {
      ctx.sessionAttribute("current_user", user.get());
    } else {
      ctx.sessionAttribute("current_user", null);
    }
  }

  /** Handler for deleting API keys. */
  public void deleteApiKey(Context ctx) {
    Optional<User> user = getUserFromRequest(ctx);
    Integer resourceId = Integer.parseInt(ctx.pathParam("id"));
    ApiKey key = this.dbService.get(ApiKey.class, resourceId);

    if (key != null) {
      if (!user.isPresent() || !user.get().getId().equals(key.userId)) {
        throw new ForbiddenResponse("You must be logged in and own the resource to delete it.");
      }

      this.dbService.delete(key);
      ctx.status(200);
    } else {
      throw new NotFoundResponse("Could not find resource");
    }
  }

  /** Handler for creating API keys. */
  public void generateApiKey(Context ctx) {
    User user = ctx.sessionAttribute("current_user");

    if (user == null) {
      throw new ForbiddenResponse("You must be logged in to request a new api key.");
    }

    ApiKey newKey = new ApiKey(user, this.jwtProvider.generateApiKey(user), ctx.userAgent());
    this.dbService.put(newKey);

    if (newKey.getId() != null) {
      ctx.status(200);
      ctx.contentType("application/json");
      ctx.result(newKey.toJsonObject().toString());
    } else {
      throw new InternalServerErrorResponse("Failed to create ApiKey, unknown error");
    }
  }

  /** Handler for listing a user's current API keys. */
  public void getApiKeys(Context ctx) {
    User user = ctx.sessionAttribute("current_user");

    if (user == null) {
      throw new ForbiddenResponse("You must be logged in to request your api keys.");
    }

    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("userId", user.id); // Scope the search to Links the user owns.

    List<ApiKey> keys = this.dbService.getAll(ApiKey.class, params);

    JSONArray arr = new JSONArray();
    for (ApiKey key : keys) {
      arr.put(key.toJsonObject());
    }

    ctx.status(200);
    ctx.contentType("application/json");
    ctx.result(arr.toString());
  }

  /**
   * Attempts to find the {@link User} from the request context. Looks up the user
   * based on the username claim in the Auth token. In the case where there is no Auth token
   * present, or the user cannot be found, returns Optional.empty().
   */
  private Optional<User> getUserFromRequest(Context ctx) {
    Optional<String> token = Optional.ofNullable(ctx.header("Authorization")).flatMap(header -> {
      String[] split = header.split(" ");
      if (split.length != 2 || !split[0].equals("Bearer")) {
        return Optional.empty();
      }

      return Optional.of(split[1]);
    });

    if (token.isPresent()) {
      Optional<DecodedJWT> jwt = this.jwtProvider.validateToken(token.get());

      if (jwt.isPresent()) {
        // Check the database for the user.
        return Optional.ofNullable(
            this.dbService.get(User.class, jwt.get().getClaim("username").asString()));
      } else {
        // A token was in the headers, but is invalid.
        throw new ForbiddenResponse("Invalid authorization token. The token might be expired.");
      }
    }
    // There was no token, so don't look for a user.
    return Optional.empty();
  }
}
