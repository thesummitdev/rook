package dev.thesummit.flink.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.thesummit.flink.database.FlinkDatabaseService;
import dev.thesummit.flink.models.Link;
import dev.thesummit.flink.models.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LinkHandlerTest {

  @Mock private Context ctx;
  @Mock private FlinkDatabaseService dbService;
  private final UUID MOCK_USER_UUID = UUID.randomUUID();
  private final UUID MOCK_LINK_UUID = UUID.randomUUID();

  private LinkHandler handler;

  @BeforeEach
  public void init() {
    MockitoAnnotations.initMocks(this);
    handler = new LinkHandler(dbService);

    User mockUser = new User("username", "userEncryptedPassword", "salt");
    mockUser.setId(MOCK_USER_UUID);

    when(ctx.sessionAttribute("current_user")).thenReturn(mockUser);
  }

  @Test
  public void can_be_created() {
    assertNotNull(handler);
    assertNotNull(ctx);
    assertNotNull(dbService);
  }

  @Test
  public void GETONE_links() {
    Link link = new Link("Test Link", "http://test.com", "test tags", MOCK_USER_UUID);
    UUID uuid = UUID.randomUUID();
    link.setId(uuid);
    when(dbService.get(Link.class, uuid)).thenReturn(link);
    when(ctx.pathParam("id")).thenReturn(uuid.toString());

    handler.getOne(ctx);
    verify(ctx).result(link.toJSONObject().toString());
    verify(ctx).status(200);
    verify(ctx).contentType("application/json");
  }

  @Test
  public void GETONE_links_not_found() {
    Link link = new Link("Test Link", "http://test.com", "test tags", MOCK_USER_UUID);
    UUID uuid = UUID.randomUUID();
    link.setId(uuid);
    when(dbService.get(Link.class, uuid)).thenReturn(null);
    when(ctx.pathParam("id")).thenReturn(uuid.toString());

    assertThrows(
        NotFoundResponse.class,
        () -> {
          handler.getOne(ctx);
        });
  }

  @Test
  public void GETONE_links_invalid_uuid_format() {

    when(ctx.pathParam("id")).thenReturn("this-is-not-a-valid-uuid");

    assertThrows(
        BadRequestResponse.class,
        () -> {
          handler.getOne(ctx);
        });
  }

  @Test
  public void GETALL_links() {
    Link mockLink = new Link("First Link", "http://test.com", "test tags", MOCK_USER_UUID);
    Link mockLink2 = new Link("Second Link", "http://test2.com", "test2 tags", MOCK_USER_UUID);
    ArrayList<Link> list = new ArrayList<Link>();
    list.add(mockLink);
    list.add(mockLink2);

    when(ctx.body()).thenReturn("{}");
    when(dbService.getAll(any(Class.class), any(HashMap.class))).thenReturn(list);

    JSONArray expectedResult = new JSONArray();
    expectedResult.put(mockLink.toJSONObject());
    expectedResult.put(mockLink2.toJSONObject());

    handler.getAll(ctx);
    verify(ctx).result(expectedResult.toString());
    verify(ctx).status(200);
    verify(ctx).contentType("application/json");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{'title': 'First Link','url':'http://test.com', 'tags':'test tags'}",
        "{'title': 'Second Link','url':'http://test.com'}",
      })
  public void CREATE_link(String body) {

    JSONObject obj = new JSONObject(body);
    when(ctx.body()).thenReturn(body);
    Link expectedLink =
        new Link(
            obj.getString("title"),
            obj.getString("url"),
            obj.optString("tags", ""),
            MOCK_USER_UUID);
    expectedLink.setId(MOCK_LINK_UUID);

    // Handle the side effect of the databaseService.put setting the ID on the new Link with the id
    // that was returned from the database.
    doAnswer(
            invocation -> {
              Link l = invocation.getArgument(0);
              l.setId(MOCK_LINK_UUID);
              return null;
            })
        .when(dbService)
        .put(any(Link.class));

    ArgumentCaptor<Link> arg = ArgumentCaptor.forClass(Link.class);
    handler.create(ctx);
    verify(dbService).put(arg.capture());
    // Ensure all fields match the input
    assertEquals(Link.class, arg.getValue().getClass());
    assertEquals(expectedLink.title, arg.getValue().title);
    assertEquals(expectedLink.tags, arg.getValue().tags);
    assertEquals(expectedLink.url, arg.getValue().url);
    assertEquals(expectedLink.getId(), arg.getValue().getId());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "{}",
        "{'tags':'test tags'}",
        "{123:45}",
        "{'url':'notaurl'}",
      })
  public void CREATE_link_invalid_body(String body) {
    when(ctx.body()).thenReturn(body);
    assertThrows(
        BadRequestResponse.class,
        () -> {
          handler.create(ctx);
        });
  }
}
