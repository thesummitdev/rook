package dev.thesummit.flink.database;

import dev.thesummit.flink.models.BaseModel;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DatabaseService {

  public void put(BaseModel entity);

  public void patch(BaseModel entity);

  public void delete(BaseModel entity);

  public <T extends BaseModel> T get(Class<T> cls, UUID id);

  public <T extends BaseModel> T get(Class<T> cls, String identifier);

  public <T extends BaseModel> List<T> getAll(Class<T> cls, Map<String, Object> params);
}
