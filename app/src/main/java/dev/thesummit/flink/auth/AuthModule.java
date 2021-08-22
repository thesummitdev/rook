package dev.thesummit.flink.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import dev.thesummit.flink.database.DatabaseService;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AuthModule extends AbstractModule {

  private static final long ONE_HOUR = 3600000;
  private static final long ONE_DAY = ONE_HOUR * 24;
  private static final String ISSUER = "dev.thesummut.flink";

  public AuthModule() {}

  @Provides()
  static JWTProvider provideJWTProvider() {
    JWTGenerator generator =
        (user, alg) -> {
          Date expires = new Date();
          expires.setTime(expires.getTime() + ONE_DAY);
          JWTCreator.Builder token =
              JWT.create()
                  .withClaim("username", user.username)
                  .withIssuer(ISSUER)
                  .withIssuedAt(new Date())
                  .withExpiresAt(expires);
          return token.sign(alg);
        };

    try {

      KeyGenerator keyGen = KeyGenerator.getInstance("AES");
      keyGen.init(256);
      SecretKey secretKey = keyGen.generateKey();
      Algorithm algorithm = Algorithm.HMAC512(secretKey.getEncoded());
      JWTVerifier verifier = JWT.require(algorithm).build();

      return new JWTProvider(algorithm, generator, verifier);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Unable to initialize AuthModule.", e);
    }
  }

  @Provides()
  @Inject()
  static PasswordManager providePasswordManager(DatabaseService dbService) {
    return new FlinkPasswordManager(dbService);
  }
}
