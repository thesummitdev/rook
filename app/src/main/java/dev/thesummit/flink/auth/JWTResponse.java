package dev.thesummit.flink.auth;

public class JWTResponse {
  public String jwt;

  public JWTResponse(String jwt) {
    this.jwt = jwt;
  }
}
