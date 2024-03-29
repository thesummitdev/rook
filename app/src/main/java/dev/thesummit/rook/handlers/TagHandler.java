package dev.thesummit.rook.handlers;

import com.google.inject.Inject;
import dev.thesummit.rook.database.DatabaseService;
import dev.thesummit.rook.models.Link;
import dev.thesummit.rook.models.User;
import io.javalin.http.Context;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;

public class TagHandler {

  private DatabaseService dbService;

  @Inject()
  public TagHandler(DatabaseService dbService) {
    this.dbService = dbService;
  }

  public void getAll(Context ctx) {
    User user = ctx.sessionAttribute("current_user");
    JSONArray arr = new JSONArray();

    // Grab all links for the current user.
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("userId", user.id); // Scope the search to Links the user owns.
    List<Link> lns = this.dbService.getAll(Link.class, params);

    // Extract tags and dedupe / sort.
    List<String> tags =
        lns.stream()
            .map(link -> link.tags.split(" "))
            .flatMap(Arrays::stream)
            // Don't include white space tags, i.e. " "
            .filter(tag -> !tag.isEmpty())
            .distinct()
            .sorted()
            .collect(Collectors.toList());

    for (String tag : tags) {
      arr.put(tag);
    }

    String response = arr.toString();
    ctx.status(200);
    ctx.contentType("application/json");
    ctx.result(response);
  }
}
