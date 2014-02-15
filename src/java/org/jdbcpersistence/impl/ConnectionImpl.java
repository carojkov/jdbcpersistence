/**
 * JDBCPersistence framework for java
 *   Copyright (C) 2004-2010 Alex Rojkov
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *    You can contact me by email jdbcpersistence   a t   gmail    d o t    com
 * */

package org.jdbcpersistence.impl;

import org.jdbcpersistence.Persistor;
import org.jdbcpersistence.Query;
import org.jdbcpersistence.ResultSetReader;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Alex Rojkov Date: 21-Aug-2005 Time: 4:05:49 PM
 */
final class ConnectionImpl extends ConnectionWrapper
  implements org.jdbcpersistence.Connection
{

  private static Logger _log
    = Logger.getLogger(ConnectionImpl.class.getName());

  private final PersistenceImpl _jdbcPersistence;

  ConnectionImpl(java.sql.Connection conn,
                 PersistenceImpl jdbcPersistence)
  {
    super(conn);

    this._jdbcPersistence = jdbcPersistence;
  }

  public int insert(Object object)
    throws SQLException
  {
    Persistor
      jdbcPersistor
      = _jdbcPersistence.getJdbcPersistor(object.getClass());
    return jdbcPersistor.insert(_conn, object);
  }

  public int[] insert(Object... batch)
    throws SQLException, BatchUpdateException
  {
    Object obj = batch[0];
    if (obj == null)
      throw new IllegalArgumentException("An array contains null reference");
    Class c = obj.getClass();
    Persistor jdbcPersistor = _jdbcPersistence.getJdbcPersistor(c);
    return jdbcPersistor.insert(_conn, batch);
  }

  public int update(Object object)
    throws SQLException
  {
    Persistor
      jdbcPersistor
      = _jdbcPersistence.getJdbcPersistor(object.getClass());
    return jdbcPersistor.update(_conn, object);
  }

  public int[] update(Object... objects)
    throws SQLException, BatchUpdateException
  {
    Object obj = objects[0];
    if (obj == null)
      throw new IllegalArgumentException("An array contains null reference");
    Class c = obj.getClass();
    Persistor jdbcPersistor = _jdbcPersistence.getJdbcPersistor(c);
    return jdbcPersistor.update(_conn, objects);
  }

  public int delete(Object object)
    throws SQLException
  {
    Persistor
      jdbcPersistor
      = _jdbcPersistence.getJdbcPersistor(object.getClass());
    return jdbcPersistor.delete(_conn, object);
  }

  public int[] delete(Object... batch)
    throws SQLException, BatchUpdateException
  {
    Object obj = batch[0];
    if (obj == null)
      throw new IllegalArgumentException("An array contains null reference");
    Class c = obj.getClass();
    Persistor jdbcPersistor = _jdbcPersistence.getJdbcPersistor(c);
    return jdbcPersistor.delete(_conn, batch);
  }

  @Override
  public <T> T load(Class<T> clazz, Object... primaryKey)
    throws SQLException
  {
    Persistor<T> jdbcPersistor = _jdbcPersistence.getJdbcPersistor(clazz);

    return jdbcPersistor.load(_conn, primaryKey);
  }

  public List executeQuery(Query query, List result, Object... params)
    throws SQLException
  {
    String q = query.getQuery();

    _log.finest("Executing query: " + query);

    result = (result == null ? new ArrayList() : result);

    Class resultClass = query.getResultType();
    PreparedStatement ps = null;
    ResultSet rs = null;
    ResultSetMetaData rsMeta;

    if (query.isNullable()) {

      char[] qChars = q.toCharArray();

      StringBuilder builder = new StringBuilder(qChars.length * 2);

      int paramIdx = 0;
      boolean isLiteral = false;
      boolean equalsPending = false;

      for (int i = 0; i < qChars.length; i++) {
        char qchar = qChars[i];
        char c = qChars[i];

        switch (qchar) {
        case '=': {
          if (isLiteral)
            builder.append('=');
          else
            equalsPending = true;

          break;
        }
        case '?': {
          if (isLiteral) {
            builder.append('?');
          }
          else if (params != null && !(paramIdx < params.length)) {
            throw new RuntimeException("Index '" +
                                       paramIdx +
                                       "' is out of bounds 0..'" +
                                       params.length +
                                       "'");
          }
          else if (params == null || params[paramIdx] == null) {
            builder.append(" IS NULL ");

            paramIdx++;
          }
          else {
            builder.append("= ?");

            equalsPending = false;
          }

          break;
        }
        case '\'': {
          if (!isLiteral) {
            if (equalsPending) {
              builder.append('=');
              equalsPending = false;
            }

            isLiteral = true;
          }
          else if (((i + 1) < qChars.length) && qChars[i + 1] == '\'') {
          }
          else
            isLiteral = false;

          builder.append('\'');

          break;
        }
        default: {
          builder.append(c);
        }
        }
      }

      q = builder.toString();
    }

    try {
      ps = _conn.prepareStatement(q);

      int paramIdx = 0;

      for (int i = 0; params != null && i < params.length; i++) {
        Object o = params[i];

        if (o == null && !query.isNullable())
          throw new RuntimeException(
            "Parameter '" + i + "' of a not nullable query is null"
          );
        else if (o != null)
          ps.setObject(++paramIdx, o);
      }

      rs = ps.executeQuery();

      if (java.util.List.class.equals(resultClass)) {
        while (rs.next())
          result.add(rs.getObject(1));

        return result;
      }
      else if (java.util.Map.class.equals(resultClass)) {
        rsMeta = rs.getMetaData();
        int colcount = rsMeta.getColumnCount();

        String[] columnNames = new String[colcount];

        for (int i = 0; i < columnNames.length; i++)
          columnNames[i] = rsMeta.getColumnName(i + 1);

        while (rs.next()) {
          Map map = new HashMap();
          for (int i = 0; i < colcount; i++)
            map.put(columnNames[i], rs.getObject(i + 1));

          result.add(map);
        }

        return result;
      }
      else {

        ResultSetReader resultSetReader
          = _jdbcPersistence.getJdbcQueryResultReader(query);

        if (resultSetReader == null) {
          rsMeta = rs.getMetaData();

          resultSetReader = _jdbcPersistence.getJdbcQueryResultReader(query,
                                                                      rsMeta);
        }

        return resultSetReader.read(rs, result);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      SQLUtils.close(rs, ps);
    }
  }

  public int executeUpdate(Query query, Object... params)
    throws SQLException
  {
    String q = query.getQuery();
    PreparedStatement ps = null;
    try {
      ps = _conn.prepareStatement(q);
      for (int i = 0; i < params.length; i++) {
        Object param = params[i];
        ps.setObject(i + 1, param);
      }
      return ps.executeUpdate();
    } catch (SQLException e) {
      throw e;
    } finally {
      SQLUtils.close(null, ps);
    }
  }

  public int[] executeBatchUpdate(Query query, Object[]... params)
    throws SQLException, BatchUpdateException
  {
    String q = query.getQuery();
    PreparedStatement ps = null;
    try {
      ps = _conn.prepareStatement(q);
      for (int i = 0; i < params.length; i++) {
        Object[] parameters = params[i];
        for (int j = 0; j < parameters.length; j++) {
          Object param = parameters[j];
          ps.setObject(j + 1, param);
        }
        ps.addBatch();
      }
      int[] result = ps.executeBatch();
      return result;
    } catch (java.sql.BatchUpdateException e) {
      throw e;
    } catch (SQLException e) {
      throw e;
    } finally {
      SQLUtils.close(null, ps);
    }
  }

  public void close()
    throws SQLException
  {
    try {
      _jdbcPersistence.close(this);
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private static String nullifyWhere(String query, int k)
  {
    StringBuffer result = new StringBuffer();
    char[] chars = query.toCharArray();
    int matchCounter = 0;
    int i;
    for (i = 0; i < chars.length; i++) {
      char aChar = chars[i];
      if (aChar == '=') {
        if (matchCounter == k) {
          break;
        }
        matchCounter++;
      }
    }
    int nextQuestioMark = query.indexOf('?', i);
    result.append(query.substring(0, i))
          .append(" IS NULL ")
          .append(query.substring(nextQuestioMark + 1));
    return result.toString();
  }
}
