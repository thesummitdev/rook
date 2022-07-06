package dev.thesummit.rook.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import dev.thesummit.rook.database.DatabaseService;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthModule extends AbstractModule {

  private static Logger log = LoggerFactory.getLogger(AuthModule.class);
  private static final long ONE_HOUR = 3600000;
  private static final long ONE_DAY = ONE_HOUR * 24;
  private static final String ISSUER = "dev.thesummit.rook";
  private static String SECRET_KEY = System.getenv("ROOK_SERVER_SECRET_KEY");

  public AuthModule() {}

  @Provides()
  static JWTProvider provideJWTProvider() {
    JWTGenerator generator =
        (user, alg) -> {
          Date expires = new Date();
          expires.setTime(expires.getTime() + (ONE_DAY * 7));
          JWTCreator.Builder token =
              JWT.create()
                  .withClaim("username", user.username)
                  .withIssuer(ISSUER)
                  .withIssuedAt(new Date())
                  .withExpiresAt(expires);
          return token.sign(alg);
        };

    if (SECRET_KEY == null) {
      log.info(
          "$rook_SERVER_SECRET_KEY is unset. Consider setting this variable for improved server"
              + " security.");
      SECRET_KEY = "rook_development_server";
    }

    Algorithm algorithm = Algorithm.HMAC512(SECRET_KEY);
    JWTVerifier verifier = JWT.require(algorithm).build();
    return new JWTProvider(algorithm, generator, verifier);
  }

  @Provides()
  @Inject()
  static PasswordManager providePasswordManager(DatabaseService dbService) {
    return new RookPasswordManager(dbService);
  }
}
