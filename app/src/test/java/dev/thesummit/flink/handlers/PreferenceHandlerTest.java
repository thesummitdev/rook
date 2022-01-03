package dev.thesummit.flink.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import dev.thesummit.flink.database.FlinkDatabaseService;
import dev.thesummit.flink.models.Preference;
import dev.thesummit.flink.models.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PreferenceHandlerTest {

  @Mock private Context ctx;
  @Mock private FlinkDatabaseService dbService;
  private final UUID MOCK_USER_UUID = UUID.randomUUID();
  private final UUID MOCK_PREF_UUID = UUID.randomUUID();

  private PreferenceHandler handler;

  @BeforeEach
  public void init() {
    handler = new PreferenceHandler(dbService);

    User mockUser = new User("username", "userEncryptedPassword", "salt");
    mockUser.setId(MOCK_USER_UUID);

    doReturn(mockUser).when(ctx).sessionAttribute("current_user");
  }

  @Test
  public void getPrefs() {

    Preference mockPref = new Preference("samplePref", "sampleValue");
    Preference mockPref2 = new Preference("samplePref2", "sampleValue2");
    Preference mockPref3 = new Preference("samplePref3", "sampleValue3");
    Preference mockPref4 = new Preference("samplePref4", "sampleValue4");

    ArrayList<Preference> appPrefs = new ArrayList<Preference>();
    ArrayList<Preference> userPrefs = new ArrayList<Preference>();
    appPrefs.add(mockPref);
    appPrefs.add(mockPref2);
    userPrefs.add(mockPref3);
    userPrefs.add(mockPref4);

    doReturn(appPrefs, userPrefs).when(dbService).getAll(any(Class.class), any(HashMap.class));

    JSONArray expectedResult = new JSONArray();
    expectedResult.put(mockPref.toJSONObject());
    expectedResult.put(mockPref2.toJSONObject());
    expectedResult.put(mockPref3.toJSONObject());
    expectedResult.put(mockPref4.toJSONObject());

    handler.getAll(ctx);

    verify(dbService, times(2)).getAll(any(), any());
    verify(ctx).result(expectedResult.toString());
    verify(ctx).status(200);
    verify(ctx).contentType("application/json");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{'key':'userPref', 'value':'userValue'}",
        "{'key':'appVersion', 'value':'1.0'}",
      })
  public void create(String body) {

    JSONObject input = new JSONObject(body);

    doReturn(body).when(ctx).body();
    Preference expectedPref = new Preference(input.getString("key"), input.getString("value"));
    expectedPref.setId(MOCK_PREF_UUID);
    if (!Preference.applicationPrefs.contains(input.getString("key"))) {
      expectedPref.userId = MOCK_USER_UUID;
    }

    // Handle side effect of the database service setting the ID on the new preference with the id
    // that was returned from the database.
    doAnswer(
            invocation -> {
              Preference p = invocation.getArgument(0);
              p.setId(MOCK_PREF_UUID);
              return null;
            })
        .when(dbService)
        .put(any(Preference.class));

    ArgumentCaptor<Preference> arg = ArgumentCaptor.forClass(Preference.class);
    handler.create(ctx);

    verify(dbService).put(arg.capture());
    // Ensure all fields match the input
    assertEquals(Preference.class, arg.getValue().getClass());
    assertEquals(expectedPref.key, arg.getValue().key);
    assertEquals(expectedPref.value, arg.getValue().value);
    assertEquals(expectedPref.getId(), arg.getValue().getId());
    assertEquals(expectedPref.userId, arg.getValue().userId);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "{}",
        "{123:45}",
        "{'url':'notaurl'}",
      })
  public void CREATE_pref_invalid_body(String body) {
    doReturn(body).when(ctx).body();
    assertThrows(
        BadRequestResponse.class,
        () -> {
          handler.create(ctx);
        });
  }
}
