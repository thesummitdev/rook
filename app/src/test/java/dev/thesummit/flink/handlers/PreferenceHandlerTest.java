package dev.thesummit.flink.handlers;

import static org.mockito.Mockito.*;

import dev.thesummit.flink.database.FlinkDatabaseService;
import dev.thesummit.flink.models.Preference;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PreferenceHandlerTest {

  @Mock private Context ctx;
  @Mock private FlinkDatabaseService dbService;

  private PreferenceHandler handler;

  @BeforeEach
  public void init() {
    handler = new PreferenceHandler(dbService);
  }

  @Test
  public void getPrefs() {

    Preference mockPref = new Preference("samplePref", "sampleValue");
    Preference mockPref2 = new Preference("samplePref2", "sampleValue2");
    Preference mockPref3 = new Preference("samplePref3", "sampleValue3");
    Preference mockPref4 = new Preference("samplePref4", "sampleValue4");

    ArrayList<Preference> list = new ArrayList<Preference>();
    list.add(mockPref);
    list.add(mockPref2);
    list.add(mockPref3);
    list.add(mockPref4);

    doReturn(list).when(dbService).getAll(any(Class.class), any(HashMap.class));

    JSONArray expectedResult = new JSONArray();
    expectedResult.put(mockPref.toJSONObject());
    expectedResult.put(mockPref2.toJSONObject());
    expectedResult.put(mockPref3.toJSONObject());
    expectedResult.put(mockPref4.toJSONObject());

    handler.getAll(ctx);

    verify(ctx).result(expectedResult.toString());
    verify(ctx).status(200);
    verify(ctx).contentType("application/json");
  }
}
