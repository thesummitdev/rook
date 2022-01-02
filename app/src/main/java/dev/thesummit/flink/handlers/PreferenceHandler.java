package dev.thesummit.flink.handlers;

import com.google.inject.Inject;
import dev.thesummit.flink.database.DatabaseService;
import dev.thesummit.flink.models.Preference;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferenceHandler {

  private static Logger log = LoggerFactory.getLogger(PreferenceHandler.class);
  private DatabaseService dbService;

  @Inject
  public PreferenceHandler(DatabaseService dbService) {
    this.dbService = dbService;
  }

  /** Request handler for PUT ${host}/prefs/ */
  public void getAll(Context ctx) {

    JSONArray arr = new JSONArray();

    HashMap<String, Object> params = new HashMap<String, Object>();
    List<Preference> prefs =
        this.dbService.getAll(Preference.class, params).stream().collect(Collectors.toList());

    for (Preference p : prefs) {
      arr.put(p.toJSONObject());
    }

    String response = arr.toString();
    ctx.status(200);
    ctx.contentType("application/json");
    ctx.result(response);
  }
}
