package dev.thesummit.flink.handlers;

import static org.mockito.Mockito.*;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.thesummit.flink.auth.FlinkPasswordManager;
import dev.thesummit.flink.auth.JWTProvider;
import dev.thesummit.flink.auth.JWTResponse;
import dev.thesummit.flink.database.FlinkDatabaseService;
import dev.thesummit.flink.models.User;
import io.javalin.http.Context;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthHandlerTest {

  @Mock private Context ctx;
  @Mock private FlinkDatabaseService dbService;
  @Mock private FlinkPasswordManager pwm;
  @Mock private JWTProvider jwtProvider;

  private User mockUser;

  private AuthHandler handler;

  @BeforeEach
  public void init() {
    handler = new AuthHandler(pwm, dbService, jwtProvider);

    mockUser = new User("testuser", "testpassword", "testsalt");
    mockUser.setId(UUID.randomUUID());
  }

  @AfterEach
  public void tearDown() {
    mockUser = null;
  }

  @Test
  public void login_AUTH() throws Exception {

    doReturn("{'username':'testuser', 'password':'testpassword'}").when(ctx).body();
    doReturn(mockUser).when(pwm).authenticateUser("testuser", "testpassword");
    doReturn("mockjwttoken").when(jwtProvider).generateToken(any(User.class));

    handler.login(ctx);
    verify(pwm).authenticateUser("testuser", "testpassword");
    verify(ctx).json(any(JWTResponse.class));
    verify(ctx).status(200);
    verify(ctx).contentType("application/json");
  }

  @Test
  public void requireUserContext() {

    DecodedJWT mockJWT = mock(DecodedJWT.class);
    Claim mockClaim = mock(Claim.class);

    doReturn("Bearer jwttoken").when(ctx).header("Authorization");

    doReturn("testuser").when(mockClaim).asString();
    doReturn(mockClaim).when(mockJWT).getClaim("username");
    doReturn(Optional.of(mockJWT)).when(jwtProvider).validateToken("jwttoken");
    doReturn(mockUser).when(dbService).get(User.class, "testuser");

    handler.requireUserContext(ctx);
    verify(ctx).sessionAttribute("current_user", mockUser);
  }

  @Test
  public void optionalUserContextWithToken() {

    DecodedJWT mockJWT = mock(DecodedJWT.class);
    Claim mockClaim = mock(Claim.class);

    doReturn("Bearer jwttoken").when(ctx).header("Authorization");

    doReturn("testuser").when(mockClaim).asString();
    doReturn(mockClaim).when(mockJWT).getClaim("username");
    doReturn(Optional.of(mockJWT)).when(jwtProvider).validateToken("jwttoken");
    doReturn(mockUser).when(dbService).get(User.class, "testuser");

    handler.optionalUserContext(ctx);
    verify(ctx).sessionAttribute("current_user", mockUser);
  }

  @Test
  public void optionalUserContextNoToken() {

    doReturn(null).when(ctx).header("Authorization");

    handler.optionalUserContext(ctx);
    verify(ctx).sessionAttribute("current_user", null);
  }
}
