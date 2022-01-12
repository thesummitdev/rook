package dev.thesummit.flink.handlers;

import com.google.inject.Inject;
import dev.thesummit.flink.database.DatabaseService;
import dev.thesummit.flink.models.Preference;
import dev.thesummit.flink.models.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferenceHandler {

  private static Logger log = LoggerFactory.getLogger(PreferenceHandler.class);
  private DatabaseService dbService;

  @Inject()
  public PreferenceHandler(DatabaseService dbService) {
    this.dbService = dbService;
  }

  /** Request handler for GET ${host}/prefs/ */
  public void getAll(Context ctx) {

    JSONArray arr = new JSONArray();

    // Fetch Application Prefs.
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("userId", null);
    List<Preference> prefs =
        this.dbService.getAll(Preference.class, params).stream().collect(Collectors.toList());

    for (Preference p : prefs) {
      arr.put(p.toJSONObject());
    }

    // Fetch User specific Prefs.
    User user = ctx.sessionAttribute("current_user");
    if (user != null) {
      HashMap<String, Object> userParams = new HashMap<String, Object>();
      userParams.put("userId", user.getId());
      List<Preference> userPrefs =
          this.dbService.getAll(Preference.class, userParams).stream().collect(Collectors.toList());

      for (Preference p : userPrefs) {
        arr.put(p.toJSONObject());
      }
    }

    String response = arr.toString();
    ctx.status(200);
    ctx.contentType("application/json");
    ctx.result(response);
  }

  /** Request handler for PUT ${host}/prefs/ */
  public void create(Context ctx) {

    User user = ctx.sessionAttribute("current_user");
    JSONObject body = null;
    Preference p = null;

    try {
      body = new JSONObject(ctx.body());
    } catch (JSONException e) {
      throw new BadRequestResponse("Unable to parse JSON payload.");
    }

    if (!body.has("key") || !body.has("value")) {
      throw new BadRequestResponse("Missing required key/value pair.");
    }

    try {

      String key = body.getString("key");
      String value = body.getString("value");

      // Check and see if this preference already exists.
      Preference exists = null;

      if (Preference.applicationPrefs.contains(key)) {
        exists = this.dbService.get(Preference.class, key);
      } else {

        // This is a user pref, so filter on the pref key and the user's id.
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", user.getId());
        params.put("key", key);
        Optional<Preference> found =
            this.dbService.getAll(Preference.class, params).stream().findAny();
        if (found.isPresent()) {
          exists = found.get();
        }
      }

      if (exists != null) {
        // If it does exist, just update the existing pref with the new value.
        exists.value = value;
        this.dbService.patch(exists);
        p = exists;
      } else {
        // If it doesn't exist, create the new pref.
        p = new Preference(key, value);

        // If the key for this pref is not one of the protected app preferences
        // then save this pref to the user that called the endpoint.
        if (!Preference.applicationPrefs.contains(key)) {
          p.userId = user.getId();
        }

        // Validate the preference before committing to the database.
        if (!p.isValid()) {
          throw new BadRequestResponse("Invalid preference parameters.");
        }

        this.dbService.put(p);
      }

    } catch (JSONException e) {
      throw new BadRequestResponse("Bad request: Could not parse Preference from request body.");
    }

    if (p.getId() != null) {
      ctx.status(200);
      ctx.contentType("application/json");
      ctx.result(p.toJSONObject().toString());
    } else {
      log.debug("New Preference failed to recieve an ID from database, creation failed.");
      throw new InternalServerErrorResponse("Failed to create preference, unknown error");
    }
  }
}
