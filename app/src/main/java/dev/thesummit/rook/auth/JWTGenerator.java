package dev.thesummit.rook.auth;

import com.auth0.jwt.algorithms.Algorithm;
import dev.thesummit.rook.models.User;

public interface JWTGenerator {
  String generate(User user, Algorithm algorithm, boolean shouldExpire);
}
