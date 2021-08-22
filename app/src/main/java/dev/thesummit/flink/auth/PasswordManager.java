package dev.thesummit.flink.auth;

import dev.thesummit.flink.models.User;

public interface PasswordManager {

  public User authenticateUser(String username, String password);

  public String getEncryptedPassword(String password, String salt);

  public String getNewSalt();
}
