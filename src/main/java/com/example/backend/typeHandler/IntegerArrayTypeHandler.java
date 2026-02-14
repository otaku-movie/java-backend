package com.example.backend.typeHandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IntegerArrayTypeHandler extends BaseTypeHandler<List<Integer>> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, List<Integer> parameter, JdbcType jdbcType) throws SQLException {
    ps.setArray(i, ps.getConnection().createArrayOf("integer", parameter.toArray()));
  }

  @Override
  public List<Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    try {
      return convertArrayToList(rs.getArray(columnName));
    } catch (SQLException ex) {
      if (isNoResultError(ex)) {
        return parseFromString(rs.getString(columnName));
      }
      throw ex;
    }
  }

  @Override
  public List<Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    try {
      return convertArrayToList(rs.getArray(columnIndex));
    } catch (SQLException ex) {
      if (isNoResultError(ex)) {
        return parseFromString(rs.getString(columnIndex));
      }
      throw ex;
    }
  }

  @Override
  public List<Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    try {
      return convertArrayToList(cs.getArray(columnIndex));
    } catch (SQLException ex) {
      if (isNoResultError(ex)) {
        return parseFromString(cs.getString(columnIndex));
      }
      throw ex;
    }
  }

  private List<Integer> convertArrayToList(Array array) throws SQLException {
    if (array == null) {
      return null;
    }
    Object[] objectArray = (Object[]) array.getArray();
    List<Integer> result = new ArrayList<>();
    for (Object obj : objectArray) {
      result.add((Integer) obj);
    }
    return result;
  }

  private static boolean isNoResultError(SQLException ex) {
    String message = ex.getMessage();
    if (message == null) {
      return false;
    }
    return message.contains("No results were returned by the query")
        || message.contains("查询没有传回任何结果")
        || message.contains("The column name")
        || message.contains("was not found in this ResultSet");
  }

  private static List<Integer> parseFromString(String raw) {
    if (raw == null) {
      return null;
    }
    String text = raw.trim();
    if (text.isEmpty()) {
      return null;
    }
    if (text.startsWith("{") && text.endsWith("}")) {
      text = text.substring(1, text.length() - 1);
    }
    if (text.isEmpty()) {
      return null;
    }
    String[] parts = text.split(",");
    List<Integer> result = new ArrayList<>(parts.length);
    for (String part : parts) {
      String p = part.trim();
      if (p.isEmpty()) {
        continue;
      }
      try {
        result.add(Integer.parseInt(p));
      } catch (NumberFormatException ignore) {
        // ignore non-numeric entries
      }
    }
    return result.isEmpty() ? null : result;
  }
}
