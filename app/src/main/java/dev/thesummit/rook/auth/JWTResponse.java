package dev.thesummit.rook.auth;

public class JWTResponse {
  public String jwt;
  public String username;
  public boolean isAdmin;

  public JWTResponse(String jwt, String username, boolean isAdmin) {
    this.jwt = jwt;
    this.username = username;
    this.isAdmin = isAdmin;
  }
}
