package dev.thesummit.flink.auth;

import com.auth0.jwt.algorithms.Algorithm;
import dev.thesummit.flink.models.User;

public interface JWTGenerator {
  String generate(User user, Algorithm algorithm);
}
