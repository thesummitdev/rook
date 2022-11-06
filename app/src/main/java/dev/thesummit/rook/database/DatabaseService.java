package dev.thesummit.rook.database;

import dev.thesummit.rook.models.BaseModel;
import dev.thesummit.rook.models.PageableBaseModel;
import java.util.List;
import java.util.Map;

public interface DatabaseService {
  public void put(BaseModel entity);

  public void patch(BaseModel entity);

  public void delete(BaseModel entity);

  public <T extends BaseModel> T get(Class<T> cls, Integer id);

  public <T extends BaseModel> T get(Class<T> cls, String identifier);

  public <T extends BaseModel> List<T> getAll(Class<T> cls, Map<String, Object> params)
      throws IllegalArgumentException;

  public <T extends PageableBaseModel> PagedResults<T> getAllPaged(
      Class<T> cls, Map<String, Object> params) throws IllegalArgumentException;
}
