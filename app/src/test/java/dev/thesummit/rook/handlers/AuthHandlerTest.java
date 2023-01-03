package dev.thesummit.rook.handlers;

import static org.mockito.Mockito.*;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.thesummit.rook.auth.JWTProvider;
import dev.thesummit.rook.auth.JWTResponse;
import dev.thesummit.rook.auth.RookPasswordManager;
import dev.thesummit.rook.database.RookDatabaseService;
import dev.thesummit.rook.models.ApiKey;
import dev.thesummit.rook.models.User;
import io.javalin.http.Context;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthHandlerTest {

  @Mock private Context ctx;
  @Mock private RookDatabaseService dbService;
  @Mock private RookPasswordManager pwm;
  @Mock private JWTProvider jwtProvider;

  private User mockUser;

  private AuthHandler handler;

  @BeforeEach
  public void init() {
    handler = new AuthHandler(pwm, dbService, jwtProvider);

    mockUser = new User("testuser", "testpassword", "testsalt");
    mockUser.setId(1);
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
  public void generateApiKey() {

    ApiKey expectedKey = new ApiKey(mockUser, "thisisanapikey", "agent");
    expectedKey.setId(1);

    doReturn(mockUser).when(ctx).sessionAttribute("current_user");
    doReturn("thisisanapikey").when(jwtProvider).generateApiKey(mockUser);
    doReturn("agent").when(ctx).userAgent();

    doAnswer(
            invocation -> {
              ApiKey key = invocation.getArgument(0);
              key.setId(1);
              return null;
            })
        .when(dbService)
        .put(any(ApiKey.class));

    handler.generateApiKey(ctx);
    verify(ctx).status(200);
    verify(ctx).result(expectedKey.toJsonObject().toString());
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
