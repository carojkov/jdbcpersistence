/**
 * JDBCPersistence framework for java
 *   Copyright (C) 2004-2014 Alex Rojkov
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
 **/

package org.jdbcpersistence.impl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Rojkov Date: 20-Aug-2005 Time: 6:04:52 PM
 */
final class MethodsForPreparedStatement
{
  public static Method setBoolean;
  public static Method setByte;
  public static Method setDouble;
  public static Method setFloat;
  public static Method setInt;
  public static Method setLong;
  public static Method setShort;
  public static Method setTimestamp;
  public static Method setURL;
  public static Method setTime;
  public static Method setArray;
  public static Method setBigDecimal;
  public static Method setBlob;
  public static Method setBytes;
  public static Method setClob;
  public static Method setDate;
  public static Method setObject;
  public static Method setRef;
  public static Method setString;
  public static Method addBatch;
  public static Method executeQuery;
  public static Method execute;
  public static Method executeUpdate;
  public static Method executeBatch;
  public static Method getUpdateCount;
  public static Method setNull;

  public static Map<Class,Method> typeToSetter = new HashMap<Class,Method>();

  static {
    initialize();
  }

  private static void initialize()
  {
    try {
      initializeReflectedMethods();
      initializeTypeToSetter();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static void initializeReflectedMethods() throws NoSuchMethodException
  {
    setBoolean = PreparedStatement.class.getMethod("setBoolean",
                                                   new Class[]{int.class,
                                                               boolean.class});
    setByte = PreparedStatement.class.getMethod("setByte",
                                                new Class[]{int.class,
                                                            byte.class});
    setDouble = PreparedStatement.class.getMethod("setDouble",
                                                  new Class[]{int.class,
                                                              double.class});
    setFloat = PreparedStatement.class.getMethod("setFloat",
                                                 new Class[]{int.class,
                                                             float.class});
    setInt = PreparedStatement.class.getMethod("setInt",
                                               new Class[]{int.class,
                                                           int.class});
    setLong = PreparedStatement.class.getMethod("setLong",
                                                new Class[]{int.class,
                                                            long.class});
    setShort = PreparedStatement.class.getMethod("setShort",
                                                 new Class[]{int.class,
                                                             short.class});
    setTimestamp = PreparedStatement.class.getMethod("setTimestamp",
                                                     new Class[]{int.class,
                                                                 java.sql.Timestamp.class});
    setURL = PreparedStatement.class.getMethod("setURL",
                                               new Class[]{int.class,
                                                           java.net.URL.class});
    setTime = PreparedStatement.class.getMethod("setTime",
                                                new Class[]{int.class,
                                                            java.sql.Time.class});
    setArray = PreparedStatement.class.getMethod("setArray",
                                                 new Class[]{int.class,
                                                             java.sql.Array.class});
    setBigDecimal = PreparedStatement.class.getMethod("setBigDecimal",
                                                      new Class[]{int.class,
                                                                  java.math.BigDecimal.class});
    setBlob = PreparedStatement.class.getMethod("setBlob",
                                                new Class[]{int.class,
                                                            java.sql.Blob.class});
    setBytes = PreparedStatement.class.getMethod("setBytes",
                                                 new Class[]{int.class,
                                                             byte[].class});
    setClob = PreparedStatement.class.getMethod("setClob",
                                                new Class[]{int.class,
                                                            java.sql.Clob.class});
    setDate = PreparedStatement.class.getMethod("setDate",
                                                new Class[]{int.class,
                                                            java.sql.Date.class});
    setObject = PreparedStatement.class.getMethod("setObject",
                                                  new Class[]{int.class,
                                                              java.lang.Object.class});
    setRef = PreparedStatement.class.getMethod("setRef",
                                               new Class[]{int.class,
                                                           java.sql.Ref.class});
    setString = PreparedStatement.class.getMethod("setString",
                                                  new Class[]{int.class,
                                                              java.lang.String.class});
    executeQuery = PreparedStatement.class.getMethod("executeQuery",
                                                     new Class[0]);
    execute = PreparedStatement.class.getMethod("execute",
                                                new Class[0]);
    executeUpdate = PreparedStatement.class.getMethod("executeUpdate",
                                                      new Class[0]);
    executeBatch = Statement.class.getMethod("executeBatch",
                                             new Class[0]);
    getUpdateCount = Statement.class.getMethod("getUpdateCount",
                                               new Class[0]);
    addBatch = PreparedStatement.class.getMethod("addBatch",
                                                 new Class[0]);
    setNull = PreparedStatement.class.getMethod("setNull",
                                                new Class[]{int.class,
                                                            int.class});

  }

  private static void initializeTypeToSetter()
  {
    typeToSetter.put(boolean.class, setBoolean);
    typeToSetter.put(Boolean.class, setBoolean);

    typeToSetter.put(byte.class, setByte);
    typeToSetter.put(Byte.class, setByte);

    typeToSetter.put(byte[].class, setBytes);

    typeToSetter.put(short.class, setShort);
    typeToSetter.put(Short.class, setShort);

    typeToSetter.put(int.class, setInt);
    typeToSetter.put(Integer.class, setInt);

    typeToSetter.put(long.class, setLong);
    typeToSetter.put(Long.class, setLong);

    typeToSetter.put(float.class, setFloat);
    typeToSetter.put(Float.class, setFloat);

    typeToSetter.put(double.class, setDouble);
    typeToSetter.put(Double.class, setDouble);

    typeToSetter.put(BigDecimal.class, setBigDecimal);

    typeToSetter.put(String.class, setString);

    typeToSetter.put(Time.class, setTime);
    typeToSetter.put(Timestamp.class, setTimestamp);
    typeToSetter.put(Date.class, setDate);

    typeToSetter.put(URL.class, setURL);
  }

  static final Method findSetter(final Class type)
  {
    Method method = typeToSetter.get(type);

    if (method == null)
      throw new RuntimeException("Type [" +
                                 type +
                                 "] is not supported at this time");

    return method;
  }
}
