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
    return convertArrayToList(rs.getArray(columnName));
  }

  @Override
  public List<Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return convertArrayToList(rs.getArray(columnIndex));
  }

  @Override
  public List<Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return convertArrayToList(cs.getArray(columnIndex));
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
}
