package com.example.backend.typeHandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL text[] / varchar[] 与 List&lt;String&gt; 的转换。
 */
public class StringArrayTypeHandler extends BaseTypeHandler<List<String>> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
    ps.setArray(i, ps.getConnection().createArrayOf("varchar", parameter.toArray()));
  }

  @Override
  public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    try {
      return arrayToList(rs.getArray(columnName));
    } catch (SQLException ex) {
      if (isNoResultError(ex)) {
        return null;
      }
      throw ex;
    }
  }

  @Override
  public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    try {
      return arrayToList(rs.getArray(columnIndex));
    } catch (SQLException ex) {
      if (isNoResultError(ex)) {
        return null;
      }
      throw ex;
    }
  }

  @Override
  public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    try {
      return arrayToList(cs.getArray(columnIndex));
    } catch (SQLException ex) {
      if (isNoResultError(ex)) {
        return null;
      }
      throw ex;
    }
  }

  private static List<String> arrayToList(java.sql.Array array) throws SQLException {
    if (array == null) {
      return null;
    }
    Object[] arr = (Object[]) array.getArray();
    List<String> list = new ArrayList<>(arr.length);
    for (Object o : arr) {
      list.add(o == null ? null : o.toString());
    }
    return list;
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
}
