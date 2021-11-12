package dev.thesummit.flink.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.thesummit.flink.database.FlinkDatabaseService;
import dev.thesummit.flink.models.Link;
import dev.thesummit.flink.models.User;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TagHandlerTest {

  @Mock private Context ctx;
  @Mock private FlinkDatabaseService dbService;
  private final UUID MOCK_USER_UUID = UUID.randomUUID();

  private TagHandler handler;

  @BeforeEach
  public void init() {
    MockitoAnnotations.initMocks(this);
    handler = new TagHandler(dbService);

    User mockUser = new User("username", "userEncryptedPassword", "salt");
    mockUser.setId(MOCK_USER_UUID);

    when(ctx.sessionAttribute("current_user")).thenReturn(mockUser);
  }

  @Test
  public void getTags() {

    Link mockLink = new Link("First Link", "http://test.com", "test tags", MOCK_USER_UUID);
    Link mockLink2 = new Link("Second Link", "http://test2.com", "test2 tags", MOCK_USER_UUID);
    ArrayList<Link> list = new ArrayList<Link>();
    list.add(mockLink);
    list.add(mockLink2);

    when(dbService.getAll(any(Class.class), any(HashMap.class))).thenReturn(list);

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
