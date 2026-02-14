package com.example.backend.typeHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL JSONB 与 List&lt;Map&lt;String,Object&gt;&gt; 的互转。
 * 适用于存储 [{label,price}, …] 等简单 JSON 数组。
 */
public class JsonbTypeHandler extends BaseTypeHandler<List<Map<String, Object>>> {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final TypeReference<List<Map<String, Object>>> TYPE_REF =
      new TypeReference<>() {};

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i,
      List<Map<String, Object>> parameter, JdbcType jdbcType) throws SQLException {
    PGobject pg = new PGobject();
    pg.setType("jsonb");
    try {
      pg.setValue(MAPPER.writeValueAsString(parameter));
    } catch (JsonProcessingException e) {
      throw new SQLException("Failed to serialize priceItems to JSON", e);
    }
    ps.setObject(i, pg);
  }

  @Override
  public List<Map<String, Object>> getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return parse(rs.getString(columnName));
  }

  @Override
  public List<Map<String, Object>> getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return parse(rs.getString(columnIndex));
  }

  @Override
  public List<Map<String, Object>> getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return parse(cs.getString(columnIndex));
  }

  private static List<Map<String, Object>> parse(String json) throws SQLException {
    if (json == null || json.isEmpty()) {
      return null;
    }
    try {
      return MAPPER.readValue(json, TYPE_REF);
    } catch (JsonProcessingException e) {
      throw new SQLException("Failed to parse priceItems JSON", e);
    }
  }
}
