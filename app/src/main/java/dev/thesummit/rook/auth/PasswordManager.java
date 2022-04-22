package dev.thesummit.rook.auth;

import dev.thesummit.rook.models.User;

public interface PasswordManager {

  public User authenticateUser(String username, String password);

  public String getEncryptedPassword(String password, String salt);

  public String getNewSalt();
}
