package dev.thesummit.rook.models;

import dev.thesummit.rook.database.DatabaseField;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.json.JSONObject;

public class User implements BaseModel {

  public User(String username, String userEncryptedPassword, String userSalt) {
    this.username = username;
    this.userEncryptedPassword = userEncryptedPassword;
    this.userSalt = userSalt;
  }

  @DatabaseField(isId = true)
  public UUID id;

  @DatabaseField(isIdentifier = true)
  public String username;

  @DatabaseField public String userEncryptedPassword;
  @DatabaseField public String userSalt;

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return this.id;
  }

  /**
   * Make a JSON compliant object from the link instance.
   *
   * @return The JSONObject.
   */
  public JSONObject toJSONObject() {
    JSONObject obj = new JSONObject().put("id", this.id).put("username", this.username);
    return obj;
  }

  public Boolean isValid() {
    return this.userSalt != null && this.userEncryptedPassword != null && this.username != null;
  }

  public static User fromResultSet(ResultSet rs) {
    try {
      User u =
          new User(
              rs.getString("username"),
              rs.getString("userencryptedpassword"),
              rs.getString("usersalt"));
      u.setId(rs.getObject("id", UUID.class));
      return u;
    } catch (SQLException e) {
      return null;
    }
  }
}