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

import org.jdbcpersistence.MappedClass;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * @author Alex Rojkov Date: 8-Aug-2005 Time: 8:17:23 AM
 */
public final class SQLUtils
{
  public static void printTableDescriptor(MappedClass jdbcMap)
  {
    System.out.println("SQLUtils.printTableDescriptor primary key {");
    MappedClass.MappedAttribute[] identifyingColumns = jdbcMap.getIdentifyingColumns();
    for (int i = 0; i < identifyingColumns.length; i++) {
      final MappedClass.MappedAttribute column = identifyingColumns[i];
      final int type = column.getSqlType();
      System.out
        .println("\t [ " +
                 (i + 1) +
                 " ]\t[" +
                 column.getName() +
                 "] \t\t\t[" +
                 sqlTypeToSqlTypeName(type) +
                 "]");
    }
    System.out.println("}//primary key");
    System.out.println("SQLUtils.printTableDescriptor columns {");
    MappedClass.MappedAttribute[] columns = jdbcMap.getColumns();
    for (int i = 0; i < columns.length; i++) {
      MappedClass.MappedAttribute column = columns[i];
      final String columnName = column.getName();
      final int type = column.getSqlType();
      System.out
        .println("\t [ " +
                 (i + 1) +
                 " ]\t[" +
                 columnName +
                 "] \t\t\t[" +
                 sqlTypeToSqlTypeName(type) +
                 "\t\t\tversionControl [ " +
                 column.isVersionControlColumn() +
                 " ]" +
                 "]");
    }
    System.out.println("}//columns");
  }

  public static String sqlTypeToSqlTypeName(final int type)
  {
    try {
      final Integer typeValue = new Integer(type);
      final Field[] fields = Types.class.getDeclaredFields();
      for (int i = 0; i < fields.length; i++) {
        final Field field = fields[i];
        final Integer l = (Integer) field.get(null);
        if (typeValue.equals(l)) {
          return field.getName();
        }
      }
    } catch (IllegalAccessException e) {
    }

    return "" + type;
  }

  public static String[] getColumns(ResultSetMetaData rsMetaData)
    throws SQLException
  {
    int colcount = rsMetaData.getColumnCount();
    String[] result = new String[colcount];
    for (int i = 0; i < result.length; i++) {
      result[i] = rsMetaData.getColumnName(i + 1);
    }

    return result;
  }

  public static void close(final ResultSet rs, final Statement st)
  {
/*
        System.out.println("SQLUtils.close----------------------------");
        new Exception().printStackTrace();
*/
    if (rs != null) {
      try {
        rs.close();
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
    if (st != null) {
      try {
        st.close();
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

}
