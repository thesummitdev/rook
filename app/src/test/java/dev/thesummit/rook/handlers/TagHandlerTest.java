package dev.thesummit.rook.handlers;

import static org.mockito.Mockito.*;

import dev.thesummit.rook.database.RookDatabaseService;
import dev.thesummit.rook.models.Link;
import dev.thesummit.rook.models.User;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagHandlerTest {

  @Mock private Context ctx;
  @Mock private RookDatabaseService dbService;
  private final Integer MOCK_USER_ID = 1;

  private TagHandler handler;

  @BeforeEach
  public void init() {
    handler = new TagHandler(dbService);

    User mockUser = new User("username", "userEncryptedPassword", "salt");
    mockUser.setId(MOCK_USER_ID);

    doReturn(mockUser).when(ctx).sessionAttribute("current_user");
  }

  @Test
  public void getTags() {

    Link mockLink = new Link("First Link", "http://test.com", "test tags", MOCK_USER_ID);
    Link mockLink2 = new Link("Second Link", "http://test2.com", "test2 tags", MOCK_USER_ID);
    ArrayList<Link> list = new ArrayList<Link>();
    list.add(mockLink);
    list.add(mockLink2);

    doReturn(list).when(dbService).getAll(any(Class.class), any(HashMap.class));

    JSONArray expectedResult = new JSONArray();
    expectedResult.put("tags");
    expectedResult.put("test");
    expectedResult.put("test2");
    handler.getAll(ctx);
    verify(ctx).result(expectedResult.toString());
    verify(ctx).status(200);
    verify(ctx).contentType("application/json");
  }
}
