package com.example.backend.config;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.parser.JsqlParserSupport;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.example.backend.enumerate.DataScope;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * MyBatis-Plus 内联拦截器：仅在 <b>SELECT</b> 解析阶段为「带影院维度」的请求追加可见范围条件（{@code IN}
 * 列表），作为 PostgreSQL RLS 之外的<b>应用层双保险</b>（例如在误用 BYPASSRLS 连接时仍能收窄结果集）。
 *
 * <p>数据来源与 {@link RlsMybatisInterceptor} 相同：{@link RequestContextHolder} 中的 {@code level}、{@code
 * cinema_ids}（由 {@link AdminDataScopeRlsService} / 拦截器写入）。{@code platform} 或未配置 {@code
 * cinema_ids} 时不改写。{@code chain} 时 {@code cinema_ids} 已展开为该品牌下全部影院 id，与 RLS 一致，仅收窄到本品牌旗下数据。
 *
 * <p>仅对 {@link #SCOPE_TABLE_COLUMN} 中登记的主表名（不区分大小写）追加条件；未登记的表不处理，避免对无关 SQL 误伤。
 * 若某条 SQL 无法被 JSQLParser 解析，则记录告警并跳过改写，避免影响业务。
 */
@Slf4j
public class DataScopeSelectInnerInterceptor extends JsqlParserSupport implements InnerInterceptor {

  /**
   * 主表名（小写）→ 用于范围过滤的列名。与 migration RLS 涉及的含影院维度表保持一致，可按需扩展。
   */
  private static final Map<String, String> SCOPE_TABLE_COLUMN =
      new LinkedHashMap<>(
          Map.ofEntries(
              Map.entry("cinema", "id"),
              Map.entry("movie_order", "cinema_id"),
              Map.entry("movie_show_time", "cinema_id"),
              Map.entry("theater_hall", "cinema_id"),
              Map.entry("seat", "cinema_id"),
              Map.entry("select_seat", "cinema_id"),
              Map.entry("refund", "cinema_id"),
              Map.entry("cinema_price_config", "cinema_id"),
              Map.entry("movie_ticket_type", "cinema_id"),
              Map.entry("cinema_spec_spec", "cinema_id")));

  private static boolean isValidCinemaIdsList(String s) {
    return s != null && !s.isEmpty() && s.matches("^(\\d+,)*\\d+$");
  }

  @Override
  public void beforeQuery(
      Executor executor,
      MappedStatement ms,
      Object parameter,
      RowBounds rowBounds,
      @SuppressWarnings("rawtypes") ResultHandler resultHandler,
      BoundSql boundSql)
      throws SQLException {
    if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
      return;
    }
    String level = RequestContextHolder.getRls("level");
    if (level == null
        || level.isEmpty()
        || DataScope.PLATFORM.getCode().equals(level)) {
      return;
    }
    if (!DataScope.CHAIN.getCode().equals(level) && !DataScope.CINEMA.getCode().equals(level)) {
      return;
    }
    String cinemaIds = RequestContextHolder.getRls("cinema_ids");
    if (!isValidCinemaIdsList(cinemaIds)) {
      return;
    }
    PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
    String original = mpBs.sql();
    try {
      mpBs.sql(parserSingle(original, cinemaIds));
    } catch (Exception e) {
      log.warn("DataScope SELECT 改写跳过 mappedStatement={} : {}", ms.getId(), e.getMessage());
    }
  }

  @Override
  protected void processSelect(Select select, int index, String sql, Object obj) {
    String cinemaIdsCsv = (String) obj;
    processSelectBody(select.getSelectBody(), cinemaIdsCsv);
    List<WithItem> withItems = select.getWithItemsList();
    if (withItems != null) {
      for (WithItem w : withItems) {
        SubSelect sub = w.getSubSelect();
        if (sub != null && sub.getSelectBody() != null) {
          processSelectBody(sub.getSelectBody(), cinemaIdsCsv);
        }
      }
    }
  }

  private void processSelectBody(SelectBody selectBody, String cinemaIdsCsv) {
    if (selectBody instanceof PlainSelect) {
      appendScopeToPlainSelect((PlainSelect) selectBody, cinemaIdsCsv);
    } else if (selectBody instanceof SetOperationList) {
      for (SelectBody b : ((SetOperationList) selectBody).getSelects()) {
        processSelectBody(b, cinemaIdsCsv);
      }
    }
  }

  private void appendScopeToPlainSelect(PlainSelect plain, String cinemaIdsCsv) {
    List<Table> tables = new ArrayList<>();
    collectFromTables(plain.getFromItem(), tables);
    if (plain.getJoins() != null) {
      for (Join j : plain.getJoins()) {
        collectFromTables(j.getRightItem(), tables);
      }
    }
    List<Expression> parts = new ArrayList<>();
    for (Table t : tables) {
      String tableName = t.getName();
      if (tableName == null) {
        continue;
      }
      String col = SCOPE_TABLE_COLUMN.get(tableName.toLowerCase());
      if (col == null) {
        continue;
      }
      InExpression in = new InExpression(aliasColumn(t, col), idListExpression(cinemaIdsCsv));
      parts.add(in);
    }
    if (parts.isEmpty()) {
      return;
    }
    Expression combined = parts.get(0);
    for (int i = 1; i < parts.size(); i++) {
      combined = new AndExpression(combined, parts.get(i));
    }
    Expression oldWhere = plain.getWhere();
    plain.setWhere(oldWhere == null ? combined : new AndExpression(oldWhere, combined));
  }

  /**
   * 生成带限定名的列：有别名用 {@code 别名.列}，否则用 {@code 表名.列}。
   *
   * <p>不能省略表名：例如 {@code cinema} 与 {@code brand} 同 JOIN 时均有 {@code id}，写成裸 {@code id IN
   * (...)} 会在 PostgreSQL 中报「列参照曖昧」。
   */
  private static Column aliasColumn(Table table, String columnName) {
    StringBuilder sb = new StringBuilder();
    if (table.getAlias() != null && table.getAlias().getName() != null) {
      sb.append(table.getAlias().getName()).append('.');
    } else {
      String tableName = table.getName();
      if (tableName != null && !tableName.isEmpty()) {
        sb.append(tableName).append('.');
      }
    }
    sb.append(columnName);
    return new Column(sb.toString());
  }

  private static ExpressionList idListExpression(String csv) {
    ExpressionList list = new ExpressionList();
    List<Expression> exps = new ArrayList<>();
    for (String p : csv.split(",")) {
      String s = p.trim();
      if (s.isEmpty()) {
        continue;
      }
      exps.add(new LongValue(Long.parseLong(s)));
    }
    list.setExpressions(exps);
    return list;
  }

  private static void collectFromTables(FromItem fromItem, List<Table> out) {
    if (fromItem == null) {
      return;
    }
    if (fromItem instanceof Table) {
      out.add((Table) fromItem);
    } else if (fromItem instanceof SubSelect) {
      SelectBody inner = ((SubSelect) fromItem).getSelectBody();
      if (inner instanceof PlainSelect) {
        PlainSelect ps = (PlainSelect) inner;
        collectFromTables(ps.getFromItem(), out);
        if (ps.getJoins() != null) {
          for (Join j : ps.getJoins()) {
            collectFromTables(j.getRightItem(), out);
          }
        }
      } else if (inner instanceof SetOperationList) {
        for (SelectBody b : ((SetOperationList) inner).getSelects()) {
          if (b instanceof PlainSelect) {
            collectFromTables(((PlainSelect) b).getFromItem(), out);
          }
        }
      }
    }
  }

  @Override
  protected void processInsert(
      net.sf.jsqlparser.statement.insert.Insert insert, int index, String sql, Object obj) {
    // 仅处理 SELECT，其它语句不解析
  }

  @Override
  protected void processUpdate(
      net.sf.jsqlparser.statement.update.Update update, int index, String sql, Object obj) {
    // 仅处理 SELECT
  }

  @Override
  protected void processDelete(
      net.sf.jsqlparser.statement.delete.Delete delete, int index, String sql, Object obj) {
    // 仅处理 SELECT
  }
}
