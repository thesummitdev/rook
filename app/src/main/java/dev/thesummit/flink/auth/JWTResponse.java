package dev.thesummit.flink.auth;

public class JWTResponse {
  public String jwt;
  public String username;

  public JWTResponse(String jwt, String username) {
    this.jwt = jwt;
    this.username = username;
  }
}
