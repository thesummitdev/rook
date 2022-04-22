package dev.thesummit.rook.auth;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import dev.thesummit.rook.models.User;
import java.util.Optional;

public class JWTProvider {

  private final Algorithm algorithm;
  private final JWTGenerator generator;
  private final JWTVerifier verifier;

  @Inject()
  public JWTProvider(Algorithm algorithm, JWTGenerator generator, JWTVerifier verifier) {
    this.algorithm = algorithm;
    this.generator = generator;
    this.verifier = verifier;
  }

  public String generateToken(User user) {
    return generator.generate(user, this.algorithm);
  }

  public Optional<DecodedJWT> validateToken(String token) {
    try {
      return Optional.of(this.verifier.verify(token));
    } catch (JWTVerificationException e) {
      return Optional.empty();
    }
  }
}
