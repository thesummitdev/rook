package dev.thesummit.flink.handlers;

import static org.junit.jupiter.api.Assertions.*;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AuthHandlerTest {

  @Mock private Context ctx;
  @Mock private FlinkDatabaseService dbService;
  @Mock private FlinkPasswordManager pwm;
  @Mock private JWTProvider jwtProvider;

  private User mockUser;

  private AuthHandler handler;

  @BeforeEach
  public void init() {
    MockitoAnnotations.initMocks(this);
    handler = new AuthHandler(pwm, dbService, jwtProvider);

    mockUser = new User("testuser", "testpassword", "testsalt");
    mockUser.setId(UUID.randomUUID());
  }

  @AfterEach
  public void tearDown() {
    mockUser = null;
  }

  @Test
  public void can_be_created() {
    assertNotNull(handler);
    assertNotNull(ctx);
    assertNotNull(dbService);
  }

  @Test
  public void login_AUTH() throws Exception {

    when(ctx.body()).thenReturn("{'username':'testuser', 'password':'testpassword'}");
    when(pwm.authenticateUser("testuser", "testpassword")).thenReturn(mockUser);
    when(jwtProvider.generateToken(any(User.class))).thenReturn("mockjwttoken");

    handler.login(ctx);
    verify(pwm).authenticateUser("testuser", "testpassword");
    verify(ctx).json(any(JWTResponse.class));
    verify(ctx).status(200);
    verify(ctx).contentType("application/json");
  }

  @Test
  public void fetchUserContext() {

    DecodedJWT mockJWT = mock(DecodedJWT.class);
    Claim mockClaim = mock(Claim.class);

    // See https://github.com/mockito/mockito/issues/1943
    // Mockito can't mock the correct method without the type hint.
    Mockito.<String>when(ctx.header("Authorization")).thenReturn("Bearer jwttoken");

    when(mockClaim.asString()).thenReturn("testuser");
    when(mockJWT.getClaim("username")).thenReturn(mockClaim);
    when(jwtProvider.validateToken("jwttoken")).thenReturn(Optional.of(mockJWT));
    when(dbService.get(User.class, "testuser")).thenReturn(mockUser);

    handler.fetchUserContext(ctx);
    verify(ctx).sessionAttribute("current_user", mockUser);
  }
}
