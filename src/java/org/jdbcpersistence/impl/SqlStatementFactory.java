package org.jdbcpersistence.impl;

import org.jdbcpersistence.MappedClass;

public class SqlStatementFactory
{
  public static final String makeSchemaClause(MappedClass jdbcMap)
  {
    String schema = jdbcMap.getSchema();
    if (null == schema || "".equals(schema)) {
      return "";
    }
    else {
      return schema + ".";
    }
  }

  public static final String makeInsert(MappedClass jdbcMap)
  {
    final StringBuffer sb = new StringBuffer("INSERT INTO ").append(
      makeSchemaClause(jdbcMap))
                                                            .
                                                              append(jdbcMap.getTableName())
                                                            .append(" (");
    final StringBuffer values = new StringBuffer();
    MappedClass.MappedAttribute[] columns = jdbcMap.getColumns();
    final int l = columns.length;
    for (int i = 0; i < l; i++) {
      final MappedClass.MappedAttribute column = columns[i];
      if (column.getGetter() == null) continue;
      sb.append(column.getName());
      values.append('?');
      sb.append(',');
      values.append(',');
    }
    sb.deleteCharAt(sb.length() - 1);
    values.deleteCharAt(values.length() - 1);
    sb.append(") VALUES (");
    sb.append(values);
    sb.append(')');
    return sb.toString();
  }

  public static final String makeUpdate(MappedClass jdbcMap)
  {
    return makeUpdate(jdbcMap.getRegularColumns(), jdbcMap);
  }

  public static final String makeUpdate(final MappedClass.MappedAttribute[] columns,
                                        MappedClass jdbcMap)
  {
    final StringBuffer sb = new StringBuffer("UPDATE ").append(makeSchemaClause(
      jdbcMap)).append(jdbcMap.getTableName()).append(" SET ");
    int l = columns.length;
    for (int i = 0; i < l; i++) {
      final MappedClass.MappedAttribute column = columns[i];
      if (column.getGetter() == null) continue;
      sb.append(column.getName()).append("=?");
      sb.append(',');
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append(" WHERE ");
    MappedClass.MappedAttribute[] identifyingColumns
      = jdbcMap.getIdentifyingColumns();
    if (identifyingColumns.length > 0) {
      l = identifyingColumns.length;
      for (int i = 0; i < l; i++) {
        final MappedClass.MappedAttribute column = identifyingColumns[i];
        sb.append(column.getName()).append("=?");
        if ((i + 1) < l) {
          sb.append(" AND ");
        }
      }
      MappedClass.MappedAttribute versionControlColumn
        = jdbcMap.getVersionControlColumn();
      if (versionControlColumn != null) {
        sb.append(" AND ").append(versionControlColumn.getName()).append("=?");
      }
    }
    else {
      throw new RuntimeException(
        "Tables without not nullable best row identifier are not supported");
    }
    return sb.toString();
  }

  public static final String makeSelect(MappedClass jdbcMap)
  {
    return makeSelect(jdbcMap.getRegularColumns(), null, jdbcMap);
  }

  public static final String makeSelect(final MappedClass.MappedAttribute[] columns,
                                        final String s,
                                        MappedClass jdbcMap)
  {
    final StringBuffer sb = new StringBuffer("SELECT ");

    int l = columns.length;

    if (l > 0) {
      for (int i = 0; i < l; i++) {
        final MappedClass.MappedAttribute column = columns[i];
        if (column.getSetter() == null) continue;
        sb.append(column.getName());
        sb.append(',');
      }
      sb.deleteCharAt(sb.length() - 1);
    }
    else {
      sb.append(" 1 ");
    }

    sb.append(" FROM ")
      .append(makeSchemaClause(jdbcMap))
      .append(jdbcMap.getTableName());
    sb.append(" WHERE ");
    MappedClass.MappedAttribute[] identifyingColumns
      = jdbcMap.getIdentifyingColumns();
    if (identifyingColumns.length > 0) {
      l = identifyingColumns.length;
      for (int i = 0; i < l; i++) {
        final MappedClass.MappedAttribute column = identifyingColumns[i];
        sb.append(column.getName()).append("=?");
        if ((i + 1) < l) {
          sb.append(" AND ");
        }
      }
    }
    else {
      throw new RuntimeException(
        "Tables without not nullable best row identifier are not supported");
    }
    if (s != null) {
      sb.append(s);
    }
    return sb.toString();
  }

  public static final String makeDelete(MappedClass jdbcMap)
  {
    final StringBuffer sb = new StringBuffer("DELETE FROM ").append(
      makeSchemaClause(jdbcMap))
                                                            .append(jdbcMap.getTableName())
                                                            .append(" WHERE ");
    MappedClass.MappedAttribute[] identifyingColumns
      = jdbcMap.getIdentifyingColumns();
    if (identifyingColumns.length > 0) {
      final int l = identifyingColumns.length;
      for (int i = 0; i < l; i++) {
        final String column = identifyingColumns[i].getName();
        sb.append(column).append("=?");
        if ((i + 1) < l) {
          sb.append(" AND ");
        }
      }
      MappedClass.MappedAttribute versionControlColumn
        = jdbcMap.getVersionControlColumn();
      if (versionControlColumn != null) {
        sb.append(" AND ").append(versionControlColumn.getName()).append("=?");
      }
    }
    else {
      throw new RuntimeException(
        "Tables without not nullable best row identifier are not supported");
    }
    return sb.toString();
  }
}