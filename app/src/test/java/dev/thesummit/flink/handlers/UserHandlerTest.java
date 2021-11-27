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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserHandlerTest {

  @Mock private Context ctx;
  @Mock private FlinkDatabaseService dbService;
  @Mock private FlinkPasswordManager pwm;

  private final String MOCK_SALT = "salt";
  private final String MOCK_ENCRYPTED_PASSWORD = "password";

  private UserHandler handler;

  @BeforeEach
  public void init() {
    handler = new UserHandler(pwm, dbService);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{'username':'someuser', 'password':'testpassword'}",
      })
  public void CREATE_link(String body) throws Exception {

    JSONObject obj = new JSONObject(body);
    doReturn(body).when(ctx).body();
    doReturn(MOCK_SALT).when(pwm).getNewSalt();
    doReturn(MOCK_ENCRYPTED_PASSWORD).when(pwm).getEncryptedPassword(anyString(), anyString());
    User expectedUser = new User(obj.getString("username"), MOCK_ENCRYPTED_PASSWORD, MOCK_SALT);

    ArgumentCaptor<User> arg = ArgumentCaptor.forClass(User.class);

    handler.create(ctx);

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
    doReturn(body).when(ctx).body();
    assertThrows(
        BadRequestResponse.class,
        () -> {
          handler.create(ctx);
        });
  }
}
