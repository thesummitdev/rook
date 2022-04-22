package dev.thesummit.rook.auth;

import dev.thesummit.rook.database.DatabaseService;
import dev.thesummit.rook.models.User;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class RookPasswordManager implements PasswordManager {

  private DatabaseService dbService;

  public RookPasswordManager(DatabaseService dbService) {
    this.dbService = dbService;
  }

  @Override
  public User authenticateUser(String username, String password) {
    User user = this.dbService.get(User.class, username);
    if (user == null) {
      return null;
    }

    String salt = user.userSalt;
    String calculatedHash = getEncryptedPassword(password, salt);
    if (calculatedHash.equals(user.userEncryptedPassword)) {
      return user;
    }

    return null;
  }

  @Override
  public String getEncryptedPassword(String password, String salt) {
    String algorithm = "PBKDF2WithHmacSHA1";
    int derivedKeyLength = 160; // for SHA1
    int iterations = 20000; // NIST specifies 10000

    byte[] saltBytes = Base64.getDecoder().decode(salt);
    KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, iterations, derivedKeyLength);

    try {
      SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
      byte[] encBytes = f.generateSecret(spec).getEncoded();
      return Base64.getEncoder().encodeToString(encBytes);

    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
    }

    return null;
  }

  @Override
  public String getNewSalt() {
    // Don't use Random!
    try {
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      // NIST recommends minimum 4 bytes. We use 8.
      byte[] salt = new byte[8];
      random.nextBytes(salt);
      return Base64.getEncoder().encodeToString(salt);

    } catch (NoSuchAlgorithmException e) {

    }
    return null;
  }
}
