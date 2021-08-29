package dev.thesummit.flink.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.thesummit.flink.auth.FlinkPasswordManager;
import dev.thesummit.flink.database.FlinkDatabaseService;
import dev.thesummit.flink.models.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserHandlerTest {

  @Mock private Context ctx;
  @Mock private FlinkDatabaseService dbService;
  @Mock private FlinkPasswordManager pwm;

  private final String MOCK_SALT = "salt";
  private final String MOCK_ENCRYPTED_PASSWORD = "salt";

  private UserHandler handler;

  @BeforeEach
  public void init() {
    MockitoAnnotations.initMocks(this);
    handler = new UserHandler(pwm, dbService);
  }

  @Test
  public void can_be_created() {
    assertNotNull(handler);
    assertNotNull(ctx);
    assertNotNull(dbService);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{'username':'someuser', 'password':'testpassword'}",
      })
  public void CREATE_link(String body) {

    JSONObject obj = new JSONObject(body);
    when(ctx.body()).thenReturn(body);
    when(pwm.getNewSalt()).thenReturn(MOCK_SALT);
    when(pwm.getEncryptedPassword(anyString(), anyString())).thenReturn(MOCK_ENCRYPTED_PASSWORD);
    User expectedUser = new User(obj.getString("username"), MOCK_ENCRYPTED_PASSWORD, MOCK_SALT);

    ArgumentCaptor<User> arg = ArgumentCaptor.forClass(User.class);
    try {
      handler.create(ctx);
    } catch (Exception e) {

    }
    verify(dbService).put(arg.capture());
    // Ensure all fields match the input
    assertEquals(User.class, arg.getValue().getClass());
    assertEquals(expectedUser.username, arg.getValue().username);
    assertEquals(expectedUser.userSalt, arg.getValue().userSalt);
    assertEquals(expectedUser.userEncryptedPassword, arg.getValue().userEncryptedPassword);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "{}",
        "{'username':'testuser'}",
        "{123:45}",
        "{'password':'somepassword'}",
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
