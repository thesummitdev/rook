package dev.thesummit.rook.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.thesummit.rook.auth.RookPasswordManager;
import dev.thesummit.rook.database.RookDatabaseService;
import dev.thesummit.rook.models.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpCode;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserHandlerTest {
  @Mock private Context ctx;
  @Mock private RookDatabaseService dbService;
  @Mock private RookPasswordManager pwm;

  private static final String MOCK_SALT = "salt";
  private static final String MOCK_ENCRYPTED_PASSWORD = "password";

  private UserHandler handler;

  @BeforeEach
  public void init() {
    handler = new UserHandler(pwm, dbService);
  }

  @ParameterizedTest
  @ValueSource(strings =
                   {
                       "{'username':'someuser', 'password':'testpassword'}",
                   })
  public void testCreateUser(String body) throws Exception {
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
  @ValueSource(strings =
                   {
                       "",
                       "{}",
                       "{'username':'testuser'}",
                       "{123:45}",
                       "{'password':'somepassword'}",
                   })
  public void testCreateUserInvalidBody(String body) {
    doReturn(body).when(ctx).body();
    assertThrows(BadRequestResponse.class, () -> { handler.create(ctx); });
  }

  @Test
  public void testGetUsersRequiresAdmin() {
    User mockAdmin = new User("admin", MOCK_ENCRYPTED_PASSWORD, MOCK_SALT);
    mockAdmin.isAdmin = true;
    doReturn(mockAdmin).when(ctx).sessionAttribute("current_user");

    handler.getAll(ctx);

    verify(ctx).status(HttpCode.OK);
  }

  @Test
  public void testGetUsersThrowsWithNoAdmin() {
    User mockRegularUser = new User("user", MOCK_ENCRYPTED_PASSWORD, MOCK_SALT);

    doReturn(mockRegularUser).when(ctx).sessionAttribute("current_user");

    assertThrows(ForbiddenResponse.class, () -> { handler.getAll(ctx); });
  }
}
